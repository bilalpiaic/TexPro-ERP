package com.example.data.repository

import android.util.Log
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.LotEntity
import com.example.data.model.LotStage
import com.example.data.model.OrgLedgerSnapshot
import com.example.data.model.OrganizationEntity
import com.example.data.model.SaleOrderEntity
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherLineEntity
import com.example.data.model.VoucherType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "GoogleDriveLedger"
private const val DRIVE_FILES = "https://www.googleapis.com/drive/v3/files"
private const val DRIVE_UPLOAD = "https://www.googleapis.com/upload/drive/v3/files"
private const val ROOT_FOLDER = "TexPro ERP"
private val JSON = "application/json; charset=UTF-8".toMediaType()

class GoogleDriveLedgerStore(
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    suspend fun saveSnapshot(accessToken: String, snapshot: OrgLedgerSnapshot): String = withContext(Dispatchers.IO) {
        val rootId = findOrCreateFolder(accessToken, ROOT_FOLDER, parentId = "root")
        val orgFolderName = "${snapshot.organization.code} - ${snapshot.organization.name}".take(120)
        val orgFolderId = findOrCreateFolder(accessToken, orgFolderName, parentId = rootId)
        val fileName = "texpro-ledger.json"
        val json = snapshotToJson(snapshot)
        val existing = findChildFile(accessToken, orgFolderId, fileName)
        if (existing != null) {
            updateFile(accessToken, existing, json)
            existing
        } else {
            createFile(accessToken, orgFolderId, fileName, json)
        }
    }

    suspend fun loadSnapshot(accessToken: String, organization: OrganizationEntity): OrgLedgerSnapshot? =
        withContext(Dispatchers.IO) {
            val rootId = findFolder(accessToken, ROOT_FOLDER, parentId = "root") ?: return@withContext null
            val orgFolderName = "${organization.code} - ${organization.name}".take(120)
            val orgFolderId = findFolder(accessToken, orgFolderName, parentId = rootId) ?: return@withContext null
            val fileId = findChildFile(accessToken, orgFolderId, "texpro-ledger.json") ?: return@withContext null
            val json = downloadFile(accessToken, fileId)
            snapshotFromJson(json)
        }

    private fun findOrCreateFolder(accessToken: String, name: String, parentId: String): String {
        return findFolder(accessToken, name, parentId) ?: createFolder(accessToken, name, parentId)
    }

    private fun findFolder(accessToken: String, name: String, parentId: String): String? {
        val escaped = name.replace("'", "\\'")
        val q = "name='$escaped' and mimeType='application/vnd.google-apps.folder' and '$parentId' in parents and trashed=false"
        val url = "$DRIVE_FILES?spaces=drive&fields=files(id,name)&q=${java.net.URLEncoder.encode(q, "UTF-8")}"
        val body = authorizedGet(accessToken, url)
        val files = JSONObject(body).optJSONArray("files") ?: JSONArray()
        return if (files.length() > 0) files.getJSONObject(0).optString("id").takeIf { it.isNotBlank() } else null
    }

    private fun findChildFile(accessToken: String, parentId: String, name: String): String? {
        val escaped = name.replace("'", "\\'")
        val q = "name='$escaped' and '$parentId' in parents and trashed=false"
        val url = "$DRIVE_FILES?spaces=drive&fields=files(id,name)&q=${java.net.URLEncoder.encode(q, "UTF-8")}"
        val body = authorizedGet(accessToken, url)
        val files = JSONObject(body).optJSONArray("files") ?: JSONArray()
        return if (files.length() > 0) files.getJSONObject(0).optString("id").takeIf { it.isNotBlank() } else null
    }

    private fun createFolder(accessToken: String, name: String, parentId: String): String {
        val metadata = JSONObject()
            .put("name", name)
            .put("mimeType", "application/vnd.google-apps.folder")
            .put("parents", JSONArray().put(parentId))
        val request = Request.Builder()
            .url(DRIVE_FILES)
            .header("Authorization", "Bearer $accessToken")
            .post(metadata.toString().toRequestBody(JSON))
            .build()
        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Could not create Drive folder '$name' (${response.code}): $body")
            }
            return JSONObject(body).getString("id")
        }
    }

    private fun createFile(accessToken: String, parentId: String, name: String, json: String): String {
        val metadata = JSONObject()
            .put("name", name)
            .put("mimeType", "application/json")
            .put("parents", JSONArray().put(parentId))
        val multipart = MultipartBody.Builder()
            .setType("multipart/related".toMediaType())
            .addPart(metadata.toString().toRequestBody(JSON))
            .addPart(json.toRequestBody(JSON))
            .build()
        val request = Request.Builder()
            .url("$DRIVE_UPLOAD?uploadType=multipart")
            .header("Authorization", "Bearer $accessToken")
            .post(multipart)
            .build()
        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Could not create Drive ledger file (${response.code}): $body")
            }
            return JSONObject(body).getString("id")
        }
    }

    private fun updateFile(accessToken: String, fileId: String, json: String) {
        val request = Request.Builder()
            .url("$DRIVE_UPLOAD/$fileId?uploadType=media")
            .header("Authorization", "Bearer $accessToken")
            .patch(json.toRequestBody(JSON))
            .build()
        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Could not update Drive ledger file (${response.code}): $body")
            }
        }
    }

    private fun downloadFile(accessToken: String, fileId: String): String {
        return authorizedGet(accessToken, "$DRIVE_FILES/$fileId?alt=media")
    }

    private fun authorizedGet(accessToken: String, url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()
        http.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.e(TAG, "Drive GET ${response.code}: $body")
                throw IllegalStateException("Google Drive request failed (${response.code}).")
            }
            return body
        }
    }
}

internal fun snapshotToJson(snapshot: OrgLedgerSnapshot): String {
    val root = JSONObject()
        .put("version", snapshot.version)
        .put("exportedAt", snapshot.exportedAt)
        .put("organization", orgToJson(snapshot.organization))
        .put("organizations", JSONArray().also { arr -> snapshot.organizations.forEach { arr.put(orgToJson(it)) } })
        .put("accounts", JSONArray().also { arr -> snapshot.accounts.forEach { arr.put(accountToJson(it)) } })
        .put("vouchers", JSONArray().also { arr -> snapshot.vouchers.forEach { arr.put(voucherToJson(it)) } })
        .put("voucherLines", JSONArray().also { arr -> snapshot.voucherLines.forEach { arr.put(lineToJson(it)) } })
        .put("saleOrders", JSONArray().also { arr -> snapshot.saleOrders.forEach { arr.put(saleOrderToJson(it)) } })
        .put("lots", JSONArray().also { arr -> snapshot.lots.forEach { arr.put(lotToJson(it)) } })
    return root.toString()
}

internal fun snapshotFromJson(raw: String): OrgLedgerSnapshot {
    val json = JSONObject(raw)
    return OrgLedgerSnapshot(
        version = json.optInt("version", 1),
        exportedAt = json.optLong("exportedAt", System.currentTimeMillis()),
        organization = orgFromJson(json.getJSONObject("organization")),
        organizations = jsonArray(json, "organizations") { orgFromJson(it) },
        accounts = jsonArray(json, "accounts") { accountFromJson(it) },
        vouchers = jsonArray(json, "vouchers") { voucherFromJson(it) },
        voucherLines = jsonArray(json, "voucherLines") { lineFromJson(it) },
        saleOrders = jsonArray(json, "saleOrders") { saleOrderFromJson(it) },
        lots = jsonArray(json, "lots") { lotFromJson(it) }
    )
}

private fun <T> jsonArray(json: JSONObject, key: String, map: (JSONObject) -> T): List<T> {
    val arr = json.optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until arr.length()) add(map(arr.getJSONObject(i)))
    }
}

private fun orgToJson(org: OrganizationEntity) = JSONObject()
    .put("id", org.id)
    .put("name", org.name)
    .put("code", org.code)
    .put("ownerUid", org.ownerUid)
    .put("currency", org.currency)
    .put("taxId", org.taxId)
    .put("millAddress", org.millAddress)
    .put("contactEmail", org.contactEmail)
    .put("createdDate", org.createdDate)
    .put("isCloudSynced", org.isCloudSynced)

private fun orgFromJson(json: JSONObject) = OrganizationEntity(
    id = json.getString("id"),
    name = json.optString("name"),
    code = json.optString("code"),
    ownerUid = json.optString("ownerUid"),
    currency = json.optString("currency", "Rs."),
    taxId = json.optString("taxId"),
    millAddress = json.optString("millAddress"),
    contactEmail = json.optString("contactEmail"),
    createdDate = json.optLong("createdDate", System.currentTimeMillis()),
    isCloudSynced = json.optBoolean("isCloudSynced", true)
)

private fun accountToJson(acc: AccountEntity) = JSONObject()
    .put("id", acc.id)
    .put("orgId", acc.orgId)
    .put("code", acc.code)
    .put("name", acc.name)
    .put("type", acc.type.name)
    .put("description", acc.description)
    .put("initialBalance", acc.initialBalance)
    .put("isSystem", acc.isSystem)

private fun accountFromJson(json: JSONObject) = AccountEntity(
    id = json.optLong("id"),
    orgId = json.optString("orgId", "org_default"),
    code = json.optString("code"),
    name = json.optString("name"),
    type = runCatching { AccountType.valueOf(json.optString("type", "ASSET")) }.getOrDefault(AccountType.ASSET),
    description = json.optString("description"),
    initialBalance = json.optDouble("initialBalance", 0.0),
    isSystem = json.optBoolean("isSystem", false)
)

private fun voucherToJson(v: VoucherEntity) = JSONObject()
    .put("id", v.id)
    .put("orgId", v.orgId)
    .put("voucherNumber", v.voucherNumber)
    .put("voucherType", v.voucherType.name)
    .put("date", v.date)
    .put("description", v.description)
    .put("reference", v.reference)
    .put("lotNumber", v.lotNumber)
    .put("saleOrderNumber", v.saleOrderNumber)
    .put("partyName", v.partyName)
    .put("quantity", v.quantity)
    .put("unitMeasure", v.unitMeasure)
    .put("unitRate", v.unitRate)
    .put("totalAmount", v.totalAmount)
    .put("status", v.status)

private fun voucherFromJson(json: JSONObject) = VoucherEntity(
    id = json.optLong("id"),
    orgId = json.optString("orgId", "org_default"),
    voucherNumber = json.optString("voucherNumber"),
    voucherType = runCatching { VoucherType.valueOf(json.optString("voucherType", "JV")) }.getOrDefault(VoucherType.JV),
    date = json.optLong("date", System.currentTimeMillis()),
    description = json.optString("description"),
    reference = json.optString("reference"),
    lotNumber = json.optString("lotNumber"),
    saleOrderNumber = json.optString("saleOrderNumber"),
    partyName = json.optString("partyName"),
    quantity = json.optDouble("quantity", 0.0),
    unitMeasure = json.optString("unitMeasure", "Meters"),
    unitRate = json.optDouble("unitRate", 0.0),
    totalAmount = json.optDouble("totalAmount", 0.0),
    status = json.optString("status", "POSTED")
)

private fun lineToJson(line: VoucherLineEntity) = JSONObject()
    .put("id", line.id)
    .put("voucherId", line.voucherId)
    .put("accountId", line.accountId)
    .put("debit", line.debit)
    .put("credit", line.credit)
    .put("quantity", line.quantity)
    .put("memo", line.memo)
    .put("lotNumber", line.lotNumber)

private fun lineFromJson(json: JSONObject) = VoucherLineEntity(
    id = json.optLong("id"),
    voucherId = json.optLong("voucherId"),
    accountId = json.optLong("accountId"),
    debit = json.optDouble("debit", 0.0),
    credit = json.optDouble("credit", 0.0),
    quantity = json.optDouble("quantity", 0.0),
    memo = json.optString("memo"),
    lotNumber = json.optString("lotNumber")
)

private fun saleOrderToJson(so: SaleOrderEntity) = JSONObject()
    .put("id", so.id)
    .put("orgId", so.orgId)
    .put("orderNumber", so.orderNumber)
    .put("customerName", so.customerName)
    .put("orderDate", so.orderDate)
    .put("deliveryDate", so.deliveryDate)
    .put("itemDescription", so.itemDescription)
    .put("quality", so.quality)
    .put("blend", so.blend)
    .put("width", so.width)
    .put("orderedPieces", so.orderedPieces)
    .put("targetMeters", so.targetMeters)
    .put("unitPrice", so.unitPrice)
    .put("status", so.status)
    .put("notes", so.notes)

private fun saleOrderFromJson(json: JSONObject) = SaleOrderEntity(
    id = json.optLong("id"),
    orgId = json.optString("orgId", "org_default"),
    orderNumber = json.optString("orderNumber"),
    customerName = json.optString("customerName"),
    orderDate = json.optLong("orderDate", System.currentTimeMillis()),
    deliveryDate = json.optLong("deliveryDate", System.currentTimeMillis()),
    itemDescription = json.optString("itemDescription"),
    quality = json.optString("quality"),
    blend = json.optString("blend"),
    width = json.optString("width"),
    orderedPieces = json.optInt("orderedPieces"),
    targetMeters = json.optDouble("targetMeters", 0.0),
    unitPrice = json.optDouble("unitPrice", 0.0),
    status = json.optString("status", "IN_PRODUCTION"),
    notes = json.optString("notes")
)

private fun lotToJson(lot: LotEntity) = JSONObject()
    .put("id", lot.id)
    .put("orgId", lot.orgId)
    .put("lotNumber", lot.lotNumber)
    .put("saleOrderId", lot.saleOrderId)
    .put("saleOrderNumber", lot.saleOrderNumber)
    .put("customerName", lot.customerName)
    .put("quality", lot.quality)
    .put("blend", lot.blend)
    .put("width", lot.width)
    .put("greyVendorName", lot.greyVendorName)
    .put("greyMeters", lot.greyMeters)
    .put("greyRatePerMeter", lot.greyRatePerMeter)
    .put("greyTotalCost", lot.greyTotalCost)
    .put("processorName", lot.processorName)
    .put("processType", lot.processType)
    .put("processedMeters", lot.processedMeters)
    .put("processingRatePerMeter", lot.processingRatePerMeter)
    .put("processingTotalCost", lot.processingTotalCost)
    .put("stitcherName", lot.stitcherName)
    .put("finishedUnits", lot.finishedUnits)
    .put("stitchingRatePerUnit", lot.stitchingRatePerUnit)
    .put("stitchingTotalCost", lot.stitchingTotalCost)
    .put("stage", lot.stage.name)
    .put("saleRatePerUnit", lot.saleRatePerUnit)
    .put("saleTotalRevenue", lot.saleTotalRevenue)
    .put("saleInvoiceNumber", lot.saleInvoiceNumber)
    .put("notes", lot.notes)

private fun lotFromJson(json: JSONObject) = LotEntity(
    id = json.optLong("id"),
    orgId = json.optString("orgId", "org_default"),
    lotNumber = json.optString("lotNumber"),
    saleOrderId = json.optLong("saleOrderId"),
    saleOrderNumber = json.optString("saleOrderNumber"),
    customerName = json.optString("customerName"),
    quality = json.optString("quality"),
    blend = json.optString("blend"),
    width = json.optString("width"),
    greyVendorName = json.optString("greyVendorName"),
    greyMeters = json.optDouble("greyMeters", 0.0),
    greyRatePerMeter = json.optDouble("greyRatePerMeter", 0.0),
    greyTotalCost = json.optDouble("greyTotalCost", 0.0),
    processorName = json.optString("processorName"),
    processType = json.optString("processType"),
    processedMeters = json.optDouble("processedMeters", 0.0),
    processingRatePerMeter = json.optDouble("processingRatePerMeter", 0.0),
    processingTotalCost = json.optDouble("processingTotalCost", 0.0),
    stitcherName = json.optString("stitcherName"),
    finishedUnits = json.optInt("finishedUnits"),
    stitchingRatePerUnit = json.optDouble("stitchingRatePerUnit", 0.0),
    stitchingTotalCost = json.optDouble("stitchingTotalCost", 0.0),
    stage = runCatching { LotStage.valueOf(json.optString("stage", "GREY_RECEIVED")) }.getOrDefault(LotStage.GREY_RECEIVED),
    saleRatePerUnit = json.optDouble("saleRatePerUnit", 0.0),
    saleTotalRevenue = json.optDouble("saleTotalRevenue", 0.0),
    saleInvoiceNumber = json.optString("saleInvoiceNumber"),
    notes = json.optString("notes")
)
