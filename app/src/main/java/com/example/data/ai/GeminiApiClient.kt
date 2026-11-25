package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.domain.ShahgeePersonaSettings
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

class GeminiApiClient(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val httpClient: OkHttpClient = defaultHttpClient()
) {
    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && apiKey != PLACEHOLDER_KEY

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String,
        persona: ShahgeePersonaSettings,
        modelIdOverride: String? = null,
        temperatureOverride: Double? = null,
        topPOverride: Double? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("GEMINI_API_KEY is missing. Set it in the AI Studio Secrets panel.")
            )
        }

        val resolvedModelId = modelIdOverride?.takeIf { it.isNotBlank() } ?: persona.modelId
        val isGemmaEndpoint = resolvedModelId.startsWith("gemma", ignoreCase = true)

        val payload = JSONObject().apply {
            val effectiveUserPrompt = if (isGemmaEndpoint && systemInstruction.isNotBlank()) {
                "[SYSTEM INSTRUCTIONS]\n$systemInstruction\n\n[USER REQUEST]\n$prompt"
            } else {
                if (systemInstruction.isNotBlank()) {
                    put(
                        "systemInstruction",
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", systemInstruction))
                        )
                    )
                }
                prompt
            }

            put(
                "contents",
                JSONArray().put(
                    JSONObject().apply {
                        put("role", "user")
                        put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", effectiveUserPrompt))
                        )
                    }
                )
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", temperatureOverride ?: persona.temperature)
                    put("topP", topPOverride ?: persona.topP)
                    put("maxOutputTokens", persona.maxOutputTokens)
                }
            )
        }

        val endpoint = "$BASE_URL/models/$resolvedModelId:generateContent"
        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-goog-api-key", apiKey)
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val apiError = parseApiError(rawBody, response.code, resolvedModelId)
                    Log.e(TAG, "Google AI API error HTTP ${response.code} ($resolvedModelId): $apiError")
                    return@withContext Result.failure(IOException(apiError))
                }

                val text = extractCandidateText(rawBody)
                if (text.isNullOrBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("$resolvedModelId returned an empty response.")
                    )
                }
                Result.success(text.trim())
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error calling Google AI API ($resolvedModelId)", e)
            Result.failure(IOException("Network error contacting $resolvedModelId: ${e.localizedMessage ?: "check connection"}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected Google AI client failure", e)
            Result.failure(e)
        }
    }

    private fun extractCandidateText(rawJson: String): String? {
        val root = JSONObject(rawJson)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        return buildString {
            for (i in 0 until parts.length()) {
                val partText = parts.optJSONObject(i)?.optString("text").orEmpty()
                if (partText.isNotEmpty()) {
                    if (isNotEmpty()) append('\n')
                    append(partText)
                }
            }
        }
    }

    private fun parseApiError(rawJson: String, statusCode: Int, modelId: String): String {
        val message = runCatching {
            JSONObject(rawJson).optJSONObject("error")?.optString("message")
        }.getOrNull()
        return if (!message.isNullOrBlank()) {
            "$modelId ($statusCode): $message"
        } else {
            "$modelId request failed with HTTP $statusCode."
        }
    }

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private const val PLACEHOLDER_KEY = "MY_GEMINI_API_KEY"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
