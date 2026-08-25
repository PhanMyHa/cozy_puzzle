package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.BasicPuzzle

interface BasicPuzzleRepository {

    suspend fun getBasicPuzzles(): List<BasicPuzzle>

    suspend fun getBasicPuzzle(puzzleId: String): BasicPuzzle?
}
