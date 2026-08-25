package com.example.cozypuzzle.presentation.shop

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.profile.GetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    getProfileUseCase: GetProfileUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getProfileUseCase().collect { player ->
                _uiState.update { state ->
                    state.copy(coins = player.coins, isLoading = false)
                }
            }
        }
    }

    fun onEvent(event: ShopEvent) {
        when (event) {
            is ShopEvent.CategorySelected ->
                _uiState.update { it.copy(selectedCategory = event.category) }
            is ShopEvent.ItemClicked -> Unit
        }
    }
}
