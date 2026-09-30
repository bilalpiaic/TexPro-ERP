package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "vouchers",
    indices = [
        Index("voucherNumber"),
        Index("voucherType"),
        Index("lotNumber"),
        Index("saleOrderNumber")
    ]
)
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orgId: String = "org_default",
    val voucherNumber: String,        // e.g. "PV-2026-101", "BP-2026-201", "JV-2026-301", "SV-2026-401"
    val voucherType: VoucherType,     // JV, CR, CP, BP, BR, SALE, PURCHASE
    val date: Long = System.currentTimeMillis(),
    val description: String,          // Transaction Narration
    val reference: String = "",       // Cheque #, Mill Delivery Note, Gate Pass #, Bill #
    val lotNumber: String = "",       // Linked Lot # e.g. "LOT-2026-801"
    val saleOrderNumber: String = "", // Linked SO # e.g. "SO-2026-101"
    val partyName: String = "",       // Vendor / Customer / Mill Name
    val quantity: Double = 0.0,       // Quantitative: Meters or Finished Pieces
    val unitMeasure: String = "Meters", // "Meters", "Pieces", "Sets", "Rolls"
    val unitRate: Double = 0.0,       // Rate per meter or piece
    val totalAmount: Double = 0.0,    // Financial Value
    val status: String = "POSTED"     // POSTED, DRAFT
)

@Entity(
    tableName = "voucher_lines",
    foreignKeys = [
        ForeignKey(
            entity = VoucherEntity::class,
            parentColumns = ["id"],
            childColumns = ["voucherId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("voucherId"),
        Index("accountId"),
        Index("lotNumber")
    ]
)
data class VoucherLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherId: Long,
    val accountId: Long,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val quantity: Double = 0.0,       // Quantitative impact on specific account (e.g. +5000m)
    val memo: String = "",
    val lotNumber: String = ""
)

data class VoucherWithLines(
    @Embedded val voucher: VoucherEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "voucherId"
    )
    val lines: List<VoucherLineEntity>
)
