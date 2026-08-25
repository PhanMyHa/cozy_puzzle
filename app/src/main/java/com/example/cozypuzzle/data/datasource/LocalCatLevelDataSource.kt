package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.domain.model.CatLevel

interface LocalCatLevelDataSource {

    suspend fun getLevels(): List<CatLevel>

    suspend fun getLevel(levelId: String): CatLevel?
}
