package com.example.cozypuzzle.presentation.catpuzzle.select

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleDifficulty
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.data.datasource.BundledCatPuzzleDataSource
import com.example.cozypuzzle.domain.repository.CatPuzzleProgressRepository
import com.example.cozypuzzle.domain.usecase.profile.GetProfileUseCase
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
class CatPuzzleLevelSelectViewModel @Inject constructor(
    private val dataSource: BundledCatPuzzleDataSource,
    private val progressRepository: CatPuzzleProgressRepository,
    getProfileUseCase: GetProfileUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CatPuzzleLevelSelectUiState())
    val uiState: StateFlow<CatPuzzleLevelSelectUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CatPuzzleSelectEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<CatPuzzleSelectEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                getProfileUseCase(),
                progressRepository.observeUnlockedLevels(),
                progressRepository.observeLevelStars()
            ) { player, unlockedLevels, starsMap ->
                val allLevels = dataSource.getLevels(CatPuzzleDifficulty.BASIC)
                val items = allLevels.map { level ->
                    val isUnlocked = unlockedLevels.contains(level.id)
                    val stars = starsMap[level.id] ?: 0
                    CatPuzzleLevelItemUiModel(
                        id = level.id,
                        number = level.number,
                        imageRes = level.imageRes,
                        stars = stars,
                        isUnlocked = isUnlocked
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        playerStars = player.coins,
                        levels = items,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun onLevelClicked(levelId: String) {
        _effects.tryEmit(CatPuzzleSelectEffect.NavigateToGame(levelId))
    }

    fun onLockedLevelClicked(levelNumber: Int = 1) {
        _effects.tryEmit(CatPuzzleSelectEffect.ShowLockedDialog(levelNumber))
    }

    fun onBackClicked() {
        _effects.tryEmit(CatPuzzleSelectEffect.NavigateBack)
    }

    private companion object {
        const val EFFECT_BUFFER = 8
    }
}
