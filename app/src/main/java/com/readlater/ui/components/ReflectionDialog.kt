package com.readlater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography

@Composable
fun ReflectionDialog(
    onDismiss: () -> Unit,
    onSubmit: (Boolean?) -> Unit // true=worth it, false=not worth it, null=skip
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CommitColors.Paper)
                .border(1.dp, CommitColors.Ink)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "REFLECTION",
                    style = CommitTypography.Label,
                    color = CommitColors.InkSoft
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Was this worth\nyour time?",
                    style = CommitTypography.DisplayLarge.copy(fontSize = 24.sp, lineHeight = 30.sp),
                    textAlign = TextAlign.Center,
                    color = CommitColors.Ink
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CommitColors.Ink)
                            .clickable { onSubmit(true) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Yes", style = CommitTypography.TaskName)
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CommitColors.Ink)
                            .clickable { onSubmit(false) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No", style = CommitTypography.TaskName)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Skip",
                    style = CommitTypography.Label.copy(fontSize = 12.sp),
                    modifier = Modifier.clickable { onSubmit(null) },
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }
        }
    }
}
