package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FinancialHealthRatios
import com.example.data.model.IncomeStatementData
import com.example.data.model.LotEntity
import com.example.data.model.TextileInventorySummary
import com.example.ui.components.AdaptiveGrid
import com.example.ui.components.HelpCaption
import com.example.ui.components.LabelValueRow
import com.example.ui.components.MetricColumn
import com.example.ui.components.PageHeader
import com.example.ui.components.columns
import com.example.ui.components.formatRs
import com.example.ui.components.rememberWidthClass
import com.example.ui.export.ReportText
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Rose500
import java.util.Locale

@Composable
fun AnalyticsScreen(
    healthRatios: FinancialHealthRatios?,
    incomeStatement: IncomeStatementData?,
    inventorySummary: TextileInventorySummary?,
    lots: List<LotEntity>,
    modifier: Modifier = Modifier
) {
    val widthClass = rememberWidthClass()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "Analysis",
                title = "Financial analytics",
                description = "Ratios and lot margins used in mill management reporting. These figures are derived from the posted general ledger (IAS 1 presentation).",
                printTitle = "TexPro financial analysis",
                reportText = { ReportText.analytics(healthRatios, incomeStatement, lots) }
            )
        }

        item {
            RatioCard(
                title = "Working capital",
                value = formatRs(healthRatios?.workingCapital ?: 0.0),
                meaning = "Current assets minus current liabilities. Positive means the mill can fund grey purchases and processor bills from operating assets."
            )
        }
        item {
            RatioCard(
                title = "Current ratio",
                value = String.format(Locale.US, "%.2f", healthRatios?.currentRatio ?: 0.0),
                meaning = "Current assets ÷ current liabilities. Above 1.0 is typically solvent for trade payables."
            )
        }
        item {
            RatioCard(
                title = "Debt to equity",
                value = String.format(Locale.US, "%.2f", healthRatios?.debtToEquityRatio ?: 0.0),
                meaning = "Total liabilities ÷ equity. Shows how much of the mill is financed by AP mills, processors, and stitchers versus owners."
            )
        }
        item {
            RatioCard(
                title = "Return on assets",
                value = String.format(Locale.US, "%.1f%%", healthRatios?.returnOnAssetsPct ?: 0.0),
                meaning = "Net income ÷ total assets. How efficiently inventory and receivables earn profit."
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Books health score", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "${healthRatios?.healthScore ?: 0} / 100",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ((healthRatios?.healthScore ?: 0) / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    HelpCaption("Composite of liquidity, leverage, and profitability. Use with the trial balance — a high score still requires Dr = Cr.")
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profitability", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    AdaptiveGrid(
                        itemCount = 3,
                        columns = widthClass.columns(compact = 1, medium = 3, expanded = 3)
                    ) { index, itemMod ->
                        when (index) {
                            0 -> MetricColumn("Revenue", formatRs(incomeStatement?.totalRevenue ?: 0.0), modifier = itemMod)
                            1 -> MetricColumn("Gross profit", formatRs(incomeStatement?.grossProfit ?: 0.0), valueColor = Emerald500, modifier = itemMod)
                            else -> MetricColumn("Net income", formatRs(incomeStatement?.netIncome ?: 0.0), modifier = itemMod)
                        }
                    }
                    HelpCaption("Gross profit is sales minus grey, processing, and CMT cost of goods sold. Net income deducts remaining expenses.")
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Inventory mix (IAS 2)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    HelpCaption("Carrying amounts of grey, WIP, processed fabric, and packed goods. Quantities sit beside rupee values.")
                    Spacer(modifier = Modifier.height(10.dp))
                    InventoryLine("Grey cloth", "${inventorySummary?.greyClothMeters?.toInt() ?: 0} m", formatRs(inventorySummary?.greyClothValue ?: 0.0))
                    InventoryLine("WIP dyeing / print", "${inventorySummary?.wipProcessingMeters?.toInt() ?: 0} m", formatRs(inventorySummary?.wipProcessingValue ?: 0.0))
                    InventoryLine("Finished fabric", "${inventorySummary?.finishedFabricMeters?.toInt() ?: 0} m", formatRs(inventorySummary?.finishedFabricValue ?: 0.0))
                    InventoryLine("Finished goods", "${inventorySummary?.finishedGoodsPieces ?: 0} pcs", formatRs(inventorySummary?.finishedGoodsValue ?: 0.0))
                }
            }
        }

        item {
            Text("Lot contribution", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            HelpCaption("Gross margin % after grey + processing + CMT versus invoice value. Dispatched lots show realised margin.")
        }

        items(lots.sortedByDescending { it.grossMarginPercent }) { lot ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(lot.lotNumber, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            String.format(Locale.US, "%.1f%%", lot.grossMarginPercent),
                            fontWeight = FontWeight.Bold,
                            color = if (lot.grossMarginPercent >= 0) Emerald500 else Rose500
                        )
                    }
                    Text("${lot.customerName} · ${lot.stage.displayName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Cost ${formatRs(lot.totalCost)}  ·  Revenue ${formatRs(lot.saleTotalRevenue)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RatioCard(title: String, value: String, meaning: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"), maxLines = 2, overflow = TextOverflow.Ellipsis)
            HelpCaption(meaning)
        }
    }
}

@Composable
private fun InventoryLine(label: String, qty: String, value: String) {
    LabelValueRow(
        label = "$label  ·  $qty",
        value = value,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
