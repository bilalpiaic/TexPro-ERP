package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VoucherType(val code: String, val displayName: String, val shortDesc: String) {
    JV("JV", "Journal Voucher", "Non-cash adjustments, inventory & WIP transfers"),
    CR("CR", "Cash Receipt", "Cash received from customer or petty cash"),
    CP("CP", "Cash Payment", "Cash paid for mill freight, labor or local expenses"),
    BP("BP", "Bank Payment", "Cheque or wire payment to Grey Vendor, Processor, or Stitcher"),
    BR("BR", "Bank Receipt", "Bank transfer / wire received from Customer for Sale Order"),
    SALE("Sale", "Sale Voucher", "Customer sales billing & finished goods dispatch"),
    PURCHASE("Purchase", "Purchase Voucher", "Grey cloth bill from weaving mill with lot meters")
}

enum class LotStage(val step: Int, val displayName: String, val location: String) {
    GREY_ORDERED(1, "Grey Ordered", "Weaving Mill"),
    GREY_RECEIVED(2, "Grey in Warehouse", "Raw Material Store"),
    AT_PROCESSOR(3, "At Dye/Print Processor", "Processing Unit"),
    AT_STITCHER(4, "At Stitcher (CMT)", "Stitching Unit"),
    FINISHED_GOODS(5, "Finished Goods", "Packing Warehouse"),
    DISPATCHED_SOLD(6, "Dispatched & Invoiced", "Customer Delivered")
}

@Entity(tableName = "sale_orders")
data class SaleOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orgId: String = "org_default",
    val orderNumber: String,          // e.g. "SO-2026-101"
    val customerName: String,         // e.g. "Brookstone Home Furnishings"
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long = System.currentTimeMillis() + (30L * 86400000L),
    val itemDescription: String,      // e.g. "Printed Bed Sheet Sets 105\" 100% Cotton"
    val quality: String = "40x40 / 100x80",
    val blend: String = "100% Combed Cotton",
    val width: String = "105\"",
    val orderedPieces: Int = 1200,    // finished units ordered
    val targetMeters: Double = 5000.0,// estimated meters needed
    val unitPrice: Double = 30.00,    // selling price per unit
    val status: String = "IN_PRODUCTION", // CONFIRMED, IN_PRODUCTION, COMPLETED, CANCELLED
    val notes: String = ""
) {
    val totalOrderValue: Double get() = orderedPieces * unitPrice
}

@Entity(tableName = "lots")
data class LotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orgId: String = "org_default",
    val lotNumber: String,            // e.g. "LOT-2026-801"
    val saleOrderId: Long,            // Linked SO ID
    val saleOrderNumber: String,      // e.g. "SO-2026-101"
    val customerName: String,

    // Step 1: Grey Cloth Specifications
    val quality: String,              // e.g. "40x40 / 100x80 Sateen"
    val blend: String,                // e.g. "100% Combed Cotton"
    val width: String,                // e.g. "105\""
    val greyVendorName: String,       // e.g. "Kohinoor Weaving Mills"
    val greyMeters: Double,           // e.g. 5200.0 meters
    val greyRatePerMeter: Double,     // e.g. $2.10 / meter
    val greyTotalCost: Double,        // e.g. $10,920.00

    // Step 2: Processing (Dyeing / Printing)
    val processorName: String = "",   // e.g. "Master Textile Printing"
    val processType: String = "",     // e.g. "Pigment Rotary Print & Sanforize"
    val processedMeters: Double = 0.0,// e.g. 4940.0 meters (yield after shrinkage)
    val processingRatePerMeter: Double = 0.0, // e.g. $0.75 / meter
    val processingTotalCost: Double = 0.0,    // e.g. $3,705.00

    // Step 3: Stitching / CMT (Cut, Make, Trim)
    val stitcherName: String = "",    // e.g. "Royal Stitching & Packaging"
    val finishedUnits: Int = 0,       // e.g. 1200 sets / pieces
    val stitchingRatePerUnit: Double = 0.0, // e.g. $2.50 / unit
    val stitchingTotalCost: Double = 0.0,   // e.g. $3,000.00

    // Step 4: Dispatch & Sales
    val stage: LotStage = LotStage.GREY_RECEIVED,
    val saleRatePerUnit: Double = 0.0, // e.g. $30.00 / unit
    val saleTotalRevenue: Double = 0.0,// e.g. $36,000.00
    val saleInvoiceNumber: String = "",

    val notes: String = ""
) {
    val totalCost: Double
        get() = greyTotalCost + processingTotalCost + stitchingTotalCost

    val costPerUnit: Double
        get() = if (finishedUnits > 0) totalCost / finishedUnits else 0.0

    val shrinkagePercent: Double
        get() = if (greyMeters > 0 && processedMeters > 0) {
            ((greyMeters - processedMeters) / greyMeters) * 100.0
        } else 0.0

    val grossProfit: Double
        get() = if (saleTotalRevenue > 0) saleTotalRevenue - totalCost else 0.0

    val grossMarginPercent: Double
        get() = if (saleTotalRevenue > 0) (grossProfit / saleTotalRevenue) * 100.0 else 0.0
}
