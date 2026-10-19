package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BundledSlideCatDataSourceTest {

    private lateinit var dataSource: BundledSlideCatDataSource

    @Before
    fun setUp() {
        dataSource = BundledSlideCatDataSource()
    }

    @Test
    fun `data source provides valid levels for all difficulties`() {
        val easy = dataSource.getLevels(SlideCatDifficulty.EASY)
        val medium = dataSource.getLevels(SlideCatDifficulty.MEDIUM)
        val hard = dataSource.getLevels(SlideCatDifficulty.HARD)

        assertEquals(5, easy.size)
        assertEquals(3, medium.size)
        assertEquals(3, hard.size)

        val all = dataSource.getAllLevels()
        assertEquals(11, all.size)
        assertEquals(11, all.map { it.id }.distinct().size)

        easy.forEach { assertEquals(3, it.gridSize) }
        medium.forEach { assertEquals(4, it.gridSize) }
        hard.forEach { assertEquals(4, it.gridSize) }
    }

    @Test
    fun `getNextLevel returns correct subsequent level`() {
        val next = dataSource.getNextLevel("slide_easy_1")
        assertNotNull(next)
        assertEquals("slide_easy_2", next?.id)

        val lastEasyNext = dataSource.getNextLevel("slide_easy_5")
        assertNull(lastEasyNext)
    }
}
