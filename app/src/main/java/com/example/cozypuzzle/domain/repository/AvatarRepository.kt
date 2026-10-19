package com.example.cozypuzzle.domain.repository

/**
 * Stores profile avatar images inside app-private storage so they survive
 * the temporary lifetime of gallery/photo-picker content URIs.
 */
interface AvatarRepository {

    /**
     * Copies the image behind [sourceUri] into private storage.
     *
     * @return the persisted local file path, or null when the source could
     * not be read (missing permissions, deleted image, ...).
     */
    suspend fun importAvatar(sourceUri: String): String?

    /** Deletes every stored avatar except [keepPath]; safe to call with null. */
    suspend fun pruneAvatars(keepPath: String?)
}
