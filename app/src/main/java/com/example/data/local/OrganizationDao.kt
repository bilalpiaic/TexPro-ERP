package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OrganizationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations ORDER BY name ASC")
    fun getAllOrganizations(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    fun getOrganizationById(id: String): Flow<OrganizationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: OrganizationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizations(organizations: List<OrganizationEntity>)

    @Update
    suspend fun updateOrganization(organization: OrganizationEntity)

    @Query("SELECT COUNT(*) FROM organizations")
    suspend fun getOrganizationCount(): Int

    @Delete
    suspend fun deleteOrganization(organization: OrganizationEntity)
}
