package com.espimsystems.mespace.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MeSpaceRadius(
    val none: Dp = 0.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 24.dp,
    val full: Dp = 999.dp,
)

internal val LocalMeSpaceRadius = staticCompositionLocalOf {
    MeSpaceRadius()
}