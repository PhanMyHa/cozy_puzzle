package com.example.cozypuzzle.domain.usecase.shop

import com.example.cozypuzzle.domain.model.ShopItem
import com.example.cozypuzzle.domain.repository.InventoryRepository
import com.example.cozypuzzle.domain.repository.PlayerRepository
import com.example.cozypuzzle.domain.repository.ShopRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/** Outcome of a purchase attempt. */
sealed interface PurchaseResult {

    data class Success(val item: ShopItem, val coinsLeft: Int) : PurchaseResult

    data object AlreadyOwned : PurchaseResult

    data object InsufficientCoins : PurchaseResult

    data object ItemNotFound : PurchaseResult
}

/**
 * Buys a catalog item with coins and stores it in the inventory.
 * Coins are only deducted after the item is safely stored.
 */
class PurchaseItemUseCase @Inject constructor(
    private val shopRepository: ShopRepository,
    private val inventoryRepository: InventoryRepository,
    private val playerRepository: PlayerRepository
) {

    suspend operator fun invoke(itemId: String): PurchaseResult {
        val item = shopRepository.getShopItems()
            .firstOrNull { it.id == itemId }
            ?: return PurchaseResult.ItemNotFound

        val ownedIds = inventoryRepository.observeOwnedItemIds().firstOrNull().orEmpty()
        if (item.isOwned || itemId in ownedIds) {
            return PurchaseResult.AlreadyOwned
        }

        val coins = playerRepository.observePlayer().firstOrNull()?.coins ?: 0
        if (coins < item.priceCoins) {
            return PurchaseResult.InsufficientCoins
        }

        inventoryRepository.addItem(itemId)
        playerRepository.addCoins(-item.priceCoins)
        return PurchaseResult.Success(item, coinsLeft = coins - item.priceCoins)
    }
}
