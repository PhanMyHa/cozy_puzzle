package com.example.cozypuzzle.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cozypuzzle.data.local.entity.PuzzleResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleResultDao {

    @Query("SELECT * FROM puzzle_results ORDER BY completedAtEpochMs DESC")
    fun observeAll(): Flow<List<PuzzleResultEntity>>

    @Query(
        "SELECT * FROM puzzle_results WHERE puzzleId = :puzzleId " +
            "ORDER BY durationMs ASC, completedAtEpochMs ASC"
    )
    suspend fun getByPuzzleId(puzzleId: String): List<PuzzleResultEntity>

    @Insert
    suspend fun insert(result: PuzzleResultEntity): Long
}
