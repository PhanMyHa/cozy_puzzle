package com.example.cozypuzzle.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cozypuzzle.core.common.ClockProvider
import com.example.cozypuzzle.core.profile.ProfileLogic
import com.example.cozypuzzle.domain.model.Player
import com.example.cozypuzzle.domain.model.profile.PlayerStats
import com.example.cozypuzzle.domain.model.starshop.StarRewardStatus
import com.example.cozypuzzle.domain.model.starshop.StarRewardTransaction
import com.example.cozypuzzle.domain.model.starshop.StarShopAchievementItem
import com.example.cozypuzzle.domain.model.starshop.StarShopState
import com.example.cozypuzzle.domain.repository.CatCollectionRepository
import com.example.cozypuzzle.domain.repository.CatPuzzleProgressRepository
import com.example.cozypuzzle.domain.repository.ColorCatProgressRepository
import com.example.cozypuzzle.domain.repository.DailyTasksRepository
import com.example.cozypuzzle.domain.repository.FindPairProgressRepository
import com.example.cozypuzzle.domain.repository.PlayerRepository
import com.example.cozypuzzle.domain.repository.SlideCatProgressRepository
import com.example.cozypuzzle.domain.repository.StarRewardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.starRewardsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "star_rewards_preferences"
)

@Singleton
class StarRewardRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clockProvider: ClockProvider,
    private val playerRepository: PlayerRepository,
    private val catPuzzleProgressRepository: CatPuzzleProgressRepository,
    private val colorCatProgressRepository: ColorCatProgressRepository,
    private val slideCatProgressRepository: SlideCatProgressRepository,
    private val findPairProgressRepository: FindPairProgressRepository,
    private val catCollectionRepository: CatCollectionRepository,
    private val dailyTasksRepository: DailyTasksRepository
) : StarRewardRepository {

    private object Keys {
        val LAST_DAILY_LOGIN_DATE = stringPreferencesKey("last_daily_login_date")
        val CLAIMED_ACHIEVEMENT_IDS = stringSetPreferencesKey("claimed_achievement_ids")
        val TRANSACTION_HISTORY = stringPreferencesKey("transaction_history")
    }

    private data class GamesProgress(
        val catPuzzle: Map<String, Int>,
        val colorCat: Map<String, Int>,
        val slideCat: Map<String, Int>,
        val findPair: Map<String, Int>
    )

    private val gamesProgressFlow = combine(
        catPuzzleProgressRepository.observeLevelStars(),
        colorCatProgressRepository.observeLevelStars(),
        slideCatProgressRepository.observeLevelStars(),
        findPairProgressRepository.observeLevelStars()
    ) { catPuzzle, colorCat, slideCat, findPair ->
        GamesProgress(catPuzzle, colorCat, slideCat, findPair)
    }

    override fun observeStarShopState(): Flow<StarShopState> {
        return combine(
            playerRepository.observePlayer(),
            gamesProgressFlow,
            catCollectionRepository.observeOwnedCatIds(),
            dailyTasksRepository.observeUiState(),
            context.starRewardsDataStore.data
        ) { player, games, ownedCats, dailyState, prefs ->
            buildState(player, games, ownedCats, dailyState, prefs)
        }
    }

    override fun observeRecentTransactions(): Flow<List<StarRewardTransaction>> {
        return context.starRewardsDataStore.data.map { prefs ->
            decodeTransactions(prefs[Keys.TRANSACTION_HISTORY])
        }
    }

    private fun buildState(
        player: Player,
        games: GamesProgress,
        ownedCats: Set<String>,
        dailyState: com.example.cozypuzzle.domain.model.daily.DailyTasksUiState,
        prefs: Preferences
    ): StarShopState {
        val today = clockProvider.currentDateString()
        val lastLoginClaimDate = prefs[Keys.LAST_DAILY_LOGIN_DATE] ?: ""
        val isDailyLoginClaimedToday = (lastLoginClaimDate == today)

        val levelsCompletedCount = games.catPuzzle.count { it.value > 0 } +
                games.colorCat.count { it.value > 0 } +
                games.slideCat.count { it.value > 0 } +
                games.findPair.count { it.value > 0 }

        val stats = ProfileLogic.calculateStats(
            catPuzzleStars = games.catPuzzle,
            colorCatStars = games.colorCat,
            slideCatStars = games.slideCat,
            findPairStars = games.findPair,
            ownedCatsCount = ownedCats.size,
            totalCatsCount = 12,
            dailyStreak = dailyState.streak.streakCount
        )

        val claimedAchievements = prefs[Keys.CLAIMED_ACHIEVEMENT_IDS] ?: emptySet()
        val achievementItems = ProfileLogic.DEFAULT_ACHIEVEMENTS.map { ach ->
            val progress = getProgressForAchievement(ach.id, stats)
            val isUnlocked = progress >= ach.target
            val status = when {
                claimedAchievements.contains(ach.id) -> StarRewardStatus.CLAIMED
                isUnlocked -> StarRewardStatus.CLAIMABLE
                else -> StarRewardStatus.NOT_COMPLETED
            }

            StarShopAchievementItem(
                id = ach.id,
                titleRes = ach.titleRes,
                descRes = ach.descRes,
                iconRes = ach.iconRes,
                currentProgress = progress.coerceAtMost(ach.target),
                target = ach.target,
                rewardStars = ach.rewardStars,
                status = status
            )
        }

        val hasUnclaimedDailyMissions = dailyState.missions.any { it.isCompleted && !it.isClaimed } ||
                (dailyState.isBonusAvailable && !dailyState.isBonusClaimed)

        val claimableDailyLogin = if (!isDailyLoginClaimedToday) 1 else 0
        val claimableDailyMissions = if (hasUnclaimedDailyMissions) 1 else 0
        val claimableAchievements = achievementItems.count { it.status == StarRewardStatus.CLAIMABLE }
        val totalClaimable = claimableDailyLogin + claimableDailyMissions + claimableAchievements

        return StarShopState(
            currentStars = player.coins,
            dailyLoginClaimedToday = isDailyLoginClaimedToday,
            dailyLoginReward = DAILY_LOGIN_REWARD_STARS,
            levelsCompletedCount = levelsCompletedCount,
            totalLevelsCount = TOTAL_CATALOG_LEVELS,
            dailyMissionsCompletedCount = dailyState.completedMissionsCount,
            dailyMissionsTotalCount = dailyState.totalMissionsCount,
            hasUnclaimedDailyMissions = hasUnclaimedDailyMissions,
            achievements = achievementItems,
            unlockedAchievementsCount = achievementItems.count { it.status != StarRewardStatus.NOT_COMPLETED },
            totalAchievementsCount = achievementItems.size,
            totalClaimableCount = totalClaimable
        )
    }

    override suspend fun claimDailyLoginReward(): Int {
        val today = clockProvider.currentDateString()
        var awarded = 0

        context.starRewardsDataStore.edit { prefs ->
            val lastDate = prefs[Keys.LAST_DAILY_LOGIN_DATE] ?: ""
            if (lastDate == today) {
                return@edit
            }
            prefs[Keys.LAST_DAILY_LOGIN_DATE] = today
            awarded = DAILY_LOGIN_REWARD_STARS
            appendTransaction(prefs, "Daily Login Reward", awarded, "DAILY_LOGIN")
        }

        if (awarded > 0) {
            playerRepository.addCoins(awarded)
        }
        return awarded
    }

    override suspend fun claimAchievementReward(achievementId: String): Int {
        val ach = ProfileLogic.DEFAULT_ACHIEVEMENTS.find { it.id == achievementId } ?: return 0
        var awarded = 0

        val catPuzzle = catPuzzleProgressRepository.observeLevelStars().first()
        val colorCat = colorCatProgressRepository.observeLevelStars().first()
        val slideCat = slideCatProgressRepository.observeLevelStars().first()
        val findPair = findPairProgressRepository.observeLevelStars().first()
        val ownedCats = catCollectionRepository.observeOwnedCatIds().first()
        val dailyState = dailyTasksRepository.observeUiState().first()

        val stats = ProfileLogic.calculateStats(
            catPuzzleStars = catPuzzle,
            colorCatStars = colorCat,
            slideCatStars = slideCat,
            findPairStars = findPair,
            ownedCatsCount = ownedCats.size,
            totalCatsCount = 12,
            dailyStreak = dailyState.streak.streakCount
        )

        val progress = getProgressForAchievement(achievementId, stats)
        if (progress < ach.target) {
            return 0
        }

        context.starRewardsDataStore.edit { prefs ->
            val currentClaimed = (prefs[Keys.CLAIMED_ACHIEVEMENT_IDS] ?: emptySet()).toMutableSet()
            if (currentClaimed.contains(achievementId)) {
                return@edit
            }
            currentClaimed.add(achievementId)
            prefs[Keys.CLAIMED_ACHIEVEMENT_IDS] = currentClaimed
            awarded = ach.rewardStars
            appendTransaction(prefs, "Achievement: ${ach.id}", awarded, "ACHIEVEMENT")
        }

        if (awarded > 0) {
            playerRepository.addCoins(awarded)
        }
        return awarded
    }

    override suspend fun recordRewardTransaction(title: String, starsDelta: Int, source: String) {
        context.starRewardsDataStore.edit { prefs ->
            appendTransaction(prefs, title, starsDelta, source)
        }
    }

    private fun getProgressForAchievement(achievementId: String, stats: PlayerStats): Int {
        return when (achievementId) {
            "ach_puzzle_novice", "ach_puzzle_master" -> stats.levelsCompleted
            "ach_star_gazer", "ach_super_star" -> stats.totalStars
            "ach_cat_friend", "ach_cat_collector" -> stats.catsCollected
            "ach_streak_flame" -> stats.dailyStreak
            else -> 0
        }
    }

    private fun appendTransaction(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        title: String,
        delta: Int,
        source: String
    ) {
        val currentEncoded = prefs[Keys.TRANSACTION_HISTORY] ?: ""
        val newTx = "${UUID.randomUUID()}|$title|$delta|${System.currentTimeMillis()}|$source"
        val existingLines = if (currentEncoded.isNotBlank()) currentEncoded.split("\n") else emptyList()
        val combinedLines = listOf(newTx) + existingLines
        val trimmed = combinedLines.take(MAX_TRANSACTIONS_SAVED)
        prefs[Keys.TRANSACTION_HISTORY] = trimmed.joinToString("\n")
    }

    private fun decodeTransactions(encoded: String?): List<StarRewardTransaction> {
        if (encoded.isNullOrBlank()) return emptyList()
        return encoded.split("\n").mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 5) {
                StarRewardTransaction(
                    id = parts[0],
                    title = parts[1],
                    starsDelta = parts[2].toIntOrNull() ?: 0,
                    timestamp = parts[3].toLongOrNull() ?: 0L,
                    source = parts[4]
                )
            } else null
        }
    }

    companion object {
        const val DAILY_LOGIN_REWARD_STARS = 15
        const val TOTAL_CATALOG_LEVELS = 28
        private const val MAX_TRANSACTIONS_SAVED = 20
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StarRewardModule {

    @Binds
    @Singleton
    abstract fun bindStarRewardRepository(
        impl: StarRewardRepositoryImpl
    ): StarRewardRepository
}
