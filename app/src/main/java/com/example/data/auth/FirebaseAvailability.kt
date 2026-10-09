package com.example.data.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

private const val TAG = "FirebaseAvailability"

/**
 * Sideload APKs are built without `google-services.json`. FirebaseAuth.getInstance()
 * then throws and the activity is killed as soon as the ViewModel is created.
 */
fun isFirebaseReady(context: Context): Boolean {
    return try {
        if (FirebaseApp.getApps(context).isNotEmpty()) return true
        FirebaseApp.initializeApp(context) != null
    } catch (e: Exception) {
        Log.w(TAG, "Firebase is not configured; mill ledger will run offline. ${e.message}")
        false
    }
}
