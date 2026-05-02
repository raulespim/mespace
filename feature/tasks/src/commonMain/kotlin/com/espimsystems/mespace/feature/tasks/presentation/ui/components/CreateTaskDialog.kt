package com.espimsystems.mespace.feature.tasks.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.espimsystems.mespace.core.designsystem.component.MeSpaceButton
import com.espimsystems.mespace.core.designsystem.component.MeSpaceTextButton
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTaskDialog(
    title: String,
    description: String,
    selectedPriority: TaskPriority,
    isLoading: Boolean,
    canConfirm: Boolean,
    errorMessage: String?,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onPrioritySelected: (TaskPriority) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        title = {
            Text(text = "Nova tarefa")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    space = MeSpaceTheme.spacing.medium,
                ),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    label = {
                        Text(text = "Título")
                    },
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 2,
                    label = {
                        Text(text = "Descrição opcional")
                    },
                )

                Text(text = "Prioridade")

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.small),
                ) {
                    TaskPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = {
                                onPrioritySelected(priority)
                            },
                            enabled = !isLoading,
                            label = {
                                Text(text = priority.label)
                            },
                        )
                    }
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            MeSpaceButton(
                text = if (isLoading) {
                    "Criando..."
                } else {
                    "Criar"
                },
                onClick = onConfirm,
                enabled = canConfirm,
            )
        },
        dismissButton = {
            MeSpaceTextButton(
                text = "Cancelar",
                onClick = onDismiss,
                enabled = !isLoading,
            )
        },
    )
}

private val TaskPriority.label: String
    get() = when (this) {
        TaskPriority.LOW -> "Baixa"
        TaskPriority.MEDIUM -> "Média"
        TaskPriority.HIGH -> "Alta"
    }
