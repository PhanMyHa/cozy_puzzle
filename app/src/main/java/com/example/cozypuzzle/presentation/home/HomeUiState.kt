package com.example.cozypuzzle.presentation.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode

data class GameModeUiModel(
    val mode: PuzzleMode,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    @DrawableRes val thumbnailRes: Int = iconRes
)

data class HomeUiState(
    val playerName: String = "",
    val avatarUri: String? = null,
    val playerLevel: Int = 5,
    val playerCoins: Int = 120,
    val levelProgress: Int = 60,
    val dailyPuzzleSize: Int = 4,
    val gameModes: List<GameModeUiModel> = defaultGameModes(),
    val isLoading: Boolean = true
) {
    companion object {
        fun defaultGameModes(): List<GameModeUiModel> = listOf(
            GameModeUiModel(
                mode = PuzzleMode.CAT_PUZZLE,
                titleRes = R.string.game_mode_cat_puzzle,
                iconRes = R.drawable.ic_mode_basic,
                thumbnailRes = R.drawable.img_puzzle_cute_cat
            ),
            GameModeUiModel(
                mode = PuzzleMode.COLOR_THE_CAT,
                titleRes = R.string.game_mode_color_the_cat,
                iconRes = R.drawable.ic_mode_color_cat,
                thumbnailRes = R.drawable.img_puzzle_nature
            ),
            GameModeUiModel(
                mode = PuzzleMode.SLIDE_THE_CAT,
                titleRes = R.string.game_mode_slide_cat,
                iconRes = R.drawable.ic_mode_slide_cat,
                thumbnailRes = R.drawable.img_puzzle_animals
            ),
            GameModeUiModel(
                mode = PuzzleMode.FIND_THE_PAIR,
                titleRes = R.string.find_pair_title,
                iconRes = R.drawable.ic_mode_find_pair,
                thumbnailRes = R.drawable.img_puzzle_cozy_room
            )
        )
    }
}
