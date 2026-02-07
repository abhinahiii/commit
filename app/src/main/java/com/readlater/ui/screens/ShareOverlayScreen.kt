package com.readlater.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readlater.ui.components.MetroButton
import com.readlater.ui.components.MetroDateTimePicker
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography
import com.readlater.ui.theme.rememberNoiseTexture
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ShareOverlayContent(
        title: String,
        onTitleChange: (String) -> Unit,
        url: String,
        selectedDate: LocalDate,
        selectedTime: LocalTime,
        onDateTimeSelected: (LocalDate, LocalTime) -> Unit,
        selectedDuration: Int,
        onDurationSelected: (Int) -> Unit,
        isLoading: Boolean,
        isFetchingTitle: Boolean,
        onCancel: () -> Unit,
        onSave: () -> Unit,
        modifier: Modifier = Modifier
) {
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) { isVisible = true }

        // Modal Overlay Background
        Box(
                modifier =
                        modifier.fillMaxSize()
                                .background(Color(0x662B2624)) // Backdrop blur equivalent color
                                .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
        ) {
                AnimatedVisibility(
                        visible = isVisible,
                        enter =
                                slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(400)
                                ) + fadeIn(animationSpec = tween(300))
                ) {
                        ShareSheet(
                                title = title,
                                url = url,
                                selectedDate = selectedDate,
                                selectedTime = selectedTime,
                                onDateTimeSelected = onDateTimeSelected,
                                selectedDuration = selectedDuration,
                                onDurationSelected = onDurationSelected,
                                isLoading = isLoading,
                                onCancel = onCancel,
                                onSave = onSave
                        )
                }
        }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShareSheet(
        title: String,
        url: String,
        selectedDate: LocalDate,
        selectedTime: LocalTime,
        onDateTimeSelected: (LocalDate, LocalTime) -> Unit,
        selectedDuration: Int,
        onDurationSelected: (Int) -> Unit,
        isLoading: Boolean,
        onCancel: () -> Unit,
        onSave: () -> Unit
) {
        val noiseBitmap = rememberNoiseTexture()

        Box(
                modifier =
                        Modifier.fillMaxWidth(0.9f) // Floating card width
                                .clip(RoundedCornerShape(16.dp)) // Reduced corner radius
                                .background(CommitColors.Paper)
        ) {
                // Noise Overlay
                Image(
                        bitmap = noiseBitmap,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize().alpha(0.08f),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(CommitColors.Ink, BlendMode.Multiply)
                )

                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                        // Header
                        Text(
                                text = "When will you\ncommit to this?",
                                style =
                                        CommitTypography.DisplayLarge.copy(
                                                fontSize = 26.sp,
                                                lineHeight = 32.sp,
                                                fontStyle =
                                                        androidx.compose.ui.text.font.FontStyle
                                                                .Italic
                                        ),
                                color = CommitColors.Ink
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Link Preview
                        LinkPreviewBlock(title, url)

                        Spacer(modifier = Modifier.height(24.dp))

                        // DateTime Picker (Replaces Suggested Times & Manual Pick)
                        MetroDateTimePicker(
                                selectedDate = selectedDate,
                                selectedTime = selectedTime,
                                onDateTimeSelected = onDateTimeSelected
                        )

                        // Duration
                        Text("DURATION", style = CommitTypography.Label)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                                val durations = listOf(15, 30, 45, 60)
                                durations.forEach { min ->
                                        DurationBlock(
                                                minutes = min,
                                                isSelected = selectedDuration == min,
                                                onClick = { onDurationSelected(min) },
                                                modifier = Modifier.weight(1f)
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                MetroButton(
                                        text = "Cancel",
                                        onClick = onCancel,
                                        filled = false,
                                        modifier = Modifier.weight(1f)
                                )
                                MetroButton(
                                        text = if (isLoading) "Scheduling..." else "Commit",
                                        onClick = onSave,
                                        enabled = !isLoading,
                                        modifier = Modifier.weight(1f) // Primary is rust by default
                                )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                }
        }
}

@Composable
private fun LinkPreviewBlock(title: String, url: String) {
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .border(1.dp, CommitColors.Line)
                                .background(Color(0x66F2F0EB)) // Transparent Cream
                                .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Meta Row
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                Box(
                                        modifier =
                                                Modifier.border(1.dp, CommitColors.Rust)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                        val type =
                                                if (url.contains("youtube") ||
                                                                url.contains("youtu.be")
                                                )
                                                        "VIDEO"
                                                else "ARTICLE"
                                        Text(
                                                type,
                                                style =
                                                        CommitTypography.Label.copy(
                                                                fontSize = 9.sp,
                                                                color = CommitColors.Rust
                                                        )
                                        )
                                }

                                val domain =
                                        try {
                                                java.net.URI(url).host?.removePrefix("www.") ?: url
                                        } catch (e: Exception) {
                                                if (url.length > 20) url.take(20) + "..." else url
                                        }
                                Text(
                                        text = domain,
                                        style =
                                                CommitTypography.Label.copy(
                                                        fontSize = 10.sp,
                                                        color = CommitColors.InkSoft
                                                ),
                                        maxLines = 1
                                )
                        }

                        // Title
                        Text(
                                text = if (title.isBlank()) "Untitled Object" else title,
                                style =
                                        CommitTypography.CardTitle.copy(
                                                color = CommitColors.Ink,
                                                fontSize = 16.sp,
                                                lineHeight = 20.sp
                                        ),
                                maxLines = 2
                        )
                }
        }
}

@Composable
private fun DurationBlock(
        minutes: Int,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
        Box(
                modifier =
                        modifier.border(
                                        1.dp,
                                        if (isSelected) CommitColors.Rust else CommitColors.Line
                                )
                                .background(
                                        if (isSelected) CommitColors.Rust else Color.Transparent
                                )
                                .clickable(onClick = onClick)
                                .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
        ) {
                Text(
                        text = "${minutes}m",
                        style =
                                CommitTypography.MonoTime.copy(
                                        fontSize = 12.sp,
                                        color =
                                                if (isSelected) CommitColors.Cream
                                                else CommitColors.InkSoft
                                )
                )
        }
}

@Composable
fun NotConnectedOverlay(
        onOpenApp: () -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier
) {
        Box(modifier = modifier.fillMaxSize().background(CommitColors.Paper).padding(24.dp)) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                ) {
                        Text("Not Connected", style = CommitTypography.DisplayLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                "Connect Google Calendar to start committing.",
                                style = CommitTypography.TaskName,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        MetroButton(text = "Open App", onClick = onOpenApp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                "Cancel",
                                style = CommitTypography.Label.copy(fontSize = 12.sp),
                                modifier = Modifier.clickable { onCancel() }
                        )
                }
        }
}
