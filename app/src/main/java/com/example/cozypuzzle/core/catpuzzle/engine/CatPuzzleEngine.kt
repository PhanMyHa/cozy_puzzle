package com.example.cozypuzzle.core.catpuzzle.engine

import com.example.cozypuzzle.core.catpuzzle.model.CatPiece
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleLevel
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleState
import com.example.cozypuzzle.core.catpuzzle.model.PlacementResult
import java.util.Random

class CatPuzzleEngine(
    private val random: Random = Random()
) {

    fun initGame(level: CatPuzzleLevel, seed: Long? = null): CatPuzzleState {
        val totalPieces = level.gridSize * level.gridSize
        val pieces = (0 until totalPieces).map { i ->
            CatPiece(id = i, correctSlot = i, isPlaced = false, currentSlot = null)
        }
        val shuffledPieces = if (seed != null) {
            pieces.shuffled(Random(seed))
        } else {
            pieces.shuffled(random)
        }

        return CatPuzzleState(
            level = level,
            slots = List(totalPieces) { null },
            trayPieces = shuffledPieces,
            selectedPieceId = null,
            hintsRemaining = CatPuzzleState.DEFAULT_HINTS,
            hintsUsed = 0,
            movesCount = 0,
            isCompleted = false,
            isPaused = false,
            elapsedTimeMs = 0L,
            starsEarned = 0
        )
    }

    fun selectTrayPiece(state: CatPuzzleState, pieceId: Int?): CatPuzzleState {
        if (state.isCompleted || state.isPaused) return state
        val newSelection = if (state.selectedPieceId == pieceId) null else pieceId
        return state.copy(selectedPieceId = newSelection)
    }

    fun placeOrMovePiece(
        state: CatPuzzleState,
        pieceId: Int,
        fromSlot: Int?,
        targetSlot: Int
    ): Pair<CatPuzzleState, PlacementResult> {
        if (state.isCompleted || state.isPaused) {
            return state to PlacementResult.Incorrect(pieceId, targetSlot, fromSlot)
        }
        val totalSlots = state.totalSlots
        if (targetSlot !in 0 until totalSlots) {
            return state.copy(movesCount = state.movesCount + 1) to PlacementResult.Incorrect(pieceId, targetSlot, fromSlot)
        }

        // Case 1: Moving a piece from a board slot to another slot
        if (fromSlot != null) {
            if (fromSlot !in 0 until totalSlots || fromSlot == targetSlot) {
                return state to PlacementResult.Incorrect(pieceId, targetSlot, fromSlot)
            }
            val movingPiece = state.slots[fromSlot]
                ?: return state to PlacementResult.Incorrect(pieceId, targetSlot, fromSlot)

            val updatedSlots = state.slots.toMutableList()
            val existingAtTarget = updatedSlots[targetSlot]

            // Place moving piece at target slot
            updatedSlots[targetSlot] = movingPiece.copy(isPlaced = true, currentSlot = targetSlot)
            // If target already had a piece, swap it to fromSlot
            updatedSlots[fromSlot] = existingAtTarget?.copy(isPlaced = true, currentSlot = fromSlot)

            val isCorrect = movingPiece.correctSlot == targetSlot
            val isCompleted = updatedSlots.indices.all { i -> updatedSlots[i]?.correctSlot == i }
            val moves = state.movesCount + 1
            val stars = if (isCompleted) calculateStars(state.elapsedTimeMs, state.hintsUsed, moves) else 0

            val nextState = state.copy(
                slots = updatedSlots,
                selectedPieceId = null,
                movesCount = moves,
                isCompleted = isCompleted,
                starsEarned = stars
            )

            return if (isCorrect) {
                nextState to PlacementResult.Correct(pieceId, targetSlot, isCompleted, fromSlot)
            } else {
                nextState to PlacementResult.Incorrect(pieceId, targetSlot, fromSlot)
            }
        }

        // Case 2: Placing a piece from tray onto board
        val placingPiece = state.trayPieces.firstOrNull { it.id == pieceId }
            ?: return state to PlacementResult.Incorrect(pieceId, targetSlot, null)

        val updatedSlots = state.slots.toMutableList()
        val existingAtTarget = updatedSlots[targetSlot]

        // Place piece into target slot
        updatedSlots[targetSlot] = placingPiece.copy(isPlaced = true, currentSlot = targetSlot)

        // Update tray: remove placed piece, and if slot had an existing piece, send it back to tray
        val updatedTray = state.trayPieces.filterNot { it.id == pieceId }.toMutableList()
        if (existingAtTarget != null) {
            updatedTray.add(existingAtTarget.copy(isPlaced = false, currentSlot = null))
        }

        val isCorrect = placingPiece.correctSlot == targetSlot
        val isCompleted = updatedSlots.indices.all { i -> updatedSlots[i]?.correctSlot == i }
        val moves = state.movesCount + 1
        val stars = if (isCompleted) calculateStars(state.elapsedTimeMs, state.hintsUsed, moves) else 0

        val nextState = state.copy(
            slots = updatedSlots,
            trayPieces = updatedTray,
            selectedPieceId = null,
            movesCount = moves,
            isCompleted = isCompleted,
            starsEarned = stars
        )

        return if (isCorrect) {
            nextState to PlacementResult.Correct(pieceId, targetSlot, isCompleted, null)
        } else {
            nextState to PlacementResult.Incorrect(pieceId, targetSlot, null)
        }
    }

    fun placePiece(
        state: CatPuzzleState,
        pieceId: Int,
        slotIndex: Int
    ): Pair<CatPuzzleState, PlacementResult> {
        return placeOrMovePiece(state, pieceId, fromSlot = null, targetSlot = slotIndex)
    }

    fun returnPieceToTray(
        state: CatPuzzleState,
        fromSlot: Int
    ): Pair<CatPuzzleState, PlacementResult.ReturnedToTray>? {
        if (state.isCompleted || state.isPaused) return null
        if (fromSlot !in 0 until state.totalSlots) return null
        val piece = state.slots[fromSlot] ?: return null

        val updatedSlots = state.slots.toMutableList()
        updatedSlots[fromSlot] = null

        val updatedTray = state.trayPieces.toMutableList()
        updatedTray.add(piece.copy(isPlaced = false, currentSlot = null))

        val nextState = state.copy(
            slots = updatedSlots,
            trayPieces = updatedTray,
            selectedPieceId = null
        )
        return nextState to PlacementResult.ReturnedToTray(piece.id, fromSlot)
    }

    fun useHint(state: CatPuzzleState): CatPuzzleState {
        if (state.isCompleted || state.isPaused || state.hintsRemaining <= 0) return state

        // Find the first slot index that doesn't have its correct piece
        val targetSlot = (0 until state.totalSlots).firstOrNull { i ->
            state.slots[i]?.correctSlot != i
        } ?: return state

        val updatedSlots = state.slots.toMutableList()
        var updatedTray = state.trayPieces.toMutableList()

        // Find the piece that belongs in targetSlot (correctSlot == targetSlot)
        val pieceFromTray = updatedTray.firstOrNull { it.correctSlot == targetSlot }
        if (pieceFromTray != null) {
            // Remove from tray
            updatedTray = updatedTray.filterNot { it.id == pieceFromTray.id }.toMutableList()
            // If target slot had a piece, return it to tray
            val existing = updatedSlots[targetSlot]
            if (existing != null) {
                updatedTray.add(existing.copy(isPlaced = false, currentSlot = null))
            }
            updatedSlots[targetSlot] = pieceFromTray.copy(isPlaced = true, currentSlot = targetSlot)
        } else {
            // Piece is currently at another board slot
            val sourceSlot = (0 until state.totalSlots).firstOrNull { i ->
                state.slots[i]?.correctSlot == targetSlot
            }
            if (sourceSlot != null) {
                val movingPiece = state.slots[sourceSlot]!!
                val existing = updatedSlots[targetSlot]
                updatedSlots[targetSlot] = movingPiece.copy(isPlaced = true, currentSlot = targetSlot)
                updatedSlots[sourceSlot] = existing?.copy(isPlaced = true, currentSlot = sourceSlot)
            }
        }

        val isCompleted = updatedSlots.indices.all { i -> updatedSlots[i]?.correctSlot == i }
        val hintsUsed = state.hintsUsed + 1
        val hintsRemaining = (state.hintsRemaining - 1).coerceAtLeast(0)
        val stars = if (isCompleted) calculateStars(state.elapsedTimeMs, hintsUsed, state.movesCount) else 0

        return state.copy(
            slots = updatedSlots,
            trayPieces = updatedTray,
            selectedPieceId = null,
            hintsUsed = hintsUsed,
            hintsRemaining = hintsRemaining,
            isCompleted = isCompleted,
            starsEarned = stars
        )
    }

    fun shuffleTray(state: CatPuzzleState): CatPuzzleState {
        if (state.isCompleted || state.isPaused) return state
        val shuffled = state.trayPieces.shuffled(random)
        return state.copy(trayPieces = shuffled, selectedPieceId = null)
    }

    fun calculateStars(durationMs: Long, hintsUsed: Int, movesCount: Int): Int {
        return when {
            hintsUsed == 0 && movesCount <= 12 -> 3
            hintsUsed <= 1 -> 3
            hintsUsed <= 2 -> 2
            else -> 1
        }
    }
}
