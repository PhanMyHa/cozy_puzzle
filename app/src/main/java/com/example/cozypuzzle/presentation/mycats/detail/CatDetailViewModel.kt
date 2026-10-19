package com.example.cozypuzzle.presentation.mycats.detail

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.repository.CatCollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatDetailViewModel @Inject constructor(
    private val catRepository: CatCollectionRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CatDetailUiState())
    val uiState: StateFlow<CatDetailUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CatDetailEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<CatDetailEffect> = _effects.asSharedFlow()

    private var currentCatId: String? = null

    fun loadCat(catId: String) {
        currentCatId = catId
        val cat = catRepository.getCatById(catId) ?: return

        viewModelScope.launch {
            val (curr, target) = catRepository.getUnlockProgress(cat)
            combine(
                catRepository.observeOwnedCatIds(),
                catRepository.observeFavoriteCatIds()
            ) { ownedIds, favIds ->
                val isOwned = ownedIds.contains(catId)
                val isFav = favIds.contains(catId)

                _uiState.update { state ->
                    state.copy(
                        cat = cat,
                        isOwned = isOwned,
                        isFavorite = isFav,
                        currentProgress = curr,
                        targetProgress = target
                    )
                }
            }.collect {}
        }
    }

    fun onFavoriteToggled() {
        val catId = currentCatId ?: return
        viewModelScope.launch {
            catRepository.toggleFavorite(catId)
        }
    }

    fun onModeSelected(mode: InteractionMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun onInteractClicked() {
        val mode = _uiState.value.selectedMode
        val reaction = when (mode) {
            InteractionMode.PET -> listOf("Purr... 🥰", "Meow~ 💕", "So cozy! ✨").random()
            InteractionMode.PLAY -> listOf("Yay! Catch the ball! 🧶", "Paws up! 🐾", "Zoomies! ⚡").random()
            InteractionMode.SMILE -> listOf("Happy kitty! 🌸", "*Happy blinks* 💖", "Love you! 💕").random()
            InteractionMode.SLEEP -> listOf("Zzz... 💤", "Sweet dreams... 🌙", "Nap time! ☁️").random()
        }

        _uiState.update { it.copy(speechReaction = reaction) }
        _effects.tryEmit(CatDetailEffect.PlayBounceReaction(reaction))
    }

    fun onBackClicked() {
        _effects.tryEmit(CatDetailEffect.NavigateBack)
    }
}
