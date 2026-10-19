package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BundledCatPuzzleDataSourceTest {

    private lateinit var dataSource: BundledCatPuzzleDataSource

    @Before
    fun setUp() {
        dataSource = BundledCatPuzzleDataSource()
    }

    @Test
    fun `basic difficulty provides exactly 9 levels`() {
        val levels = dataSource.getLevels(CatPuzzleDifficulty.BASIC)
        assertEquals(9, levels.size)
    }

    @Test
    fun `all 9 levels have sequential numbers from 1 to 9`() {
        val levels = dataSource.getLevels(CatPuzzleDifficulty.BASIC)
        for (i in 1..9) {
            val level = levels[i - 1]
            assertEquals(i, level.number)
            assertEquals("cat_$i", level.id)
            assertTrue(level.imageRes != 0)
        }
    }

    @Test
    fun `getNextLevel returns next sequential level and null for last level`() {
        val nextAfter1 = dataSource.getNextLevel("cat_1")
        assertNotNull(nextAfter1)
        assertEquals("cat_2", nextAfter1?.id)

        val nextAfter8 = dataSource.getNextLevel("cat_8")
        assertNotNull(nextAfter8)
        assertEquals("cat_9", nextAfter8?.id)

        val nextAfter9 = dataSource.getNextLevel("cat_9")
        assertNull(nextAfter9)
    }
}
