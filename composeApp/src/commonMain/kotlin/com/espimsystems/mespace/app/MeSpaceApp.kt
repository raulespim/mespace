package com.espimsystems.mespace.app

import androidx.compose.runtime.Composable
import com.espimsystems.mespace.app.navigation.AppNavigation
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme

@Composable
fun MeSpaceApp() {
    MeSpaceTheme {
        AppNavigation()
    }
}