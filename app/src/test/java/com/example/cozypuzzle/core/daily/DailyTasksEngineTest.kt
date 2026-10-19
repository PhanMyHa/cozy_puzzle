package com.example.cozypuzzle.core.daily

import com.example.cozypuzzle.domain.model.daily.DailyMission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyTasksEngineTest {

    @Test
    fun `initial state evaluates missions to 0 progress and not completed`() {
        val missions = DailyTasksEngine.evaluateMissions(
            levelsCompleted = 0,
            pairsMatched = 0,
            starsEarned = 0,
            modesPlayedCount = 0,
            claimedMissionIds = emptySet()
        )

        assertEquals(5, missions.size)
        assertTrue(missions.all { !it.isCompleted && !it.isClaimed && it.currentProgress == 0 })
    }

    @Test
    fun `completing 1 level completes First Step but not Puzzle Master`() {
        val missions = DailyTasksEngine.evaluateMissions(
            levelsCompleted = 1,
            pairsMatched = 0,
            starsEarned = 0,
            modesPlayedCount = 1,
            claimedMissionIds = emptySet()
        )

        val firstStep = missions.first { it.id == DailyMission.ID_FIRST_STEP }
        val puzzleMaster = missions.first { it.id == DailyMission.ID_PUZZLE_MASTER }

        assertTrue(firstStep.isCompleted)
        assertEquals(1, firstStep.currentProgress)

        assertFalse(puzzleMaster.isCompleted)
        assertEquals(1, puzzleMaster.currentProgress)
    }

    @Test
    fun `completing 3 levels completes both First Step and Puzzle Master`() {
        val missions = DailyTasksEngine.evaluateMissions(
            levelsCompleted = 3,
            pairsMatched = 0,
            starsEarned = 0,
            modesPlayedCount = 1,
            claimedMissionIds = emptySet()
        )

        val firstStep = missions.first { it.id == DailyMission.ID_FIRST_STEP }
        val puzzleMaster = missions.first { it.id == DailyMission.ID_PUZZLE_MASTER }

        assertTrue(firstStep.isCompleted)
        assertTrue(puzzleMaster.isCompleted)
        assertEquals(3, puzzleMaster.currentProgress)
    }

    @Test
    fun `matching 3 pairs completes Sharp Memory mission`() {
        val missions = DailyTasksEngine.evaluateMissions(
            levelsCompleted = 0,
            pairsMatched = 3,
            starsEarned = 0,
            modesPlayedCount = 1,
            claimedMissionIds = emptySet()
        )

        val sharpMemory = missions.first { it.id == DailyMission.ID_SHARP_MEMORY }
        assertTrue(sharpMemory.isCompleted)
        assertEquals(3, sharpMemory.currentProgress)
    }

    @Test
    fun `earning 3 stars completes Star Collector mission`() {
        val missions = DailyTasksEngine.evaluateMissions(
            levelsCompleted = 1,
            pairsMatched = 0,
            starsEarned = 3,
            modesPlayedCount = 1,
            claimedMissionIds = emptySet()
        )

        val starCollector = missions.first { it.id == DailyMission.ID_STAR_COLLECTOR }
        assertTrue(starCollector.isCompleted)
        assertEquals(3, starCollector.currentProgress)
    }

    @Test
    fun `playing 2 distinct modes completes Cozy Explorer mission`() {
        val missions = DailyTasksEngine.evaluateMissions(
            levelsCompleted = 0,
            pairsMatched = 0,
            starsEarned = 0,
            modesPlayedCount = 2,
            claimedMissionIds = emptySet()
        )

        val cozyExplorer = missions.first { it.id == DailyMission.ID_COZY_EXPLORER }
        assertTrue(cozyExplorer.isCompleted)
        assertEquals(2, cozyExplorer.currentProgress)
    }

    @Test
    fun `bonus reward is only available when all 5 missions are completed and not claimed`() {
        val partialMissions = DailyTasksEngine.evaluateMissions(
            levelsCompleted = 3,
            pairsMatched = 3,
            starsEarned = 3,
            modesPlayedCount = 1, // Need 2 modes
            claimedMissionIds = emptySet()
        )
        assertFalse(DailyTasksEngine.isBonusAvailable(partialMissions, isBonusClaimed = false))

        val allMissions = DailyTasksEngine.evaluateMissions(
            levelsCompleted = 3,
            pairsMatched = 3,
            starsEarned = 3,
            modesPlayedCount = 2,
            claimedMissionIds = emptySet()
        )
        assertTrue(DailyTasksEngine.isBonusAvailable(allMissions, isBonusClaimed = false))
        assertFalse(DailyTasksEngine.isBonusAvailable(allMissions, isBonusClaimed = true))
    }

    @Test
    fun `first puzzle completed starts a streak of 1`() {
        val today = LocalDate.of(2026, 10, 18)
        val (streak, streakDate) = DailyTasksEngine.calculateStreakOnLevelCompleted(
            lastStreakDate = "",
            currentStreak = 0,
            todayDate = today.toString(),
            todayEpochDay = today.toEpochDay()
        )

        assertEquals(1, streak)
        assertEquals("2026-10-18", streakDate)
    }

    @Test
    fun `multiple completions on the same day preserve streak count`() {
        val today = LocalDate.of(2026, 10, 18)
        val (streak, streakDate) = DailyTasksEngine.calculateStreakOnLevelCompleted(
            lastStreakDate = "2026-10-18",
            currentStreak = 3,
            todayDate = today.toString(),
            todayEpochDay = today.toEpochDay()
        )

        assertEquals(3, streak)
        assertEquals("2026-10-18", streakDate)
    }

    @Test
    fun `completion on consecutive day increments streak count`() {
        val yesterday = LocalDate.of(2026, 10, 17)
        val today = LocalDate.of(2026, 10, 18)

        val (streak, streakDate) = DailyTasksEngine.calculateStreakOnLevelCompleted(
            lastStreakDate = yesterday.toString(),
            currentStreak = 4,
            todayDate = today.toString(),
            todayEpochDay = today.toEpochDay()
        )

        assertEquals(5, streak)
        assertEquals("2026-10-18", streakDate)
    }

    @Test
    fun `completion after missing a day resets streak to 1`() {
        val twoDaysAgo = LocalDate.of(2026, 10, 16)
        val today = LocalDate.of(2026, 10, 18)

        val (streak, streakDate) = DailyTasksEngine.calculateStreakOnLevelCompleted(
            lastStreakDate = twoDaysAgo.toString(),
            currentStreak = 7,
            todayDate = today.toString(),
            todayEpochDay = today.toEpochDay()
        )

        assertEquals(1, streak)
        assertEquals("2026-10-18", streakDate)
    }

    @Test
    fun `streak rollover preserves streak if last completed was yesterday`() {
        val yesterday = LocalDate.of(2026, 10, 17)
        val today = LocalDate.of(2026, 10, 18)

        val streak = DailyTasksEngine.calculateStreakOnRollover(
            lastStreakDate = yesterday.toString(),
            storedStreak = 5,
            todayEpochDay = today.toEpochDay()
        )

        assertEquals(5, streak)
    }

    @Test
    fun `streak rollover resets streak to 0 if last completed was more than 1 day ago`() {
        val twoDaysAgo = LocalDate.of(2026, 10, 16)
        val today = LocalDate.of(2026, 10, 18)

        val streak = DailyTasksEngine.calculateStreakOnRollover(
            lastStreakDate = twoDaysAgo.toString(),
            storedStreak = 5,
            todayEpochDay = today.toEpochDay()
        )

        assertEquals(0, streak)
    }

    @Test
    fun `buildWeekDays correctly identifies days of the week, today, and completion states`() {
        // 2026-10-18 is a Sunday (day 7)
        val sunday = LocalDate.of(2026, 10, 18)
        val completed = setOf("2026-10-16", "2026-10-17") // Friday and Saturday completed

        val weekDays = DailyTasksEngine.buildWeekDays(sunday, completed)

        assertEquals(7, weekDays.size)
        // Check days 1 to 7
        for (i in 1..7) {
            assertEquals(i, weekDays[i - 1].dayNumber)
        }

        // Sunday is day 7 and today
        val sun = weekDays[6]
        assertTrue(sun.isToday)
        assertFalse(sun.isCompleted)
        assertFalse(sun.isFuture)

        // Friday is day 5 and completed
        val fri = weekDays[4]
        assertFalse(fri.isToday)
        assertTrue(fri.isCompleted)

        // Saturday is day 6 and completed
        val sat = weekDays[5]
        assertFalse(sat.isToday)
        assertTrue(sat.isCompleted)

        // Monday is day 1 and not completed
        val mon = weekDays[0]
        assertFalse(mon.isCompleted)
    }
}
