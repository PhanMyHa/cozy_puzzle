package com.example.cozypuzzle.domain.model

data class DailyPuzzleResult(
    val id: Long = 0L,
    val puzzleId: String,
    val durationMs: Long,
    val attempts: Int,
    val brushColorId: String,
    val completedAtEpochMs: Long
)
