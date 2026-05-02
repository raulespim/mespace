package com.espimsystems.mespace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.espimsystems.mespace.app.MeSpaceApp
import com.espimsystems.mespace.app.MeSpacePreviewApp
import com.espimsystems.mespace.core.database.DatabaseDriverFactory
import com.espimsystems.mespace.core.database.MeSpaceDatabaseFactory
import com.espimsystems.mespace.core.logging.AppLogLevel
import com.espimsystems.mespace.core.logging.AppLogger
import com.espimsystems.mespace.core.logging.AppLoggerFactory

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory,
) {
    val logger = rememberAppLogger()

    val database = remember(databaseDriverFactory) {
        MeSpaceDatabaseFactory(databaseDriverFactory).createDatabase()
    }

    MeSpaceApp(
        logger = logger,
        database = database,
    )
}

@Composable
@Preview(showBackground = true)
fun AppPreview() {
    val logger = rememberAppLogger()

    MeSpacePreviewApp(
        logger = logger,
    )
}

@Composable
private fun rememberAppLogger(): AppLogger {
    return remember {
        AppLoggerFactory.create(
            minimumLevel = AppLogLevel.DEBUG,
            enabled = true,
        )
    }
}
