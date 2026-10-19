package com.example.cozypuzzle.domain.usecase.shop

import com.example.cozypuzzle.domain.model.ItemCategory
import com.example.cozypuzzle.domain.model.ItemRarity
import com.example.cozypuzzle.domain.model.ShopItem
import com.example.cozypuzzle.domain.repository.InventoryRepository
import com.example.cozypuzzle.domain.repository.ShopRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetShopItemsUseCaseTest {

    @Test
    fun `catalog is annotated with owned items`() = runTest {
        val ownedFlow = MutableStateFlow<Set<String>>(setOf("flower_rose"))
        val useCase = GetShopItemsUseCase(
            shopRepository = object : ShopRepository {
                override suspend fun getShopItems(): List<ShopItem> =
                    listOf(item("flower_rose"), item("pet_owl"))
            },
            inventoryRepository = object : InventoryRepository {
                override fun observeOwnedItemIds(): Flow<Set<String>> = ownedFlow

                override suspend fun addItem(itemId: String) = Unit
            }
        )

        val items = useCase().first()

        assertTrue(items.first { it.id == "flower_rose" }.isOwned)
        assertFalse(items.first { it.id == "pet_owl" }.isOwned)
    }

    private fun item(id: String): ShopItem = ShopItem(
        id = id,
        name = id,
        emoji = "🌼",
        category = ItemCategory.FLOWERS,
        rarity = ItemRarity.COMMON,
        priceCoins = 10
    )
}
