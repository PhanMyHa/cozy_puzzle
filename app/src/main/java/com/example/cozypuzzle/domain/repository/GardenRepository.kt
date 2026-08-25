package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.GardenItem
import kotlinx.coroutines.flow.Flow

interface GardenRepository {

    fun observeGardenItems(): Flow<List<GardenItem>>

    suspend fun upsertItem(item: GardenItem)

    suspend fun removeItem(itemId: String)
}
