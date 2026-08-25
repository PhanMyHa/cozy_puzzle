package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.core.puzzle.engine.PuzzleGenerator
import com.example.cozypuzzle.core.puzzle.model.Puzzle
import com.example.cozypuzzle.core.puzzle.model.PuzzleConfig
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.domain.model.BasicPuzzleConfig
import com.example.cozypuzzle.domain.repository.BasicPuzzleRepository
import com.example.cozypuzzle.domain.repository.ConfigRepository
import javax.inject.Inject

data class BasicPuzzleGame(
    val puzzle: Puzzle,
    val puzzleTitle: String,
    val imageId: String,
    val gridSize: Int,
    val maxHints: Int,
    val allowRotate: Boolean,
    val allowSwap: Boolean,
    val allowDrag: Boolean
)

class StartBasicPuzzleUseCase @Inject constructor(
    private val basicPuzzleRepository: BasicPuzzleRepository,
    private val configRepository: ConfigRepository,
    private val generator: PuzzleGenerator
) {

    suspend operator fun invoke(puzzleId: String, requestedSize: Int, seed: Long): BasicPuzzleGame {
        val remoteConfig = configRepository.basicPuzzleConfig.value
        if (!remoteConfig.enabled) {
            throw IllegalStateException("Basic mode is disabled by remote configuration")
        }
        val metadata = basicPuzzleRepository.getBasicPuzzle(puzzleId)
            ?: throw IllegalArgumentException("Unknown basic puzzle '$puzzleId'")
        require(requestedSize in metadata.availableGridSizes) {
            "Grid size $requestedSize is not available for puzzle '${metadata.id}'. " +
                "Available sizes: ${metadata.availableGridSizes}"
        }
        require(remoteConfig.allowGridSize(requestedSize)) {
            "Grid size $requestedSize is disabled by configuration"
        }

        return BasicPuzzleGame(
            puzzle = generator.generate(
                config = PuzzleConfig(
                    rows = requestedSize,
                    columns = requestedSize,
                    mode = PuzzleMode.BASIC,
                    difficulty = metadata.difficulty,
                    contentId = metadata.imageId
                ),
                seed = seed
            ),
            puzzleTitle = metadata.title,
            imageId = metadata.imageId,
            gridSize = requestedSize,
            maxHints = remoteConfig.maxHints,
            allowRotate = remoteConfig.allowRotate,
            allowSwap = remoteConfig.allowSwap,
            allowDrag = remoteConfig.allowDrag
        )
    }

    private fun BasicPuzzleConfig.allowGridSize(size: Int): Boolean =
        size in availableSizes
}
