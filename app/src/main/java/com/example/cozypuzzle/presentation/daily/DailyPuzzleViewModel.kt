package com.example.cozypuzzle.presentation.daily

import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.path.model.StrokePoint
import com.example.cozypuzzle.core.path.validation.DailyStrokeOutcome
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.core.ui.BrushPalette
import com.example.cozypuzzle.domain.model.DailyPuzzle
import com.example.cozypuzzle.domain.usecase.daily.CreateDailyPuzzleResultUseCase
import com.example.cozypuzzle.domain.usecase.daily.ExtendDailyStrokeUseCase
import com.example.cozypuzzle.domain.usecase.daily.GetTodaysDailyPuzzleUseCase
import com.example.cozypuzzle.domain.usecase.daily.StartDailyStrokeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyPuzzleViewModel @Inject constructor(
    private val getTodaysDailyPuzzleUseCase: GetTodaysDailyPuzzleUseCase,
    private val startDailyStrokeUseCase: StartDailyStrokeUseCase,
    private val extendDailyStrokeUseCase: ExtendDailyStrokeUseCase,
    private val createDailyPuzzleResultUseCase: CreateDailyPuzzleResultUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(DailyPuzzleUiState())
    val uiState: StateFlow<DailyPuzzleUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<DailyPuzzleEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<DailyPuzzleEffect> = _effects.asSharedFlow()

    private var puzzle: DailyPuzzle? = null
    private var timerJob: Job? = null
    private var failureResetJob: Job? = null
    private var lastTickNanos = 0L

    init {
        loadTodaysPuzzle()
    }

    fun onEvent(event: DailyPuzzleEvent) {
        when (event) {
            is DailyPuzzleEvent.StrokeStarted -> handleStrokeStarted(event.x, event.y)
            is DailyPuzzleEvent.StrokeExtended -> handleStrokeExtended(event.points)
            DailyPuzzleEvent.StrokeEnded -> handleStrokeEnded()
            is DailyPuzzleEvent.ColorSelected -> selectColor(event.brushId)
            DailyPuzzleEvent.ClearStroke -> clearStroke()
            DailyPuzzleEvent.Restart -> restart()
            DailyPuzzleEvent.Back -> emitEffect(DailyPuzzleEffect.NavigateBack)
        }
    }

    // ---------------------------------------------------------------------
    // Loading
    // ---------------------------------------------------------------------

    private fun loadTodaysPuzzle() {
        timerJob?.cancel()
        _uiState.value = DailyPuzzleUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val daily = getTodaysDailyPuzzleUseCase()
                puzzle = daily
                val brushId = daily.recommendedBrushId
                    ?.let { BrushPalette.byIdOrNull(it)?.id }
                    ?: BrushPalette.DEFAULT_BRUSH_ID
                _uiState.value = DailyPuzzleUiState(
                    isLoading = false,
                    puzzleId = daily.id,
                    puzzleTitle = daily.title,
                    boardSize = daily.boardSize,
                    startCell = daily.startCell,
                    goalCell = daily.goalCell,
                    obstacleCells = daily.obstacleCells,
                    difficultyLabelRes = DailyPuzzleUiState.difficultyLabelRes(daily.difficulty),
                    selectedBrushId = brushId
                )
                startTimer()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = DailyPuzzleUiState(isLoading = false, isError = true)
            }
        }
    }

    // ---------------------------------------------------------------------
    // Drawing input
    // ---------------------------------------------------------------------

    private fun handleStrokeStarted(x: Float, y: Float) {
        val state = _uiState.value
        val daily = puzzle ?: return
        if (!state.acceptsInput || state.isDrawing) return

        when (startDailyStrokeUseCase(daily, StrokePoint(x, y))) {
            is DailyStrokeOutcome.Accepted -> _uiState.update {
                it.copy(isDrawing = true, pathPoints = listOf(StrokePoint(x, y)))
            }
            else -> Unit // Silent: the player simply did not begin at the sprout.
        }
    }

    private fun handleStrokeExtended(points: List<StrokePoint>) {
        val state = _uiState.value
        val daily = puzzle ?: return
        if (!state.acceptsInput || !state.isDrawing || points.isEmpty()) return

        when (val outcome = extendDailyStrokeUseCase(daily, state.pathPoints, points)) {
            is DailyStrokeOutcome.Accepted -> {
                _uiState.update { it.copy(pathPoints = it.pathPoints + points) }
                if (outcome.reachedGoal) completePuzzle()
            }
            is DailyStrokeOutcome.Failed -> failAttempt(outcome)
        }
    }

    private fun handleStrokeEnded() {
        if (_uiState.value.isDrawing) {
            _uiState.update { it.copy(isDrawing = false) }
        }
    }

    // ---------------------------------------------------------------------
    // Outcome handling
    // ---------------------------------------------------------------------

    private fun failAttempt(failure: DailyStrokeOutcome.Failed) {
        failureResetJob?.cancel()
        _uiState.update {
            it.copy(
                isDrawing = false,
                isFailing = true,
                attempts = it.attempts + 1
            )
        }
        emitEffect(DailyPuzzleEffect.ShowFailure)

        // Let the flash/shake breathe before wiping the paint away.
        failureResetJob = viewModelScope.launch {
            delay(FAILURE_RESET_DELAY_MS)
            _uiState.update { it.copy(pathPoints = emptyList(), isFailing = false) }
        }
    }

    private fun completePuzzle() {
        val state = _uiState.value
        val daily = puzzle ?: return
        timerJob?.cancel()

        val stats = CreateDailyPuzzleResultUseCase.Stats(
            puzzle = daily,
            durationMs = state.elapsedTimeMs,
            attempts = state.attempts,
            brushColorId = state.selectedBrushId
        )
        val result = createDailyPuzzleResultUseCase(stats)

        _uiState.update {
            it.copy(isDrawing = false, isCompleted = true, result = result)
        }
    }

    // ---------------------------------------------------------------------
    // Player actions
    // ---------------------------------------------------------------------

    private fun selectColor(brushId: String) {
        if (_uiState.value.acceptsInput) {
            _uiState.update { it.copy(selectedBrushId = brushId) }
        }
    }

    /** Clears the current stroke; attempts and the clock keep running. */
    private fun clearStroke() {
        if (!_uiState.value.acceptsInput) return
        failureResetJob?.cancel()
        _uiState.update { it.copy(pathPoints = emptyList(), isDrawing = false) }
    }

    /** Full reset of today's puzzle: stroke, attempts and timer. */
    private fun restart() {
        if (_uiState.value.isLoading) return
        failureResetJob?.cancel()
        _uiState.update {
            it.copy(
                pathPoints = emptyList(),
                isDrawing = false,
                isFailing = false,
                attempts = 0,
                elapsedTimeMs = 0L,
                isCompleted = false,
                result = null
            )
        }
        startTimer()
    }

    // ---------------------------------------------------------------------
    // Timer
    // ---------------------------------------------------------------------

    private fun startTimer() {
        timerJob?.cancel()
        lastTickNanos = System.nanoTime()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_INTERVAL_MS)
                val now = System.nanoTime()
                val deltaMs = (now - lastTickNanos) / NANOS_PER_MS
                lastTickNanos = now
                if (deltaMs > 0) {
                    _uiState.update { it.copy(elapsedTimeMs = it.elapsedTimeMs + deltaMs) }
                }
            }
        }
    }

    private fun emitEffect(effect: DailyPuzzleEffect) {
        _effects.tryEmit(effect)
    }

    override fun onCleared() {
        timerJob?.cancel()
        failureResetJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TICK_INTERVAL_MS = 250L
        const val NANOS_PER_MS = 1_000_000L
        const val FAILURE_RESET_DELAY_MS = 420L
        const val EFFECT_BUFFER = 8
    }
}
