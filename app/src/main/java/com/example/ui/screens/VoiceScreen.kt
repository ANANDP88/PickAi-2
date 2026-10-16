package com.example.ui.screens

import android.app.Activity
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import com.example.util.AiVoiceManager
import com.example.util.ShareHelper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.VoiceAssistantState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Full-screen Voice Assistant Screen inspired by modern fluid voice modes (ChatGPT Voice style),
 * featuring a continuous, hypnotic circular waveform voice animation in one spot,
 * matching the app's purple and blue color palette without uneven high and low waves.
 * Zero ChatGPT logo/branding is used.
 */
@Composable
fun VoiceScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val voiceState = viewModel.voiceState
    val isTtsSpeaking = viewModel.isTtsSpeaking

    var speechRecognizer: SpeechRecognizer? by remember { mutableStateOf(null) }
    var currentSpokenText by remember { mutableStateOf("") }
    var silenceTimerJob by remember { mutableStateOf<Job?>(null) }
    var maxTimeoutJob by remember { mutableStateOf<Job?>(null) }
    var noAudioDetectedTimer by remember { mutableStateOf<Job?>(null) }
    var showVirtualMicHelper by remember { mutableStateOf(false) }

    var isKeyboardModeOpen by remember { mutableStateOf(false) }
    var textInputQuery by remember { mutableStateOf("") }

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Initialize TextToSpeech engine with Google TTS natural voices
    DisposableEffect(Unit) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context, { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                ttsInstance?.let {
                    AiVoiceManager.configureOptimalVoice(it, Locale("hi", "IN"))
                }
            } else {
                // Fallback to standard system engine
                val fallbackTts = TextToSpeech(context) { fbStatus ->
                    if (fbStatus == TextToSpeech.SUCCESS) {
                        isTtsReady = true
                    }
                }
                tts = fallbackTts
            }
        }, AiVoiceManager.GOOGLE_TTS_ENGINE)

        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                viewModel.isTtsSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                viewModel.isTtsSpeaking = false
            }

            override fun onError(utteranceId: String?) {
                viewModel.isTtsSpeaking = false
            }
        })
        tts = ttsInstance

        onDispose {
            silenceTimerJob?.cancel()
            maxTimeoutJob?.cancel()
            noAudioDetectedTimer?.cancel()
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            tts?.stop()
            tts?.shutdown()
            viewModel.resetVoiceState()
        }
    }

    // Function to speak aloud natural response without markdown or URLs
    fun speakAloud(text: String) {
        val clean = AiVoiceManager.cleanTextForSpeech(text)
        if (clean.isNotBlank() && isTtsReady && tts != null) {
            tts?.stop()
            viewModel.isTtsSpeaking = true
            val locale = AiVoiceManager.detectLocale(clean)
            AiVoiceManager.configureOptimalVoice(tts!!, locale)
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, params, "pickai_voice_${System.currentTimeMillis()}")
        }
    }

    fun stopAudioPlayback() {
        tts?.stop()
        viewModel.isTtsSpeaking = false
    }

    // Process spoken or entered query with Gemini API
    fun stopListeningAndProcess(query: String) {
        silenceTimerJob?.cancel()
        maxTimeoutJob?.cancel()
        noAudioDetectedTimer?.cancel()
        showVirtualMicHelper = false

        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}

        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            viewModel.updateVoiceState(
                VoiceAssistantState.Error("Aawaz detect nahi hui. Neeche diye suggestions ya keyboard se try karein.")
            )
            return
        }

        viewModel.processVoiceInputWithGemini(cleanQuery) { answer ->
            speakAloud(answer)
        }
    }

    fun stopListeningManually() {
        silenceTimerJob?.cancel()
        maxTimeoutJob?.cancel()
        noAudioDetectedTimer?.cancel()
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}

        if (currentSpokenText.isNotBlank()) {
            stopListeningAndProcess(currentSpokenText)
        } else {
            viewModel.resetVoiceState()
        }
    }

    // Standard Android System Speech Recognition Dialog Launcher
    val systemVoiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = spokenMatches?.firstOrNull()?.trim() ?: ""
            if (spoken.isNotBlank()) {
                stopListeningAndProcess(spoken)
            }
        }
    }

    fun launchSystemVoiceDialog() {
        stopAudioPlayback()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "hi-IN", "en-IN"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "PickAI ko batayein: Kuch bhi poochein...")
        }
        try {
            systemVoiceLauncher.launch(intent)
        } catch (e: Exception) {
            viewModel.updateVoiceState(
                VoiceAssistantState.Error("System voice dialog start nahi ho saka. Microphone button ya text use karein.")
            )
        }
    }

    fun startListening() {
        stopAudioPlayback()
        silenceTimerJob?.cancel()
        maxTimeoutJob?.cancel()
        noAudioDetectedTimer?.cancel()
        currentSpokenText = ""
        showVirtualMicHelper = false

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            viewModel.updateVoiceState(
                VoiceAssistantState.Error(
                    message = "Device par Speech Recognition uplabdh nahi hai. System Dialog ya Keyboard input mode use karein.",
                    canRetry = true,
                    failedQuery = ""
                )
            )
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                // Multi-language recognition (Hindi, Hinglish, and English)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US"))
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    viewModel.updateVoiceState(
                        VoiceAssistantState.Listening(partialText = "Listening... Bolna shuru karein", rmsLevel = 1f)
                    )
                }

                override fun onBeginningOfSpeech() {
                    noAudioDetectedTimer?.cancel()
                    showVirtualMicHelper = false
                    viewModel.updateVoiceState(
                        VoiceAssistantState.Listening(partialText = "PickAI is listening...", rmsLevel = 3f)
                    )
                }

                override fun onRmsChanged(rmsdB: Float) {
                    if (viewModel.voiceState is VoiceAssistantState.Listening) {
                        val rms = (rmsdB / 2f).coerceIn(1f, 10f)
                        if (rms > 1.8f) {
                            noAudioDetectedTimer?.cancel()
                            showVirtualMicHelper = false
                        }
                        viewModel.updateVoiceState(
                            VoiceAssistantState.Listening(partialText = currentSpokenText, rmsLevel = rms)
                        )
                    }
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    silenceTimerJob?.cancel()
                    silenceTimerJob = scope.launch {
                        delay(600)
                        if (currentSpokenText.isNotBlank()) {
                            stopListeningAndProcess(currentSpokenText)
                        }
                    }
                }

                override fun onError(error: Int) {
                    silenceTimerJob?.cancel()
                    maxTimeoutJob?.cancel()
                    noAudioDetectedTimer?.cancel()

                    if (currentSpokenText.isNotBlank()) {
                        stopListeningAndProcess(currentSpokenText)
                    } else {
                        showVirtualMicHelper = true
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Kuch saaf nahi sunai diya. Kripya thoda paas aakar saaf bolein."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Aawaz detect nahi hui. Kripya Retry button dabayein aur bolein."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording mein problem aayi. Microphone check karein."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission grant nahi hai. Settings se allow karein."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Voice recognition ke liye internet connection zaroori hai."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy hai. Ek second ruk kar try karein."
                            else -> "Voice recognition error ($error). Kripya Retry dabayein."
                        }
                        viewModel.updateVoiceState(
                            VoiceAssistantState.Error(
                                message = errorMsg,
                                canRetry = true,
                                failedQuery = ""
                            )
                        )
                    }
                }

                override fun onResults(results: Bundle?) {
                    silenceTimerJob?.cancel()
                    maxTimeoutJob?.cancel()
                    noAudioDetectedTimer?.cancel()
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()?.trim() ?: currentSpokenText
                    if (spoken.isNotBlank()) {
                        stopListeningAndProcess(spoken)
                    } else {
                        showVirtualMicHelper = true
                        viewModel.updateVoiceState(
                            VoiceAssistantState.Error(
                                message = "Aawaz detect nahi hui. Kripya Retry dabayein aur bolein.",
                                canRetry = true,
                                failedQuery = ""
                            )
                        )
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = partialMatches?.firstOrNull()?.trim() ?: ""
                    if (partial.isNotBlank()) {
                        noAudioDetectedTimer?.cancel()
                        showVirtualMicHelper = false
                        currentSpokenText = partial
                        viewModel.updateVoiceState(
                            VoiceAssistantState.Listening(partialText = partial, rmsLevel = 3f)
                        )

                        // Auto-stop on 1.2s silence
                        silenceTimerJob?.cancel()
                        silenceTimerJob = scope.launch {
                            delay(1200)
                            if (currentSpokenText.isNotBlank()) {
                                stopListeningAndProcess(currentSpokenText)
                            }
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
            viewModel.updateVoiceState(VoiceAssistantState.Listening("Listening...", 1f))

            // Virtual mic detection timer: If after 3.5s zero audio packets arrive, offer instant fallback
            noAudioDetectedTimer = scope.launch {
                delay(3500)
                if (viewModel.voiceState is VoiceAssistantState.Listening && currentSpokenText.isBlank()) {
                    showVirtualMicHelper = true
                }
            }

            // Safety maximum timeout (7.5 seconds)
            maxTimeoutJob = scope.launch {
                delay(7500)
                if (viewModel.voiceState is VoiceAssistantState.Listening) {
                    if (currentSpokenText.isNotBlank()) {
                        stopListeningAndProcess(currentSpokenText)
                    } else {
                        showVirtualMicHelper = true
                        stopListeningManually()
                    }
                }
            }
        } catch (e: Exception) {
            showVirtualMicHelper = true
            viewModel.updateVoiceState(
                VoiceAssistantState.Error("Microphone initialize nahi ho saka. Quick suggestions use karein.")
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            viewModel.updateVoiceState(
                VoiceAssistantState.Error("Microphone permission zaroori hai voice search ke liye.")
            )
        }
    }

    // -------------------------------------------------------------
    // HYPNOTIC CIRCULAR WAVEFORM ANIMATION IN ONE SPOT
    // Smooth harmonic curves in purple and blue palette with zero jagged spikes
    // -------------------------------------------------------------
    val infiniteTransition = rememberInfiniteTransition(label = "circular_orb_animation")
    val isListening = voiceState is VoiceAssistantState.Listening
    val isProcessing = voiceState is VoiceAssistantState.Processing
    val isSpeaking = voiceState is VoiceAssistantState.Speaking || isTtsSpeaking
    val isVoiceActive = isListening || isProcessing || isSpeaking
    val currentRms = (voiceState as? VoiceAssistantState.Listening)?.rmsLevel ?: 1f

    // Rotation: smooth continuous 360 rotation of harmonic wave
    val waveRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isProcessing -> 2200
                    isListening -> 3800
                    isSpeaking -> 3000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Inner core fluid breathing scale (1.00 to 1.14 smoothly)
    val coreBreathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isVoiceActive) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isVoiceActive) 800 else 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_breathing"
    )

    // Middle harmonic wave pulse
    val midWaveScale by infiniteTransition.animateFloat(
        initialValue = 1.05f,
        targetValue = if (isVoiceActive) 1.32f else 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isVoiceActive) 1100 else 2800,
                delayMillis = 100,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mid_wave"
    )

    // Outer radiating smooth ripple wave
    val outerRippleProgress by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isVoiceActive) 1500 else 3400,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_ripple"
    )

    // Palette: Modern Purple, Indigo, Cyan and Royal Blue
    val deepPurple = Color(0xFF7C3AED)
    val vividIndigo = Color(0xFF6366F1)
    val skyCyan = Color(0xFF38BDF8)
    val neonCyan = Color(0xFF06B6D4)
    val royalBlue = Color(0xFF2563EB)
    val softLavender = Color(0xFFC7D2FE)

    val quickVoiceSuggestions = listOf(
        "Photo banane ke liye best AI tool kaunsa hai?",
        "Aaj ki daily AI news kya hai?",
        "Coding ke liye kaunsa tool recommend karoge?",
        "Summarize today's top AI breakthroughs",
        "Music aur song generation tools batao"
    )

    // Main Canvas Container: Full screen dark immersive aesthetic (ChatGPT Voice mode)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF08090E),
                        Color(0xFF0D0F18),
                        Color(0xFF101322),
                        Color(0xFF090A10)
                    )
                )
            )
            .testTag("voice_screen_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // -------------------------------------------------------------
            // 1. TOP BAR (Minimalist, dark glass pill, no ChatGPT icon)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Subtle back or close indicator
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF181B28),
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.resetVoiceState() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Voice",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Center Pill: PickAI Voice • Gemini 3.5 Flash
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF161928),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334F46E5))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isListening -> Color(0xFFEF4444)
                                        isProcessing -> skyCyan
                                        isSpeaking -> deepPurple
                                        else -> Color(0xFF10B981)
                                    }
                                )
                        )
                        Text(
                            text = "PickAI Voice",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "•",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "3.5 Flash",
                            color = skyCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Audio Mute/Speaker Toggle
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF181B28),
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (isTtsSpeaking) {
                                stopAudioPlayback()
                            } else {
                                val lastAnswer = (voiceState as? VoiceAssistantState.Speaking)?.answer
                                    ?: viewModel.voiceMessages.lastOrNull { it.sender == "pickai" }?.text
                                if (!lastAnswer.isNullOrBlank()) {
                                    speakAloud(lastAnswer)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isTtsSpeaking) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Audio toggle",
                            tint = if (isTtsSpeaking) Color(0xFFF43F5E) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. CENTER HERO: THE CIRCULAR FLUID WAVEFORM VOICE ORB IN ONE SPOT
            // (Matching purple and blue scheme, without uneven jagged waves)
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Voice Orb Container
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .testTag("circular_waveform_voice_orb")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            when (voiceState) {
                                is VoiceAssistantState.Listening -> stopListeningManually()
                                is VoiceAssistantState.Speaking -> {
                                    if (isTtsSpeaking) stopAudioPlayback() else startListening()
                                }
                                is VoiceAssistantState.Processing -> {}
                                else -> {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Canvas drawing the continuous fluid circular waveform and glowing sphere
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val baseRadius = size.minDimension * 0.32f

                        // 1. Ambient Background Glow (Deep soft aura)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    deepPurple.copy(alpha = if (isVoiceActive) 0.38f else 0.18f),
                                    vividIndigo.copy(alpha = if (isVoiceActive) 0.22f else 0.10f),
                                    skyCyan.copy(alpha = if (isVoiceActive) 0.12f else 0.04f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = baseRadius * 1.8f * coreBreathingScale
                            ),
                            radius = baseRadius * 1.8f * coreBreathingScale,
                            center = center
                        )

                        // 2. Outer Smooth Radiating Circular Ripple Wave
                        val rippleRadius = baseRadius * outerRippleProgress
                        val rippleAlpha = (1f - (outerRippleProgress - 0.90f) / 0.75f).coerceIn(0f, 0.45f)
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(deepPurple, skyCyan, vividIndigo, neonCyan, deepPurple),
                                center = center
                            ),
                            radius = rippleRadius,
                            center = center,
                            alpha = rippleAlpha,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // 3. Secondary Harmonic Concentric Wave Ring
                        val ring2Radius = baseRadius * midWaveScale
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(skyCyan, deepPurple, neonCyan, royalBlue, skyCyan),
                                center = center
                            ),
                            radius = ring2Radius,
                            center = center,
                            alpha = if (isVoiceActive) 0.85f else 0.35f,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 4. Primary Inner Harmonic Concentric Wave Ring
                        val ring1Radius = baseRadius * coreBreathingScale
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(deepPurple, vividIndigo, skyCyan, neonCyan, deepPurple),
                                center = center
                            ),
                            radius = ring1Radius,
                            center = center,
                            alpha = if (isVoiceActive) 0.95f else 0.60f,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // 5. Smooth Continuous Harmonic Circular Waveform (NO uneven jagged spikes)
                        // Uses symmetrical continuous trigonometrics to create a silky, organic fluid waveform around the circle
                        val path = Path()
                        val steps = 120
                        val harmonicAmplitude = if (isVoiceActive) {
                            (6.5.dp.toPx() + (currentRms.coerceIn(1f, 8f)) * 1.8f)
                        } else {
                            3.2.dp.toPx()
                        }
                        // Symmetrical 5-harmonic wave cycle around the circle
                        val waveFrequency = 5
                        val radOffset = Math.toRadians(waveRotation.toDouble())

                        for (i in 0..steps) {
                            val theta = i * (2.0 * Math.PI / steps)
                            // Continuous sine modulation keeps top and bottom silky smooth without sharp corners
                            val harmonicModulation = kotlin.math.sin(waveFrequency * theta + radOffset).toFloat()
                            val secondaryModulation = (kotlin.math.cos(2 * theta - radOffset * 0.5) * 0.3f).toFloat()
                            val r = (baseRadius * 1.12f) + (harmonicModulation + secondaryModulation) * harmonicAmplitude
                            val x = center.x + r * kotlin.math.cos(theta).toFloat()
                            val y = center.y + r * kotlin.math.sin(theta).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()

                        drawPath(
                            path = path,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    deepPurple,
                                    skyCyan,
                                    vividIndigo,
                                    neonCyan,
                                    royalBlue,
                                    deepPurple
                                ),
                                center = center
                            ),
                            style = Stroke(
                                width = if (isVoiceActive) 3.5.dp.toPx() else 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        // 6. Central Glowing Luminous Core Sphere (Fluid Gradient)
                        val coreRadius = baseRadius * 0.76f * (if (isVoiceActive) coreBreathingScale else 1f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isVoiceActive) 0.45f else 0.20f),
                                    skyCyan.copy(alpha = if (isVoiceActive) 0.85f else 0.55f),
                                    deepPurple.copy(alpha = if (isVoiceActive) 0.95f else 0.70f),
                                    royalBlue.copy(alpha = 0.85f)
                                ),
                                center = center,
                                radius = coreRadius
                            ),
                            radius = coreRadius,
                            center = center
                        )

                        // 7. Core Inner Highlight Ring
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(skyCyan, deepPurple, Color.White, neonCyan, skyCyan),
                                center = center
                            ),
                            radius = coreRadius * 0.95f,
                            center = center,
                            alpha = if (isVoiceActive) 0.75f else 0.40f,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Central Subtle State Icon within the Glowing Orb
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isListening -> {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            isProcessing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                    color = Color.White
                                )
                            }
                            isSpeaking -> {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Speaking",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Tap Orb to Talk",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. CONVERSATION TRANSCRIPT & LIVE STATUS
            // Clear States: Ready, Listening, Processing, Speaking, Error
            // -------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live Transcription or State Status Badge
                when (voiceState) {
                    is VoiceAssistantState.Listening -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x33EF4444),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66EF4444))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                                Text(
                                    text = if (voiceState.partialText.isNotBlank()) voiceState.partialText else "Listening... (Aap bol sakte hain)",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    is VoiceAssistantState.Processing -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x3338BDF8),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6638BDF8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = skyCyan
                                )
                                Text(
                                    text = "Processing with Gemini 3.5 Flash...",
                                    color = skyCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    is VoiceAssistantState.Speaking -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x337C3AED),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x667C3AED))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = softLavender,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Speaking response aloud...",
                                    color = softLavender,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    is VoiceAssistantState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x33F43F5E),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x88F43F5E)),
                            modifier = Modifier.testTag("voice_error_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFDA4AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = voiceState.message,
                                    color = Color(0xFFFDA4AF),
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 2
                                )
                                if (voiceState.canRetry) {
                                    TextButton(
                                        onClick = {
                                            viewModel.retryVoiceQuery { speakAloud(it) }
                                        },
                                        modifier = Modifier.height(28.dp).testTag("voice_error_retry_btn")
                                    ) {
                                        Text("Retry", color = skyCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    is VoiceAssistantState.Idle -> {
                        Text(
                            text = "Tap the glowing central orb to talk in Hindi, Hinglish, or English",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Virtual Mic Helper Banner (for emulator with silent mic)
                if (showVirtualMicHelper && !isSpeaking && !isProcessing && voiceState !is VoiceAssistantState.Error) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E2235),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x336366F1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 Tip: Tap quick suggestions below or use keyboard mode to ask anything:",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // AI Answer Card with visible Play/Pause/Stop control
                val latestAnswer = (voiceState as? VoiceAssistantState.Speaking)?.answer
                    ?: viewModel.voiceMessages.lastOrNull { it.sender == "pickai" }?.text

                if (!latestAnswer.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 145.dp)
                            .testTag("voice_answer_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF161928),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334F46E5))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = skyCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "PickAI Answer",
                                        color = skyCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Play / Replay button
                                    IconButton(
                                        onClick = { speakAloud(latestAnswer) },
                                        modifier = Modifier.size(28.dp).testTag("voice_answer_replay_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Play/Replay answer",
                                            tint = if (isTtsSpeaking) skyCyan else Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Stop Speaking button
                                    if (isTtsSpeaking) {
                                        IconButton(
                                            onClick = { stopAudioPlayback() },
                                            modifier = Modifier.size(28.dp).testTag("voice_answer_stop_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Stop,
                                                contentDescription = "Stop speaking",
                                                tint = Color(0xFFF43F5E),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Share Answer / Link button
                                    IconButton(
                                        onClick = { ShareHelper.shareAiAnswer(context, latestAnswer) },
                                        modifier = Modifier.size(28.dp).testTag("voice_answer_share_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share answer",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latestAnswer,
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 4. BOTTOM FLOATING CONTROLS
            // NO redundant microphone buttons:
            // - Central Orb is the single main voice control.
            // - Left: Keyboard mode toggle
            // - Center: Cancel / Stop button when active, or Ready status chip
            // - Right: Speaker output / mute toggle
            // -------------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Suggestion Chips (Smooth horizontal scroll)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickVoiceSuggestions) { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF161826),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x226366F1)),
                            modifier = Modifier.clickable {
                                stopAudioPlayback()
                                stopListeningAndProcess(suggestion)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = skyCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = suggestion,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Slide-out Keyboard Input Mode
                AnimatedVisibility(
                    visible = isKeyboardModeOpen,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF161928),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334F46E5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = textInputQuery,
                                onValueChange = { textInputQuery = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                                cursorBrush = SolidColor(skyCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 10.dp)
                                    .testTag("voice_keyboard_input"),
                                decorationBox = { innerTextField ->
                                    if (textInputQuery.isEmpty()) {
                                        Text(
                                            text = "Bolkar ya likhkar poochein...",
                                            color = Color(0xFF64748B),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            if (textInputQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val q = textInputQuery
                                        textInputQuery = ""
                                        isKeyboardModeOpen = false
                                        stopAudioPlayback()
                                        stopListeningAndProcess(q)
                                    },
                                    modifier = Modifier.size(36.dp).testTag("voice_keyboard_send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = skyCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // The Clean Floating Capsule Dock
                Surface(
                    shape = RoundedCornerShape(36.dp),
                    color = Color(0xFF141724),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334F46E5)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Keyboard Toggle Button
                        Surface(
                            shape = CircleShape,
                            color = if (isKeyboardModeOpen) Color(0xFF262A40) else Color(0xFF1B1E30),
                            modifier = Modifier.size(46.dp)
                        ) {
                            IconButton(
                                onClick = { isKeyboardModeOpen = !isKeyboardModeOpen },
                                modifier = Modifier.fillMaxSize().testTag("voice_keyboard_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = "Toggle Keyboard input",
                                    tint = if (isKeyboardModeOpen) skyCyan else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Center: Cancel / Stop Button when active, or Ready Indicator
                        if (isVoiceActive) {
                            Button(
                                onClick = {
                                    stopListeningManually()
                                    stopAudioPlayback()
                                    viewModel.resetVoiceState()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("voice_cancel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel / Stop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else if (voiceState is VoiceAssistantState.Error) {
                            Button(
                                onClick = {
                                    viewModel.retryVoiceQuery { speakAloud(it) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = vividIndigo
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("voice_retry_action_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF1B1E30),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x336366F1)),
                                modifier = Modifier.testTag("voice_ready_indicator")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Text(
                                        text = "Tap Orb to Talk",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Right: Speaker Audio Output Mute / Unmute Toggle
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1B1E30),
                            modifier = Modifier.size(46.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (isTtsSpeaking) {
                                        stopAudioPlayback()
                                    } else {
                                        viewModel.toggleSpeechOutput()
                                    }
                                },
                                modifier = Modifier.fillMaxSize().testTag("voice_speaker_toggle")
                            ) {
                                Icon(
                                    imageVector = if (!viewModel.isSpeechOutputEnabled) {
                                        Icons.Default.VolumeMute
                                    } else if (isTtsSpeaking) {
                                        Icons.Default.GraphicEq
                                    } else {
                                        Icons.Default.VolumeUp
                                    },
                                    contentDescription = "Toggle speaker audio",
                                    tint = if (!viewModel.isSpeechOutputEnabled) Color(0xFFF43F5E) else if (isTtsSpeaking) skyCyan else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
