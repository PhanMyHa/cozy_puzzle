package com.example.cozypuzzle.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cozypuzzle.data.datasource.BundledColorCatDataSource
import com.example.cozypuzzle.domain.repository.ColorCatProgressRepository
import com.example.cozypuzzle.domain.repository.PlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.colorCatDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "color_cat_preferences"
)

@Singleton
class ColorCatProgressRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerRepository: PlayerRepository
) : ColorCatProgressRepository {

    private object Keys {
        val UNLOCKED_LEVELS = stringSetPreferencesKey("color_cat_unlocked_levels")
        val LEVEL_STARS = stringPreferencesKey("color_cat_level_stars")
    }

    override fun observeUnlockedLevels(): Flow<Set<String>> =
        context.colorCatDataStore.data.map { prefs ->
            val set = prefs[Keys.UNLOCKED_LEVELS] ?: emptySet()
            if (set.isEmpty()) setOf(BundledColorCatDataSource.FIRST_LEVEL_ID) else set
        }

    override fun observeLevelStars(): Flow<Map<String, Int>> =
        context.colorCatDataStore.data.map { prefs ->
            decodeStars(prefs[Keys.LEVEL_STARS])
        }

    override suspend fun isLevelUnlocked(levelId: String): Boolean {
        if (levelId == BundledColorCatDataSource.FIRST_LEVEL_ID) return true
        val unlocked = observeUnlockedLevels().first()
        return unlocked.contains(levelId)
    }

    override suspend fun getLevelStars(levelId: String): Int {
        val starsMap = observeLevelStars().first()
        return starsMap[levelId] ?: 0
    }

    override suspend fun completeLevel(levelId: String, stars: Int, nextLevelId: String?): Int {
        var addedStars = 0
        context.colorCatDataStore.edit { prefs ->
            val currentUnlocked = (prefs[Keys.UNLOCKED_LEVELS] ?: emptySet()).toMutableSet()
            currentUnlocked.add(BundledColorCatDataSource.FIRST_LEVEL_ID)
            currentUnlocked.add(levelId)
            if (nextLevelId != null) {
                currentUnlocked.add(nextLevelId)
            }
            prefs[Keys.UNLOCKED_LEVELS] = currentUnlocked

            val currentStarsMap = decodeStars(prefs[Keys.LEVEL_STARS]).toMutableMap()
            val oldStars = currentStarsMap[levelId] ?: 0
            if (stars > oldStars) {
                addedStars = stars - oldStars
                currentStarsMap[levelId] = stars
                prefs[Keys.LEVEL_STARS] = encodeStars(currentStarsMap)
            }
        }

        if (addedStars > 0) {
            playerRepository.addCoins(addedStars)
        }
        return addedStars
    }

    private fun decodeStars(encoded: String?): Map<String, Int> {
        if (encoded.isNullOrBlank()) return emptyMap()
        return encoded.split(",")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val stars = parts[1].toIntOrNull() ?: 0
                    parts[0] to stars
                } else null
            }
            .toMap()
        }

    private fun encodeStars(map: Map<String, Int>): String {
        return map.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ColorCatProgressModule {

    @Binds
    abstract fun bindColorCatProgressRepository(
        impl: ColorCatProgressRepositoryImpl
    ): ColorCatProgressRepository
}
