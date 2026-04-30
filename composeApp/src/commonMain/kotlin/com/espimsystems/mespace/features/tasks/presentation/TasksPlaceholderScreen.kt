package com.espimsystems.mespace.features.tasks.presentation

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
fun TasksPlaceholderScreen(
    spaceName: String,
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
                title = "Tarefas de $spaceName",
                description = "Aqui ficarão as tarefas atribuídas aos membros deste espaço.",
            )
        }
    }
}