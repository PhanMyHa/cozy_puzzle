package com.example.cozypuzzle.presentation.findpair.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.findpair.engine.FindPairEngine
import com.example.cozypuzzle.core.findpair.model.FlipResult
import com.example.cozypuzzle.data.datasource.BundledFindPairDataSource
import com.example.cozypuzzle.domain.repository.FindPairProgressRepository
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
import java.util.Locale
import com.example.cozypuzzle.core.event.GameEvent
import com.example.cozypuzzle.core.event.GameEventBus
import javax.inject.Inject

@HiltViewModel
class FindPairGameViewModel @Inject constructor(
    private val dataSource: BundledFindPairDataSource,
    private val progressRepository: FindPairProgressRepository,
    private val gameEventBus: GameEventBus
) : ViewModel() {

    private val engine = FindPairEngine()

    private val _uiState = MutableStateFlow(FindPairGameUiState())
    val uiState: StateFlow<FindPairGameUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<FindPairGameEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<FindPairGameEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null
    private var mismatchJob: Job? = null
    private var hintDismissJob: Job? = null

    fun loadLevel(levelId: String) {
        val level = dataSource.getLevel(levelId) ?: dataSource.getAllLevels().first()
        val initialState = engine.createInitialState(level)
        _uiState.value = FindPairGameUiState(
            gameState = initialState,
            formattedTimer = formatTimer(initialState.remainingSeconds)
        )
        gameEventBus.emit(GameEvent.ModePlayed("find_the_pair"))
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value.gameState ?: break
                if (!current.isPaused && !current.isCompleted && !current.isGameOver) {
                    val updated = engine.tickTimer(current)
                    _uiState.update {
                        it.copy(
                            gameState = updated,
                            formattedTimer = formatTimer(updated.remainingSeconds)
                        )
                    }

                    if (updated.isGameOver) {
                        timerJob?.cancel()
                        _effects.tryEmit(FindPairGameEffect.ShowGameOverDialog)
                        break
                    }
                }
            }
        }
    }

    fun onCardClicked(cardId: Int) {
        val current = _uiState.value.gameState ?: return
        if (!engine.canFlipCard(current, cardId)) return

        val (nextState, result) = engine.flipCard(current, cardId)
        _uiState.update { it.copy(gameState = nextState) }

        when (result) {
            is FlipResult.CardRevealed -> {
                _effects.tryEmit(FindPairGameEffect.AnimateCardFlip(cardId, toFaceUp = true))
            }
            is FlipResult.CardsMatched -> {
                gameEventBus.emit(GameEvent.PairsMatched(1))
                _effects.tryEmit(FindPairGameEffect.AnimateCardFlip(cardId, toFaceUp = true))
                _effects.tryEmit(
                    FindPairGameEffect.AnimateCardsMatched(result.firstCardId, result.secondCardId)
                )

                if (result.isCompleted) {
                    timerJob?.cancel()
                    val nextLevel = dataSource.getNextLevel(nextState.level.id)
                    val elapsedSeconds = nextState.level.timeLimitSeconds - nextState.remainingSeconds
                    viewModelScope.launch {
                        val addedStars = progressRepository.completeLevel(
                            levelId = nextState.level.id,
                            stars = result.starsEarned,
                            nextLevelId = nextLevel?.id
                        )
                        gameEventBus.emit(
                            GameEvent.LevelCompleted(
                                mode = "find_the_pair",
                                levelId = nextState.level.id,
                                starsEarned = result.starsEarned
                            )
                        )
                        // Allow player to view the matched final pair for 3.0s and see the gradual fade-out
                        delay(3800L)
                        _effects.tryEmit(
                            FindPairGameEffect.ShowLevelComplete(
                                stars = result.starsEarned,
                                pairsFound = nextState.matchedPairCount,
                                totalPairs = nextState.totalPairs,
                                flips = nextState.flipsCount,
                                formattedTime = formatTimer(elapsedSeconds),
                                nextLevelId = nextLevel?.id,
                                addedStars = addedStars
                            )
                        )
                    }
                }
            }
            is FlipResult.CardsMismatched -> {
                _effects.tryEmit(FindPairGameEffect.AnimateCardFlip(cardId, toFaceUp = true))
                // Delay briefly so player can see both cards, then flip them face-down
                mismatchJob?.cancel()
                mismatchJob = viewModelScope.launch {
                    delay(800L)
                    val latest = _uiState.value.gameState ?: return@launch
                    val resolved = engine.resolveMismatch(latest)
                    _uiState.update { it.copy(gameState = resolved) }
                }
            }
            null -> Unit
        }
    }

    fun onHintClicked() {
        val current = _uiState.value.gameState ?: return
        if (current.hintsRemaining <= 0) {
            _effects.tryEmit(FindPairGameEffect.ShowMessage(R.string.find_pair_no_hints))
            return
        }

        val (updated, hintedPair) = engine.useHint(current)
        if (hintedPair != null) {
            _uiState.update { it.copy(gameState = updated) }

            hintDismissJob?.cancel()
            hintDismissJob = viewModelScope.launch {
                delay(3500L)
                val latest = _uiState.value.gameState ?: return@launch
                val cleared = engine.clearHint(latest)
                _uiState.update { it.copy(gameState = cleared) }
            }
        }
    }

    fun onResetClicked() {
        _uiState.update { it.copy(showResetConfirmation = true) }
    }

    fun dismissResetConfirmation() {
        _uiState.update { it.copy(showResetConfirmation = false) }
    }

    fun onConfirmReset() {
        val current = _uiState.value.gameState ?: return
        loadLevel(current.level.id)
        _uiState.update { it.copy(showResetConfirmation = false) }
    }

    fun onPauseClicked() {
        val current = _uiState.value.gameState ?: return
        val paused = engine.pause(current)
        _uiState.update { it.copy(gameState = paused) }
        _effects.tryEmit(FindPairGameEffect.ShowPauseDialog)
    }

    fun onResumeClicked() {
        val current = _uiState.value.gameState ?: return
        val resumed = engine.resume(current)
        _uiState.update { it.copy(gameState = resumed) }
        _effects.tryEmit(FindPairGameEffect.DismissPauseDialog)
    }

    fun onPauseRestartClicked() {
        val current = _uiState.value.gameState ?: return
        loadLevel(current.level.id)
        _effects.tryEmit(FindPairGameEffect.DismissPauseDialog)
    }

    fun onExitClicked() {
        _effects.tryEmit(FindPairGameEffect.DismissPauseDialog)
        _effects.tryEmit(FindPairGameEffect.NavigateToLevels)
    }

    fun onNextLevelClicked(nextLevelId: String) {
        loadLevel(nextLevelId)
    }

    fun onRetryGameOver() {
        val current = _uiState.value.gameState ?: return
        loadLevel(current.level.id)
    }

    override fun onCleared() {
        timerJob?.cancel()
        mismatchJob?.cancel()
        hintDismissJob?.cancel()
        super.onCleared()
    }

    private fun formatTimer(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}
