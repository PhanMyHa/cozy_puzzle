package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.domain.model.DailyPuzzle
import com.example.cozypuzzle.domain.model.DailyPuzzleTheme
import javax.inject.Inject

/**
 * Locally bundled daily puzzles. Boards are written row-major; `S` marks the
 * start cell, `G` the goal and `#` an obstacle.
 */
class BundledDailyPuzzleDataSource @Inject constructor() : LocalDailyPuzzleDataSource {

    private val puzzles: List<DailyPuzzle> = listOf(
        puzzle(
            id = "dew_drop",
            title = "Dew Drop",
            theme = DailyPuzzleTheme.MEADOW,
            difficulty = Difficulty.EASY,
            recommendedBrushId = "pink",
            grid = listOf(
                "S...",
                ".##.",
                "....",
                "...G"
            )
        ),
        puzzle(
            id = "morning_meadow",
            title = "Morning Meadow",
            theme = DailyPuzzleTheme.MEADOW,
            difficulty = Difficulty.EASY,
            recommendedBrushId = "green",
            grid = listOf(
                "S..#.",
                ".#.#.",
                ".#...",
                "..##.",
                "....G"
            )
        ),
        puzzle(
            id = "butterfly_lane",
            title = "Butterfly Lane",
            theme = DailyPuzzleTheme.GARDEN,
            difficulty = Difficulty.MEDIUM,
            recommendedBrushId = "purple",
            grid = listOf(
                "..#.S",
                ".##..",
                "G....",
                ".#.#.",
                "...#."
            )
        ),
        puzzle(
            id = "clover_corner",
            title = "Clover Corner",
            theme = DailyPuzzleTheme.GARDEN,
            difficulty = Difficulty.MEDIUM,
            recommendedBrushId = "blue",
            grid = listOf(
                "S#....",
                ".#.##.",
                "...#..",
                ".###.#",
                ".....#",
                "..#..G"
            )
        ),
        puzzle(
            id = "thorny_rose",
            title = "Thorny Rose",
            theme = DailyPuzzleTheme.GARDEN,
            difficulty = Difficulty.HARD,
            recommendedBrushId = "orange",
            grid = listOf(
                "..#..S",
                ".##.#.",
                "....#.",
                "##.#..",
                "...#.#",
                "G#...."
            )
        ),
        puzzle(
            id = "starlit_garden",
            title = "Starlit Garden",
            theme = DailyPuzzleTheme.POND,
            difficulty = Difficulty.HARD,
            recommendedBrushId = "yellow",
            grid = listOf(
                "S..#...",
                ".#.#.#.",
                ".#...#.",
                "...#...",
                "##.#.#.",
                "...#..G",
                ".#...#."
            )
        ),
        puzzle(
            id = "grand_gala",
            title = "Grand Gala",
            theme = DailyPuzzleTheme.MEADOW,
            difficulty = Difficulty.EXPERT,
            recommendedBrushId = "pink",
            grid = listOf(
                "S...#...",
                "###.#.#.",
                "....#.#.",
                ".####.#.",
                ".#......",
                ".#.###.#",
                "...#....",
                ".######G"
            )
        )
    )

    override suspend fun getPuzzles(): List<DailyPuzzle> = puzzles

    override suspend fun getPuzzle(puzzleId: String): DailyPuzzle? =
        puzzles.firstOrNull { it.id == puzzleId }

    private fun puzzle(
        id: String,
        title: String,
        theme: DailyPuzzleTheme,
        difficulty: Difficulty,
        recommendedBrushId: String?,
        grid: List<String>
    ): DailyPuzzle {
        val size = grid.size
        var start = -1
        var goal = -1
        val obstacles = mutableSetOf<Int>()
        grid.forEachIndexed { row, line ->
            require(line.length == size) { "Row $row of '$id' must have $size columns" }
            line.forEachIndexed { column, char ->
                val cell = row * size + column
                when (char) {
                    'S' -> start = cell
                    'G' -> goal = cell
                    '#' -> obstacles += cell
                    '.', ' ' -> Unit
                    else -> throw IllegalArgumentException("Unknown symbol '$char' in '$id'")
                }
            }
        }
        require(start >= 0 && goal >= 0) { "Puzzle '$id' needs a start and a goal" }
        return DailyPuzzle(
            id = id,
            title = title,
            boardSize = size,
            startCell = start,
            goalCell = goal,
            obstacleCells = obstacles,
            difficulty = difficulty,
            theme = theme,
            recommendedBrushId = recommendedBrushId
        )
    }
}
