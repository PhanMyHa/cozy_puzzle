package com.example.cozypuzzle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "garden_items")
data class GardenItemEntity(
    @PrimaryKey val id: String,
    val itemType: String,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val scale: Float
)
