package com.example.cozypuzzle.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.cozypuzzle.domain.repository.AppSettings
import com.example.cozypuzzle.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings_preferences"
)

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val SOUND_EFFECTS = booleanPreferencesKey("settings_sound_effects")
        val MUSIC = booleanPreferencesKey("settings_music")
        val VIBRATION = booleanPreferencesKey("settings_vibration")
    }

    override fun observeSettings(): Flow<AppSettings> =
        context.settingsDataStore.data.map { prefs ->
            AppSettings(
                soundEffectsEnabled = prefs[Keys.SOUND_EFFECTS] ?: true,
                musicEnabled = prefs[Keys.MUSIC] ?: true,
                vibrationEnabled = prefs[Keys.VIBRATION] ?: true
            )
        }

    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.SOUND_EFFECTS] = enabled
        }
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.MUSIC] = enabled
        }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.VIBRATION] = enabled
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
