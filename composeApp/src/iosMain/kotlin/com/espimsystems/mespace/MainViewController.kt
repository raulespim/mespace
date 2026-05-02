package com.espimsystems.mespace

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.espimsystems.mespace.core.database.IosDatabaseDriverFactory

fun MainViewController() = ComposeUIViewController {
    val databaseDriverFactory = remember {
        IosDatabaseDriverFactory()
    }

    App(
        databaseDriverFactory = databaseDriverFactory,
    )
}
