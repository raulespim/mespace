package com.espimsystems.mespace.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val MeSpaceLightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF312E81),

    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),

    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),

    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),

    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
)

private val MeSpaceDarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF111827),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),

    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF111827),
    secondaryContainer = Color(0xFF155E75),
    onSecondaryContainer = Color(0xFFCFFAFE),

    background = Color(0xFF020617),
    onBackground = Color(0xFFE5E7EB),

    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),

    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
)

private val DefaultSpacing = MeSpaceSpacing()
private val DefaultRadius = MeSpaceRadius()
private val DefaultElevation = MeSpaceElevation()

object MeSpaceTheme {

    val spacing: MeSpaceSpacing
        @Composable
        get() = LocalMeSpaceSpacing.current

    val radius: MeSpaceRadius
        @Composable
        get() = LocalMeSpaceRadius.current

    val elevation: MeSpaceElevation
        @Composable
        get() = LocalMeSpaceElevation.current

    val semanticColors: MeSpaceSemanticColors
        @Composable
        get() = LocalMeSpaceSemanticColors.current
}

@Composable
fun MeSpaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val materialColorScheme = if (darkTheme) {
        MeSpaceDarkColorScheme
    } else {
        MeSpaceLightColorScheme
    }

    val semanticColors = if (darkTheme) {
        DarkMeSpaceSemanticColors
    } else {
        LightMeSpaceSemanticColors
    }

    CompositionLocalProvider(
        LocalMeSpaceSpacing provides DefaultSpacing,
        LocalMeSpaceRadius provides DefaultRadius,
        LocalMeSpaceElevation provides DefaultElevation,
        LocalMeSpaceSemanticColors provides semanticColors,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content,
        )
    }
}