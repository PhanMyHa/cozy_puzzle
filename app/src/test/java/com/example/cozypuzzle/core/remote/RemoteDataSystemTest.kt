package com.example.cozypuzzle.core.remote

import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleDifficulty
import com.example.cozypuzzle.core.colorthecat.model.ColorCatDifficulty
import com.example.cozypuzzle.core.findpair.model.FindPairDifficulty
import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import com.example.cozypuzzle.data.remote.client.RemoteConfigNotConfiguredException
import com.example.cozypuzzle.data.remote.client.RemoteJsonClient
import com.example.cozypuzzle.data.remote.dto.CatDto
import com.example.cozypuzzle.data.remote.dto.CatPuzzleLevelDto
import com.example.cozypuzzle.data.remote.dto.CatUnlockConditionDto
import com.example.cozypuzzle.data.remote.dto.ColorCatLevelDto
import com.example.cozypuzzle.data.remote.dto.ColorableRegionDto
import com.example.cozypuzzle.data.remote.dto.DecorativePathDto
import com.example.cozypuzzle.data.remote.dto.FindPairLevelDto
import com.example.cozypuzzle.data.remote.dto.SlideCatLevelDto
import com.example.cozypuzzle.data.remote.mapper.RemoteDataMappers
import com.example.cozypuzzle.data.remote.mapper.RemoteDataMappers.toDomain
import com.example.cozypuzzle.data.remote.parser.RemoteDataParseException
import com.example.cozypuzzle.data.remote.parser.RemoteDataParsers
import com.example.cozypuzzle.domain.model.mycats.CatRarity
import com.example.cozypuzzle.domain.model.mycats.CatTheme
import com.example.cozypuzzle.domain.model.mycats.CatUnlockCondition
import com.example.cozypuzzle.domain.model.mycats.UnlockGameMode
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteDataSystemTest {

    @Before
    fun setUp() {
        RemoteDataConfig.resetToDefaults()
    }

    @After
    fun tearDown() {
        RemoteDataConfig.resetToDefaults()
    }

    // --- Configuration Tests ---

    @Test
    fun `placeholder base URL is detected correctly`() {
        assertTrue(RemoteDataConfig.isPlaceholderConfigured())

        RemoteDataConfig.rawBaseUrl = "https://raw.githubusercontent.com/mycatfan/cozy-puzzle-data/main/"
        assertFalse(RemoteDataConfig.isPlaceholderConfigured())

        RemoteDataConfig.rawBaseUrl = "http://insecure.example.com/"
        assertTrue(RemoteDataConfig.isPlaceholderConfigured())
    }

    @Test
    fun `resolveUrl builds correct paths with or without leading slashes`() {
        RemoteDataConfig.rawBaseUrl = "https://raw.githubusercontent.com/user/repo/main/"
        assertEquals(
            "https://raw.githubusercontent.com/user/repo/main/manifest.json",
            RemoteDataConfig.resolveUrl("manifest.json")
        )
        assertEquals(
            "https://raw.githubusercontent.com/user/repo/main/levels/cat_puzzle.json",
            RemoteDataConfig.resolveUrl("/levels/cat_puzzle.json")
        )
    }

    // --- Parser Tests ---

    @Test
    fun `parseManifest extracts fields and file map`() {
        val json = """
            {
              "schema_version": 1,
              "data_version": 42,
              "app_min_version": "1.0",
              "updated_at": "2026-10-18T00:00:00Z",
              "files": {
                "cat_puzzle": "levels/cat_puzzle.json",
                "cats": "collections/cats.json"
              }
            }
        """.trimIndent()

        val manifest = RemoteDataParsers.parseManifest(json)
        assertEquals(1, manifest.schemaVersion)
        assertEquals(42, manifest.dataVersion)
        assertEquals("1.0", manifest.appMinVersion)
        assertEquals("levels/cat_puzzle.json", manifest.files["cat_puzzle"])
        assertEquals("collections/cats.json", manifest.files["cats"])
    }

    @Test(expected = RemoteDataParseException::class)
    fun `parseManifest rejects invalid schema version`() {
        val json = """{ "data_version": 1 }"""
        RemoteDataParsers.parseManifest(json)
    }

    @Test(expected = RemoteDataParseException::class)
    fun `parseManifest rejects malformed JSON`() {
        RemoteDataParsers.parseManifest("{ not-valid-json }")
    }

    @Test
    fun `parseGameConfig extracts balance values`() {
        val json = """
            {
              "schema_version": 1,
              "default_grid_size": 4,
              "daily_reward_stars": 20,
              "max_hints_per_level": 5,
              "color_cat_default_palette": ["#FFB6D2", "#83D4F7"]
            }
        """.trimIndent()

        val config = RemoteDataParsers.parseGameConfig(json)
        assertEquals(4, config.defaultGridSize)
        assertEquals(20, config.dailyRewardStars)
        assertEquals(5, config.maxHintsPerLevel)
        assertEquals(listOf("#FFB6D2", "#83D4F7"), config.colorCatDefaultPalette)
    }

    @Test
    fun `parseCatPuzzleLevels parses level items accurately`() {
        val json = """
            {
              "schema_version": 1,
              "levels": [
                {
                  "id": "cat_1",
                  "number": 1,
                  "title": "Level 1",
                  "difficulty": "BASIC",
                  "image_asset": "img_puzzle_cute_cat",
                  "rows": 3,
                  "columns": 3
                }
              ]
            }
        """.trimIndent()

        val levels = RemoteDataParsers.parseCatPuzzleLevels(json)
        assertEquals(1, levels.size)
        val l = levels[0]
        assertEquals("cat_1", l.id)
        assertEquals(1, l.number)
        assertEquals("Level 1", l.title)
        assertEquals("BASIC", l.difficulty)
        assertEquals("img_puzzle_cute_cat", l.imageAsset)
    }

    @Test
    fun `parseColorCatLevels parses SVG regions and decorative paths`() {
        val json = """
            {
              "schema_version": 1,
              "levels": [
                {
                  "id": "color_1",
                  "number": 1,
                  "title": "Color 1",
                  "difficulty": "BASIC",
                  "view_box_width": 300.0,
                  "view_box_height": 300.0,
                  "palette": ["#FFB6D2"],
                  "regions": [
                    { "id": "ear", "name": "Ear", "path_data": "M 0 0 L 10 10 Z" }
                  ],
                  "decorative_paths": [
                    { "path_data": "M 5 5 L 8 8", "stroke_color": "#1E3A5F", "stroke_width": 2.0 }
                  ]
                }
              ]
            }
        """.trimIndent()

        val levels = RemoteDataParsers.parseColorCatLevels(json)
        assertEquals(1, levels.size)
        assertEquals("color_1", levels[0].id)
        assertEquals(1, levels[0].regions.size)
        assertEquals("M 0 0 L 10 10 Z", levels[0].regions[0].pathData)
        assertEquals(1, levels[0].decorativePaths.size)
    }

    @Test
    fun `parseSlideCatLevels parses grid and move limits`() {
        val json = """
            {
              "schema_version": 1,
              "levels": [
                {
                  "id": "slide_1",
                  "number": 1,
                  "title": "Slide 1",
                  "difficulty": "EASY",
                  "grid_size": 3,
                  "image_asset": "img_puzzle_cute_cat",
                  "shuffle_moves": 15,
                  "three_star_moves": 20,
                  "two_star_moves": 35
                }
              ]
            }
        """.trimIndent()

        val levels = RemoteDataParsers.parseSlideCatLevels(json)
        assertEquals(1, levels.size)
        assertEquals(3, levels[0].gridSize)
        assertEquals(15, levels[0].shuffleMoves)
        assertEquals(20, levels[0].threeStarMoves)
    }

    @Test
    fun `parseFindPairLevels parses card images and limits`() {
        val json = """
            {
              "schema_version": 1,
              "levels": [
                {
                  "id": "pair_1",
                  "number": 1,
                  "title": "Pair 1",
                  "difficulty": "EASY",
                  "grid_rows": 4,
                  "grid_columns": 3,
                  "time_limit_seconds": 120,
                  "three_star_max_flips": 16,
                  "two_star_max_flips": 24,
                  "images": ["img_puzzle_cute_cat", "img_puzzle_cozy_room"]
                }
              ]
            }
        """.trimIndent()

        val levels = RemoteDataParsers.parseFindPairLevels(json)
        assertEquals(1, levels.size)
        assertEquals(4, levels[0].gridRows)
        assertEquals(3, levels[0].gridColumns)
        assertEquals(2, levels[0].images.size)
    }

    @Test
    fun `parseCats parses unlock conditions properly`() {
        val json = """
            {
              "schema_version": 1,
              "cats": [
                {
                  "id": "cat_milo",
                  "name": "Milo",
                  "rarity": "COMMON",
                  "theme_id": "SLEEPY",
                  "description": "Cute sleepy cat",
                  "image_asset": "img_cat_milo",
                  "unlock_condition": { "type": "DEFAULT_UNLOCKED" }
                },
                {
                  "id": "cat_luna",
                  "name": "Luna",
                  "rarity": "RARE",
                  "theme_id": "CHEF",
                  "description": "Chef cat",
                  "image_asset": "img_cat_luna",
                  "unlock_condition": {
                    "type": "COMPLETE_LEVEL",
                    "mode": "CAT_PUZZLE",
                    "level_number": 1,
                    "level_id": "cat_1"
                  }
                }
              ]
            }
        """.trimIndent()

        val cats = RemoteDataParsers.parseCats(json)
        assertEquals(2, cats.size)
        assertEquals("DEFAULT_UNLOCKED", cats[0].unlockCondition.type)
        assertEquals("COMPLETE_LEVEL", cats[1].unlockCondition.type)
        assertEquals("CAT_PUZZLE", cats[1].unlockCondition.mode)
        assertEquals(1, cats[1].unlockCondition.levelNumber)
    }

    // --- Mapper Tests ---

    @Test
    fun `parseColorSafe parses 6-digit and 8-digit hex without Android Color dependency`() {
        val pink = RemoteDataMappers.parseColorSafe("#FFB6D2", 0)
        assertEquals(0xFFFFB6D2.toInt(), pink)

        val withAlpha = RemoteDataMappers.parseColorSafe("#80FFB6D2", 0)
        assertEquals(0x80FFB6D2.toInt(), withAlpha)

        val fallback = RemoteDataMappers.parseColorSafe("invalid-hex", 12345)
        assertEquals(12345, fallback)
    }

    @Test
    fun `resolveDrawable safely resolves known drawables or uses fallback`() {
        val catRes = RemoteDataMappers.resolveDrawable("img_cat_milo", R.drawable.img_puzzle_cute_cat)
        assertEquals(R.drawable.img_cat_milo, catRes)

        val fallbackRes = RemoteDataMappers.resolveDrawable("unknown_asset", R.drawable.img_puzzle_cute_cat)
        assertEquals(R.drawable.img_puzzle_cute_cat, fallbackRes)
    }

    @Test
    fun `CatPuzzleLevelDto toDomain maps accurately`() {
        val dto = CatPuzzleLevelDto(
            id = "cat_1",
            number = 1,
            title = "Level 1",
            difficulty = "BASIC",
            imageAsset = "img_puzzle_cute_cat",
            rows = 3,
            columns = 3
        )
        val domain = dto.toDomain()
        assertEquals("cat_1", domain.id)
        assertEquals(CatPuzzleDifficulty.BASIC, domain.difficulty)
        assertEquals(R.drawable.img_puzzle_cute_cat, domain.imageRes)
        assertEquals(3, domain.gridSize)
    }

    @Test
    fun `ColorCatLevelDto toDomain maps accurately`() {
        val dto = ColorCatLevelDto(
            id = "color_1",
            number = 1,
            title = "Level 1",
            difficulty = "BASIC",
            palette = listOf("#FFB6D2", "#83D4F7"),
            regions = listOf(ColorableRegionDto("face", "Face", "M 0 0 Z")),
            decorativePaths = listOf(DecorativePathDto("M 1 1", strokeColor = "#1E3A5F"))
        )
        val domain = dto.toDomain()
        assertEquals("color_1", domain.id)
        assertEquals(ColorCatDifficulty.BASIC, domain.difficulty)
        assertEquals(2, domain.paletteColors.size)
        assertEquals(1, domain.regions.size)
        assertEquals("face", domain.regions[0].id)
    }

    @Test
    fun `SlideCatLevelDto toDomain maps accurately`() {
        val dto = SlideCatLevelDto(
            id = "slide_1",
            number = 1,
            title = "Slide 1",
            difficulty = "HARD",
            gridSize = 4,
            imageAsset = "img_puzzle_cute_cat",
            shuffleMoves = 50,
            threeStarMoves = 70,
            twoStarMoves = 100
        )
        val domain = dto.toDomain()
        assertEquals("slide_1", domain.id)
        assertEquals(SlideCatDifficulty.HARD, domain.difficulty)
        assertEquals(4, domain.gridSize)
        assertEquals(50, domain.shuffleMoves)
    }

    @Test
    fun `FindPairLevelDto toDomain maps accurately`() {
        val dto = FindPairLevelDto(
            id = "pair_1",
            number = 1,
            title = "Pair 1",
            difficulty = "MEDIUM",
            gridRows = 4,
            gridColumns = 4,
            timeLimitSeconds = 180,
            threeStarMaxFlips = 24,
            twoStarMaxFlips = 36,
            images = listOf("img_puzzle_cute_cat", "img_puzzle_cozy_room")
        )
        val domain = dto.toDomain()
        assertEquals("pair_1", domain.id)
        assertEquals(FindPairDifficulty.MEDIUM, domain.difficulty)
        assertEquals(4, domain.rows)
        assertEquals(4, domain.columns)
        assertEquals(2, domain.images.size)
    }

    @Test
    fun `CatDto toDomain maps accurately`() {
        val dto = CatDto(
            id = "cat_luna",
            name = "Luna",
            rarity = "EPIC",
            themeId = "CHEF",
            description = "Chef cat",
            imageAsset = "img_cat_luna",
            unlockCondition = CatUnlockConditionDto(
                type = "COMPLETE_LEVEL",
                mode = "CAT_PUZZLE",
                levelNumber = 1,
                levelId = "cat_1"
            ),
            unlockHint = "Complete Level 1"
        )
        val domain = dto.toDomain()
        assertEquals("cat_luna", domain.id)
        assertEquals(CatRarity.EPIC, domain.rarity)
        assertEquals(CatTheme.CHEF, domain.theme)
        assertEquals(R.drawable.img_cat_luna, domain.imageRes)
        assertTrue(domain.unlockCondition is CatUnlockCondition.CompleteSpecificLevel)
        val cond = domain.unlockCondition as CatUnlockCondition.CompleteSpecificLevel
        assertEquals(UnlockGameMode.CAT_PUZZLE, cond.gameMode)
        assertEquals("cat_1", cond.levelId)
    }

    // --- Remote Client Safety Tests ---

    @Test
    fun `RemoteJsonClient detects placeholder URL and bypasses network call`() = runBlocking {
        val client = RemoteJsonClient()
        val result = client.fetchJson(RemoteDataConfig.rawBaseUrl)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RemoteConfigNotConfiguredException)
    }

    @Test
    fun `RemoteJsonClient rejects non-HTTPS URL`() = runBlocking {
        val client = RemoteJsonClient()
        RemoteDataConfig.rawBaseUrl = "http://raw.githubusercontent.com/user/repo/main/"
        val result = client.fetchJson("http://raw.githubusercontent.com/user/repo/main/manifest.json")
        assertTrue(result.isFailure)
    }
}
