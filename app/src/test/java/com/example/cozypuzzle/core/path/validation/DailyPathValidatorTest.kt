package com.example.cozypuzzle.core.path.validation

import com.example.cozypuzzle.core.path.model.StrokePoint
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.domain.model.DailyPuzzle
import com.example.cozypuzzle.domain.model.DailyPuzzleTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPathValidatorTest {

    private val validator = DailyPathValidator()

    // 5×5 board with a single obstacle in the middle.
    private fun puzzleWithObstacles(vararg cells: Int) = DailyPuzzle(
        id = "test",
        title = "Test",
        boardSize = 5,
        startCell = 0,
        goalCell = 24,
        obstacleCells = cells.toSet(),
        difficulty = Difficulty.EASY,
        theme = DailyPuzzleTheme.MEADOW
    )

    @Test
    fun `start must be near the start cell center`() {
        val puzzle = puzzleWithObstacles(12)
        val start = puzzle.centerOf(puzzle.startCell)

        val ok = validator.validateStart(puzzle, StrokePoint(start.x + 0.3f, start.y + 0.2f))
        val far = validator.validateStart(puzzle, StrokePoint(start.x + 1.5f, start.y))

        assertTrue(ok is DailyStrokeOutcome.Accepted)
        assertTrue(far is DailyStrokeOutcome.Failed)
        assertEquals(FailureReason.NOT_AT_START, (far as DailyStrokeOutcome.Failed).reason)
    }

    @Test
    fun `center line through free corridor does not hit adjacent obstacles`() {
        // Obstacles at cells 6 and 16: the vertical corridor around x≈0.5
        // between them stays clear even with a full-cell stroke width.
        val puzzle = puzzleWithObstacles(6, 16)
        val from = StrokePoint(0.5f, 1f)
        val to = StrokePoint(0.5f, 2f)

        assertFalse(validator.segmentHitsObstacle(puzzle, from, to))
    }

    @Test
    fun `thick stroke grazing an obstacle fails even when center line misses`() {
        // A thin line at x=1.75 misses the obstacle square [2,3]×[2,3],
        // but the one-cell-wide stroke reaches 0.42 units and must clip it.
        val puzzle = puzzleWithObstacles(12)
        val from = StrokePoint(1.75f, 0.8f)
        val to = StrokePoint(1.75f, 2.5f)

        assertTrue(validator.segmentHitsObstacle(puzzle, from, to))
    }

    @Test
    fun `extension through an obstacle fails the attempt`() {
        val puzzle = puzzleWithObstacles(12)
        // Continuous samples marching straight through the obstacle square
        // [2,3]×[2,3] at y = 2.5.
        val path = listOf(StrokePoint(1.0f, 2.5f))
        val points = listOf(
            StrokePoint(1.6f, 2.5f),
            StrokePoint(2.2f, 2.5f)
        )

        val outcome = validator.validateExtension(puzzle, path, points)

        assertTrue(outcome is DailyStrokeOutcome.Failed)
        assertEquals(FailureReason.OBSTACLE_HIT, (outcome as DailyStrokeOutcome.Failed).reason)
    }

    @Test
    fun `teleporting samples are rejected as discontinuous`() {
        val puzzle = puzzleWithObstacles()
        val path = listOf(StrokePoint(0.5f, 0.5f))
        val points = listOf(StrokePoint(4.5f, 4.5f))

        val outcome = validator.validateExtension(puzzle, path, points)

        assertTrue(outcome is DailyStrokeOutcome.Failed)
        assertEquals(FailureReason.DISCONTINUOUS, (outcome as DailyStrokeOutcome.Failed).reason)
    }

    @Test
    fun `reaching the goal reports completion`() {
        val puzzle = puzzleWithObstacles()
        val goalCenter = puzzle.centerOf(puzzle.goalCell)
        val beforeGoal = StrokePoint(goalCenter.x - 0.4f, goalCenter.y - 0.4f)

        val outcome = validator.validateExtension(
            puzzle,
            listOf(beforeGoal),
            listOf(StrokePoint(goalCenter.x - 0.1f, goalCenter.y - 0.1f))
        )

        assertTrue(outcome is DailyStrokeOutcome.Accepted)
        assertTrue((outcome as DailyStrokeOutcome.Accepted).reachedGoal)
    }

    @Test
    fun `stopping short of the goal keeps the attempt in progress`() {
        val puzzle = puzzleWithObstacles()
        val goalCenter = puzzle.centerOf(puzzle.goalCell)

        val outcome = validator.validateExtension(
            puzzle,
            emptyList(),
            listOf(StrokePoint(goalCenter.x - 2f, goalCenter.y))
        )

        assertTrue(outcome is DailyStrokeOutcome.Accepted)
        assertFalse((outcome as DailyStrokeOutcome.Accepted).reachedGoal)
    }

    @Test
    fun `empty extension is accepted without progress`() {
        val puzzle = puzzleWithObstacles()

        val outcome = validator.validateExtension(puzzle, emptyList(), emptyList())

        assertTrue(outcome is DailyStrokeOutcome.Accepted)
        assertFalse((outcome as DailyStrokeOutcome.Accepted).reachedGoal)
    }
}
