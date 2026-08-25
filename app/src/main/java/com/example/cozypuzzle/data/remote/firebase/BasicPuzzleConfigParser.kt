package com.example.cozypuzzle.data.remote.firebase

import com.example.cozypuzzle.domain.model.BasicPuzzleConfig
import org.json.JSONArray
import org.json.JSONObject

class BasicPuzzleConfigParser {

    fun parse(raw: String): BasicPuzzleConfig? = try {
        val json = JSONObject(raw)
        val sizes = json.optJSONArray("available_sizes")
            ?.let { array -> (0 until array.length()).mapNotNull { array.optInt(it, -1) } }
            .orEmpty()
            .filter { it in MIN_BOARD_SIZE..MAX_BOARD_SIZE }
            .distinct()
        val defaultSize = json.optInt("default_size", -1)

        if (!json.optBoolean("enabled")) {
            null
        } else if (sizes.isEmpty() || defaultSize !in sizes) {
            null
        } else {
            val categories = json.optJSONArray("categories")
                ?.let { array -> (0 until array.length()).mapNotNull { array.optString(it) } }
                .orEmpty()
                .filter(String::isNotBlank)
            BasicPuzzleConfig(
                enabled = true,
                availableSizes = sizes,
                defaultSize = defaultSize,
                categories = categories.ifEmpty { BasicPuzzleConfig.DEFAULT.categories },
                allowRotate = json.optBoolean("allow_rotate", true),
                allowSwap = json.optBoolean("allow_swap", true),
                allowDrag = json.optBoolean("allow_drag", true),
                maxHints = json.optInt("max_hints", 0).coerceAtLeast(0)
            )
        }
    } catch (e: Exception) {
        null
    }

    private companion object {
        const val MIN_BOARD_SIZE = 2
        const val MAX_BOARD_SIZE = 8
    }
}
