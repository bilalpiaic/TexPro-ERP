package com.example.data.local

import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.LotEntity
import com.example.data.model.LotStage
import com.example.data.model.OrganizationEntity
import com.example.data.model.SaleOrderEntity
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherLineEntity
import com.example.data.model.VoucherType

object InitialData {

    val organizations = listOf(
        OrganizationEntity(
            id = "org_default",
            name = "TexPro Fabrics Ltd.",
            code = "TXP-8491",
            ownerUid = "system_owner",
            currency = "Rs.",
            taxId = "NTN-489102-1",
            millAddress = "Plot 12, Industrial Weaving Estate, Faisalabad",
            contactEmail = "admin@texprofabrics.com"
        ),
        OrganizationEntity(
            id = "org_crescent",
            name = "Crescent Weaving & Dyeing Mills",
            code = "CRW-2042",
            ownerUid = "system_owner",
            currency = "Rs.",
            taxId = "NTN-319504-8",
            millAddress = "Sector 4, Textile Processing Zone, Karachi",
            contactEmail = "accounts@crescentmills.com"
        )
    )

    val accounts = listOf(
        // Assets (1000 - 1999)
        AccountEntity(id = 1, code = "1010", name = "Cash in Hand (Petty & Mill)", type = AccountType.ASSET, description = "Cash on hand for local transport, carriage and labor", initialBalance = 15000.0, isSystem = true),
        AccountEntity(id = 2, code = "1020", name = "Bank Account (Meezan Commercial)", type = AccountType.ASSET, description = "Main corporate bank account for supplier payments and export receipts", initialBalance = 95000.0, isSystem = true),
        AccountEntity(id = 3, code = "1200", name = "Accounts Receivable (Trade Customers)", type = AccountType.ASSET, description = "Outstanding sales invoices claims against buyers", initialBalance = 48000.0, isSystem = true),
        AccountEntity(id = 4, code = "1410", name = "Inventory - Raw Material (Grey Cloth)", type = AccountType.ASSET, description = "Purchased grey fabric in warehouse awaiting processing", initialBalance = 32500.0, isSystem = true),
        AccountEntity(id = 5, code = "1420", name = "Work-in-Progress (WIP - Processing)", type = AccountType.ASSET, description = "Fabric currently at dyeing, bleaching, and rotary printing mills", initialBalance = 18400.0, isSystem = true),
        AccountEntity(id = 6, code = "1430", name = "Inventory - Finished Processed Fabric", type = AccountType.ASSET, description = "Dyed and printed fabric ready for stitching and inspection", initialBalance = 24000.0, isSystem = true),
        AccountEntity(id = 7, code = "1440", name = "Inventory - Finished Goods (Packed Bedsets/Garments)", type = AccountType.ASSET, description = "Finished packaged goods ready for container shipping", initialBalance = 35000.0, isSystem = true),
        AccountEntity(id = 8, code = "1600", name = "Plant, Machinery & Inspection Tables", type = AccountType.ASSET, description = "Fabric grading, rolling, cutting, and packaging equipment", initialBalance = 40000.0, isSystem = false),

        // Liabilities (2000 - 2999)
        AccountEntity(id = 9, code = "2010", name = "Accounts Payable - Grey Weaving Mills", type = AccountType.LIABILITY, description = "Payables to grey cloth suppliers and weaving mills", initialBalance = 28000.0, isSystem = true),
        AccountEntity(id = 10, code = "2020", name = "Accounts Payable - Dyeing & Printing Processors", type = AccountType.LIABILITY, description = "Payables to processing mills for printing and finishing", initialBalance = 12500.0, isSystem = true),
        AccountEntity(id = 11, code = "2030", name = "Accounts Payable - Stitching & CMT Vendors", type = AccountType.LIABILITY, description = "Payables to contract stitching units for CMT and packaging", initialBalance = 8400.0, isSystem = true),
        AccountEntity(id = 12, code = "2100", name = "Short-term Working Capital Loan", type = AccountType.LIABILITY, description = "Running finance and export refinance facility", initialBalance = 30000.0, isSystem = false),

        // Equity (3000 - 3999)
        AccountEntity(id = 13, code = "3010", name = "Owner Capital / Partner Equity", type = AccountType.EQUITY, description = "Contributed capital by textile enterprise partners", initialBalance = 170000.0, isSystem = true),
        AccountEntity(id = 14, code = "3020", name = "Retained Earnings", type = AccountType.EQUITY, description = "Accumulated business profits retained in enterprise", initialBalance = 59000.0, isSystem = true),

        // Revenue (4000 - 4999)
        AccountEntity(id = 15, code = "4010", name = "Textile Finished Goods Sales Revenue", type = AccountType.REVENUE, description = "Revenue from export and local sales of bedsets and apparel", initialBalance = 0.0, isSystem = true),
        AccountEntity(id = 16, code = "4020", name = "Remnant & Cutting Waste Fabric Sales", type = AccountType.REVENUE, description = "Sale of rags, cut pieces, and edge trimmings", initialBalance = 0.0, isSystem = false),

        // Expenses & COGS (5000 - 6999)
        AccountEntity(id = 17, code = "5010", name = "COGS - Grey Cloth Consumed", type = AccountType.EXPENSE, description = "Direct material cost of grey fabric used in sold lots", initialBalance = 0.0, isSystem = true),
        AccountEntity(id = 18, code = "5020", name = "COGS - Dyeing & Printing Charges", type = AccountType.EXPENSE, description = "Direct processing, dyeing, and chemical finishing fees", initialBalance = 0.0, isSystem = true),
        AccountEntity(id = 19, code = "5030", name = "COGS - Stitching & CMT Charges", type = AccountType.EXPENSE, description = "Direct labor charges for cutting, making, and trimming", initialBalance = 0.0, isSystem = true),
        AccountEntity(id = 20, code = "6010", name = "Mill Carriage, Freight & Logistics", type = AccountType.EXPENSE, description = "Transport between weaving mill, processor, stitcher, and port", initialBalance = 0.0, isSystem = false),
        AccountEntity(id = 21, code = "6020", name = "Factory Staff & QC Supervision", type = AccountType.EXPENSE, description = "Quality controllers, fabric inspectors, and supervisors", initialBalance = 0.0, isSystem = false),
        AccountEntity(id = 22, code = "6030", name = "Office Admin, Utility & Marketing", type = AccountType.EXPENSE, description = "Electricity, corporate office, buyer sampling, and communication", initialBalance = 0.0, isSystem = false)
    )

    val saleOrders = listOf(
        SaleOrderEntity(
            id = 1,
            orderNumber = "SO-2026-101",
            customerName = "Brookstone Home Furnishings",
            orderDate = System.currentTimeMillis() - (28 * 86400000L),
            deliveryDate = System.currentTimeMillis() + (7 * 86400000L),
            itemDescription = "Luxury Sateen Bed Sheet Sets (Flat Sheet + Fitted + 2 Pillowcases)",
            quality = "40x40 / 100x80 Satin",
            blend = "100% Combed Cotton",
            width = "105\"",
            orderedPieces = 1200,
            targetMeters = 5200.0,
            unitPrice = 30.00,
            status = "COMPLETED",
            notes = "Export packing in zipper PVC bags with barcode inserts. Fulfilled by LOT-2026-801."
        ),
        SaleOrderEntity(
            id = 2,
            orderNumber = "SO-2026-102",
            customerName = "Nordic Comfort Living (Sweden)",
            orderDate = System.currentTimeMillis() - (16 * 86400000L),
            deliveryDate = System.currentTimeMillis() + (14 * 86400000L),
            itemDescription = "Reactive Dyed Percale Duvet Cover Sets 96\"",
            quality = "30x30 / 68x68 Percale",
            blend = "100% Ring Spun Cotton",
            width = "96\"",
            orderedPieces = 800,
            targetMeters = 4000.0,
            unitPrice = 35.00,
            status = "IN_PRODUCTION",
            notes = "Oeko-Tex Standard 100 certified dyestuffs. Linked to LOT-2026-802."
        ),
        SaleOrderEntity(
            id = 3,
            orderNumber = "SO-2026-103",
            customerName = "Elegance Fashion Retailers",
            orderDate = System.currentTimeMillis() - (6 * 86400000L),
            deliveryDate = System.currentTimeMillis() + (24 * 86400000L),
            itemDescription = "Digital Floral Printed Fabric Rolls 58\"",
            quality = "50x50 / 130x100 Modal Poplin",
            blend = "60% Cotton / 40% Modal",
            width = "58\"",
            orderedPieces = 2500,
            targetMeters = 2750.0,
            unitPrice = 8.50,
            status = "IN_PRODUCTION",
            notes = "100m rolls double-folded on cardboard cores. Linked to LOT-2026-803."
        )
    )

    val lots = listOf(
        LotEntity(
            id = 1,
            lotNumber = "LOT-2026-801",
            saleOrderId = 1,
            saleOrderNumber = "SO-2026-101",
            customerName = "Brookstone Home Furnishings",
            quality = "40x40 / 100x80 Satin",
            blend = "100% Combed Cotton",
            width = "105\"",
            greyVendorName = "Kohinoor Weaving Mills",
            greyMeters = 5200.0,
            greyRatePerMeter = 2.10,
            greyTotalCost = 10920.0,
            processorName = "Master Textile Printing",
            processType = "Rotary Pigment Print & Sanforize",
            processedMeters = 4940.0, // 5% shrinkage
            processingRatePerMeter = 0.75,
            processingTotalCost = 3705.0,
            stitcherName = "Royal CMT Stitching",
            finishedUnits = 1200,
            stitchingRatePerUnit = 2.50,
            stitchingTotalCost = 3000.0,
            stage = LotStage.DISPATCHED_SOLD,
            saleRatePerUnit = 30.00,
            saleTotalRevenue = 36000.0,
            saleInvoiceNumber = "SV-2026-401",
            notes = "Lot complete. Full sequence verified from grey cloth to customer delivery."
        ),
        LotEntity(
            id = 2,
            lotNumber = "LOT-2026-802",
            saleOrderId = 2,
            saleOrderNumber = "SO-2026-102",
            customerName = "Nordic Comfort Living (Sweden)",
            quality = "30x30 / 68x68 Percale",
            blend = "100% Ring Spun Cotton",
            width = "96\"",
            greyVendorName = "Crescent Weaving Ltd",
            greyMeters = 4000.0,
            greyRatePerMeter = 1.90,
            greyTotalCost = 7600.0,
            processorName = "Apex Dyeing & Chemical Processors",
            processType = "Continuous Vat Dye - Deep Navy",
            processedMeters = 3800.0, // 5% shrinkage
            processingRatePerMeter = 0.85,
            processingTotalCost = 3230.0,
            stitcherName = "Al-Haram Stitching Unit",
            finishedUnits = 800,
            stitchingRatePerUnit = 2.80,
            stitchingTotalCost = 2240.0,
            stage = LotStage.AT_STITCHER,
            saleRatePerUnit = 35.00,
            saleTotalRevenue = 28000.0,
            notes = "Currently at stitching unit. Expected completion in 3 days."
        ),
        LotEntity(
            id = 3,
            lotNumber = "LOT-2026-803",
            saleOrderId = 3,
            saleOrderNumber = "SO-2026-103",
            customerName = "Elegance Fashion Retailers",
            quality = "50x50 / 130x100 Modal Poplin",
            blend = "60% Cotton / 40% Modal",
            width = "58\"",
            greyVendorName = "Diamond Weaving Corp",
            greyMeters = 2750.0,
            greyRatePerMeter = 2.40,
            greyTotalCost = 6600.0,
            processorName = "Digital Tex Screen Printers",
            processType = "High-Res Reactive Digital Printing",
            processedMeters = 2600.0,
            processingRatePerMeter = 1.40,
            processingTotalCost = 3640.0,
            stitcherName = "In-House Rolling & Packing",
            finishedUnits = 0,
            stitchingRatePerUnit = 0.0,
            stitchingTotalCost = 0.0,
            stage = LotStage.AT_PROCESSOR,
            saleRatePerUnit = 8.50,
            saleTotalRevenue = 21250.0,
            notes = "Grey cloth delivered to processor gate on Sep 26."
        )
    )

    fun getInitialVouchers(): List<Pair<VoucherEntity, List<VoucherLineEntity>>> {
        val now = System.currentTimeMillis()
        val day = 86400000L

        return listOf(
            // 1. PURCHASE VOUCHER: Purchase Grey Cloth from Kohinoor Weaving (Lot-801)
            Pair(
                VoucherEntity(
                    id = 1,
                    voucherNumber = "PV-2026-101",
                    voucherType = VoucherType.PURCHASE,
                    date = now - (24 * day),
                    description = "Purchase Grey Cloth 40x40/100x80 105\" 100% Cotton for SO-101",
                    reference = "BILL-KW-8492",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Kohinoor Weaving Mills",
                    quantity = 5200.0,
                    unitMeasure = "Meters",
                    unitRate = 2.10,
                    totalAmount = 10920.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 1, voucherId = 1, accountId = 4, debit = 10920.0, credit = 0.0, quantity = 5200.0, memo = "5,200m Grey Cloth @ $2.10/m received", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 2, voucherId = 1, accountId = 9, debit = 0.0, credit = 10920.0, quantity = 0.0, memo = "AP Kohinoor Weaving Mill", lotNumber = "LOT-2026-801")
                )
            ),

            // 2. BANK PAYMENT VOUCHER: Settle Kohinoor Weaving via Bank Cheque
            Pair(
                VoucherEntity(
                    id = 2,
                    voucherNumber = "BP-2026-201",
                    voucherType = VoucherType.BP,
                    date = now - (20 * day),
                    description = "Bank Payment to Kohinoor Weaving Mills via Meezan Cheque #49201",
                    reference = "CHQ-49201",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Kohinoor Weaving Mills",
                    quantity = 5200.0,
                    unitMeasure = "Meters",
                    unitRate = 2.10,
                    totalAmount = 10920.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 3, voucherId = 2, accountId = 9, debit = 10920.0, credit = 0.0, quantity = 0.0, memo = "AP Clearance Kohinoor Weaving", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 4, voucherId = 2, accountId = 2, debit = 0.0, credit = 10920.0, quantity = 0.0, memo = "Meezan Bank withdrawal", lotNumber = "LOT-2026-801")
                )
            ),

            // 3. JOURNAL VOUCHER (JV): Issue Grey to Dyeing/Printing Processor (WIP transfer)
            Pair(
                VoucherEntity(
                    id = 3,
                    voucherNumber = "JV-2026-301",
                    voucherType = VoucherType.JV,
                    date = now - (18 * day),
                    description = "Issue 5,200m Grey Cloth to Master Textile Printing (Lot-801 WIP Transfer)",
                    reference = "GATEPASS-GP-412",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Master Textile Printing",
                    quantity = 5200.0,
                    unitMeasure = "Meters",
                    unitRate = 2.10,
                    totalAmount = 10920.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 5, voucherId = 3, accountId = 5, debit = 10920.0, credit = 0.0, quantity = 5200.0, memo = "WIP Processing Master Textile", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 6, voucherId = 3, accountId = 4, debit = 0.0, credit = 10920.0, quantity = 5200.0, memo = "Grey Cloth issued from warehouse", lotNumber = "LOT-2026-801")
                )
            ),

            // 4. PURCHASE VOUCHER: Processing & Printing Bill from Master Textile (4,940m output)
            Pair(
                VoucherEntity(
                    id = 4,
                    voucherNumber = "PV-2026-102",
                    voucherType = VoucherType.PURCHASE,
                    date = now - (12 * day),
                    description = "Processing & Rotary Printing Bill 4,940m @ $0.75/m from Master Textile",
                    reference = "BILL-MT-994",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Master Textile Printing",
                    quantity = 4940.0,
                    unitMeasure = "Meters",
                    unitRate = 0.75,
                    totalAmount = 3705.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 7, voucherId = 4, accountId = 6, debit = 14625.0, credit = 0.0, quantity = 4940.0, memo = "Finished Processed Fabric (Grey $10,920 + Print $3,705)", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 8, voucherId = 4, accountId = 5, debit = 0.0, credit = 10920.0, quantity = 5200.0, memo = "Clear WIP Processing", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 9, voucherId = 4, accountId = 10, debit = 0.0, credit = 3705.0, quantity = 0.0, memo = "AP Master Textile Printing", lotNumber = "LOT-2026-801")
                )
            ),

            // 5. BANK PAYMENT VOUCHER: Settle Master Textile Processing Charges
            Pair(
                VoucherEntity(
                    id = 5,
                    voucherNumber = "BP-2026-202",
                    voucherType = VoucherType.BP,
                    date = now - (10 * day),
                    description = "Online Bank Transfer to Master Textile for Printing Bill #994",
                    reference = "TXN-882109",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Master Textile Printing",
                    quantity = 4940.0,
                    unitMeasure = "Meters",
                    unitRate = 0.75,
                    totalAmount = 3705.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 10, voucherId = 5, accountId = 10, debit = 3705.0, credit = 0.0, quantity = 0.0, memo = "Settle AP Processor", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 11, voucherId = 5, accountId = 2, debit = 0.0, credit = 3705.0, quantity = 0.0, memo = "Meezan Bank disbursement", lotNumber = "LOT-2026-801")
                )
            ),

            // 6. JOURNAL VOUCHER (JV): Issue 4,940m Fabric to Stitcher & Receive 1,200 Finished Bedsets
            Pair(
                VoucherEntity(
                    id = 6,
                    voucherNumber = "JV-2026-302",
                    voucherType = VoucherType.JV,
                    date = now - (6 * day),
                    description = "CMT Stitching 1,200 Bed Sheet Sets @ $2.50 by Royal Stitching (Lot-801)",
                    reference = "CMT-ROYAL-710",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Royal CMT Stitching",
                    quantity = 1200.0,
                    unitMeasure = "Sets",
                    unitRate = 2.50,
                    totalAmount = 3000.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 12, voucherId = 6, accountId = 7, debit = 17625.0, credit = 0.0, quantity = 1200.0, memo = "1,200 Finished Sets ($14.69/set) into Finished Store", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 13, voucherId = 6, accountId = 6, debit = 0.0, credit = 14625.0, quantity = 4940.0, memo = "Consume 4,940m Finished Fabric", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 14, voucherId = 6, accountId = 11, debit = 0.0, credit = 3000.0, quantity = 0.0, memo = "AP Royal CMT Stitching Vendor", lotNumber = "LOT-2026-801")
                )
            ),

            // 7. CASH PAYMENT VOUCHER (CP): Pay factory loading & bilti carriage
            Pair(
                VoucherEntity(
                    id = 7,
                    voucherNumber = "CP-2026-501",
                    voucherType = VoucherType.CP,
                    date = now - (4 * day),
                    description = "Cash Paid for Mill Carriage & Container Loading Labor for Brookstone Shipment",
                    reference = "VCH-PETTY-104",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "City Freight & Loading",
                    quantity = 1.0,
                    unitMeasure = "Shipment",
                    unitRate = 450.0,
                    totalAmount = 450.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 15, voucherId = 7, accountId = 20, debit = 450.0, credit = 0.0, quantity = 0.0, memo = "Mill Carriage Expense", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 16, voucherId = 7, accountId = 1, debit = 0.0, credit = 450.0, quantity = 0.0, memo = "Petty cash payout", lotNumber = "LOT-2026-801")
                )
            ),

            // 8. SALE VOUCHER: Dispatch & Invoice 1,200 Bed Sets to Brookstone Home Furnishings
            Pair(
                VoucherEntity(
                    id = 8,
                    voucherNumber = "SV-2026-401",
                    voucherType = VoucherType.SALE,
                    date = now - (3 * day),
                    description = "Export Invoice 1,200 Sets Luxury Sateen Bed Sheets @ $30.00 to Brookstone Home",
                    reference = "INV-BH-2026",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Brookstone Home Furnishings",
                    quantity = 1200.0,
                    unitMeasure = "Sets",
                    unitRate = 30.00,
                    totalAmount = 36000.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 17, voucherId = 8, accountId = 3, debit = 36000.0, credit = 0.0, quantity = 1200.0, memo = "AR Brookstone Home Furnishings", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 18, voucherId = 8, accountId = 15, debit = 0.0, credit = 36000.0, quantity = 1200.0, memo = "Finished Goods Sales Revenue", lotNumber = "LOT-2026-801"),
                    // Cost of Goods Sold booking:
                    VoucherLineEntity(id = 19, voucherId = 8, accountId = 17, debit = 10920.0, credit = 0.0, quantity = 5200.0, memo = "COGS Grey Cloth Lot-801", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 20, voucherId = 8, accountId = 18, debit = 3705.0, credit = 0.0, quantity = 4940.0, memo = "COGS Processing Lot-801", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 21, voucherId = 8, accountId = 19, debit = 3000.0, credit = 0.0, quantity = 1200.0, memo = "COGS Stitching Lot-801", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 22, voucherId = 8, accountId = 7, debit = 0.0, credit = 17625.0, quantity = 1200.0, memo = "Derecognize 1,200 Sets Finished Goods Inventory", lotNumber = "LOT-2026-801")
                )
            ),

            // 9. BANK RECEIPT VOUCHER (BR): Foreign Wire Received from Brookstone Home
            Pair(
                VoucherEntity(
                    id = 9,
                    voucherNumber = "BR-2026-601",
                    voucherType = VoucherType.BR,
                    date = now - (1 * day),
                    description = "Inward Foreign Export Wire Received from Brookstone Home Furnishings (INV-BH-2026)",
                    reference = "SWIFT-WIRE-84192",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Brookstone Home Furnishings",
                    quantity = 1200.0,
                    unitMeasure = "Sets",
                    unitRate = 30.00,
                    totalAmount = 36000.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 23, voucherId = 9, accountId = 2, debit = 36000.0, credit = 0.0, quantity = 0.0, memo = "Export proceeds in Meezan Bank", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 24, voucherId = 9, accountId = 3, debit = 0.0, credit = 36000.0, quantity = 0.0, memo = "Clear AR Brookstone Home", lotNumber = "LOT-2026-801")
                )
            ),

            // 10. CASH RECEIPT VOUCHER (CR): Cash received from local market for remnant cutting waste
            Pair(
                VoucherEntity(
                    id = 10,
                    voucherNumber = "CR-2026-701",
                    voucherType = VoucherType.CR,
                    date = now,
                    description = "Cash Received from Local Market for Remnant Cutting Fabric Waste Pieces",
                    reference = "RCP-CASH-331",
                    lotNumber = "LOT-2026-801",
                    saleOrderNumber = "SO-2026-101",
                    partyName = "Local Waste Trader",
                    quantity = 150.0,
                    unitMeasure = "Kg",
                    unitRate = 2.50,
                    totalAmount = 375.0,
                    status = "POSTED"
                ),
                listOf(
                    VoucherLineEntity(id = 25, voucherId = 10, accountId = 1, debit = 375.0, credit = 0.0, quantity = 150.0, memo = "Cash received into office cash box", lotNumber = "LOT-2026-801"),
                    VoucherLineEntity(id = 26, voucherId = 10, accountId = 16, debit = 0.0, credit = 375.0, quantity = 0.0, memo = "Scrap & Cutting Waste Fabric Revenue", lotNumber = "LOT-2026-801")
                )
            )
        )
    }
}
