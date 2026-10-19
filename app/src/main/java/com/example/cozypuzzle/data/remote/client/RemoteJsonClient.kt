package com.example.cozypuzzle.data.remote.client

import com.example.cozypuzzle.core.remote.RemoteDataConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

class RemoteConfigNotConfiguredException(message: String) : Exception(message)
class RemoteHttpException(val statusCode: Int, message: String) : Exception(message)

@Singleton
class RemoteJsonClient @Inject constructor() {

    /**
     * Fetches a raw JSON document from a secure HTTPS URL.
     * Safely detects placeholder repositories and skips network calls.
     */
    suspend fun fetchJson(urlString: String): Result<String> = withContext(Dispatchers.IO) {
        // If placeholder URL is detected, fail immediately without opening network connections
        if (RemoteDataConfig.isPlaceholderConfigured() || urlString.contains(RemoteDataConfig.PLACEHOLDER_USERNAME)) {
            return@withContext Result.failure(
                RemoteConfigNotConfiguredException(
                    "Remote URL uses placeholder '${RemoteDataConfig.PLACEHOLDER_USERNAME}'. Configure a real GitHub repository in RemoteDataConfig."
                )
            )
        }

        if (!urlString.startsWith("https://", ignoreCase = true)) {
            return@withContext Result.failure(
                IllegalArgumentException("Insecure URL rejected. Cozy Puzzle requires HTTPS: $urlString")
            )
        }

        var connection: HttpsURLConnection? = null
        try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = RemoteDataConfig.CONNECT_TIMEOUT_MS
                readTimeout = RemoteDataConfig.READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("User-Agent", "CozyPuzzle-Android-Client")
                instanceFollowRedirects = true
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val stream = connection.inputStream
                val content = stream.bufferedReader(Charsets.UTF_8).use(BufferedReader::readText)
                if (content.isBlank()) {
                    Result.failure(IOException("Received empty response from $urlString"))
                } else {
                    Result.success(content)
                }
            } else {
                Result.failure(
                    RemoteHttpException(
                        statusCode = responseCode,
                        message = "HTTP request to $urlString failed with status $responseCode: ${connection.responseMessage}"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
