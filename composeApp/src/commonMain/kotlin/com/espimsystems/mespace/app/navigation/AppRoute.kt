package com.espimsystems.mespace.app.navigation

import androidx.navigation3.runtime.NavKey

sealed interface AppRoute : NavKey {

    data object Welcome : AppRoute

    data object Spaces : AppRoute

    data class Tasks(
        val spaceId: String,
        val spaceName: String,
    ) : AppRoute
}