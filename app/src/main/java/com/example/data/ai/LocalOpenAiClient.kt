package com.example.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Lightweight client for local/self-hosted Ollama, LM Studio, or OpenAI-compatible servers
 * running Gemma, Dolphin, or Qwen 2.5 models.
 */
class LocalOpenAiClient(
    private val httpClient: OkHttpClient = defaultHttpClient()
) {
    suspend fun discoverModels(baseUrl: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val normalized = normalizeBaseUrl(baseUrl)
            ?: return@withContext Result.failure(IllegalArgumentException("Enter a valid http(s):// host URL."))

        // 1. Try OpenAI-compatible /v1/models first (supported by Ollama >=0.1.24, LM Studio, vLLM)
        val v1ModelsResult = fetchModelNames("$normalized/v1/models", isOllamaTags = false)
        if (v1ModelsResult.isSuccess) {
            return@withContext v1ModelsResult
        }

        // 2. Fallback to native Ollama /api/tags
        fetchModelNames("$normalized/api/tags", isOllamaTags = true)
    }

    suspend fun chatCompletion(
        baseUrl: String,
        modelTag: String,
        systemInstruction: String,
        prompt: String,
        temperature: Double,
        topP: Double
    ): Result<String> = withContext(Dispatchers.IO) {
        val normalized = normalizeBaseUrl(baseUrl)
            ?: return@withContext Result.failure(IllegalArgumentException("Local inference server URL is invalid."))

        val messages = JSONArray().apply {
            if (systemInstruction.isNotBlank()) {
                put(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    }
                )
            }
            put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }
            )
        }

        val payload = JSONObject().apply {
            put("model", modelTag)
            put("messages", messages)
            put("temperature", temperature)
            put("top_p", topP)
            put("stream", false)
        }

        val request = Request.Builder()
            .url("$normalized/v1/chat/completions")
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Local model $modelTag returned HTTP ${response.code}.")
                    )
                }
                val root = JSONObject(rawBody)
                val choices = root.optJSONArray("choices")
                val text = choices
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    ?.trim()
                if (text.isNullOrBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("Local model $modelTag returned an empty message.")
                    )
                }
                Result.success(text)
            }
        } catch (e: IOException) {
            Result.failure(IOException("Unable to reach $normalized ($modelTag): ${e.localizedMessage ?: "connection error"}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchModelNames(url: String, isOllamaTags: Boolean): Result<List<String>> {
        val request = Request.Builder().url(url).get().build()
        return try {
            httpClient.newCall(request).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IOException("HTTP ${response.code} from $url"))
                }
                val root = JSONObject(raw)
                val arrayKey = if (isOllamaTags) "models" else "data"
                val idKey = if (isOllamaTags) "name" else "id"
                val arr = root.optJSONArray(arrayKey) ?: JSONArray()
                val names = buildList {
                    for (i in 0 until arr.length()) {
                        val id = arr.optJSONObject(i)?.optString(idKey).orEmpty().trim()
                        if (id.isNotEmpty()) add(id)
                    }
                }
                Result.success(names)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeBaseUrl(rawUrl: String): String? {
        val trimmed = rawUrl.trim().trimEnd('/')
        if (trimmed.isBlank()) return null
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) return null
        return trimmed
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}
