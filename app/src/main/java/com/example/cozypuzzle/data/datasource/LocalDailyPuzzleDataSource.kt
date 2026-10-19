package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.domain.model.DailyPuzzle

/**
 * Source of daily puzzle definitions. Kept as an interface so a future
 * Firebase-backed implementation can be swapped in without touching the
 * domain or presentation layers.
 */
interface LocalDailyPuzzleDataSource {

    suspend fun getPuzzles(): List<DailyPuzzle>

    suspend fun getPuzzle(puzzleId: String): DailyPuzzle?
}
