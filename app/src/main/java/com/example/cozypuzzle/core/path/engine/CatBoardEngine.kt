package com.example.cozypuzzle.core.path.engine

import com.example.cozypuzzle.core.path.model.CatAction
import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.model.PathTile
import javax.inject.Inject

/**
 * Pure state transitions for the cat path board. Immutable: every operation
 * returns a new [CatBoard]. Cat, home and obstacle cells are never modified.
 */
class CatBoardEngine @Inject constructor() {

    fun rotate(board: CatBoard, cell: Int): CatBoard {
        val tile = requireMovableTile(board, cell)
        return replaceTile(board, cell, tile.copy(rotation = (tile.rotation + ROTATION_STEP) % FULL_ROTATION))
    }

    fun swap(board: CatBoard, firstCell: Int, secondCell: Int): CatBoard {
        require(firstCell != secondCell) { "Cannot swap cell $firstCell with itself" }
        val first = requireMovableTile(board, firstCell)
        val second = requireMovableTile(board, secondCell)
        val swapped = mapOf(
            firstCell to second,
            secondCell to first
        )
        return board.copy(tiles = board.tiles + swapped)
    }

    fun applyAction(board: CatBoard, action: CatAction): CatBoard = when (action) {
        is CatAction.Rotate -> rotate(board, action.cell)
        is CatAction.Swap -> swap(board, action.firstCell, action.secondCell)
    }

    private fun requireMovableTile(board: CatBoard, cell: Int): PathTile {
        require(cell in 0 until board.cellCount) {
            "Cell $cell is outside the ${board.rows}x${board.columns} board"
        }
        require(!board.obstacles.contains(cell)) { "Cell $cell holds an obstacle and cannot be modified" }
        require(cell != board.catPosition && cell != board.homePosition) {
            "Cell $cell is fixed and cannot be modified"
        }
        return board.tileAt(cell)
            ?: throw IllegalArgumentException("Cell $cell has no tile to manipulate")
    }

    private fun replaceTile(board: CatBoard, cell: Int, tile: PathTile): CatBoard =
        board.copy(tiles = board.tiles + (cell to tile))

    private companion object {
        const val ROTATION_STEP = 90
        const val FULL_ROTATION = 360
    }
}
