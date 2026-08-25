package com.example.cozypuzzle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cozypuzzle.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory_items ORDER BY acquiredAtEpochMs DESC")
    fun observeAll(): Flow<List<InventoryItemEntity>>

    @Insert
    suspend fun insert(item: InventoryItemEntity)
}
