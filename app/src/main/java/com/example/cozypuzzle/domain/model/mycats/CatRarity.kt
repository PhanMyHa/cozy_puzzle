package com.example.cozypuzzle.domain.model.mycats

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.example.cozypuzzle.R

enum class CatRarity(
    @StringRes val titleRes: Int,
    @ColorRes val colorRes: Int,
    @ColorRes val bgRes: Int,
    val starsRating: Int
) {
    COMMON(R.string.cat_rarity_common, R.color.cozy_rarity_common, R.color.cozy_rarity_common_bg, 1),
    RARE(R.string.cat_rarity_rare, R.color.cozy_rarity_rare, R.color.cozy_rarity_rare_bg, 2),
    EPIC(R.string.cat_rarity_epic, R.color.cozy_rarity_epic, R.color.cozy_rarity_epic_bg, 3),
    LEGENDARY(R.string.cat_rarity_legendary, R.color.cozy_rarity_legendary, R.color.cozy_rarity_legendary_bg, 3)
}
