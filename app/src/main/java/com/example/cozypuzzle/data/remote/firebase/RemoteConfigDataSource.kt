package com.example.cozypuzzle.data.remote.firebase

import com.example.cozypuzzle.domain.model.BasicPuzzleConfig

interface RemoteConfigDataSource {

    suspend fun fetchAndActivate(): Boolean

    fun getBasicPuzzleConfig(): BasicPuzzleConfig?
}
