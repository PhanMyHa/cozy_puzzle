package com.example.cozypuzzle.domain.model

data class ShopItem(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val rarity: ItemRarity,
    val priceCoins: Int,
    val isOwned: Boolean = false
)
