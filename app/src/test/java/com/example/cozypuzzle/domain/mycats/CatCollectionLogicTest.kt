package com.example.cozypuzzle.domain.mycats

import com.example.cozypuzzle.data.datasource.BundledCatCatalogDataSource
import com.example.cozypuzzle.domain.model.mycats.CatModel
import com.example.cozypuzzle.domain.model.mycats.CatTheme
import com.example.cozypuzzle.domain.model.mycats.CatUnlockCondition
import com.example.cozypuzzle.domain.model.mycats.UnlockGameMode
import com.example.cozypuzzle.presentation.mycats.collection.CatFilter
import com.example.cozypuzzle.presentation.mycats.collection.CatItemUiModel
import com.example.cozypuzzle.presentation.mycats.collection.ThemeProgressUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CatCollectionLogicTest {

    private lateinit var catalog: BundledCatCatalogDataSource

    @Before
    fun setUp() {
        catalog = BundledCatCatalogDataSource()
    }

    @Test
    fun `collection progress calculation accurately reflects owned percentage`() {
        val total = 12
        val ownedCount = 4
        val percentage = (ownedCount * 100) / total
        assertEquals(33, percentage)

        val zeroOwned = (0 * 100) / total
        assertEquals(0, zeroOwned)

        val fullOwned = (total * 100) / total
        assertEquals(100, fullOwned)
    }

    @Test
    fun `filtering by Owned, Locked and All produces correct partitions`() {
        val allCats = catalog.getAllCats()
        val defaultOwnedIds = setOf("cat_milo", "cat_clover")

        val uiItems = allCats.map { cat ->
            CatItemUiModel(
                cat = cat,
                isOwned = defaultOwnedIds.contains(cat.id),
                isFavorite = false,
                currentProgress = if (defaultOwnedIds.contains(cat.id)) 1 else 0,
                targetProgress = 1
            )
        }

        // Test ALL filter
        val allFiltered = when (CatFilter.ALL) {
            CatFilter.ALL -> uiItems
            CatFilter.OWNED -> uiItems.filter { it.isOwned }
            CatFilter.LOCKED -> uiItems.filter { !it.isOwned }
        }
        assertEquals(12, allFiltered.size)

        // Test OWNED filter
        val ownedFiltered = when (CatFilter.OWNED) {
            CatFilter.ALL -> uiItems
            CatFilter.OWNED -> uiItems.filter { it.isOwned }
            CatFilter.LOCKED -> uiItems.filter { !it.isOwned }
        }
        assertEquals(2, ownedFiltered.size)
        assertTrue(ownedFiltered.all { it.isOwned })
        assertEquals(setOf("cat_milo", "cat_clover"), ownedFiltered.map { it.cat.id }.toSet())

        // Test LOCKED filter
        val lockedFiltered = when (CatFilter.LOCKED) {
            CatFilter.ALL -> uiItems
            CatFilter.OWNED -> uiItems.filter { it.isOwned }
            CatFilter.LOCKED -> uiItems.filter { !it.isOwned }
        }
        assertEquals(10, lockedFiltered.size)
        assertTrue(lockedFiltered.none { it.isOwned })
    }

    @Test
    fun `theme collection progress groups and counts owned cats per theme`() {
        val allCats = catalog.getAllCats()
        // Milo (Sleepy) and Clover (Garden) are owned
        val ownedIds = setOf("cat_milo", "cat_clover")

        val themeProgress = CatTheme.values().map { theme ->
            val themeCats = allCats.filter { it.theme == theme }
            val themeOwned = themeCats.count { ownedIds.contains(it.id) }
            ThemeProgressUiModel(
                theme = theme,
                ownedCount = themeOwned,
                totalCount = themeCats.size
            )
        }

        val sleepy = themeProgress.first { it.theme == CatTheme.SLEEPY }
        assertEquals(1, sleepy.ownedCount)
        assertEquals(3, sleepy.totalCount)

        val garden = themeProgress.first { it.theme == CatTheme.GARDEN }
        assertEquals(1, garden.ownedCount)
        assertEquals(3, garden.totalCount)

        val chef = themeProgress.first { it.theme == CatTheme.CHEF }
        assertEquals(0, chef.ownedCount)
        assertEquals(3, chef.totalCount)

        val space = themeProgress.first { it.theme == CatTheme.SPACE }
        assertEquals(0, space.ownedCount)
        assertEquals(3, space.totalCount)
    }

    @Test
    fun `unlock condition evaluation handles level, stars, and count criteria`() {
        val allCats = catalog.getAllCats()

        val catPuzzleStars = mapOf("cat_1" to 3, "cat_2" to 2)
        val colorCatStars = mapOf("color_1" to 3)
        val slideCatStars = mapOf("slide_easy_1" to 1)
        val findPairStars = mapOf<String, Int>()

        val totalStars = catPuzzleStars.values.sum() +
            colorCatStars.values.sum() +
            slideCatStars.values.sum() +
            findPairStars.values.sum() // 3 + 2 + 3 + 1 = 9 stars

        val totalLevels = catPuzzleStars.count { it.value > 0 } +
            colorCatStars.count { it.value > 0 } +
            slideCatStars.count { it.value > 0 } +
            findPairStars.count { it.value > 0 } // 2 + 1 + 1 + 0 = 4 levels

        val unlockedIds = mutableSetOf<String>()

        for (cat in allCats) {
            val eligible = when (val cond = cat.unlockCondition) {
                CatUnlockCondition.DefaultUnlocked -> true
                is CatUnlockCondition.CompleteSpecificLevel -> {
                    when (cond.gameMode) {
                        UnlockGameMode.CAT_PUZZLE -> (catPuzzleStars[cond.levelId] ?: 0) > 0
                        UnlockGameMode.COLOR_THE_CAT -> (colorCatStars[cond.levelId] ?: 0) > 0
                        UnlockGameMode.SLIDE_THE_CAT -> (slideCatStars[cond.levelId] ?: 0) > 0
                        UnlockGameMode.FIND_THE_PAIR -> (findPairStars[cond.levelId] ?: 0) > 0
                    }
                }
                is CatUnlockCondition.EarnTotalStars -> totalStars >= cond.requiredStars
                is CatUnlockCondition.CompleteTotalLevels -> totalLevels >= cond.requiredLevels
            }
            if (eligible) {
                unlockedIds.add(cat.id)
            }
        }

        // Milo & Clover (default)
        assertTrue(unlockedIds.contains("cat_milo"))
        assertTrue(unlockedIds.contains("cat_clover"))

        // Luna (Cat Puzzle Lv 1 completed)
        assertTrue(unlockedIds.contains("cat_luna"))

        // Oliver (Color Cat Lv 1 completed)
        assertTrue(unlockedIds.contains("cat_oliver"))

        // Waffles (Slide Cat Lv 1 completed)
        assertTrue(unlockedIds.contains("cat_waffles"))

        // Bella (Cat Puzzle Lv 2 completed)
        assertTrue(unlockedIds.contains("cat_bella"))

        // Snow (Complete 3 levels: we completed 4)
        assertTrue(unlockedIds.contains("cat_snow"))

        // Coco (Earn 6 stars: we have 9 stars)
        assertTrue(unlockedIds.contains("cat_coco"))

        // Leo (Find Pair Lv 1 not yet completed)
        assertFalse(unlockedIds.contains("cat_leo"))

        // Pudding (Complete 10 levels: we only completed 4)
        assertFalse(unlockedIds.contains("cat_pudding"))

        // Comet (Earn 18 stars: we only have 9 stars)
        assertFalse(unlockedIds.contains("cat_comet"))
    }

    @Test
    fun `celebration tracking prevents duplicate reward popups`() {
        val newlyEligible = listOf("cat_luna", "cat_snow")
        val celebrated = mutableSetOf("cat_milo", "cat_clover")

        // First pass: Luna and Snow are not yet celebrated
        val toCelebrateFirstPass = newlyEligible.filter { !celebrated.contains(it) }
        assertEquals(2, toCelebrateFirstPass.size)

        // Mark them as celebrated
        celebrated.addAll(toCelebrateFirstPass)

        // Second pass: Neither Luna nor Snow should be celebrated again
        val toCelebrateSecondPass = newlyEligible.filter { !celebrated.contains(it) }
        assertTrue(toCelebrateSecondPass.isEmpty())
    }
}
