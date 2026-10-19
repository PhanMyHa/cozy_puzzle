package com.example.cozypuzzle.data.remote.cache

import android.content.Context
import com.example.cozypuzzle.core.remote.RemoteDataConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataCache @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val cacheDirectory: File by lazy {
        File(context.cacheDir, "remote_puzzle_data").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private fun getCacheFile(key: String): File {
        val safeName = key.replace('/', '_').replace('\\', '_')
        return File(cacheDirectory, safeName)
    }

    /**
     * Synchronous retrieval of cached JSON string if present.
     */
    fun getCacheSync(key: String): String? {
        val file = getCacheFile(key)
        return if (file.exists() && file.isFile && file.length() > 0) {
            try {
                file.readText(Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Retrieves cached JSON string if present (asynchronous).
     */
    suspend fun getCache(key: String): String? = withContext(Dispatchers.IO) {
        getCacheSync(key)
    }

    /**
     * Determines whether the cached file exists and has not expired (synchronous).
     */
    fun isCacheValidSync(key: String): Boolean {
        val file = getCacheFile(key)
        if (!file.exists() || !file.isFile || file.length() == 0L) {
            return false
        }
        val age = System.currentTimeMillis() - file.lastModified()
        return age < RemoteDataConfig.CACHE_EXPIRY_MS
    }

    /**
     * Determines whether the cached file exists and has not expired.
     */
    suspend fun isCacheValid(key: String): Boolean = withContext(Dispatchers.IO) {
        isCacheValidSync(key)
    }

    /**
     * Saves JSON content atomically using a temporary file (synchronous).
     */
    fun saveCacheSync(key: String, content: String): Boolean {
        if (content.isBlank()) return false

        val targetFile = getCacheFile(key)
        val tempFile = File(cacheDirectory, "${targetFile.name}.tmp")

        return try {
            FileOutputStream(tempFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
            }
            if (tempFile.exists() && tempFile.length() > 0) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                tempFile.renameTo(targetFile)
            } else {
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            false
        }
    }

    /**
     * Saves JSON content atomically using a temporary file.
     * Guaranteed never to truncate or corrupt existing cache file on write errors.
     */
    suspend fun saveCache(key: String, content: String): Boolean = withContext(Dispatchers.IO) {
        saveCacheSync(key, content)
    }

    /**
     * Reads bundled sample JSON from assets/sample_remote_data/<relativePath> (synchronous).
     */
    fun loadFromAssetsSync(relativePath: String): String? {
        return try {
            val assetPath = "sample_remote_data/" + relativePath.removePrefix("/")
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Reads bundled sample JSON from assets/sample_remote_data/<relativePath>.
     * Used when remote is unconfigured or unreachable and disk cache is empty.
     */
    suspend fun loadFromAssets(relativePath: String): String? = withContext(Dispatchers.IO) {
        loadFromAssetsSync(relativePath)
    }

    /**
     * Clears all disk cache files.
     */
    suspend fun clearCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            cacheDirectory.listFiles()?.forEach { it.delete() }
            true
        } catch (e: Exception) {
            false
        }
    }
}
