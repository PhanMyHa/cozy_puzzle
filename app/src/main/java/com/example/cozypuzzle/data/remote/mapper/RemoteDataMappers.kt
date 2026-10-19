package com.example.cozypuzzle.data.remote.mapper

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleDifficulty
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleLevel
import com.example.cozypuzzle.core.colorthecat.model.ColorCatDifficulty
import com.example.cozypuzzle.core.colorthecat.model.ColorCatLevel
import com.example.cozypuzzle.core.colorthecat.model.ColorableRegion
import com.example.cozypuzzle.core.colorthecat.model.DecorativePath
import com.example.cozypuzzle.core.findpair.model.FindPairDifficulty
import com.example.cozypuzzle.core.findpair.model.FindPairLevel
import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import com.example.cozypuzzle.core.slidethecat.model.SlideCatLevel
import com.example.cozypuzzle.data.remote.dto.CatDto
import com.example.cozypuzzle.data.remote.dto.CatPuzzleLevelDto
import com.example.cozypuzzle.data.remote.dto.ColorCatLevelDto
import com.example.cozypuzzle.data.remote.dto.FindPairLevelDto
import com.example.cozypuzzle.data.remote.dto.SlideCatLevelDto
import com.example.cozypuzzle.domain.model.mycats.CatModel
import com.example.cozypuzzle.domain.model.mycats.CatRarity
import com.example.cozypuzzle.domain.model.mycats.CatTheme
import com.example.cozypuzzle.domain.model.mycats.CatUnlockCondition
import com.example.cozypuzzle.domain.model.mycats.UnlockGameMode

object RemoteDataMappers {

    private val DRAWABLE_MAP = mapOf(
        // Cats
        "img_cat_milo" to R.drawable.img_cat_milo,
        "img_cat_luna" to R.drawable.img_cat_luna,
        "img_cat_snow" to R.drawable.img_cat_snow,
        "img_cat_oliver" to R.drawable.img_cat_oliver,
        "img_cat_coco" to R.drawable.img_cat_coco,
        "img_cat_waffles" to R.drawable.img_cat_waffles,
        "img_cat_leo" to R.drawable.img_cat_leo,
        "img_cat_nova" to R.drawable.img_cat_nova,
        "img_cat_comet" to R.drawable.img_cat_comet,
        "img_cat_bella" to R.drawable.img_cat_bella,
        "img_cat_clover" to R.drawable.img_cat_clover,
        "img_cat_pudding" to R.drawable.img_cat_pudding,

        // Puzzle images
        "img_puzzle_cute_cat" to R.drawable.img_puzzle_cute_cat,
        "img_puzzle_cozy_room" to R.drawable.img_puzzle_cozy_room,
        "img_puzzle_little_garden" to R.drawable.img_puzzle_little_garden,
        "img_puzzle_animals" to R.drawable.img_puzzle_animals,
        "img_puzzle_picnic" to R.drawable.img_puzzle_picnic,
        "img_puzzle_nature" to R.drawable.img_puzzle_nature,
        "img_puzzle_food" to R.drawable.img_puzzle_food,
        "img_puzzle_frog_pond" to R.drawable.img_puzzle_frog_pond,
        "img_puzzle_space" to R.drawable.img_puzzle_space,
        "img_puzzle_cat_artist" to R.drawable.img_puzzle_cat_artist
    )

    fun resolveDrawable(assetName: String?, fallbackRes: Int): Int {
        if (assetName.isNullOrBlank()) return fallbackRes
        return DRAWABLE_MAP[assetName.trim()] ?: fallbackRes
    }

    /**
     * Safely parse hex string without relying on android.graphics.Color
     * so it runs seamlessly in standard JVM unit tests.
     */
    fun parseColorSafe(hex: String?, defaultColor: Int): Int {
        if (hex.isNullOrBlank()) return defaultColor
        return try {
            val clean = hex.trim().removePrefix("#")
            when (clean.length) {
                6 -> (0xFF000000.toLong() or clean.toLong(16)).toInt()
                8 -> clean.toLong(16).toInt()
                else -> defaultColor
            }
        } catch (e: Exception) {
            defaultColor
        }
    }

    fun CatPuzzleLevelDto.toDomain(): CatPuzzleLevel {
        val diff = when (difficulty.uppercase()) {
            "INTERMEDIATE" -> CatPuzzleDifficulty.INTERMEDIATE
            "HARD" -> CatPuzzleDifficulty.HARD
            else -> CatPuzzleDifficulty.BASIC
        }
        val img = resolveDrawable(imageAsset, R.drawable.img_puzzle_cute_cat)
        val grid = if (rows > 0) rows else 3
        return CatPuzzleLevel(
            id = id,
            number = number,
            title = title,
            difficulty = diff,
            imageRes = img,
            gridSize = grid
        )
    }

    fun ColorCatLevelDto.toDomain(): ColorCatLevel {
        val diff = when (difficulty.uppercase()) {
            "INTERMEDIATE" -> ColorCatDifficulty.INTERMEDIATE
            "HARD" -> ColorCatDifficulty.HARD
            else -> ColorCatDifficulty.BASIC
        }
        val thumb = resolveDrawable(thumbnailAsset, R.drawable.img_puzzle_cute_cat)
        val parsedPalette = palette.map { parseColorSafe(it, 0xFF83D4F7.toInt()) }
            .ifEmpty {
                listOf(
                    0xFFFFB6D2.toInt(),
                    0xFF83D4F7.toInt(),
                    0xFFFFE68C.toInt(),
                    0xFFA8E6CF.toInt(),
                    0xFFDCD0FF.toInt(),
                    0xFFFFD3B6.toInt(),
                    0xFFFFAAA5.toInt(),
                    0xFFFFFFFF.toInt()
                )
            }

        val domainRegions = regions.map {
            ColorableRegion(
                id = it.id,
                name = it.name,
                pathData = it.pathData
            )
        }

        val domainDecorative = decorativePaths.map {
            DecorativePath(
                pathData = it.pathData,
                strokeColor = parseColorSafe(it.strokeColor, 0xFF1E3A5F.toInt()),
                strokeWidth = it.strokeWidth,
                fillColor = parseColorSafe(it.fillColor, 0x00000000)
            )
        }

        return ColorCatLevel(
            id = id,
            number = number,
            title = title,
            difficulty = diff,
            viewBoxWidth = viewBoxWidth,
            viewBoxHeight = viewBoxHeight,
            regions = domainRegions,
            decorativePaths = domainDecorative,
            paletteColors = parsedPalette,
            thumbnailRes = thumb
        )
    }

    fun SlideCatLevelDto.toDomain(): SlideCatLevel {
        val diff = when (difficulty.uppercase()) {
            "MEDIUM" -> SlideCatDifficulty.MEDIUM
            "HARD" -> SlideCatDifficulty.HARD
            else -> SlideCatDifficulty.EASY
        }
        val img = resolveDrawable(imageAsset, R.drawable.img_puzzle_cute_cat)
        return SlideCatLevel(
            id = id,
            number = number,
            title = title,
            difficulty = diff,
            gridSize = if (gridSize >= 3) gridSize else 3,
            imageRes = img,
            thumbnailRes = img,
            shuffleMoves = shuffleMoves,
            threeStarMoves = threeStarMoves,
            twoStarMoves = twoStarMoves
        )
    }

    fun FindPairLevelDto.toDomain(): FindPairLevel {
        val diff = when (difficulty.uppercase()) {
            "MEDIUM" -> FindPairDifficulty.MEDIUM
            "HARD" -> FindPairDifficulty.HARD
            else -> FindPairDifficulty.EASY
        }
        val imgList = images.map { resolveDrawable(it, R.drawable.img_puzzle_cute_cat) }
            .ifEmpty { listOf(R.drawable.img_puzzle_cute_cat) }
        val thumb = resolveDrawable(thumbnailAsset, imgList.first())

        return FindPairLevel(
            id = id,
            number = number,
            title = title,
            difficulty = diff,
            columns = if (gridColumns > 0) gridColumns else 3,
            rows = if (gridRows > 0) gridRows else 4,
            timeLimitSeconds = if (timeLimitSeconds > 0) timeLimitSeconds else 120,
            threeStarMaxFlips = threeStarMaxFlips,
            twoStarMaxFlips = twoStarMaxFlips,
            images = imgList,
            thumbnailRes = thumb
        )
    }

    fun CatDto.toDomain(): CatModel {
        val catRarity = when (rarity.uppercase()) {
            "RARE" -> CatRarity.RARE
            "EPIC" -> CatRarity.EPIC
            "LEGENDARY" -> CatRarity.LEGENDARY
            else -> CatRarity.COMMON
        }

        val catTheme = when (themeId.uppercase()) {
            "CHEF" -> CatTheme.CHEF
            "SPACE" -> CatTheme.SPACE
            "GARDEN" -> CatTheme.GARDEN
            else -> CatTheme.SLEEPY
        }

        val condition: CatUnlockCondition = when (unlockCondition.type.uppercase()) {
            "COMPLETE_LEVEL" -> {
                val mode = when (unlockCondition.mode?.uppercase()) {
                    "COLOR_THE_CAT" -> UnlockGameMode.COLOR_THE_CAT
                    "SLIDE_THE_CAT" -> UnlockGameMode.SLIDE_THE_CAT
                    "FIND_THE_PAIR" -> UnlockGameMode.FIND_THE_PAIR
                    else -> UnlockGameMode.CAT_PUZZLE
                }
                CatUnlockCondition.CompleteSpecificLevel(
                    gameMode = mode,
                    levelNumber = unlockCondition.levelNumber ?: 1,
                    levelId = unlockCondition.levelId ?: ""
                )
            }
            "COMPLETE_TOTAL_LEVELS" -> {
                CatUnlockCondition.CompleteTotalLevels(
                    requiredLevels = unlockCondition.count ?: 3
                )
            }
            "EARN_TOTAL_STARS" -> {
                CatUnlockCondition.EarnTotalStars(
                    requiredStars = unlockCondition.count ?: 6
                )
            }
            else -> CatUnlockCondition.DefaultUnlocked
        }

        val img = resolveDrawable(imageAsset, R.drawable.img_cat_milo)

        return CatModel(
            id = id,
            name = name,
            imageRes = img,
            rarity = catRarity,
            theme = catTheme,
            description = description,
            foundIn = unlockHint,
            unlockCondition = condition,
            unlockHint = unlockHint
        )
    }
}
