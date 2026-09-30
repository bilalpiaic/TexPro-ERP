package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LotEntity
import com.example.data.model.LotStage
import com.example.data.model.SaleOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TextileDao {
    // Sale Orders
    @Query("SELECT * FROM sale_orders ORDER BY orderDate DESC")
    fun getAllSaleOrders(): Flow<List<SaleOrderEntity>>

    @Query("SELECT * FROM sale_orders WHERE id = :id LIMIT 1")
    suspend fun getSaleOrderById(id: Long): SaleOrderEntity?

    @Query("SELECT * FROM sale_orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getSaleOrderByNumber(orderNumber: String): SaleOrderEntity?

    @Query("SELECT COUNT(*) FROM sale_orders")
    suspend fun getSaleOrderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleOrder(order: SaleOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleOrders(orders: List<SaleOrderEntity>)

    @Update
    suspend fun updateSaleOrder(order: SaleOrderEntity)

    // Lots
    @Query("SELECT * FROM lots ORDER BY id DESC")
    fun getAllLots(): Flow<List<LotEntity>>

    @Query("SELECT * FROM lots WHERE lotNumber = :lotNumber LIMIT 1")
    suspend fun getLotByNumber(lotNumber: String): LotEntity?

    @Query("SELECT * FROM lots WHERE saleOrderId = :saleOrderId")
    fun getLotsForSaleOrder(saleOrderId: Long): Flow<List<LotEntity>>

    @Query("SELECT COUNT(*) FROM lots")
    suspend fun getLotCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: LotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLots(lots: List<LotEntity>)

    @Update
    suspend fun updateLot(lot: LotEntity)

    @Delete
    suspend fun deleteLot(lot: LotEntity)
}
