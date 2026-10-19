package com.example.cozypuzzle.presentation.slidethecat.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.slidethecat.engine.SlideCatEngine
import com.example.cozypuzzle.data.datasource.BundledSlideCatDataSource
import com.example.cozypuzzle.domain.repository.SlideCatProgressRepository
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
class SlideCatGameViewModel @Inject constructor(
    private val dataSource: BundledSlideCatDataSource,
    private val progressRepository: SlideCatProgressRepository,
    private val gameEventBus: GameEventBus
) : ViewModel() {

    private val engine = SlideCatEngine()

    private val _uiState = MutableStateFlow(SlideCatGameUiState())
    val uiState: StateFlow<SlideCatGameUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SlideCatGameEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<SlideCatGameEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null
    private var hintDismissJob: Job? = null

    fun loadLevel(levelId: String) {
        val level = dataSource.getLevel(levelId) ?: dataSource.getAllLevels().first()
        val initialState = engine.createInitialState(level)
        _uiState.value = SlideCatGameUiState(
            gameState = initialState,
            formattedTime = "00:00"
        )
        gameEventBus.emit(GameEvent.ModePlayed("slide_the_cat"))
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value.gameState ?: break
                if (!current.isPaused && !current.isCompleted) {
                    val updated = engine.tickTime(current, 1000L)
                    _uiState.update {
                        it.copy(
                            gameState = updated,
                            formattedTime = formatTime(updated.elapsedTimeMs)
                        )
                    }
                }
            }
        }
    }

    fun onTileClicked(slotIndex: Int) {
        val current = _uiState.value.gameState ?: return
        if (current.isCompleted || current.isPaused) return

        if (!engine.canMove(current, slotIndex)) return

        val updated = engine.moveTile(current, slotIndex)
        _uiState.update { it.copy(gameState = updated) }

        if (updated.isCompleted) {
            timerJob?.cancel()
            val nextLevel = dataSource.getNextLevel(updated.level.id)
            viewModelScope.launch {
                val addedStars = progressRepository.completeLevel(
                    levelId = updated.level.id,
                    stars = updated.starsEarned,
                    nextLevelId = nextLevel?.id
                )
                gameEventBus.emit(
                    GameEvent.LevelCompleted(
                        mode = "slide_the_cat",
                        levelId = updated.level.id,
                        starsEarned = updated.starsEarned
                    )
                )
                _effects.tryEmit(
                    SlideCatGameEffect.ShowLevelComplete(
                        stars = updated.starsEarned,
                        moves = updated.movesCount,
                        hintsUsed = updated.hintsUsed,
                        formattedTime = formatTime(updated.elapsedTimeMs),
                        nextLevelId = nextLevel?.id,
                        addedStars = addedStars
                    )
                )
            }
        }
    }

    fun onHintClicked() {
        val current = _uiState.value.gameState ?: return
        if (current.hintsRemaining <= 0) {
            _effects.tryEmit(SlideCatGameEffect.ShowMessage(R.string.slide_cat_no_hints))
            return
        }

        val updated = engine.useHint(current)
        _uiState.update { it.copy(gameState = updated) }

        hintDismissJob?.cancel()
        hintDismissJob = viewModelScope.launch {
            delay(4000L)
            val latest = _uiState.value.gameState ?: return@launch
            val cleared = engine.hideTileNumbersHint(latest)
            _uiState.update { it.copy(gameState = cleared) }
        }
    }

    fun toggleReferenceOverlay() {
        val current = _uiState.value.gameState ?: return
        val updated = engine.toggleReferenceOverlay(current)
        _uiState.update { it.copy(gameState = updated) }
    }

    fun onRestartClicked() {
        _uiState.update { it.copy(showRestartConfirmation = true) }
    }

    fun dismissRestartConfirmation() {
        _uiState.update { it.copy(showRestartConfirmation = false) }
    }

    fun onConfirmRestart() {
        val current = _uiState.value.gameState ?: return
        loadLevel(current.level.id)
        _uiState.update { it.copy(showRestartConfirmation = false) }
    }

    fun onPauseClicked() {
        val current = _uiState.value.gameState ?: return
        val paused = engine.pause(current)
        _uiState.update { it.copy(gameState = paused) }
        _effects.tryEmit(SlideCatGameEffect.ShowPauseDialog)
    }

    fun onResumeClicked() {
        val current = _uiState.value.gameState ?: return
        val resumed = engine.resume(current)
        _uiState.update { it.copy(gameState = resumed) }
        _effects.tryEmit(SlideCatGameEffect.DismissPauseDialog)
    }

    fun onPauseRestartClicked() {
        val current = _uiState.value.gameState ?: return
        loadLevel(current.level.id)
        _effects.tryEmit(SlideCatGameEffect.DismissPauseDialog)
    }

    fun onExitClicked() {
        _effects.tryEmit(SlideCatGameEffect.DismissPauseDialog)
        _effects.tryEmit(SlideCatGameEffect.NavigateToLevels)
    }

    fun onNextLevelClicked(nextLevelId: String) {
        loadLevel(nextLevelId)
    }

    override fun onCleared() {
        timerJob?.cancel()
        hintDismissJob?.cancel()
        super.onCleared()
    }

    private fun formatTime(millis: Long): String {
        val totalSecs = millis / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}
