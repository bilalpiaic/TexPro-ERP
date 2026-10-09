package com.example.ui.export

import com.example.data.model.AccountWithBalance
import com.example.data.model.BalanceSheetData
import com.example.data.model.FinancialHealthRatios
import com.example.data.model.IncomeStatementData
import com.example.data.model.LotEntity
import com.example.data.model.SaleOrderEntity
import com.example.data.model.TextileInventorySummary
import com.example.data.model.TrialBalanceData
import com.example.data.model.VoucherWithLines
import com.example.ui.components.formatRs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportText {
    private val dateFmt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US)
    private val dayFmt = SimpleDateFormat("dd MMM yyyy", Locale.US)

    fun dashboard(
        orgName: String,
        balanceSheet: BalanceSheetData?,
        income: IncomeStatementData?,
        inventory: TextileInventorySummary?,
        health: FinancialHealthRatios?,
        lots: List<LotEntity>,
        saleOrders: List<SaleOrderEntity>
    ): String = buildString {
        appendLine("TEXPRO ERP — MANAGEMENT DASHBOARD")
        appendLine(orgName)
        appendLine("Generated: ${dateFmt.format(Date())}")
        appendLine()
        appendLine("STATEMENT OF FINANCIAL POSITION")
        appendLine("Assets:           ${formatRs(balanceSheet?.totalAssets ?: 0.0)}")
        appendLine("Liabilities:      ${formatRs(balanceSheet?.totalLiabilities ?: 0.0)}")
        appendLine("Equity:           ${formatRs(balanceSheet?.totalEquity ?: 0.0)}")
        appendLine("Balanced:         ${if (balanceSheet?.isBalanced == true) "Yes" else "No"}")
        appendLine()
        appendLine("INCOME STATEMENT")
        appendLine("Revenue:          ${formatRs(income?.totalRevenue ?: 0.0)}")
        appendLine("Expenses:         ${formatRs(income?.totalExpense ?: 0.0)}")
        appendLine("Gross profit:     ${formatRs(income?.grossProfit ?: 0.0)}")
        appendLine("Net income:       ${formatRs(income?.netIncome ?: 0.0)}")
        appendLine("Net margin:       ${String.format(Locale.US, "%.1f", income?.netProfitMarginPct ?: 0.0)}%")
        appendLine()
        appendLine("INVENTORY (IAS 2)")
        appendLine("Grey cloth:       ${inventory?.greyClothMeters?.toInt() ?: 0} m  ${formatRs(inventory?.greyClothValue ?: 0.0)}")
        appendLine("WIP processing:   ${inventory?.wipProcessingMeters?.toInt() ?: 0} m  ${formatRs(inventory?.wipProcessingValue ?: 0.0)}")
        appendLine("Finished fabric:  ${inventory?.finishedFabricMeters?.toInt() ?: 0} m  ${formatRs(inventory?.finishedFabricValue ?: 0.0)}")
        appendLine("Finished goods:   ${inventory?.finishedGoodsPieces ?: 0} pcs  ${formatRs(inventory?.finishedGoodsValue ?: 0.0)}")
        appendLine("Total inventory:  ${formatRs(inventory?.totalInventoryValue ?: 0.0)}")
        appendLine()
        appendLine("RATIOS")
        appendLine("Working capital:  ${formatRs(health?.workingCapital ?: 0.0)}")
        appendLine("Current ratio:    ${String.format(Locale.US, "%.2f", health?.currentRatio ?: 0.0)}")
        appendLine("Debt / equity:    ${String.format(Locale.US, "%.2f", health?.debtToEquityRatio ?: 0.0)}")
        appendLine("ROA:              ${String.format(Locale.US, "%.1f", health?.returnOnAssetsPct ?: 0.0)}%")
        appendLine("Health score:     ${health?.healthScore ?: 0} / 100")
        appendLine()
        appendLine("ACTIVITY")
        appendLine("Open sale orders: ${saleOrders.count { it.status != "COMPLETED" }}")
        appendLine("Lots in process:  ${lots.size}")
    }

    fun trialBalance(data: TrialBalanceData?): String = buildString {
        appendLine("TRIAL BALANCE")
        appendLine("Generated: ${dateFmt.format(Date())}")
        appendLine()
        appendLine(String.format(Locale.US, "%-10s %-32s %16s %16s", "Code", "Account", "Debit", "Credit"))
        data?.items.orEmpty().forEach { item ->
            appendLine(
                String.format(
                    Locale.US,
                    "%-10s %-32s %16s %16s",
                    item.account.code,
                    item.account.name.take(32),
                    if (item.debit > 0) formatRs(item.debit) else "-",
                    if (item.credit > 0) formatRs(item.credit) else "-"
                )
            )
        }
        appendLine()
        appendLine("Total debit:  ${formatRs(data?.totalDebit ?: 0.0)}")
        appendLine("Total credit: ${formatRs(data?.totalCredit ?: 0.0)}")
        appendLine("Balanced:     ${if (data?.isBalanced == true) "Yes" else "No"}")
    }

    fun profitAndLoss(data: IncomeStatementData?): String = buildString {
        appendLine("STATEMENT OF PROFIT OR LOSS")
        appendLine("Generated: ${dateFmt.format(Date())}")
        appendLine()
        appendLine("Revenue")
        data?.revenues.orEmpty().forEach { appendLine("  ${it.account.name}: ${formatRs(it.currentBalance)}") }
        appendLine("Total revenue: ${formatRs(data?.totalRevenue ?: 0.0)}")
        appendLine()
        appendLine("Expenses / COGS")
        data?.expenses.orEmpty().forEach { appendLine("  ${it.account.name}: ${formatRs(it.currentBalance)}") }
        appendLine("Total expenses: ${formatRs(data?.totalExpense ?: 0.0)}")
        appendLine()
        appendLine("Gross profit: ${formatRs(data?.grossProfit ?: 0.0)}")
        appendLine("Net income:   ${formatRs(data?.netIncome ?: 0.0)}")
        appendLine("Net margin:   ${String.format(Locale.US, "%.1f", data?.netProfitMarginPct ?: 0.0)}%")
    }

    fun balanceSheet(data: BalanceSheetData?): String = buildString {
        appendLine("STATEMENT OF FINANCIAL POSITION")
        appendLine("Generated: ${dateFmt.format(Date())}")
        appendLine()
        appendLine("ASSETS")
        data?.assets.orEmpty().forEach { appendLine("  ${it.account.code} ${it.account.name}: ${formatRs(it.currentBalance)}") }
        appendLine("Total assets: ${formatRs(data?.totalAssets ?: 0.0)}")
        appendLine()
        appendLine("LIABILITIES")
        data?.liabilities.orEmpty().forEach { appendLine("  ${it.account.code} ${it.account.name}: ${formatRs(it.currentBalance)}") }
        appendLine("Total liabilities: ${formatRs(data?.totalLiabilities ?: 0.0)}")
        appendLine()
        appendLine("EQUITY")
        data?.equity.orEmpty().forEach { appendLine("  ${it.account.code} ${it.account.name}: ${formatRs(it.currentBalance)}") }
        appendLine("Period earnings: ${formatRs(data?.retainedEarningsCurrentPeriod ?: 0.0)}")
        appendLine("Total equity: ${formatRs(data?.totalEquity ?: 0.0)}")
        appendLine()
        appendLine("Balanced: ${if (data?.isBalanced == true) "Yes" else "No"}")
    }

    fun voucher(v: VoucherWithLines, accounts: List<AccountWithBalance>): String {
        val map = accounts.associate { it.account.id to it.account }
        val header = v.voucher
        return buildString {
            appendLine("VOUCHER ${header.voucherType.code} ${header.voucherNumber}")
            appendLine("Date: ${dayFmt.format(Date(header.date))}")
            appendLine("Party: ${header.partyName}")
            appendLine("Narration: ${header.description}")
            if (header.lotNumber.isNotBlank()) appendLine("Lot: ${header.lotNumber}")
            appendLine()
            appendLine(String.format(Locale.US, "%-10s %-28s %14s %14s", "Code", "Account", "Debit", "Credit"))
            v.lines.forEach { line ->
                val acc = map[line.accountId]
                appendLine(
                    String.format(
                        Locale.US,
                        "%-10s %-28s %14s %14s",
                        acc?.code ?: "",
                        (acc?.name ?: "#${line.accountId}").take(28),
                        if (line.debit > 0) formatRs(line.debit) else "-",
                        if (line.credit > 0) formatRs(line.credit) else "-"
                    )
                )
            }
            appendLine()
            appendLine("Total: ${formatRs(header.totalAmount)}")
        }
    }

    fun saleOrders(orders: List<SaleOrderEntity>): String = buildString {
        appendLine("SALE ORDER REGISTER")
        appendLine("Generated: ${dateFmt.format(Date())}")
        appendLine()
        orders.forEach { so ->
            appendLine("${so.orderNumber}  ${so.customerName}  ${so.status}")
            appendLine("  ${so.itemDescription}")
            appendLine("  ${so.orderedPieces} pcs / ${so.targetMeters.toInt()} m  ${formatRs(so.totalOrderValue)}")
            appendLine()
        }
    }

    fun lots(lots: List<LotEntity>): String = buildString {
        appendLine("PRODUCTION LOT REGISTER")
        appendLine("Generated: ${dateFmt.format(Date())}")
        appendLine()
        lots.forEach { lot ->
            appendLine("${lot.lotNumber}  SO ${lot.saleOrderNumber}  ${lot.stage.displayName}")
            appendLine("  Buyer: ${lot.customerName}")
            appendLine("  Grey ${lot.greyMeters.toInt()} m  Cost ${formatRs(lot.totalCost)}")
            if (lot.saleTotalRevenue > 0) {
                appendLine("  Revenue ${formatRs(lot.saleTotalRevenue)}  Margin ${String.format(Locale.US, "%.1f", lot.grossMarginPercent)}%")
            }
            appendLine()
        }
    }

    fun analytics(
        health: FinancialHealthRatios?,
        income: IncomeStatementData?,
        lots: List<LotEntity>
    ): String = buildString {
        appendLine("FINANCIAL ANALYSIS")
        appendLine("Generated: ${dateFmt.format(Date())}")
        appendLine()
        appendLine("Health score ${health?.healthScore ?: 0}/100")
        appendLine("Working capital ${formatRs(health?.workingCapital ?: 0.0)} (current assets − current liabilities)")
        appendLine("Current ratio ${String.format(Locale.US, "%.2f", health?.currentRatio ?: 0.0)} (ability to pay short-term debts)")
        appendLine("Debt/equity ${String.format(Locale.US, "%.2f", health?.debtToEquityRatio ?: 0.0)}")
        appendLine("ROA ${String.format(Locale.US, "%.1f", health?.returnOnAssetsPct ?: 0.0)}%")
        appendLine()
        appendLine("Net income ${formatRs(income?.netIncome ?: 0.0)}")
        appendLine()
        appendLine("LOT MARGINS")
        lots.sortedByDescending { it.grossMarginPercent }.forEach { lot ->
            appendLine("${lot.lotNumber}  ${String.format(Locale.US, "%.1f", lot.grossMarginPercent)}%  ${formatRs(lot.saleTotalRevenue)}")
        }
    }
}
