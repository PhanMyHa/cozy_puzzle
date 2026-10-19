package com.example.cozypuzzle.domain.usecase.profile

import com.example.cozypuzzle.domain.model.Player
import com.example.cozypuzzle.domain.repository.PlayerRepository
import javax.inject.Inject

/**
 * Updates the display name shown on the profile screen.
 */
class UpdatePlayerNameUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {

    /** @return true when the name was accepted and persisted. */
    suspend operator fun invoke(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_NAME_LENGTH) return false
        playerRepository.updatePlayerName(trimmed)
        return true
    }

    private companion object {
        const val MAX_NAME_LENGTH = 24
    }
}
