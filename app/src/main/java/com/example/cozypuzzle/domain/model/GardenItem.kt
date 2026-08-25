package com.example.cozypuzzle.domain.model

data class GardenItem(
    val id: String,
    val itemType: String,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val scale: Float
)
