package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.domain.model.ItemCategory
import com.example.cozypuzzle.domain.model.ItemRarity
import com.example.cozypuzzle.domain.model.ShopItem
import javax.inject.Inject

/**
 * Locally bundled cozy catalog. Every item is represented by a cute emoji so
 * no artwork assets are required for this first iteration.
 */
class BundledShopCatalogDataSource @Inject constructor() : LocalShopCatalogDataSource {

    private val catalog: List<ShopItem> = listOf(
        item("flower_rose", "Rose", "🌹", ItemCategory.FLOWERS, ItemRarity.COMMON, 60),
        item("flower_sunflower", "Sunflower", "🌻", ItemCategory.FLOWERS, ItemRarity.COMMON, 80),
        item("flower_tulip", "Tulips", "💐", ItemCategory.FLOWERS, ItemRarity.RARE, 260),
        item("flower_sakura", "Sakura", "🌸", ItemCategory.FLOWERS, ItemRarity.EPIC, 640),

        item("tree_pine", "Pine", "🌲", ItemCategory.TREES, ItemRarity.COMMON, 90),
        item("tree_oak", "Oak", "🌳", ItemCategory.TREES, ItemRarity.COMMON, 110),
        item("tree_palm", "Palm", "🌴", ItemCategory.TREES, ItemRarity.RARE, 300),

        item("house_cottage", "Cottage", "🏡", ItemCategory.BUILDINGS, ItemRarity.RARE, 380),
        item("house_cabin", "Cabin", "🏠", ItemCategory.BUILDINGS, ItemRarity.COMMON, 150),
        item("house_castle", "Castle", "🏰", ItemCategory.BUILDINGS, ItemRarity.LEGENDARY, 1800),

        item("furn_chair", "Cozy Chair", "🪑", ItemCategory.FURNITURE, ItemRarity.COMMON, 70),
        item("furn_bed", "Fluffy Bed", "🛏️", ItemCategory.FURNITURE, ItemRarity.RARE, 240),
        item("furn_lamp", "Warm Lamp", "💡", ItemCategory.FURNITURE, ItemRarity.COMMON, 120),
        item("furn_bath", "Wooden Bathtub", "🛁", ItemCategory.FURNITURE, ItemRarity.EPIC, 720),

        item("decor_lantern", "Lantern", "🏮", ItemCategory.DECORATIONS, ItemRarity.COMMON, 100),
        item("decor_balloon", "Balloon", "🎈", ItemCategory.DECORATIONS, ItemRarity.COMMON, 50),
        item("decor_gift", "Mystery Gift", "🎁", ItemCategory.DECORATIONS, ItemRarity.RARE, 350),
        item("decor_trophy", "Golden Trophy", "🏆", ItemCategory.DECORATIONS, ItemRarity.LEGENDARY, 1500),

        item("pet_cat", "Kitten", "🐱", ItemCategory.PETS, ItemRarity.RARE, 450),
        item("pet_dog", "Puppy", "🐶", ItemCategory.PETS, ItemRarity.RARE, 450),
        item("pet_bunny", "Bunny", "🐰", ItemCategory.PETS, ItemRarity.EPIC, 800),
        item("pet_frog", "Frog", "🐸", ItemCategory.PETS, ItemRarity.COMMON, 200),
        item("pet_owl", "Owl", "🦉", ItemCategory.PETS, ItemRarity.LEGENDARY, 1600),

        item("theme_rainy", "Rainy Day", "☔", ItemCategory.THEMES, ItemRarity.RARE, 280),
        item("theme_snowy", "Snowy Night", "❄️", ItemCategory.THEMES, ItemRarity.EPIC, 700),
        item("theme_starry", "Starry Sky", "✨", ItemCategory.THEMES, ItemRarity.LEGENDARY, 1400)
    )

    override suspend fun getCatalog(): List<ShopItem> = catalog

    private fun item(
        id: String,
        name: String,
        emoji: String,
        category: ItemCategory,
        rarity: ItemRarity,
        priceCoins: Int
    ): ShopItem = ShopItem(
        id = id,
        name = name,
        emoji = emoji,
        category = category,
        rarity = rarity,
        priceCoins = priceCoins
    )
}
