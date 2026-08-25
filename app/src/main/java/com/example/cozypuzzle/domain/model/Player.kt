package com.example.cozypuzzle.domain.model

data class Player(
    val id: Long = 0L,
    val name: String,
    val avatarId: Int,
    val level: Int,
    val coins: Int,
    val gems: Int
) {
    companion object {
        val DEFAULT = Player(
            name = "",
            avatarId = 0,
            level = 1,
            coins = 0,
            gems = 0
        )
    }
}
