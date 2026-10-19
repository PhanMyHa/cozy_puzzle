package com.example.cozypuzzle.presentation.shop

import com.example.cozypuzzle.domain.model.ItemCategory
import com.example.cozypuzzle.domain.model.ShopItem

data class ShopUiState(
    val coins: Int = 0,
    val catalog: List<ShopItem> = emptyList(),
    /** null shows every category. */
    val selectedCategory: ItemCategory? = null,
    val isLoading: Boolean = true
) {
    val visibleItems: List<ShopItem>
        get() = when (val category = selectedCategory) {
            null -> catalog
            else -> catalog.filter { it.category == category }
        }
}
