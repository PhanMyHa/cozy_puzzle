package com.example.cozypuzzle.presentation.puzzle

import androidx.annotation.DrawableRes
import com.example.cozypuzzle.R

object BasicPuzzleImages {

    private val imagesByContentId = mapOf(
        "cute_cat" to R.drawable.img_puzzle_cute_cat,
        "cozy_room" to R.drawable.img_puzzle_cozy_room,
        "little_garden" to R.drawable.img_puzzle_little_garden,
        "frog_pond" to R.drawable.img_puzzle_frog_pond,
        "picnic" to R.drawable.img_puzzle_picnic
    )

    @DrawableRes
    fun imageResFor(contentId: String?): Int =
        contentId?.let { imagesByContentId[it] } ?: DEFAULT_IMAGE_RES

    private const val DEFAULT_IMAGE_ID = "little_garden"

    private val DEFAULT_IMAGE_RES: Int = imagesByContentId[DEFAULT_IMAGE_ID]!!
}
