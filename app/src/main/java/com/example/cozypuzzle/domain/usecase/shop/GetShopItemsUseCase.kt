package com.example.cozypuzzle.domain.usecase.shop

import com.example.cozypuzzle.domain.model.ShopItem
import com.example.cozypuzzle.domain.repository.InventoryRepository
import com.example.cozypuzzle.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Streams the shop catalog annotated with the player's owned items.
 */
class GetShopItemsUseCase @Inject constructor(
    private val shopRepository: ShopRepository,
    private val inventoryRepository: InventoryRepository
) {

    operator fun invoke(): Flow<List<ShopItem>> =
        inventoryRepository.observeOwnedItemIds().map { ownedIds ->
            shopRepository.getShopItems().map { item ->
                item.copy(isOwned = item.id in ownedIds)
            }
        }
}
