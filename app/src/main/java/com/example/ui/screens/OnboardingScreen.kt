package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.MainViewModel

data class OnboardingSlide(
    val title: String,
    val description: String,
    val badge: String
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val slides = listOf(
        OnboardingSlide(
            title = "Explore The Best AI Tools",
            description = "Browse hand-curated AI platforms across Chatbots, Coding, Images, Video, Audio, and Content creation.",
            badge = "Curated & Verified"
        ),
        OnboardingSlide(
            title = "Interactive Prompt Sandbox",
            description = "Copy battle-tested prompt templates, customize variables, and run simulations directly within PickAI.",
            badge = "Prompt Engineering"
        ),
        OnboardingSlide(
            title = "Live Voice Companion",
            description = "Speak directly with PickAI for instant creative ideas, tool recommendations, and audio synthesis.",
            badge = "Voice-Powered"
        )
    )

    val currentSlide = slides[currentPage]

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.testTag("skip_onboarding_button")
                ) {
                    Text("Skip")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Mascot Illustration
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pickai_mascot),
                    contentDescription = "PickAI Mascot",
                    modifier = Modifier.size(120.dp)
                )
            }

            // Slide Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = currentSlide.badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = currentSlide.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = currentSlide.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )

                // Page Indicator Dots
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    slides.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (currentPage == index) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentPage == index) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    }
                                )
                        )
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Previous")
                    }
                }

                Button(
                    onClick = {
                        if (currentPage < slides.size - 1) {
                            currentPage++
                        } else {
                            viewModel.completeOnboarding()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("next_onboarding_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (currentPage == slides.size - 1) "Get Started" else "Next")
                }
            }
        }
    }
}
