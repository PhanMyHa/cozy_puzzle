package com.example.cozypuzzle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cozypuzzle.data.local.entity.PuzzleSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleSessionDao {

    @Query("SELECT * FROM puzzle_sessions ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<PuzzleSessionEntity>>

    @Query("SELECT * FROM puzzle_sessions WHERE id = :sessionId")
    suspend fun getById(sessionId: Long): PuzzleSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PuzzleSessionEntity): Long

    @Update
    suspend fun update(session: PuzzleSessionEntity)

    @Query("DELETE FROM puzzle_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: Long)
}
