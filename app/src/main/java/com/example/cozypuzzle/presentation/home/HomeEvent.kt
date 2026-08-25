package com.example.cozypuzzle.presentation.home

import com.example.cozypuzzle.core.puzzle.model.PuzzleMode

sealed interface HomeEvent {

    data object DailyPuzzleClicked : HomeEvent

    data class GameModeClicked(
        val mode: PuzzleMode
    ) : HomeEvent
}

sealed interface HomeEffect {

    data object FeatureComingSoon : HomeEffect

    data object NavigateToBasicPuzzles : HomeEffect

    data object NavigateToCatLevels : HomeEffect
}
