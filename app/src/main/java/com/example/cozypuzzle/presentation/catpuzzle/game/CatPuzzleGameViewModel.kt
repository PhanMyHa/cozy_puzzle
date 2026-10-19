package com.example.cozypuzzle.presentation.catpuzzle.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.core.catpuzzle.engine.CatPuzzleEngine
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleLevel
import com.example.cozypuzzle.core.catpuzzle.model.PlacementResult
import com.example.cozypuzzle.core.ui.BaseViewModel
import com.example.cozypuzzle.data.datasource.BundledCatPuzzleDataSource
import com.example.cozypuzzle.domain.repository.CatPuzzleProgressRepository
import com.example.cozypuzzle.presentation.catpuzzle.select.CatPuzzleLevelSelectFragment
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
import com.example.cozypuzzle.core.event.GameEvent
import com.example.cozypuzzle.core.event.GameEventBus
import javax.inject.Inject

@HiltViewModel
class CatPuzzleGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataSource: BundledCatPuzzleDataSource,
    private val progressRepository: CatPuzzleProgressRepository,
    private val gameEventBus: GameEventBus
) : BaseViewModel() {

    private val levelIdArg: String =
        savedStateHandle[CatPuzzleLevelSelectFragment.KEY_LEVEL_ID] ?: BundledCatPuzzleDataSource.FIRST_LEVEL_ID

    private val engine = CatPuzzleEngine()

    private val _uiState = MutableStateFlow(CatPuzzleGameUiState())
    val uiState: StateFlow<CatPuzzleGameUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CatPuzzleGameEffect>(
        extraBufferCapacity = EFFECT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<CatPuzzleGameEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null
    private var currentLevel: CatPuzzleLevel? = null

    init {
        loadLevel(levelIdArg)
    }

    fun loadLevel(levelId: String) {
        val level = dataSource.getLevel(levelId) ?: dataSource.getLevel(BundledCatPuzzleDataSource.FIRST_LEVEL_ID)!!
        currentLevel = level
        val initialPuzzleState = engine.initGame(level)
        val nextLevel = dataSource.getNextLevel(level.id)

        _uiState.update {
            it.copy(
                level = level,
                puzzleState = initialPuzzleState,
                elapsedTimeMs = 0L,
                isPaused = false,
                isCompleted = false,
                starsEarned = 0,
                nextLevelId = nextLevel?.id,
                isLoading = false
            )
        }
        gameEventBus.emit(GameEvent.ModePlayed("cat_puzzle"))
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (!_uiState.value.isPaused && !_uiState.value.isCompleted) {
                    _uiState.update { it.copy(elapsedTimeMs = it.elapsedTimeMs + 1000L) }
                }
            }
        }
    }

    fun onPieceSelected(pieceId: Int) {
        val currentState = _uiState.value.puzzleState ?: return
        val updatedPuzzle = engine.selectTrayPiece(currentState, pieceId)
        _uiState.update { it.copy(puzzleState = updatedPuzzle) }
    }

    fun onSlotTapped(slotIndex: Int) {
        val currentState = _uiState.value.puzzleState ?: return
        val selectedId = currentState.selectedPieceId ?: return
        attemptPlaceOrMove(selectedId, fromSlot = null, targetSlot = slotIndex)
    }

    fun onPieceDropped(pieceId: Int, fromSlot: Int?, targetSlot: Int) {
        attemptPlaceOrMove(pieceId, fromSlot, targetSlot)
    }

    fun onPieceReturnedToTray(fromSlot: Int) {
        val currentState = _uiState.value.puzzleState ?: return
        val result = engine.returnPieceToTray(currentState, fromSlot) ?: return
        _uiState.update { it.copy(puzzleState = result.first) }
    }

    private fun attemptPlaceOrMove(pieceId: Int, fromSlot: Int?, targetSlot: Int) {
        val currentState = _uiState.value.puzzleState ?: return
        val (nextState, result) = engine.placeOrMovePiece(currentState, pieceId, fromSlot, targetSlot)

        when (result) {
            is PlacementResult.Correct -> {
                _uiState.update { it.copy(puzzleState = nextState) }
                _effects.tryEmit(CatPuzzleGameEffect.PlayCorrectAnimation(result.slotIndex))
                _effects.tryEmit(CatPuzzleGameEffect.PlaySuccessSoundOrHaptic)
                if (result.isCompleted) {
                    handleCompletion(nextState.starsEarned)
                }
            }
            is PlacementResult.Incorrect -> {
                // If incorrect, piece still stays at the wrong position, and NO animation is played
                _uiState.update { it.copy(puzzleState = nextState) }
            }
            is PlacementResult.ReturnedToTray -> {
                _uiState.update { it.copy(puzzleState = nextState) }
            }
        }
    }

    fun onHintClicked() {
        val currentState = _uiState.value.puzzleState ?: return
        val targetSlot = (0 until currentState.totalSlots).firstOrNull { i ->
            currentState.slots[i]?.correctSlot != i
        }
        val nextState = engine.useHint(currentState)
        _uiState.update { it.copy(puzzleState = nextState) }
        if (targetSlot != null) {
            _effects.tryEmit(CatPuzzleGameEffect.PlayCorrectAnimation(targetSlot))
            _effects.tryEmit(CatPuzzleGameEffect.PlaySuccessSoundOrHaptic)
        }
        if (nextState.isCompleted) {
            handleCompletion(nextState.starsEarned)
        }
    }

    fun onShuffleClicked() {
        val currentState = _uiState.value.puzzleState ?: return
        val nextState = engine.shuffleTray(currentState)
        _uiState.update { it.copy(puzzleState = nextState) }
    }

    fun onPauseClicked() {
        if (_uiState.value.isCompleted) return
        _uiState.update { it.copy(isPaused = true) }
    }

    fun onResumeClicked() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun onRestartClicked() {
        val level = currentLevel ?: return
        loadLevel(level.id)
    }

    fun onNextLevelClicked() {
        val nextId = _uiState.value.nextLevelId
        if (nextId != null) {
            loadLevel(nextId)
        } else {
            _effects.tryEmit(CatPuzzleGameEffect.NavigateBack)
        }
    }

    fun onExitClicked() {
        _effects.tryEmit(CatPuzzleGameEffect.NavigateBack)
    }

    private fun handleCompletion(stars: Int) {
        if (_uiState.value.isCompleted) return
        timerJob?.cancel()
        val level = currentLevel ?: return
        val nextLevelId = _uiState.value.nextLevelId

        _uiState.update {
            it.copy(
                isCompleted = true,
                starsEarned = stars
            )
        }

        viewModelScope.launch {
            val addedStars = progressRepository.completeLevel(level.id, stars, nextLevelId)
            _uiState.update { it.copy(addedStars = addedStars) }
            gameEventBus.emit(GameEvent.LevelCompleted("cat_puzzle", level.id, stars))
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private companion object {
        const val EFFECT_BUFFER = 8
    }
}
