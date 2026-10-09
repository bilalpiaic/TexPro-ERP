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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LotEntity
import com.example.data.model.LotStage
import com.example.data.model.SaleOrderEntity
import com.example.ui.components.AdaptiveGrid
import com.example.ui.components.AmountText
import com.example.ui.components.MetricColumn
import com.example.ui.components.PageHeader
import com.example.ui.components.columns
import com.example.ui.components.formatCurrency
import com.example.ui.components.isCompact
import com.example.ui.components.rememberWidthClass
import com.example.ui.export.ReportText
import com.example.ui.components.formatPercent
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import java.util.Locale

@Composable
fun LotsWorkflowScreen(
    lots: List<LotEntity>,
    saleOrders: List<SaleOrderEntity>,
    onPurchaseGreyCloth: (soId: Long, soNum: String, cust: String, lotNum: String, qual: String, blend: String, width: String, vendor: String, meters: Double, rate: Double) -> Unit,
    onSendToProcessor: (lot: LotEntity, procName: String, pType: String, rate: Double) -> Unit,
    onReceiveFromProcessor: (lot: LotEntity, recMeters: Double) -> Unit,
    onIssueToStitcherAndReceive: (lot: LotEntity, stitcher: String, units: Int, rate: Double) -> Unit,
    onDispatchAndInvoice: (lot: LotEntity, saleRate: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All Lots", "Grey in Store", "At Processor", "At Stitcher", "Finished Goods", "Dispatched")

    var showNewLotDialog by remember { mutableStateOf(false) }
    var lotToSendToProcessor by remember { mutableStateOf<LotEntity?>(null) }
    var lotToReceiveFromProcessor by remember { mutableStateOf<LotEntity?>(null) }
    var lotToIssueStitcher by remember { mutableStateOf<LotEntity?>(null) }
    var lotToDispatch by remember { mutableStateOf<LotEntity?>(null) }

    val filteredLots = when (selectedFilterIndex) {
        1 -> lots.filter { it.stage == LotStage.GREY_RECEIVED }
        2 -> lots.filter { it.stage == LotStage.AT_PROCESSOR }
        3 -> lots.filter { it.stage == LotStage.AT_STITCHER }
        4 -> lots.filter { it.stage == LotStage.FINISHED_GOODS }
        5 -> lots.filter { it.stage == LotStage.DISPATCHED_SOLD }
        else -> lots
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedFilterIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                filterTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index },
                        text = {
                            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    PageHeader(
                        eyebrow = "Input · Production",
                        title = "Lot workflow",
                        description = "Move each sale-order lot through grey purchase, dyeing/printing, CMT stitching, and dispatch. Each step posts the matching voucher to the ledger.",
                        printTitle = "Production lot register",
                        reportText = { ReportText.lots(lots) },
                        primaryActionLabel = "New grey purchase",
                        onPrimaryAction = { showNewLotDialog = true }
                    )
                    Text(
                        "${filteredLots.size} lots in this filter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(filteredLots) { lot ->
                    TextileLotCard(
                        lot = lot,
                        onSendProcessorClick = { lotToSendToProcessor = lot },
                        onReceiveProcessorClick = { lotToReceiveFromProcessor = lot },
                        onStitchClick = { lotToIssueStitcher = lot },
                        onDispatchClick = { lotToDispatch = lot }
                    )
                }
            }
        }

        // FAB to Purchase Grey Cloth (Initiate Lot)
        FloatingActionButton(
            onClick = { showNewLotDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("lots_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Purchase Grey Cloth (New Lot)")
        }
    }

    if (showNewLotDialog) {
        NewLotDialog(
            saleOrders = saleOrders,
            onDismiss = { showNewLotDialog = false },
            onConfirm = { soId, soNum, cust, lotNum, qual, blend, width, vendor, meters, rate ->
                onPurchaseGreyCloth(soId, soNum, cust, lotNum, qual, blend, width, vendor, meters, rate)
                showNewLotDialog = false
            }
        )
    }

    if (lotToSendToProcessor != null) {
        SendProcessorDialog(
            lot = lotToSendToProcessor!!,
            onDismiss = { lotToSendToProcessor = null },
            onConfirm = { procName, pType, rate ->
                onSendToProcessor(lotToSendToProcessor!!, procName, pType, rate)
                lotToSendToProcessor = null
            }
        )
    }

    if (lotToReceiveFromProcessor != null) {
        ReceiveProcessorDialog(
            lot = lotToReceiveFromProcessor!!,
            onDismiss = { lotToReceiveFromProcessor = null },
            onConfirm = { meters ->
                onReceiveFromProcessor(lotToReceiveFromProcessor!!, meters)
                lotToReceiveFromProcessor = null
            }
        )
    }

    if (lotToIssueStitcher != null) {
        StitchingDialog(
            lot = lotToIssueStitcher!!,
            onDismiss = { lotToIssueStitcher = null },
            onConfirm = { stitcher, units, rate ->
                onIssueToStitcherAndReceive(lotToIssueStitcher!!, stitcher, units, rate)
                lotToIssueStitcher = null
            }
        )
    }

    if (lotToDispatch != null) {
        DispatchDialog(
            lot = lotToDispatch!!,
            onDismiss = { lotToDispatch = null },
            onConfirm = { saleRate ->
                onDispatchAndInvoice(lotToDispatch!!, saleRate)
                lotToDispatch = null
            }
        )
    }
}

@Composable
fun TextileLotCard(
    lot: LotEntity,
    onSendProcessorClick: () -> Unit,
    onReceiveProcessorClick: () -> Unit,
    onStitchClick: () -> Unit,
    onDispatchClick: () -> Unit
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
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
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(lot.lotNumber, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = stageColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Linked SO: ${lot.saleOrderNumber}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(stageColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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
            Text("Buyer / Sale Order: ${lot.customerName}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("Specs: Quality: ${lot.quality} | Blend: ${lot.blend} | Width: ${lot.width}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Sequence Steps Visualizer (Grey -> Processor -> Stitcher -> Customer)
            Text("LOT-WISE SEQUENCE RECONCILIATION:", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))

            // Step 1: Grey Cloth
            SequenceStepItem(
                stepNum = "1",
                stepTitle = "Grey Cloth Purchase",
                party = lot.greyVendorName,
                quantitative = "${lot.greyMeters.toInt()} Meters @ $${lot.greyRatePerMeter}/m",
                financialCost = formatCurrency(lot.greyTotalCost),
                isDone = true
            )

            // Step 2: Dyeing & Printing
            val step2Done = lot.processedMeters > 0 || lot.stage == LotStage.AT_PROCESSOR || lot.stage == LotStage.AT_STITCHER || lot.stage == LotStage.FINISHED_GOODS || lot.stage == LotStage.DISPATCHED_SOLD
            SequenceStepItem(
                stepNum = "2",
                stepTitle = "Dye/Print Processing",
                party = if (lot.processorName.isNotEmpty()) lot.processorName else "Pending Issue",
                quantitative = if (lot.processedMeters > 0) "${lot.processedMeters.toInt()}m (${String.format(Locale.US, "%.1f", lot.shrinkagePercent)}% shrinkage)" else "In Process",
                financialCost = if (lot.processingTotalCost > 0) formatCurrency(lot.processingTotalCost) else "-",
                isDone = lot.processedMeters > 0
            )

            // Step 3: Stitching & CMT
            val step3Done = lot.finishedUnits > 0
            SequenceStepItem(
                stepNum = "3",
                stepTitle = "Stitching & CMT",
                party = if (lot.stitcherName.isNotEmpty()) lot.stitcherName else "Pending CMT",
                quantitative = if (lot.finishedUnits > 0) "${lot.finishedUnits} Finished Sets/Pcs" else "Pending Output",
                financialCost = if (lot.stitchingTotalCost > 0) formatCurrency(lot.stitchingTotalCost) else "-",
                isDone = step3Done
            )

            // Step 4: Dispatch & Customer Invoicing
            val step4Done = lot.stage == LotStage.DISPATCHED_SOLD
            SequenceStepItem(
                stepNum = "4",
                stepTitle = "Final Customer Dispatch",
                party = lot.customerName,
                quantitative = if (lot.saleTotalRevenue > 0) "${lot.finishedUnits} Shipped Sets" else "-",
                financialCost = if (lot.saleTotalRevenue > 0) "${formatCurrency(lot.saleTotalRevenue)} (Revenue)" else "-",
                isDone = step4Done,
                isRevenue = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Cost vs Profit Summary Box
            val lotWidthClass = rememberWidthClass()
            AdaptiveGrid(
                itemCount = if (lot.saleTotalRevenue > 0) 2 else 1,
                columns = lotWidthClass.columns(compact = 1, medium = 2, expanded = 2),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) { index, itemMod ->
                if (index == 0) {
                    MetricColumn(
                        label = if (lot.finishedUnits > 0) "Total Manufacturing Cost · ${formatCurrency(lot.costPerUnit)} / set" else "Total Manufacturing Cost",
                        value = formatCurrency(lot.totalCost),
                        modifier = itemMod
                    )
                } else {
                    MetricColumn(
                        label = "Gross Profit / Margin",
                        value = "${formatCurrency(lot.grossProfit)} (+${String.format(Locale.US, "%.1f", lot.grossMarginPercent)}%)",
                        valueColor = Emerald500,
                        alignEnd = !lotWidthClass.isCompact,
                        modifier = itemMod
                    )
                }
            }

            // Stage Action Buttons
            Spacer(modifier = Modifier.height(12.dp))
            val actionFill = if (lotWidthClass.isCompact) Modifier.fillMaxWidth() else Modifier
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                when (lot.stage) {
                    LotStage.GREY_RECEIVED -> {
                        Button(
                            onClick = onSendProcessorClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Amber500),
                            modifier = actionFill
                        ) {
                            Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send to Processor (Dye/Print)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    LotStage.AT_PROCESSOR -> {
                        Button(
                            onClick = onReceiveProcessorClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                            modifier = actionFill
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Receive Processed Fabric", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    LotStage.FINISHED_GOODS -> {
                        if (lot.finishedUnits == 0) {
                            Button(
                                onClick = onStitchClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                                modifier = actionFill
                            ) {
                                Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Issue to Stitcher (CMT)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        } else {
                            Button(
                                onClick = onDispatchClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                modifier = actionFill
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dispatch & Invoice Customer (SV)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    LotStage.AT_STITCHER -> {
                        Button(
                            onClick = onStitchClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                            modifier = actionFill
                        ) {
                            Text("Receive from Stitcher", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    LotStage.DISPATCHED_SOLD -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Emerald500.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("ORDER FULFILLED & INVOICED (${lot.saleInvoiceNumber})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Emerald500)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun SequenceStepItem(
    stepNum: String,
    stepTitle: String,
    party: String,
    quantitative: String,
    financialCost: String,
    isDone: Boolean,
    isRevenue: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isDone) Emerald500 else Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(stepNum, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stepTitle, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$party • $quantitative", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        AmountText(
            text = financialCost,
            style = MaterialTheme.typography.bodySmall,
            color = if (isRevenue) Emerald500 else MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )
    }
}

// Dialog 1: Purchase Grey Cloth (New Lot)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewLotDialog(
    saleOrders: List<SaleOrderEntity>,
    onDismiss: () -> Unit,
    onConfirm: (soId: Long, soNum: String, cust: String, lotNum: String, qual: String, blend: String, width: String, vendor: String, meters: Double, rate: Double) -> Unit
) {
    var selectedSo by remember { mutableStateOf(saleOrders.firstOrNull()) }
    var expandedDropdown by remember { mutableStateOf(false) }

    var lotNumber by remember { mutableStateOf("LOT-2026-${(100..999).random()}") }
    var greyVendor by remember { mutableStateOf("Kohinoor Weaving Mills") }
    var quality by remember(selectedSo) { mutableStateOf(selectedSo?.quality ?: "40x40 / 100x80") }
    var blend by remember(selectedSo) { mutableStateOf(selectedSo?.blend ?: "100% Combed Cotton") }
    var width by remember(selectedSo) { mutableStateOf(selectedSo?.width ?: "105\"") }
    var metersStr by remember(selectedSo) { mutableStateOf(selectedSo?.targetMeters?.toInt()?.toString() ?: "5000") }
    var rateStr by remember { mutableStateOf("2.10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Purchase Grey Cloth (Lot-Wise)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Select Backed Sale Order (Start till End):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = "${selectedSo?.orderNumber} - ${selectedSo?.customerName}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Sale Order") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                            saleOrders.forEach { so ->
                                DropdownMenuItem(
                                    text = { Text("${so.orderNumber} - ${so.customerName} (${so.orderedPieces} pcs)") },
                                    onClick = {
                                        selectedSo = so
                                        quality = so.quality
                                        blend = so.blend
                                        width = so.width
                                        metersStr = so.targetMeters.toInt().toString()
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                item { OutlinedTextField(value = lotNumber, onValueChange = { lotNumber = it }, label = { Text("Lot Number") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = greyVendor, onValueChange = { greyVendor = it }, label = { Text("Grey Vendor (Weaving Mill)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = quality, onValueChange = { quality = it }, label = { Text("Quality (e.g. 40x40 / 100x80 Satin)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = blend, onValueChange = { blend = it }, label = { Text("Blend (e.g. 100% Cotton, 80/20)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("Fabric Width (e.g. 105\", 96\", 58\")") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = metersStr, onValueChange = { metersStr = it }, label = { Text("Grey Meters Ordered") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = rateStr, onValueChange = { rateStr = it }, label = { Text("Rate per Meter ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = metersStr.toDoubleOrNull() ?: 0.0
                    val r = rateStr.toDoubleOrNull() ?: 0.0
                    if (selectedSo != null && m > 0 && r > 0) {
                        onConfirm(selectedSo!!.id, selectedSo!!.orderNumber, selectedSo!!.customerName, lotNumber, quality, blend, width, greyVendor, m, r)
                    }
                }
            ) { Text("Confirm Grey Purchase") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Dialog 2: Send Grey to Processor
@Composable
private fun SendProcessorDialog(
    lot: LotEntity,
    onDismiss: () -> Unit,
    onConfirm: (procName: String, pType: String, rate: Double) -> Unit
) {
    var procName by remember { mutableStateOf("Master Textile Printing") }
    var pType by remember { mutableStateOf("Rotary Pigment Print & Sanforize") }
    var rateStr by remember { mutableStateOf("0.75") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue Lot ${lot.lotNumber} to Processor", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Total Grey Meters to Issue: ${lot.greyMeters.toInt()}m", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                OutlinedTextField(value = procName, onValueChange = { procName = it }, label = { Text("Dye/Print Processor Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pType, onValueChange = { pType = it }, label = { Text("Process Type (Dyeing / Digital Print / Rotary)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rateStr, onValueChange = { rateStr = it }, label = { Text("Processing Rate per Meter ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Text("Ledger Impact: Automatically posts Journal Voucher (JV) transferring Grey Stock to WIP Processing.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rateStr.toDoubleOrNull() ?: 0.0
                    onConfirm(procName, pType, r)
                }
            ) { Text("Dispatch to Processor") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Dialog 3: Receive from Processor
@Composable
private fun ReceiveProcessorDialog(
    lot: LotEntity,
    onDismiss: () -> Unit,
    onConfirm: (meters: Double) -> Unit
) {
    var metersStr by remember { mutableStateOf(String.format(Locale.US, "%.0f", lot.greyMeters * 0.95)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receive Processed Fabric (${lot.lotNumber})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Issued Grey: ${lot.greyMeters.toInt()}m | Processor: ${lot.processorName}", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(value = metersStr, onValueChange = { metersStr = it }, label = { Text("Finished Output Meters Received") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                val m = metersStr.toDoubleOrNull() ?: 0.0
                if (m > 0 && lot.greyMeters > 0) {
                    val shrink = ((lot.greyMeters - m) / lot.greyMeters) * 100.0
                    Text("Calculated Shrinkage / Loss: ${String.format(Locale.US, "%.1f", shrink)}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Amber500)
                }
                Text("Ledger Impact: Posts Purchase Voucher for processing bill and adds to Finished Fabric Inventory.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = metersStr.toDoubleOrNull() ?: 0.0
                    if (m > 0) onConfirm(m)
                }
            ) { Text("Record Fabric Delivery") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Dialog 4: Stitching & CMT
@Composable
private fun StitchingDialog(
    lot: LotEntity,
    onDismiss: () -> Unit,
    onConfirm: (stitcher: String, units: Int, rate: Double) -> Unit
) {
    var stitcherName by remember { mutableStateOf("Royal CMT Stitching") }
    var unitsStr by remember { mutableStateOf("1200") }
    var rateStr by remember { mutableStateOf("2.50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("CMT Stitching for Lot ${lot.lotNumber}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Finished Fabric Available: ${lot.processedMeters.toInt()} meters", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(value = stitcherName, onValueChange = { stitcherName = it }, label = { Text("Stitching Contractor Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = unitsStr, onValueChange = { unitsStr = it }, label = { Text("Finished Sets / Garments Stitched") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rateStr, onValueChange = { rateStr = it }, label = { Text("CMT Rate per Unit ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Text("Ledger Impact: Posts JV capitalizing fabric and stitching labor into 1440 Finished Goods Store.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val u = unitsStr.toIntOrNull() ?: 0
                    val r = rateStr.toDoubleOrNull() ?: 0.0
                    if (u > 0 && r > 0) onConfirm(stitcherName, u, r)
                }
            ) { Text("Confirm Finished Goods") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// Dialog 5: Dispatch & Invoicing
@Composable
private fun DispatchDialog(
    lot: LotEntity,
    onDismiss: () -> Unit,
    onConfirm: (saleRate: Double) -> Unit
) {
    var rateStr by remember { mutableStateOf("30.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dispatch & Invoice to Customer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Customer: ${lot.customerName} (SO: ${lot.saleOrderNumber})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text("Finished Quantity to Ship: ${lot.finishedUnits} sets/pcs", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(value = rateStr, onValueChange = { rateStr = it }, label = { Text("Final Selling Rate per Unit ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                val rate = rateStr.toDoubleOrNull() ?: 0.0
                val rev = lot.finishedUnits * rate
                val profit = rev - lot.totalCost
                Text("Total Invoice Amount: ${formatCurrency(rev)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Emerald500))
                Text("Estimated Lot Profit: ${formatCurrency(profit)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                Text("Ledger Impact: Generates Sale Voucher (SV) booking Accounts Receivable, Revenue, and direct Cost of Goods Sold.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rateStr.toDoubleOrNull() ?: 0.0
                    if (r > 0) onConfirm(r)
                }
            ) { Text("Issue Sale Voucher & Dispatch") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
