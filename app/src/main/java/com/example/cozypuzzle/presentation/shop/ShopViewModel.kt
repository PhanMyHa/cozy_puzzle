package com.example.cozypuzzle.presentation.shop

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.profile.GetProfileUseCase
import com.example.cozypuzzle.domain.usecase.shop.GetShopItemsUseCase
import com.example.cozypuzzle.domain.usecase.shop.PurchaseResult
import com.example.cozypuzzle.domain.usecase.shop.PurchaseItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getShopItemsUseCase: GetShopItemsUseCase,
    private val purchaseItemUseCase: PurchaseItemUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ShopEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<ShopEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            launch {
                getProfileUseCase().collect { player ->
                    _uiState.update { it.copy(coins = player.coins) }
                }
            }
            launch {
                getShopItemsUseCase().collect { items ->
                    _uiState.update { it.copy(catalog = items, isLoading = false) }
                }
            }
        }
    }

    fun onEvent(event: ShopEvent) {
        when (event) {
            is ShopEvent.CategorySelected ->
                _uiState.update { it.copy(selectedCategory = event.category) }
            is ShopEvent.ItemClicked -> purchase(event.itemId)
        }
    }

    private fun purchase(itemId: String) {
        viewModelScope.launch {
            val effect = when (purchaseItemUseCase(itemId)) {
                is PurchaseResult.Success -> ShopEffect.PurchaseSuccess
                PurchaseResult.InsufficientCoins -> ShopEffect.InsufficientCoins
                PurchaseResult.AlreadyOwned -> ShopEffect.AlreadyOwned
                PurchaseResult.ItemNotFound -> null
            }
            if (effect != null) _effects.tryEmit(effect)
        }
    }

    private companion object {
        const val EFFECT_BUFFER = 8
    }
}
