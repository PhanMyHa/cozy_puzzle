package com.example.cozypuzzle.core.daily

import com.example.cozypuzzle.domain.model.daily.DailyMission
import com.example.cozypuzzle.domain.model.daily.DailyMissionItemUiModel
import com.example.cozypuzzle.domain.model.daily.DailyMissionType
import com.example.cozypuzzle.domain.model.daily.DayOfWeekUiModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DailyTasksEngine {

    fun calculateStreakOnRollover(
        lastStreakDate: String,
        storedStreak: Int,
        todayEpochDay: Long
    ): Int {
        if (lastStreakDate.isBlank()) return 0
        return try {
            val lastEpoch = LocalDate.parse(lastStreakDate).toEpochDay()
            if (todayEpochDay - lastEpoch == 1L) {
                // Streak is preserved from yesterday
                storedStreak
            } else {
                0
            }
        } catch (_: Exception) {
            0
        }
    }

    fun calculateStreakOnLevelCompleted(
        lastStreakDate: String,
        currentStreak: Int,
        todayDate: String,
        todayEpochDay: Long
    ): Pair<Int, String> {
        if (lastStreakDate == todayDate) {
            // Already counted today
            return currentStreak to todayDate
        }

        val newStreak = if (lastStreakDate.isNotBlank()) {
            val lastEpoch = try {
                LocalDate.parse(lastStreakDate).toEpochDay()
            } catch (_: Exception) {
                0L
            }
            if (todayEpochDay - lastEpoch == 1L) {
                currentStreak + 1
            } else {
                1
            }
        } else {
            1
        }
        return newStreak to todayDate
    }

    fun buildWeekDays(
        today: LocalDate,
        completedDates: Set<String>
    ): List<DayOfWeekUiModel> {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0..6).map { i ->
            val date = monday.plusDays(i.toLong())
            val dateStr = date.toString()
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            DayOfWeekUiModel(
                dayNumber = i + 1,
                dayName = dayName,
                isToday = date == today,
                isCompleted = completedDates.contains(dateStr),
                isFuture = date > today
            )
        }
    }

    fun evaluateMissions(
        levelsCompleted: Int,
        pairsMatched: Int,
        starsEarned: Int,
        modesPlayedCount: Int,
        claimedMissionIds: Set<String>
    ): List<DailyMissionItemUiModel> {
        return DailyMission.DEFAULT_MISSIONS.map { mission ->
            val progress = when (mission.type) {
                DailyMissionType.COMPLETE_LEVELS -> levelsCompleted
                DailyMissionType.MATCH_PAIRS -> pairsMatched
                DailyMissionType.EARN_STARS -> starsEarned
                DailyMissionType.PLAY_MODES -> modesPlayedCount
            }
            val isCompleted = progress >= mission.target
            val isClaimed = claimedMissionIds.contains(mission.id)

            DailyMissionItemUiModel(
                id = mission.id,
                titleRes = mission.titleRes,
                descRes = mission.descRes,
                iconRes = mission.iconRes,
                currentProgress = progress.coerceAtMost(mission.target),
                target = mission.target,
                rewardCoins = mission.rewardCoins,
                isCompleted = isCompleted,
                isClaimed = isClaimed,
                targetModeId = mission.targetModeId
            )
        }
    }

    fun isBonusAvailable(
        missions: List<DailyMissionItemUiModel>,
        isBonusClaimed: Boolean
    ): Boolean {
        if (isBonusClaimed || missions.isEmpty()) return false
        return missions.all { it.isCompleted }
    }
}
