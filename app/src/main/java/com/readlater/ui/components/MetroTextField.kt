package com.readlater.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography

@Composable
fun MetroTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        placeholder: String = "",
        singleLine: Boolean = true
) {
        Column(modifier = modifier.fillMaxWidth()) {
                Text(
                        text = label,
                        style = CommitTypography.Label,
                        color = CommitColors.InkSoft,
                        modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .border(1.dp, CommitColors.Line)
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                        BasicTextField(
                                value = value,
                                onValueChange = onValueChange,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle =
                                        CommitTypography.TaskName.copy(color = CommitColors.Ink),
                                singleLine = singleLine,
                                cursorBrush = SolidColor(CommitColors.Ink),
                                decorationBox = { innerTextField ->
                                        if (value.isBlank() && placeholder.isNotEmpty()) {
                                                Text(
                                                        text = placeholder,
                                                        style = CommitTypography.TaskName,
                                                        color = CommitColors.InkSoft
                                                )
                                        }
                                        innerTextField()
                                }
                        )
                }
        }
}
