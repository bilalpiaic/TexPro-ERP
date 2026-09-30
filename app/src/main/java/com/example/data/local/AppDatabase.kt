package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.LotEntity
import com.example.data.model.OrganizationEntity
import com.example.data.model.SaleOrderEntity
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherLineEntity

@Database(
    entities = [
        OrganizationEntity::class,
        AccountEntity::class,
        VoucherEntity::class,
        VoucherLineEntity::class,
        SaleOrderEntity::class,
        LotEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun organizationDao(): OrganizationDao
    abstract fun accountDao(): AccountDao
    abstract fun voucherDao(): VoucherDao
    abstract fun textileDao(): TextileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "texpro_textile_erp.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
