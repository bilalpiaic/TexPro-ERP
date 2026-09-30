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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleOrderEntity
import com.example.ui.components.formatCurrency
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

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CUSTOMER SALE ORDERS (SO)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primary)
                        Text("Origin of Lot Sequence", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        "${saleOrders.size} Customer Contracts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Quality / Blend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${so.quality} • ${so.blend}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Column {
                                Text("Width", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(so.width, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Ordered Quantity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${so.orderedPieces} pcs (${so.targetMeters.toInt()}m grey)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Order Contract Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatCurrency(so.totalOrderValue), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Emerald500))
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
        title = { Text("Book Customer Sale Order (SO)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { OutlinedTextField(value = orderNum, onValueChange = { orderNum = it }, label = { Text("Order Number") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer / Buyer Name") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = itemDesc, onValueChange = { itemDesc = it }, label = { Text("Product Description") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = quality, onValueChange = { quality = it }, label = { Text("Fabric Quality (Warp x Weft / Ends x Picks)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = blend, onValueChange = { blend = it }, label = { Text("Fiber Blend (100% Cotton, Poly/Cotton)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = width, onValueChange = { width = it }, label = { Text("Width (e.g. 105\", 96\", 58\")") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = piecesStr, onValueChange = { piecesStr = it }, label = { Text("Pieces / Sets") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = metersStr, onValueChange = { metersStr = it }, label = { Text("Target Grey (m)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }
                }
                item { OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Selling Price per Piece ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Special Buyer Specs / Terms") }, modifier = Modifier.fillMaxWidth()) }
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
            ) { Text("Create Sale Order") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
