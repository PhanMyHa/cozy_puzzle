package com.example.cozypuzzle.core.path.model

data class PathResult(
    val found: Boolean,
    val cells: List<Int> = emptyList()
) {
    companion object {
        val NOT_FOUND = PathResult(found = false)
    }
}
