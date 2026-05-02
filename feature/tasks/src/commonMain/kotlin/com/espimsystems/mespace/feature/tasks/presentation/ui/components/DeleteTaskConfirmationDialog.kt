package com.espimsystems.mespace.feature.tasks.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.espimsystems.mespace.core.designsystem.component.MeSpaceButton
import com.espimsystems.mespace.core.designsystem.component.MeSpaceTextButton
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme
import com.espimsystems.mespace.feature.tasks.presentation.TaskPendingDeletionUiModel

@Composable
fun DeleteTaskConfirmationDialog(
    task: TaskPendingDeletionUiModel,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) {
                onDismiss()
            }
        },
        title = {
            Text(text = "Remover tarefa?")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.medium),
            ) {
                Text(
                    text = "Essa ação não pode ser desfeita.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        confirmButton = {
            MeSpaceButton(
                text = if (isDeleting) {
                    "Removendo..."
                } else {
                    "Remover"
                },
                onClick = onConfirm,
                enabled = !isDeleting,
            )
        },
        dismissButton = {
            MeSpaceTextButton(
                text = "Cancelar",
                onClick = onDismiss,
                enabled = !isDeleting,
            )
        },
    )
}
