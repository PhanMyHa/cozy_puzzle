package com.example.cozypuzzle.domain.model

data class ShopItem(
    val id: String,
    val name: String,
    /** Emoji shown as the item artwork in the cozy shop grid. */
    val emoji: String,
    val category: ItemCategory,
    val rarity: ItemRarity,
    val priceCoins: Int,
    val isOwned: Boolean = false
)
