package com.espimsystems.mespace.features.spaces.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.espimsystems.mespace.core.designsystem.component.MeSpaceEmptyState
import com.espimsystems.mespace.core.designsystem.component.MeSpaceScaffold
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme

@Composable
fun SpacesPlaceholderScreen(
    onOpenTasksClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MeSpaceScaffold { contentPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(MeSpaceTheme.spacing.extraLarge),
            contentAlignment = Alignment.Center,
        ) {
            MeSpaceEmptyState(
                title = "Seu espaço",
                description = "Aqui ficarão os espaços compartilhados, como casa, casal ou família.",
                actionText = "Ver tarefas",
                onActionClick = onOpenTasksClick,
            )
        }
    }
}