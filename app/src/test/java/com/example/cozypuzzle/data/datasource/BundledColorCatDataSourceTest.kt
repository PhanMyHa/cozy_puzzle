package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.colorthecat.model.ColorCatDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BundledColorCatDataSourceTest {

    private lateinit var dataSource: BundledColorCatDataSource

    @Before
    fun setUp() {
        dataSource = BundledColorCatDataSource()
    }

    @Test
    fun `basic levels contain 5 valid levels`() {
        val levels = dataSource.getLevels(ColorCatDifficulty.BASIC)
        assertEquals(5, levels.size)
        assertEquals(BundledColorCatDataSource.FIRST_LEVEL_ID, levels.first().id)

        levels.forEachIndexed { index, level ->
            assertEquals(index + 1, level.number)
            assertTrue(level.regions.size >= 6)
            assertTrue(level.paletteColors.isNotEmpty())
            assertTrue(level.viewBoxWidth > 0f)
            assertTrue(level.viewBoxHeight > 0f)

            // Ensure all region IDs are unique within the level
            val regionIds = level.regions.map { it.id }
            assertEquals(regionIds.size, regionIds.distinct().size)

            level.regions.forEach { region ->
                assertTrue(region.pathData.isNotBlank())
            }
        }
    }

    @Test
    fun `getNextLevel returns correct subsequent level`() {
        val next = dataSource.getNextLevel("color_cat_1")
        assertNotNull(next)
        assertEquals("color_cat_2", next?.id)

        val lastNext = dataSource.getNextLevel("color_cat_5")
        assertNull(lastNext)
    }

    @Test
    fun `getLevel returns specific level`() {
        val level3 = dataSource.getLevel("color_cat_3")
        assertNotNull(level3)
        assertEquals(3, level3?.number)
        assertEquals("Level 3", level3?.title)
    }
}
