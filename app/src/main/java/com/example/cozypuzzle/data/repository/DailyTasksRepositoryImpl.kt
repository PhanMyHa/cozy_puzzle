package com.example.cozypuzzle.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cozypuzzle.core.common.ClockProvider
import com.example.cozypuzzle.core.common.DispatcherProvider
import com.example.cozypuzzle.core.event.GameEvent
import com.example.cozypuzzle.core.event.GameEventBus
import com.example.cozypuzzle.domain.model.Player
import com.example.cozypuzzle.domain.model.daily.DailyMission
import com.example.cozypuzzle.domain.model.daily.DailyMissionItemUiModel
import com.example.cozypuzzle.domain.model.daily.DailyMissionType
import com.example.cozypuzzle.domain.model.daily.DailyStreakUiModel
import com.example.cozypuzzle.domain.model.daily.DailyTasksUiState
import com.example.cozypuzzle.domain.model.daily.DayOfWeekUiModel
import com.example.cozypuzzle.domain.repository.DailyTasksRepository
import com.example.cozypuzzle.domain.repository.PlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dailyTasksDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "daily_tasks_preferences"
)

@Singleton
class DailyTasksRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clockProvider: ClockProvider,
    private val playerRepository: PlayerRepository,
    private val gameEventBus: GameEventBus,
    dispatcherProvider: DispatcherProvider
) : DailyTasksRepository {

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.io)

    private object Keys {
        val CURRENT_DATE = stringPreferencesKey("daily_current_date")
        val LAST_STREAK_DATE = stringPreferencesKey("daily_last_streak_date")
        val STREAK_COUNT = intPreferencesKey("daily_streak_count")
        val COMPLETED_DATES = stringSetPreferencesKey("daily_completed_dates")
        val LEVELS_COMPLETED_COUNT = intPreferencesKey("daily_levels_completed_count")
        val STARS_EARNED_COUNT = intPreferencesKey("daily_stars_earned_count")
        val PAIRS_MATCHED_COUNT = intPreferencesKey("daily_pairs_matched_count")
        val MODES_PLAYED = stringSetPreferencesKey("daily_modes_played")
        val CLAIMED_MISSIONS = stringSetPreferencesKey("daily_claimed_missions")
        val BONUS_CLAIMED = booleanPreferencesKey("daily_bonus_claimed")
    }

    init {
        scope.launch {
            ensureDateSynchronized()
            gameEventBus.events.collect { event ->
                when (event) {
                    is GameEvent.LevelCompleted -> {
                        recordLevelCompleted(event.mode, event.levelId, event.starsEarned)
                    }
                    is GameEvent.PairsMatched -> {
                        recordPairsMatched(event.count)
                    }
                    is GameEvent.ModePlayed -> {
                        recordModePlayed(event.mode)
                    }
                }
            }
        }
    }

    private data class DailyDataSnapshot(
        val streakCount: Int,
        val lastStreakDate: String,
        val completedDates: Set<String>,
        val levelsCompleted: Int,
        val starsEarned: Int,
        val pairsMatched: Int,
        val modesPlayed: Set<String>,
        val claimedMissions: Set<String>,
        val bonusClaimed: Boolean
    )

    private fun readSnapshot(prefs: Preferences, todayDate: String): DailyDataSnapshot {
        val storedDate = prefs[Keys.CURRENT_DATE] ?: ""
        val lastStreakDate = prefs[Keys.LAST_STREAK_DATE] ?: ""
        val storedStreak = prefs[Keys.STREAK_COUNT] ?: 0
        val completedDates = prefs[Keys.COMPLETED_DATES] ?: emptySet()

        // If day rollover happened but not yet committed by edit, view as reset
        return if (storedDate != todayDate) {
            val adjustedStreak = calculateStreakOnRollover(lastStreakDate, storedStreak)
            DailyDataSnapshot(
                streakCount = adjustedStreak,
                lastStreakDate = lastStreakDate,
                completedDates = completedDates,
                levelsCompleted = 0,
                starsEarned = 0,
                pairsMatched = 0,
                modesPlayed = emptySet(),
                claimedMissions = emptySet(),
                bonusClaimed = false
            )
        } else {
            DailyDataSnapshot(
                streakCount = storedStreak,
                lastStreakDate = lastStreakDate,
                completedDates = completedDates,
                levelsCompleted = prefs[Keys.LEVELS_COMPLETED_COUNT] ?: 0,
                starsEarned = prefs[Keys.STARS_EARNED_COUNT] ?: 0,
                pairsMatched = prefs[Keys.PAIRS_MATCHED_COUNT] ?: 0,
                modesPlayed = prefs[Keys.MODES_PLAYED] ?: emptySet(),
                claimedMissions = prefs[Keys.CLAIMED_MISSIONS] ?: emptySet(),
                bonusClaimed = prefs[Keys.BONUS_CLAIMED] ?: false
            )
        }
    }

    private fun calculateStreakOnRollover(lastStreakDate: String, storedStreak: Int): Int {
        return com.example.cozypuzzle.core.daily.DailyTasksEngine.calculateStreakOnRollover(
            lastStreakDate = lastStreakDate,
            storedStreak = storedStreak,
            todayEpochDay = clockProvider.currentEpochDay()
        )
    }

    private suspend fun ensureDateSynchronized() {
        val today = clockProvider.currentDateString()
        context.dailyTasksDataStore.edit { prefs ->
            val stored = prefs[Keys.CURRENT_DATE] ?: ""
            if (stored != today) {
                val lastStreakDate = prefs[Keys.LAST_STREAK_DATE] ?: ""
                val currentStreak = prefs[Keys.STREAK_COUNT] ?: 0
                val adjustedStreak = calculateStreakOnRollover(lastStreakDate, currentStreak)

                prefs[Keys.CURRENT_DATE] = today
                prefs[Keys.STREAK_COUNT] = adjustedStreak
                prefs[Keys.LEVELS_COMPLETED_COUNT] = 0
                prefs[Keys.STARS_EARNED_COUNT] = 0
                prefs[Keys.PAIRS_MATCHED_COUNT] = 0
                prefs[Keys.MODES_PLAYED] = emptySet()
                prefs[Keys.CLAIMED_MISSIONS] = emptySet()
                prefs[Keys.BONUS_CLAIMED] = false
            }
        }
    }

    override fun observeUiState(): Flow<DailyTasksUiState> {
        val dailyFlow = context.dailyTasksDataStore.data.map { prefs ->
            val todayDate = clockProvider.currentDateString()
            readSnapshot(prefs, todayDate)
        }

        return combine(dailyFlow, playerRepository.observePlayer()) { snapshot, player ->
            buildUiState(snapshot, player)
        }
    }

    override fun observeUnclaimedCount(): Flow<Int> {
        return observeUiState().map { state ->
            val unclaimedMissions = state.missions.count { it.isCompleted && !it.isClaimed }
            val unclaimedBonus = if (state.isBonusAvailable && !state.isBonusClaimed) 1 else 0
            unclaimedMissions + unclaimedBonus
        }.distinctUntilChanged()
    }

    private fun buildUiState(snapshot: DailyDataSnapshot, player: Player): DailyTasksUiState {
        val today = clockProvider.currentDate()
        val todayStr = today.toString()
        val isCompletedToday = snapshot.completedDates.contains(todayStr)

        val weekDays = com.example.cozypuzzle.core.daily.DailyTasksEngine.buildWeekDays(
            today = today,
            completedDates = snapshot.completedDates
        )

        val streakUi = DailyStreakUiModel(
            streakCount = snapshot.streakCount,
            isCompletedToday = isCompletedToday,
            days = weekDays
        )

        val missionUiModels = com.example.cozypuzzle.core.daily.DailyTasksEngine.evaluateMissions(
            levelsCompleted = snapshot.levelsCompleted,
            pairsMatched = snapshot.pairsMatched,
            starsEarned = snapshot.starsEarned,
            modesPlayedCount = snapshot.modesPlayed.size,
            claimedMissionIds = snapshot.claimedMissions
        )

        val completedMissionsCount = missionUiModels.count { it.isCompleted }
        val isBonusAvailable = com.example.cozypuzzle.core.daily.DailyTasksEngine.isBonusAvailable(
            missions = missionUiModels,
            isBonusClaimed = snapshot.bonusClaimed
        )

        return DailyTasksUiState(
            isLoading = false,
            playerCoins = player.coins,
            streak = streakUi,
            missions = missionUiModels,
            bonusRewardCoins = DailyMission.BONUS_COINS,
            completedMissionsCount = completedMissionsCount,
            totalMissionsCount = missionUiModels.size,
            isBonusAvailable = isBonusAvailable,
            isBonusClaimed = snapshot.bonusClaimed
        )
    }

    override suspend fun claimMissionReward(missionId: String): Int {
        val mission = DailyMission.DEFAULT_MISSIONS.find { it.id == missionId } ?: return 0
        val today = clockProvider.currentDateString()

        var rewardedCoins = 0
        context.dailyTasksDataStore.edit { prefs ->
            val snapshot = readSnapshot(prefs, today)
            if (snapshot.claimedMissions.contains(missionId)) {
                return@edit
            }

            val progress = when (mission.type) {
                DailyMissionType.COMPLETE_LEVELS -> snapshot.levelsCompleted
                DailyMissionType.MATCH_PAIRS -> snapshot.pairsMatched
                DailyMissionType.EARN_STARS -> snapshot.starsEarned
                DailyMissionType.PLAY_MODES -> snapshot.modesPlayed.size
            }

            if (progress >= mission.target) {
                val updatedClaimed = snapshot.claimedMissions.toMutableSet()
                updatedClaimed.add(missionId)
                prefs[Keys.CURRENT_DATE] = today
                prefs[Keys.CLAIMED_MISSIONS] = updatedClaimed
                rewardedCoins = mission.rewardCoins
            }
        }

        if (rewardedCoins > 0) {
            playerRepository.addCoins(rewardedCoins)
        }
        return rewardedCoins
    }

    override suspend fun claimBonusReward(): Int {
        val today = clockProvider.currentDateString()
        var rewardedCoins = 0

        context.dailyTasksDataStore.edit { prefs ->
            val snapshot = readSnapshot(prefs, today)
            if (snapshot.bonusClaimed) return@edit

            val allCompleted = DailyMission.DEFAULT_MISSIONS.all { mission ->
                val progress = when (mission.type) {
                    DailyMissionType.COMPLETE_LEVELS -> snapshot.levelsCompleted
                    DailyMissionType.MATCH_PAIRS -> snapshot.pairsMatched
                    DailyMissionType.EARN_STARS -> snapshot.starsEarned
                    DailyMissionType.PLAY_MODES -> snapshot.modesPlayed.size
                }
                progress >= mission.target
            }

            if (allCompleted) {
                prefs[Keys.CURRENT_DATE] = today
                prefs[Keys.BONUS_CLAIMED] = true
                rewardedCoins = DailyMission.BONUS_COINS
            }
        }

        if (rewardedCoins > 0) {
            playerRepository.addCoins(rewardedCoins)
        }
        return rewardedCoins
    }

    override suspend fun recordLevelCompleted(mode: String, levelId: String, stars: Int) {
        val today = clockProvider.currentDateString()
        val todayEpoch = clockProvider.currentEpochDay()

        context.dailyTasksDataStore.edit { prefs ->
            val snapshot = readSnapshot(prefs, today)
            prefs[Keys.CURRENT_DATE] = today
            prefs[Keys.LEVELS_COMPLETED_COUNT] = snapshot.levelsCompleted + 1
            prefs[Keys.STARS_EARNED_COUNT] = snapshot.starsEarned + stars

            val updatedModes = snapshot.modesPlayed.toMutableSet()
            updatedModes.add(mode)
            prefs[Keys.MODES_PLAYED] = updatedModes

            val updatedCompletedDates = snapshot.completedDates.toMutableSet()
            updatedCompletedDates.add(today)
            prefs[Keys.COMPLETED_DATES] = updatedCompletedDates

            // Streak tracking
            val (newStreak, newStreakDate) = com.example.cozypuzzle.core.daily.DailyTasksEngine.calculateStreakOnLevelCompleted(
                lastStreakDate = snapshot.lastStreakDate,
                currentStreak = snapshot.streakCount,
                todayDate = today,
                todayEpochDay = todayEpoch
            )
            prefs[Keys.STREAK_COUNT] = newStreak
            prefs[Keys.LAST_STREAK_DATE] = newStreakDate
        }
    }

    override suspend fun recordPairsMatched(count: Int) {
        val today = clockProvider.currentDateString()
        context.dailyTasksDataStore.edit { prefs ->
            val snapshot = readSnapshot(prefs, today)
            prefs[Keys.CURRENT_DATE] = today
            prefs[Keys.PAIRS_MATCHED_COUNT] = snapshot.pairsMatched + count
        }
    }

    override suspend fun recordModePlayed(mode: String) {
        val today = clockProvider.currentDateString()
        context.dailyTasksDataStore.edit { prefs ->
            val snapshot = readSnapshot(prefs, today)
            prefs[Keys.CURRENT_DATE] = today
            val updatedModes = snapshot.modesPlayed.toMutableSet()
            updatedModes.add(mode)
            prefs[Keys.MODES_PLAYED] = updatedModes
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DailyTasksRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDailyTasksRepository(
        impl: DailyTasksRepositoryImpl
    ): DailyTasksRepository
}
