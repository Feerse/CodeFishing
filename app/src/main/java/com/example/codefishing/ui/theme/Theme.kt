package com.example.codefishing.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.codefishing.core.data.ThemeMode

private val SkemaGelap = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF06212B),
    primaryContainer = Color(0xFF4A2410),
    onPrimaryContainer = Color(0xFFFFD9C7),
    secondary = Tertiary,
    onSecondary = Neutral,
    secondaryContainer = Secondary,
    onSecondaryContainer = OnDark,
    tertiary = Tertiary,
    onTertiary = Neutral,
    background = Neutral,
    onBackground = OnDark,
    surface = Secondary,
    onSurface = OnDark,
    surfaceVariant = Secondary,
    onSurfaceVariant = OnDarkMuted,
    surfaceContainerLowest = Color(0xFF071923),
    surfaceContainerLow = Color(0xFF0B2432),
    surfaceContainer = Secondary,
    surfaceContainerHigh = Color(0xFF173F4E),
    surfaceContainerHighest = Color(0xFF1B4A5F),
    outline = Color(0xFF41616F),
    outlineVariant = Color(0xFF244754),
    inverseSurface = OnDark,
    inverseOnSurface = Neutral,
    error = ErrorRed,
    onError = Color(0xFF3A0A12),
)

private val SkemaTerang = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = Color(0xFFFFE1D3),
    onPrimaryContainer = Color(0xFF3A1200),
    secondary = SecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E9EF),
    onSecondaryContainer = OnLight,
    tertiary = TertiaryLight,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD5F2E5),
    onTertiaryContainer = Color(0xFF063A2A),
    background = NeutralLight,
    onBackground = OnLight,
    surface = Color(0xFFFFFFFF),
    onSurface = OnLight,
    surfaceVariant = Color(0xFFE4EFF3),
    onSurfaceVariant = OnLightMuted,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4FAFC),
    surfaceContainer = Color(0xFFEDF6F9),
    surfaceContainerHigh = Color(0xFFE4EFF3),
    surfaceContainerHighest = Color(0xFFDCEAF0),
    outline = Color(0xFFA9BEC7),
    outlineVariant = Color(0xFFC6D6DD),
    inverseSurface = Secondary,
    inverseOnSurface = OnDark,
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

val LocalWarna = staticCompositionLocalOf { WarnaGelap }

@Composable
fun CodeFishingTheme(
    mode: ThemeMode = ThemeMode.Sistem,
    content: @Composable () -> Unit,
) {
    val gelap = mode.gelap(isSystemInDarkTheme())
    val warna = if (gelap) WarnaGelap else WarnaTerang
    val skema = if (gelap) SkemaGelap else SkemaTerang

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.cariActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !gelap
                isAppearanceLightNavigationBars = !gelap
            }
        }
    }

    CompositionLocalProvider(LocalWarna provides warna) {
        MaterialTheme(
            colorScheme = skema,
            typography = Typography,
            content = content,
        )
    }
}

private tailrec fun Context.cariActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.cariActivity()
    else -> null
}
