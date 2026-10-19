package com.example.cozypuzzle.presentation.mycats.detail

import com.example.cozypuzzle.domain.model.mycats.CatModel

enum class InteractionMode {
    PET,
    PLAY,
    SMILE,
    SLEEP
}

data class CatDetailUiState(
    val cat: CatModel? = null,
    val isOwned: Boolean = false,
    val isFavorite: Boolean = false,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val selectedMode: InteractionMode = InteractionMode.PET,
    val speechReaction: String = "Meow~ 💕"
) {
    val progressPercentage: Int
        get() = if (targetProgress > 0) (currentProgress * 100) / targetProgress else 0
}

sealed class CatDetailEffect {
    object NavigateBack : CatDetailEffect()
    data class PlayBounceReaction(val speech: String) : CatDetailEffect()
}
