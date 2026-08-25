package com.example.cozypuzzle.presentation.profile

data class ProfileUiState(
    val playerName: String = "",
    val level: Int = 0,
    val coins: Int = 0,
    val isLoading: Boolean = false
)
