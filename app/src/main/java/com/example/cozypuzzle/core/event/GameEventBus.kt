package com.example.cozypuzzle.core.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface GameEvent {
    data class LevelCompleted(
        val mode: String,
        val levelId: String,
        val starsEarned: Int
    ) : GameEvent

    data class ModePlayed(
        val mode: String
    ) : GameEvent

    data class PairsMatched(
        val count: Int = 1
    ) : GameEvent
}

@Singleton
class GameEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<GameEvent>(
        extraBufferCapacity = BUFFER_CAPACITY
    )
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    fun emit(event: GameEvent) {
        _events.tryEmit(event)
    }

    private companion object {
        const val BUFFER_CAPACITY = 64
    }
}
