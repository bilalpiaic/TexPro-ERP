package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.AccountWithBalance
import com.example.data.model.BalanceSheetData
import com.example.data.model.FinancialHealthRatios
import com.example.data.model.IncomeStatementData
import com.example.data.model.LotEntity
import com.example.data.model.LotStage
import com.example.data.model.StockMovementItem
import com.example.data.model.TextileInventorySummary
import com.example.data.model.TrialBalanceData
import com.example.data.model.VoucherType
import com.example.data.model.VoucherWithLines
import com.example.ui.components.BalanceCheckBanner
import com.example.ui.components.CurrencyTogglePill
import com.example.ui.components.DateRangeFilterHeader
import com.example.ui.components.PageHeader
import com.example.ui.export.ReportText
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDualCurrency
import com.example.ui.components.formatRs
import com.example.ui.components.formatUsd
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun GeneralLedgerReportsScreen(
    accounts: List<AccountWithBalance>,
    allVouchers: List<VoucherWithLines>,
    trialBalance: TrialBalanceData?,
    balanceSheet: BalanceSheetData?,
    incomeStatement: IncomeStatementData?,
    lots: List<LotEntity>,
    healthRatios: FinancialHealthRatios? = null,
    inventorySummary: TextileInventorySummary? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Analysis", "General Ledger", "Debtors (AR)", "Stock", "Trial Balance", "Profit & Loss", "Balance Sheet", "Lot margin")

    // Universal Date Filter State (From Date - To Date)
    val now = System.currentTimeMillis()
    var fromDate by remember { mutableLongStateOf(now - (60L * 86400000L)) } // Default: last 60 days
    var toDate by remember { mutableLongStateOf(now + (86400000L)) }

    // Dual Currency Option for Sales Invoice & Debtors Ledgers
    var isUsdPreferred by remember { mutableStateOf(false) }
    var exchangeRate by remember { mutableDoubleStateOf(278.50) } // Rs. 278.50 per USD

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                )
            }
        }

        if (selectedTab != 0) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                PageHeader(
                    eyebrow = "Reports",
                    title = tabTitles[selectedTab],
                    description = when (selectedTab) {
                        1 -> "Account-level running ledger. Debit and credit postings with narration."
                        2 -> "Trade receivables (IAS 1). Invoices less receipts, optional USD display."
                        3 -> "Opening, inward, outward, and closing stock by production stage (IAS 2)."
                        4 -> "All accounts with period debit and credit totals. Totals must match."
                        5 -> "Statement of profit or loss for the selected dates."
                        6 -> "Statement of financial position as at the end date."
                        else -> "Gross margin by lot after grey, processing, and CMT cost."
                    },
                    printTitle = "TexPro ${tabTitles[selectedTab]}",
                    reportText = {
                        when (selectedTab) {
                            4 -> ReportText.trialBalance(trialBalance)
                            5 -> ReportText.profitAndLoss(incomeStatement)
                            6 -> ReportText.balanceSheet(balanceSheet)
                            7 -> ReportText.lots(lots)
                            else -> ReportText.trialBalance(trialBalance)
                        }
                    }
                )
            }
        }

        if (selectedTab != 0) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            DateRangeFilterHeader(
                fromDate = fromDate,
                toDate = toDate,
                onPresetSelected = { preset ->
                    val cal = Calendar.getInstance()
                    when (preset) {
                        "ALL" -> {
                            fromDate = 0L
                            toDate = System.currentTimeMillis() + 86400000L
                        }
                        "MONTH" -> {
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            fromDate = cal.timeInMillis
                            toDate = System.currentTimeMillis() + 86400000L
                        }
                        "30D" -> {
                            fromDate = System.currentTimeMillis() - (30L * 86400000L)
                            toDate = System.currentTimeMillis() + 86400000L
                        }
                        "YEAR" -> {
                            cal.set(Calendar.DAY_OF_YEAR, 1)
                            fromDate = cal.timeInMillis
                            toDate = System.currentTimeMillis() + 86400000L
                        }
                    }
                }
            )
        }
        }

        when (selectedTab) {
            0 -> AnalyticsScreen(
                healthRatios = healthRatios,
                incomeStatement = incomeStatement,
                inventorySummary = inventorySummary,
                lots = lots
            )
            1 -> GeneralLedgerAccountDrilldownView(
                accounts = accounts,
                vouchers = allVouchers,
                fromDate = fromDate,
                toDate = toDate
            )
            2 -> DebtorsLedgerDualCurrencyView(
                vouchers = allVouchers,
                fromDate = fromDate,
                toDate = toDate,
                isUsdPreferred = isUsdPreferred,
                exchangeRate = exchangeRate,
                onToggleCurrency = { isUsdPreferred = it }
            )
            3 -> StockMovementReportView(
                lots = lots,
                vouchers = allVouchers,
                fromDate = fromDate,
                toDate = toDate
            )
            4 -> TrialBalanceReportView(
                accounts = accounts,
                vouchers = allVouchers,
                fromDate = fromDate,
                toDate = toDate
            )
            5 -> ProfitAndLossReportView(
                accounts = accounts,
                vouchers = allVouchers,
                fromDate = fromDate,
                toDate = toDate
            )
            6 -> BalanceSheetReportView(
                accounts = accounts,
                vouchers = allVouchers,
                toDate = toDate
            )
            7 -> LotProfitabilityReportView(
                lots = lots,
                isUsdPreferred = isUsdPreferred,
                exchangeRate = exchangeRate,
                onToggleCurrency = { isUsdPreferred = it }
            )
        }
    }
}

data class LedgerLineRow(
    val date: Long,
    val voucherNumber: String,
    val voucherType: VoucherType,
    val lotNumber: String,
    val narration: String,
    val quantity: Double,
    val unitMeasure: String,
    val debit: Double,
    val credit: Double,
    val runningBalance: Double
)

/**
 * 1. General Ledger with Date Filtering (From - To), Opening Balance, Movements, and Closing Balance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneralLedgerAccountDrilldownView(
    accounts: List<AccountWithBalance>,
    vouchers: List<VoucherWithLines>,
    fromDate: Long,
    toDate: Long
) {
    var selectedAccountId by remember(accounts) { mutableStateOf(accounts.firstOrNull()?.account?.id ?: 1L) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val currentAccountWithBal = accounts.find { it.account.id == selectedAccountId }
    val currentAccount = currentAccountWithBal?.account

    // 1. Calculate Opening Balance before fromDate
    val sortedVouchers = remember(vouchers) { vouchers.sortedBy { it.voucher.date } }

    var openingBalance = currentAccount?.initialBalance ?: 0.0
    val periodRows = mutableListOf<LedgerLineRow>()
    var totalPeriodDr = 0.0
    var totalPeriodCr = 0.0

    var runningBal = openingBalance

    for (vWithLines in sortedVouchers) {
        val matchingLines = vWithLines.lines.filter { it.accountId == selectedAccountId }
        for (line in matchingLines) {
            val isAssetOrExpense = currentAccount?.type == AccountType.ASSET || currentAccount?.type == AccountType.EXPENSE
            val delta = if (isAssetOrExpense) (line.debit - line.credit) else (line.credit - line.debit)

            if (vWithLines.voucher.date < fromDate) {
                openingBalance += delta
                runningBal = openingBalance
            } else if (vWithLines.voucher.date <= toDate) {
                runningBal += delta
                totalPeriodDr += line.debit
                totalPeriodCr += line.credit
                periodRows.add(
                    LedgerLineRow(
                        date = vWithLines.voucher.date,
                        voucherNumber = vWithLines.voucher.voucherNumber,
                        voucherType = vWithLines.voucher.voucherType,
                        lotNumber = line.lotNumber.ifEmpty { vWithLines.voucher.lotNumber },
                        narration = line.memo.ifEmpty { vWithLines.voucher.description },
                        quantity = line.quantity.takeIf { it > 0 } ?: vWithLines.voucher.quantity,
                        unitMeasure = vWithLines.voucher.unitMeasure,
                        debit = line.debit,
                        credit = line.credit,
                        runningBalance = runningBal
                    )
                )
            }
        }
    }

    val closingBalance = runningBal

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = !expandedDropdown }
            ) {
                OutlinedTextField(
                    value = "${currentAccount?.code} - ${currentAccount?.name} (${currentAccount?.type?.displayName})",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select General Ledger Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                    accounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text("${acc.account.code} - ${acc.account.name} (Bal: ${formatRs(acc.currentBalance)})") },
                            onClick = {
                                selectedAccountId = acc.account.id
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Account Summary Banner with Opening and Closing Balances
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PERIOD SUMMARY (BASE CURRENCY RS.)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text(currentAccount?.name ?: "", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Closing Balance:", style = MaterialTheme.typography.labelSmall)
                            Text(formatRs(closingBalance), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Opening Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRs(openingBalance), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Period Debits (+)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRs(totalPeriodDr), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Emerald500))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Period Credits (-)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatRs(totalPeriodCr), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Rose500))
                        }
                    }
                }
            }
        }

        item {
            Text("${periodRows.size} Transactions during selected date range", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(periodRows.reversed()) { row ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(row.date))
                            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${row.voucherType.code}: ${row.voucherNumber}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                            if (row.lotNumber.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("• ${row.lotNumber}", style = MaterialTheme.typography.labelSmall.copy(color = Amber500, fontWeight = FontWeight.Bold))
                            }
                        }

                        Text("Bal: ${formatRs(row.runningBalance)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(row.narration, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (row.quantity > 0) {
                            Text("Qty: ${row.quantity.toInt()} ${row.unitMeasure}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Blue500)
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (row.debit > 0) Text("Dr: ${formatRs(row.debit)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Emerald500))
                            if (row.credit > 0) Text("Cr: ${formatRs(row.credit)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Rose500))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Debtors (Accounts Receivable) Ledger with Dual Currency Option ($ USD and Base Rs.)
 */
@Composable
private fun DebtorsLedgerDualCurrencyView(
    vouchers: List<VoucherWithLines>,
    fromDate: Long,
    toDate: Long,
    isUsdPreferred: Boolean,
    exchangeRate: Double,
    onToggleCurrency: (Boolean) -> Unit
) {
    val arAccountId = 3L // 1200 Accounts Receivable (Trade Customers)

    // Filter all AR vouchers (Sales and Bank Receipts)
    val arVouchers = remember(vouchers, fromDate, toDate) {
        vouchers.filter { v ->
            v.voucher.date in fromDate..toDate &&
                    v.lines.any { it.accountId == arAccountId }
        }.sortedByDescending { it.voucher.date }
    }

    val totalBilledRs = arVouchers.filter { it.voucher.voucherType == VoucherType.SALE }.sumOf { it.voucher.totalAmount }
    val totalReceivedRs = arVouchers.filter { it.voucher.voucherType == VoucherType.BR || it.voucher.voucherType == VoucherType.CR }.sumOf { it.voucher.totalAmount }
    val netOutstandingRs = totalBilledRs - totalReceivedRs

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dual Currency Controller Bar
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DEBTORS / TRADE CUSTOMERS LEDGER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text("Dual Currency Accounting", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        CurrencyTogglePill(
                            isUsdPreferred = isUsdPreferred,
                            exchangeRate = exchangeRate,
                            onToggle = onToggleCurrency
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Billed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (isUsdPreferred) formatUsd(totalBilledRs / exchangeRate) else formatRs(totalBilledRs),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Column {
                            Text("Total Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (isUsdPreferred) formatUsd(totalReceivedRs / exchangeRate) else formatRs(totalReceivedRs),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Emerald500)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Outstanding (AR)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (isUsdPreferred) formatUsd(netOutstandingRs / exchangeRate) else formatRs(netOutstandingRs),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, color = Blue500)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("${arVouchers.size} Customer Sales Invoices & Receipts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(arVouchers) { vWithLines ->
            val v = vWithLines.voucher
            val isInvoice = v.voucherType == VoucherType.SALE
            val accentColor = if (isInvoice) Blue500 else Emerald500

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(v.voucherType.code, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accentColor)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(v.voucherNumber, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            if (v.lotNumber.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("• ${v.lotNumber}", style = MaterialTheme.typography.labelSmall.copy(color = Amber500, fontWeight = FontWeight.Bold))
                            }
                        }

                        // Dual Currency Amount Display
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (isUsdPreferred) formatUsd(v.totalAmount / exchangeRate) else formatRs(v.totalAmount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = accentColor)
                            )
                            Text(
                                if (isUsdPreferred) formatRs(v.totalAmount) else formatUsd(v.totalAmount / exchangeRate),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Customer: ${v.partyName}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text(v.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (v.quantity > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Dispatched Quantity: ${v.quantity.toInt()} ${v.unitMeasure} @ ${if (isUsdPreferred) formatUsd(v.unitRate) else formatRs(v.unitRate * exchangeRate)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Stocks Movement Report (Opening, Inwards, Outwards, Closing across Grey, WIP, Finished Fabric, Finished Goods)
 */
@Composable
private fun StockMovementReportView(
    lots: List<LotEntity>,
    vouchers: List<VoucherWithLines>,
    fromDate: Long,
    toDate: Long
) {
    // Dynamically derive stock movement across the 4 key stages using date-filtered vouchers and lots:
    val stockRows = remember(lots, vouchers, fromDate, toDate) {
        val vouchersBefore = vouchers.filter { it.voucher.date < fromDate }
        val vouchersInRange = vouchers.filter { it.voucher.date in fromDate..toDate }

        fun getCategoryMovement(
            category: String,
            accountId: Long,
            lotNumbers: String,
            quality: String,
            unitMeasure: String
        ): StockMovementItem {
            val linesBefore = vouchersBefore.flatMap { v -> v.lines.filter { it.accountId == accountId } }
            val linesInRange = vouchersInRange.flatMap { v -> v.lines.filter { it.accountId == accountId } }

            val initialBal = when (accountId) {
                4L -> 32500.0 // 1410 Grey Cloth
                5L -> 18400.0 // 1420 WIP
                6L -> 24000.0 // 1430 Finished Fabric
                7L -> 35000.0 // 1440 Finished Goods
                else -> 0.0
            }
            val initialQty = when (accountId) {
                4L -> 15400.0
                5L -> 8200.0
                6L -> 10000.0
                7L -> 1200.0
                else -> 0.0
            }

            val drBeforeVal = linesBefore.sumOf { it.debit }
            val crBeforeVal = linesBefore.sumOf { it.credit }
            val drBeforeQty = linesBefore.sumOf { if (it.quantity > 0) it.quantity else (it.debit / 2.2) }
            val crBeforeQty = linesBefore.sumOf { if (it.quantity > 0) it.quantity else (it.credit / 2.2) }

            val openingVal = initialBal + drBeforeVal - crBeforeVal
            val openingQty = (initialQty + drBeforeQty - crBeforeQty).coerceAtLeast(0.0)

            val inVal = linesInRange.sumOf { it.debit }
            val outVal = linesInRange.sumOf { it.credit }
            val inQty = linesInRange.sumOf { if (it.quantity > 0) it.quantity else (it.debit / 2.2) }
            val outQty = linesInRange.sumOf { if (it.quantity > 0) it.quantity else (it.credit / 2.2) }

            val closingVal = openingVal + inVal - outVal
            val closingQty = (openingQty + inQty - outQty).coerceAtLeast(0.0)

            return StockMovementItem(
                category = category,
                lotNumber = lotNumbers,
                quality = quality,
                unitMeasure = unitMeasure,
                openingQty = openingQty,
                openingValueRs = openingVal,
                inwardQty = inQty,
                inwardValueRs = inVal,
                outwardQty = outQty,
                outwardValueRs = outVal,
                closingQty = closingQty,
                closingValueRs = closingVal
            )
        }

        listOf(
            getCategoryMovement("1. Raw Material: Grey Cloth", 4L, "LOT-2026-801, 802, 803", "40x40 / 100x80 & 30x30", "Meters"),
            getCategoryMovement("2. Work-in-Progress (WIP: Processors)", 5L, "LOT-2026-802, 803", "Dyeing / Digital Print", "Meters"),
            getCategoryMovement("3. Finished Processed Fabric in Mill", 6L, "LOT-2026-801, 802", "Rotary & Vat Dyed Sateen", "Meters"),
            getCategoryMovement("4. Finished Goods (Packed Bedsets)", 7L, "LOT-2026-801", "Export Zipper Pack Sets", "Pieces / Sets")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TEXTILE STOCKS MOVEMENT REGISTER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text("Quantitative & Financial Audit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("BASE: RS.", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(
                        "Detailed opening, inward purchase/receipt, outward consumption, and closing stock reconciliation across all 4 production tiers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(stockRows) { row ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(row.category, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(row.unitMeasure, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    }
                    Text("Lots: ${row.lotNumber} | Specs: ${row.quality}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // 4-Column Stock Matrix: Opening, Inward, Outward, Closing
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StockMovementColumn("Opening", "${row.openingQty.toInt()}", formatRs(row.openingValueRs))
                        StockMovementColumn("Inward (+)", "${row.inwardQty.toInt()}", formatRs(row.inwardValueRs), isPositive = true)
                        StockMovementColumn("Outward (-)", "${row.outwardQty.toInt()}", formatRs(row.outwardValueRs), isNegative = true)
                        StockMovementColumn("Closing Stock", "${row.closingQty.toInt()}", formatRs(row.closingValueRs), isHighlight = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun StockMovementColumn(label: String, qty: String, value: String, isPositive: Boolean = false, isNegative: Boolean = false, isHighlight: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            qty,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Emerald500 else if (isNegative) Rose500 else if (isHighlight) Blue500 else MaterialTheme.colorScheme.onSurface
            )
        )
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * 4. Trial Balance with Date Filter
 */
@Composable
private fun TrialBalanceReportView(
    accounts: List<AccountWithBalance>,
    vouchers: List<VoucherWithLines>,
    fromDate: Long,
    toDate: Long
) {
    // Compute trial balance for the specified date range
    val items = remember(accounts, vouchers, fromDate, toDate) {
        val vouchersInRange = vouchers.filter { it.voucher.date in fromDate..toDate }
        val linesByAccount = vouchersInRange.flatMap { it.lines }.groupBy { it.accountId }

        var totalDr = 0.0
        var totalCr = 0.0

        accounts.map { acc ->
            val lines = linesByAccount[acc.account.id] ?: emptyList()
            val dr = lines.sumOf { it.debit }
            val cr = lines.sumOf { it.credit }
            totalDr += dr
            totalCr += cr

            Triple(acc.account, dr, cr)
        }
    }

    val totalDr = items.sumOf { it.second }
    val totalCr = items.sumOf { it.third }
    val isBalanced = kotlin.math.abs(totalDr - totalCr) < 1.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            BalanceCheckBanner(isBalanced = isBalanced, totalAssets = totalDr, totalLiabilitiesAndEquity = totalCr)
        }

        items(items) { (acc, dr, cr) ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${acc.code} - ${acc.name}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(acc.type.displayName, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(if (dr > 0) formatRs(dr) else "-", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = if (dr > 0) Emerald500 else Color.Gray))
                        Text(if (cr > 0) formatRs(cr) else "-", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = if (cr > 0) Rose500 else Color.Gray))
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTALS (DEBITS == CREDITS)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(formatRs(totalDr), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Emerald500))
                        Text(formatRs(totalCr), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Rose500))
                    }
                }
            }
        }
    }
}

/**
 * 5. Profit & Loss Statement with Date Filter
 */
@Composable
private fun ProfitAndLossReportView(
    accounts: List<AccountWithBalance>,
    vouchers: List<VoucherWithLines>,
    fromDate: Long,
    toDate: Long
) {
    val vouchersInRange = remember(vouchers, fromDate, toDate) {
        vouchers.filter { it.voucher.date in fromDate..toDate }
    }
    val linesByAccount = remember(vouchersInRange) {
        vouchersInRange.flatMap { it.lines }.groupBy { it.accountId }
    }

    val revAccounts = accounts.filter { it.account.type == AccountType.REVENUE }
    val expAccounts = accounts.filter { it.account.type == AccountType.EXPENSE }

    val totalRev = revAccounts.sumOf { acc ->
        val lines = linesByAccount[acc.account.id] ?: emptyList()
        lines.sumOf { it.credit - it.debit }
    }
    val totalExp = expAccounts.sumOf { acc ->
        val lines = linesByAccount[acc.account.id] ?: emptyList()
        lines.sumOf { it.debit - it.credit }
    }
    val netProfit = totalRev - totalExp
    val margin = if (totalRev > 0) (netProfit / totalRev) * 100.0 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PROFIT & LOSS STATEMENT (BASE RS.)", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("REVENUES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Emerald500)
                    revAccounts.forEach { acc ->
                        val bal = (linesByAccount[acc.account.id] ?: emptyList()).sumOf { it.credit - it.debit }
                        ReportRow(acc.account.name, formatRs(bal), isPositive = true)
                    }
                    ReportRow("Total Revenues", formatRs(totalRev), isPositive = true, isBold = true)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("COST OF GOODS SOLD & EXPENSES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Rose500)
                    expAccounts.forEach { acc ->
                        val bal = (linesByAccount[acc.account.id] ?: emptyList()).sumOf { it.debit - it.credit }
                        ReportRow(acc.account.name, "- " + formatRs(bal), isPositive = false)
                    }
                    ReportRow("Total COGS & Expenses", "- " + formatRs(totalExp), isPositive = false, isBold = true)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    ReportRow(
                        label = "NET OPERATING PROFIT",
                        value = formatRs(netProfit),
                        isPositive = netProfit >= 0,
                        isBold = true,
                        isHeadline = true
                    )
                    Text("Net Profit Margin: ${String.format(Locale.US, "%.1f", margin)}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * 6. Balance Sheet as of To Date
 */
@Composable
private fun BalanceSheetReportView(
    accounts: List<AccountWithBalance>,
    vouchers: List<VoucherWithLines>,
    toDate: Long
) {
    val vouchersUpToDate = remember(vouchers, toDate) {
        vouchers.filter { it.voucher.date <= toDate }
    }
    val linesByAccount = remember(vouchersUpToDate) {
        vouchersUpToDate.flatMap { it.lines }.groupBy { it.accountId }
    }

    val assetAccounts = accounts.filter { it.account.type == AccountType.ASSET }
    val liabAccounts = accounts.filter { it.account.type == AccountType.LIABILITY }
    val eqAccounts = accounts.filter { it.account.type == AccountType.EQUITY }
    val revAccounts = accounts.filter { it.account.type == AccountType.REVENUE }
    val expAccounts = accounts.filter { it.account.type == AccountType.EXPENSE }

    val totalAssets = assetAccounts.sumOf { acc ->
        val lines = linesByAccount[acc.account.id] ?: emptyList()
        acc.account.initialBalance + lines.sumOf { it.debit - it.credit }
    }
    val totalLiabilities = liabAccounts.sumOf { acc ->
        val lines = linesByAccount[acc.account.id] ?: emptyList()
        acc.account.initialBalance + lines.sumOf { it.credit - it.debit }
    }
    val totalEquityBase = eqAccounts.sumOf { acc ->
        val lines = linesByAccount[acc.account.id] ?: emptyList()
        acc.account.initialBalance + lines.sumOf { it.credit - it.debit }
    }

    val periodRev = revAccounts.sumOf { acc -> (linesByAccount[acc.account.id] ?: emptyList()).sumOf { it.credit - it.debit } }
    val periodExp = expAccounts.sumOf { acc -> (linesByAccount[acc.account.id] ?: emptyList()).sumOf { it.debit - it.credit } }
    val currentPeriodNetIncome = periodRev - periodExp
    val totalEquity = totalEquityBase + currentPeriodNetIncome

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BALANCE SHEET AS OF SPECIFIED DATE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("ASSETS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Blue500)
                    assetAccounts.forEach { a ->
                        val bal = a.account.initialBalance + ((linesByAccount[a.account.id] ?: emptyList()).sumOf { it.debit - it.credit })
                        ReportRow(a.account.name, formatRs(bal), isPositive = true)
                    }
                    ReportRow("Total Assets", formatRs(totalAssets), isPositive = true, isBold = true)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("LIABILITIES", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Rose500)
                    liabAccounts.forEach { l ->
                        val bal = l.account.initialBalance + ((linesByAccount[l.account.id] ?: emptyList()).sumOf { it.credit - it.debit })
                        ReportRow(l.account.name, formatRs(bal), isPositive = false)
                    }
                    ReportRow("Total Liabilities", formatRs(totalLiabilities), isPositive = false, isBold = true)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("PARTNERS' EQUITY & EARNINGS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Indigo500)
                    eqAccounts.forEach { eq ->
                        val bal = eq.account.initialBalance + ((linesByAccount[eq.account.id] ?: emptyList()).sumOf { it.credit - it.debit })
                        ReportRow(eq.account.name, formatRs(bal), isPositive = true)
                    }
                    ReportRow("Retained Net Profit", formatRs(currentPeriodNetIncome), isPositive = true)
                    ReportRow("Total Equity", formatRs(totalEquity), isPositive = true, isBold = true)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    ReportRow("Total Liabilities & Equity", formatRs(totalLiabilities + totalEquity), isPositive = true, isBold = true, isHeadline = true)
                }
            }
        }
    }
}

/**
 * 7. Lot Profitability & Quantitative Reconciliation
 */
@Composable
private fun LotProfitabilityReportView(
    lots: List<LotEntity>,
    isUsdPreferred: Boolean,
    exchangeRate: Double,
    onToggleCurrency: (Boolean) -> Unit
) {
    fun fmt(valRs: Double): String = if (isUsdPreferred) formatUsd(valRs / exchangeRate) else formatRs(valRs)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("LOT-WISE QUANTITATIVE & FINANCIAL PROFITABILITY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text("Backed by Sale Order (Start till End)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                CurrencyTogglePill(
                    isUsdPreferred = isUsdPreferred,
                    exchangeRate = exchangeRate,
                    onToggle = onToggleCurrency
                )
            }
        }

        items(lots) { lot ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${lot.lotNumber} (${lot.saleOrderNumber})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(lot.stage.displayName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Blue500))
                    }
                    Text("Buyer: ${lot.customerName} | ${lot.quality} | ${lot.width}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Grey Cloth Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${fmt(lot.greyTotalCost)} (${lot.greyMeters.toInt()}m)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Dye/Print Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(fmt(lot.processingTotalCost), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("CMT Stitching", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(fmt(lot.stitchingTotalCost), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Mfg Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(fmt(lot.totalCost), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Sales Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (lot.saleTotalRevenue > 0) fmt(lot.saleTotalRevenue) else "-", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Emerald500))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Realized Margin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val mText = if (lot.saleTotalRevenue > 0) "${fmt(lot.grossProfit)} (${String.format(Locale.US, "%.1f", lot.grossMarginPercent)}%)" else "-"
                            Text(mText, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = if (lot.grossProfit >= 0) Emerald500 else Rose500))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, isPositive: Boolean, isBold: Boolean = false, isHeadline: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isHeadline) 6.dp else 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isHeadline) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            else if (isBold) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            style = if (isHeadline) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = if (isPositive) Emerald500 else Rose500)
            else if (isBold) MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = if (isPositive) Emerald500 else Rose500)
            else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = if (isPositive) Emerald500 else Rose500)
        )
    }
}
