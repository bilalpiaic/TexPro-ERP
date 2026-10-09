package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
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
import com.google.firebase.auth.FirebaseUser
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

    // Authentication State (Google Sign-In)
    val currentUser: StateFlow<FirebaseUser?> = authManager.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authManager.currentUser)

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
            val user = currentUser.value
            val ownerUid = user?.uid ?: "local_admin"
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

            if (user != null) {
                val profile = SaaSUserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: user.email ?: "User",
                    activeOrgId = orgId,
                    joinedOrgIds = listOf(orgId)
                )
                saasRepository.createOrganization(newOrg, profile)
            }

            _userFeedbackMessage.value = "Organization '$name' ($code) created successfully!"
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(activity)
            result.onSuccess { user ->
                _userFeedbackMessage.value = "Welcome ${user.displayName ?: user.email}! Signed in with Google."
                // Save user profile in Firestore
                val currentId = _currentOrgId.value
                val profile = SaaSUserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    activeOrgId = currentId,
                    joinedOrgIds = listOf(currentId)
                )
                saasRepository.saveUserProfile(profile)
            }.onFailure { err ->
                _userFeedbackMessage.value = "Sign in was not completed: ${err.message}"
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        _userFeedbackMessage.value = "Signed out of TexPro Cloud."
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                _userFeedbackMessage.value = "Sign in first to delete a cloud account."
                return@launch
            }
            try {
                saasRepository.deleteUserCloudData(user.uid)
                val result = authManager.deleteAccount()
                result.onSuccess {
                    _userFeedbackMessage.value = "Account deleted. Local mill data on this device was kept."
                }.onFailure { err ->
                    _userFeedbackMessage.value =
                        "Cloud profile removed, but Google requires a recent sign-in to finish deleting the account. Sign in again, then retry. (${err.message})"
                }
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Could not delete account: ${e.message}"
            }
        }
    }

    fun syncCurrentTenantWithCloud() {
        viewModelScope.launch {
            val org = currentOrganization.value ?: return@launch
            if (currentUser.value == null) {
                _userFeedbackMessage.value = "Sign in with Google to sync this organization to the cloud."
                return@launch
            }
            _isCloudSyncing.value = true
            _userFeedbackMessage.value = "Syncing ${org.name} to Firestore..."
            try {
                saasRepository.syncTenantDataToCloud(
                    orgId = org.id,
                    accounts = repository.rawAccounts.first(),
                    vouchers = repository.allVouchers.first(),
                    lots = repository.lots.first(),
                    saleOrders = repository.saleOrders.first()
                )
                _userFeedbackMessage.value = "Cloud sync complete for ${org.name}."
            } catch (e: Exception) {
                _userFeedbackMessage.value = "Cloud sync failed: ${e.message}"
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }
}
