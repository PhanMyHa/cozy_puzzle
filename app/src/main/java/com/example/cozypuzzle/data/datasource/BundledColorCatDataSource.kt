package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.colorthecat.model.ColorCatDifficulty
import com.example.cozypuzzle.core.colorthecat.model.ColorCatLevel
import com.example.cozypuzzle.core.colorthecat.model.ColorableRegion
import com.example.cozypuzzle.core.colorthecat.model.DecorativePath
import com.example.cozypuzzle.data.remote.datasource.GameRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledColorCatDataSource @Inject constructor() {

    @Inject
    lateinit var gameRemoteDataSource: GameRemoteDataSource

    private val cozyPastelPalette = listOf(
        0xFFFFB6D2.toInt(), // Pastel Pink
        0xFF83D4F7.toInt(), // Pastel Blue
        0xFFFFE68C.toInt(), // Pastel Yellow
        0xFFA8E6CF.toInt(), // Pastel Mint
        0xFFDCD0FF.toInt(), // Pastel Lavender
        0xFFFFD3B6.toInt(), // Pastel Peach
        0xFFFFAAA5.toInt(), // Soft Coral
        0xFFFFFFFF.toInt()  // White
    )

    private val basicLevels = listOf(
        createLevel1(),
        createLevel2(),
        createLevel3(),
        createLevel4(),
        createLevel5()
    )

    private val activeLevels: List<ColorCatLevel>
        get() {
            val remote = if (::gameRemoteDataSource.isInitialized) {
                gameRemoteDataSource.getColorCatLevels()
            } else {
                null
            }
            return if (!remote.isNullOrEmpty()) remote else basicLevels
        }

    fun getLevels(difficulty: ColorCatDifficulty): List<ColorCatLevel> = when (difficulty) {
        ColorCatDifficulty.BASIC -> activeLevels.filter { it.difficulty == ColorCatDifficulty.BASIC }
        ColorCatDifficulty.INTERMEDIATE -> activeLevels.filter { it.difficulty == ColorCatDifficulty.INTERMEDIATE }
        ColorCatDifficulty.HARD -> activeLevels.filter { it.difficulty == ColorCatDifficulty.HARD }
    }

    fun getAllLevels(): List<ColorCatLevel> = activeLevels

    fun getLevel(levelId: String): ColorCatLevel? =
        activeLevels.firstOrNull { it.id == levelId }

    fun getNextLevel(currentLevelId: String): ColorCatLevel? {
        val index = activeLevels.indexOfFirst { it.id == currentLevelId }
        return if (index != -1 && index + 1 < activeLevels.size) activeLevels[index + 1] else null
    }

    private fun createLevel1(): ColorCatLevel {
        return ColorCatLevel(
            id = "color_cat_1",
            number = 1,
            title = "Level 1",
            difficulty = ColorCatDifficulty.BASIC,
            viewBoxWidth = 300f,
            viewBoxHeight = 300f,
            paletteColors = cozyPastelPalette,
            thumbnailRes = R.drawable.img_puzzle_cute_cat,
            regions = listOf(
                ColorableRegion(
                    id = "ear_left",
                    name = "Left Ear",
                    pathData = "M 85 105 L 55 45 Q 90 50 115 85 Z"
                ),
                ColorableRegion(
                    id = "ear_right",
                    name = "Right Ear",
                    pathData = "M 185 85 Q 210 50 245 45 L 215 105 Z"
                ),
                ColorableRegion(
                    id = "ear_inner_left",
                    name = "Inner Left Ear",
                    pathData = "M 82 85 L 68 55 Q 88 58 102 75 Z"
                ),
                ColorableRegion(
                    id = "ear_inner_right",
                    name = "Inner Right Ear",
                    pathData = "M 198 75 Q 212 58 232 55 L 218 85 Z"
                ),
                ColorableRegion(
                    id = "face",
                    name = "Face",
                    pathData = "M 90 100 C 60 120 60 170 100 185 C 130 195 170 195 200 185 C 240 170 240 120 210 100 C 190 85 110 85 90 100 Z"
                ),
                ColorableRegion(
                    id = "body",
                    name = "Body",
                    pathData = "M 100 180 C 70 200 70 260 105 270 L 195 270 C 230 260 230 200 200 180 Z"
                ),
                ColorableRegion(
                    id = "belly",
                    name = "Belly",
                    pathData = "M 125 195 C 110 215 110 250 130 265 L 170 265 C 190 250 190 215 175 195 Z"
                ),
                ColorableRegion(
                    id = "tail",
                    name = "Tail",
                    pathData = "M 215 250 C 255 245 275 210 265 180 C 255 155 240 165 245 185 C 250 205 235 230 210 235 Z"
                ),
                ColorableRegion(
                    id = "bowtie",
                    name = "Bowtie",
                    pathData = "M 150 185 L 120 172 L 120 198 Z M 150 185 L 180 172 L 180 198 Z"
                ),
                ColorableRegion(
                    id = "bowtie_knot",
                    name = "Bowtie Knot",
                    pathData = "M 143 180 C 143 175 157 175 157 180 C 157 190 143 190 143 180 Z"
                )
            ),
            decorativePaths = listOf(
                DecorativePath("M 70 145 L 30 140 M 70 155 L 25 158"), // Whiskers left
                DecorativePath("M 230 145 L 270 140 M 230 155 L 275 158"), // Whiskers right
                DecorativePath("M 105 135 Q 115 145 125 135 M 175 135 Q 185 145 195 135"), // Eyes
                DecorativePath("M 146 148 L 154 148 L 150 153 Z M 150 153 Q 145 160 140 158 M 150 153 Q 155 160 160 158") // Nose & mouth
            )
        )
    }

    private fun createLevel2(): ColorCatLevel {
        return ColorCatLevel(
            id = "color_cat_2",
            number = 2,
            title = "Level 2",
            difficulty = ColorCatDifficulty.BASIC,
            viewBoxWidth = 300f,
            viewBoxHeight = 300f,
            paletteColors = cozyPastelPalette,
            thumbnailRes = R.drawable.img_puzzle_little_garden,
            regions = listOf(
                ColorableRegion(
                    id = "ear_left",
                    name = "Left Ear",
                    pathData = "M 80 100 L 55 45 Q 90 50 110 80 Z"
                ),
                ColorableRegion(
                    id = "ear_right",
                    name = "Right Ear",
                    pathData = "M 190 80 Q 210 50 245 45 L 220 100 Z"
                ),
                ColorableRegion(
                    id = "flower_petals",
                    name = "Flower Petals",
                    pathData = "M 90 60 C 80 40 100 30 110 45 C 120 30 140 40 130 60 C 145 70 135 90 115 85 C 95 90 85 70 90 60 Z"
                ),
                ColorableRegion(
                    id = "flower_center",
                    name = "Flower Center",
                    pathData = "M 105 60 C 105 54 115 54 115 60 C 115 66 105 66 105 60 Z"
                ),
                ColorableRegion(
                    id = "face",
                    name = "Face",
                    pathData = "M 90 100 C 55 125 55 175 95 190 C 130 200 170 200 205 190 C 245 175 245 125 210 100 C 190 85 110 85 90 100 Z"
                ),
                ColorableRegion(
                    id = "collar",
                    name = "Collar",
                    pathData = "M 105 188 C 130 198 170 198 195 188 L 192 198 C 170 208 130 208 108 198 Z"
                ),
                ColorableRegion(
                    id = "bell",
                    name = "Collar Bell",
                    pathData = "M 144 205 C 144 199 156 199 156 205 C 156 213 144 213 144 205 Z"
                ),
                ColorableRegion(
                    id = "body",
                    name = "Body",
                    pathData = "M 108 198 C 75 220 75 265 110 272 L 190 272 C 225 265 225 220 192 198 Z"
                ),
                ColorableRegion(
                    id = "tail",
                    name = "Tail",
                    pathData = "M 85 250 C 45 245 25 210 35 180 C 45 155 60 165 55 185 C 50 205 65 230 90 235 Z"
                )
            ),
            decorativePaths = listOf(
                DecorativePath("M 70 148 L 30 143 M 70 158 L 25 161"),
                DecorativePath("M 230 148 L 270 143 M 230 158 L 275 161"),
                DecorativePath("M 105 138 Q 115 148 125 138 M 175 138 Q 185 148 195 138"),
                DecorativePath("M 146 150 L 154 150 L 150 155 Z M 150 155 Q 145 162 140 160 M 150 155 Q 155 162 160 160")
            )
        )
    }

    private fun createLevel3(): ColorCatLevel {
        return ColorCatLevel(
            id = "color_cat_3",
            number = 3,
            title = "Level 3",
            difficulty = ColorCatDifficulty.BASIC,
            viewBoxWidth = 300f,
            viewBoxHeight = 300f,
            paletteColors = cozyPastelPalette,
            thumbnailRes = R.drawable.img_puzzle_cozy_room,
            regions = listOf(
                ColorableRegion(
                    id = "party_hat",
                    name = "Party Hat",
                    pathData = "M 125 75 L 150 25 L 175 75 Z"
                ),
                ColorableRegion(
                    id = "pompom",
                    name = "Hat Pom-pom",
                    pathData = "M 143 22 C 143 16 157 16 157 22 C 157 28 143 28 143 22 Z"
                ),
                ColorableRegion(
                    id = "ear_left",
                    name = "Left Ear",
                    pathData = "M 95 105 L 75 50 Q 105 60 125 80 Z"
                ),
                ColorableRegion(
                    id = "ear_right",
                    name = "Right Ear",
                    pathData = "M 175 80 Q 195 60 225 50 L 205 105 Z"
                ),
                ColorableRegion(
                    id = "face",
                    name = "Face",
                    pathData = "M 95 105 C 65 125 65 175 100 190 C 130 200 170 200 200 190 C 235 175 235 125 205 105 C 185 90 115 90 95 105 Z"
                ),
                ColorableRegion(
                    id = "body",
                    name = "Body",
                    pathData = "M 105 190 C 75 210 75 260 110 270 L 190 270 C 225 260 225 210 195 190 Z"
                ),
                ColorableRegion(
                    id = "balloon",
                    name = "Party Balloon",
                    pathData = "M 225 130 C 205 110 205 80 225 65 C 245 50 275 60 275 85 C 275 115 245 135 225 130 Z"
                ),
                ColorableRegion(
                    id = "tail",
                    name = "Tail",
                    pathData = "M 195 250 C 235 245 255 210 245 180 C 235 155 220 165 225 185 C 230 205 215 230 190 235 Z"
                )
            ),
            decorativePaths = listOf(
                DecorativePath("M 225 130 L 215 160 L 210 210"), // Balloon string
                DecorativePath("M 70 148 L 30 143 M 70 158 L 25 161"),
                DecorativePath("M 230 148 L 270 143 M 230 158 L 275 161"),
                DecorativePath("M 110 140 C 110 135 120 135 120 140 C 120 145 110 145 110 140 Z"), // Eye left dot
                DecorativePath("M 180 140 C 180 135 190 135 190 140 C 190 145 180 145 180 140 Z"), // Eye right dot
                DecorativePath("M 146 150 L 154 150 L 150 155 Z M 150 155 Q 145 162 140 160 M 150 155 Q 155 162 160 160")
            )
        )
    }

    private fun createLevel4(): ColorCatLevel {
        // Matches the design from the concept mockup screen (Sunhat Sailor Kitty with flower!)
        return ColorCatLevel(
            id = "color_cat_4",
            number = 4,
            title = "Level 4",
            difficulty = ColorCatDifficulty.BASIC,
            viewBoxWidth = 300f,
            viewBoxHeight = 300f,
            paletteColors = cozyPastelPalette,
            thumbnailRes = R.drawable.img_puzzle_animals,
            regions = listOf(
                ColorableRegion(
                    id = "hat_crown",
                    name = "Hat Top",
                    pathData = "M 105 75 C 105 40 195 40 195 75 Z"
                ),
                ColorableRegion(
                    id = "hat_brim",
                    name = "Hat Brim",
                    pathData = "M 70 85 C 95 65 205 65 230 85 C 220 95 80 95 70 85 Z"
                ),
                ColorableRegion(
                    id = "flower_on_hat",
                    name = "Hat Flower",
                    pathData = "M 180 65 C 175 55 190 50 195 60 C 205 50 215 60 210 70 C 215 80 200 85 195 75 C 185 85 175 75 180 65 Z"
                ),
                ColorableRegion(
                    id = "ear_left",
                    name = "Left Ear",
                    pathData = "M 80 100 L 60 65 L 85 80 Z"
                ),
                ColorableRegion(
                    id = "ear_right",
                    name = "Right Ear",
                    pathData = "M 215 80 L 240 65 L 220 100 Z"
                ),
                ColorableRegion(
                    id = "face",
                    name = "Face",
                    pathData = "M 85 95 C 60 120 60 170 100 185 C 130 195 170 195 200 185 C 240 170 240 120 215 95 C 190 90 110 90 85 95 Z"
                ),
                ColorableRegion(
                    id = "ribbon_tie",
                    name = "Ribbon Tie",
                    pathData = "M 150 185 L 125 178 L 125 196 Z M 150 185 L 175 178 L 175 196 Z"
                ),
                ColorableRegion(
                    id = "body",
                    name = "Body",
                    pathData = "M 100 185 C 75 205 75 255 110 268 L 190 268 C 225 255 225 205 200 185 Z"
                ),
                ColorableRegion(
                    id = "tail",
                    name = "Tail",
                    pathData = "M 210 250 C 250 245 270 210 260 180 C 250 155 235 165 240 185 C 245 205 230 230 205 235 Z"
                ),
                ColorableRegion(
                    id = "garden_flower",
                    name = "Garden Flower",
                    pathData = "M 40 250 C 30 235 45 225 55 235 C 65 225 80 235 70 250 C 80 265 65 275 55 265 C 45 275 30 265 40 250 Z"
                )
            ),
            decorativePaths = listOf(
                DecorativePath("M 50 250 C 50 247 60 247 60 250 C 60 253 50 253 50 250 Z"), // Flower center
                DecorativePath("M 70 148 L 30 143 M 70 158 L 25 161"),
                DecorativePath("M 230 148 L 270 143 M 230 158 L 275 161"),
                DecorativePath("M 112 140 C 112 135 122 135 122 140 C 122 145 112 145 112 140 Z"),
                DecorativePath("M 178 140 C 178 135 188 135 188 140 C 188 145 178 145 178 140 Z"),
                DecorativePath("M 146 150 L 154 150 L 150 155 Z M 150 155 Q 145 162 140 160 M 150 155 Q 155 162 160 160")
            )
        )
    }

    private fun createLevel5(): ColorCatLevel {
        return ColorCatLevel(
            id = "color_cat_5",
            number = 5,
            title = "Level 5",
            difficulty = ColorCatDifficulty.BASIC,
            viewBoxWidth = 300f,
            viewBoxHeight = 300f,
            paletteColors = cozyPastelPalette,
            thumbnailRes = R.drawable.img_puzzle_picnic,
            regions = listOf(
                ColorableRegion(
                    id = "ear_left",
                    name = "Left Ear",
                    pathData = "M 85 105 L 60 45 Q 95 50 115 85 Z"
                ),
                ColorableRegion(
                    id = "ear_right",
                    name = "Right Ear",
                    pathData = "M 185 85 Q 205 50 240 45 L 215 105 Z"
                ),
                ColorableRegion(
                    id = "face",
                    name = "Face",
                    pathData = "M 90 100 C 60 120 60 170 100 185 C 130 195 170 195 200 185 C 240 170 240 120 210 100 C 190 85 110 85 90 100 Z"
                ),
                ColorableRegion(
                    id = "cheek_left",
                    name = "Left Blush",
                    pathData = "M 85 150 C 85 142 100 142 100 150 C 100 158 85 158 85 150 Z"
                ),
                ColorableRegion(
                    id = "cheek_right",
                    name = "Right Blush",
                    pathData = "M 200 150 C 200 142 215 142 215 150 C 215 158 200 158 200 150 Z"
                ),
                ColorableRegion(
                    id = "body",
                    name = "Body",
                    pathData = "M 100 180 C 70 200 70 260 105 270 L 195 270 C 230 260 230 200 200 180 Z"
                ),
                ColorableRegion(
                    id = "belly",
                    name = "Belly",
                    pathData = "M 125 195 C 110 215 110 250 130 265 L 170 265 C 190 250 190 215 175 195 Z"
                ),
                ColorableRegion(
                    id = "tail",
                    name = "Tail",
                    pathData = "M 215 250 C 255 245 275 210 265 180 C 255 155 240 165 245 185 C 250 205 235 230 210 235 Z"
                ),
                ColorableRegion(
                    id = "butterfly_left",
                    name = "Butterfly Left Wing",
                    pathData = "M 55 90 C 35 75 35 50 55 55 C 65 60 65 75 55 90 Z"
                ),
                ColorableRegion(
                    id = "butterfly_right",
                    name = "Butterfly Right Wing",
                    pathData = "M 55 90 C 75 75 75 50 55 55 C 45 60 45 75 55 90 Z"
                )
            ),
            decorativePaths = listOf(
                DecorativePath("M 53 60 L 57 85"), // Butterfly body
                DecorativePath("M 55 55 L 48 45 M 55 55 L 62 45"), // Butterfly antennae
                DecorativePath("M 70 145 L 30 140 M 70 155 L 25 158"),
                DecorativePath("M 230 145 L 270 140 M 230 155 L 275 158"),
                DecorativePath("M 105 135 Q 115 145 125 135 M 175 135 Q 185 145 195 135"),
                DecorativePath("M 146 148 L 154 148 L 150 153 Z M 150 153 Q 145 160 140 158 M 150 153 Q 155 160 160 158")
            )
        )
    }

    companion object {
        const val FIRST_LEVEL_ID = "color_cat_1"
    }
}
