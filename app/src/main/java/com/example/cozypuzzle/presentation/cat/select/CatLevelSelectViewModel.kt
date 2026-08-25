package com.example.cozypuzzle.presentation.cat.select

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.cat.GetCatLevelsUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.GetBestPuzzleResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatLevelSelectViewModel @Inject constructor(
    private val getCatLevelsUseCase: GetCatLevelsUseCase,
    private val getBestPuzzleResultUseCase: GetBestPuzzleResultUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CatLevelSelectUiState())
    val uiState: StateFlow<CatLevelSelectUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CatLevelSelectEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<CatLevelSelectEffect> = _effects.asSharedFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun onEvent(event: CatLevelSelectEvent) {
        when (event) {
            is CatLevelSelectEvent.StartLevel -> _effects.tryEmit(
                CatLevelSelectEffect.NavigateToGameplay(event.levelId)
            )
        }
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadLevels() }
    }

    private suspend fun loadLevels() {
        try {
            val levels = getCatLevelsUseCase().map { level ->
                CatLevelListItemUiModel(
                    id = level.id,
                    title = level.title,
                    difficultyLabelRes = CatLevelSelectUiState.difficultyLabelRes(level.difficulty),
                    gridSize = level.rows,
                    bestTimeMs = getBestPuzzleResultUseCase(level.id)?.durationMs
                )
            }
            _uiState.value = CatLevelSelectUiState(isLoading = false, levels = levels)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.value = CatLevelSelectUiState(isLoading = false, levels = emptyList())
        }
    }

    private companion object {
        const val EFFECT_BUFFER = 8
    }
}
