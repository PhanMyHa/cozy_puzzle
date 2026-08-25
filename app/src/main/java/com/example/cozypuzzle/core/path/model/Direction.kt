package com.example.cozypuzzle.core.path.model

enum class Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    val opposite: Direction
        get() = when (this) {
            NORTH -> SOUTH
            EAST -> WEST
            SOUTH -> NORTH
            WEST -> EAST
        }

    val rotationDegrees: Int
        get() = when (this) {
            NORTH -> 0
            EAST -> 90
            SOUTH -> 180
            WEST -> 270
        }

    companion object {
        fun fromDegrees(degrees: Int): Direction =
            entries.first { it.rotationDegrees == ((degrees % 360) + 360) % 360 }
    }
}
