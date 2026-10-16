package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

object AiVoiceManager {

    private const val TAG = "AiVoiceManager"
    const val GOOGLE_TTS_ENGINE = "com.google.android.tts"

    // Default configuration values
    var speechRate: Float = 1.0f
    var voicePitch: Float = 1.0f
    var preferredStyle: String = "natural" // "natural", "female", "male"

    /**
     * Cleans text before speaking aloud so the TTS does NOT read formatting,
     * markdown asterisks, hashes, backticks, bullet characters, or raw URLs.
     */
    fun cleanTextForSpeech(rawText: String): String {
        return rawText
            // Remove markdown bold and italic
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("_{1,2}(.*?)_{1,2}"), "$1")
            // Remove code blocks and inline code
            .replace(Regex("`{1,3}(.*?)`{1,3}"), "$1")
            // Remove markdown headings (e.g. ### Title)
            .replace(Regex("(?m)^#+\\s*"), "")
            // Remove markdown links [title](url) -> title
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            // Remove standalone web URLs
            .replace(Regex("https?://\\S+"), "")
            // Remove bullet points and numbered list markers
            .replace(Regex("(?m)^[•\\-*\\d.]+\\s+"), "")
            // Replace technical symbols that sound awkward in speech
            .replace("→", " to ")
            .replace("•", " ")
            .replace("~", " ")
            .replace("|", " ")
            // Collapse multiple spaces and newlines
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Intelligently detects whether text is in Hindi (Devanagari or Roman Hindi/Hinglish)
     * or English, so the TTS engine selects the corresponding voice naturally.
     */
    fun detectLocale(text: String): Locale {
        // 1. Check for Devanagari script characters (\u0900 to \u097F)
        val hasDevanagari = text.any { it in '\u0900'..'\u097F' }
        if (hasDevanagari) {
            return Locale("hi", "IN")
        }

        // 2. Check for common Roman Hindi / Hinglish vocabulary markers
        val lower = text.lowercase()
        val hinglishWords = setOf(
            "hai", "hain", "karein", "karna", "aap", "tum", "yeh", "woh",
            "namaste", "kaise", "kya", "nahi", "accha", "bahut", "kaunsa",
            "batayein", "mujhe", "suno", "aaj", "ki", "ka", "ke", "ko",
            "par", "se", "mein", "liye", "bhi", "toh", "wala", "wali",
            "dost", "batao", "kaise", "kare", "hoga"
        )

        val words = lower.split(Regex("[^a-zA-Z]+")).filter { it.isNotBlank() }
        val matchingCount = words.count { it in hinglishWords }

        // If 2 or more Hinglish words are detected, prefer Indian Hindi voice
        return if (matchingCount >= 2) {
            Locale("hi", "IN")
        } else {
            Locale.US
        }
    }

    /**
     * Configures the best available natural/neural voice on the TTS engine
     * for the detected target locale and applied speech rate.
     */
    fun configureOptimalVoice(tts: TextToSpeech, targetLocale: Locale) {
        try {
            val availability = tts.isLanguageAvailable(targetLocale)
            if (availability == TextToSpeech.LANG_NOT_SUPPORTED || availability == TextToSpeech.LANG_MISSING_DATA) {
                Log.w(TAG, "Locale $targetLocale missing or unsupported, falling back to US English")
                tts.language = Locale.US
            } else {
                tts.language = targetLocale
            }

            // Attempt to pick a natural / high quality neural voice if available
            val voices: Set<Voice>? = tts.voices
            if (!voices.isNullOrEmpty()) {
                val matchingVoices = voices.filter { voice ->
                    voice.locale.language == targetLocale.language
                }

                if (matchingVoices.isNotEmpty()) {
                    val bestVoice = matchingVoices.maxByOrNull { voice ->
                        var score = 0
                        val name = voice.name.lowercase()
                        if (name.contains("neural")) score += 50
                        if (name.contains("natural")) score += 40
                        if (name.contains("network")) score += 30
                        if (name.contains("wavenet")) score += 45

                        // Filter/score by preferred style
                        when (preferredStyle) {
                            "female" -> if (name.contains("female") || name.contains("-f-") || name.contains("girl")) score += 35
                            "male" -> if (name.contains("male") || name.contains("-m-") || name.contains("boy")) score += 35
                        }

                        if (voice.quality >= Voice.QUALITY_HIGH) score += 20
                        if (voice.latency == Voice.LATENCY_NORMAL || voice.latency == Voice.LATENCY_LOW) score += 5
                        score
                    }

                    if (bestVoice != null) {
                        tts.voice = bestVoice
                        Log.d(TAG, "Selected TTS voice: ${bestVoice.name} for ${targetLocale.displayName}")
                    }
                }
            }

            tts.setPitch(voicePitch)
            tts.setSpeechRate(speechRate.coerceIn(0.7f, 1.4f))

        } catch (e: Exception) {
            Log.w(TAG, "Error configuring optimal voice: ${e.message}")
        }
    }

    /**
     * Speaks text using the provided TTS engine with audio cleanup and locale selection.
     */
    fun speakCleanText(
        tts: TextToSpeech,
        rawText: String,
        utteranceId: String = "pickai_voice_${System.currentTimeMillis()}"
    ) {
        val clean = cleanTextForSpeech(rawText)
        if (clean.isBlank()) return

        val locale = detectLocale(clean)
        configureOptimalVoice(tts, locale)

        tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Request audio focus for speaking
     */
    fun requestAudioFocus(audioManager: AudioManager, focusChangeListener: AudioManager.OnAudioFocusChangeListener): AudioFocusRequest? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            audioManager.requestAudioFocus(focusRequest)
            focusRequest
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            null
        }
    }

    /**
     * Abandon audio focus
     */
    fun abandonAudioFocus(audioManager: AudioManager, focusRequest: AudioFocusRequest?, focusChangeListener: AudioManager.OnAudioFocusChangeListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }
}
