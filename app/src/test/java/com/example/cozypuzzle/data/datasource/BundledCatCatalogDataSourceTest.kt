package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.domain.model.mycats.CatRarity
import com.example.cozypuzzle.domain.model.mycats.CatTheme
import com.example.cozypuzzle.domain.model.mycats.CatUnlockCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BundledCatCatalogDataSourceTest {

    private lateinit var dataSource: BundledCatCatalogDataSource

    @Before
    fun setUp() {
        dataSource = BundledCatCatalogDataSource()
    }

    @Test
    fun `catalog provides exactly 12 distinct cats`() {
        val cats = dataSource.getAllCats()
        assertEquals(12, cats.size)
        val uniqueIds = cats.map { it.id }.toSet()
        assertEquals(12, uniqueIds.size)
    }

    @Test
    fun `all cats have valid resources and non-empty metadata`() {
        val cats = dataSource.getAllCats()
        for (cat in cats) {
            assertTrue(cat.id.isNotBlank())
            assertTrue(cat.name.isNotBlank())
            assertTrue(cat.description.isNotBlank())
            assertTrue(cat.foundIn.isNotBlank())
            assertTrue(cat.imageRes != 0)
        }
    }

    @Test
    fun `all 4 themes are covered with at least 3 cats each`() {
        val cats = dataSource.getAllCats()
        for (theme in CatTheme.values()) {
            val count = cats.count { it.theme == theme }
            assertTrue("Theme $theme should have >= 3 cats", count >= 3)
        }
    }

    @Test
    fun `all 4 rarities are represented in the catalog`() {
        val cats = dataSource.getAllCats()
        for (rarity in CatRarity.values()) {
            val count = cats.count { it.rarity == rarity }
            assertTrue("Rarity $rarity should have at least 1 cat", count >= 1)
        }
    }

    @Test
    fun `default unlocked cats include Milo and Clover`() {
        val defaultIds = dataSource.getDefaultUnlockedIds()
        assertTrue(defaultIds.contains("cat_milo"))
        assertTrue(defaultIds.contains("cat_clover"))
    }

    @Test
    fun `fallback image resource is valid`() {
        assertTrue(BundledCatCatalogDataSource.DEFAULT_FALLBACK_IMAGE_RES != 0)
    }

    @Test
    fun `getCatById returns correct cat or null when not found`() {
        val milo = dataSource.getCatById("cat_milo")
        assertNotNull(milo)
        assertEquals("Milo", milo?.name)
        assertEquals(CatTheme.SLEEPY, milo?.theme)
        assertEquals(CatRarity.COMMON, milo?.rarity)

        val nonExistent = dataSource.getCatById("unknown_cat")
        assertEquals(null, nonExistent)
    }
}
