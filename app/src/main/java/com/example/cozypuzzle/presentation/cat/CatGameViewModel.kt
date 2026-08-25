package com.example.cozypuzzle.presentation.cat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.path.history.CatBoardHistory
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.cat.GetCatHintUseCase
import com.example.cozypuzzle.domain.usecase.cat.RotateCatTileUseCase
import com.example.cozypuzzle.domain.usecase.cat.StartCatLevelUseCase
import com.example.cozypuzzle.domain.usecase.cat.SwapCatTilesUseCase
import com.example.cozypuzzle.domain.usecase.cat.ValidateCatPathUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.CreatePuzzleResultUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.SavePuzzleResultUseCase
import com.example.cozypuzzle.presentation.puzzle.PuzzleNavArgs
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
class CatGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startCatLevelUseCase: StartCatLevelUseCase,
    private val rotateCatTileUseCase: RotateCatTileUseCase,
    private val swapCatTilesUseCase: SwapCatTilesUseCase,
    private val validateCatPathUseCase: ValidateCatPathUseCase,
    private val getCatHintUseCase: GetCatHintUseCase,
    private val createPuzzleResultUseCase: CreatePuzzleResultUseCase,
    private val savePuzzleResultUseCase: SavePuzzleResultUseCase
) : BaseViewModel() {

    private val levelIdArg: String = savedStateHandle[PuzzleNavArgs.KEY_LEVEL_ID] ?: DEFAULT_LEVEL_ID

    private val _uiState = MutableStateFlow(CatGameUiState())
    val uiState: StateFlow<CatGameUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CatGameEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<CatGameEffect> = _effects.asSharedFlow()

    private var history: CatBoardHistory? = null
    private var timerJob: Job? = null
    private var walkJob: Job? = null
    private var lastTickNanos = 0L
    private var currentDifficulty = Difficulty.EASY

    init {
        startLevel()
    }

    fun onEvent(event: CatGameEvent) {
        when (event) {
            is CatGameEvent.CellTapped -> handleCellTapped(event.cell)
            is CatGameEvent.RotateSelected -> handleRotate(event.cell)
            CatGameEvent.Undo -> handleUndo()
            CatGameEvent.Redo -> handleRedo()
            CatGameEvent.Hint -> handleHint()
            CatGameEvent.Walk -> startWalk()
            CatGameEvent.Pause -> setPaused(true)
            CatGameEvent.Resume -> setPaused(false)
            CatGameEvent.Restart -> startLevel()
            CatGameEvent.Back -> emitEffect(CatGameEffect.NavigateBackToSelection)
        }
    }

    private fun startLevel() {
        walkJob?.cancel()
        timerJob?.cancel()
        _uiState.value = CatGameUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val game = startCatLevelUseCase(levelIdArg, seed = generateSeed())
                history = CatBoardHistory(game.board)
                currentDifficulty = game.difficulty
                _uiState.value = CatGameUiState(
                    isLoading = false,
                    levelTitle = game.levelTitle,
                    board = game.board,
                    hintsRemaining = game.maxHints,
                    maxHints = game.maxHints
                )
                refreshPath()
                startTimer()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = CatGameUiState(isLoading = false, isError = true)
            }
        }
    }

    private fun handleCellTapped(cell: Int) {
        if (!acceptsInput()) return
        val board = _uiState.value.board ?: return
        if (cell == board.catPosition || cell == board.homePosition) return
        when (val selected = _uiState.value.selectedCell) {
            null -> _uiState.update { it.copy(selectedCell = cell, hintCells = emptyList()) }
            cell -> _uiState.update { it.copy(selectedCell = null) }
            else -> {
                commitAction(swapCatTilesUseCase(board, selected, cell))
                _uiState.update { it.copy(selectedCell = null) }
            }
        }
    }

    private fun handleRotate(cell: Int) {
        if (!acceptsInput()) return
        commitAction(rotateCatTileUseCase(_uiState.value.board ?: return, cell))
    }

    private fun handleUndo() {
        val currentHistory = history ?: return
        if (!acceptsInput()) return
        val restored = currentHistory.undo() ?: return
        _uiState.update { state ->
            state.copy(
                board = restored,
                selectedCell = null,
                hintCells = emptyList(),
                moves = (state.moves - 1).coerceAtLeast(0),
                canUndo = currentHistory.canUndo(),
                canRedo = currentHistory.canRedo()
            )
        }
        refreshPath()
    }

    private fun handleRedo() {
        val currentHistory = history ?: return
        if (!acceptsInput()) return
        val restored = currentHistory.redo() ?: return
        _uiState.update { state ->
            state.copy(
                board = restored,
                selectedCell = null,
                hintCells = emptyList(),
                moves = state.moves + 1,
                canUndo = currentHistory.canUndo(),
                canRedo = currentHistory.canRedo()
            )
        }
        refreshPath()
    }

    private fun handleHint() {
        if (!acceptsInput()) return
        val state = _uiState.value
        val hint = getCatHintUseCase(state.board ?: return, state.hintsRemaining) ?: return
        _uiState.update {
            it.copy(hintsRemaining = it.hintsRemaining - 1, hintCells = hint.cells)
        }
    }

    /** Walk requires a complete path; the cat then steps home cell by cell. */
    private fun startWalk() {
        if (!acceptsInput()) return
        val path = _uiState.value.pathCells
        if (!_uiState.value.pathFound || path.isEmpty()) return

        timerJob?.cancel()
        _uiState.update { it.copy(isWalking = true, selectedCell = null, hintCells = emptyList()) }

        walkJob = viewModelScope.launch {
            for (index in path.indices) {
                delay(STEP_DURATION_MS)
                if (!isActive) break
                _uiState.update { it.copy(walkIndex = index) }
            }
            completeLevel()
        }
    }

    private fun completeLevel() {
        val state = _uiState.value
        val board = state.board ?: return
        val result = createPuzzleResultUseCase(
            CreatePuzzleResultUseCase.Stats(
                puzzleId = levelIdArg,
                mode = PuzzleMode.FIND_MY_CAT,
                difficulty = currentDifficulty,
                gridSize = board.rows,
                durationMs = state.elapsedTimeMs,
                moveCount = state.moves,
                hintsUsed = state.maxHints - state.hintsRemaining
            )
        )
        _uiState.update { current ->
            current.copy(isCompleted = true, isWalking = false, result = result)
        }
        viewModelScope.launch { savePuzzleResultUseCase(result) }
    }

    private fun setPaused(paused: Boolean) {
        if (_uiState.value.isCompleted || _uiState.value.isLoading || _uiState.value.isError) return
        if (paused) {
            timerJob?.cancel()
        } else if (_uiState.value.isPaused) {
            startTimer()
        }
        _uiState.update { it.copy(isPaused = paused) }
    }

    private fun commitAction(updatedBoard: com.example.cozypuzzle.core.path.model.CatBoard) {
        val currentHistory = history ?: return
        currentHistory.commit(updatedBoard)
        _uiState.update { state ->
            state.copy(
                board = updatedBoard,
                moves = state.moves + 1,
                hintCells = emptyList(),
                canUndo = currentHistory.canUndo(),
                canRedo = currentHistory.canRedo()
            )
        }
        refreshPath()
    }

    private fun refreshPath() {
        val board = _uiState.value.board ?: return
        val pathResult = validateCatPathUseCase(board)
        _uiState.update {
            it.copy(pathFound = pathResult.found, pathCells = pathResult.cells)
        }
    }

    private fun acceptsInput(): Boolean =
        !_uiState.value.isLoading && !_uiState.value.isPaused &&
            !_uiState.value.isCompleted && !_uiState.value.isWalking

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

    private fun emitEffect(effect: CatGameEffect) {
        _effects.tryEmit(effect)
    }

    private fun generateSeed(): Long = kotlin.random.Random.Default.nextLong()

    private companion object {
        const val DEFAULT_LEVEL_ID = "first_steps"
        const val TICK_INTERVAL_MS = 250L
        const val NANOS_PER_MS = 1_000_000L
        const val STEP_DURATION_MS = 320L
        const val EFFECT_BUFFER = 8
    }
}
