package com.example.cozypuzzle.presentation.garden

import com.example.cozypuzzle.domain.model.GardenItem

data class GardenUiState(
    val items: List<GardenItem> = emptyList(),
    val selectedItemId: String? = null,
    val isEditMode: Boolean = false,
    val petCount: Int = 0,
    val isLoading: Boolean = true
)
