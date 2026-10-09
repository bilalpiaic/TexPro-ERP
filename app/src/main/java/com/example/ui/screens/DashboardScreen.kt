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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWithBalance
import com.example.data.model.BalanceSheetData
import com.example.data.model.FinancialHealthRatios
import com.example.data.model.IncomeStatementData
import com.example.data.model.LotEntity
import com.example.data.model.LotStage
import com.example.data.model.SaleOrderEntity
import com.example.data.model.TextileInventorySummary
import com.example.data.model.VoucherType
import com.example.data.model.VoucherWithLines
import com.example.ui.components.AdaptiveGrid
import com.example.ui.components.BalanceCheckBanner
import com.example.ui.components.MetricColumn
import com.example.ui.components.PageHeader
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.WidthClass
import com.example.ui.components.columns
import com.example.ui.components.formatCurrency
import com.example.ui.components.rememberWidthClass
import com.example.ui.export.ReportText
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    organizationName: String = "TexPro mill books",
    balanceSheet: BalanceSheetData?,
    incomeStatement: IncomeStatementData?,
    inventorySummary: TextileInventorySummary?,
    healthRatios: FinancialHealthRatios?,
    lots: List<LotEntity>,
    saleOrders: List<SaleOrderEntity>,
    recentVouchers: List<VoucherWithLines>,
    onNavigateToLots: () -> Unit,
    onNavigateToVouchers: () -> Unit,
    onNavigateToSaleOrders: () -> Unit,
    onNavigateToReports: () -> Unit,
    onOpenNewVoucherDialog: (VoucherType) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCashBank = (balanceSheet?.assets?.find { it.account.code == "1010" }?.currentBalance ?: 0.0) +
            (balanceSheet?.assets?.find { it.account.code == "1020" }?.currentBalance ?: 0.0)
    val widthClass = rememberWidthClass()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PageHeader(
                eyebrow = "Home",
                title = "Mill books at a glance",
                description = "Cash, profit, inventory, and open lots. Use Orders to book a sale, Production to move a lot, Journal to post a voucher, and Reports for IAS-style statements.",
                printTitle = "TexPro dashboard",
                reportText = {
                    ReportText.dashboard(
                        orgName = organizationName,
                        balanceSheet = balanceSheet,
                        income = incomeStatement,
                        inventory = inventorySummary,
                        health = healthRatios,
                        lots = lots,
                        saleOrders = saleOrders
                    )
                },
                primaryActionLabel = "New sale order",
                onPrimaryAction = onNavigateToSaleOrders
            )
        }

        // Hero Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TEXPRO ERP • TEXTILE MANUFACTURING & ACCOUNTING",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Mill Operations & Ledger",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AdaptiveGrid(
                        itemCount = 3,
                        columns = widthClass.columns(compact = 1, medium = 3, expanded = 3),
                        horizontalSpacing = 10.dp,
                        verticalSpacing = 10.dp
                    ) { index, itemMod ->
                        when (index) {
                            0 -> QuickMetric(label = "Cash & Bank", value = formatCurrency(totalCashBank), modifier = itemMod)
                            1 -> QuickMetric(label = "Net Income (P&L)", value = formatCurrency(incomeStatement?.netIncome ?: 0.0), isPositive = true, modifier = itemMod)
                            else -> QuickMetric(label = "Active Lots", value = "${lots.size} in sequence", modifier = itemMod)
                        }
                    }
                }
            }
        }

        // Double-Entry Ledger Invariant Banner
        item {
            val totalAssets = balanceSheet?.totalAssets ?: 0.0
            val totalLiabEquity = (balanceSheet?.totalLiabilities ?: 0.0) + (balanceSheet?.totalEquity ?: 0.0) + (balanceSheet?.retainedEarningsCurrentPeriod ?: 0.0)
            BalanceCheckBanner(
                isBalanced = balanceSheet?.isBalanced ?: true,
                totalAssets = totalAssets,
                totalLiabilitiesAndEquity = totalLiabEquity
            )
        }

        // Quantitative Along with Financial Impact Card (Textile Manufacturing Inventory)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("QUANTITATIVE & FINANCIAL INVENTORY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("Lot-Wise Pipeline Valuation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Text(
                            formatCurrency(inventorySummary?.totalInventoryValue ?: 0.0),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Emerald500, fontFeatureSettings = "tnum"),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AdaptiveGrid(
                        itemCount = 4,
                        columns = widthClass.columns(compact = 2, medium = 4, expanded = 4)
                    ) { index, itemMod ->
                        when (index) {
                            0 -> InventoryStagePill(
                                stage = "1. Grey Cloth",
                                qty = "${inventorySummary?.greyClothMeters?.toInt() ?: 0} m",
                                value = formatCurrency(inventorySummary?.greyClothValue ?: 0.0),
                                color = Blue500,
                                modifier = itemMod
                            )
                            1 -> InventoryStagePill(
                                stage = "2. WIP Dye/Print",
                                qty = "${inventorySummary?.wipProcessingMeters?.toInt() ?: 0} m",
                                value = formatCurrency(inventorySummary?.wipProcessingValue ?: 0.0),
                                color = Amber500,
                                modifier = itemMod
                            )
                            2 -> InventoryStagePill(
                                stage = "3. Fin. Fabric",
                                qty = "${inventorySummary?.finishedFabricMeters?.toInt() ?: 0} m",
                                value = formatCurrency(inventorySummary?.finishedFabricValue ?: 0.0),
                                color = Indigo500,
                                modifier = itemMod
                            )
                            else -> InventoryStagePill(
                                stage = "4. Fin. Goods",
                                qty = "${inventorySummary?.finishedGoodsPieces ?: 0} pcs",
                                value = formatCurrency(inventorySummary?.finishedGoodsValue ?: 0.0),
                                color = Emerald500,
                                modifier = itemMod
                            )
                        }
                    }
                }
            }
        }

        // Categorized Voucher Quick Launchers (JV, CR, CP, BP, BR, Sale, Purchase)
        item {
            SectionHeader(
                title = "Post a voucher",
                subtitle = "Standard classes: JV, CR, CP, BP, BR, Sale, Purchase. Each posting must balance Debit = Credit.",
                actionText = "Open journal",
                onActionClick = onNavigateToVouchers
            )
            AdaptiveGrid(
                itemCount = 7,
                columns = widthClass.columns(compact = 2, medium = 4, expanded = 4)
            ) { index, itemMod ->
                when (index) {
                    0 -> VoucherButton("JV", "Journal", Indigo500, onClick = { onOpenNewVoucherDialog(VoucherType.JV) }, modifier = itemMod)
                    1 -> VoucherButton("CR", "Cash Receipt", Emerald500, onClick = { onOpenNewVoucherDialog(VoucherType.CR) }, modifier = itemMod)
                    2 -> VoucherButton("CP", "Cash Payment", Rose500, onClick = { onOpenNewVoucherDialog(VoucherType.CP) }, modifier = itemMod)
                    3 -> VoucherButton("BR", "Bank Receipt", Blue500, onClick = { onOpenNewVoucherDialog(VoucherType.BR) }, modifier = itemMod)
                    4 -> VoucherButton("BP", "Bank Payment", Amber500, onClick = { onOpenNewVoucherDialog(VoucherType.BP) }, modifier = itemMod)
                    5 -> VoucherButton("Sale", "Sale Voucher", Emerald500, isMajor = true, onClick = { onOpenNewVoucherDialog(VoucherType.SALE) }, modifier = itemMod)
                    else -> VoucherButton("Purchase", "Grey Purchase", Blue500, isMajor = true, onClick = { onOpenNewVoucherDialog(VoucherType.PURCHASE) }, modifier = itemMod)
                }
            }
        }

        // Active Lots Sequence Overview
        item {
            SectionHeader(
                title = "Manufacturing Lots (Sale Order Backed)",
                actionText = "Manage Sequence",
                onActionClick = onNavigateToLots
            )
        }

        items(lots.take(3)) { lot ->
            LotSummaryCard(
                lot = lot,
                onClick = onNavigateToLots
            )
        }

        // Recent Categorized Vouchers
        item {
            SectionHeader(
                title = "Recent General Journal Postings",
                actionText = "View All",
                onActionClick = onNavigateToVouchers
            )
        }

        items(recentVouchers.take(5)) { vWithLines ->
            val v = vWithLines.voucher
            val vColor = when (v.voucherType) {
                VoucherType.JV -> Indigo500
                VoucherType.CR -> Emerald500
                VoucherType.CP -> Rose500
                VoucherType.BP -> Amber500
                VoucherType.BR -> Blue500
                VoucherType.SALE -> Emerald500
                VoucherType.PURCHASE -> Blue500
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToVouchers() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(vColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                v.voucherType.code,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = vColor
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    v.voucherNumber,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (v.lotNumber.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "• ${v.lotNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Text(
                                v.description,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            formatCurrency(v.totalAmount),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (v.quantity > 0) {
                            Text(
                                "${v.quantity.toInt()} ${v.unitMeasure}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickMetric(label: String, value: String, isPositive: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(10.dp)
    ) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                    color = if (isPositive) Emerald500 else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InventoryStagePill(stage: String, qty: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(8.dp)
    ) {
        Column {
            Text(stage, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(qty, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFeatureSettings = "tnum"), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun VoucherButton(code: String, label: String, color: Color, isMajor: Boolean = false, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(code, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun LotSummaryCard(
    lot: LotEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stageColor = when (lot.stage) {
        LotStage.GREY_ORDERED -> Blue500
        LotStage.GREY_RECEIVED -> Blue500
        LotStage.AT_PROCESSOR -> Amber500
        LotStage.AT_STITCHER -> Indigo500
        LotStage.FINISHED_GOODS -> Emerald500
        LotStage.DISPATCHED_SOLD -> Emerald500
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(stageColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            lot.lotNumber,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = stageColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "SO: ${lot.saleOrderNumber}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(stageColor)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        lot.stage.displayName.uppercase(Locale.US),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Buyer: ${lot.customerName}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Specs: ${lot.quality} | ${lot.blend} | Width: ${lot.width}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            val lotWidthClass = rememberWidthClass()
            val revText = if (lot.saleTotalRevenue > 0) "${formatCurrency(lot.saleTotalRevenue)} (+${String.format(Locale.US, "%.1f", lot.grossMarginPercent)}%)" else "-"
            AdaptiveGrid(
                itemCount = 3,
                columns = lotWidthClass.columns(compact = 1, medium = 3, expanded = 3),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(8.dp)
            ) { index, itemMod ->
                when (index) {
                    0 -> MetricColumn(
                        label = "Grey Input",
                        value = "${lot.greyMeters.toInt()}m (${formatCurrency(lot.greyTotalCost)})",
                        modifier = itemMod
                    )
                    1 -> MetricColumn(
                        label = "Accumulated Cost",
                        value = formatCurrency(lot.totalCost),
                        modifier = itemMod
                    )
                    else -> MetricColumn(
                        label = if (lot.stage == LotStage.DISPATCHED_SOLD) "Revenue / Margin" else "Expected Revenue",
                        value = revText,
                        valueColor = Emerald500,
                        alignEnd = lotWidthClass != WidthClass.Compact,
                        modifier = itemMod
                    )
                }
            }
        }
    }
}
