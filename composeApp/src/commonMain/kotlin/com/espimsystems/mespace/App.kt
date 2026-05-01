package com.espimsystems.mespace

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.espimsystems.mespace.app.MeSpaceApp
import com.espimsystems.mespace.core.logging.AppLogLevel
import com.espimsystems.mespace.core.logging.AppLoggerFactory

@Composable
@Preview(showBackground = true)
fun App() {

    val logger = remember {
        AppLoggerFactory.create(
            minimumLevel = AppLogLevel.DEBUG,
            enabled = true,
        )
    }

    MeSpaceApp(
        logger = logger,
    )
}