package com.example.cozypuzzle.presentation.findpair.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.findpair.model.FindPairDifficulty
import com.example.cozypuzzle.data.datasource.BundledFindPairDataSource
import com.example.cozypuzzle.domain.repository.FindPairProgressRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindPairLevelSelectViewModel @Inject constructor(
    private val dataSource: BundledFindPairDataSource,
    private val progressRepository: FindPairProgressRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindPairLevelSelectUiState(isLoading = true))
    val uiState: StateFlow<FindPairLevelSelectUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<FindPairLevelSelectEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<FindPairLevelSelectEffect> = _effects.asSharedFlow()

    private val selectedDifficulty = MutableStateFlow(FindPairDifficulty.EASY)

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
                    FindPairLevelUiItem(
                        level = level,
                        isUnlocked = isUnlocked,
                        stars = stars
                    )
                }
                FindPairLevelSelectUiState(
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

    fun selectDifficulty(difficulty: FindPairDifficulty) {
        selectedDifficulty.value = difficulty
    }

    fun onLevelClicked(item: FindPairLevelUiItem) {
        if (item.isUnlocked) {
            _effects.tryEmit(FindPairLevelSelectEffect.NavigateToGame(item.level.id))
        } else {
            _effects.tryEmit(FindPairLevelSelectEffect.ShowLockedDialog(item.level.number))
        }
    }

    fun onBackClicked() {
        _effects.tryEmit(FindPairLevelSelectEffect.NavigateBack)
    }
}
