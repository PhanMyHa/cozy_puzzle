package com.example.cozypuzzle.domain.model.mycats

enum class UnlockGameMode {
    CAT_PUZZLE,
    COLOR_THE_CAT,
    SLIDE_THE_CAT,
    FIND_THE_PAIR
}

sealed class CatUnlockCondition {
    object DefaultUnlocked : CatUnlockCondition()

    data class CompleteSpecificLevel(
        val gameMode: UnlockGameMode,
        val levelNumber: Int,
        val levelId: String
    ) : CatUnlockCondition()

    data class EarnTotalStars(
        val requiredStars: Int
    ) : CatUnlockCondition()

    data class CompleteTotalLevels(
        val requiredLevels: Int
    ) : CatUnlockCondition()
}
