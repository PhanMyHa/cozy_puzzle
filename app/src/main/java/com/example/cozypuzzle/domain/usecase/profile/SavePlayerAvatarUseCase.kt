package com.example.cozypuzzle.domain.usecase.profile

import com.example.cozypuzzle.domain.repository.AvatarRepository
import com.example.cozypuzzle.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Imports a freshly picked avatar photo into app-private storage and makes
 * it the active profile picture. The previous photo file is cleaned up so
 * storage does not grow with every upload.
 */
class SavePlayerAvatarUseCase @Inject constructor(
    private val avatarRepository: AvatarRepository,
    private val playerRepository: PlayerRepository
) {

    /** @return true when the new avatar is stored and referenced by the profile. */
    suspend operator fun invoke(sourceUri: String): Boolean {
        val storedPath = avatarRepository.importAvatar(sourceUri) ?: return false
        playerRepository.updateAvatarUri(storedPath)
        // Keep the avatar folder tidy: drop every photo except the active one.
        avatarRepository.pruneAvatars(keepPath = storedPath)
        return true
    }
}
