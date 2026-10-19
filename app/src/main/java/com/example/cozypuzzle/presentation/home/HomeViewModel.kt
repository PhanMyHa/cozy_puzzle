package com.example.cozypuzzle.presentation.home

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.profile.GetProfileUseCase
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
class HomeViewModel @Inject constructor(
    getProfileUseCase: GetProfileUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<HomeEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            getProfileUseCase().collect { player ->
                _uiState.update { state ->
                    state.copy(
                        playerName = player.name,
                        avatarUri = player.avatarUri,
                        playerLevel = player.level,
                        playerCoins = player.coins,
                        levelProgress = ((player.coins % 100).coerceIn(20, 85)),
                        dailyPuzzleSize = 4,
                        gameModes = HomeUiState.defaultGameModes(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.DailyPuzzleClicked ->
                _effects.tryEmit(HomeEffect.NavigateToDailyPuzzle)
            is HomeEvent.GameModeClicked ->
                _effects.tryEmit(
                    when (event.mode) {
                        PuzzleMode.CAT_PUZZLE -> HomeEffect.NavigateToCatPuzzle
                        PuzzleMode.COLOR_THE_CAT -> HomeEffect.NavigateToColorCatSelect
                        PuzzleMode.SLIDE_THE_CAT -> HomeEffect.NavigateToSlideCatSelect
                        PuzzleMode.FIND_THE_PAIR -> HomeEffect.NavigateToFindPairSelect
                        PuzzleMode.DAILY_DRAW -> HomeEffect.NavigateToDailyPuzzle
                    }
                )
        }
    }

    private companion object {
        const val EFFECT_BUFFER = 8
    }
}
