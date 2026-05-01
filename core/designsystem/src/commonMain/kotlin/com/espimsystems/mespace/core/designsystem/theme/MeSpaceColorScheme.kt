package com.espimsystems.mespace.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MeSpaceSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,

    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
)

internal val LightMeSpaceSemanticColors = MeSpaceSemanticColors(
    success = Color(0xFF15803D),
    onSuccess = Color.White,
    successContainer = Color(0xFFDCFCE7),
    onSuccessContainer = Color(0xFF14532D),

    warning = Color(0xFFD97706),
    onWarning = Color.White,
    warningContainer = Color(0xFFFEF3C7),
    onWarningContainer = Color(0xFF78350F),

    info = Color(0xFF2563EB),
    onInfo = Color.White,
    infoContainer = Color(0xFFDBEAFE),
    onInfoContainer = Color(0xFF1E3A8A),
)

internal val DarkMeSpaceSemanticColors = MeSpaceSemanticColors(
    success = Color(0xFF22C55E),
    onSuccess = Color(0xFF052E16),
    successContainer = Color(0xFF14532D),
    onSuccessContainer = Color(0xFFDCFCE7),

    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFF451A03),
    warningContainer = Color(0xFF78350F),
    onWarningContainer = Color(0xFFFEF3C7),

    info = Color(0xFF60A5FA),
    onInfo = Color(0xFF172554),
    infoContainer = Color(0xFF1E3A8A),
    onInfoContainer = Color(0xFFDBEAFE),
)

internal val LocalMeSpaceSemanticColors = staticCompositionLocalOf {
    LightMeSpaceSemanticColors
}