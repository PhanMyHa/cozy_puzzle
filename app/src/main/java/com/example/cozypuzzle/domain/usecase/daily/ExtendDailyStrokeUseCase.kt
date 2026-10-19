package com.example.cozypuzzle.domain.usecase.daily

import com.example.cozypuzzle.core.path.model.StrokePoint
import com.example.cozypuzzle.core.path.validation.DailyPathValidator
import com.example.cozypuzzle.core.path.validation.DailyStrokeOutcome
import com.example.cozypuzzle.domain.model.DailyPuzzle
import javax.inject.Inject

/**
 * Extends an in-progress stroke with freshly recorded samples and validates
 * continuity plus obstacle collision using the full one-cell stroke width.
 */
class ExtendDailyStrokeUseCase @Inject constructor(
    private val validator: DailyPathValidator
) {

    operator fun invoke(
        puzzle: DailyPuzzle,
        currentPath: List<StrokePoint>,
        newPoints: List<StrokePoint>
    ): DailyStrokeOutcome = validator.validateExtension(puzzle, currentPath, newPoints)
}
