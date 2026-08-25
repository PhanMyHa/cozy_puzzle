package com.example.cozypuzzle.core.puzzle.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PuzzlePieceTest {

    @Test
    fun `piece creation stores all fields`() {
        val piece = PuzzlePiece(
            id = 5,
            targetPosition = 5,
            currentPosition = 12,
            rotation = 90
        )

        assertEquals(5, piece.id)
        assertEquals(5, piece.targetPosition)
        assertEquals(12, piece.currentPosition)
        assertEquals(90, piece.rotation)
    }

    @Test
    fun `pieces with same values are equal`() {
        val first = PuzzlePiece(id = 1, targetPosition = 1, currentPosition = 3, rotation = 180)
        val second = PuzzlePiece(id = 1, targetPosition = 1, currentPosition = 3, rotation = 180)

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `pieces differing in one field are not equal`() {
        val first = PuzzlePiece(id = 1, targetPosition = 1, currentPosition = 3, rotation = 0)
        val second = first.copy(rotation = 270)

        assertNotEquals(first, second)
    }

    @Test
    fun `copy changes only targeted field`() {
        val piece = PuzzlePiece(id = 2, targetPosition = 2, currentPosition = 7, rotation = 0)

        val moved = piece.copy(currentPosition = 4)

        assertEquals(4, moved.currentPosition)
        assertEquals(piece.id, moved.id)
        assertEquals(piece.targetPosition, moved.targetPosition)
        assertEquals(piece.rotation, moved.rotation)
    }
}
