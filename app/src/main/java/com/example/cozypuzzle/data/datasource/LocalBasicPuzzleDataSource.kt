package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.domain.model.BasicPuzzle

interface LocalBasicPuzzleDataSource {

    suspend fun getPuzzles(): List<BasicPuzzle>

    suspend fun getPuzzle(puzzleId: String): BasicPuzzle?
}
