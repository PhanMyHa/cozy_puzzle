package com.example.cozypuzzle.presentation.shop

import com.example.cozypuzzle.domain.model.ItemCategory

sealed interface ShopEvent {

    /** Pass null to show the whole catalog. */
    data class CategorySelected(val category: ItemCategory?) : ShopEvent

    data class ItemClicked(val itemId: String) : ShopEvent
}

sealed interface ShopEffect {

    data object PurchaseSuccess : ShopEffect

    data object InsufficientCoins : ShopEffect

    data object AlreadyOwned : ShopEffect
}
