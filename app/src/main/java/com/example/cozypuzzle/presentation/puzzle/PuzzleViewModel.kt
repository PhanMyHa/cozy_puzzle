package com.example.cozypuzzle.presentation.puzzle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.puzzle.history.PuzzleHistory
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.domain.usecase.puzzle.CheckPuzzleCompletedUseCase
import com.example.cozypuzzle.presentation.puzzle.PuzzleNavArgs.KEY_GRID_SIZE
import com.example.cozypuzzle.presentation.puzzle.PuzzleNavArgs.KEY_PUZZLE_ID
import com.example.cozypuzzle.domain.usecase.puzzle.CreatePuzzleResultUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.GetPuzzleHintUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.MovePieceUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.RedoPuzzleUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.RotatePieceUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.SavePuzzleResultUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.StartBasicPuzzleUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.SwapPiecesUseCase
import com.example.cozypuzzle.domain.usecase.puzzle.UndoPuzzleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class PuzzleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startBasicPuzzleUseCase: StartBasicPuzzleUseCase,
    private val rotatePieceUseCase: RotatePieceUseCase,
    private val swapPiecesUseCase: SwapPiecesUseCase,
    private val movePieceUseCase: MovePieceUseCase,
    private val checkPuzzleCompletedUseCase: CheckPuzzleCompletedUseCase,
    private val undoPuzzleUseCase: UndoPuzzleUseCase,
    private val redoPuzzleUseCase: RedoPuzzleUseCase,
    private val getPuzzleHintUseCase: GetPuzzleHintUseCase,
    private val createPuzzleResultUseCase: CreatePuzzleResultUseCase,
    private val savePuzzleResultUseCase: SavePuzzleResultUseCase
) : BaseViewModel() {

    private val puzzleIdArg: String = savedStateHandle[KEY_PUZZLE_ID] ?: DEFAULT_PUZZLE_ID
    private val gridSizeArg: Int = savedStateHandle[KEY_GRID_SIZE] ?: DEFAULT_GRID_SIZE
    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<PuzzleEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<PuzzleEffect> = _effects.asSharedFlow()

    private var history: PuzzleHistory? = null
    private var timerJob: Job? = null
    private var lastTickNanos: Long = 0L

    init {
        startGame()
    }

    fun onEvent(event: PuzzleEvent) {
        when (event) {
            is PuzzleEvent.PieceClicked -> handlePieceClicked(event.pieceId)
            is PuzzleEvent.RotatePiece -> handleRotate(event.pieceId)
            is PuzzleEvent.SwapPieces -> handleSwap(event.firstId, event.secondId)
            is PuzzleEvent.MovePiece -> handleMove(event.pieceId, event.position)
            PuzzleEvent.Undo -> handleUndo()
            PuzzleEvent.Redo -> handleRedo()
            PuzzleEvent.Hint -> handleHint()
            PuzzleEvent.Pause -> setPaused(true)
            PuzzleEvent.Resume -> setPaused(false)
            PuzzleEvent.Restart -> startGame()
            PuzzleEvent.Back -> emitEffect(PuzzleEffect.NavigateBackToSelection)
        }
    }

    private fun startGame() {
        timerJob?.cancel()
        _uiState.value = PuzzleUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val game = startBasicPuzzleUseCase(
                    puzzleId = puzzleIdArg,
                    requestedSize = gridSizeArg,
                    seed = generateSeed()
                )
                history = PuzzleHistory(game.puzzle)
                _uiState.value = PuzzleUiState(
                    isLoading = false,
                    puzzleTitle = game.puzzleTitle,
                    imageId = game.imageId,
                    puzzle = game.puzzle,
                    gridSize = game.gridSize,
                    hintsRemaining = game.maxHints,
                    maxHints = game.maxHints,
                    allowRotate = game.allowRotate,
                    allowSwap = game.allowSwap,
                    allowDrag = game.allowDrag
                )
                startTimer()
            } catch (e: Exception) {
                _uiState.value = PuzzleUiState(isLoading = false, isError = true)
            }
        }
    }

    private fun handlePieceClicked(pieceId: Int) {
        if (!acceptsInput()) return
        when (val selected = _uiState.value.selectedPieceId) {
            null -> _uiState.update { it.copy(selectedPieceId = pieceId) }
            pieceId -> _uiState.update { it.copy(selectedPieceId = null) }
            else -> {
                performSwap(selected, pieceId)
                _uiState.update { it.copy(selectedPieceId = null) }
            }
        }
    }

    private fun handleRotate(pieceId: Int) {
        val state = _uiState.value
        if (!acceptsInput() || !state.allowRotate) return
        val puzzle = state.puzzle ?: return
        commitAction(rotatePieceUseCase(puzzle, pieceId))
    }

    private fun handleSwap(firstId: Int, secondId: Int) {
        if (!acceptsInput() || !_uiState.value.allowSwap) return
        performSwap(firstId, secondId)
    }

    private fun performSwap(firstId: Int, secondId: Int) {
        val puzzle = _uiState.value.puzzle ?: return
        commitAction(swapPiecesUseCase(puzzle, firstId, secondId))
    }

    private fun handleMove(pieceId: Int, position: Int) {
        val state = _uiState.value
        if (!acceptsInput() || !state.allowDrag) return
        val puzzle = state.puzzle ?: return
        if (position == puzzle.pieces.firstOrNull { it.id == pieceId }?.currentPosition) return
        commitAction(movePieceUseCase(puzzle, pieceId, position))
    }

    private fun handleUndo() {
        val currentHistory = history ?: return
        if (!acceptsInput()) return
        val restored = undoPuzzleUseCase(currentHistory) ?: return
        _uiState.update { state ->
            state.copy(
                puzzle = restored,
                selectedPieceId = null,
                hintPieceId = null,
                moves = (state.moves - 1).coerceAtLeast(0),
                canUndo = currentHistory.canUndo(),
                canRedo = currentHistory.canRedo()
            )
        }
    }

    private fun handleRedo() {
        val currentHistory = history ?: return
        if (!acceptsInput()) return
        val restored = redoPuzzleUseCase(currentHistory) ?: return
        _uiState.update { state ->
            state.copy(
                puzzle = restored,
                selectedPieceId = null,
                hintPieceId = null,
                moves = state.moves + 1,
                canUndo = currentHistory.canUndo(),
                canRedo = currentHistory.canRedo()
            )
        }
    }

    private fun handleHint() {
        if (!acceptsInput()) return
        val state = _uiState.value
        val puzzle = state.puzzle ?: return
        val hint = getPuzzleHintUseCase(puzzle, state.hintsRemaining) ?: return
        _uiState.update { it.copy(hintsRemaining = it.hintsRemaining - 1, hintPieceId = hint.pieceId) }
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

    private fun commitAction(updatedPuzzle: com.example.cozypuzzle.core.puzzle.model.Puzzle) {
        val currentHistory = history ?: return
        currentHistory.commit(updatedPuzzle)
        _uiState.update { state ->
            state.copy(
                puzzle = updatedPuzzle,
                moves = state.moves + 1,
                hintPieceId = null,
                canUndo = currentHistory.canUndo(),
                canRedo = currentHistory.canRedo()
            )
        }
        checkCompletion(updatedPuzzle)
    }

    private fun checkCompletion(puzzle: com.example.cozypuzzle.core.puzzle.model.Puzzle) {
        if (!checkPuzzleCompletedUseCase(puzzle)) return
        timerJob?.cancel()
        val state = _uiState.value
        val result = createPuzzleResultUseCase(
            CreatePuzzleResultUseCase.Stats(
                puzzleId = puzzleIdArg,
                mode = puzzle.config.mode,
                difficulty = puzzle.config.difficulty,
                gridSize = puzzle.config.rows,
                durationMs = state.elapsedTimeMs,
                moveCount = state.moves,
                hintsUsed = state.maxHints - state.hintsRemaining
            )
        )
        _uiState.update { current ->
            current.copy(
                isCompleted = true,
                selectedPieceId = null,
                hintPieceId = null,
                result = result
            )
        }
        viewModelScope.launch { savePuzzleResultUseCase(result) }
    }

    private fun acceptsInput(): Boolean =
        !_uiState.value.isLoading && !_uiState.value.isPaused && !_uiState.value.isCompleted

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

    private fun emitEffect(effect: PuzzleEffect) {
        _effects.tryEmit(effect)
    }

    private fun generateSeed(): Long = kotlin.random.Random.Default.nextLong()

    private companion object {
        const val DEFAULT_PUZZLE_ID = "cute_cat"
        const val DEFAULT_GRID_SIZE = 4
        const val TICK_INTERVAL_MS = 250L
        const val NANOS_PER_MS = 1_000_000L
        const val EFFECT_BUFFER = 8
    }
}
