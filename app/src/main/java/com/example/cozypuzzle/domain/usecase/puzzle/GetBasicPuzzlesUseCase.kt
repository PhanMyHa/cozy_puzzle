package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.domain.model.BasicPuzzle
import com.example.cozypuzzle.domain.repository.BasicPuzzleRepository
import javax.inject.Inject

class GetBasicPuzzlesUseCase @Inject constructor(
    private val basicPuzzleRepository: BasicPuzzleRepository
) {

    suspend operator fun invoke(): List<BasicPuzzle> =
        basicPuzzleRepository.getBasicPuzzles()
}
