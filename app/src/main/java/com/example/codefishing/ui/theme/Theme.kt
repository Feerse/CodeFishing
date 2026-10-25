package com.example.codefishing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Aplikasi hanya punya tema gelap "Oceanic Deep Dive" (tanpa dynamic color supaya warna konsisten).
private val CodeFishingColors = darkColorScheme(
    primary = Primary,
    onPrimary = Neutral,
    secondary = Tertiary,
    onSecondary = Neutral,
    tertiary = Tertiary,
    background = Neutral,
    onBackground = OnDark,
    surface = Secondary,
    onSurface = OnDark,
    surfaceVariant = Secondary,
    onSurfaceVariant = OnDarkMuted,
    error = ErrorRed,
)

@Composable
fun CodeFishingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CodeFishingColors,
        typography = Typography,
        content = content,
    )
}
