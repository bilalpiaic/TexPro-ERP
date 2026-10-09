package com.example.data.auth

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "AuthManager"
private const val PREFS = "texpro_google_session"
private const val KEY_ID = "id"
private const val KEY_EMAIL = "email"
private const val KEY_NAME = "name"
private const val KEY_PHOTO = "photo"
private const val KEY_FIREBASE = "firebase"

const val GOOGLE_DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file"

sealed class GoogleSignInOutcome {
    data class Success(val account: SignedInAccount) : GoogleSignInOutcome()
    data class NeedsUserConsent(val pendingIntent: PendingIntent) : GoogleSignInOutcome()
    data class Cancelled(val message: String = "Google Sign-In was cancelled.") : GoogleSignInOutcome()
    data class Failure(val message: String) : GoogleSignInOutcome()
}

class AuthManager(private val context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val firebaseReady = isFirebaseReady(appContext)
    private val auth: FirebaseAuth? = if (firebaseReady) {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
            null
        }
    } else {
        null
    }
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(appContext) }

    private val _signedInAccount = MutableStateFlow(readPersistedAccount())
    val signedInAccount: StateFlow<SignedInAccount?> = _signedInAccount.asStateFlow()

    @Volatile
    var driveAccessToken: String? = null
        private set

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    init {
        auth?.currentUser?.let { firebaseUser ->
            val account = firebaseUser.toSignedInAccount()
            _signedInAccount.value = account
            persist(account)
        }
    }

    suspend fun beginSignIn(activity: Activity): GoogleSignInOutcome {
        val webClientId = resolveWebClientId()
        if (webClientId != null) {
            when (val credentialResult = signInWithCredentialManager(activity, webClientId)) {
                is GoogleSignInOutcome.Success -> {
                    return when (val drive = authorizeGoogleAndDrive(activity)) {
                        is GoogleSignInOutcome.NeedsUserConsent -> drive
                        is GoogleSignInOutcome.Failure -> credentialResult
                        else -> drive
                    }
                }
                is GoogleSignInOutcome.Cancelled -> return credentialResult
                is GoogleSignInOutcome.Failure -> {
                    Log.w(TAG, "Credential Manager did not complete: ${credentialResult.message}")
                }
                is GoogleSignInOutcome.NeedsUserConsent -> return credentialResult
            }
        }
        return authorizeGoogleAndDrive(activity)
    }

    suspend fun completeAuthorization(activity: Activity, data: Intent?): GoogleSignInOutcome {
        if (data == null) {
            return GoogleSignInOutcome.Cancelled()
        }
        return try {
            val authorizationResult = Identity.getAuthorizationClient(activity)
                .getAuthorizationResultFromIntent(data)
            handleAuthorizationResult(authorizationResult.accessToken)
        } catch (e: ApiException) {
            Log.w(TAG, "Authorization result failed: ${e.statusCode} ${e.message}")
            if (e.statusCode == 16 || e.statusCode == 12501) {
                GoogleSignInOutcome.Cancelled()
            } else {
                GoogleSignInOutcome.Failure(humanizeGoogleError(e))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Authorization completion failed: ${e.message}", e)
            GoogleSignInOutcome.Failure(e.message ?: "Google Sign-In did not finish.")
        }
    }

    suspend fun refreshDriveAccessToken(activity: Activity): GoogleSignInOutcome {
        return authorizeGoogleAndDrive(activity, preferSilent = true)
    }

    fun signOut() {
        auth?.signOut()
        driveAccessToken = null
        _signedInAccount.value = null
        prefs.edit().clear().apply()
    }

    /**
     * Deletes the Firebase Auth user. Caller must wipe Firestore profile first while
     * still authenticated. Re-authentication is required if the session is stale.
     */
    suspend fun deleteAccount(): Result<Unit> {
        val user = auth?.currentUser
            ?: return Result.failure(IllegalStateException("Not signed in with Firebase"))
        return try {
            user.delete().await()
            signOut()
            Result.success(Unit)
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Log.w(TAG, "Recent login required before account deletion")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Account deletion failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun signInWithCredentialManager(
        activity: Activity,
        webClientId: String
    ): GoogleSignInOutcome {
        return try {
            val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()
            val result = credentialManager.getCredential(activity, request)
            consumeGoogleIdCredential(result.credential)
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInOutcome.Cancelled()
        } catch (e: NoCredentialException) {
            tryGoogleIdOption(activity, webClientId, filterAuthorized = false)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Sign-In with Google button option failed: ${e.message}")
            tryGoogleIdOption(activity, webClientId, filterAuthorized = false)
        } catch (e: Exception) {
            Log.e(TAG, "Credential Manager sign-in failed: ${e.message}", e)
            GoogleSignInOutcome.Failure(e.message ?: "Google Sign-In failed.")
        }
    }

    private suspend fun tryGoogleIdOption(
        activity: Activity,
        webClientId: String,
        filterAuthorized: Boolean
    ): GoogleSignInOutcome {
        return try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(filterAuthorized)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()
            val result = credentialManager.getCredential(activity, request)
            consumeGoogleIdCredential(result.credential)
        } catch (e: NoCredentialException) {
            if (filterAuthorized) {
                tryGoogleIdOption(activity, webClientId, filterAuthorized = false)
            } else {
                GoogleSignInOutcome.Failure("No Google account is available on this device.")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInOutcome.Cancelled()
        } catch (e: Exception) {
            GoogleSignInOutcome.Failure(e.message ?: "Google account picker was not shown.")
        }
    }

    private suspend fun consumeGoogleIdCredential(
        credential: androidx.credentials.Credential
    ): GoogleSignInOutcome {
        if (credential !is CustomCredential || credential.type != TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return GoogleSignInOutcome.Failure("Unsupported Google credential type.")
        }
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        val firebaseUser = tryLinkFirebase(idToken)
        val account = firebaseUser?.toSignedInAccount()
            ?: SignedInAccount(
                id = googleIdTokenCredential.id.ifBlank { googleIdTokenCredential.idToken.take(24) },
                email = googleIdTokenCredential.id,
                displayName = googleIdTokenCredential.displayName
                    ?: googleIdTokenCredential.id.substringBefore("@"),
                photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                firebaseLinked = firebaseUser != null
            )
        _signedInAccount.value = account
        persist(account)
        return GoogleSignInOutcome.Success(account)
    }

    private suspend fun authorizeGoogleAndDrive(
        activity: Activity,
        preferSilent: Boolean = false
    ): GoogleSignInOutcome {
        return try {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(
                    listOf(
                        Scope("email"),
                        Scope("profile"),
                        Scope("openid"),
                        Scope(GOOGLE_DRIVE_FILE_SCOPE)
                    )
                )
                .build()
            val authorizationResult = Identity.getAuthorizationClient(activity)
                .authorize(request)
                .await()
            if (authorizationResult.hasResolution()) {
                val pendingIntent = authorizationResult.pendingIntent
                    ?: return GoogleSignInOutcome.Failure("Google Sign-In could not open the account picker.")
                GoogleSignInOutcome.NeedsUserConsent(pendingIntent)
            } else {
                handleAuthorizationResult(authorizationResult.accessToken)
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Authorization failed: ${e.statusCode} ${e.message}")
            if (e.statusCode == 16 || e.statusCode == 12501) {
                GoogleSignInOutcome.Cancelled()
            } else {
                GoogleSignInOutcome.Failure(humanizeGoogleError(e))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google authorization failed: ${e.message}", e)
            if (preferSilent) {
                GoogleSignInOutcome.Failure(e.message ?: "Could not refresh Google Drive access.")
            } else {
                GoogleSignInOutcome.Failure(
                    e.message ?: "Google Play services could not start Sign in with Google."
                )
            }
        }
    }

    private suspend fun handleAuthorizationResult(accessToken: String?): GoogleSignInOutcome {
        if (accessToken.isNullOrBlank()) {
            return GoogleSignInOutcome.Failure("Google did not return an access token.")
        }
        driveAccessToken = accessToken
        return try {
            val account = fetchUserInfo(accessToken)
            _signedInAccount.value = account
            persist(account)
            GoogleSignInOutcome.Success(account)
        } catch (e: Exception) {
            Log.e(TAG, "User info failed: ${e.message}", e)
            val fallback = SignedInAccount(
                id = "google",
                email = "",
                displayName = "Google account",
                firebaseLinked = false
            )
            _signedInAccount.value = fallback
            persist(fallback)
            GoogleSignInOutcome.Success(fallback)
        }
    }

    private suspend fun fetchUserInfo(accessToken: String): SignedInAccount = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://www.googleapis.com/oauth2/v3/userinfo")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()
        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Google user info failed (${response.code}).")
            }
            val json = JSONObject(body)
            SignedInAccount(
                id = json.optString("sub").ifBlank { json.optString("email") },
                email = json.optString("email"),
                displayName = json.optString("name").ifBlank { json.optString("email") },
                photoUrl = json.optString("picture").takeIf { it.isNotBlank() },
                firebaseLinked = currentUser != null
            )
        }
    }

    private suspend fun tryLinkFirebase(idToken: String): FirebaseUser? {
        val firebaseAuth = auth ?: return null
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(firebaseCredential).await().user
        } catch (e: Exception) {
            Log.w(TAG, "Firebase link skipped: ${e.message}")
            null
        }
    }

    private fun resolveWebClientId(): String? {
        val resId = appContext.resources.getIdentifier(
            "default_web_client_id",
            "string",
            appContext.packageName
        )
        if (resId == 0) return null
        val value = appContext.getString(resId)
        return value.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") && !it.startsWith("REPLACE") }
    }

    private fun persist(account: SignedInAccount) {
        prefs.edit()
            .putString(KEY_ID, account.id)
            .putString(KEY_EMAIL, account.email)
            .putString(KEY_NAME, account.displayName)
            .putString(KEY_PHOTO, account.photoUrl)
            .putBoolean(KEY_FIREBASE, account.firebaseLinked)
            .apply()
    }

    private fun readPersistedAccount(): SignedInAccount? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_NAME, "") ?: ""
        if (id.isBlank() && email.isBlank()) return null
        return SignedInAccount(
            id = id.ifBlank { email },
            email = email,
            displayName = name.ifBlank { email },
            photoUrl = prefs.getString(KEY_PHOTO, null),
            firebaseLinked = prefs.getBoolean(KEY_FIREBASE, false)
        )
    }

    private fun FirebaseUser.toSignedInAccount(): SignedInAccount {
        return SignedInAccount(
            id = uid,
            email = email.orEmpty(),
            displayName = displayName ?: email?.substringBefore("@") ?: "Google account",
            photoUrl = photoUrl?.toString(),
            firebaseLinked = true
        )
    }

    private fun humanizeGoogleError(e: ApiException): String {
        return when (e.statusCode) {
            10 -> "This APK is not registered for Google Sign-In. Add the debug SHA-1 in Google Cloud / Firebase for package ${appContext.packageName}."
            7 -> "Network error. Check the connection and try Sign in with Google again."
            else -> e.message ?: "Google Sign-In failed (status ${e.statusCode})."
        }
    }
}
