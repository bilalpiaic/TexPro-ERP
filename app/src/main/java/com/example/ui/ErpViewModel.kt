package com.example.ui

import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.auth.GoogleSignInOutcome
import com.example.data.auth.SignedInAccount
import com.example.data.auth.isFirebaseReady
import com.example.data.local.AppDatabase
import com.example.data.model.AccountWithBalance
import com.example.data.model.BalanceSheetData
import com.example.data.model.FinancialHealthRatios
import com.example.data.model.IncomeStatementData
import com.example.data.model.LotEntity
import com.example.data.model.OrgMemberRole
import com.example.data.model.OrganizationEntity
import com.example.data.model.SaaSUserProfile
import com.example.data.model.SaleOrderEntity
import com.example.data.model.TextileInventorySummary
import com.example.data.model.TrialBalanceData
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherLineEntity
import com.example.data.model.VoucherType
import com.example.data.model.VoucherWithLines
import com.example.data.repository.ErpRepository
import com.example.data.repository.FirestoreSaasRepository
import com.example.data.repository.GoogleDriveLedgerStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ErpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ErpRepository(AppDatabase.getInstance(application))
    private val authManager = AuthManager(application)
    private val saasRepository = FirestoreSaasRepository(application)
    private val driveStore = GoogleDriveLedgerStore()

    // SaaS Organization State
    val allOrganizations: StateFlow<List<OrganizationEntity>> = repository.allOrganizations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentOrgId = MutableStateFlow("org_default")
    val currentOrgId: StateFlow<String> = _currentOrgId.asStateFlow()

    val currentOrganization: StateFlow<OrganizationEntity?> = combine(allOrganizations, _currentOrgId) { orgs, id ->
        orgs.find { it.id == id } ?: orgs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentUserRole = MutableStateFlow(OrgMemberRole.OWNER)
    val currentUserRole: StateFlow<OrgMemberRole> = _currentUserRole.asStateFlow()

    val signedInAccount: StateFlow<SignedInAccount?> = authManager.signedInAccount

    private val _pendingGoogleConsent = MutableStateFlow<PendingIntent?>(null)
    val pendingGoogleConsent: StateFlow<PendingIntent?> = _pendingGoogleConsent.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    // Cloud Sync State
    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    val accountsWithBalances: StateFlow<List<AccountWithBalance>> = repository.accountsWithBalances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVouchers: StateFlow<List<VoucherWithLines>> = repository.allVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saleOrders: StateFlow<List<SaleOrderEntity>> = repository.saleOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lots: StateFlow<List<LotEntity>> = repository.lots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trialBalanceData: StateFlow<TrialBalanceData?> = repository.trialBalanceData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val balanceSheetData: StateFlow<BalanceSheetData?> = repository.balanceSheetData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val incomeStatementData: StateFlow<IncomeStatementData?> = repository.incomeStatementData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inventorySummary: StateFlow<TextileInventorySummary?> = repository.inventorySummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val financialHealthRatios: StateFlow<FinancialHealthRatios?> = repository.financialHealthRatios
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage = _userFeedbackMessage.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeSeedDataIfEmpty()
        }
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    fun createSaleOrder(
        orderNumber: String,
        customerName: String,
        itemDescription: String,
        quality: String,
        blend: String,
        width: String,
        pieces: Int,
        targetMeters: Double,
        unitPrice: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.createSaleOrder(
                SaleOrderEntity(
                    orderNumber = orderNumber,
                    customerName = customerName,
                    itemDescription = itemDescription,
                    quality = quality,
                    blend = blend,
                    width = width,
                    orderedPieces = pieces,
                    targetMeters = targetMeters,
                    unitPrice = unitPrice,
                    notes = notes
                )
            )
            _userFeedbackMessage.value = "Sale Order $orderNumber registered for $customerName"
        }
    }

    fun purchaseGreyClothForLot(
        saleOrderId: Long,
        saleOrderNumber: String,
        customerName: String,
        lotNumber: String,
        quality: String,
        blend: String,
        width: String,
        greyVendor: String,
        meters: Double,
        ratePerMeter: Double
    ) {
        viewModelScope.launch {
            repository.purchaseGreyCloth(
                saleOrderId = saleOrderId,
                saleOrderNumber = saleOrderNumber,
                customerName = customerName,
                lotNumber = lotNumber,
                quality = quality,
                blend = blend,
                width = width,
                greyVendor = greyVendor,
                meters = meters,
                ratePerMeter = ratePerMeter
            )
            _userFeedbackMessage.value = "Lot $lotNumber created! Purchase Voucher (PV) posted for $meters m from $greyVendor"
        }
    }

    fun sendGreyToProcessor(
        lot: LotEntity,
        processorName: String,
        processType: String,
        ratePerMeter: Double
    ) {
        viewModelScope.launch {
            repository.sendGreyToProcessor(lot, processorName, processType, ratePerMeter)
            _userFeedbackMessage.value = "${lot.lotNumber} sent to $processorName. Journal Voucher (JV) posted to WIP."
        }
    }

    fun receiveFromProcessor(
        lot: LotEntity,
        receivedMeters: Double
    ) {
        viewModelScope.launch {
            repository.receiveFromProcessor(lot, receivedMeters)
            _userFeedbackMessage.value = "${lot.lotNumber} received from processor ($receivedMeters m). Processing bill booked!"
        }
    }

    fun issueToStitcherAndReceive(
        lot: LotEntity,
        stitcherName: String,
        finishedUnits: Int,
        ratePerUnit: Double
    ) {
        viewModelScope.launch {
            repository.issueToStitcherAndReceive(lot, stitcherName, finishedUnits, ratePerUnit)
            _userFeedbackMessage.value = "${lot.lotNumber} stitched! $finishedUnits sets added to Finished Goods inventory."
        }
    }

    fun dispatchAndInvoiceCustomer(
        lot: LotEntity,
        saleRatePerUnit: Double
    ) {
        viewModelScope.launch {
            repository.dispatchAndInvoiceCustomer(lot, saleRatePerUnit)
            _userFeedbackMessage.value = "Dispatched ${lot.finishedUnits} sets for ${lot.customerName}! Sale Voucher (SV) & COGS posted to General Ledger."
        }
    }

    fun recordCashBankVoucher(
        voucherType: VoucherType,
        partyName: String,
        amount: Double,
        description: String,
        reference: String,
        targetAccountId: Long,
        lotNumber: String = ""
    ) {
        viewModelScope.launch {
            repository.recordCashBankVoucher(
                voucherType = voucherType,
                partyName = partyName,
                amount = amount,
                description = description,
                reference = reference,
                targetAccountId = targetAccountId,
                lotNumber = lotNumber
            )
            _userFeedbackMessage.value = "${voucherType.displayName} ($amount) posted to General Ledger!"
        }
    }

    fun postCustomVoucher(
        voucher: VoucherEntity,
        lines: List<VoucherLineEntity>
    ) {
        val totalDr = lines.sumOf { it.debit }
        val totalCr = lines.sumOf { it.credit }
        if (kotlin.math.abs(totalDr - totalCr) > 0.01) {
            _userFeedbackMessage.value = "Error: Unbalanced entry! Total Debits ($totalDr) must equal Total Credits ($totalCr)."
            return
        }

        viewModelScope.launch {
            repository.postVoucher(voucher, lines)
            _userFeedbackMessage.value = "Voucher ${voucher.voucherNumber} (${voucher.voucherType.code}) posted successfully!"
        }
    }

    fun switchOrganization(orgId: String) {
        _currentOrgId.value = orgId
        val org = allOrganizations.value.find { it.id == orgId }
        _userFeedbackMessage.value = "Switched to Organization: ${org?.name ?: orgId}"
    }

    fun createNewOrganization(
        name: String,
        code: String,
        taxId: String,
        millAddress: String,
        currency: String = "Rs."
    ) {
        viewModelScope.launch {
            val orgId = "org_${System.currentTimeMillis() % 100000}"
            val user = signedInAccount.value
            val ownerUid = user?.id ?: "local_admin"
            val newOrg = OrganizationEntity(
                id = orgId,
                name = name,
                code = code,
                ownerUid = ownerUid,
                currency = currency,
                taxId = taxId,
                millAddress = millAddress,
                contactEmail = user?.email ?: ""
            )
            repository.insertOrganization(newOrg)
            _currentOrgId.value = orgId

            if (user != null && isFirebaseReady(getApplication()) && authManager.currentUser != null) {
                val profile = SaaSUserProfile(
                    uid = user.id,
                    email = user.email,
                    displayName = user.displayName.ifBlank { user.email },
                    activeOrgId = orgId,
                    joinedOrgIds = listOf(orgId)
                )
                saasRepository.createOrganization(newOrg, profile)
            }

            _userFeedbackMessage.value = "Organization '$name' ($code) created successfully!"
        }
    }

    fun notifyGoogleSignInUnavailable() {
        _userFeedbackMessage.value = "Could not open Google Sign-In. Close this sheet and tap the company header again."
    }

    fun startGoogleSignIn(activity: Activity) {
        viewModelScope.launch {
            _isSigningIn.value = true
            try {
                applyGoogleOutcome(authManager.beginSignIn(activity), welcome = true)
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    fun consumeGoogleConsent() {
        _pendingGoogleConsent.value = null
    }

    fun finishGoogleSignIn(activity: Activity, data: Intent?) {
        viewModelScope.launch {
            _isSigningIn.value = true
            try {
                applyGoogleOutcome(authManager.completeAuthorization(activity, data), welcome = true)
            } finally {
                _isSigningIn.value = false
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _userFeedbackMessage.value = "Signed out of Google. Mill books stay on this device."
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val user = signedInAccount.value
            if (user == null) {
                _userFeedbackMessage.value = "Sign in first to delete a cloud account."
                return@launch
            }
            try {
                if (isFirebaseReady(getApplication()) && authManager.currentUser != null) {
                    saasRepository.deleteUserCloudData(authManager.currentUser!!.uid)
                    val result = authManager.deleteAccount()
                    result.onSuccess {
                        _userFeedbackMessage.value = "Account deleted. Local mill data on this device was kept."
                    }.onFailure { err ->
                        _userFeedbackMessage.value =
                            "Cloud profile removed, but Google requires a recent sign-in to finish deleting the account. Sign in again, then retry. (${err.message})"
                    }
                } else {
                    authManager.signOut()
                    _userFeedbackMessage.value = "Google session cleared on this device. Local mill data was kept."
                }
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Could not delete account: ${e.message}"
            }
        }
    }

    fun saveActiveOrgToDrive(activity: Activity) {
        viewModelScope.launch {
            val org = currentOrganization.value
            if (signedInAccount.value == null) {
                _userFeedbackMessage.value = "Sign in with Google first to save this mill on Drive."
                return@launch
            }
            if (org == null) {
                _userFeedbackMessage.value = "Create or select an organization first."
                return@launch
            }
            _isCloudSyncing.value = true
            try {
                val token = ensureDriveToken(activity) ?: return@launch
                val snapshot = repository.exportLedgerSnapshot(org)
                driveStore.saveSnapshot(token, snapshot)
                _userFeedbackMessage.value = "Saved ${org.name} to Google Drive folder TexPro ERP / ${org.code} - ${org.name}."
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Google Drive save failed: ${e.message}"
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun loadActiveOrgFromDrive(activity: Activity) {
        viewModelScope.launch {
            val org = currentOrganization.value
            if (signedInAccount.value == null) {
                _userFeedbackMessage.value = "Sign in with Google first to load mill books from Drive."
                return@launch
            }
            if (org == null) {
                _userFeedbackMessage.value = "Create or select an organization first."
                return@launch
            }
            _isCloudSyncing.value = true
            try {
                val token = ensureDriveToken(activity) ?: return@launch
                val snapshot = driveStore.loadSnapshot(token, org)
                if (snapshot == null) {
                    _userFeedbackMessage.value = "No TexPro ledger found in Google Drive for ${org.code}."
                    return@launch
                }
                repository.replaceLedgerFromSnapshot(snapshot)
                _currentOrgId.value = snapshot.organization.id
                _userFeedbackMessage.value = "Loaded ${snapshot.organization.name} from Google Drive."
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Google Drive load failed: ${e.message}"
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun syncCurrentTenantWithCloud() {
        viewModelScope.launch {
            val org = currentOrganization.value ?: return@launch
            if (signedInAccount.value == null) {
                _userFeedbackMessage.value = "Sign in with Google to sync this organization."
                return@launch
            }
            _isCloudSyncing.value = true
            try {
                if (isFirebaseReady(getApplication()) && authManager.currentUser != null) {
                    _userFeedbackMessage.value = "Syncing ${org.name} to Firestore..."
                    saasRepository.syncTenantDataToCloud(
                        orgId = org.id,
                        accounts = repository.rawAccounts.first(),
                        vouchers = repository.allVouchers.first(),
                        lots = repository.lots.first(),
                        saleOrders = repository.saleOrders.first()
                    )
                    _userFeedbackMessage.value = "Cloud sync complete for ${org.name}."
                } else {
                    _userFeedbackMessage.value = "Firebase is not configured. Use Save mill to Google Drive instead."
                }
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Cloud sync failed: ${e.message}"
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    private suspend fun ensureDriveToken(activity: Activity): String? {
        authManager.driveAccessToken?.let { return it }
        return when (val outcome = authManager.refreshDriveAccessToken(activity)) {
            is GoogleSignInOutcome.Success -> authManager.driveAccessToken
            is GoogleSignInOutcome.NeedsUserConsent -> {
                _pendingGoogleConsent.value = outcome.pendingIntent
                _userFeedbackMessage.value = "Allow Drive access in the Google prompt, then tap Save / Load again."
                null
            }
            is GoogleSignInOutcome.Cancelled -> {
                _userFeedbackMessage.value = outcome.message
                null
            }
            is GoogleSignInOutcome.Failure -> {
                _userFeedbackMessage.value = outcome.message
                null
            }
        }
    }

    private suspend fun applyGoogleOutcome(outcome: GoogleSignInOutcome, welcome: Boolean) {
        when (outcome) {
            is GoogleSignInOutcome.Success -> {
                val account = outcome.account
                if (welcome) {
                    val label = account.displayName.ifBlank { account.email }.ifBlank { "Google account" }
                    _userFeedbackMessage.value = "Welcome $label. Mill books can be saved to your Google Drive."
                }
                val firebaseUser = authManager.currentUser
                if (firebaseUser != null && isFirebaseReady(getApplication())) {
                    val currentId = _currentOrgId.value
                    saasRepository.saveUserProfile(
                        SaaSUserProfile(
                            uid = firebaseUser.uid,
                            email = account.email,
                            displayName = account.displayName,
                            activeOrgId = currentId,
                            joinedOrgIds = listOf(currentId)
                        )
                    )
                }
            }
            is GoogleSignInOutcome.NeedsUserConsent -> {
                _pendingGoogleConsent.value = outcome.pendingIntent
            }
            is GoogleSignInOutcome.Cancelled -> {
                _userFeedbackMessage.value = outcome.message
            }
            is GoogleSignInOutcome.Failure -> {
                _userFeedbackMessage.value = outcome.message
            }
        }
    }
}
