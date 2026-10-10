package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.AccountWithBalance
import com.example.data.model.BalanceSheetData
import com.example.data.model.FinancialHealthRatios
import com.example.data.model.IncomeStatementData
import com.example.data.model.LotEntity
import com.example.data.model.LotStage
import com.example.data.model.OrganizationEntity
import com.example.data.model.SaleOrderEntity
import com.example.data.model.TextileInventorySummary
import com.example.data.model.TrialBalanceData
import com.example.data.model.TrialBalanceItem
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherLineEntity
import com.example.data.model.VoucherType
import com.example.data.model.VoucherWithLines
import androidx.room.withTransaction
import com.example.data.model.OrgLedgerSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ErpRepository(private val database: AppDatabase) {

    private val accountDao = database.accountDao()
    private val voucherDao = database.voucherDao()
    private val textileDao = database.textileDao()
    private val organizationDao = database.organizationDao()

    val allOrganizations: Flow<List<OrganizationEntity>> = organizationDao.getAllOrganizations()
    val rawAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allVouchers: Flow<List<VoucherWithLines>> = voucherDao.getAllVouchersWithLines()
    val allVoucherLines: Flow<List<VoucherLineEntity>> = voucherDao.getAllVoucherLines()
    val saleOrders: Flow<List<SaleOrderEntity>> = textileDao.getAllSaleOrders()
    val lots: Flow<List<LotEntity>> = textileDao.getAllLots()

    /**
     * Reactive General Ledger balances derived from posted voucher lines
     */
    val accountsWithBalances: Flow<List<AccountWithBalance>> = combine(
        rawAccounts,
        allVoucherLines
    ) { accounts, lines ->
        val linesByAccount = lines.groupBy { it.accountId }

        accounts.map { account ->
            val accountLines = linesByAccount[account.id] ?: emptyList()
            val totalDebit = accountLines.sumOf { it.debit }
            val totalCredit = accountLines.sumOf { it.credit }

            val balance = when (account.type) {
                AccountType.ASSET -> account.initialBalance + (totalDebit - totalCredit)
                AccountType.EXPENSE -> account.initialBalance + (totalDebit - totalCredit)
                AccountType.LIABILITY -> account.initialBalance + (totalCredit - totalDebit)
                AccountType.EQUITY -> account.initialBalance + (totalCredit - totalDebit)
                AccountType.REVENUE -> account.initialBalance + (totalCredit - totalDebit)
            }

            AccountWithBalance(
                account = account,
                currentBalance = balance,
                totalDebit = totalDebit,
                totalCredit = totalCredit
            )
        }
    }

    /**
     * Multi-column Trial Balance Report
     */
    val trialBalanceData: Flow<TrialBalanceData> = accountsWithBalances.map { accList ->
        var totalDr = 0.0
        var totalCr = 0.0

        val items = accList.map { acc ->
            val netDebit = if (acc.currentBalance > 0 && (acc.account.type == AccountType.ASSET || acc.account.type == AccountType.EXPENSE)) {
                acc.currentBalance
            } else 0.0

            val netCredit = if (acc.currentBalance > 0 && (acc.account.type == AccountType.LIABILITY || acc.account.type == AccountType.EQUITY || acc.account.type == AccountType.REVENUE)) {
                acc.currentBalance
            } else 0.0

            totalDr += netDebit
            totalCr += netCredit

            TrialBalanceItem(
                account = acc.account,
                debit = acc.totalDebit,
                credit = acc.totalCredit,
                netDebit = netDebit,
                netCredit = netCredit
            )
        }

        val isBalanced = kotlin.math.abs(totalDr - totalCr) < 1.0

        TrialBalanceData(
            items = items,
            totalDebit = totalDr,
            totalCredit = totalCr,
            isBalanced = isBalanced
        )
    }

    /**
     * Balance Sheet Statement (Assets = Liabilities + Equity)
     */
    val balanceSheetData: Flow<BalanceSheetData> = accountsWithBalances.map { list ->
        val assets = list.filter { it.account.type == AccountType.ASSET }
        val liabilities = list.filter { it.account.type == AccountType.LIABILITY }
        val equity = list.filter { it.account.type == AccountType.EQUITY }
        val revenues = list.filter { it.account.type == AccountType.REVENUE }
        val expenses = list.filter { it.account.type == AccountType.EXPENSE }

        val totalAssets = assets.sumOf { it.currentBalance }
        val totalLiabilities = liabilities.sumOf { it.currentBalance }
        val totalEquity = equity.sumOf { it.currentBalance }
        val currentPeriodNetIncome = revenues.sumOf { it.currentBalance } - expenses.sumOf { it.currentBalance }

        val totalLiabEquity = totalLiabilities + totalEquity + currentPeriodNetIncome
        val isBalanced = kotlin.math.abs(totalAssets - totalLiabEquity) < 1.0

        BalanceSheetData(
            assets = assets,
            liabilities = liabilities,
            equity = equity,
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            totalEquity = totalEquity,
            retainedEarningsCurrentPeriod = currentPeriodNetIncome,
            isBalanced = isBalanced
        )
    }

    /**
     * Income Statement (Profit & Loss)
     */
    val incomeStatementData: Flow<IncomeStatementData> = accountsWithBalances.map { list ->
        val revenues = list.filter { it.account.type == AccountType.REVENUE }
        val expenses = list.filter { it.account.type == AccountType.EXPENSE }

        val totalRevenue = revenues.sumOf { it.currentBalance }
        val totalExpense = expenses.sumOf { it.currentBalance }
        val netIncome = totalRevenue - totalExpense
        val netProfitMarginPct = if (totalRevenue > 0) (netIncome / totalRevenue) * 100.0 else 0.0

        IncomeStatementData(
            revenues = revenues,
            expenses = expenses,
            totalRevenue = totalRevenue,
            totalExpense = totalExpense,
            grossProfit = totalRevenue,
            netIncome = netIncome,
            netProfitMarginPct = netProfitMarginPct
        )
    }

    /**
     * Quantitative & Financial Inventory Valuation across production stages
     */
    val inventorySummary: Flow<TextileInventorySummary> = combine(
        accountsWithBalances,
        lots
    ) { accList, lotList ->
        val greyBal = accList.find { it.account.code == "1410" }?.currentBalance ?: 0.0
        val wipBal = accList.find { it.account.code == "1420" }?.currentBalance ?: 0.0
        val finishedFabricBal = accList.find { it.account.code == "1430" }?.currentBalance ?: 0.0
        val finishedGoodsBal = accList.find { it.account.code == "1440" }?.currentBalance ?: 0.0

        val greyMeters = lotList.filter { it.stage == LotStage.GREY_RECEIVED }.sumOf { it.greyMeters }
        val wipMeters = lotList.filter { it.stage == LotStage.AT_PROCESSOR }.sumOf { it.greyMeters }
        val finishedFabricMeters = lotList.filter { it.stage == LotStage.AT_STITCHER }.sumOf { it.processedMeters }
        val finishedPieces = lotList.filter { it.stage == LotStage.FINISHED_GOODS }.sumOf { it.finishedUnits }

        TextileInventorySummary(
            greyClothMeters = greyMeters,
            greyClothValue = greyBal,
            wipProcessingMeters = wipMeters,
            wipProcessingValue = wipBal,
            finishedFabricMeters = finishedFabricMeters,
            finishedFabricValue = finishedFabricBal,
            finishedGoodsPieces = finishedPieces,
            finishedGoodsValue = finishedGoodsBal,
            totalInventoryValue = greyBal + wipBal + finishedFabricBal + finishedGoodsBal
        )
    }

    /**
     * Financial Health Ratios
     */
    val financialHealthRatios: Flow<FinancialHealthRatios> = combine(
        balanceSheetData,
        incomeStatementData
    ) { bs, income ->
        val currentAssets = bs.assets.sumOf { it.currentBalance }
        val currentLiabilities = bs.liabilities.sumOf { it.currentBalance }
        val cashAndEquivalents = bs.assets.filter { it.account.code.startsWith("10") }.sumOf { it.currentBalance }

        val workingCapital = currentAssets - currentLiabilities
        val currentRatio = if (currentLiabilities > 0) currentAssets / currentLiabilities else 3.0
        val quickRatio = if (currentLiabilities > 0) cashAndEquivalents / currentLiabilities else 2.0
        val debtToEquity = if (bs.totalEquity > 0) bs.totalLiabilities / bs.totalEquity else 0.3
        val roa = if (bs.totalAssets > 0) (income.netIncome / bs.totalAssets) * 100.0 else 0.0

        var score = 75
        if (currentRatio >= 1.5) score += 10
        if (quickRatio >= 1.0) score += 5
        if (debtToEquity < 0.8) score += 5
        if (income.netIncome > 0) score += 5
        score = score.coerceIn(0, 100)

        FinancialHealthRatios(
            workingCapital = workingCapital,
            currentRatio = currentRatio,
            quickRatio = quickRatio,
            debtToEquityRatio = debtToEquity,
            returnOnAssetsPct = roa,
            healthScore = score
        )
    }

    suspend fun initializeSeedDataIfEmpty() {
        if (organizationDao.getOrganizationCount() == 0) {
            organizationDao.insertOrganizations(InitialData.organizations)
        }
        if (accountDao.getAccountCount() == 0) {
            accountDao.insertAccounts(InitialData.accounts)
        }
        if (textileDao.getSaleOrderCount() == 0) {
            textileDao.insertSaleOrders(InitialData.saleOrders)
        }
        if (textileDao.getLotCount() == 0) {
            textileDao.insertLots(InitialData.lots)
        }
        if (voucherDao.getVoucherCount() == 0) {
            val initialVouchers = InitialData.getInitialVouchers()
            initialVouchers.forEach { (voucher, lines) ->
                voucherDao.insertCompleteVoucher(voucher, lines)
            }
        }
    }

    suspend fun insertOrganization(organization: OrganizationEntity) {
        organizationDao.insertOrganization(organization)
    }

    suspend fun exportLedgerSnapshot(organization: OrganizationEntity): OrgLedgerSnapshot {
        val vouchersWithLines = allVouchers.first()
        return OrgLedgerSnapshot(
            organization = organization,
            organizations = allOrganizations.first(),
            accounts = rawAccounts.first(),
            vouchers = vouchersWithLines.map { it.voucher },
            voucherLines = vouchersWithLines.flatMap { it.lines },
            saleOrders = saleOrders.first(),
            lots = lots.first()
        )
    }

    suspend fun replaceLedgerFromSnapshot(snapshot: OrgLedgerSnapshot) {
        database.withTransaction {
            voucherDao.deleteAllLines()
            voucherDao.deleteAllVouchers()
            textileDao.deleteAllLots()
            textileDao.deleteAllSaleOrders()
            accountDao.deleteAllAccounts()
            organizationDao.deleteAllOrganizations()

            val orgs = if (snapshot.organizations.isNotEmpty()) snapshot.organizations else listOf(snapshot.organization)
            organizationDao.insertOrganizations(orgs)
            if (snapshot.accounts.isNotEmpty()) {
                accountDao.insertAccounts(snapshot.accounts)
            }
            snapshot.vouchers.forEach { voucherDao.insertVoucher(it) }
            if (snapshot.voucherLines.isNotEmpty()) {
                voucherDao.insertLines(snapshot.voucherLines)
            }
            if (snapshot.saleOrders.isNotEmpty()) {
                textileDao.insertSaleOrders(snapshot.saleOrders)
            }
            if (snapshot.lots.isNotEmpty()) {
                textileDao.insertLots(snapshot.lots)
            }
        }
    }

    suspend fun deleteOrganization(organization: OrganizationEntity) {
        organizationDao.deleteOrganization(organization)
    }

    suspend fun postVoucher(voucher: VoucherEntity, lines: List<VoucherLineEntity>): Long {
        return voucherDao.insertCompleteVoucher(voucher, lines)
    }

    suspend fun createSaleOrder(order: SaleOrderEntity): Long {
        return textileDao.insertSaleOrder(order)
    }

    suspend fun createLot(lot: LotEntity): Long {
        return textileDao.insertLot(lot)
    }

    suspend fun updateLot(lot: LotEntity) {
        textileDao.updateLot(lot)
    }

    /**
     * Complete Sequence Action 1: Purchase Grey Cloth for Lot (PV)
     */
    suspend fun purchaseGreyCloth(
        saleOrderId: Long,
        saleOrderNumber: String,
        customerName: String,
        lotNumber: String,
        quality: String,
        blend: String,
        width: String,
        greyVendor: String,
        meters: Double,
        ratePerMeter: Double
    ) {
        val totalCost = meters * ratePerMeter

        val lot = LotEntity(
            lotNumber = lotNumber,
            saleOrderId = saleOrderId,
            saleOrderNumber = saleOrderNumber,
            customerName = customerName,
            quality = quality,
            blend = blend,
            width = width,
            greyVendorName = greyVendor,
            greyMeters = meters,
            greyRatePerMeter = ratePerMeter,
            greyTotalCost = totalCost,
            stage = LotStage.GREY_RECEIVED
        )
        textileDao.insertLot(lot)

        // Post Purchase Voucher (PV): Dr 1410 Grey Cloth, Cr 2010 AP Grey Vendor
        val voucher = VoucherEntity(
            voucherNumber = "PV-${System.currentTimeMillis() % 10000}",
            voucherType = VoucherType.PURCHASE,
            date = System.currentTimeMillis(),
            description = "Purchase Grey Cloth $quality $blend $width ($meters m @ $$ratePerMeter) for $lotNumber",
            reference = "MILL-DN-${System.currentTimeMillis() % 1000}",
            lotNumber = lotNumber,
            saleOrderNumber = saleOrderNumber,
            partyName = greyVendor,
            quantity = meters,
            unitMeasure = "Meters",
            unitRate = ratePerMeter,
            totalAmount = totalCost,
            status = "POSTED"
        )
        val lines = listOf(
            VoucherLineEntity(voucherId = 0, accountId = 4, debit = totalCost, credit = 0.0, quantity = meters, memo = "$meters m Grey Cloth received into mill store", lotNumber = lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 9, debit = 0.0, credit = totalCost, quantity = 0.0, memo = "AP Weaving Mill $greyVendor", lotNumber = lotNumber)
        )
        voucherDao.insertCompleteVoucher(voucher, lines)
    }

    /**
     * Complete Sequence Action 2: Send Grey to Dyeing/Printing Processor (JV)
     */
    suspend fun sendGreyToProcessor(
        lot: LotEntity,
        processorName: String,
        processType: String,
        ratePerMeter: Double
    ) {
        val updatedLot = lot.copy(
            processorName = processorName,
            processType = processType,
            processingRatePerMeter = ratePerMeter,
            stage = LotStage.AT_PROCESSOR
        )
        textileDao.updateLot(updatedLot)

        // Post Journal Voucher (JV): Transfer from 1410 Grey Store to 1420 WIP Processing
        val voucher = VoucherEntity(
            voucherNumber = "JV-${System.currentTimeMillis() % 10000}",
            voucherType = VoucherType.JV,
            date = System.currentTimeMillis(),
            description = "Issue ${lot.greyMeters}m Grey Cloth to $processorName ($processType) for ${lot.lotNumber}",
            reference = "GATEPASS-GP-${System.currentTimeMillis() % 1000}",
            lotNumber = lot.lotNumber,
            saleOrderNumber = lot.saleOrderNumber,
            partyName = processorName,
            quantity = lot.greyMeters,
            unitMeasure = "Meters",
            unitRate = lot.greyRatePerMeter,
            totalAmount = lot.greyTotalCost,
            status = "POSTED"
        )
        val lines = listOf(
            VoucherLineEntity(voucherId = 0, accountId = 5, debit = lot.greyTotalCost, credit = 0.0, quantity = lot.greyMeters, memo = "WIP Processing $processorName", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 4, debit = 0.0, credit = lot.greyTotalCost, quantity = lot.greyMeters, memo = "Clear raw grey store", lotNumber = lot.lotNumber)
        )
        voucherDao.insertCompleteVoucher(voucher, lines)
    }

    /**
     * Complete Sequence Action 3: Receive Processed Fabric from Processor (Purchase Voucher for Job Work)
     */
    suspend fun receiveFromProcessor(
        lot: LotEntity,
        receivedMeters: Double
    ) {
        val processingCost = receivedMeters * lot.processingRatePerMeter
        val totalFinishedFabricCost = lot.greyTotalCost + processingCost

        val updatedLot = lot.copy(
            processedMeters = receivedMeters,
            processingTotalCost = processingCost,
            stage = LotStage.FINISHED_GOODS // Ready for stitching
        )
        textileDao.updateLot(updatedLot)

        // Post Purchase Voucher (PV) for processing charges & transfer to 1430 Finished Fabric:
        val voucher = VoucherEntity(
            voucherNumber = "PV-${System.currentTimeMillis() % 10000}",
            voucherType = VoucherType.PURCHASE,
            date = System.currentTimeMillis(),
            description = "Dye/Print Processing Bill ${lot.processorName} for ${lot.lotNumber} ($receivedMeters m @ $${lot.processingRatePerMeter})",
            reference = "BILL-PROC-${System.currentTimeMillis() % 1000}",
            lotNumber = lot.lotNumber,
            saleOrderNumber = lot.saleOrderNumber,
            partyName = lot.processorName,
            quantity = receivedMeters,
            unitMeasure = "Meters",
            unitRate = lot.processingRatePerMeter,
            totalAmount = processingCost,
            status = "POSTED"
        )
        val lines = listOf(
            VoucherLineEntity(voucherId = 0, accountId = 6, debit = totalFinishedFabricCost, credit = 0.0, quantity = receivedMeters, memo = "Finished Fabric Stock in warehouse", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 5, debit = 0.0, credit = lot.greyTotalCost, quantity = lot.greyMeters, memo = "Clear WIP Processing", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 10, debit = 0.0, credit = processingCost, quantity = 0.0, memo = "AP ${lot.processorName} processing fee", lotNumber = lot.lotNumber)
        )
        voucherDao.insertCompleteVoucher(voucher, lines)
    }

    /**
     * Complete Sequence Action 4: Issue to Stitcher & Receive Finished Goods (JV)
     */
    suspend fun issueToStitcherAndReceive(
        lot: LotEntity,
        stitcherName: String,
        finishedUnits: Int,
        ratePerUnit: Double
    ) {
        val stitchingCost = finishedUnits * ratePerUnit
        val totalCost = lot.greyTotalCost + lot.processingTotalCost + stitchingCost

        val updatedLot = lot.copy(
            stitcherName = stitcherName,
            finishedUnits = finishedUnits,
            stitchingRatePerUnit = ratePerUnit,
            stitchingTotalCost = stitchingCost,
            stage = LotStage.FINISHED_GOODS
        )
        textileDao.updateLot(updatedLot)

        // Post Journal Voucher (JV): Dr 1440 Finished Goods, Cr 1430 Finished Fabric, Cr 2030 AP Stitcher
        val voucher = VoucherEntity(
            voucherNumber = "JV-${System.currentTimeMillis() % 10000}",
            voucherType = VoucherType.JV,
            date = System.currentTimeMillis(),
            description = "CMT Stitching $finishedUnits units by $stitcherName for ${lot.lotNumber}",
            reference = "CMT-${System.currentTimeMillis() % 1000}",
            lotNumber = lot.lotNumber,
            saleOrderNumber = lot.saleOrderNumber,
            partyName = stitcherName,
            quantity = finishedUnits.toDouble(),
            unitMeasure = "Units",
            unitRate = ratePerUnit,
            totalAmount = stitchingCost,
            status = "POSTED"
        )
        val lines = listOf(
            VoucherLineEntity(voucherId = 0, accountId = 7, debit = totalCost, credit = 0.0, quantity = finishedUnits.toDouble(), memo = "$finishedUnits Packed units in warehouse", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 6, debit = 0.0, credit = (lot.greyTotalCost + lot.processingTotalCost), quantity = lot.processedMeters, memo = "Consume processed fabric", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 11, debit = 0.0, credit = stitchingCost, quantity = 0.0, memo = "AP $stitcherName CMT charges", lotNumber = lot.lotNumber)
        )
        voucherDao.insertCompleteVoucher(voucher, lines)
    }

    /**
     * Complete Sequence Action 5: Dispatch & Invoicing to Final Customer (Sale Voucher)
     */
    suspend fun dispatchAndInvoiceCustomer(
        lot: LotEntity,
        saleRatePerUnit: Double
    ) {
        val totalRevenue = lot.finishedUnits * saleRatePerUnit
        val invoiceNumber = "SV-${System.currentTimeMillis() % 10000}"

        val updatedLot = lot.copy(
            stage = LotStage.DISPATCHED_SOLD,
            saleRatePerUnit = saleRatePerUnit,
            saleTotalRevenue = totalRevenue,
            saleInvoiceNumber = invoiceNumber
        )
        textileDao.updateLot(updatedLot)

        // Also update linked Sale Order
        val so = textileDao.getSaleOrderById(lot.saleOrderId)
        if (so != null) {
            textileDao.updateSaleOrder(so.copy(status = "COMPLETED"))
        }

        // Post Sale Voucher (SV):
        // Dr 1200 AR Customer $totalRevenue, Cr 4010 Sales Revenue $totalRevenue
        // Dr 5010/5020/5030 COGS, Cr 1440 Finished Goods
        val voucher = VoucherEntity(
            voucherNumber = invoiceNumber,
            voucherType = VoucherType.SALE,
            date = System.currentTimeMillis(),
            description = "Sales Invoice to ${lot.customerName} for ${lot.finishedUnits} units (${lot.lotNumber})",
            reference = "DISPATCH-${lot.saleOrderNumber}",
            lotNumber = lot.lotNumber,
            saleOrderNumber = lot.saleOrderNumber,
            partyName = lot.customerName,
            quantity = lot.finishedUnits.toDouble(),
            unitMeasure = "Units",
            unitRate = saleRatePerUnit,
            totalAmount = totalRevenue,
            status = "POSTED"
        )
        val lines = listOf(
            VoucherLineEntity(voucherId = 0, accountId = 3, debit = totalRevenue, credit = 0.0, quantity = lot.finishedUnits.toDouble(), memo = "AR ${lot.customerName}", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 15, debit = 0.0, credit = totalRevenue, quantity = lot.finishedUnits.toDouble(), memo = "Sales Revenue Finished Goods", lotNumber = lot.lotNumber),
            // Cost of Goods Sold:
            VoucherLineEntity(voucherId = 0, accountId = 17, debit = lot.greyTotalCost, credit = 0.0, quantity = lot.greyMeters, memo = "COGS Grey Cloth Lot ${lot.lotNumber}", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 18, debit = lot.processingTotalCost, credit = 0.0, quantity = lot.processedMeters, memo = "COGS Processing Lot ${lot.lotNumber}", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 19, debit = lot.stitchingTotalCost, credit = 0.0, quantity = lot.finishedUnits.toDouble(), memo = "COGS Stitching Lot ${lot.lotNumber}", lotNumber = lot.lotNumber),
            VoucherLineEntity(voucherId = 0, accountId = 7, debit = 0.0, credit = lot.totalCost, quantity = lot.finishedUnits.toDouble(), memo = "De-recognize finished goods stock", lotNumber = lot.lotNumber)
        )
        voucherDao.insertCompleteVoucher(voucher, lines)
    }

    /**
     * Settle payments via Cash or Bank (CR, CP, BR, BP)
     */
    suspend fun recordCashBankVoucher(
        voucherType: VoucherType, // CR, CP, BR, BP
        partyName: String,
        amount: Double,
        description: String,
        reference: String,
        targetAccountId: Long, // e.g. AP Grey Vendor, AP Processor, AR Customer, or Expense
        lotNumber: String = ""
    ) {
        val voucherNumber = "${voucherType.code}-${System.currentTimeMillis() % 10000}"
        val cashAccountId = 1L // 1010 Cash in Hand
        val bankAccountId = 2L // 1020 Bank Account

        val (cashBankId, isPayment) = when (voucherType) {
            VoucherType.CP -> Pair(cashAccountId, true)
            VoucherType.BP -> Pair(bankAccountId, true)
            VoucherType.CR -> Pair(cashAccountId, false)
            VoucherType.BR -> Pair(bankAccountId, false)
            else -> Pair(bankAccountId, true)
        }

        val lines = if (isPayment) {
            // Payment: Debit target account (e.g. AP or Expense), Credit Cash/Bank
            listOf(
                VoucherLineEntity(voucherId = 0, accountId = targetAccountId, debit = amount, credit = 0.0, memo = description, lotNumber = lotNumber),
                VoucherLineEntity(voucherId = 0, accountId = cashBankId, debit = 0.0, credit = amount, memo = "Disbursed via ${if (cashBankId == 1L) "Cash" else "Bank"}", lotNumber = lotNumber)
            )
        } else {
            // Receipt: Debit Cash/Bank, Credit target account (e.g. AR or Revenue)
            listOf(
                VoucherLineEntity(voucherId = 0, accountId = cashBankId, debit = amount, credit = 0.0, memo = "Receipt into ${if (cashBankId == 1L) "Cash" else "Bank"}", lotNumber = lotNumber),
                VoucherLineEntity(voucherId = 0, accountId = targetAccountId, debit = 0.0, credit = amount, memo = description, lotNumber = lotNumber)
            )
        }

        val voucher = VoucherEntity(
            voucherNumber = voucherNumber,
            voucherType = voucherType,
            date = System.currentTimeMillis(),
            description = description,
            reference = reference,
            lotNumber = lotNumber,
            partyName = partyName,
            totalAmount = amount,
            status = "POSTED"
        )
        voucherDao.insertCompleteVoucher(voucher, lines)
    }

    suspend fun addAccount(account: AccountEntity): Long {
        return accountDao.insertAccount(account)
    }
}
