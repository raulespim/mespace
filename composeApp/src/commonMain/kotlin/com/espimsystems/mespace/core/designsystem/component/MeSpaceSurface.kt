package com.espimsystems.mespace.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme

@Composable
fun MeSpaceSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(MeSpaceTheme.spacing.large),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(MeSpaceTheme.radius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = MeSpaceTheme.elevation.small,
        ),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(contentPadding),
        ) {
            content()
        }
    }
}