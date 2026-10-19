package com.example.cozypuzzle.core.catpuzzle

import com.example.cozypuzzle.core.catpuzzle.engine.CatPuzzleEngine
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleDifficulty
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleLevel
import com.example.cozypuzzle.core.catpuzzle.model.PlacementResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CatPuzzleEngineTest {

    private lateinit var engine: CatPuzzleEngine
    private val testLevel = CatPuzzleLevel(
        id = "test_cat_1",
        number = 1,
        title = "Test Level",
        difficulty = CatPuzzleDifficulty.BASIC,
        imageRes = 0,
        gridSize = 3
    )

    @Before
    fun setUp() {
        engine = CatPuzzleEngine()
    }

    @Test
    fun `initGame creates 9 pieces in tray and 9 empty slots`() {
        val state = engine.initGame(testLevel, seed = 42L)

        assertEquals(9, state.totalSlots)
        assertEquals(0, state.placedCount)
        assertEquals(0, state.correctlyPlacedCount)
        assertEquals(9, state.trayPieces.size)
        assertEquals(9, state.slots.size)
        assertTrue(state.slots.all { it == null })
        assertFalse(state.isCompleted)
        assertFalse(state.isPaused)
        assertEquals(3, state.hintsRemaining)
        assertEquals(0, state.hintsUsed)
        assertEquals(0, state.movesCount)
    }

    @Test
    fun `placePiece into wrong slot returns Incorrect but STILL places piece at that wrong slot`() {
        val state = engine.initGame(testLevel, seed = 42L)
        val piece = state.trayPieces.first()
        val wrongSlot = (piece.correctSlot + 1) % 9

        val (nextState, result) = engine.placePiece(state, piece.id, wrongSlot)

        assertTrue(result is PlacementResult.Incorrect)
        assertEquals(1, nextState.placedCount)
        assertEquals(0, nextState.correctlyPlacedCount)
        assertEquals(8, nextState.trayPieces.size)
        assertEquals(piece.id, nextState.slots[wrongSlot]?.id)
        assertEquals(wrongSlot, nextState.slots[wrongSlot]?.currentSlot)
        assertEquals(1, nextState.movesCount)
        assertFalse(nextState.isCompleted)
    }

    @Test
    fun `placePiece into correct slot places piece and returns Correct`() {
        val state = engine.initGame(testLevel, seed = 42L)
        val piece = state.trayPieces.first()
        val correctSlot = piece.correctSlot

        val (nextState, result) = engine.placePiece(state, piece.id, correctSlot)

        assertTrue(result is PlacementResult.Correct)
        assertEquals(1, nextState.placedCount)
        assertEquals(1, nextState.correctlyPlacedCount)
        assertEquals(8, nextState.trayPieces.size)
        assertEquals(piece.id, nextState.slots[correctSlot]?.id)
        assertTrue(nextState.slots[correctSlot]!!.isPlaced)
        assertFalse(nextState.isCompleted)
    }

    @Test
    fun `moving piece from one board slot to another empty slot updates board`() {
        val state = engine.initGame(testLevel, seed = 42L)
        val piece = state.trayPieces.first()

        // Place at slot 0 (even if wrong)
        val (stateWithPiece, _) = engine.placePiece(state, piece.id, 0)
        assertEquals(piece.id, stateWithPiece.slots[0]?.id)

        // Now move from slot 0 to slot 1
        val (movedState, result) = engine.placeOrMovePiece(stateWithPiece, piece.id, fromSlot = 0, targetSlot = 1)
        assertNull(movedState.slots[0])
        assertEquals(piece.id, movedState.slots[1]?.id)
        assertEquals(1, movedState.slots[1]?.currentSlot)
        assertEquals(2, movedState.movesCount)
    }

    @Test
    fun `moving piece from board slot to occupied board slot swaps both pieces`() {
        val state = engine.initGame(testLevel, seed = 42L)
        val pieceA = state.trayPieces[0]
        val pieceB = state.trayPieces[1]

        val (state1, _) = engine.placePiece(state, pieceA.id, 0)
        val (state2, _) = engine.placePiece(state1, pieceB.id, 1)

        assertEquals(pieceA.id, state2.slots[0]?.id)
        assertEquals(pieceB.id, state2.slots[1]?.id)

        // Move pieceA from slot 0 to slot 1 (occupied by pieceB) -> swaps them!
        val (swappedState, _) = engine.placeOrMovePiece(state2, pieceA.id, fromSlot = 0, targetSlot = 1)
        assertEquals(pieceB.id, swappedState.slots[0]?.id)
        assertEquals(pieceA.id, swappedState.slots[1]?.id)
    }

    @Test
    fun `returnPieceToTray removes piece from board and restores to tray`() {
        val state = engine.initGame(testLevel, seed = 42L)
        val piece = state.trayPieces.first()

        val (stateWithPiece, _) = engine.placePiece(state, piece.id, 4)
        assertEquals(8, stateWithPiece.trayPieces.size)
        assertEquals(piece.id, stateWithPiece.slots[4]?.id)

        val result = engine.returnPieceToTray(stateWithPiece, fromSlot = 4)
        assertNotNull(result)
        val (returnedState, returnResult) = result!!
        assertNull(returnedState.slots[4])
        assertEquals(9, returnedState.trayPieces.size)
        assertTrue(returnedState.trayPieces.any { it.id == piece.id })
        assertEquals(piece.id, returnResult.pieceId)
    }

    @Test
    fun `level is NOT completed if 9 pieces are placed but some are in wrong slots`() {
        var state = engine.initGame(testLevel, seed = 42L)

        // Place pieces 0..8 shifted by 1
        for (i in 0 until 9) {
            val wrongSlot = (i + 1) % 9
            val (nextState, _) = engine.placePiece(state, pieceId = i, slotIndex = wrongSlot)
            state = nextState
        }

        assertEquals(9, state.placedCount)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `placing all 9 pieces in their correct slots completes the level with stars`() {
        var state = engine.initGame(testLevel, seed = 42L)

        for (slot in 0 until 9) {
            val (nextState, result) = engine.placePiece(state, pieceId = slot, slotIndex = slot)
            state = nextState
            assertTrue(result is PlacementResult.Correct)
        }

        assertTrue(state.isCompleted)
        assertEquals(9, state.placedCount)
        assertEquals(9, state.correctlyPlacedCount)
        assertTrue(state.trayPieces.isEmpty())
        assertTrue(state.starsEarned in 1..3)
    }

    @Test
    fun `useHint automatically places an unplaced piece into its correct slot`() {
        val state = engine.initGame(testLevel, seed = 42L)
        assertEquals(3, state.hintsRemaining)
        assertEquals(0, state.hintsUsed)

        val nextState = engine.useHint(state)

        assertEquals(1, nextState.placedCount)
        assertEquals(1, nextState.correctlyPlacedCount)
        assertEquals(8, nextState.trayPieces.size)
        assertEquals(2, nextState.hintsRemaining)
        assertEquals(1, nextState.hintsUsed)
        assertNotNull(nextState.slots.firstOrNull { it != null })
    }

    @Test
    fun `shuffleTray maintains tray piece count`() {
        val state = engine.initGame(testLevel, seed = 42L)
        val nextState = engine.shuffleTray(state)

        assertEquals(9, nextState.trayPieces.size)
        assertEquals(state.trayPieces.map { it.id }.toSet(), nextState.trayPieces.map { it.id }.toSet())
    }

    @Test
    fun `calculateStars gives 3 stars for zero hints`() {
        val stars = engine.calculateStars(durationMs = 30000L, hintsUsed = 0, movesCount = 9)
        assertEquals(3, stars)
    }

    @Test
    fun `calculateStars gives 2 stars for two hints`() {
        val stars = engine.calculateStars(durationMs = 60000L, hintsUsed = 2, movesCount = 15)
        assertEquals(2, stars)
    }
}
