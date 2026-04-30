package com.espimsystems.mespace.features.welcome.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.espimsystems.mespace.core.designsystem.component.MeSpaceButton
import com.espimsystems.mespace.core.designsystem.component.MeSpaceScaffold
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme

@Composable
fun WelcomeScreen(
    state: WelcomeUiState,
    onIntent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    MeSpaceScaffold { contentPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(MeSpaceTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = state.appName,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = state.headline,
                modifier = Modifier.padding(top = MeSpaceTheme.spacing.medium),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MeSpaceButton(
                text = state.primaryActionText,
                onClick = {
                    onIntent(WelcomeIntent.ContinueClicked)
                },
                modifier = Modifier.padding(top = MeSpaceTheme.spacing.doubleExtraLarge),
            )
        }
    }
}