package com.example.data.model

data class BalanceSheetData(
    val assets: List<AccountWithBalance>,
    val liabilities: List<AccountWithBalance>,
    val equity: List<AccountWithBalance>,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val totalEquity: Double,
    val retainedEarningsCurrentPeriod: Double,
    val isBalanced: Boolean
)

data class IncomeStatementData(
    val revenues: List<AccountWithBalance>,
    val expenses: List<AccountWithBalance>,
    val totalRevenue: Double,
    val totalExpense: Double,
    val grossProfit: Double,
    val netIncome: Double,
    val netProfitMarginPct: Double
)

data class TrialBalanceItem(
    val account: AccountEntity,
    val debit: Double,
    val credit: Double,
    val netDebit: Double,
    val netCredit: Double
)

data class TrialBalanceData(
    val items: List<TrialBalanceItem>,
    val totalDebit: Double,
    val totalCredit: Double,
    val isBalanced: Boolean
)

data class FinancialHealthRatios(
    val workingCapital: Double,
    val currentRatio: Double,
    val quickRatio: Double,
    val debtToEquityRatio: Double,
    val returnOnAssetsPct: Double,
    val healthScore: Int
)

data class TextileInventorySummary(
    val greyClothMeters: Double,
    val greyClothValue: Double,
    val wipProcessingMeters: Double,
    val wipProcessingValue: Double,
    val finishedFabricMeters: Double,
    val finishedFabricValue: Double,
    val finishedGoodsPieces: Int,
    val finishedGoodsValue: Double,
    val totalInventoryValue: Double
)

/**
 * Quantitative & Financial Stock Movement Item
 */
data class StockMovementItem(
    val category: String,          // Raw Grey Cloth, WIP Processing, Finished Fabric, Finished Goods
    val lotNumber: String,         // LOT-2026-801
    val quality: String,           // 40x40 / 100x80 Satin
    val unitMeasure: String,       // Meters / Pieces
    val openingQty: Double,
    val openingValueRs: Double,
    val inwardQty: Double,
    val inwardValueRs: Double,
    val outwardQty: Double,
    val outwardValueRs: Double,
    val closingQty: Double,
    val closingValueRs: Double
)

data class StockMovementReportData(
    val items: List<StockMovementItem>,
    val totalOpeningValueRs: Double,
    val totalInwardValueRs: Double,
    val totalOutwardValueRs: Double,
    val totalClosingValueRs: Double
)
