package com.readlater.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import com.readlater.data.SavedEvent
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography
import com.readlater.ui.theme.coloredShadow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId

@Composable
fun CommitmentCard(
    event: SavedEvent,
    modifier: Modifier = Modifier,
    onReschedule: () -> Unit = {},
    onKeep: () -> Unit = {}
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val maxDrag = 300f
    
    // Derived values for rotation and opacity based on offsetX
    val rotation = offsetX.value * 0.05f
    val opacity = 1f - (offsetX.value.absoluteValue / (maxDrag * 3)).coerceIn(0f, 0.5f) // Subtle fade

    val dateTime = Instant.ofEpochMilli(event.scheduledDateTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val timeText = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    // Estimation: 1.5h duration roughly if not specified, or use duration
    val endTime = dateTime.plusMinutes(event.durationMinutes.toLong())
    val endTimeText = endTime.format(DateTimeFormatter.ofPattern("HH:mm"))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp) // min-height from CSS
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value.absoluteValue > 100) {
                                // Trigger action
                                if (offsetX.value > 0) {
                                    onKeep()
                                } else {
                                    onReschedule()
                                }
                                // Reset for now (or animate away if we handled the list removal)
                                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            } else {
                                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        // Clamp drag if desired, or let it fly
                        val newOffset = offsetX.value + dragAmount
                        // CSS logic: if(Math.abs(diff) < 100) ... but here we want to allow dragging further to trigger
                        offsetX.snapTo(newOffset)
                    }
                }
            }
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .rotate(rotation)
            .alpha(opacity)
            .coloredShadow(
                color = Color(0xFF2B2624), // rgba(43, 38, 36)
                alpha = 0.15f,
                borderRadius = 2.dp,
                shadowRadius = 40.dp,
                offsetY = 20.dp
            )
            .background(CommitColors.Rust, RoundedCornerShape(2.dp))
            .padding(vertical = 32.dp, horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.matchParentSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$timeText — $endTimeText",
                    style = CommitTypography.MonoTime,
                    color = CommitColors.Cream.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title.ifBlank { "Commitment" },
                    style = CommitTypography.CardTitle
                )
                
                if (event.title.length < 50) { // Simulate "span" for extra text if needed or just part of title
                     // If we had a subtitle field, we'd use it. For now, we can maybe show a static motivational text or metadata
                     Spacer(modifier = Modifier.height(12.dp))
                     Text(
                         text = "Focus on the outcome.", // Placeholder or check if description exists
                         style = CommitTypography.CardSubtitle
                     )
                }
            }

            // Footer / Actions Hint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CommitColors.Cream.copy(alpha = 0.2f))
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionHint(text = "← Reschedule")
                ActionHint(text = "Kept →")
            }
        }
    }
}

@Composable
private fun ActionHint(text: String) {
    Text(
        text = text.uppercase(),
        style = CommitTypography.Label.copy(
            color = CommitColors.Cream,
            fontSize = 9.sp,
            letterSpacing = 1.sp // 0.1em
        ),
        modifier = Modifier.alpha(0.6f)
    )
}
