package com.example.cozypuzzle.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.cozypuzzle.data.local.entity.GardenItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenItemDao {

    @Query("SELECT * FROM garden_items")
    fun observeAll(): Flow<List<GardenItemEntity>>

    @Upsert
    suspend fun upsert(item: GardenItemEntity)

    @Delete
    suspend fun delete(item: GardenItemEntity)

    @Query("DELETE FROM garden_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)
}
