package com.espimsystems.mespace.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.espimsystems.mespace.core.designsystem.theme.MeSpaceTheme

@Composable
fun MeSpaceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(MeSpaceTheme.radius.medium),
        contentPadding = PaddingValues(
            horizontal = MeSpaceTheme.spacing.extraLarge,
            vertical = MeSpaceTheme.spacing.medium,
        ),
    ) {
        Text(text = text)
    }
}

@Composable
fun MeSpaceOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(MeSpaceTheme.radius.medium),
        contentPadding = PaddingValues(
            horizontal = MeSpaceTheme.spacing.extraLarge,
            vertical = MeSpaceTheme.spacing.medium,
        ),
    ) {
        Text(text = text)
    }
}

@Composable
fun MeSpaceTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(MeSpaceTheme.radius.medium),
        contentPadding = ButtonDefaults.TextButtonContentPadding,
    ) {
        Text(text = text)
    }
}