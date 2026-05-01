package com.espimsystems.mespace.feature.tasks.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority

@Composable
fun CreateTaskDialog(
    title: String,
    description: String,
    selectedPriority: TaskPriority,
    isLoading: Boolean,
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
                    space = 12.dp,
                ),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChanged,
                    enabled = !isLoading,
                    singleLine = true,
                    label = {
                        Text(text = "Título")
                    },
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChanged,
                    enabled = !isLoading,
                    minLines = 2,
                    label = {
                        Text(text = "Descrição opcional")
                    },
                )

                Text(text = "Prioridade")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                    ),
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
                    Text(text = message)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading,
            ) {
                Text(
                    text = if (isLoading) {
                        "Criando..."
                    } else {
                        "Criar"
                    },
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text(text = "Cancelar")
            }
        },
    )
}

private val TaskPriority.label: String
    get() = when (this) {
        TaskPriority.LOW -> "Baixa"
        TaskPriority.MEDIUM -> "Média"
        TaskPriority.HIGH -> "Alta"
    }