package com.example.cozypuzzle.domain.usecase.profile

import com.example.cozypuzzle.domain.model.Player
import com.example.cozypuzzle.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {

    operator fun invoke(): Flow<Player> = playerRepository.observePlayer()
}
