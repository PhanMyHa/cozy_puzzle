package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {

    fun observePlayer(): Flow<Player>

    suspend fun updatePlayerName(name: String)

    suspend fun addCoins(amount: Int)
}
