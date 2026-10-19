package com.example.cozypuzzle.core.slidethecat.engine

import com.example.cozypuzzle.core.slidethecat.model.SlideCatLevel
import com.example.cozypuzzle.core.slidethecat.model.SlideCatState
import com.example.cozypuzzle.core.slidethecat.model.SlideTile
import kotlin.math.abs
import kotlin.random.Random

class SlideCatEngine {

    fun createInitialState(level: SlideCatLevel, seed: Long? = null): SlideCatState {
        val totalSlots = level.gridSize * level.gridSize
        val random = if (seed != null) Random(seed) else Random.Default

        val solvedBoard = (0 until totalSlots - 1).map { index ->
            SlideTile(id = index, correctIndex = index)
        }.toMutableList<SlideTile?>()
        solvedBoard.add(null) // Empty slot at the bottom-right

        var currentEmpty = totalSlots - 1
        var previousMovedSlot = -1

        // Shuffle by making valid moves from solved state to guarantee solvability
        val movesToPerform = level.shuffleMoves.coerceAtLeast(10)
        repeat(movesToPerform) {
            val validNeighbors = getAdjacentIndices(currentEmpty, level.gridSize)
            val candidateNeighbors = if (validNeighbors.size > 1 && previousMovedSlot != -1) {
                validNeighbors.filter { it != previousMovedSlot }
            } else {
                validNeighbors
            }

            val chosenSlot = candidateNeighbors.random(random)
            // Swap chosenSlot and currentEmpty
            solvedBoard[currentEmpty] = solvedBoard[chosenSlot]
            solvedBoard[chosenSlot] = null

            previousMovedSlot = currentEmpty
            currentEmpty = chosenSlot
        }

        // Check if accidentally solved after shuffle; if so, make one more valid move
        if (checkIfSolved(solvedBoard, currentEmpty, totalSlots)) {
            val neighbor = getAdjacentIndices(currentEmpty, level.gridSize).first()
            solvedBoard[currentEmpty] = solvedBoard[neighbor]
            solvedBoard[neighbor] = null
            currentEmpty = neighbor
        }

        return SlideCatState(
            level = level,
            board = solvedBoard,
            emptyIndex = currentEmpty,
            movesCount = 0,
            hintsRemaining = SlideCatState.DEFAULT_HINTS,
            hintsUsed = 0,
            isReferenceOverlayVisible = false,
            showTileNumbersHint = false,
            isCompleted = false,
            isPaused = false,
            elapsedTimeMs = 0L,
            starsEarned = 0
        )
    }

    fun canMove(state: SlideCatState, tileIndex: Int): Boolean {
        if (state.isCompleted || state.isPaused) return false
        val totalSlots = state.level.gridSize * state.level.gridSize
        if (tileIndex !in 0 until totalSlots) return false
        if (tileIndex == state.emptyIndex) return false

        return isAdjacent(tileIndex, state.emptyIndex, state.level.gridSize)
    }

    fun moveTile(state: SlideCatState, tileIndex: Int): SlideCatState {
        if (!canMove(state, tileIndex)) return state

        val updatedBoard = state.board.toMutableList()
        val movingTile = updatedBoard[tileIndex] ?: return state

        // Swap moving tile and empty slot
        updatedBoard[state.emptyIndex] = movingTile
        updatedBoard[tileIndex] = null

        val newEmptyIndex = tileIndex
        val newMovesCount = state.movesCount + 1

        val solved = checkIfSolved(updatedBoard, newEmptyIndex, state.totalSlots)
        val stars = if (solved) {
            calculateStars(newMovesCount, state.level)
        } else {
            0
        }

        return state.copy(
            board = updatedBoard,
            emptyIndex = newEmptyIndex,
            movesCount = newMovesCount,
            isCompleted = solved,
            starsEarned = stars
        )
    }

    fun useHint(state: SlideCatState): SlideCatState {
        if (state.hintsRemaining <= 0 || state.isCompleted || state.isPaused) return state

        return state.copy(
            hintsRemaining = state.hintsRemaining - 1,
            hintsUsed = state.hintsUsed + 1,
            showTileNumbersHint = true
        )
    }

    fun hideTileNumbersHint(state: SlideCatState): SlideCatState {
        return state.copy(showTileNumbersHint = false)
    }

    fun toggleReferenceOverlay(state: SlideCatState): SlideCatState {
        return state.copy(isReferenceOverlayVisible = !state.isReferenceOverlayVisible)
    }

    fun restart(state: SlideCatState, seed: Long? = null): SlideCatState {
        return createInitialState(state.level, seed)
    }

    fun pause(state: SlideCatState): SlideCatState {
        if (state.isCompleted) return state
        return state.copy(isPaused = true)
    }

    fun resume(state: SlideCatState): SlideCatState {
        return state.copy(isPaused = false)
    }

    fun tickTime(state: SlideCatState, deltaMs: Long): SlideCatState {
        if (state.isPaused || state.isCompleted) return state
        return state.copy(elapsedTimeMs = state.elapsedTimeMs + deltaMs)
    }

    fun calculateStars(movesCount: Int, level: SlideCatLevel): Int {
        return when {
            movesCount <= level.threeStarMoves -> 3
            movesCount <= level.twoStarMoves -> 2
            else -> 1
        }
    }

    fun isAdjacent(idxA: Int, idxB: Int, gridSize: Int): Boolean {
        val rowA = idxA / gridSize
        val colA = idxA % gridSize
        val rowB = idxB / gridSize
        val colB = idxB % gridSize

        val rowDiff = abs(rowA - rowB)
        val colDiff = abs(colA - colB)

        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }

    fun getAdjacentIndices(index: Int, gridSize: Int): List<Int> {
        val row = index / gridSize
        val col = index % gridSize
        val neighbors = mutableListOf<Int>()

        if (row > 0) neighbors.add((row - 1) * gridSize + col) // Up
        if (row < gridSize - 1) neighbors.add((row + 1) * gridSize + col) // Down
        if (col > 0) neighbors.add(row * gridSize + (col - 1)) // Left
        if (col < gridSize - 1) neighbors.add(row * gridSize + (col + 1)) // Right

        return neighbors
    }

    private fun checkIfSolved(board: List<SlideTile?>, emptyIndex: Int, totalSlots: Int): Boolean {
        if (emptyIndex != totalSlots - 1) return false
        for (i in 0 until totalSlots - 1) {
            val tile = board[i] ?: return false
            if (tile.correctIndex != i) return false
        }
        return true
    }
}
