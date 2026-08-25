package com.example.cozypuzzle.core.path.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionTest {

    @Test
    fun `opposite directions pair up`() {
        assertEquals(Direction.SOUTH, Direction.NORTH.opposite)
        assertEquals(Direction.NORTH, Direction.SOUTH.opposite)
        assertEquals(Direction.EAST, Direction.WEST.opposite)
        assertEquals(Direction.WEST, Direction.EAST.opposite)
    }

    @Test
    fun `rotation degrees map back via fromDegrees`() {
        Direction.entries.forEach { direction ->
            assertEquals(direction, Direction.fromDegrees(direction.rotationDegrees))
        }
    }

    @Test
    fun `fromDegrees normalizes arbitrary angles`() {
        assertEquals(Direction.NORTH, Direction.fromDegrees(360))
        assertEquals(Direction.WEST, Direction.fromDegrees(-90))
        assertEquals(Direction.EAST, Direction.fromDegrees(450))
    }
}
