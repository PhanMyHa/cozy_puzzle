package com.example.cozypuzzle.presentation.mycats.collection

import com.example.cozypuzzle.domain.model.mycats.CatModel
import com.example.cozypuzzle.domain.model.mycats.CatTheme

enum class CatFilter {
    ALL,
    OWNED,
    LOCKED
}

data class CatItemUiModel(
    val cat: CatModel,
    val isOwned: Boolean,
    val isFavorite: Boolean,
    val currentProgress: Int,
    val targetProgress: Int
)

data class ThemeProgressUiModel(
    val theme: CatTheme,
    val ownedCount: Int,
    val totalCount: Int
)

data class MyCatsUiState(
    val filter: CatFilter = CatFilter.ALL,
    val cats: List<CatItemUiModel> = emptyList(),
    val totalCats: Int = 0,
    val ownedCatsCount: Int = 0,
    val playerStars: Int = 0,
    val themeProgress: List<ThemeProgressUiModel> = emptyList(),
    val isLoading: Boolean = false
) {
    val progressPercentage: Int
        get() = if (totalCats > 0) (ownedCatsCount * 100) / totalCats else 0
}

sealed class MyCatsEffect {
    data class NavigateToDetail(val catId: String) : MyCatsEffect()
    data class ShowNewCatUnlocked(val cat: CatModel) : MyCatsEffect()
    data class ShowLockedCondition(val cat: CatModel, val current: Int, val target: Int) : MyCatsEffect()
}
