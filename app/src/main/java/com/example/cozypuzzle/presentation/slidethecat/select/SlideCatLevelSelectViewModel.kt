package com.example.cozypuzzle.presentation.slidethecat.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import com.example.cozypuzzle.data.datasource.BundledSlideCatDataSource
import com.example.cozypuzzle.domain.repository.PlayerRepository
import com.example.cozypuzzle.domain.repository.SlideCatProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SlideCatLevelSelectViewModel @Inject constructor(
    private val dataSource: BundledSlideCatDataSource,
    private val progressRepository: SlideCatProgressRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SlideCatLevelSelectUiState(isLoading = true))
    val uiState: StateFlow<SlideCatLevelSelectUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SlideCatLevelSelectEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<SlideCatLevelSelectEffect> = _effects.asSharedFlow()

    private val selectedDifficulty = MutableStateFlow(SlideCatDifficulty.EASY)

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
                    SlideCatLevelUiItem(
                        level = level,
                        isUnlocked = isUnlocked,
                        stars = stars
                    )
                }
                SlideCatLevelSelectUiState(
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

    fun selectDifficulty(difficulty: SlideCatDifficulty) {
        selectedDifficulty.value = difficulty
    }

    fun onLevelClicked(item: SlideCatLevelUiItem) {
        if (item.isUnlocked) {
            _effects.tryEmit(SlideCatLevelSelectEffect.NavigateToGame(item.level.id))
        } else {
            _effects.tryEmit(SlideCatLevelSelectEffect.ShowLockedDialog(item.level.number))
        }
    }

    fun onBackClicked() {
        _effects.tryEmit(SlideCatLevelSelectEffect.NavigateBack)
    }
}
