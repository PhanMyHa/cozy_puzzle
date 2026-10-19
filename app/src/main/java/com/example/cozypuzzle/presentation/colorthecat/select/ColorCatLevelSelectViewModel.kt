package com.example.cozypuzzle.presentation.colorthecat.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.colorthecat.model.ColorCatDifficulty
import com.example.cozypuzzle.data.datasource.BundledColorCatDataSource
import com.example.cozypuzzle.domain.repository.ColorCatProgressRepository
import com.example.cozypuzzle.domain.repository.PlayerRepository
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
class ColorCatLevelSelectViewModel @Inject constructor(
    private val dataSource: BundledColorCatDataSource,
    private val progressRepository: ColorCatProgressRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColorCatLevelSelectUiState(isLoading = true))
    val uiState: StateFlow<ColorCatLevelSelectUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ColorCatLevelSelectEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<ColorCatLevelSelectEffect> = _effects.asSharedFlow()

    private val selectedDifficulty = MutableStateFlow(ColorCatDifficulty.BASIC)

    init {
        viewModelScope.launch {
            combine(
                selectedDifficulty,
                progressRepository.observeUnlockedLevels(),
                progressRepository.observeLevelStars(),
                playerRepository.observePlayer()
            ) { difficulty, unlockedSet, starsMap, player ->
                val levels = dataSource.getLevels(difficulty)
                val items = levels.map { level ->
                    val isUnlocked = unlockedSet.contains(level.id)
                    val stars = starsMap[level.id] ?: 0
                    ColorCatLevelUiItem(
                        level = level,
                        isUnlocked = isUnlocked,
                        stars = stars
                    )
                }
                ColorCatLevelSelectUiState(
                    selectedDifficulty = difficulty,
                    levels = items,
                    totalStars = player.coins,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectDifficulty(difficulty: ColorCatDifficulty) {
        selectedDifficulty.value = difficulty
    }

    fun onLevelClicked(item: ColorCatLevelUiItem) {
        if (item.isUnlocked) {
            _effects.tryEmit(ColorCatLevelSelectEffect.NavigateToGame(item.level.id))
        } else {
            _effects.tryEmit(ColorCatLevelSelectEffect.ShowLockedDialog(item.level.number))
        }
    }

    fun onBackClicked() {
        _effects.tryEmit(ColorCatLevelSelectEffect.NavigateBack)
    }
}
