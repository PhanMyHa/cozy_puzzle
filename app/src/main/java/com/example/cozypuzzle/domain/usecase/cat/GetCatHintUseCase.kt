package com.example.cozypuzzle.domain.usecase.cat

import com.example.cozypuzzle.core.path.engine.CatBoardEngine
import com.example.cozypuzzle.core.path.model.CatAction
import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.solver.PathFinder
import javax.inject.Inject

data class CatHint(
    /** Cells to highlight on the board (source of the solving move). */
    val cells: List<Int>,
    val action: CatAction
)

/**
 * Finds a single move that opens a cat-to-home path. Rotations are tried
 * first, then pairwise swaps; the board itself is never modified.
 */
class GetCatHintUseCase @Inject constructor(
    private val engine: CatBoardEngine,
    private val pathFinder: PathFinder
) {

    operator fun invoke(board: CatBoard, hintsRemaining: Int): CatHint? {
        if (hintsRemaining <= 0) return null

        val movable = board.tiles.keys.filter { cell ->
            cell != board.catPosition && cell != board.homePosition
        }

        movable.forEach { cell ->
            val rotated = engine.rotate(board, cell)
            if (pathFinder.findPath(rotated).found) {
                return CatHint(cells = listOf(cell), action = CatAction.Rotate(cell))
            }
        }

        movable.forEachIndexed { index, first ->
            movable.drop(index + 1).forEach { second ->
                val swapped = engine.swap(board, first, second)
                if (pathFinder.findPath(swapped).found) {
                    return CatHint(
                        cells = listOf(first, second),
                        action = CatAction.Swap(first, second)
                    )
                }
            }
        }
        return null
    }
}
