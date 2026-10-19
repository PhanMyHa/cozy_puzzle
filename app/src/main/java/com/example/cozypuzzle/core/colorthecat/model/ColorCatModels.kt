package com.example.cozypuzzle.core.colorthecat.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

enum class ColorCatDifficulty {
    BASIC,
    INTERMEDIATE,
    HARD
}

data class ColorableRegion(
    val id: String,
    val name: String,
    val pathData: String,
    @ColorInt val defaultColor: Int = 0xFFFFFFFF.toInt(),
    val strokeWidth: Float = 2.5f,
    val isRequired: Boolean = true
)

data class DecorativePath(
    val pathData: String,
    @ColorInt val strokeColor: Int = 0xFF1E3A5F.toInt(),
    val strokeWidth: Float = 2f,
    @ColorInt val fillColor: Int = 0x00000000
)

data class ColorCatLevel(
    val id: String,
    val number: Int,
    val title: String,
    val difficulty: ColorCatDifficulty,
    val viewBoxWidth: Float = 300f,
    val viewBoxHeight: Float = 300f,
    val regions: List<ColorableRegion>,
    val decorativePaths: List<DecorativePath> = emptyList(),
    val paletteColors: List<Int>,
    @DrawableRes val thumbnailRes: Int? = null
)

data class ColoringAction(
    val regionId: String,
    @ColorInt val previousColor: Int,
    @ColorInt val newColor: Int
)

data class ColorCatState(
    val level: ColorCatLevel,
    val regionColors: Map<String, Int> = emptyMap(),
    @ColorInt val selectedColor: Int,
    val undoStack: List<ColoringAction> = emptyList(),
    val hintsRemaining: Int = DEFAULT_HINTS,
    val hintsUsed: Int = 0,
    val highlightedRegionId: String? = null,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedTimeMs: Long = 0L,
    val starsEarned: Int = 0
) {
    val totalRequiredRegions: Int get() = level.regions.count { it.isRequired }
    val coloredRequiredRegionsCount: Int
        get() = level.regions.count { region ->
            region.isRequired && regionColors.containsKey(region.id)
        }

    companion object {
        const val DEFAULT_HINTS = 3
    }
}
