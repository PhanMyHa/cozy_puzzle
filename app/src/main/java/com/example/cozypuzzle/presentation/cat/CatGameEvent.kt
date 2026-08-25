package com.example.cozypuzzle.presentation.cat

sealed interface CatGameEvent {

    data class CellTapped(val cell: Int) : CatGameEvent

    data class RotateSelected(val cell: Int) : CatGameEvent

    data object Undo : CatGameEvent

    data object Redo : CatGameEvent

    data object Hint : CatGameEvent

    data object Walk : CatGameEvent

    data object Pause : CatGameEvent

    data object Resume : CatGameEvent

    data object Restart : CatGameEvent

    data object Back : CatGameEvent
}

sealed interface CatGameEffect {

    data object NavigateBackToSelection : CatGameEffect

    data object NavigateHome : CatGameEffect
}
