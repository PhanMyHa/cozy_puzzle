package com.example.cozypuzzle.presentation.garden

sealed interface GardenEvent {

    data object ToggleEditMode : GardenEvent

    data class SelectItem(
        val id: String
    ) : GardenEvent

    data class MoveItem(
        val id: String,
        val x: Float,
        val y: Float
    ) : GardenEvent

    data class RotateItem(
        val id: String,
        val rotation: Float
    ) : GardenEvent

    data class RemoveItem(
        val id: String
    ) : GardenEvent
}
