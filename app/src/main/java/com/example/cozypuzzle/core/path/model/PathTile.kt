package com.example.cozypuzzle.core.path.model

data class PathTile(
    val id: Int,
    val baseConnections: Set<Direction>,
    val rotation: Int = 0
) {
    val connections: Set<Direction>
        get() = baseConnections.map { direction ->
            Direction.fromDegrees(direction.rotationDegrees + rotation)
        }.toSet()
}
