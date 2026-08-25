package com.example.cozypuzzle.presentation.shop

import com.example.cozypuzzle.domain.model.ItemCategory

sealed interface ShopEvent {

    data class CategorySelected(
        val category: ItemCategory
    ) : ShopEvent

    data class ItemClicked(
        val itemId: String
    ) : ShopEvent
}
