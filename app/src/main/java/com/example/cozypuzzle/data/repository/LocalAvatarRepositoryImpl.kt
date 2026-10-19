package com.example.cozypuzzle.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.example.cozypuzzle.domain.repository.AvatarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copies picked photos into `files/avatars`. Files are named by timestamp so
 * successive uploads never clash and old ones can be pruned afterwards.
 */
@Singleton
class LocalAvatarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AvatarRepository {

    override suspend fun importAvatar(sourceUri: String): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val resolver = context.contentResolver
                val avatarDir = File(context.filesDir, AVATAR_DIR).apply { mkdirs() }
                val target = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
                resolver.openInputStream(sourceUri.toUri())?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext null
                if (target.length() == 0L) null else target.absolutePath
            }.getOrNull()
        }

    override suspend fun pruneAvatars(keepPath: String?) {
        withContext(Dispatchers.IO) {
            runCatching {
                val keep = keepPath?.let(::File)
                File(context.filesDir, AVATAR_DIR)
                    .listFiles()
                    ?.filter { it.absolutePath != keepPath }
                    ?.forEach(File::delete)
                keep?.let { if (!it.exists()) it.delete() }
            }
        }
    }

    private companion object {
        const val AVATAR_DIR = "avatars"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AvatarRepositoryModule {

    @Binds
    abstract fun bindAvatarRepository(
        impl: LocalAvatarRepositoryImpl
    ): AvatarRepository
}
