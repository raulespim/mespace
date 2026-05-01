package com.espimsystems.mespace.feature.tasks.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.presentation.TaskListItemUiModel

@Composable
fun TaskItem(
    task: TaskListItemUiModel,
    onStatusClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = task.status == TaskStatus.DONE

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (isDone) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                )

                task.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                task.assignedToLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = isDone,
                    onClick = onStatusClick,
                    label = {
                        Text(text = task.status.label)
                    },
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(text = task.priority.label)
                    },
                )

                TextButton(
                    onClick = onDeleteClick,
                ) {
                    Text(text = "Remover")
                }
            }
        }
    }
}

private val TaskStatus.label: String
    get() = when (this) {
        TaskStatus.PENDING -> "Pendente"
        TaskStatus.IN_PROGRESS -> "Em andamento"
        TaskStatus.DONE -> "Concluída"
    }

private val TaskPriority.label: String
    get() = when (this) {
        TaskPriority.LOW -> "Baixa"
        TaskPriority.MEDIUM -> "Média"
        TaskPriority.HIGH -> "Alta"
    }