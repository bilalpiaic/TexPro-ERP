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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWithBalance
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherLineEntity
import com.example.data.model.VoucherType
import com.example.data.model.VoucherWithLines
import com.example.ui.components.DebitCreditPair
import com.example.ui.components.FormPairRow
import com.example.ui.components.PageHeader
import com.example.ui.components.WrapRow
import com.example.ui.components.formatCurrency
import com.example.ui.export.ReportExport
import com.example.ui.export.ReportText
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VouchersJournalScreen(
    vouchers: List<VoucherWithLines>,
    accounts: List<AccountWithBalance>,
    initialVoucherType: VoucherType? = null,
    onPostVoucher: (voucher: VoucherEntity, lines: List<VoucherLineEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(if (initialVoucherType != null) initialVoucherType.ordinal + 1 else 0) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewVoucherDialog by remember { mutableStateOf(false) }

    val filterTabs = listOf("All") + VoucherType.entries.map { "${it.code} (${it.displayName})" }

    val filteredVouchers = vouchers.filter { vWithLines ->
        val v = vWithLines.voucher
        val matchesType = if (selectedFilterIndex == 0) true else {
            v.voucherType.ordinal == (selectedFilterIndex - 1)
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            v.voucherNumber.contains(searchQuery, ignoreCase = true) ||
                    v.description.contains(searchQuery, ignoreCase = true) ||
                    v.partyName.contains(searchQuery, ignoreCase = true) ||
                    v.lotNumber.contains(searchQuery, ignoreCase = true)
        }
        matchesType && matchesSearch
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

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Voucher #, Party, Narration, or Lot #...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PageHeader(
                        eyebrow = "Input · General journal",
                        title = "Vouchers",
                        description = "Post JV, cash, bank, sale, and purchase vouchers. Every entry must balance (total debit = total credit). Print or save a voucher from its card.",
                        printTitle = "General journal",
                        reportText = {
                            buildString {
                                appendLine("GENERAL JOURNAL (${filteredVouchers.size} entries)")
                                filteredVouchers.take(50).forEach { v ->
                                    appendLine(ReportText.voucher(v, accounts))
                                    appendLine("----")
                                }
                            }
                        },
                        primaryActionLabel = "New voucher",
                        onPrimaryAction = { showNewVoucherDialog = true }
                    )
                }

                items(filteredVouchers) { vWithLines ->
                    VoucherDetailedCard(
                        voucherWithLines = vWithLines,
                        accounts = accounts
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showNewVoucherDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("vouchers_fab_add")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post Voucher")
        }
    }

    if (showNewVoucherDialog) {
        NewCategorizedVoucherDialog(
            accounts = accounts,
            onDismiss = { showNewVoucherDialog = false },
            onConfirm = { v, lines ->
                onPostVoucher(v, lines)
                showNewVoucherDialog = false
            }
        )
    }
}

@Composable
fun VoucherDetailedCard(
    voucherWithLines: VoucherWithLines,
    accounts: List<AccountWithBalance>
) {
    val context = LocalContext.current
    val v = voucherWithLines.voucher
    val accountsMap = remember(accounts) { accounts.associate { it.account.id to it.account } }

    val vColor = when (v.voucherType) {
        VoucherType.JV -> Indigo500
        VoucherType.CR -> Emerald500
        VoucherType.CP -> Rose500
        VoucherType.BP -> Amber500
        VoucherType.BR -> Blue500
        VoucherType.SALE -> Emerald500
        VoucherType.PURCHASE -> Blue500
    }

    val totalDebit = voucherWithLines.lines.sumOf { it.debit }
    val totalCredit = voucherWithLines.lines.sumOf { it.credit }
    val isBalanced = kotlin.math.abs(totalDebit - totalCredit) < 0.01

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(vColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(v.voucherType.code, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = vColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        v.voucherNumber,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (v.lotNumber.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(v.lotNumber, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isBalanced) Emerald500.copy(alpha = 0.15f) else Rose500.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        if (isBalanced) "BALANCED" else "UNBALANCED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isBalanced) Emerald500 else Rose500
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(v.description, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))

            WrapRow(horizontalSpacing = 8.dp, verticalSpacing = 4.dp) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(v.date))
                Text("Date: $dateStr | Party: ${v.partyName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (v.quantity > 0) {
                    Text(
                        "${v.quantity.toInt()} ${v.unitMeasure} @ $${v.unitRate}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // Journal Lines
            WrapRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                OutlinedButton(
                    onClick = {
                        val body = ReportText.voucher(voucherWithLines, accounts)
                        ReportExport.printHtml(context, v.voucherNumber, ReportExport.htmlFromPlain(body))
                    },
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Print") }
                OutlinedButton(
                    onClick = {
                        ReportExport.sharePlainText(context, v.voucherNumber, ReportText.voucher(voucherWithLines, accounts))
                    },
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Save") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            voucherWithLines.lines.forEach { line ->
                val acc = accountsMap[line.accountId]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${acc?.code ?: ""} - ${acc?.name ?: "Account #${line.accountId}"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (line.memo.isNotEmpty()) {
                            Text(line.memo, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    DebitCreditPair(
                        debit = if (line.debit > 0) formatCurrency(line.debit) else "-",
                        credit = if (line.credit > 0) formatCurrency(line.credit) else "-",
                        debitColor = if (line.debit > 0) Emerald500 else Color.Gray,
                        creditColor = if (line.credit > 0) Rose500 else Color.Gray
                    )
                }
            }
        }
    }
}

data class EditableLine(
    var accountId: Long = 1L,
    var debitStr: String = "",
    var creditStr: String = "",
    var memo: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewCategorizedVoucherDialog(
    accounts: List<AccountWithBalance>,
    onDismiss: () -> Unit,
    onConfirm: (voucher: VoucherEntity, lines: List<VoucherLineEntity>) -> Unit
) {
    var vType by remember { mutableStateOf(VoucherType.JV) }
    var voucherNum by remember(vType) { mutableStateOf("${vType.code}-${System.currentTimeMillis() % 10000}") }
    var partyName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var lotNumber by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("0") }
    var unitRateStr by remember { mutableStateOf("0") }

    var expandedTypeDropdown by remember { mutableStateOf(false) }

    val defaultLines = remember {
        mutableStateListOf(
            EditableLine(accountId = accounts.firstOrNull()?.account?.id ?: 1L, debitStr = "1000.00", creditStr = "0.00", memo = "Debit entry"),
            EditableLine(accountId = accounts.getOrNull(1)?.account?.id ?: 2L, debitStr = "0.00", creditStr = "1000.00", memo = "Credit entry")
        )
    }

    val totalDebit = defaultLines.sumOf { it.debitStr.toDoubleOrNull() ?: 0.0 }
    val totalCredit = defaultLines.sumOf { it.creditStr.toDoubleOrNull() ?: 0.0 }
    val variance = kotlin.math.abs(totalDebit - totalCredit)
    val isBalanced = variance < 0.01 && totalDebit > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Post General Voucher", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isBalanced) Emerald500.copy(alpha = 0.15f) else Rose500.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isBalanced) "BALANCED" else "VAR: ${formatCurrency(variance)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isBalanced) Emerald500 else Rose500
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voucher Type Picker
                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedTypeDropdown,
                        onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                    ) {
                        OutlinedTextField(
                            value = "${vType.code} - ${vType.displayName}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Voucher Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTypeDropdown,
                            onDismissRequest = { expandedTypeDropdown = false }
                        ) {
                            VoucherType.entries.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.code} - ${t.displayName} (${t.shortDesc})") },
                                    onClick = {
                                        vType = t
                                        expandedTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                item { OutlinedTextField(value = voucherNum, onValueChange = { voucherNum = it }, label = { Text("Voucher Number") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = partyName, onValueChange = { partyName = it }, label = { Text("Party Name (Customer, Mill, Processor)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Transaction Narration (Description)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("Reference / Bilty / Cheque / Bill #") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = lotNumber, onValueChange = { lotNumber = it }, label = { Text("Linked Lot Number (Optional)") }, modifier = Modifier.fillMaxWidth()) }

                item {
                    FormPairRow(
                        first = { fieldMod ->
                            OutlinedTextField(
                                value = quantityStr,
                                onValueChange = { quantityStr = it },
                                label = { Text("Quantitative Qty") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = fieldMod
                            )
                        },
                        second = { fieldMod ->
                            OutlinedTextField(
                                value = unitRateStr,
                                onValueChange = { unitRateStr = it },
                                label = { Text("Unit Rate ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = fieldMod
                            )
                        }
                    )
                }

                item {
                    Text("Journal Lines (Debits must equal Credits):", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }

                items(defaultLines.size) { idx ->
                    val line = defaultLines[idx]
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            AccountDropdown(accounts = accounts, selectedAccountId = line.accountId, onSelect = { line.accountId = it })
                            FormPairRow(
                                first = { fieldMod ->
                                    OutlinedTextField(value = line.debitStr, onValueChange = { line.debitStr = it }, label = { Text("Debit ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = fieldMod)
                                },
                                second = { fieldMod ->
                                    OutlinedTextField(value = line.creditStr, onValueChange = { line.creditStr = it }, label = { Text("Credit ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = fieldMod)
                                }
                            )
                            OutlinedTextField(value = line.memo, onValueChange = { line.memo = it }, label = { Text("Line memo") }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                defaultLines.add(EditableLine(accountId = accounts.firstOrNull()?.account?.id ?: 1L, debitStr = "0.00", creditStr = "0.00", memo = ""))
                            }
                        ) {
                            Text("+ Add Line")
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Dr: ${formatCurrency(totalDebit)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Emerald500))
                            Text("Total Cr: ${formatCurrency(totalCredit)}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Rose500))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isBalanced && description.isNotBlank()) {
                        val v = VoucherEntity(
                            voucherNumber = voucherNum,
                            voucherType = vType,
                            date = System.currentTimeMillis(),
                            description = description,
                            reference = reference,
                            lotNumber = lotNumber,
                            partyName = partyName,
                            quantity = quantityStr.toDoubleOrNull() ?: 0.0,
                            unitRate = unitRateStr.toDoubleOrNull() ?: 0.0,
                            totalAmount = totalDebit,
                            status = "POSTED"
                        )
                        val lines = defaultLines.map {
                            VoucherLineEntity(
                                voucherId = 0,
                                accountId = it.accountId,
                                debit = it.debitStr.toDoubleOrNull() ?: 0.0,
                                credit = it.creditStr.toDoubleOrNull() ?: 0.0,
                                memo = it.memo,
                                lotNumber = lotNumber
                            )
                        }
                        onConfirm(v, lines)
                    }
                },
                enabled = isBalanced && description.isNotBlank()
            ) {
                Text("Post Voucher")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
    accounts: List<AccountWithBalance>,
    selectedAccountId: Long,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.find { it.account.id == selectedAccountId }?.account

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = "${selected?.code ?: ""} - ${selected?.name ?: "Select Account"}",
            onValueChange = {},
            readOnly = true,
            label = { Text("Account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { acc ->
                DropdownMenuItem(
                    text = { Text("${acc.account.code} - ${acc.account.name} (${acc.account.type.displayName})") },
                    onClick = {
                        onSelect(acc.account.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
