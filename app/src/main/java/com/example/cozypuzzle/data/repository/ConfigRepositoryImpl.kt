package com.example.cozypuzzle.data.repository

import com.example.cozypuzzle.data.remote.firebase.RemoteConfigDataSource
import com.example.cozypuzzle.domain.model.BasicPuzzleConfig
import com.example.cozypuzzle.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepositoryImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource
) : ConfigRepository {

    private val _basicPuzzleConfig = MutableStateFlow(BasicPuzzleConfig.DEFAULT)

    override val basicPuzzleConfig: StateFlow<BasicPuzzleConfig> =
        _basicPuzzleConfig.asStateFlow()

    override suspend fun refreshBasicPuzzleConfig() {
        val activated = try {
            remoteConfigDataSource.fetchAndActivate()
        } catch (e: Exception) {
            false
        }
        if (!activated) return

        remoteConfigDataSource.getBasicPuzzleConfig()
            ?.let { parsed -> _basicPuzzleConfig.value = parsed }
    }
}
