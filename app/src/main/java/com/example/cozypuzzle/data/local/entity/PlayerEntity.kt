package com.example.cozypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player")
data class PlayerEntity(
    @PrimaryKey val id: Long = DEFAULT_PLAYER_ID,
    val name: String,
    val avatarId: Int,
    val level: Int,
    val coins: Int,
    val gems: Int
) {
    companion object {
        const val DEFAULT_PLAYER_ID = 1L
    }
}
