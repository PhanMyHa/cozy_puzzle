package com.example.cozypuzzle.data.remote.parser

import com.example.cozypuzzle.data.remote.dto.CatDto
import com.example.cozypuzzle.data.remote.dto.CatPuzzleLevelDto
import com.example.cozypuzzle.data.remote.dto.CatUnlockConditionDto
import com.example.cozypuzzle.data.remote.dto.ColorCatLevelDto
import com.example.cozypuzzle.data.remote.dto.ColorableRegionDto
import com.example.cozypuzzle.data.remote.dto.DecorativePathDto
import com.example.cozypuzzle.data.remote.dto.FindPairLevelDto
import com.example.cozypuzzle.data.remote.dto.GameConfigDto
import com.example.cozypuzzle.data.remote.dto.ManifestDto
import com.example.cozypuzzle.data.remote.dto.SlideCatLevelDto
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RemoteDataParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

object RemoteDataParsers {

    @Throws(RemoteDataParseException::class)
    fun parseManifest(jsonString: String): ManifestDto {
        return try {
            val json = JSONObject(jsonString)
            val schemaVersion = json.optInt("schema_version", -1)
            if (schemaVersion <= 0) {
                throw RemoteDataParseException("Invalid or missing schema_version in manifest")
            }
            val dataVersion = json.optInt("data_version", 1)
            val appMinVersion = json.optString("app_min_version", "").takeIf { it.isNotEmpty() }
            val updatedAt = json.optString("updated_at", "").takeIf { it.isNotEmpty() }

            val filesMap = mutableMapOf<String, String>()
            val filesObj = json.optJSONObject("files")
            if (filesObj != null) {
                val keys = filesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = filesObj.optString(key, "")
                    if (value.isNotEmpty()) {
                        filesMap[key] = value
                    }
                }
            }

            ManifestDto(
                schemaVersion = schemaVersion,
                dataVersion = dataVersion,
                appMinVersion = appMinVersion,
                updatedAt = updatedAt,
                files = filesMap
            )
        } catch (e: JSONException) {
            throw RemoteDataParseException("Failed to parse manifest JSON: ${e.message}", e)
        }
    }

    @Throws(RemoteDataParseException::class)
    fun parseGameConfig(jsonString: String): GameConfigDto {
        return try {
            val json = JSONObject(jsonString)
            val schemaVersion = json.optInt("schema_version", 1)

            val paletteList = mutableListOf<String>()
            val paletteArray = json.optJSONArray("color_cat_default_palette")
            if (paletteArray != null) {
                for (i in 0 until paletteArray.length()) {
                    val colorHex = paletteArray.optString(i, "").trim()
                    if (colorHex.isNotEmpty()) {
                        paletteList.add(colorHex)
                    }
                }
            }

            GameConfigDto(
                schemaVersion = schemaVersion,
                defaultGridSize = json.optInt("default_grid_size", 3),
                dailyRewardStars = json.optInt("daily_reward_stars", 15),
                maxHintsPerLevel = json.optInt("max_hints_per_level", 3),
                catPuzzleDefaultRows = json.optInt("cat_puzzle_default_rows", 3),
                catPuzzleDefaultColumns = json.optInt("cat_puzzle_default_columns", 3),
                findPairDefaultTimeLimit = json.optInt("find_pair_default_time_limit", 120),
                slideCatDefaultShuffleMoves = json.optInt("slide_cat_default_shuffle_moves", 15),
                colorCatDefaultPalette = paletteList
            )
        } catch (e: JSONException) {
            throw RemoteDataParseException("Failed to parse game_config JSON: ${e.message}", e)
        }
    }

    @Throws(RemoteDataParseException::class)
    fun parseCatPuzzleLevels(jsonString: String): List<CatPuzzleLevelDto> {
        return try {
            val json = JSONObject(jsonString)
            val levelsArray = json.optJSONArray("levels")
                ?: throw RemoteDataParseException("Missing 'levels' array in cat_puzzle JSON")

            val levels = mutableListOf<CatPuzzleLevelDto>()
            for (i in 0 until levelsArray.length()) {
                val item = levelsArray.getJSONObject(i)
                val id = item.optString("id", "").trim()
                if (id.isEmpty()) throw RemoteDataParseException("Cat puzzle level item missing 'id' at index $i")

                levels.add(
                    CatPuzzleLevelDto(
                        id = id,
                        number = item.optInt("number", i + 1),
                        title = item.optString("title", "Level ${i + 1}"),
                        difficulty = item.optString("difficulty", "BASIC").uppercase(),
                        imageUrl = item.optString("image_url", "").takeIf { it.isNotEmpty() },
                        imageAsset = item.optString("image_asset", "").takeIf { it.isNotEmpty() },
                        rows = item.optInt("rows", 3),
                        columns = item.optInt("columns", 3)
                    )
                )
            }
            levels
        } catch (e: JSONException) {
            throw RemoteDataParseException("Failed to parse cat_puzzle JSON: ${e.message}", e)
        }
    }

    @Throws(RemoteDataParseException::class)
    fun parseColorCatLevels(jsonString: String): List<ColorCatLevelDto> {
        return try {
            val json = JSONObject(jsonString)
            val levelsArray = json.optJSONArray("levels")
                ?: throw RemoteDataParseException("Missing 'levels' array in color_the_cat JSON")

            val levels = mutableListOf<ColorCatLevelDto>()
            for (i in 0 until levelsArray.length()) {
                val item = levelsArray.getJSONObject(i)
                val id = item.optString("id", "").trim()
                if (id.isEmpty()) throw RemoteDataParseException("Color cat level missing 'id' at index $i")

                val paletteList = mutableListOf<String>()
                val paletteArray = item.optJSONArray("palette")
                if (paletteArray != null) {
                    for (p in 0 until paletteArray.length()) {
                        val hex = paletteArray.optString(p, "").trim()
                        if (hex.isNotEmpty()) paletteList.add(hex)
                    }
                }

                val regionsList = mutableListOf<ColorableRegionDto>()
                val regionsArray = item.optJSONArray("regions")
                if (regionsArray != null) {
                    for (r in 0 until regionsArray.length()) {
                        val regionObj = regionsArray.getJSONObject(r)
                        val regId = regionObj.optString("id", "").trim()
                        val pathData = regionObj.optString("path_data", "").trim()
                        if (regId.isNotEmpty() && pathData.isNotEmpty()) {
                            regionsList.add(
                                ColorableRegionDto(
                                    id = regId,
                                    name = regionObj.optString("name", regId),
                                    pathData = pathData
                                )
                            )
                        }
                    }
                }

                val decorativeList = mutableListOf<DecorativePathDto>()
                val decorativeArray = item.optJSONArray("decorative_paths")
                if (decorativeArray != null) {
                    for (d in 0 until decorativeArray.length()) {
                        val decObj = decorativeArray.getJSONObject(d)
                        val pathData = decObj.optString("path_data", "").trim()
                        if (pathData.isNotEmpty()) {
                            decorativeList.add(
                                DecorativePathDto(
                                    pathData = pathData,
                                    strokeColor = decObj.optString("stroke_color", "").takeIf { it.isNotEmpty() },
                                    strokeWidth = decObj.optDouble("stroke_width", 2.5).toFloat(),
                                    fillColor = decObj.optString("fill_color", "").takeIf { it.isNotEmpty() }
                                )
                            )
                        }
                    }
                }

                levels.add(
                    ColorCatLevelDto(
                        id = id,
                        number = item.optInt("number", i + 1),
                        title = item.optString("title", "Level ${i + 1}"),
                        difficulty = item.optString("difficulty", "BASIC").uppercase(),
                        viewBoxWidth = item.optDouble("view_box_width", 300.0).toFloat(),
                        viewBoxHeight = item.optDouble("view_box_height", 300.0).toFloat(),
                        palette = paletteList,
                        svgUrl = item.optString("svg_url", "").takeIf { it.isNotEmpty() },
                        thumbnailUrl = item.optString("thumbnail_url", "").takeIf { it.isNotEmpty() },
                        thumbnailAsset = item.optString("thumbnail_asset", "").takeIf { it.isNotEmpty() },
                        regions = regionsList,
                        decorativePaths = decorativeList
                    )
                )
            }
            levels
        } catch (e: JSONException) {
            throw RemoteDataParseException("Failed to parse color_the_cat JSON: ${e.message}", e)
        }
    }

    @Throws(RemoteDataParseException::class)
    fun parseSlideCatLevels(jsonString: String): List<SlideCatLevelDto> {
        return try {
            val json = JSONObject(jsonString)
            val levelsArray = json.optJSONArray("levels")
                ?: throw RemoteDataParseException("Missing 'levels' array in slide_the_cat JSON")

            val levels = mutableListOf<SlideCatLevelDto>()
            for (i in 0 until levelsArray.length()) {
                val item = levelsArray.getJSONObject(i)
                val id = item.optString("id", "").trim()
                if (id.isEmpty()) throw RemoteDataParseException("Slide cat level missing 'id' at index $i")

                levels.add(
                    SlideCatLevelDto(
                        id = id,
                        number = item.optInt("number", i + 1),
                        title = item.optString("title", "Level ${i + 1}"),
                        difficulty = item.optString("difficulty", "EASY").uppercase(),
                        gridSize = item.optInt("grid_size", 3),
                        imageUrl = item.optString("image_url", "").takeIf { it.isNotEmpty() },
                        imageAsset = item.optString("image_asset", "").takeIf { it.isNotEmpty() },
                        shuffleMoves = item.optInt("shuffle_moves", 15),
                        threeStarMoves = item.optInt("three_star_moves", 22),
                        twoStarMoves = item.optInt("two_star_moves", 38)
                    )
                )
            }
            levels
        } catch (e: JSONException) {
            throw RemoteDataParseException("Failed to parse slide_the_cat JSON: ${e.message}", e)
        }
    }

    @Throws(RemoteDataParseException::class)
    fun parseFindPairLevels(jsonString: String): List<FindPairLevelDto> {
        return try {
            val json = JSONObject(jsonString)
            val levelsArray = json.optJSONArray("levels")
                ?: throw RemoteDataParseException("Missing 'levels' array in find_the_pair JSON")

            val levels = mutableListOf<FindPairLevelDto>()
            for (i in 0 until levelsArray.length()) {
                val item = levelsArray.getJSONObject(i)
                val id = item.optString("id", "").trim()
                if (id.isEmpty()) throw RemoteDataParseException("Find pair level missing 'id' at index $i")

                val imagesList = mutableListOf<String>()
                val imagesArray = item.optJSONArray("images")
                if (imagesArray != null) {
                    for (imgIdx in 0 until imagesArray.length()) {
                        val img = imagesArray.optString(imgIdx, "").trim()
                        if (img.isNotEmpty()) imagesList.add(img)
                    }
                }

                levels.add(
                    FindPairLevelDto(
                        id = id,
                        number = item.optInt("number", i + 1),
                        title = item.optString("title", "Level ${i + 1}"),
                        difficulty = item.optString("difficulty", "EASY").uppercase(),
                        gridRows = item.optInt("grid_rows", 4),
                        gridColumns = item.optInt("grid_columns", 3),
                        timeLimitSeconds = item.optInt("time_limit_seconds", 120),
                        threeStarMaxFlips = item.optInt("three_star_max_flips", 16),
                        twoStarMaxFlips = item.optInt("two_star_max_flips", 24),
                        images = imagesList,
                        thumbnailUrl = item.optString("thumbnail_url", "").takeIf { it.isNotEmpty() },
                        thumbnailAsset = item.optString("thumbnail_asset", "").takeIf { it.isNotEmpty() }
                    )
                )
            }
            levels
        } catch (e: JSONException) {
            throw RemoteDataParseException("Failed to parse find_the_pair JSON: ${e.message}", e)
        }
    }

    @Throws(RemoteDataParseException::class)
    fun parseCats(jsonString: String): List<CatDto> {
        return try {
            val json = JSONObject(jsonString)
            val catsArray = json.optJSONArray("cats")
                ?: throw RemoteDataParseException("Missing 'cats' array in cats JSON")

            val cats = mutableListOf<CatDto>()
            for (i in 0 until catsArray.length()) {
                val item = catsArray.getJSONObject(i)
                val id = item.optString("id", "").trim()
                if (id.isEmpty()) throw RemoteDataParseException("Cat item missing 'id' at index $i")

                val condObj = item.optJSONObject("unlock_condition")
                val cond = if (condObj != null) {
                    CatUnlockConditionDto(
                        type = condObj.optString("type", "DEFAULT_UNLOCKED").uppercase(),
                        mode = condObj.optString("mode", "").takeIf { it.isNotEmpty() },
                        levelNumber = if (condObj.has("level_number")) condObj.optInt("level_number") else null,
                        levelId = condObj.optString("level_id", "").takeIf { it.isNotEmpty() },
                        count = if (condObj.has("count")) condObj.optInt("count") else null
                    )
                } else {
                    CatUnlockConditionDto(type = "DEFAULT_UNLOCKED")
                }

                cats.add(
                    CatDto(
                        id = id,
                        name = item.optString("name", "Cat ${i + 1}"),
                        rarity = item.optString("rarity", "COMMON").uppercase(),
                        themeId = item.optString("theme_id", "SLEEPY").uppercase(),
                        description = item.optString("description", ""),
                        imageUrl = item.optString("image_url", "").takeIf { it.isNotEmpty() },
                        imageAsset = item.optString("image_asset", "").takeIf { it.isNotEmpty() },
                        thumbnailUrl = item.optString("thumbnail_url", "").takeIf { it.isNotEmpty() },
                        unlockCondition = cond,
                        unlockHint = item.optString("unlock_hint", "Available from start")
                    )
                )
            }
            cats
        } catch (e: JSONException) {
            throw RemoteDataParseException("Failed to parse cats JSON: ${e.message}", e)
        }
    }
}
