package com.example.cozypuzzle.domain.model

data class Player(
    val id: Long = 0L,
    val name: String,
    val avatarId: Int,

    /** Local file path of the uploaded avatar photo; null until one is set. */
    val avatarUri: String? = null,

    val level: Int,
    val coins: Int,
    val gems: Int
) {
    companion object {
        val DEFAULT = Player(
            name = "",
            avatarId = 0,
            level = 1,
            coins = STARTING_COINS,
            gems = 0
        )

        /** Welcome grant so the shop can be enjoyed right away. */
        const val STARTING_COINS = 300
    }
}
