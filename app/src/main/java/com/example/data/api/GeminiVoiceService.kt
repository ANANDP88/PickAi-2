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
import java.util.concurrent.TimeUnit

object GeminiVoiceService {

    private const val TAG = "GeminiVoiceService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Calls Gemini 3.5 Flash API with user query.
     * Returns null if key is not configured or network call fails, allowing local smart fallback.
     */
    suspend fun generateVoiceAnswer(query: String): String? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Gemini API key is not set or default. Using smart local assistant.")
            return@withContext null
        }

        try {
            val url = "$BASE_URL?key=$apiKey"

            val systemInstruction = "You are PickAI, an expert AI tools companion and voice assistant. " +
                "The user asks questions via voice speech in Hindi, Hinglish, or English. " +
                "Respond in the same language as the user. " +
                "Provide direct, concise, and helpful advice (2 to 3 sentences maximum), ideal for text-to-speech audio playback. " +
                "Recommend specific tools available today, explain concepts simply, and keep a friendly tone."

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val turnObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", query) })
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
                }
                put("generationConfig", configObj)
            }

            val requestBody = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API call failed with HTTP code: ${response.code}")
                return@withContext null
            }

            val responseString = response.body?.string() ?: return@withContext null
            val responseJson = JSONObject(responseString)

            val candidates = responseJson.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            if (parts.length() == 0) return@withContext null

            val text = parts.getJSONObject(0).optString("text")
            if (text.isNotBlank()) {
                text.trim()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            null
        }
    }
}
