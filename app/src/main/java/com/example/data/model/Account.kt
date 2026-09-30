package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType(val displayName: String, val normalBalance: String) {
    ASSET("Asset", "Debit"),
    LIABILITY("Liability", "Credit"),
    EQUITY("Equity", "Credit"),
    REVENUE("Revenue", "Credit"),
    EXPENSE("Expense", "Debit")
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orgId: String = "org_default",
    val code: String,          // e.g. "1010"
    val name: String,          // e.g. "Operating Cash Account"
    val type: AccountType,     // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    val description: String = "",
    val initialBalance: Double = 0.0,
    val isSystem: Boolean = false
)

data class AccountWithBalance(
    val account: AccountEntity,
    val currentBalance: Double,
    val totalDebit: Double,
    val totalCredit: Double
)
