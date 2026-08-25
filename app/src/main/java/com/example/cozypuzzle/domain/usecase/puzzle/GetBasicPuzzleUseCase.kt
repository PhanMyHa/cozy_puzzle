package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.domain.model.BasicPuzzle
import com.example.cozypuzzle.domain.repository.BasicPuzzleRepository
import javax.inject.Inject

class GetBasicPuzzleUseCase @Inject constructor(
    private val basicPuzzleRepository: BasicPuzzleRepository
) {

    suspend operator fun invoke(puzzleId: String): BasicPuzzle? =
        basicPuzzleRepository.getBasicPuzzle(puzzleId)
}
