package com.example.cozypuzzle.domain.model.mycats

import androidx.annotation.StringRes
import com.example.cozypuzzle.R

enum class CatTheme(
    val id: String,
    @StringRes val titleRes: Int,
    val iconEmoji: String
) {
    SLEEPY("theme_sleepy", R.string.cat_theme_sleepy, "💤"),
    CHEF("theme_chef", R.string.cat_theme_chef, "🧑‍🍳"),
    SPACE("theme_space", R.string.cat_theme_space, "🚀"),
    GARDEN("theme_garden", R.string.cat_theme_garden, "🌸")
}
