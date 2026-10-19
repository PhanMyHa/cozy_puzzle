package com.example.cozypuzzle.domain.model.daily

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.cozypuzzle.R

enum class DailyMissionType {
    COMPLETE_LEVELS,
    MATCH_PAIRS,
    EARN_STARS,
    PLAY_MODES
}

data class DailyMission(
    val id: String,
    val type: DailyMissionType,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @DrawableRes val iconRes: Int,
    val target: Int,
    val rewardCoins: Int,
    val targetModeId: String? = null
) {
    companion object {
        const val ID_FIRST_STEP = "mission_first_step"
        const val ID_PUZZLE_MASTER = "mission_puzzle_master"
        const val ID_SHARP_MEMORY = "mission_sharp_memory"
        const val ID_STAR_COLLECTOR = "mission_star_collector"
        const val ID_COZY_EXPLORER = "mission_cozy_explorer"

        val DEFAULT_MISSIONS: List<DailyMission> = listOf(
            DailyMission(
                id = ID_FIRST_STEP,
                type = DailyMissionType.COMPLETE_LEVELS,
                titleRes = R.string.mission_first_step_title,
                descRes = R.string.mission_first_step_desc,
                iconRes = R.drawable.ic_mode_cat,
                target = 1,
                rewardCoins = 20,
                targetModeId = "cat_puzzle"
            ),
            DailyMission(
                id = ID_PUZZLE_MASTER,
                type = DailyMissionType.COMPLETE_LEVELS,
                titleRes = R.string.mission_puzzle_master_title,
                descRes = R.string.mission_puzzle_master_desc,
                iconRes = R.drawable.ic_paw,
                target = 3,
                rewardCoins = 40,
                targetModeId = "cat_puzzle"
            ),
            DailyMission(
                id = ID_SHARP_MEMORY,
                type = DailyMissionType.MATCH_PAIRS,
                titleRes = R.string.mission_sharp_memory_title,
                descRes = R.string.mission_sharp_memory_desc,
                iconRes = R.drawable.ic_mode_find_pair,
                target = 3,
                rewardCoins = 25,
                targetModeId = "find_the_pair"
            ),
            DailyMission(
                id = ID_STAR_COLLECTOR,
                type = DailyMissionType.EARN_STARS,
                titleRes = R.string.mission_star_collector_title,
                descRes = R.string.mission_star_collector_desc,
                iconRes = R.drawable.ic_star_yellow,
                target = 3,
                rewardCoins = 30
            ),
            DailyMission(
                id = ID_COZY_EXPLORER,
                type = DailyMissionType.PLAY_MODES,
                titleRes = R.string.mission_cozy_explorer_title,
                descRes = R.string.mission_cozy_explorer_desc,
                iconRes = R.drawable.ic_mode_slide_cat,
                target = 2,
                rewardCoins = 35
            )
        )

        const val BONUS_COINS = 100
    }
}

data class DailyMissionItemUiModel(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @DrawableRes val iconRes: Int,
    val currentProgress: Int,
    val target: Int,
    val rewardCoins: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val targetModeId: String? = null
)

data class DayOfWeekUiModel(
    val dayNumber: Int, // 1..7 (Mon..Sun)
    val dayName: String,
    val isToday: Boolean,
    val isCompleted: Boolean,
    val isFuture: Boolean
)

data class DailyStreakUiModel(
    val streakCount: Int,
    val isCompletedToday: Boolean,
    val days: List<DayOfWeekUiModel>
)

data class DailyTasksUiState(
    val isLoading: Boolean = false,
    val playerCoins: Int = 0,
    val streak: DailyStreakUiModel = DailyStreakUiModel(
        streakCount = 0,
        isCompletedToday = false,
        days = emptyList()
    ),
    val missions: List<DailyMissionItemUiModel> = emptyList(),
    val bonusRewardCoins: Int = DailyMission.BONUS_COINS,
    val completedMissionsCount: Int = 0,
    val totalMissionsCount: Int = 5,
    val isBonusAvailable: Boolean = false,
    val isBonusClaimed: Boolean = false
)

sealed interface DailyTasksEffect {
    data class ShowRewardCelebration(val amount: Int, val title: String) : DailyTasksEffect
    data class ShowMessage(val messageRes: Int? = null, val text: String? = null) : DailyTasksEffect
    data class NavigateToGame(val targetModeId: String) : DailyTasksEffect
}
