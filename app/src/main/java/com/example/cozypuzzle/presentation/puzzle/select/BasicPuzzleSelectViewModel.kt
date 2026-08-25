package com.example.cozypuzzle.presentation.puzzle.select

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.puzzle.GetBasicPuzzlesUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.GetBestPuzzleResultUseCase
import com.example.cozypuzzle.presentation.puzzle.BasicPuzzleImages
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
class BasicPuzzleSelectViewModel @Inject constructor(
    private val getBasicPuzzlesUseCase: GetBasicPuzzlesUseCase,
    private val getBestPuzzleResultUseCase: GetBestPuzzleResultUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(BasicPuzzleSelectUiState())
    val uiState: StateFlow<BasicPuzzleSelectUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<BasicPuzzleSelectEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<BasicPuzzleSelectEffect> = _effects.asSharedFlow()

    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun onEvent(event: BasicPuzzleSelectEvent) {
        when (event) {
            is BasicPuzzleSelectEvent.StartPuzzle -> _effects.tryEmit(
                BasicPuzzleSelectEffect.NavigateToGameplay(event.puzzleId, event.gridSize)
            )
        }
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { loadPuzzles() }
    }

    private suspend fun loadPuzzles() {
        try {
            val puzzles = getBasicPuzzlesUseCase().map { puzzle ->
                BasicPuzzleListItemUiModel(
                    id = puzzle.id,
                    title = puzzle.title,
                    imageRes = BasicPuzzleImages.imageResFor(puzzle.imageId),
                    difficultyLabelRes =
                        BasicPuzzleSelectUiState.difficultyLabelRes(puzzle.difficulty),
                    gridSizes = puzzle.availableGridSizes,
                    bestTimeMs = getBestPuzzleResultUseCase(puzzle.id)?.durationMs
                )
            }
            _uiState.value = BasicPuzzleSelectUiState(isLoading = false, puzzles = puzzles)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.value = BasicPuzzleSelectUiState(isLoading = false, puzzles = emptyList())
        }
    }

    private companion object {
        const val EFFECT_BUFFER = 8
    }
}
