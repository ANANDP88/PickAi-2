package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreativeToolsScreen(
    viewModel: MainViewModel,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabTitles = listOf("Text & Summary", "Code Helper", "Image Prompts")
    val tabIcons = listOf(Icons.Default.Description, Icons.Default.Code, Icons.Default.Image)
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("AI Creative Studio", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1, fontSize = 12.sp) },
                        icon = { Icon(tabIcons[index], contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TextSummaryTool(viewModel, context, snackbarHostState)
                1 -> CodeHelperTool(viewModel, context, snackbarHostState)
                2 -> ImagePromptBuilderTool(context, snackbarHostState)
            }
        }
    }
}

@Composable
fun TextSummaryTool(
    viewModel: MainViewModel,
    context: Context,
    snackbarHostState: SnackbarHostState
) {
    var style by remember { mutableStateOf("Bullet Points") }
    val styles = listOf("Bullet Points", "Executive Brief", "Key Takeaways")
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "⚡ Instant Text & Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Paste any article, email, or meeting transcript to extract high-yield insights.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = viewModel.summaryInputText,
                onValueChange = { viewModel.summaryInputText = it },
                label = { Text("Paste text here...") },
                placeholder = { Text("Enter notes, blog draft, or complex article...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_input_field"),
                minLines = 4,
                maxLines = 8,
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            Text(
                text = "Summary Format:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(styles) { s ->
                    FilterChip(
                        selected = style == s,
                        onClick = { style = s },
                        label = { Text(s) }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (viewModel.summaryInputText.isBlank()) {
                        viewModel.summaryInputText = "Artificial intelligence tools in 2026 have shifted from basic conversational chatbots to proactive ambient agents. Modern enterprise workflows now combine real-time voice, cross-file reasoning, multi-modal generation, and zero-shot code deployment. Organizations adopting modular AI pipelines report up to 45% reduction in turnaround time."
                    }
                    viewModel.generateSummary(style)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_summary_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (viewModel.summaryInputText.isBlank()) "Load Example & Summarize" else "Generate Summary")
            }
        }

        if (viewModel.summaryOutputText.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Generated Summary",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Summary", viewModel.summaryOutputText)
                                clipboard.setPrimaryClip(clip)
                                scope.launch { snackbarHostState.showSnackbar("Summary copied to clipboard!") }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.summaryOutputText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CodeHelperTool(
    viewModel: MainViewModel,
    context: Context,
    snackbarHostState: SnackbarHostState
) {
    val languages = listOf("Kotlin", "Python", "TypeScript")
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "💻 Code Helper & Architect",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "State your programming goal, algorithm, or bug to generate clean, idiomatic code.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Text(
                text = "Target Language:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    FilterChip(
                        selected = viewModel.codeLanguage == lang,
                        onClick = { viewModel.codeLanguage = lang },
                        label = { Text(lang) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = viewModel.codeGoal,
                onValueChange = { viewModel.codeGoal = it },
                label = { Text("What do you want to code?") },
                placeholder = { Text("e.g. Clean architecture Room migration or Compose pull-to-refresh") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("code_goal_input"),
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            Button(
                onClick = {
                    if (viewModel.codeGoal.isBlank()) {
                        viewModel.codeGoal = "Jetpack Compose ViewModel state management with Coroutines"
                    }
                    viewModel.generateCode()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_code_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (viewModel.codeGoal.isBlank()) "Load Example & Generate Code" else "Generate Code")
            }
        }

        if (viewModel.generatedCodeOutput.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${viewModel.codeLanguage} Snippet",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Code", viewModel.generatedCodeOutput)
                                clipboard.setPrimaryClip(clip)
                                scope.launch { snackbarHostState.showSnackbar("Code copied to clipboard!") }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = viewModel.generatedCodeOutput,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImagePromptBuilderTool(
    context: Context,
    snackbarHostState: SnackbarHostState
) {
    var subject by remember { mutableStateOf("Cyberpunk robot mascot holding coffee") }
    var selectedStyle by remember { mutableStateOf("Cinematic 3D") }
    var selectedLighting by remember { mutableStateOf("Volumetric Neon") }
    var selectedAspectRatio by remember { mutableStateOf("16:9") }
    val scope = rememberCoroutineScope()

    val styles = listOf("Cinematic 3D", "Photorealistic", "Anime Watercolor", "Claymorphism", "Vintage Retro")
    val lightings = listOf("Volumetric Neon", "Golden Hour Sun", "Studio Softbox", "Moody Chiaroscuro")
    val aspectRatios = listOf("16:9", "1:1", "9:16", "4:3")

    val fullPrompt = remember(subject, selectedStyle, selectedLighting, selectedAspectRatio) {
        "$subject, $selectedStyle, with $selectedLighting lighting, highly detailed, 8k resolution, octane render --ar $selectedAspectRatio --v 6.0"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "🎨 Image Prompt Architect",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Construct professional Midjourney and DALL-E prompts with precision parameters.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Core Subject / Scene") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            Text("Art Style:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(styles) { s ->
                    FilterChip(selected = selectedStyle == s, onClick = { selectedStyle = s }, label = { Text(s) })
                }
            }
        }

        item {
            Text("Lighting Mood:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(lightings) { l ->
                    FilterChip(selected = selectedLighting == l, onClick = { selectedLighting = l }, label = { Text(l) })
                }
            }
        }

        item {
            Text("Aspect Ratio:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(aspectRatios) { ar ->
                    FilterChip(selected = selectedAspectRatio == ar, onClick = { selectedAspectRatio = ar }, label = { Text(ar) })
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated Midjourney Prompt",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Image Prompt", fullPrompt)
                            clipboard.setPrimaryClip(clip)
                            scope.launch { snackbarHostState.showSnackbar("Prompt copied to clipboard!") }
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fullPrompt,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
