package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.domain.model.ItemCategory
import com.example.cozypuzzle.domain.model.ItemRarity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledShopCatalogDataSourceTest {

    @Test
    fun `every category has at least one item`() = runTest {
        val catalog = BundledShopCatalogDataSource().getCatalog()

        ItemCategory.entries.forEach { category ->
            assertTrue(
                "Catalog is missing items for $category",
                catalog.any { it.category == category }
            )
        }
    }

    @Test
    fun `item ids are unique and prices are positive`() = runTest {
        val catalog = BundledShopCatalogDataSource().getCatalog()

        assertEquals(catalog.size, catalog.map { it.id }.toSet().size)
        catalog.forEach { item ->
            assertTrue("Price of ${item.id} must be positive", item.priceCoins > 0)
            assertTrue("Emoji of ${item.id} must not be blank", item.emoji.isNotBlank())
        }
    }
}
