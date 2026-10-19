package com.example.cozypuzzle.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * The player's purchased items, keyed by shop item id.
 */
interface InventoryRepository {

    fun observeOwnedItemIds(): Flow<Set<String>>

    suspend fun addItem(itemId: String)
}
