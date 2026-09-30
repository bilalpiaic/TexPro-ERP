package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherLineEntity
import com.example.data.model.VoucherType
import com.example.data.model.VoucherWithLines
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Transaction
    @Query("SELECT * FROM vouchers ORDER BY date DESC, id DESC")
    fun getAllVouchersWithLines(): Flow<List<VoucherWithLines>>

    @Transaction
    @Query("SELECT * FROM vouchers WHERE voucherType = :type ORDER BY date DESC, id DESC")
    fun getVouchersByType(type: VoucherType): Flow<List<VoucherWithLines>>

    @Transaction
    @Query("SELECT * FROM vouchers WHERE lotNumber = :lotNumber ORDER BY date ASC, id ASC")
    fun getVouchersForLot(lotNumber: String): Flow<List<VoucherWithLines>>

    @Query("SELECT * FROM voucher_lines")
    fun getAllVoucherLines(): Flow<List<VoucherLineEntity>>

    @Query("SELECT * FROM voucher_lines WHERE accountId = :accountId")
    fun getLinesForAccount(accountId: Long): Flow<List<VoucherLineEntity>>

    @Query("SELECT * FROM voucher_lines WHERE lotNumber = :lotNumber")
    fun getLinesForLot(lotNumber: String): Flow<List<VoucherLineEntity>>

    @Query("SELECT COUNT(*) FROM vouchers")
    suspend fun getVoucherCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<VoucherLineEntity>)

    @Transaction
    suspend fun insertCompleteVoucher(voucher: VoucherEntity, lines: List<VoucherLineEntity>): Long {
        val voucherId = insertVoucher(voucher)
        val linkedLines = lines.map { it.copy(voucherId = voucherId) }
        insertLines(linkedLines)
        return voucherId
    }

    @Delete
    suspend fun deleteVoucher(voucher: VoucherEntity)
}
