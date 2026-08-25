package com.example.cozypuzzle.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.cozypuzzle.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player WHERE id = :playerId")
    fun observeById(playerId: Long): Flow<PlayerEntity?>

    @Query("SELECT * FROM player WHERE id = :playerId")
    suspend fun getById(playerId: Long): PlayerEntity?

    @Upsert
    suspend fun upsert(player: PlayerEntity)
}
