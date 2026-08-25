package com.example.cozypuzzle.presentation.shop

import com.example.cozypuzzle.domain.model.ItemCategory

data class ShopUiState(
    val coins: Int = 0,
    val selectedCategory: ItemCategory? = null,
    val isLoading: Boolean = true
)
