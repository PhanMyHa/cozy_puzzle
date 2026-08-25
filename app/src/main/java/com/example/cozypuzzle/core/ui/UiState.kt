package com.example.cozypuzzle.core.ui

interface UiState {

    data object Loading : UiState

    data object Empty : UiState

    data class Error(val message: String) : UiState
}
