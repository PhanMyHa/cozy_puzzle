package com.example.cozypuzzle.core.path.model

data class CatBoard(
    val id: String,
    val rows: Int,
    val columns: Int,
    /** Tiles keyed by cell index; cells without a tile are empty ground. */
    val tiles: Map<Int, PathTile>,
    val obstacles: Set<Int>,
    val catPosition: Int,
    val homePosition: Int
) {
    val cellCount: Int
        get() = rows * columns

    fun tileAt(cell: Int): PathTile? = tiles[cell]

    fun neighbors(cell: Int): List<Pair<Direction, Int>> = buildList {
        val col = cell % columns
        val row = cell / columns
        if (row > 0) add(Direction.NORTH to cell - columns)
        if (col < columns - 1) add(Direction.EAST to cell + 1)
        if (row < rows - 1) add(Direction.SOUTH to cell + columns)
        if (col > 0) add(Direction.WEST to cell - 1)
    }

    /** Two adjacent cells are connected when both tiles open towards each other. */
    fun connected(from: Int, direction: Direction, to: Int): Boolean =
        tileAt(from)?.connections?.contains(direction) == true &&
            tileAt(to)?.connections?.contains(direction.opposite) == true
}

sealed interface CatAction {

    data class Rotate(
        val cell: Int
    ) : CatAction

    data class Swap(
        val firstCell: Int,
        val secondCell: Int
    ) : CatAction
}
