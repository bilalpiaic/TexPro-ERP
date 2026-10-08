package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AccountEntity
import com.example.data.model.LotEntity
import com.example.data.model.OrgMember
import com.example.data.model.OrgMemberRole
import com.example.data.model.OrganizationEntity
import com.example.data.model.SaaSUserProfile
import com.example.data.model.SaleOrderEntity
import com.example.data.model.VoucherWithLines
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreSaasRepo"

class FirestoreSaasRepository(private val context: Context) {

    // Mandatory: Initialize Firestore with custom DB ID from firebase_applet_config.xml
    private val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    suspend fun saveUserProfile(profile: SaaSUserProfile) {
        try {
            val userDoc = firestore.collection("users").document(profile.uid)
            val data = mapOf(
                "id" to profile.uid,
                "email" to profile.email,
                "displayName" to profile.displayName,
                "activeOrgId" to profile.activeOrgId,
                "joinedOrgIds" to profile.joinedOrgIds,
                "updatedAt" to System.currentTimeMillis()
            )
            userDoc.set(data, SetOptions.merge()).await()
            Log.d(TAG, "User profile saved for: ${profile.uid}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile: ${e.message}", e)
        }
    }

    suspend fun getUserProfile(uid: String): SaaSUserProfile? {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                val email = snapshot.getString("email") ?: ""
                val displayName = snapshot.getString("displayName") ?: ""
                val activeOrgId = snapshot.getString("activeOrgId") ?: "org_default"
                val joinedOrgIds = (snapshot.get("joinedOrgIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                SaaSUserProfile(uid, email, displayName, activeOrgId, joinedOrgIds)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile: ${e.message}", e)
            null
        }
    }

    suspend fun createOrganization(org: OrganizationEntity, ownerUser: SaaSUserProfile): Boolean {
        return try {
            val orgDoc = firestore.collection("organizations").document(org.id)
            val memberUids = listOf(ownerUser.uid)
            val orgData = mapOf(
                "id" to org.id,
                "name" to org.name,
                "code" to org.code,
                "ownerUid" to ownerUser.uid,
                "currency" to org.currency,
                "taxId" to org.taxId,
                "millAddress" to org.millAddress,
                "contactEmail" to org.contactEmail,
                "memberUids" to memberUids,
                "createdDate" to org.createdDate
            )
            orgDoc.set(orgData).await()

            // Add owner as OrgMember in subcollection
            val memberDoc = orgDoc.collection("members").document(ownerUser.uid)
            val memberData = mapOf(
                "userId" to ownerUser.uid,
                "orgId" to org.id,
                "email" to ownerUser.email,
                "displayName" to ownerUser.displayName,
                "role" to OrgMemberRole.OWNER.name,
                "joinedDate" to System.currentTimeMillis()
            )
            memberDoc.set(memberData).await()

            // Update user joinedOrgIds
            val updatedJoined = (ownerUser.joinedOrgIds + org.id).distinct()
            saveUserProfile(ownerUser.copy(activeOrgId = org.id, joinedOrgIds = updatedJoined))

            Log.d(TAG, "Created cloud organization: ${org.name} (${org.id})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create organization: ${e.message}", e)
            false
        }
    }

    suspend fun syncTenantDataToCloud(
        orgId: String,
        accounts: List<AccountEntity>,
        vouchers: List<VoucherWithLines>,
        lots: List<LotEntity>,
        saleOrders: List<SaleOrderEntity>
    ) {
        try {
            val orgDoc = firestore.collection("organizations").document(orgId)

            // 1. Sync Accounts
            for (acc in accounts) {
                val accData = mapOf(
                    "id" to acc.id.toString(),
                    "orgId" to orgId,
                    "code" to acc.code,
                    "name" to acc.name,
                    "type" to acc.type.name,
                    "description" to acc.description,
                    "initialBalance" to acc.initialBalance
                )
                orgDoc.collection("accounts").document(acc.id.toString()).set(accData, SetOptions.merge()).await()
            }

            // 2. Sync Vouchers
            for (vWithLines in vouchers) {
                val v = vWithLines.voucher
                val vData = mapOf(
                    "id" to v.id.toString(),
                    "orgId" to orgId,
                    "voucherNumber" to v.voucherNumber,
                    "voucherType" to v.voucherType.name,
                    "date" to v.date,
                    "description" to v.description,
                    "reference" to v.reference,
                    "partyName" to v.partyName,
                    "lotNumber" to v.lotNumber,
                    "saleOrderNumber" to v.saleOrderNumber,
                    "quantity" to v.quantity,
                    "unitMeasure" to v.unitMeasure,
                    "unitRate" to v.unitRate,
                    "totalAmount" to v.totalAmount
                )
                orgDoc.collection("vouchers").document(v.id.toString()).set(vData, SetOptions.merge()).await()
            }

            // 3. Sync Lots
            for (lot in lots) {
                val lotData = mapOf(
                    "id" to lot.id.toString(),
                    "orgId" to orgId,
                    "lotNumber" to lot.lotNumber,
                    "saleOrderId" to lot.saleOrderId,
                    "saleOrderNumber" to lot.saleOrderNumber,
                    "customerName" to lot.customerName,
                    "quality" to lot.quality,
                    "blend" to lot.blend,
                    "width" to lot.width,
                    "greyVendorName" to lot.greyVendorName,
                    "greyMeters" to lot.greyMeters,
                    "greyRatePerMeter" to lot.greyRatePerMeter,
                    "greyTotalCost" to lot.greyTotalCost,
                    "stage" to lot.stage.name,
                    "totalCost" to lot.totalCost,
                    "saleTotalRevenue" to lot.saleTotalRevenue
                )
                orgDoc.collection("lots").document(lot.id.toString()).set(lotData, SetOptions.merge()).await()
            }

            // 4. Sync Sale Orders
            for (so in saleOrders) {
                val soData = mapOf(
                    "id" to so.id.toString(),
                    "orgId" to orgId,
                    "orderNumber" to so.orderNumber,
                    "customerName" to so.customerName,
                    "orderDate" to so.orderDate,
                    "deliveryDate" to so.deliveryDate,
                    "itemDescription" to so.itemDescription,
                    "quality" to so.quality,
                    "blend" to so.blend,
                    "width" to so.width,
                    "orderedPieces" to so.orderedPieces,
                    "targetMeters" to so.targetMeters,
                    "unitPrice" to so.unitPrice,
                    "status" to so.status
                )
                orgDoc.collection("sale_orders").document(so.id.toString()).set(soData, SetOptions.merge()).await()
            }

            Log.d(TAG, "Completed cloud tenant sync for org: $orgId")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing tenant data: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteUserCloudData(uid: String) {
        try {
            firestore.collection("users").document(uid).delete().await()
            Log.d(TAG, "Deleted Firestore profile for $uid")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user profile: ${e.message}", e)
            throw e
        }
    }
}
