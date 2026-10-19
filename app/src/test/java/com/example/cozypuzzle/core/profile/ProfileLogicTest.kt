package com.example.cozypuzzle.core.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileLogicTest {

    @Test
    fun `nickname validation rejects empty or blank strings`() {
        assertFalse(ProfileLogic.validateNickname(""))
        assertFalse(ProfileLogic.validateNickname("   "))
        assertFalse(ProfileLogic.validateNickname("\t\n"))
    }

    @Test
    fun `nickname validation rejects names longer than 24 characters`() {
        val tooLong = "A".repeat(25)
        assertFalse(ProfileLogic.validateNickname(tooLong))

        val exactMax = "A".repeat(24)
        assertTrue(ProfileLogic.validateNickname(exactMax))
    }

    @Test
    fun `nickname validation accepts valid names and trims whitespace`() {
        assertTrue(ProfileLogic.validateNickname("CozyCat"))
        assertTrue(ProfileLogic.validateNickname("  Kitty Lover  "))
        assertTrue(ProfileLogic.validateNickname("Milo 123"))
    }

    @Test
    fun `calculateStats correctly aggregates total stars and completed levels across all 4 games`() {
        val catPuzzleStars = mapOf("cat_1" to 3, "cat_2" to 2, "cat_3" to 0) // 5 stars, 2 levels done
        val colorCatStars = mapOf("color_1" to 3, "color_2" to 3) // 6 stars, 2 levels done
        val slideCatStars = mapOf("slide_1" to 1) // 1 star, 1 level done
        val findPairStars = mapOf("pair_1" to 2, "pair_2" to 3) // 5 stars, 2 levels done

        val stats = ProfileLogic.calculateStats(
            catPuzzleStars = catPuzzleStars,
            colorCatStars = colorCatStars,
            slideCatStars = slideCatStars,
            findPairStars = findPairStars,
            ownedCatsCount = 4,
            totalCatsCount = 12,
            dailyStreak = 5
        )

        assertEquals(17, stats.totalStars) // 5 + 6 + 1 + 5 = 17
        assertEquals(7, stats.levelsCompleted) // 2 + 2 + 1 + 2 = 7
        assertEquals(4, stats.catsCollected)
        assertEquals(12, stats.totalCats)
        assertEquals(5, stats.dailyStreak)
    }

    @Test
    fun `calculateStats handles empty progress with zeros`() {
        val stats = ProfileLogic.calculateStats(
            catPuzzleStars = emptyMap(),
            colorCatStars = emptyMap(),
            slideCatStars = emptyMap(),
            findPairStars = emptyMap(),
            ownedCatsCount = 2,
            totalCatsCount = 12,
            dailyStreak = 0
        )

        assertEquals(0, stats.totalStars)
        assertEquals(0, stats.levelsCompleted)
        assertEquals(2, stats.catsCollected)
        assertEquals(0, stats.dailyStreak)
    }

    @Test
    fun `evaluateAchievements unlocks achievements only when target thresholds are reached`() {
        val stats = ProfileLogic.calculateStats(
            catPuzzleStars = mapOf("cat_1" to 3, "cat_2" to 3, "cat_3" to 3, "cat_4" to 3, "cat_5" to 3), // 15 stars, 5 levels
            colorCatStars = mapOf("color_1" to 3, "color_2" to 3), // 6 stars, 2 levels
            slideCatStars = emptyMap(),
            findPairStars = emptyMap(),
            ownedCatsCount = 3,
            totalCatsCount = 12,
            dailyStreak = 3
        )
        // totalStars = 21, levelsCompleted = 7, catsCollected = 3, dailyStreak = 3

        val achievements = ProfileLogic.evaluateAchievements(stats)

        // ach_puzzle_novice: target 5 levels -> 7 completed -> Unlocked!
        val novice = achievements.first { it.id == "ach_puzzle_novice" }
        assertTrue(novice.isUnlocked)
        assertEquals(5, novice.currentProgress)

        // ach_puzzle_master: target 15 levels -> 7 completed -> Locked
        val master = achievements.first { it.id == "ach_puzzle_master" }
        assertFalse(master.isUnlocked)
        assertEquals(7, master.currentProgress)

        // ach_star_gazer: target 20 stars -> 21 earned -> Unlocked!
        val starGazer = achievements.first { it.id == "ach_star_gazer" }
        assertTrue(starGazer.isUnlocked)
        assertEquals(20, starGazer.currentProgress)

        // ach_super_star: target 50 stars -> 21 earned -> Locked
        val superStar = achievements.first { it.id == "ach_super_star" }
        assertFalse(superStar.isUnlocked)
        assertEquals(21, superStar.currentProgress)

        // ach_cat_friend: target 3 cats -> 3 collected -> Unlocked!
        val catFriend = achievements.first { it.id == "ach_cat_friend" }
        assertTrue(catFriend.isUnlocked)
        assertEquals(3, catFriend.currentProgress)

        // ach_cat_collector: target 6 cats -> 3 collected -> Locked
        val catCollector = achievements.first { it.id == "ach_cat_collector" }
        assertFalse(catCollector.isUnlocked)
        assertEquals(3, catCollector.currentProgress)

        // ach_streak_flame: target 3 days streak -> 3 streak -> Unlocked!
        val streak = achievements.first { it.id == "ach_streak_flame" }
        assertTrue(streak.isUnlocked)
        assertEquals(3, streak.currentProgress)
    }
}
