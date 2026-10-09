package com.example.ui.export

import com.example.data.model.IncomeStatementData
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportTextTest {
    @Test
    fun profitAndLossContainsHeadings() {
        val text = ReportText.profitAndLoss(
            IncomeStatementData(
                revenues = emptyList(),
                expenses = emptyList(),
                totalRevenue = 100.0,
                totalExpense = 40.0,
                grossProfit = 60.0,
                netIncome = 60.0,
                netProfitMarginPct = 60.0
            )
        )
        assertTrue(text.contains("STATEMENT OF PROFIT OR LOSS"))
        assertTrue(text.contains("Net income"))
    }
}
