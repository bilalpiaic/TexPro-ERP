package com.example.data.model

data class OrgLedgerSnapshot(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val organization: OrganizationEntity,
    val organizations: List<OrganizationEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val vouchers: List<VoucherEntity> = emptyList(),
    val voucherLines: List<VoucherLineEntity> = emptyList(),
    val saleOrders: List<SaleOrderEntity> = emptyList(),
    val lots: List<LotEntity> = emptyList()
)
