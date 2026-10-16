package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveVoiceScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isTtsReady = true
            }
        }
        tts = ttsInstance

        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.voiceMessages.size) {
        if (viewModel.voiceMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.voiceMessages.size - 1)
            // Speak latest AI message if speech is active
            val latest = viewModel.voiceMessages.last()
            if (latest.sender == "pickai" && isTtsReady) {
                tts?.speak(latest.text, TextToSpeech.QUEUE_FLUSH, null, "pickai_tts")
            }
        }
    }

    val quickPrompts = listOf(
        "Recommend top AI coding tool",
        "Generate Midjourney prompt idea",
        "How do I write better prompts?",
        "Best tool for creating music"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_pickai_mascot),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Live Voice & Chat", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val latest = viewModel.voiceMessages.lastOrNull { it.sender == "pickai" }
                        if (latest != null && isTtsReady) {
                            tts?.speak(latest.text, TextToSpeech.QUEUE_FLUSH, null, "pickai_tts")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Animated Voice Mascot Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pulsing mascot avatar circle
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .scale(pulseScale)
                                .border(
                                    width = if (isListening) 3.dp else 1.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_pickai_mascot),
                                contentDescription = "PickAI Voice Mascot",
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isListening) "Listening to your voice..." else "PickAI Voice Companion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = if (isListening) "Speak now, PickAI is listening" else "Tap the microphone or choose a voice prompt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Prompt Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    SuggestionChip(
                        onClick = { viewModel.sendVoiceMessage(prompt) },
                        label = { Text(prompt, maxLines = 1) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Message History
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.voiceMessages, key = { it.id }) { msg ->
                    val isPickAi = msg.sender == "pickai"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isPickAi) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isPickAi) 4.dp else 16.dp,
                                bottomEnd = if (isPickAi) 16.dp else 4.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPickAi) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            ),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isPickAi) "PickAI" else "You",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPickAi) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isPickAi) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onPrimary
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Voice Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Interactive Voice Record Toggle
                IconButton(
                    onClick = {
                        isListening = !isListening
                        if (isListening) {
                            // Simulate voice capture
                            viewModel.sendVoiceMessage("Give me a prompt for an aesthetic logo design.")
                            isListening = false
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
                        )
                        .testTag("voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask PickAI anything...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("voice_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendVoiceMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("send_voice_query_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
