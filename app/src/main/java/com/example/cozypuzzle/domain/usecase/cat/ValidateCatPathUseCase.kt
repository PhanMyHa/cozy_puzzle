package com.example.cozypuzzle.domain.usecase.cat

import com.example.cozypuzzle.core.path.model.CatBoard
import com.example.cozypuzzle.core.path.solver.PathFinder
import javax.inject.Inject

class ValidateCatPathUseCase @Inject constructor(
    private val pathFinder: PathFinder
) {

    operator fun invoke(board: CatBoard) = pathFinder.findPath(board)
}
