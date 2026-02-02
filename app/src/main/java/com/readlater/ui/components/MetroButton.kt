package com.readlater.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun MetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = true
) {
    if (filled) {
        Button(
            onClick = onClick,
            modifier = modifier
                .height(48.dp)
                .fillMaxWidth(),
            enabled = enabled,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.outline,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = text.lowercase(),
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .height(48.dp)
                .fillMaxWidth(),
            enabled = enabled,
            shape = RectangleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.outline
            ),
            border = BorderStroke(
                1.dp,
                if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = text.lowercase(),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// Alias for backward compatibility
@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = true
) = MetroButton(text, onClick, modifier, enabled, filled)
