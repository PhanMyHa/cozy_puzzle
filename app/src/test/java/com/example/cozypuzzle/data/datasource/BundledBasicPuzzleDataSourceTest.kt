package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledBasicPuzzleDataSourceTest {

    private val dataSource = BundledBasicPuzzleDataSource()

    @Test
    fun `loads five bundled puzzles`() = runTest {
        val puzzles = dataSource.getPuzzles()

        assertEquals(5, puzzles.size)
    }

    @Test
    fun `every puzzle has complete metadata`() = runTest {
        val puzzles = dataSource.getPuzzles()

        puzzles.forEach { puzzle ->
            assertTrue(puzzle.id.isNotBlank())
            assertTrue(puzzle.title.isNotBlank())
            assertTrue(puzzle.imageId.isNotBlank())
            assertNotNull(puzzle.difficulty)
        }
    }

    @Test
    fun `puzzle ids are unique`() = runTest {
        val puzzles = dataSource.getPuzzles()

        assertEquals(puzzles.size, puzzles.map { it.id }.toSet().size)
    }

    @Test
    fun `available grid sizes are valid and sorted`() = runTest {
        dataSource.getPuzzles().forEach { puzzle ->
            assertTrue("No grid sizes for ${puzzle.id}", puzzle.availableGridSizes.isNotEmpty())
            puzzle.availableGridSizes.forEach { size ->
                assertTrue(size in MIN_GRID_SIZE..MAX_GRID_SIZE)
            }
            assertEquals(
                puzzle.availableGridSizes,
                puzzle.availableGridSizes.sorted()
            )
        }
    }

    @Test
    fun `expected catalog titles exist`() = runTest {
        val titles = dataSource.getPuzzles().map { it.title }

        assertEquals(
            listOf("Cute Cat", "Cozy Room", "Little Garden", "Frog Pond", "Picnic"),
            titles
        )
    }

    @Test
    fun `difficulties cover the full range`() = runTest {
        val difficulties = dataSource.getPuzzles().map { it.difficulty }.toSet()

        assertEquals(Difficulty.entries.toSet(), difficulties)
    }

    @Test
    fun `getPuzzle returns matching puzzle by id`() = runTest {
        val puzzle = dataSource.getPuzzle("cute_cat")

        assertNotNull(puzzle)
        assertEquals("Cute Cat", puzzle?.title)
    }

    @Test
    fun `getPuzzle returns null for unknown id`() = runTest {
        assertNull(dataSource.getPuzzle("does_not_exist"))
    }

    private companion object {
        const val MIN_GRID_SIZE = 3
        const val MAX_GRID_SIZE = 6
    }
}
