package com.example.cozypuzzle.domain.usecase.shop

import com.example.cozypuzzle.domain.model.ItemCategory
import com.example.cozypuzzle.domain.model.ItemRarity
import com.example.cozypuzzle.domain.model.Player
import com.example.cozypuzzle.domain.model.ShopItem
import com.example.cozypuzzle.domain.repository.AvatarRepository
import com.example.cozypuzzle.domain.repository.InventoryRepository
import com.example.cozypuzzle.domain.repository.PlayerRepository
import com.example.cozypuzzle.domain.repository.ShopRepository
import com.example.cozypuzzle.domain.usecase.profile.SavePlayerAvatarUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseItemUseCaseTest {

    private val catalog = listOf(
        item("flower_rose", "Rose", priceCoins = 60),
        item("pet_owl", "Owl", priceCoins = 1600)
    )

    private val coinsFlow = MutableStateFlow(100)
    private val ownedIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    private var coinsDelta = 0
    private val itemsAdded = mutableListOf<String>()

    @Test
    fun `buying an affordable item deducts coins and stores it`() = runTest {
        val result = useCase()("flower_rose")

        assertTrue(result is PurchaseResult.Success)
        assertEquals(40, (result as PurchaseResult.Success).coinsLeft)
        assertEquals(-60, coinsDelta)
        assertEquals(listOf("flower_rose"), itemsAdded)
    }

    @Test
    fun `buying with insufficient coins fails and changes nothing`() = runTest {
        val result = useCase()("pet_owl")

        assertEquals(PurchaseResult.InsufficientCoins, result)
        assertEquals(0, coinsDelta)
        assertTrue(itemsAdded.isEmpty())
    }

    @Test
    fun `buying an already owned item fails without double charging`() = runTest {
        ownedIdsFlow.value = setOf("flower_rose")

        assertEquals(PurchaseResult.AlreadyOwned, useCase()("flower_rose"))
        assertEquals(0, coinsDelta)
    }

    @Test
    fun `buying twice only succeeds once`() = runTest {
        assertTrue(useCase()("flower_rose") is PurchaseResult.Success)

        assertEquals(PurchaseResult.AlreadyOwned, useCase()("flower_rose"))
        assertEquals(-60, coinsDelta)
    }

    @Test
    fun `unknown item id reports not found`() = runTest {
        assertEquals(PurchaseResult.ItemNotFound, useCase()("does_not_exist"))
    }

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private fun useCase() = PurchaseItemUseCase(
        shopRepository = object : ShopRepository {
            override suspend fun getShopItems(): List<ShopItem> = catalog
        },
        inventoryRepository = object : InventoryRepository {
            override fun observeOwnedItemIds(): Flow<Set<String>> = ownedIdsFlow

            override suspend fun addItem(itemId: String) {
                itemsAdded += itemId
                ownedIdsFlow.value = ownedIdsFlow.value + itemId
            }
        },
        playerRepository = object : PlayerRepository {
            override fun observePlayer(): Flow<Player> =
                coinsFlow.map { Player(name = "", avatarId = 0, level = 1, coins = it, gems = 0) }

            override suspend fun updatePlayerName(name: String) = Unit

            override suspend fun updateAvatarUri(avatarUri: String?) = Unit

            override suspend fun addCoins(amount: Int) {
                coinsDelta += amount
                coinsFlow.value = (coinsFlow.value + amount).coerceAtLeast(0)
            }
        }
    )

    private fun item(id: String, name: String, priceCoins: Int): ShopItem = ShopItem(
        id = id,
        name = name,
        emoji = "🌼",
        category = ItemCategory.FLOWERS,
        rarity = ItemRarity.COMMON,
        priceCoins = priceCoins
    )
}

class SavePlayerAvatarUseCaseTest {

    @Test
    fun `successful import persists the new path and prunes old photos`() = runTest {
        val playerRepo = FakePlayerRepo()
        var prunedKeepPath: String? = NO_PRUNE
        val useCase = SavePlayerAvatarUseCase(
            avatarRepository = object : AvatarRepository {
                override suspend fun importAvatar(sourceUri: String): String? =
                    "/data/avatars/new.jpg"

                override suspend fun pruneAvatars(keepPath: String?) {
                    prunedKeepPath = keepPath
                }
            },
            playerRepository = playerRepo
        )

        val saved = useCase("content://gallery/1")

        assertTrue(saved)
        assertEquals("/data/avatars/new.jpg", playerRepo.current.avatarUri)
        assertEquals("/data/avatars/new.jpg", prunedKeepPath)
    }

    @Test
    fun `unreadable source fails without touching the profile`() = runTest {
        val playerRepo = FakePlayerRepo()
        val useCase = SavePlayerAvatarUseCase(
            avatarRepository = object : AvatarRepository {
                override suspend fun importAvatar(sourceUri: String): String? = null

                override suspend fun pruneAvatars(keepPath: String?) = Unit
            },
            playerRepository = playerRepo
        )

        assertFalse(useCase("content://gone"))
        assertEquals(null, playerRepo.current.avatarUri)
    }

    private companion object {
        const val NO_PRUNE = "__never__"
    }
}

private class FakePlayerRepo : PlayerRepository {

    private val flow = MutableStateFlow(Player.DEFAULT)

    val current: Player get() = flow.value

    override fun observePlayer(): Flow<Player> = flow

    override suspend fun updatePlayerName(name: String) {
        flow.value = flow.value.copy(name = name.trim())
    }

    override suspend fun updateAvatarUri(avatarUri: String?) {
        flow.value = flow.value.copy(avatarUri = avatarUri)
    }

    override suspend fun addCoins(amount: Int) = Unit
}
