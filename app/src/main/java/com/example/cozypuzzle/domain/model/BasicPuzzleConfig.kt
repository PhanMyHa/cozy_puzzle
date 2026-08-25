package com.example.cozypuzzle.domain.model

data class BasicPuzzleConfig(
    val enabled: Boolean,
    val availableSizes: List<Int>,
    val defaultSize: Int,
    val categories: List<String>,
    val allowRotate: Boolean,
    val allowSwap: Boolean,
    val allowDrag: Boolean,
    val maxHints: Int
) {
    companion object {
        val DEFAULT = BasicPuzzleConfig(
            enabled = true,
            availableSizes = listOf(3, 4, 5, 6),
            defaultSize = 4,
            categories = listOf("animals", "nature", "food", "space"),
            allowRotate = true,
            allowSwap = true,
            allowDrag = true,
            maxHints = 3
        )
    }
}
