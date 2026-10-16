package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel? = null,
    onToggleTheme: (() -> Unit)? = null
) {
    val toggleTheme = onToggleTheme ?: { viewModel?.toggleTheme() }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen")
    ) {
        // Decorative top-right organic gradient wave (as seen in visual reference)
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(240.dp, 160.dp)
        ) {
            val path = Path().apply {
                moveTo(size.width, 0f)
                lineTo(size.width * 0.35f, 0f)
                cubicTo(
                    size.width * 0.5f, size.height * 0.6f,
                    size.width * 0.8f, size.height * 0.4f,
                    size.width, size.height
                )
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.5f),
                        Color(0xFF6366F1).copy(alpha = 0.35f),
                        Color(0xFF8B5CF6).copy(alpha = 0.25f)
                    ),
                    start = Offset(size.width * 0.4f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }

        // Decorative bottom-left organic gradient wave
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(220.dp, 140.dp)
        ) {
            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, size.height * 0.3f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.7f,
                    size.width * 0.6f, size.height * 0.6f,
                    size.width, size.height
                )
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.3f),
                        Color(0xFF38BDF8).copy(alpha = 0.4f)
                    ),
                    start = Offset(0f, size.height * 0.5f),
                    end = Offset(size.width, size.height)
                )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                ) {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("settings_back_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Make PickAI work your way.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // 1. Appearance
                SettingsItem(
                    title = "Appearance",
                    subtitle = if (viewModel?.isDarkMode == true) "Dark Theme active" else "Choose your preferred theme",
                    icon = Icons.Default.Palette,
                    onClick = { toggleTheme() }
                )

                // 2. Notifications
                SettingsItem(
                    title = "Notifications",
                    subtitle = if (notificationsEnabled) "Manage app notifications" else "Notifications paused",
                    icon = Icons.Default.Notifications,
                    onClick = { showNotificationDialog = true }
                )

                // 3. Help & Support
                SettingsItem(
                    title = "Help & Support",
                    subtitle = "Get help using PickAI",
                    icon = Icons.Default.HelpOutline,
                    onClick = { showHelpDialog = true }
                )

                // 4. About PickAI (With custom mascot icon as in user's request)
                SettingsItem(
                    title = "About PickAI",
                    subtitle = "Version and app information",
                    icon = null,
                    customIcon = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_pickai_mascot),
                                contentDescription = "PickAI Logo",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    onClick = { showAboutDialog = true }
                )

                // 5. Replay Onboarding
                SettingsItem(
                    title = "Replay Onboarding",
                    subtitle = "View the introduction screens again",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        viewModel?.replayOnboarding()
                        navController.navigate(Screen.Onboarding.route)
                    }
                )
            }
        }
    }

    // Dialogs
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pickai_mascot),
                        contentDescription = "PickAI Mascot",
                        modifier = Modifier.size(44.dp)
                    )
                }
            },
            title = { Text("PickAI", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Smarter Tools. Bigger Ideas.",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "PickAI curates the world's most innovative AI tools across Chatbots, Coding, Images, Video, Audio, and Content.\n\nSave favorite tools, test prompt recipes in the sandbox, and talk with the live voice companion.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Version 1.0.0 • AI Studio Edition",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Close")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Help & Support", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "• Bookmarking tools:\nTap the bookmark icon in the tool details top bar to pin tools to your Saved tab.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Prompt Sandbox:\nOn any tool's page, you can copy sample prompts or run instant simulations.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Live Voice:\nUse the Live Voice tab to talk to PickAI or trigger audio summaries.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Support contact: support@pickai.app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Save")
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Manage Notifications", fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New Tools & Prompt Drops",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Get notified when new AI models are released",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    customIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .testTag("settings_item_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (customIcon != null) {
                customIcon()
            } else if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
