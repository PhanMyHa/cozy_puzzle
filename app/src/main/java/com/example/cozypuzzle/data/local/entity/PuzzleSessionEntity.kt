package com.example.cozypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puzzle_sessions")
data class PuzzleSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val puzzleMode: String,
    val difficulty: String,
    val boardStateJson: String,
    val elapsedTimeMs: Long,
    val moveCount: Int,
    val hintsUsed: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
