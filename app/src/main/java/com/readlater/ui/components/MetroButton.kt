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
import androidx.compose.ui.unit.dp
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography

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
            shape = MaterialTheme.shapes.extraSmall, // Radius 2px
            colors = ButtonDefaults.buttonColors(
                containerColor = CommitColors.Rust,
                contentColor = CommitColors.Cream,
                disabledContainerColor = CommitColors.Line,
                disabledContentColor = CommitColors.InkSoft
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp, // Slight lift
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = text.uppercase(),
                style = CommitTypography.Label.copy(color = CommitColors.Cream)
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .height(48.dp)
                .fillMaxWidth(),
            enabled = enabled,
            shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CommitColors.Ink,
                disabledContentColor = CommitColors.InkSoft
            ),
            border = BorderStroke(
                1.dp,
                if (enabled) CommitColors.Ink else CommitColors.Line
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = text.uppercase(),
                style = CommitTypography.Label.copy(color = CommitColors.Ink)
            )
        }
    }
}
