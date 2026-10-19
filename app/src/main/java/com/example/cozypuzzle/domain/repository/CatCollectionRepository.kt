package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.mycats.CatModel
import kotlinx.coroutines.flow.Flow

interface CatCollectionRepository {

    fun getAllCats(): List<CatModel>

    fun getCatById(catId: String): CatModel?

    fun observeOwnedCatIds(): Flow<Set<String>>

    fun observeFavoriteCatIds(): Flow<Set<String>>

    fun observeCelebratedCatIds(): Flow<Set<String>>

    suspend fun isCatOwned(catId: String): Boolean

    suspend fun isCatFavorite(catId: String): Boolean

    suspend fun toggleFavorite(catId: String)

    suspend fun unlockCat(catId: String): Boolean

    suspend fun markCelebrated(catId: String)

    suspend fun evaluateUnlockConditions(): List<CatModel>

    suspend fun getUnlockProgress(cat: CatModel): Pair<Int, Int>

    suspend fun getAllCatsUnlockProgress(): Map<String, Pair<Int, Int>>
}
