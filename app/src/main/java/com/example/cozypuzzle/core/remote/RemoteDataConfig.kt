package com.example.cozypuzzle.core.remote

/**
 * Single source of configuration for the Cozy Puzzle GitHub Remote JSON system.
 *
 * To connect to your real GitHub repository:
 * 1. Create a repository named `cozy-puzzle-data` (or your preferred name) on GitHub.
 * 2. Upload the sample JSON files from `docs/sample_remote_data/cozy-puzzle-data/`.
 * 3. Replace [PLACEHOLDER_USERNAME] below with your real GitHub username.
 *
 * Example real URL:
 * `https://raw.githubusercontent.com/octocat/cozy-puzzle-data/main/`
 */
object RemoteDataConfig {

    const val PLACEHOLDER_USERNAME = "PLACEHOLDER_USER"
    const val DEFAULT_REPO_NAME = "cozy-puzzle-data"
    const val DEFAULT_BRANCH = "main"

    /**
     * Base URL pointing to the raw content root of the GitHub repository.
     * Keep the trailing slash.
     */
    @Volatile
    var rawBaseUrl: String = "https://raw.githubusercontent.com/$PLACEHOLDER_USERNAME/$DEFAULT_REPO_NAME/$DEFAULT_BRANCH/"

    /**
     * Relative path from the base URL to the root manifest file.
     */
    const val MANIFEST_PATH: String = "manifest.json"

    /**
     * HTTP connect timeout in milliseconds.
     */
    const val CONNECT_TIMEOUT_MS: Int = 10_000

    /**
     * HTTP read timeout in milliseconds.
     */
    const val READ_TIMEOUT_MS: Int = 10_000

    /**
     * Disk cache validity duration (24 hours).
     */
    const val CACHE_EXPIRY_MS: Long = 24 * 60 * 60 * 1000L

    /**
     * Checks if the current base URL is still using the placeholder username.
     * When true, remote HTTP fetch is bypassed and local fallback/cache is used safely.
     */
    fun isPlaceholderConfigured(): Boolean {
        val trimmed = rawBaseUrl.trim()
        return trimmed.isEmpty() || trimmed.contains(PLACEHOLDER_USERNAME) || !trimmed.startsWith("https://")
    }

    /**
     * Resolves the full URL for a given relative path.
     */
    fun resolveUrl(relativePath: String): String {
        val base = if (rawBaseUrl.endsWith("/")) rawBaseUrl else "$rawBaseUrl/"
        val cleanPath = relativePath.removePrefix("/")
        return "$base$cleanPath"
    }

    /**
     * Resets to default placeholder configuration (useful for unit tests).
     */
    fun resetToDefaults() {
        rawBaseUrl = "https://raw.githubusercontent.com/$PLACEHOLDER_USERNAME/$DEFAULT_REPO_NAME/$DEFAULT_BRANCH/"
    }
}
