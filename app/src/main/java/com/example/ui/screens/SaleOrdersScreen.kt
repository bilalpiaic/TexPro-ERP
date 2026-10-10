package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.SaleOrderEntity
import com.example.ui.components.AdaptiveGrid
import com.example.ui.components.FormPairRow
import com.example.ui.components.LabeledField
import com.example.ui.components.MetricColumn
import com.example.ui.components.PageHeader
import com.example.ui.components.columns
import com.example.ui.components.formatCurrency
import com.example.ui.components.isCompact
import com.example.ui.components.rememberWidthClass
import com.example.ui.export.ReportText
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaleOrdersScreen(
    saleOrders: List<SaleOrderEntity>,
    onCreateSaleOrder: (orderNum: String, cust: String, item: String, qual: String, blend: String, width: String, pcs: Int, meters: Double, price: Double, notes: String) -> Unit,
    onStartLotForSo: (SaleOrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val widthClass = rememberWidthClass()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PageHeader(
                    eyebrow = "Input · Sales",
                    title = "Sale orders",
                    description = "Customer contract is the start of every lot. Enter quality, blend, width, pieces, and price. Then start a production lot from the order card.",
                    printTitle = "Sale order register",
                    reportText = { ReportText.saleOrders(saleOrders) },
                    primaryActionLabel = "New sale order",
                    onPrimaryAction = { showCreateDialog = true }
                )
            }

            items(saleOrders) { so ->
                val isCompleted = so.status == "COMPLETED"
                val statusColor = if (isCompleted) Emerald500 else Amber500

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(so.orderNumber, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                val dStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(so.orderDate))
                                Text(dStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(so.status, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = statusColor)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(so.customerName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(so.itemDescription, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))

                        AdaptiveGrid(
                            itemCount = 3,
                            columns = widthClass.columns(compact = 1, medium = 3, expanded = 3)
                        ) { index, itemMod ->
                            when (index) {
                                0 -> MetricColumn("Quality / Blend", "${so.quality} • ${so.blend}", modifier = itemMod)
                                1 -> MetricColumn("Width", so.width, modifier = itemMod)
                                else -> MetricColumn(
                                    "Ordered Quantity",
                                    "${so.orderedPieces} pcs (${so.targetMeters.toInt()}m grey)",
                                    alignEnd = !widthClass.isCompact,
                                    modifier = itemMod
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        if (widthClass.isCompact) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column {
                                    Text("Order Contract Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        formatCurrency(so.totalOrderValue),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Emerald500, fontFeatureSettings = "tnum"),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (!isCompleted) {
                                    Button(
                                        onClick = { onStartLotForSo(so) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Start Lot (Grey Purchase)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Order Contract Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        formatCurrency(so.totalOrderValue),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Emerald500, fontFeatureSettings = "tnum"),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (!isCompleted) {
                                    Button(
                                        onClick = { onStartLotForSo(so) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Start Lot (Grey Purchase)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("so_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Sale Order")
        }
    }

    if (showCreateDialog) {
        NewSaleOrderDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { num, cust, item, qual, blend, width, pcs, meters, price, notes ->
                onCreateSaleOrder(num, cust, item, qual, blend, width, pcs, meters, price, notes)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun NewSaleOrderDialog(
    onDismiss: () -> Unit,
    onConfirm: (orderNum: String, cust: String, item: String, qual: String, blend: String, width: String, pcs: Int, meters: Double, price: Double, notes: String) -> Unit
) {
    var orderNum by remember { mutableStateOf("SO-2026-${(100..999).random()}") }
    var customerName by remember { mutableStateOf("") }
    var itemDesc by remember { mutableStateOf("Printed Cotton Bed Sheet Sets") }
    var quality by remember { mutableStateOf("40x40 / 100x80 Satin") }
    var blend by remember { mutableStateOf("100% Combed Cotton") }
    var width by remember { mutableStateOf("105\"") }
    var piecesStr by remember { mutableStateOf("1000") }
    var metersStr by remember { mutableStateOf("4500") }
    var priceStr by remember { mutableStateOf("28.00") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New sale order", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { LabeledField("Order number", orderNum, { orderNum = it }, helper = "Unique contract reference, e.g. SO-2026-101", required = true) }
                item { LabeledField("Customer / buyer", customerName, { customerName = it }, helper = "Legal name of the buyer for AR and the invoice", required = true) }
                item { LabeledField("Product", itemDesc, { itemDesc = it }, helper = "What will be invoiced (bed sets, fabric, garments)") }
                item { LabeledField("Quality", quality, { quality = it }, helper = "Warp × weft / ends × picks, e.g. 40x40 / 100x80") }
                item { LabeledField("Blend", blend, { blend = it }, helper = "Fibre composition, e.g. 100% combed cotton") }
                item { LabeledField("Width", width, { width = it }, helper = "Grey width in inches") }
                item {
                    FormPairRow(
                        first = { fieldMod ->
                            LabeledField("Pieces / sets", piecesStr, { piecesStr = it }, helper = "Finished units", required = true, keyboardType = KeyboardType.Number, modifier = fieldMod)
                        },
                        second = { fieldMod ->
                            LabeledField("Grey metres", metersStr, { metersStr = it }, helper = "Estimated grey input", keyboardType = KeyboardType.Number, modifier = fieldMod)
                        }
                    )
                }
                item { LabeledField("Selling price / piece (Rs.)", priceStr, { priceStr = it }, helper = "Unit invoice price in mill currency", required = true, keyboardType = KeyboardType.Number) }
                item { LabeledField("Terms / notes", notes, { notes = it }, helper = "Delivery, packing, or buyer specs", singleLine = false) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pcs = piecesStr.toIntOrNull() ?: 0
                    val m = metersStr.toDoubleOrNull() ?: 0.0
                    val p = priceStr.toDoubleOrNull() ?: 0.0
                    if (orderNum.isNotBlank() && customerName.isNotBlank() && pcs > 0 && p > 0) {
                        onConfirm(orderNum, customerName, itemDesc, quality, blend, width, pcs, m, p, notes)
                    }
                }
            ) { Text("Save order") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
