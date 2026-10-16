package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PickAiPrimaryDark,
    onPrimary = PickAiOnPrimaryDark,
    primaryContainer = PickAiPrimaryContainerDark,
    onPrimaryContainer = PickAiOnPrimaryContainerDark,
    secondary = PickAiSecondaryDark,
    onSecondary = PickAiOnSecondaryDark,
    secondaryContainer = PickAiSecondaryContainerDark,
    onSecondaryContainer = PickAiOnSecondaryContainerDark,
    tertiary = PickAiTertiaryDark,
    onTertiary = PickAiOnTertiaryDark,
    tertiaryContainer = PickAiTertiaryContainerDark,
    onTertiaryContainer = PickAiOnTertiaryContainerDark,
    background = PickAiBackgroundDark,
    onBackground = PickAiOnBackgroundDark,
    surface = PickAiSurfaceDark,
    onSurface = PickAiOnSurfaceDark,
    surfaceVariant = PickAiSurfaceVariantDark,
    onSurfaceVariant = PickAiOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = PickAiPrimaryLight,
    onPrimary = PickAiOnPrimaryLight,
    primaryContainer = PickAiPrimaryContainerLight,
    onPrimaryContainer = PickAiOnPrimaryContainerLight,
    secondary = PickAiSecondaryLight,
    onSecondary = PickAiOnSecondaryLight,
    secondaryContainer = PickAiSecondaryContainerLight,
    onSecondaryContainer = PickAiOnSecondaryContainerLight,
    tertiary = PickAiTertiaryLight,
    onTertiary = PickAiOnTertiaryLight,
    tertiaryContainer = PickAiTertiaryContainerLight,
    onTertiaryContainer = PickAiOnTertiaryContainerLight,
    background = PickAiBackgroundLight,
    onBackground = PickAiOnBackgroundLight,
    surface = PickAiSurfaceLight,
    onSurface = PickAiOnSurfaceLight,
    surfaceVariant = PickAiSurfaceVariantLight,
    onSurfaceVariant = PickAiOnSurfaceVariantLight
)

@Composable
fun PickAiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep MyApplicationTheme for backward compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    PickAiTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
