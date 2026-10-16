package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

enum class VoiceErrorType {
    API_KEY_NOT_CONFIGURED,
    INVALID_API_KEY,
    NO_INTERNET,
    TIMEOUT,
    RATE_LIMIT,
    SERVER_ERROR,
    UNKNOWN_ERROR
}

sealed class VoiceApiResult {
    data class Success(val answer: String) : VoiceApiResult()
    data class Error(
        val errorType: VoiceErrorType,
        val userFriendlyMessage: String,
        val technicalDetail: String? = null
    ) : VoiceApiResult()
}

object GeminiVoiceService {

    private const val TAG = "GeminiVoiceService"

    // Official supported model for basic text and conversational tasks (gemini-3.5-flash)
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    // OkHttpClient with 60-second timeouts as mandated by gemini-api guidelines
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Checks if the Gemini API key is configured with a real value.
     */
    fun isApiKeyConfigured(): Boolean {
        val key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Calls Gemini 3.5 Flash API with user query.
     * Never silently falls back to canned answers.
     * Returns structured VoiceApiResult with specific error types and friendly Hindi/English messages.
     */
    suspend fun generateVoiceAnswer(query: String): VoiceApiResult = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext VoiceApiResult.Error(
                errorType = VoiceErrorType.UNKNOWN_ERROR,
                userFriendlyMessage = "Koi sawal detect nahi hua. Kripya microphone dabakar dobara bolein."
            )
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "GEMINI_API_KEY is not configured in Secrets panel")
            return@withContext VoiceApiResult.Error(
                errorType = VoiceErrorType.API_KEY_NOT_CONFIGURED,
                userFriendlyMessage = "Gemini API key configure nahi hai. AI Studio ke Secrets panel mein GEMINI_API_KEY add karein.",
                technicalDetail = "Missing BuildConfig.GEMINI_API_KEY"
            )
        }

        try {
            val url = "$BASE_URL?key=$apiKey"

            val systemInstruction = "You are PickAI, an expert AI tools companion and intelligent voice assistant. " +
                "The user speaks naturally in Hindi, Hinglish (Roman Hindi), or English. " +
                "Always respond in the same language and conversational style as the user. " +
                "Provide direct, highly accurate, and helpful answers in 2 to 3 concise sentences, optimal for natural spoken audio playback. " +
                "Do not use markdown bolding (**), asterisks, bullet points, hashes, or technical symbols in your output so it sounds completely natural when read aloud."

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val turnObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", cleanQuery) })
                        }
                        put("parts", partsArray)
                    }
                    put(turnObj)
                }
                put("contents", contentsArray)

                val systemObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    }
                    put("parts", partsArray)
                }
                put("systemInstruction", systemObj)

                val configObj = JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 500)
                }
                put("generationConfig", configObj)
            }

            val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseCode = response.code
            val responseBodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: $responseCode, body: $responseBodyString")

                return@withContext when (responseCode) {
                    400 -> {
                        val isInvalidKey = responseBodyString.contains("API_KEY_INVALID", ignoreCase = true) ||
                            responseBodyString.contains("API key not valid", ignoreCase = true)
                        if (isInvalidKey) {
                            VoiceApiResult.Error(
                                errorType = VoiceErrorType.INVALID_API_KEY,
                                userFriendlyMessage = "Gemini API key invalid hai. Kripya AI Studio Secrets panel mein valid key verify karein.",
                                technicalDetail = "HTTP 400: Invalid API Key"
                            )
                        } else {
                            VoiceApiResult.Error(
                                errorType = VoiceErrorType.UNKNOWN_ERROR,
                                userFriendlyMessage = "Request process nahi ho saki. Kripya dobara try karein.",
                                technicalDetail = "HTTP 400: $responseBodyString"
                            )
                        }
                    }
                    403 -> {
                        VoiceApiResult.Error(
                            errorType = VoiceErrorType.INVALID_API_KEY,
                            userFriendlyMessage = "Gemini API access denied. Kripya project API permissions check karein.",
                            technicalDetail = "HTTP 403: Forbidden"
                        )
                    }
                    429 -> {
                        VoiceApiResult.Error(
                            errorType = VoiceErrorType.RATE_LIMIT,
                            userFriendlyMessage = "Gemini API rate limit reach ho gayi hai. Kuch seconds baad dobara try karein.",
                            technicalDetail = "HTTP 429: Rate Limit Exceeded"
                        )
                    }
                    500, 502, 503, 504 -> {
                        VoiceApiResult.Error(
                            errorType = VoiceErrorType.SERVER_ERROR,
                            userFriendlyMessage = "Gemini server busy hai. Kripya Retry button dabayein.",
                            technicalDetail = "HTTP $responseCode: Server Error"
                        )
                    }
                    else -> {
                        VoiceApiResult.Error(
                            errorType = VoiceErrorType.UNKNOWN_ERROR,
                            userFriendlyMessage = "AI response prapt nahi ho saka (Error $responseCode). Dobara koshish karein.",
                            technicalDetail = "HTTP $responseCode"
                        )
                    }
                }
            }

            val responseJson = JSONObject(responseBodyString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext VoiceApiResult.Error(
                    errorType = VoiceErrorType.UNKNOWN_ERROR,
                    userFriendlyMessage = "AI se khali response aaya. Kripya doosra sawal poochein."
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts == null || parts.length() == 0) {
                return@withContext VoiceApiResult.Error(
                    errorType = VoiceErrorType.UNKNOWN_ERROR,
                    userFriendlyMessage = "AI response format anapekshit tha. Dobara try karein."
                )
            }

            val text = parts.getJSONObject(0).optString("text").trim()
            if (text.isBlank()) {
                return@withContext VoiceApiResult.Error(
                    errorType = VoiceErrorType.UNKNOWN_ERROR,
                    userFriendlyMessage = "AI response text blank hai."
                )
            }

            // Success
            VoiceApiResult.Success(answer = text)

        } catch (e: UnknownHostException) {
            Log.e(TAG, "No internet connection: ${e.message}")
            VoiceApiResult.Error(
                errorType = VoiceErrorType.NO_INTERNET,
                userFriendlyMessage = "Internet connection nahi mil raha hai. Kripya Wi-Fi ya mobile data check karein.",
                technicalDetail = e.message
            )
        } catch (e: ConnectException) {
            Log.e(TAG, "Connection failed: ${e.message}")
            VoiceApiResult.Error(
                errorType = VoiceErrorType.NO_INTERNET,
                userFriendlyMessage = "Server se connect nahi ho saka. Apna network check karein.",
                technicalDetail = e.message
            )
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Request timed out: ${e.message}")
            VoiceApiResult.Error(
                errorType = VoiceErrorType.TIMEOUT,
                userFriendlyMessage = "Request timeout ho gayi. Kripya Retry dabayein.",
                technicalDetail = e.message
            )
        } catch (e: IOException) {
            Log.e(TAG, "Network IO Exception: ${e.message}")
            VoiceApiResult.Error(
                errorType = VoiceErrorType.NO_INTERNET,
                userFriendlyMessage = "Network error: Connection toot gaya. Dobara try karein.",
                technicalDetail = e.message
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            VoiceApiResult.Error(
                errorType = VoiceErrorType.UNKNOWN_ERROR,
                userFriendlyMessage = "Ek anapekshit error aaya: ${e.localizedMessage ?: "Unknown"}",
                technicalDetail = e.message
            )
        }
    }
}
