package com.example.cozypuzzle.core.path.solver

import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.model.PathResult
import java.util.PriorityQueue
import javax.inject.Inject

/**
 * A* search over connected path tiles with unit step cost and a Manhattan
 * distance heuristic. Movement is orthogonal only, across tile connections.
 */
class AStarPathFinder @Inject constructor() : PathFinder {

    override fun findPath(board: CatBoard): PathResult {
        if (board.catPosition == board.homePosition) {
            return PathResult(found = true, cells = listOf(board.catPosition))
        }
        if (!board.tiles.containsKey(board.catPosition) ||
            !board.tiles.containsKey(board.homePosition)
        ) {
            return PathResult.NOT_FOUND
        }

        val start = board.catPosition
        val goal = board.homePosition

        val open = PriorityQueue<SearchNode>(INITIAL_CAPACITY, compareBy { it.priority })
        val cameFrom = mutableMapOf<Int, Int>()
        val bestCost = mutableMapOf(start to 0)

        open.add(SearchNode(cell = start, cost = 0, priority = heuristic(start, goal, board)))

        while (open.isNotEmpty()) {
            val current = open.poll()
            if (current.cell == goal) return PathResult(found = true, cells = reconstruct(cameFrom, goal))

            if (current.cost > bestCost.getOrDefault(current.cell, Int.MAX_VALUE)) continue

            board.neighbors(current.cell).forEach { (direction, neighbor) ->
                if (board.obstacles.contains(neighbor)) return@forEach
                if (!board.connected(current.cell, direction, neighbor)) return@forEach

                val nextCost = current.cost + STEP_COST
                if (nextCost < bestCost.getOrDefault(neighbor, Int.MAX_VALUE)) {
                    bestCost[neighbor] = nextCost
                    cameFrom[neighbor] = current.cell
                    open.add(
                        SearchNode(
                            cell = neighbor,
                            cost = nextCost,
                            priority = nextCost + heuristic(neighbor, goal, board)
                        )
                    )
                }
            }
        }
        return PathResult.NOT_FOUND
    }

    private fun heuristic(from: Int, to: Int, board: CatBoard): Int {
        val fromCol = from % board.columns
        val fromRow = from / board.columns
        val toCol = to % board.columns
        val toRow = to / board.columns
        return kotlin.math.abs(fromCol - toCol) + kotlin.math.abs(fromRow - toRow)
    }

    private fun reconstruct(cameFrom: Map<Int, Int>, goal: Int): List<Int> {
        val path = ArrayDeque<Int>()
        var cursor: Int? = goal
        while (cursor != null) {
            path.addFirst(cursor)
            cursor = cameFrom[cursor]
        }
        return path.toList()
    }

    private data class SearchNode(
        val cell: Int,
        val cost: Int,
        val priority: Int
    )

    private companion object {
        const val STEP_COST = 1
        const val INITIAL_CAPACITY = 16
    }
}
