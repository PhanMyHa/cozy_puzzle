package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.DailyPuzzle

interface DailyPuzzleRepository {

    suspend fun getDailyPuzzles(): List<DailyPuzzle>

    suspend fun getDailyPuzzle(puzzleId: String): DailyPuzzle?
}
