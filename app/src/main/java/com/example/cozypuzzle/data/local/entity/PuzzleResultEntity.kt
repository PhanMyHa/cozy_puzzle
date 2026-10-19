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

    /** Daily Draw only: number of failed attempts before completing. */
    val attemptCount: Int? = null,

    /** Daily Draw only: id of the brush color used. */
    val brushColorId: String? = null,

    val completedAtEpochMs: Long
)
