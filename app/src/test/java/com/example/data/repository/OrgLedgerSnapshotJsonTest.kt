package com.example.data.repository

import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.OrgLedgerSnapshot
import com.example.data.model.OrganizationEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OrgLedgerSnapshotJsonTest {
    @Test
    fun roundTripsOrganizationAndAccounts() {
        val org = OrganizationEntity(
            id = "org_1",
            name = "Master Textile",
            code = "MTM-101",
            ownerUid = "google-1",
            currency = "Rs.",
            taxId = "NTN-1",
            millAddress = "Faisalabad",
            contactEmail = "mill@example.com"
        )
        val snapshot = OrgLedgerSnapshot(
            organization = org,
            organizations = listOf(org),
            accounts = listOf(
                AccountEntity(
                    id = 1,
                    orgId = org.id,
                    code = "1010",
                    name = "Cash",
                    type = AccountType.ASSET,
                    initialBalance = 100.0
                )
            )
        )
        val restored = snapshotFromJson(snapshotToJson(snapshot))
        assertEquals(org.id, restored.organization.id)
        assertEquals(org.name, restored.organization.name)
        assertEquals(1, restored.accounts.size)
        assertEquals("1010", restored.accounts.first().code)
        assertEquals(100.0, restored.accounts.first().initialBalance, 0.001)
    }
}
