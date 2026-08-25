package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.BasicPuzzleConfig
import kotlinx.coroutines.flow.StateFlow

interface ConfigRepository {

    val basicPuzzleConfig: StateFlow<BasicPuzzleConfig>

    suspend fun refreshBasicPuzzleConfig()
}
