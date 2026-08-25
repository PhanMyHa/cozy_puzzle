package com.example.cozypuzzle.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.cozypuzzle.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Query("SELECT * FROM pets ORDER BY level DESC")
    fun observeAll(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getById(petId: String): PetEntity?

    @Upsert
    suspend fun upsert(pet: PetEntity)
}
