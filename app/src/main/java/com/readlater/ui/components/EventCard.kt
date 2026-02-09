package com.readlater.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readlater.data.EventStatus
import com.readlater.data.SavedEvent
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun formatDuration(minutes: Int): String {
    return when {
        minutes < 60 -> "${minutes} min"
        minutes == 60 -> "1 hr"
        minutes % 60 == 0 -> "${minutes / 60} hrs"
        minutes < 120 -> "1 hr ${minutes % 60} min"
        else -> "${minutes / 60} hrs ${minutes % 60} min"
    }
}

@Composable
fun EventCard(
        event: SavedEvent,
        onArchiveClick: () -> Unit,
        onRescheduleClick: () -> Unit,
        onDoneClick: () -> Unit,
        onUrlClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val dateTime =
            Instant.ofEpochMilli(event.scheduledDateTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

    val today = LocalDate.now()
    val eventDate = dateTime.toLocalDate()
    val isOverdue = event.scheduledDateTime < System.currentTimeMillis()

    val dateText =
            when (eventDate) {
                today -> "today"
                today.minusDays(1) -> "yesterday"
                today.plusDays(1) -> "tomorrow"
                else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d")).lowercase()
            }
    val timeText = dateTime.format(DateTimeFormatter.ofPattern("h:mm a")).lowercase()

    Surface(
            modifier =
                    modifier.fillMaxWidth()
                            .border(BorderStroke(1.dp, CommitColors.Line.copy(alpha = 0.3f))),
            shape = RectangleShape,
            color = CommitColors.DarkCard
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                    text = event.title.ifBlank { "untitled" }.lowercase(),
                    style = CommitTypography.CardTitle.copy(fontSize = 14.sp),
                    color = CommitColors.Surface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                    text = event.url.lowercase(),
                    style = CommitTypography.Label,
                    color = CommitColors.Surface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onUrlClick() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                        text = "$dateText · $timeText · ${formatDuration(event.durationMinutes)}",
                        style = CommitTypography.Label.copy(fontSize = 11.sp),
                        color = CommitColors.Surface.copy(alpha = 0.6f)
                )
            }

            if (isOverdue && event.status == EventStatus.SCHEDULED) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                        text = "overdue",
                        style = CommitTypography.Label.copy(fontSize = 11.sp),
                        color = CommitColors.RedAccent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextAction(text = "archive", onClick = onArchiveClick)
                Spacer(modifier = Modifier.width(16.dp))
                TextAction(text = "reschedule", onClick = onRescheduleClick)
                Spacer(modifier = Modifier.width(16.dp))
                TextAction(text = "done", onClick = onDoneClick, primary = true)
            }
        }
    }
}

@Composable
fun CompletedEventCard(
        event: SavedEvent,
        onUndoClick: () -> Unit,
        onScheduleAgainClick: () -> Unit,
        onUrlClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val completedAt =
            event.completedAt?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }
    val today = LocalDate.now()
    val completedText =
            when (completedAt) {
                today -> "completed today"
                today.minusDays(1) -> "completed yesterday"
                else ->
                        completedAt?.let {
                            "completed ${it.format(DateTimeFormatter.ofPattern("MMM d")).lowercase()}"
                        }
                                ?: "completed"
            }

    Surface(
            modifier =
                    modifier.fillMaxWidth()
                            .border(BorderStroke(1.dp, CommitColors.Line.copy(alpha = 0.3f))),
            shape = RectangleShape,
            color = CommitColors.DarkCard
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                    text = event.title.ifBlank { "untitled" }.lowercase(),
                    style = CommitTypography.CardTitle.copy(fontSize = 14.sp),
                    color = CommitColors.Surface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                    text = event.url.lowercase(),
                    style = CommitTypography.Label,
                    color = CommitColors.Surface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onUrlClick() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                    text = completedText,
                    style = CommitTypography.Label.copy(fontSize = 11.sp),
                    color = CommitColors.Surface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextAction(text = "undo", onClick = onUndoClick)
                Spacer(modifier = Modifier.width(16.dp))
                TextAction(text = "schedule again", onClick = onScheduleAgainClick, primary = true)
            }
        }
    }
}

@Composable
fun ArchivedEventCard(
        event: SavedEvent,
        onRestoreClick: () -> Unit,
        onDeleteForeverClick: () -> Unit,
        onUrlClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val archivedAt =
            event.archivedAt?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }
    val today = LocalDate.now()
    val archivedText =
            when (archivedAt) {
                today -> "archived today"
                today.minusDays(1) -> "archived yesterday"
                else ->
                        archivedAt?.let {
                            "archived ${it.format(DateTimeFormatter.ofPattern("MMM d")).lowercase()}"
                        }
                                ?: "archived"
            }

    Surface(
            modifier =
                    modifier.fillMaxWidth()
                            .border(BorderStroke(1.dp, CommitColors.Line.copy(alpha = 0.3f))),
            shape = RectangleShape,
            color = CommitColors.DarkCard
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                    text = event.title.ifBlank { "untitled" }.lowercase(),
                    style = CommitTypography.CardTitle.copy(fontSize = 14.sp),
                    color = CommitColors.Surface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                    text = event.url.lowercase(),
                    style = CommitTypography.Label,
                    color = CommitColors.Surface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onUrlClick() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                    text = archivedText,
                    style = CommitTypography.Label.copy(fontSize = 11.sp),
                    color = CommitColors.Surface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextAction(text = "restore", onClick = onRestoreClick)
                Spacer(modifier = Modifier.width(16.dp))
                TextAction(text = "delete", onClick = onDeleteForeverClick, destructive = true)
            }
        }
    }
}

@Composable
private fun TextAction(
        text: String,
        onClick: () -> Unit,
        primary: Boolean = false,
        destructive: Boolean = false
) {
    val color =
            when {
                destructive -> CommitColors.RedAccent
                primary -> CommitColors.Surface
                else -> CommitColors.Surface.copy(alpha = 0.6f)
            }

    Text(
            text = text,
            style = CommitTypography.Label.copy(fontSize = 11.sp),
            color = color,
            modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp)
    )
}
