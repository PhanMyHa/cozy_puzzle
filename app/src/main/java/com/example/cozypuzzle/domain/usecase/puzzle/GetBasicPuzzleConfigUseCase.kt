package com.example.cozypuzzle.domain.usecase.puzzle

import com.example.cozypuzzle.domain.model.BasicPuzzleConfig
import com.example.cozypuzzle.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetBasicPuzzleConfigUseCase @Inject constructor(
    private val configRepository: ConfigRepository
) {

    operator fun invoke(): StateFlow<BasicPuzzleConfig> = configRepository.basicPuzzleConfig
}
