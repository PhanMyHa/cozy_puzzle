package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.findpair.model.FindPairDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BundledFindPairDataSourceTest {

    private lateinit var dataSource: BundledFindPairDataSource

    @Before
    fun setUp() {
        dataSource = BundledFindPairDataSource()
    }

    @Test
    fun `data source provides valid levels with correct card counts`() {
        val easy = dataSource.getLevels(FindPairDifficulty.EASY)
        val medium = dataSource.getLevels(FindPairDifficulty.MEDIUM)
        val hard = dataSource.getLevels(FindPairDifficulty.HARD)

        assertEquals(5, easy.size)
        assertEquals(3, medium.size)
        assertEquals(3, hard.size)

        easy.forEach {
            assertEquals(12, it.totalCards)
            assertEquals(6, it.totalPairs)
            assertTrue(it.images.size >= 6)
        }

        medium.forEach {
            assertEquals(16, it.totalCards)
            assertEquals(8, it.totalPairs)
            assertTrue(it.images.size >= 8)
        }

        hard.forEach {
            assertEquals(20, it.totalCards)
            assertEquals(10, it.totalPairs)
            assertTrue(it.images.size >= 10)
        }

        val all = dataSource.getAllLevels()
        assertEquals(11, all.size)
        assertEquals(11, all.map { it.id }.distinct().size)
    }

    @Test
    fun `getNextLevel returns correct subsequent level`() {
        val next = dataSource.getNextLevel("pair_easy_1")
        assertNotNull(next)
        assertEquals("pair_easy_2", next?.id)

        val lastEasy = dataSource.getNextLevel("pair_easy_5")
        assertNull(lastEasy)
    }
}
