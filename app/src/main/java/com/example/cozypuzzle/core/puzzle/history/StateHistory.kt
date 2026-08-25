package com.example.cozypuzzle.core.puzzle.history

/**
 * Generic undo/redo state container shared by every game mode.
 * `PuzzleHistory` is a typealias of this class for the Basic mode board.
 */
class StateHistory<T>(initialState: T) {

    private val undoStack = ArrayDeque<T>()
    private val redoStack = ArrayDeque<T>()

    var currentState: T = initialState
        private set

    fun commit(newState: T) {
        undoStack.addLast(currentState)
        redoStack.clear()
        currentState = newState
    }

    fun undo(): T? {
        val previous = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(currentState)
        currentState = previous
        return currentState
    }

    fun redo(): T? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(currentState)
        currentState = next
        return currentState
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun clear(state: T) {
        undoStack.clear()
        redoStack.clear()
        currentState = state
    }
}
