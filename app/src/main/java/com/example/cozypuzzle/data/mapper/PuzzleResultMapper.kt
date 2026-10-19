package com.example.cozypuzzle.data.mapper

import com.example.cozypuzzle.core.puzzle.model.Difficulty
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.data.local.entity.PuzzleResultEntity
import com.example.cozypuzzle.domain.model.PuzzleResult

fun PuzzleResult.toEntity(): PuzzleResultEntity = PuzzleResultEntity(
    puzzleId = puzzleId,
    puzzleMode = mode.name,
    difficulty = difficulty.name,
    rows = rows,
    columns = columns,
    durationMs = durationMs,
    moveCount = moveCount,
    hintsUsed = hintsUsed,
    attemptCount = attempts,
    brushColorId = brushColorId,
    completedAtEpochMs = completedAtEpochMs
)

fun PuzzleResultEntity.toDomain(): PuzzleResult = PuzzleResult(
    id = id,
    puzzleId = puzzleId,
    mode = runCatching { PuzzleMode.valueOf(puzzleMode) }.getOrDefault(PuzzleMode.DAILY_DRAW),
    difficulty = Difficulty.valueOf(difficulty),
    rows = rows,
    columns = columns,
    durationMs = durationMs,
    moveCount = moveCount,
    hintsUsed = hintsUsed,
    attempts = attemptCount,
    brushColorId = brushColorId,
    completedAtEpochMs = completedAtEpochMs
)
