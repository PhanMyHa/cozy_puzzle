package com.example.cozypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val species: String,
    val level: Int,
    val experience: Int,
    val mood: String,
    val favoriteItemId: String?,
    val gardenPositionX: Float?,
    val gardenPositionY: Float?
)
