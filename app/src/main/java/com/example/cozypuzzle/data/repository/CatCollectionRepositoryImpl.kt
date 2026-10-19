package com.example.cozypuzzle.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cozypuzzle.data.datasource.BundledCatCatalogDataSource
import com.example.cozypuzzle.domain.model.mycats.CatModel
import com.example.cozypuzzle.domain.model.mycats.CatUnlockCondition
import com.example.cozypuzzle.domain.model.mycats.UnlockGameMode
import com.example.cozypuzzle.domain.repository.CatCollectionRepository
import com.example.cozypuzzle.domain.repository.CatPuzzleProgressRepository
import com.example.cozypuzzle.domain.repository.ColorCatProgressRepository
import com.example.cozypuzzle.domain.repository.FindPairProgressRepository
import com.example.cozypuzzle.domain.repository.SlideCatProgressRepository
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
import kotlin.math.min

private val Context.myCatsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "my_cats_preferences"
)

@Singleton
class CatCollectionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val catalogDataSource: BundledCatCatalogDataSource,
    private val catPuzzleRepo: CatPuzzleProgressRepository,
    private val colorCatRepo: ColorCatProgressRepository,
    private val slideCatRepo: SlideCatProgressRepository,
    private val findPairRepo: FindPairProgressRepository
) : CatCollectionRepository {

    private object Keys {
        val OWNED_CATS = stringSetPreferencesKey("owned_cat_ids")
        val FAVORITE_CATS = stringSetPreferencesKey("favorite_cat_ids")
        val CELEBRATED_CATS = stringSetPreferencesKey("celebrated_cat_ids")
    }

    override fun getAllCats(): List<CatModel> = catalogDataSource.getAllCats()

    override fun getCatById(catId: String): CatModel? = catalogDataSource.getCatById(catId)

    override fun observeOwnedCatIds(): Flow<Set<String>> =
        context.myCatsDataStore.data.map { prefs ->
            val set = prefs[Keys.OWNED_CATS] ?: emptySet()
            if (set.isEmpty()) catalogDataSource.getDefaultUnlockedIds() else set
        }

    override fun observeFavoriteCatIds(): Flow<Set<String>> =
        context.myCatsDataStore.data.map { prefs ->
            prefs[Keys.FAVORITE_CATS] ?: emptySet()
        }

    override fun observeCelebratedCatIds(): Flow<Set<String>> =
        context.myCatsDataStore.data.map { prefs ->
            val set = prefs[Keys.CELEBRATED_CATS] ?: emptySet()
            if (set.isEmpty()) catalogDataSource.getDefaultUnlockedIds() else set
        }

    override suspend fun isCatOwned(catId: String): Boolean {
        val owned = observeOwnedCatIds().first()
        return owned.contains(catId)
    }

    override suspend fun isCatFavorite(catId: String): Boolean {
        val favs = observeFavoriteCatIds().first()
        return favs.contains(catId)
    }

    override suspend fun toggleFavorite(catId: String) {
        context.myCatsDataStore.edit { prefs ->
            val current = (prefs[Keys.FAVORITE_CATS] ?: emptySet()).toMutableSet()
            if (current.contains(catId)) {
                current.remove(catId)
            } else {
                current.add(catId)
            }
            prefs[Keys.FAVORITE_CATS] = current
        }
    }

    override suspend fun unlockCat(catId: String): Boolean {
        var newlyUnlocked = false
        context.myCatsDataStore.edit { prefs ->
            val current = (prefs[Keys.OWNED_CATS] ?: catalogDataSource.getDefaultUnlockedIds()).toMutableSet()
            if (!current.contains(catId)) {
                current.add(catId)
                prefs[Keys.OWNED_CATS] = current
                newlyUnlocked = true
            }
        }
        return newlyUnlocked
    }

    override suspend fun markCelebrated(catId: String) {
        context.myCatsDataStore.edit { prefs ->
            val current = (prefs[Keys.CELEBRATED_CATS] ?: catalogDataSource.getDefaultUnlockedIds()).toMutableSet()
            current.add(catId)
            prefs[Keys.CELEBRATED_CATS] = current
        }
    }

    override suspend fun evaluateUnlockConditions(): List<CatModel> {
        val currentOwned = observeOwnedCatIds().first().toMutableSet()
        val currentCelebrated = observeCelebratedCatIds().first()

        val catPuzzleStars = catPuzzleRepo.observeLevelStars().first()
        val colorCatStars = colorCatRepo.observeLevelStars().first()
        val slideCatStars = slideCatRepo.observeLevelStars().first()
        val findPairStars = findPairRepo.observeLevelStars().first()

        val totalStars = catPuzzleStars.values.sum() +
            colorCatStars.values.sum() +
            slideCatStars.values.sum() +
            findPairStars.values.sum()

        val totalLevels = catPuzzleStars.count { it.value > 0 } +
            colorCatStars.count { it.value > 0 } +
            slideCatStars.count { it.value > 0 } +
            findPairStars.count { it.value > 0 }

        val newlyEligibleCats = mutableListOf<CatModel>()

        for (cat in catalogDataSource.getAllCats()) {
            val isEligible = when (val cond = cat.unlockCondition) {
                CatUnlockCondition.DefaultUnlocked -> true
                is CatUnlockCondition.CompleteSpecificLevel -> {
                    when (cond.gameMode) {
                        UnlockGameMode.CAT_PUZZLE -> (catPuzzleStars[cond.levelId] ?: 0) > 0
                        UnlockGameMode.COLOR_THE_CAT -> (colorCatStars[cond.levelId] ?: 0) > 0
                        UnlockGameMode.SLIDE_THE_CAT -> (slideCatStars[cond.levelId] ?: 0) > 0
                        UnlockGameMode.FIND_THE_PAIR -> (findPairStars[cond.levelId] ?: 0) > 0
                    }
                }
                is CatUnlockCondition.EarnTotalStars -> totalStars >= cond.requiredStars
                is CatUnlockCondition.CompleteTotalLevels -> totalLevels >= cond.requiredLevels
            }

            if (isEligible) {
                if (!currentOwned.contains(cat.id)) {
                    currentOwned.add(cat.id)
                }
                if (!currentCelebrated.contains(cat.id)) {
                    newlyEligibleCats.add(cat)
                }
            }
        }

        context.myCatsDataStore.edit { prefs ->
            prefs[Keys.OWNED_CATS] = currentOwned
        }

        return newlyEligibleCats
    }

    override suspend fun getAllCatsUnlockProgress(): Map<String, Pair<Int, Int>> {
        val catPuzzleStars = catPuzzleRepo.observeLevelStars().first()
        val colorCatStars = colorCatRepo.observeLevelStars().first()
        val slideCatStars = slideCatRepo.observeLevelStars().first()
        val findPairStars = findPairRepo.observeLevelStars().first()

        val totalStars = catPuzzleStars.values.sum() +
            colorCatStars.values.sum() +
            slideCatStars.values.sum() +
            findPairStars.values.sum()

        val totalLevels = catPuzzleStars.count { it.value > 0 } +
            colorCatStars.count { it.value > 0 } +
            slideCatStars.count { it.value > 0 } +
            findPairStars.count { it.value > 0 }

        return catalogDataSource.getAllCats().associate { cat ->
            val progress = calculateCatProgress(
                condition = cat.unlockCondition,
                catPuzzleStars = catPuzzleStars,
                colorCatStars = colorCatStars,
                slideCatStars = slideCatStars,
                findPairStars = findPairStars,
                totalStars = totalStars,
                totalLevels = totalLevels
            )
            cat.id to progress
        }
    }

    override suspend fun getUnlockProgress(cat: CatModel): Pair<Int, Int> {
        val catPuzzleStars = catPuzzleRepo.observeLevelStars().first()
        val colorCatStars = colorCatRepo.observeLevelStars().first()
        val slideCatStars = slideCatRepo.observeLevelStars().first()
        val findPairStars = findPairRepo.observeLevelStars().first()

        val totalStars = catPuzzleStars.values.sum() +
            colorCatStars.values.sum() +
            slideCatStars.values.sum() +
            findPairStars.values.sum()

        val totalLevels = catPuzzleStars.count { it.value > 0 } +
            colorCatStars.count { it.value > 0 } +
            slideCatStars.count { it.value > 0 } +
            findPairStars.count { it.value > 0 }

        return calculateCatProgress(
            condition = cat.unlockCondition,
            catPuzzleStars = catPuzzleStars,
            colorCatStars = colorCatStars,
            slideCatStars = slideCatStars,
            findPairStars = findPairStars,
            totalStars = totalStars,
            totalLevels = totalLevels
        )
    }

    private fun calculateCatProgress(
        condition: CatUnlockCondition,
        catPuzzleStars: Map<String, Int>,
        colorCatStars: Map<String, Int>,
        slideCatStars: Map<String, Int>,
        findPairStars: Map<String, Int>,
        totalStars: Int,
        totalLevels: Int
    ): Pair<Int, Int> {
        return when (condition) {
            CatUnlockCondition.DefaultUnlocked -> Pair(1, 1)
            is CatUnlockCondition.CompleteSpecificLevel -> {
                val isCompleted = when (condition.gameMode) {
                    UnlockGameMode.CAT_PUZZLE -> (catPuzzleStars[condition.levelId] ?: 0) > 0
                    UnlockGameMode.COLOR_THE_CAT -> (colorCatStars[condition.levelId] ?: 0) > 0
                    UnlockGameMode.SLIDE_THE_CAT -> (slideCatStars[condition.levelId] ?: 0) > 0
                    UnlockGameMode.FIND_THE_PAIR -> (findPairStars[condition.levelId] ?: 0) > 0
                }
                Pair(if (isCompleted) 1 else 0, 1)
            }
            is CatUnlockCondition.EarnTotalStars -> {
                Pair(min(totalStars, condition.requiredStars), condition.requiredStars)
            }
            is CatUnlockCondition.CompleteTotalLevels -> {
                Pair(min(totalLevels, condition.requiredLevels), condition.requiredLevels)
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CatCollectionModule {

    @Binds
    abstract fun bindCatCollectionRepository(
        impl: CatCollectionRepositoryImpl
    ): CatCollectionRepository
}
