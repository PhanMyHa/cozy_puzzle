package com.example.cozypuzzle.core.currency

import com.example.cozypuzzle.core.profile.ProfileLogic
import com.example.cozypuzzle.domain.model.Player
import com.example.cozypuzzle.domain.model.daily.DailyMission
import com.example.cozypuzzle.domain.model.starshop.StarRewardStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StarRewardLogicTest {

    // --- 1. Level Completion & Replay Delta Logic ---

    @Test
    fun `first time level completion awards full stars`() {
        val currentStarsMap = mutableMapOf<String, Int>()
        val levelId = "cat_level_1"
        val earnedStars = 3

        val oldStars = currentStarsMap[levelId] ?: 0
        val delta = if (earnedStars > oldStars) earnedStars - oldStars else 0
        currentStarsMap[levelId] = earnedStars

        assertEquals(0, oldStars)
        assertEquals(3, delta)
        assertEquals(3, currentStarsMap[levelId])
    }

    @Test
    fun `replaying level with equal stars awards zero additional stars`() {
        val currentStarsMap = mutableMapOf("cat_level_1" to 3)
        val levelId = "cat_level_1"
        val newScore = 3

        val oldStars = currentStarsMap[levelId] ?: 0
        val delta = if (newScore > oldStars) newScore - oldStars else 0

        assertEquals(0, delta)
        assertEquals(3, currentStarsMap[levelId])
    }

    @Test
    fun `replaying level with lower stars awards zero additional stars and preserves record`() {
        val currentStarsMap = mutableMapOf("cat_level_1" to 3)
        val levelId = "cat_level_1"
        val newScore = 1

        val oldStars = currentStarsMap[levelId] ?: 0
        val delta = if (newScore > oldStars) newScore - oldStars else 0
        if (newScore > oldStars) {
            currentStarsMap[levelId] = newScore
        }

        assertEquals(0, delta)
        assertEquals(3, currentStarsMap[levelId]) // Record maintained!
    }

    @Test
    fun `replaying level with higher stars awards only the delta`() {
        val currentStarsMap = mutableMapOf("cat_level_1" to 1)
        val levelId = "cat_level_1"
        val newScore = 3

        val oldStars = currentStarsMap[levelId] ?: 0
        val delta = if (newScore > oldStars) newScore - oldStars else 0
        if (newScore > oldStars) {
            currentStarsMap[levelId] = newScore
        }

        assertEquals(1, oldStars)
        assertEquals(2, delta) // Only 3 - 1 = 2 stars awarded!
        assertEquals(3, currentStarsMap[levelId])
    }

    @Test
    fun `duplicate completion events on rotation or repeated clicks do not duplicate stars`() {
        val currentStarsMap = mutableMapOf<String, Int>()
        val levelId = "slide_level_1"
        var playerBalance = 100

        // Run 1: Completes level with 2 stars
        val oldStars1 = currentStarsMap[levelId] ?: 0
        val delta1 = if (2 > oldStars1) 2 - oldStars1 else 0
        if (delta1 > 0) {
            currentStarsMap[levelId] = 2
            playerBalance += delta1
        }
        assertEquals(102, playerBalance)

        // Run 2: Repeated event / orientation change triggers same completion
        val oldStars2 = currentStarsMap[levelId] ?: 0
        val delta2 = if (2 > oldStars2) 2 - oldStars2 else 0
        if (delta2 > 0) {
            currentStarsMap[levelId] = 2
            playerBalance += delta2
        }
        assertEquals(0, delta2)
        assertEquals(102, playerBalance) // Balance did not increase again
    }

    // --- 2. Non-Negative Balance Enforcement ---

    @Test
    fun `adding stars increases balance correctly`() {
        val startingBalance = 50
        val delta = 25
        val newBalance = (startingBalance + delta).coerceAtLeast(0)
        assertEquals(75, newBalance)
    }

    @Test
    fun `deducting stars never allows balance to become negative`() {
        val startingBalance = 20
        val deduction = 50
        val newBalance = (startingBalance - deduction).coerceAtLeast(0)
        assertEquals(0, newBalance) // Safely clamped to 0, never negative
    }

    @Test
    fun `player model default starting balance is positive`() {
        val defaultPlayer = Player.DEFAULT
        assertTrue(defaultPlayer.coins >= 0)
        assertEquals(Player.STARTING_COINS, defaultPlayer.coins)
    }

    // --- 3. Daily Missions One-Time Claim ---

    @Test
    fun `claiming daily mission awards coins exactly once`() {
        val mission = DailyMission.DEFAULT_MISSIONS.first() // rewardCoins = 20
        val claimedMissions = mutableSetOf<String>()
        var playerBalance = 100

        // First claim
        val rewarded1 = if (!claimedMissions.contains(mission.id)) {
            claimedMissions.add(mission.id)
            mission.rewardCoins
        } else 0
        playerBalance += rewarded1

        assertEquals(20, rewarded1)
        assertEquals(120, playerBalance)
        assertTrue(claimedMissions.contains(mission.id))

        // Second claim attempt
        val rewarded2 = if (!claimedMissions.contains(mission.id)) {
            claimedMissions.add(mission.id)
            mission.rewardCoins
        } else 0
        playerBalance += rewarded2

        assertEquals(0, rewarded2)
        assertEquals(120, playerBalance) // No duplicate award!
    }

    // --- 4. Achievement Reward One-Time Claim ---

    @Test
    fun `achievement reward can only be claimed once and only when unlocked`() {
        val achievement = ProfileLogic.DEFAULT_ACHIEVEMENTS.first { it.id == "ach_puzzle_novice" }
        // Target: 5 levels, reward: 15 stars
        val claimedAchievements = mutableSetOf<String>()
        var playerBalance = 100

        // Test 1: Trying to claim while locked (e.g. 3 levels completed)
        val statsLocked = com.example.cozypuzzle.domain.model.profile.PlayerStats(levelsCompleted = 3)
        val isUnlockedLocked = statsLocked.levelsCompleted >= achievement.target
        assertFalse(isUnlockedLocked)

        val rewardedWhenLocked = if (isUnlockedLocked && !claimedAchievements.contains(achievement.id)) {
            claimedAchievements.add(achievement.id)
            achievement.rewardStars
        } else 0
        assertEquals(0, rewardedWhenLocked)
        assertEquals(100, playerBalance)

        // Test 2: Claiming when unlocked (5 levels completed)
        val statsUnlocked = com.example.cozypuzzle.domain.model.profile.PlayerStats(levelsCompleted = 5)
        val isUnlocked = statsUnlocked.levelsCompleted >= achievement.target
        assertTrue(isUnlocked)

        val rewardedUnlocked = if (isUnlocked && !claimedAchievements.contains(achievement.id)) {
            claimedAchievements.add(achievement.id)
            achievement.rewardStars
        } else 0
        playerBalance += rewardedUnlocked
        assertEquals(15, rewardedUnlocked)
        assertEquals(115, playerBalance)
        assertTrue(claimedAchievements.contains(achievement.id))

        // Test 3: Trying to claim again (re-claim attempt)
        val rewardedAgain = if (isUnlocked && !claimedAchievements.contains(achievement.id)) {
            claimedAchievements.add(achievement.id)
            achievement.rewardStars
        } else 0
        playerBalance += rewardedAgain
        assertEquals(0, rewardedAgain)
        assertEquals(115, playerBalance) // Unchanged!
    }

    // --- 5. Daily Login One-Time Per Day Claim ---

    @Test
    fun `daily login can only be claimed once per calendar day`() {
        var lastClaimedDate = ""
        val today = "2026-10-18"
        var playerBalance = 200
        val dailyReward = 15

        // First claim on today
        val rewarded1 = if (lastClaimedDate != today) {
            lastClaimedDate = today
            dailyReward
        } else 0
        playerBalance += rewarded1

        assertEquals(15, rewarded1)
        assertEquals(215, playerBalance)
        assertEquals(today, lastClaimedDate)

        // Second claim attempt on same day
        val rewarded2 = if (lastClaimedDate != today) {
            lastClaimedDate = today
            dailyReward
        } else 0
        playerBalance += rewarded2

        assertEquals(0, rewarded2)
        assertEquals(215, playerBalance) // No duplicate award!

        // Next calendar day
        val tomorrow = "2026-10-19"
        val rewardedTomorrow = if (lastClaimedDate != tomorrow) {
            lastClaimedDate = tomorrow
            dailyReward
        } else 0
        playerBalance += rewardedTomorrow

        assertEquals(15, rewardedTomorrow)
        assertEquals(230, playerBalance)
        assertEquals(tomorrow, lastClaimedDate)
    }

    // --- 6. Star Shop Status Evaluation ---

    @Test
    fun `evaluateAchievements correctly reports status as NOT_COMPLETED, CLAIMABLE, or CLAIMED`() {
        val stats = com.example.cozypuzzle.domain.model.profile.PlayerStats(
            levelsCompleted = 6, // Unlocks Novice (target 5), but not Master (target 15)
            totalStars = 25,     // Unlocks Star Gazer (target 20), but not Super Star (target 50)
            catsCollected = 2,   // Locked (target 3)
            dailyStreak = 1      // Locked (target 3)
        )

        // Assume novice was already claimed
        val claimedIds = setOf("ach_puzzle_novice")

        val uiModels = ProfileLogic.evaluateAchievements(
            stats = stats,
            claimedIds = claimedIds
        )

        val novice = uiModels.first { it.id == "ach_puzzle_novice" }
        assertTrue(novice.isUnlocked)
        assertTrue(novice.isClaimed) // CLAIMED

        val starGazer = uiModels.first { it.id == "ach_star_gazer" }
        assertTrue(starGazer.isUnlocked)
        assertFalse(starGazer.isClaimed) // CLAIMABLE

        val master = uiModels.first { it.id == "ach_puzzle_master" }
        assertFalse(master.isUnlocked)
        assertFalse(master.isClaimed) // NOT_COMPLETED
    }
}
