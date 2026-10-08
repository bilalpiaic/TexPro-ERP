package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.SaleOrderEntity
import com.example.data.model.VoucherType
import com.example.ui.ErpViewModel
import com.example.ui.components.OrganizationTenantDialog
import com.example.ui.components.OrganizationTopHeader
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeneralLedgerReportsScreen
import com.example.ui.screens.LotsWorkflowScreen
import com.example.ui.screens.SaleOrdersScreen
import com.example.ui.screens.VouchersJournalScreen
import com.example.ui.theme.TexProErpTheme

enum class MainNavTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    LOTS("Lots Flow", Icons.Default.Timeline, "tab_lots"),
    VOUCHERS("Journal", Icons.Default.ReceiptLong, "tab_vouchers"),
    SALE_ORDERS("Sale Orders", Icons.Default.Assignment, "tab_so"),
    REPORTS("Reports", Icons.Default.AccountBalanceWallet, "tab_reports")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TexProErpTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    erpViewModel: ErpViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(MainNavTab.DASHBOARD) }
    var preselectedVoucherType by remember { mutableStateOf<VoucherType?>(null) }
    var showOrgDialog by remember { mutableStateOf(false) }

    val accounts by erpViewModel.accountsWithBalances.collectAsStateWithLifecycle()
    val vouchers by erpViewModel.allVouchers.collectAsStateWithLifecycle()
    val saleOrders by erpViewModel.saleOrders.collectAsStateWithLifecycle()
    val lots by erpViewModel.lots.collectAsStateWithLifecycle()
    val trialBalance by erpViewModel.trialBalanceData.collectAsStateWithLifecycle()
    val balanceSheet by erpViewModel.balanceSheetData.collectAsStateWithLifecycle()
    val incomeStatement by erpViewModel.incomeStatementData.collectAsStateWithLifecycle()
    val inventorySummary by erpViewModel.inventorySummary.collectAsStateWithLifecycle()
    val healthRatios by erpViewModel.financialHealthRatios.collectAsStateWithLifecycle()
    val feedbackMessage by erpViewModel.userFeedbackMessage.collectAsStateWithLifecycle()

    val currentOrg by erpViewModel.currentOrganization.collectAsStateWithLifecycle()
    val allOrgs by erpViewModel.allOrganizations.collectAsStateWithLifecycle()
    val currentUser by erpViewModel.currentUser.collectAsStateWithLifecycle()
    val userRole by erpViewModel.currentUserRole.collectAsStateWithLifecycle()
    val isSyncing by erpViewModel.isCloudSyncing.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            erpViewModel.clearFeedbackMessage()
        }
    }

    if (showOrgDialog) {
        OrganizationTenantDialog(
            allOrganizations = allOrgs,
            currentOrg = currentOrg,
            currentUser = currentUser,
            userRole = userRole,
            isSyncing = isSyncing,
            onDismiss = { showOrgDialog = false },
            onSwitchOrg = { orgId ->
                erpViewModel.switchOrganization(orgId)
                showOrgDialog = false
            },
            onCreateOrg = { name, code, taxId, millAddress, currency ->
                erpViewModel.createNewOrganization(name, code, taxId, millAddress, currency)
            },
            onSignInWithGoogle = { activity ->
                erpViewModel.signInWithGoogle(activity)
            },
            onSignOut = {
                erpViewModel.signOut()
            },
            onSyncCloud = {
                erpViewModel.syncCurrentTenantWithCloud()
            },
            onDeleteAccount = {
                erpViewModel.deleteAccount()
            }
        )
    }

    // BackHandler: return to Dashboard if on any sub-tab
    BackHandler(enabled = currentTab != MainNavTab.DASHBOARD) {
        currentTab = MainNavTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            OrganizationTopHeader(
                currentOrg = currentOrg,
                userRole = userRole,
                currentUser = currentUser,
                isSyncing = isSyncing,
                onOpenOrgDialog = { showOrgDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                MainNavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                        label = {
                            Text(
                                text = tab.label,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            MainNavTab.DASHBOARD -> DashboardScreen(
                balanceSheet = balanceSheet,
                incomeStatement = incomeStatement,
                inventorySummary = inventorySummary,
                healthRatios = healthRatios,
                lots = lots,
                saleOrders = saleOrders,
                recentVouchers = vouchers,
                onNavigateToLots = { currentTab = MainNavTab.LOTS },
                onNavigateToVouchers = {
                    preselectedVoucherType = null
                    currentTab = MainNavTab.VOUCHERS
                },
                onNavigateToSaleOrders = { currentTab = MainNavTab.SALE_ORDERS },
                onNavigateToReports = { currentTab = MainNavTab.REPORTS },
                onOpenNewVoucherDialog = { vType ->
                    preselectedVoucherType = vType
                    currentTab = MainNavTab.VOUCHERS
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainNavTab.LOTS -> LotsWorkflowScreen(
                lots = lots,
                saleOrders = saleOrders,
                onPurchaseGreyCloth = { soId, soNum, cust, lotNum, qual, blend, width, vendor, meters, rate ->
                    erpViewModel.purchaseGreyClothForLot(soId, soNum, cust, lotNum, qual, blend, width, vendor, meters, rate)
                },
                onSendToProcessor = { lot, procName, pType, rate ->
                    erpViewModel.sendGreyToProcessor(lot, procName, pType, rate)
                },
                onReceiveFromProcessor = { lot, recMeters ->
                    erpViewModel.receiveFromProcessor(lot, recMeters)
                },
                onIssueToStitcherAndReceive = { lot, stitcher, units, rate ->
                    erpViewModel.issueToStitcherAndReceive(lot, stitcher, units, rate)
                },
                onDispatchAndInvoice = { lot, saleRate ->
                    erpViewModel.dispatchAndInvoiceCustomer(lot, saleRate)
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainNavTab.VOUCHERS -> VouchersJournalScreen(
                vouchers = vouchers,
                accounts = accounts,
                initialVoucherType = preselectedVoucherType,
                onPostVoucher = { voucher, lines ->
                    erpViewModel.postCustomVoucher(voucher, lines)
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainNavTab.SALE_ORDERS -> SaleOrdersScreen(
                saleOrders = saleOrders,
                onCreateSaleOrder = { num, cust, item, qual, blend, width, pcs, meters, price, notes ->
                    erpViewModel.createSaleOrder(num, cust, item, qual, blend, width, pcs, meters, price, notes)
                },
                onStartLotForSo = { so ->
                    currentTab = MainNavTab.LOTS
                },
                modifier = Modifier.padding(innerPadding)
            )

            MainNavTab.REPORTS -> GeneralLedgerReportsScreen(
                accounts = accounts,
                allVouchers = vouchers,
                trialBalance = trialBalance,
                balanceSheet = balanceSheet,
                incomeStatement = incomeStatement,
                lots = lots,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
