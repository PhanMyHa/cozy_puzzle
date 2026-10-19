package com.example.cozypuzzle.domain.usecase.daily

import com.example.cozypuzzle.core.path.model.StrokePoint
import com.example.cozypuzzle.core.path.validation.DailyPathValidator
import com.example.cozypuzzle.core.path.validation.DailyStrokeOutcome
import com.example.cozypuzzle.domain.model.DailyPuzzle
import javax.inject.Inject

/**
 * Starts a drawing attempt: the first touch must land on/near the start point.
 */
class StartDailyStrokeUseCase @Inject constructor(
    private val validator: DailyPathValidator
) {

    operator fun invoke(puzzle: DailyPuzzle, point: StrokePoint): DailyStrokeOutcome =
        validator.validateStart(puzzle, point)
}
