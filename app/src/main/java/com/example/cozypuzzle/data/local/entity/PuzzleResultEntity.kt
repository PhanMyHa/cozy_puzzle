package com.example.cozypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_results")
data class PuzzleResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val puzzleId: String,
    val puzzleMode: String,
    val difficulty: String,
    val rows: Int,
    val columns: Int,
    val durationMs: Long,
    val moveCount: Int,
    val hintsUsed: Int,
    val completedAtEpochMs: Long
)
