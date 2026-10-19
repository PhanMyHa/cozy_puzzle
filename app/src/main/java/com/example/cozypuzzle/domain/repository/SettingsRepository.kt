package com.example.cozypuzzle.domain.repository

import kotlinx.coroutines.flow.Flow

data class AppSettings(
    val soundEffectsEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

interface SettingsRepository {

    fun observeSettings(): Flow<AppSettings>

    suspend fun setSoundEffectsEnabled(enabled: Boolean)

    suspend fun setMusicEnabled(enabled: Boolean)

    suspend fun setVibrationEnabled(enabled: Boolean)
}
