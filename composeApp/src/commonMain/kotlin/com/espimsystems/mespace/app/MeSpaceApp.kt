package com.espimsystems.mespace.app

import androidx.compose.runtime.Composable
import com.espimsystems.mespace.app.navigation.AppNavigation
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme
import com.espimsystems.mespace.core.logging.AppLogger

@Composable
fun MeSpaceApp(
    logger: AppLogger,
) {
    MeSpaceTheme {
        AppNavigation(
            logger = logger
        )
    }
}