package com.example.cozypuzzle.data.remote.firebase

import android.util.Log
import com.example.cozypuzzle.core.common.DispatcherProvider
import com.example.cozypuzzle.domain.model.BasicPuzzleConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigDataSource @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val dispatcherProvider: DispatcherProvider,
    private val parser: BasicPuzzleConfigParser
) : RemoteConfigDataSource {

    init {
        configureDefaults()
    }

    override suspend fun fetchAndActivate(): Boolean = try {
        withContext(dispatcherProvider.io) {
            remoteConfig.fetchAndActivate().await()
        }
    } catch (e: Exception) {
        Log.w(TAG, "Remote Config fetch failed, falling back", e)
        false
    }

    override fun getBasicPuzzleConfig(): BasicPuzzleConfig? {
        val parsed = parser.parse(remoteConfig.getString(RemoteConfigKeys.BASIC_PUZZLE_CONFIG))
        if (parsed == null) {
            Log.w(TAG, "Malformed or disabled basic puzzle config, keeping fallback")
        }
        return parsed
    }

    private fun configureDefaults() {
        remoteConfig.setDefaultsAsync(
            mapOf(RemoteConfigKeys.BASIC_PUZZLE_CONFIG to DEFAULT_BASIC_PUZZLE_CONFIG_JSON)
        )
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(MIN_FETCH_INTERVAL_SECONDS)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
    }

    private companion object {
        const val TAG = "FirebaseRCDataSource"
        const val MIN_FETCH_INTERVAL_SECONDS = 3600L

        val DEFAULT_BASIC_PUZZLE_CONFIG_JSON: String = JSONObject().apply {
            put("enabled", true)
            put("available_sizes", JSONArray(listOf(3, 4, 5, 6)))
            put("default_size", 4)
            put("categories", JSONArray(BasicPuzzleConfig.DEFAULT.categories))
            put("allow_rotate", true)
            put("allow_swap", true)
            put("allow_drag", true)
            put("max_hints", 3)
        }.toString()
    }
}
