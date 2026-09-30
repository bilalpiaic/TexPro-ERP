package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrgMemberRole(val displayName: String, val description: String) {
    OWNER("Owner", "Full Administrative, Financial & Operational Authority"),
    ADMIN("Administrator", "User Management, Mill Setup & System Parameters"),
    CHIEF_ACCOUNTANT("Chief Accountant", "General Ledger, Vouchers Posting & Financial Reports"),
    PRODUCTION_MANAGER("Production Manager", "Grey Cloth Procurement, Dye/Print, Stitching & Lot Sequences"),
    VIEWER("Auditor / Viewer", "Read-only access to ledgers, stock registers and balance sheets")
}

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,                // e.g. "TXP-8491"
    val ownerUid: String,
    val currency: String = "Rs.",
    val taxId: String = "",          // NTN / Sales Tax registration
    val millAddress: String = "",
    val contactEmail: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val isCloudSynced: Boolean = true
)

data class OrgMember(
    val userId: String,
    val orgId: String,
    val email: String,
    val displayName: String,
    val role: OrgMemberRole,
    val joinedDate: Long = System.currentTimeMillis()
)

data class SaaSUserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val activeOrgId: String,
    val joinedOrgIds: List<String> = emptyList()
)
