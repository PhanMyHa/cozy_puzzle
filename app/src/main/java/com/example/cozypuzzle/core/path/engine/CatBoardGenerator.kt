package com.example.cozypuzzle.core.path.engine

import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.model.Direction
import com.example.cozypuzzle.core.path.model.PathTile
import com.example.cozypuzzle.core.puzzle.model.Difficulty
import java.util.ArrayDeque
import java.util.Random
import javax.inject.Inject

/**
 * Deterministic level builder. A route from the cat to the home cell is carved
 * first (BFS + optional detour), guaranteeing a solvable board. Free cells
 * receive obstacles based on difficulty. Finally the board is scrambled
 * (placements + rotations) so levels start unsolved but stay solvable.
 */
class CatBoardGenerator @Inject constructor() {

    fun generate(
        levelId: String,
        rows: Int,
        columns: Int,
        difficulty: Difficulty,
        seed: Long
    ): CatBoard {
        require(rows >= MIN_SIZE && columns >= MIN_SIZE) {
            "Board ${rows}x${columns} too small, minimum is ${MIN_SIZE}x$MIN_SIZE"
        }
        val random = Random(seed)

        var cat = random.nextInt(rows * columns)
        var home = pickHome(cat, rows, columns, random)

        val route = carveRoute(cat, home, rows, columns, random)
        cat = route.first()
        home = route.last()

        val solvedTiles = solvedTiles(route)
        val obstacles = pickObstacles(rows * columns, difficulty, setOf(cat, home), random)

        var candidate = scramble(CatBoard(levelId, rows, columns, solvedTiles, obstacles, cat, home), random)
        var attempt = 1
        while (!isScrambled(candidate, solvedTiles) && attempt < MAX_ATTEMPTS) {
            candidate = scramble(CatBoard(levelId, rows, columns, solvedTiles, obstacles, cat, home), Random(seed + attempt))
            attempt++
        }
        return candidate
    }

    private fun pickHome(cat: Int, rows: Int, columns: Int, random: Random): Int {
        var home = random.nextInt(rows * columns)
        while (home == cat || manhattan(cat, home, columns) < MIN_HOME_DISTANCE) {
            home = random.nextInt(rows * columns)
        }
        return home
    }

    /** Shortest corridor via BFS, optionally widened with one bounded detour. */
    private fun carveRoute(cat: Int, home: Int, rows: Int, columns: Int, random: Random): List<Int> {
        val base = bfsPath(cat, home, rows, columns) ?: return listOf(cat, home)
        if (base.size < DETOUR_MIN_PATH || random.nextDouble() > DETOUR_PROBABILITY) return base

        // Insert a small bulge through a cell adjacent to both corridor ends.
        val insertIndex = 1 + random.nextInt(base.size - 2)
        val before = base[insertIndex - 1]
        val pivot = base[insertIndex]
        val detourCandidates = neighbors(pivot, rows, columns)
            .filter { !base.contains(it) && neighbors(it, rows, columns).contains(before) }
        if (detourCandidates.isEmpty()) return base
        val detourCell = detourCandidates[random.nextInt(detourCandidates.size)]

        return buildList {
            addAll(base.subList(0, insertIndex))
            add(detourCell)
            add(pivot)
            addAll(base.subList(insertIndex + 1, base.size))
        }
    }

    private fun bfsPath(start: Int, goal: Int, rows: Int, columns: Int): List<Int>? {
        if (start == goal) return listOf(start)
        val cameFrom = mutableMapOf<Int, Int>()
        val queue = ArrayDeque<Int>().apply { add(start) }
        val visited = mutableSetOf(start)

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            if (current == goal) break
            neighbors(current, rows, columns).forEach { neighbor ->
                if (visited.add(neighbor)) {
                    cameFrom[neighbor] = current
                    queue.add(neighbor)
                }
            }
        }
        if (!cameFrom.containsKey(goal)) return null

        val path = ArrayDeque<Int>()
        var cursor: Int? = goal
        while (cursor != null) {
            path.addFirst(cursor)
            cursor = cameFrom[cursor]
        }
        return path.toList()
    }

    private fun neighbors(cell: Int, rows: Int, columns: Int): List<Int> = buildList {
        if (cell - columns >= 0) add(cell - columns)
        if (cell % columns < columns - 1) add(cell + 1)
        if (cell + columns < rows * columns) add(cell + columns)
        if (cell % columns > 0) add(cell - 1)
    }

    private fun solvedTiles(route: List<Int>): Map<Int, PathTile> {
        val connections = mutableMapOf<Int, MutableSet<Direction>>()
        route.zipWithNext().forEach { (from, to) ->
            val direction = directionBetween(from, to)
            connections.getOrPut(from) { mutableSetOf() }.add(direction)
            connections.getOrPut(to) { mutableSetOf() }.add(direction.opposite)
        }
        return connections.mapValues { (cell, dirs) -> PathTile(id = cell, baseConnections = dirs.toSet()) }
    }

    private fun directionBetween(from: Int, to: Int): Direction = when {
        to == from + 1 -> Direction.EAST
        to == from - 1 -> Direction.WEST
        to > from -> Direction.SOUTH
        else -> Direction.NORTH
    }

    private fun pickObstacles(
        cellCount: Int,
        difficulty: Difficulty,
        protectedCells: Set<Int>,
        random: Random
    ): Set<Int> = buildSet {
        (0 until cellCount).forEach { cell ->
            if (!protectedCells.contains(cell) && random.nextDouble() < obstacleProbability(difficulty)) add(cell)
        }
    }

    private fun obstacleProbability(difficulty: Difficulty): Double = when (difficulty) {
        Difficulty.EASY -> EASY_OBSTACLE_CHANCE
        Difficulty.MEDIUM -> MEDIUM_OBSTACLE_CHANCE
        Difficulty.HARD -> HARD_OBSTACLE_CHANCE
        Difficulty.EXPERT -> EXPERT_OBSTACLE_CHANCE
    }

    private fun scramble(board: CatBoard, random: Random): CatBoard {
        val movableCells = board.tiles.keys.filter { cell ->
            cell != board.catPosition && cell != board.homePosition
        }
        val targets = movableCells.shuffled(random)
        val placements = movableCells.indices.associate { index ->
            targets[index] to board.tiles.getValue(movableCells[index])
        }
        val rotated = placements.mapValues { (_, tile) ->
            tile.copy(rotation = random.nextInt(ROTATION_STEPS) * ROTATION_DEGREES)
        }
        // Keep the fixed cat/home tiles; replace only the movable ones.
        return board.copy(tiles = board.tiles - movableCells.toSet() + rotated)
    }

    private fun isScrambled(candidate: CatBoard, solvedTiles: Map<Int, PathTile>): Boolean =
        candidate.tiles.any { (cell, tile) ->
            solvedTiles[cell]?.let { solved ->
                solved.baseConnections != tile.baseConnections || tile.rotation % FULL_ROTATION != 0
            } ?: true
        }

    private fun manhattan(from: Int, to: Int, columns: Int): Int =
        kotlin.math.abs(from % columns - to % columns) +
            kotlin.math.abs(from / columns - to / columns)

    private companion object {
        const val MIN_SIZE = 2
        const val MIN_HOME_DISTANCE = 3
        const val DETOUR_PROBABILITY = 0.5
        const val DETOUR_MIN_PATH = 4
        const val MAX_ATTEMPTS = 20
        const val ROTATION_STEPS = 4
        const val ROTATION_DEGREES = 90
        const val FULL_ROTATION = 360

        const val EASY_OBSTACLE_CHANCE = 0.10
        const val MEDIUM_OBSTACLE_CHANCE = 0.18
        const val HARD_OBSTACLE_CHANCE = 0.25
        const val EXPERT_OBSTACLE_CHANCE = 0.32
    }
}
