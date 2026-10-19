package com.example.cozypuzzle.domain.model.mycats

import androidx.annotation.DrawableRes

data class CatModel(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
    val rarity: CatRarity,
    val theme: CatTheme,
    val description: String,
    val foundIn: String,
    val unlockCondition: CatUnlockCondition,
    val unlockHint: String
)
