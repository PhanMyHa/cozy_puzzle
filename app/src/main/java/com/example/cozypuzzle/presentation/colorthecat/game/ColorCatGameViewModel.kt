package com.example.cozypuzzle.presentation.colorthecat.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.colorthecat.engine.ColoringEngine
import com.example.cozypuzzle.data.datasource.BundledColorCatDataSource
import com.example.cozypuzzle.domain.repository.ColorCatProgressRepository
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
class ColorCatGameViewModel @Inject constructor(
    private val dataSource: BundledColorCatDataSource,
    private val progressRepository: ColorCatProgressRepository,
    private val gameEventBus: GameEventBus
) : ViewModel() {

    private val engine = ColoringEngine()

    private val _uiState = MutableStateFlow(ColorCatGameUiState())
    val uiState: StateFlow<ColorCatGameUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ColorCatGameEffect>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<ColorCatGameEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null
    private var hintDismissJob: Job? = null

    fun loadLevel(levelId: String) {
        val level = dataSource.getLevel(levelId) ?: dataSource.getAllLevels().first()
        val initialState = engine.createInitialState(level)
        _uiState.value = ColorCatGameUiState(
            gameState = initialState,
            formattedTime = "00:00",
            canUndo = false
        )
        gameEventBus.emit(GameEvent.ModePlayed("color_the_cat"))
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

    fun onColorSelected(color: Int) {
        val current = _uiState.value.gameState ?: return
        val updated = engine.selectColor(current, color)
        _uiState.update { it.copy(gameState = updated) }
    }

    fun onRegionClicked(regionId: String) {
        val current = _uiState.value.gameState ?: return
        if (current.isCompleted || current.isPaused) return

        val updated = engine.colorRegion(current, regionId)
        _uiState.update {
            it.copy(
                gameState = updated,
                canUndo = updated.undoStack.isNotEmpty()
            )
        }

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
                        mode = "color_the_cat",
                        levelId = updated.level.id,
                        starsEarned = updated.starsEarned
                    )
                )
                _effects.tryEmit(
                    ColorCatGameEffect.ShowLevelComplete(
                        stars = updated.starsEarned,
                        hintsUsed = updated.hintsUsed,
                        formattedTime = formatTime(updated.elapsedTimeMs),
                        nextLevelId = nextLevel?.id,
                        addedStars = addedStars
                    )
                )
            }
        }
    }

    fun onUndoClicked() {
        val current = _uiState.value.gameState ?: return
        val updated = engine.undo(current)
        _uiState.update {
            it.copy(
                gameState = updated,
                canUndo = updated.undoStack.isNotEmpty()
            )
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
        val updated = engine.reset(current)
        _uiState.update {
            it.copy(
                gameState = updated,
                canUndo = false,
                showResetConfirmation = false
            )
        }
    }

    fun onHintClicked() {
        val current = _uiState.value.gameState ?: return
        if (current.hintsRemaining <= 0) {
            _effects.tryEmit(ColorCatGameEffect.ShowMessage(R.string.color_cat_no_hints))
            return
        }

        val (updated, hintedRegionId) = engine.useHint(current)
        if (hintedRegionId != null) {
            _uiState.update { it.copy(gameState = updated) }

            // Auto clear highlight after 3.5 seconds if not colored
            hintDismissJob?.cancel()
            hintDismissJob = viewModelScope.launch {
                delay(3500L)
                val latest = _uiState.value.gameState ?: return@launch
                val cleared = engine.clearHint(latest)
                _uiState.update { it.copy(gameState = cleared) }
            }
        }
    }

    fun onPauseClicked() {
        val current = _uiState.value.gameState ?: return
        val paused = engine.pause(current)
        _uiState.update { it.copy(gameState = paused) }
        _effects.tryEmit(ColorCatGameEffect.ShowPauseDialog)
    }

    fun onResumeClicked() {
        val current = _uiState.value.gameState ?: return
        val resumed = engine.resume(current)
        _uiState.update { it.copy(gameState = resumed) }
        _effects.tryEmit(ColorCatGameEffect.DismissPauseDialog)
    }

    fun onRestartClicked() {
        val current = _uiState.value.gameState ?: return
        loadLevel(current.level.id)
        _effects.tryEmit(ColorCatGameEffect.DismissPauseDialog)
    }

    fun onExitClicked() {
        _effects.tryEmit(ColorCatGameEffect.DismissPauseDialog)
        _effects.tryEmit(ColorCatGameEffect.NavigateToLevels)
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
