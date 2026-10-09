package com.example.data.gemini

import com.example.BuildConfig
import com.example.data.security.FirebaseAppCheckManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * PRODUCTION SECURITY NOTICE (Phase 16–18):
 *
 * Current State:
 * The [BackendGeminiGateway] is the designated production gateway that connects to the
 * authenticated Firebase Cloud Function proxy. It attaches the Firebase App Check token
 * (X-Firebase-AppCheck) and user auth token, preventing direct client exposure of the
 * Gemini API key.
 *
 * The [DirectClientGeminiGateway] is restricted strictly for explicit local development
 * or test harness usage where BuildConfig.GEMINI_API_KEY is present.
 */
interface GeminiGateway {
    val isClientSideKeyExposed: Boolean

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String,
        historyContentsJson: JSONArray? = null
    ): Result<String>
}

/**
 * Production Gateway: Routes requests through the authenticated Firebase backend proxy.
 * Completely eliminates the client-side Gemini API key.
 * Transmits the Firebase App Check token (X-Firebase-AppCheck) and user authorization token.
 */
class BackendGeminiGateway(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val proxyEndpoint: String? = null,
    private val appCheckTokenProvider: (suspend () -> String?)? = { FirebaseAppCheckManager.getAppCheckToken() },
    private val authTokenProvider: (suspend () -> String?)? = null
) : GeminiGateway {

    override val isClientSideKeyExposed: Boolean = false

    override suspend fun generateContent(
        prompt: String,
        systemInstruction: String,
        historyContentsJson: JSONArray?
    ): Result<String> = withContext(Dispatchers.IO) {
        if (prompt.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Prompt must not be empty"))
        }
        if (prompt.length > 10000) {
            return@withContext Result.failure(IllegalArgumentException("Prompt exceeds maximum allowed size"))
        }

        val endpoint = proxyEndpoint
        if (endpoint.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Backend proxy endpoint not yet deployed"))
        }

        try {
            val appCheckToken = appCheckTokenProvider?.invoke()
            val authToken = authTokenProvider?.invoke()

            val payload = JSONObject().apply {
                put("prompt", prompt)
                put("systemInstruction", systemInstruction)
                if (historyContentsJson != null) {
                    put("contents", historyContentsJson)
                }
            }

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))

            if (!appCheckToken.isNullOrBlank()) {
                requestBuilder.header("X-Firebase-AppCheck", appCheckToken)
            }
            if (!authToken.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Proxy error: HTTP ${response.code}"))
            }

            val resStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response from proxy"))
            val resJson = JSONObject(resStr)
            val reply = resJson.optString("text")
            if (reply.isNotBlank()) {
                Result.success(reply.trim())
            } else {
                Result.failure(Exception("Malformed proxy response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Direct client-side implementation.
 * WARNING: Exposes BuildConfig.GEMINI_API_KEY inside the Android APK.
 * Strictly used for testing or optional local development bypass.
 */
class DirectClientGeminiGateway(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) : GeminiGateway {

    override val isClientSideKeyExposed: Boolean = true

    override suspend fun generateContent(
        prompt: String,
        systemInstruction: String,
        historyContentsJson: JSONArray?
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalStateException("GEMINI_API_KEY is not configured"))
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val contents = historyContentsJson ?: JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply { put(JSONObject().apply { put("text", prompt) }) })
                })
            }

            val payload = JSONObject().apply {
                put("contents", contents)
                val sysPart = JSONObject().apply { put("text", systemInstruction) }
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply { put(sysPart) })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 800)
                })
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}"))
            }

            val resStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
            val resJson = JSONObject(resStr)
            val candidates = resJson.optJSONArray("candidates")
            val firstCand = candidates?.optJSONObject(0)
            val content = firstCand?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Empty candidate text"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
