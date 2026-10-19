package com.example.cozypuzzle.data.remote.dto

data class ManifestDto(
    val schemaVersion: Int,
    val dataVersion: Int,
    val appMinVersion: String? = null,
    val updatedAt: String? = null,
    val files: Map<String, String> = emptyMap()
)

data class GameConfigDto(
    val schemaVersion: Int,
    val defaultGridSize: Int = 3,
    val dailyRewardStars: Int = 15,
    val maxHintsPerLevel: Int = 3,
    val catPuzzleDefaultRows: Int = 3,
    val catPuzzleDefaultColumns: Int = 3,
    val findPairDefaultTimeLimit: Int = 120,
    val slideCatDefaultShuffleMoves: Int = 15,
    val colorCatDefaultPalette: List<String> = emptyList()
)

data class CatPuzzleLevelDto(
    val id: String,
    val number: Int,
    val title: String,
    val difficulty: String,
    val imageUrl: String? = null,
    val imageAsset: String? = null,
    val rows: Int = 3,
    val columns: Int = 3
)

data class ColorableRegionDto(
    val id: String,
    val name: String,
    val pathData: String
)

data class DecorativePathDto(
    val pathData: String,
    val strokeColor: String? = null,
    val strokeWidth: Float = 2.5f,
    val fillColor: String? = null
)

data class ColorCatLevelDto(
    val id: String,
    val number: Int,
    val title: String,
    val difficulty: String,
    val viewBoxWidth: Float = 300f,
    val viewBoxHeight: Float = 300f,
    val palette: List<String> = emptyList(),
    val svgUrl: String? = null,
    val thumbnailUrl: String? = null,
    val thumbnailAsset: String? = null,
    val regions: List<ColorableRegionDto> = emptyList(),
    val decorativePaths: List<DecorativePathDto> = emptyList()
)

data class SlideCatLevelDto(
    val id: String,
    val number: Int,
    val title: String,
    val difficulty: String,
    val gridSize: Int = 3,
    val imageUrl: String? = null,
    val imageAsset: String? = null,
    val shuffleMoves: Int = 15,
    val threeStarMoves: Int = 22,
    val twoStarMoves: Int = 38
)

data class FindPairLevelDto(
    val id: String,
    val number: Int,
    val title: String,
    val difficulty: String,
    val gridRows: Int = 4,
    val gridColumns: Int = 3,
    val timeLimitSeconds: Int = 120,
    val threeStarMaxFlips: Int = 16,
    val twoStarMaxFlips: Int = 24,
    val images: List<String> = emptyList(),
    val thumbnailUrl: String? = null,
    val thumbnailAsset: String? = null
)

data class CatUnlockConditionDto(
    val type: String, // "DEFAULT_UNLOCKED", "COMPLETE_LEVEL", "COMPLETE_TOTAL_LEVELS", "EARN_TOTAL_STARS"
    val mode: String? = null,
    val levelNumber: Int? = null,
    val levelId: String? = null,
    val count: Int? = null
)

data class CatDto(
    val id: String,
    val name: String,
    val rarity: String,
    val themeId: String,
    val description: String,
    val imageUrl: String? = null,
    val imageAsset: String? = null,
    val thumbnailUrl: String? = null,
    val unlockCondition: CatUnlockConditionDto = CatUnlockConditionDto(type = "DEFAULT_UNLOCKED"),
    val unlockHint: String = "Available from start"
)
