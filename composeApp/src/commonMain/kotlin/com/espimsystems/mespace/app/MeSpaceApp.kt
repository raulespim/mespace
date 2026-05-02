package com.espimsystems.mespace.app

import androidx.compose.runtime.Composable
import com.espimsystems.mespace.app.navigation.AppNavigation
import com.espimsystems.mespace.app.navigation.PreviewAppNavigation
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme
import com.espimsystems.mespace.core.database.MeSpaceDatabase
import com.espimsystems.mespace.core.logging.AppLogger

@Composable
fun MeSpaceApp(
    logger: AppLogger,
    database: MeSpaceDatabase,
) {
    MeSpaceTheme {
        AppNavigation(
            logger = logger,
            database = database,
        )
    }
}

@Composable
fun MeSpacePreviewApp(
    logger: AppLogger,
) {
    MeSpaceTheme {
        PreviewAppNavigation(
            logger = logger,
        )
    }
}
