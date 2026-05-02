package com.espimsystems.mespace.feature.tasks.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.espimsystems.mespace.core.designsystem.component.MeSpaceTextButton
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme
import com.espimsystems.mespace.feature.tasks.domain.model.TaskPriority
import com.espimsystems.mespace.feature.tasks.domain.model.TaskStatus
import com.espimsystems.mespace.feature.tasks.presentation.TaskListItemUiModel

@OptIn(ExperimentalLayoutApi::class)
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
        shape = RoundedCornerShape(MeSpaceTheme.radius.small),
        colors = CardDefaults.cardColors(
            containerColor = task.containerColor,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MeSpaceTheme.elevation.small,
        ),
    ) {
        Column(
            modifier = Modifier.padding(MeSpaceTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.medium),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.extraSmall),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
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

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(MeSpaceTheme.spacing.small),
            ) {
                FilterChip(
                    selected = task.status == TaskStatus.DONE,
                    onClick = onStatusClick,
                    enabled = !task.isUpdating && !task.isDeleting,
                    label = {
                        Text(
                            text = if (task.isUpdating) {
                                "Atualizando..."
                            } else {
                                task.status.label
                            },
                            color = task.status.contentColor,
                        )
                    },
                    leadingIcon = if (task.isUpdating) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    } else {
                        null
                    },
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(text = task.priority.label)
                    },
                )

                MeSpaceTextButton(
                    text = if (task.isDeleting) {
                        "Removendo..."
                    } else {
                        "Remover"
                    },
                    onClick = onDeleteClick,
                    enabled = !task.isDeleting && !task.isUpdating,
                )
            }
        }
    }
}

private val TaskListItemUiModel.containerColor: Color
    @Composable
    get() = when (status) {
        TaskStatus.DONE -> MeSpaceTheme.semanticColors.successContainer.copy(alpha = 0.36f)
        else -> MaterialTheme.colorScheme.surface
    }

private val TaskStatus.contentColor: Color
    @Composable
    get() = when (this) {
        TaskStatus.PENDING -> MeSpaceTheme.semanticColors.warning
        TaskStatus.IN_PROGRESS -> MeSpaceTheme.semanticColors.info
        TaskStatus.DONE -> MeSpaceTheme.semanticColors.success
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
