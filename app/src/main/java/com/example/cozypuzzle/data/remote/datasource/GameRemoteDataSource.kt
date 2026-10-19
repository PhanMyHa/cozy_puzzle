package com.example.cozypuzzle.data.remote.datasource

import android.util.Log
import com.example.cozypuzzle.core.catpuzzle.model.CatPuzzleLevel
import com.example.cozypuzzle.core.colorthecat.model.ColorCatLevel
import com.example.cozypuzzle.core.findpair.model.FindPairLevel
import com.example.cozypuzzle.core.remote.RemoteDataConfig
import com.example.cozypuzzle.core.slidethecat.model.SlideCatLevel
import com.example.cozypuzzle.data.remote.cache.RemoteDataCache
import com.example.cozypuzzle.data.remote.client.RemoteJsonClient
import com.example.cozypuzzle.data.remote.dto.GameConfigDto
import com.example.cozypuzzle.data.remote.dto.ManifestDto
import com.example.cozypuzzle.data.remote.mapper.RemoteDataMappers.toDomain
import com.example.cozypuzzle.data.remote.parser.RemoteDataParsers
import com.example.cozypuzzle.domain.model.mycats.CatModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRemoteDataSource @Inject constructor(
    private val client: RemoteJsonClient,
    private val cache: RemoteDataCache
) {

    private val lock = Any()

    @Volatile
    private var cachedManifest: ManifestDto? = null

    @Volatile
    private var cachedGameConfig: GameConfigDto? = null

    @Volatile
    private var cachedCatPuzzleLevels: List<CatPuzzleLevel>? = null

    @Volatile
    private var cachedColorCatLevels: List<ColorCatLevel>? = null

    @Volatile
    private var cachedSlideCatLevels: List<SlideCatLevel>? = null

    @Volatile
    private var cachedFindPairLevels: List<FindPairLevel>? = null

    @Volatile
    private var cachedCats: List<CatModel>? = null

    /**
     * Synchronously returns Cat Puzzle levels from memory, disk cache, or bundled assets.
     */
    fun getCatPuzzleLevels(): List<CatPuzzleLevel> {
        cachedCatPuzzleLevels?.let { return it }
        synchronized(lock) {
            cachedCatPuzzleLevels?.let { return it }
            val levels = loadCatPuzzleLevelsInternal()
            cachedCatPuzzleLevels = levels
            return levels
        }
    }

    /**
     * Asynchronously returns Cat Puzzle levels, optionally fetching the latest version from remote.
     */
    suspend fun getCatPuzzleLevelsAsync(forceRefresh: Boolean = false): List<CatPuzzleLevel> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            cachedCatPuzzleLevels?.let { return@withContext it }
        }
        val levels = loadCatPuzzleLevelsInternal(forceRefresh)
        synchronized(lock) {
            cachedCatPuzzleLevels = levels
        }
        levels
    }

    /**
     * Synchronously returns Color the Cat levels from memory, disk cache, or bundled assets.
     */
    fun getColorCatLevels(): List<ColorCatLevel> {
        cachedColorCatLevels?.let { return it }
        synchronized(lock) {
            cachedColorCatLevels?.let { return it }
            val levels = loadColorCatLevelsInternal()
            cachedColorCatLevels = levels
            return levels
        }
    }

    /**
     * Asynchronously returns Color the Cat levels.
     */
    suspend fun getColorCatLevelsAsync(forceRefresh: Boolean = false): List<ColorCatLevel> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            cachedColorCatLevels?.let { return@withContext it }
        }
        val levels = loadColorCatLevelsInternal(forceRefresh)
        synchronized(lock) {
            cachedColorCatLevels = levels
        }
        levels
    }

    /**
     * Synchronously returns Slide the Cat levels from memory, disk cache, or bundled assets.
     */
    fun getSlideCatLevels(): List<SlideCatLevel> {
        cachedSlideCatLevels?.let { return it }
        synchronized(lock) {
            cachedSlideCatLevels?.let { return it }
            val levels = loadSlideCatLevelsInternal()
            cachedSlideCatLevels = levels
            return levels
        }
    }

    /**
     * Asynchronously returns Slide the Cat levels.
     */
    suspend fun getSlideCatLevelsAsync(forceRefresh: Boolean = false): List<SlideCatLevel> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            cachedSlideCatLevels?.let { return@withContext it }
        }
        val levels = loadSlideCatLevelsInternal(forceRefresh)
        synchronized(lock) {
            cachedSlideCatLevels = levels
        }
        levels
    }

    /**
     * Synchronously returns Find the Pair levels from memory, disk cache, or bundled assets.
     */
    fun getFindPairLevels(): List<FindPairLevel> {
        cachedFindPairLevels?.let { return it }
        synchronized(lock) {
            cachedFindPairLevels?.let { return it }
            val levels = loadFindPairLevelsInternal()
            cachedFindPairLevels = levels
            return levels
        }
    }

    /**
     * Asynchronously returns Find the Pair levels.
     */
    suspend fun getFindPairLevelsAsync(forceRefresh: Boolean = false): List<FindPairLevel> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            cachedFindPairLevels?.let { return@withContext it }
        }
        val levels = loadFindPairLevelsInternal(forceRefresh)
        synchronized(lock) {
            cachedFindPairLevels = levels
        }
        levels
    }

    /**
     * Synchronously returns the Cat collection catalog from memory, disk cache, or bundled assets.
     */
    fun getCats(): List<CatModel> {
        cachedCats?.let { return it }
        synchronized(lock) {
            cachedCats?.let { return it }
            val cats = loadCatsInternal()
            cachedCats = cats
            return cats
        }
    }

    /**
     * Asynchronously returns the Cat collection catalog.
     */
    suspend fun getCatsAsync(forceRefresh: Boolean = false): List<CatModel> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            cachedCats?.let { return@withContext it }
        }
        val cats = loadCatsInternal(forceRefresh)
        synchronized(lock) {
            cachedCats = cats
        }
        cats
    }

    /**
     * Synchronously returns Game Config.
     */
    fun getGameConfig(): GameConfigDto {
        cachedGameConfig?.let { return it }
        synchronized(lock) {
            cachedGameConfig?.let { return it }
            val config = loadGameConfigInternal()
            cachedGameConfig = config
            return config
        }
    }

    /**
     * Synchronizes all data files from the GitHub repository based on manifest.json.
     * Returns true if remote data was successfully updated, or false if bypassed (e.g. placeholder URL).
     */
    suspend fun syncRemoteData(): Result<Boolean> = withContext(Dispatchers.IO) {
        if (RemoteDataConfig.isPlaceholderConfigured()) {
            Log.d(TAG, "Placeholder URL detected, skipping remote network fetch.")
            return@withContext Result.success(false)
        }

        try {
            val manifestUrl = RemoteDataConfig.resolveUrl(RemoteDataConfig.MANIFEST_PATH)
            val manifestResult = client.fetchJson(manifestUrl)
            val manifestJson = manifestResult.getOrThrow()
            val manifest = RemoteDataParsers.parseManifest(manifestJson)

            // Save manifest to disk cache
            cache.saveCache(RemoteDataConfig.MANIFEST_PATH, manifestJson)
            cachedManifest = manifest

            // Synchronize each declared file
            manifest.files.forEach { (fileKey, relativePath) ->
                try {
                    val fileUrl = RemoteDataConfig.resolveUrl(relativePath)
                    val contentResult = client.fetchJson(fileUrl)
                    val contentJson = contentResult.getOrNull()
                    if (contentJson != null) {
                        // Validate content before saving cache
                        when (fileKey) {
                            "game_config" -> {
                                val parsed = RemoteDataParsers.parseGameConfig(contentJson)
                                cache.saveCache(relativePath, contentJson)
                                cachedGameConfig = parsed
                            }
                            "cat_puzzle" -> {
                                val parsed = RemoteDataParsers.parseCatPuzzleLevels(contentJson).map { it.toDomain() }
                                cache.saveCache(relativePath, contentJson)
                                cachedCatPuzzleLevels = parsed
                            }
                            "color_the_cat" -> {
                                val parsed = RemoteDataParsers.parseColorCatLevels(contentJson).map { it.toDomain() }
                                cache.saveCache(relativePath, contentJson)
                                cachedColorCatLevels = parsed
                            }
                            "slide_the_cat" -> {
                                val parsed = RemoteDataParsers.parseSlideCatLevels(contentJson).map { it.toDomain() }
                                cache.saveCache(relativePath, contentJson)
                                cachedSlideCatLevels = parsed
                            }
                            "find_the_pair" -> {
                                val parsed = RemoteDataParsers.parseFindPairLevels(contentJson).map { it.toDomain() }
                                cache.saveCache(relativePath, contentJson)
                                cachedFindPairLevels = parsed
                            }
                            "cats" -> {
                                val parsed = RemoteDataParsers.parseCats(contentJson).map { it.toDomain() }
                                cache.saveCache(relativePath, contentJson)
                                cachedCats = parsed
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync remote file $fileKey ($relativePath): ${e.message}")
                    // Crucial: continue syncing other files and never overwrite valid cache with empty data
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync remote manifest: ${e.message}")
            Result.failure(e)
        }
    }

    private fun loadCatPuzzleLevelsInternal(forceRemote: Boolean = false): List<CatPuzzleLevel> {
        val path = "levels/cat_puzzle.json"
        val rawJson = loadRawJsonWithFallback(path, forceRemote)
        return if (rawJson != null) {
            try {
                RemoteDataParsers.parseCatPuzzleLevels(rawJson).map { it.toDomain() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse cat_puzzle JSON from cache, falling back to bundled", e)
                loadFromAssetsDirect(path)?.let {
                    RemoteDataParsers.parseCatPuzzleLevels(it).map { dto -> dto.toDomain() }
                } ?: emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun loadColorCatLevelsInternal(forceRemote: Boolean = false): List<ColorCatLevel> {
        val path = "levels/color_the_cat.json"
        val rawJson = loadRawJsonWithFallback(path, forceRemote)
        return if (rawJson != null) {
            try {
                RemoteDataParsers.parseColorCatLevels(rawJson).map { it.toDomain() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse color_the_cat JSON from cache, falling back to bundled", e)
                loadFromAssetsDirect(path)?.let {
                    RemoteDataParsers.parseColorCatLevels(it).map { dto -> dto.toDomain() }
                } ?: emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun loadSlideCatLevelsInternal(forceRemote: Boolean = false): List<SlideCatLevel> {
        val path = "levels/slide_the_cat.json"
        val rawJson = loadRawJsonWithFallback(path, forceRemote)
        return if (rawJson != null) {
            try {
                RemoteDataParsers.parseSlideCatLevels(rawJson).map { it.toDomain() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse slide_the_cat JSON from cache, falling back to bundled", e)
                loadFromAssetsDirect(path)?.let {
                    RemoteDataParsers.parseSlideCatLevels(it).map { dto -> dto.toDomain() }
                } ?: emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun loadFindPairLevelsInternal(forceRemote: Boolean = false): List<FindPairLevel> {
        val path = "levels/find_the_pair.json"
        val rawJson = loadRawJsonWithFallback(path, forceRemote)
        return if (rawJson != null) {
            try {
                RemoteDataParsers.parseFindPairLevels(rawJson).map { it.toDomain() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse find_the_pair JSON from cache, falling back to bundled", e)
                loadFromAssetsDirect(path)?.let {
                    RemoteDataParsers.parseFindPairLevels(it).map { dto -> dto.toDomain() }
                } ?: emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun loadCatsInternal(forceRemote: Boolean = false): List<CatModel> {
        val path = "collections/cats.json"
        val rawJson = loadRawJsonWithFallback(path, forceRemote)
        return if (rawJson != null) {
            try {
                RemoteDataParsers.parseCats(rawJson).map { it.toDomain() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse cats JSON from cache, falling back to bundled", e)
                loadFromAssetsDirect(path)?.let {
                    RemoteDataParsers.parseCats(it).map { dto -> dto.toDomain() }
                } ?: emptyList()
            }
        } else {
            emptyList()
        }
    }

    private fun loadGameConfigInternal(): GameConfigDto {
        val path = "config/game_config.json"
        val rawJson = loadRawJsonWithFallback(path, false)
        return if (rawJson != null) {
            try {
                RemoteDataParsers.parseGameConfig(rawJson)
            } catch (e: Exception) {
                loadFromAssetsDirect(path)?.let {
                    RemoteDataParsers.parseGameConfig(it)
                } ?: GameConfigDto(schemaVersion = 1)
            }
        } else {
            GameConfigDto(schemaVersion = 1)
        }
    }

    /**
     * Resolves raw JSON through the cascade: Disk Cache -> Bundled Asset.
     */
    private fun loadRawJsonWithFallback(relativePath: String, forceRemote: Boolean): String? {
        // 1. Check disk cache
        val diskCache = cache.getCacheSync(relativePath)
        if (diskCache != null && diskCache.isNotBlank()) {
            return diskCache
        }

        // 2. Fall back to bundled assets
        val bundledAsset = cache.loadFromAssetsSync(relativePath)
        if (bundledAsset != null && bundledAsset.isNotBlank()) {
            // Populate disk cache so subsequent reads are immediate
            cache.saveCacheSync(relativePath, bundledAsset)
            return bundledAsset
        }

        return null
    }

    private fun loadFromAssetsDirect(relativePath: String): String? {
        return cache.loadFromAssetsSync(relativePath)
    }

    companion object {
        private const val TAG = "GameRemoteDataSource"
    }
}
