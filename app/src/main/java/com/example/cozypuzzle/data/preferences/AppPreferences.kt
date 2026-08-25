package com.example.cozypuzzle.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cozy_puzzle_preferences"
)

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserPreferences(
    val soundEnabled: Boolean,
    val musicEnabled: Boolean,
    val theme: AppTheme,
    val onboardingCompleted: Boolean
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val THEME = stringPreferencesKey("theme")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: DEFAULT_SOUND_ENABLED,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: DEFAULT_MUSIC_ENABLED,
            theme = prefs[Keys.THEME]?.let { stored ->
                AppTheme.entries.firstOrNull { it.name == stored }
            } ?: AppTheme.SYSTEM,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    companion object {
        const val DEFAULT_SOUND_ENABLED = true
        const val DEFAULT_MUSIC_ENABLED = true
    }
}
