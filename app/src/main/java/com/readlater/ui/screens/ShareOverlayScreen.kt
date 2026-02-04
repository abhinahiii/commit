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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.readlater.ui.components.MetroButton
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography
import com.readlater.ui.theme.rememberNoiseTexture
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

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
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Modal Overlay Background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x662B2624)) // Backdrop blur equivalent color
            .clickable(enabled = false) {},
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(CommitColors.Paper)
    ) {
        // Noise Overlay
        Image(
            bitmap = noiseBitmap,
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.08f),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(CommitColors.Ink, BlendMode.Multiply)
        )

        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "When will you\ncommit to this?",
                style = CommitTypography.DisplayLarge.copy(
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                color = CommitColors.Ink
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Link Preview
            LinkPreviewBlock(title, url)

            Spacer(modifier = Modifier.height(28.dp))

            // Smart Chips
            Text("SUGGESTED TIMES", style = CommitTypography.Label)
            Spacer(modifier = Modifier.height(12.dp))
            
            val suggestions = listOf(
                "Tomorrow Morning" to LocalDateTime.now().plusDays(1).withHour(9).withMinute(0),
                "This Weekend" to LocalDateTime.now().with(TemporalAdjusters.next(java.time.DayOfWeek.SATURDAY)).withHour(10).withMinute(0),
                "Next Monday" to LocalDateTime.now().with(TemporalAdjusters.next(java.time.DayOfWeek.MONDAY)).withHour(9).withMinute(0)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { (label, dateTime) ->
                    val isSelected = selectedDate == dateTime.toLocalDate() // roughly
                    ChipButton(
                        text = label, 
                        isSelected = isSelected,
                        onClick = { onDateTimeSelected(dateTime.toLocalDate(), dateTime.toLocalTime()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Calendar / Pick Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("OR PICK A TIME", style = CommitTypography.Label)
                Text(
                    text = "${selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"))} @ ${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                    style = CommitTypography.MonoTime.copy(color = CommitColors.Rust),
                    modifier = Modifier
                        .border(width = 0.dp, color = Color.Transparent) // Placeholder for dashed border if we had it
                        .clickable { /* Trigger picker */ }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Visual Calendar Grid (Mocked Viz)
            CalendarAvailabilityGrid()

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(28.dp))

            // Warning Box
            if (selectedDuration >= 60 || true) { // Always show for demo/fidelity
                WarningBox() 
                Spacer(modifier = Modifier.height(28.dp))
            }

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
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CommitColors.Line)
            .background(Color(0x66F2F0EB)) // Transparent Cream
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail Placeholder
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Color.Gray)
        ) {
            // In real app, Load image. For now, gray box.
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .border(1.dp, CommitColors.Rust)
                        .padding(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        "VIDEO", 
                        style = CommitTypography.Label.copy(fontSize = 9.sp, color = CommitColors.Rust)
                    )
                }
                Text(
                   text = "youtube.com", // Mock domain from URL logic
                   style = CommitTypography.Label.copy(fontSize = 10.sp),
                   maxLines = 1
                )
            }
            Text(
                text = if (title.isBlank()) "Untitled" else title,
                style = CommitTypography.CardTitle.copy(
                    color = CommitColors.Ink, 
                    fontSize = 15.sp, 
                    lineHeight = 18.sp
                ),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ChipButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, CommitColors.Rust)
            .background(if (isSelected) CommitColors.Rust else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = CommitTypography.Brand.copy(
                fontSize = 13.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                color = if (isSelected) CommitColors.Cream else CommitColors.Rust
            )
        )
    }
}

@Composable
private fun DurationBlock(minutes: Int, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, if (isSelected) CommitColors.Rust else CommitColors.Line)
            .background(if (isSelected) CommitColors.Rust else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${minutes}m",
            style = CommitTypography.MonoTime.copy(
                fontSize = 12.sp,
                color = if (isSelected) CommitColors.Cream else CommitColors.InkSoft
            )
        )
    }
}

@Composable
private fun WarningBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CommitColors.Cream)
            .padding(12.dp)
            .padding(start = 4.dp), // Simulation of border-left logic
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left Border Simulation
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp) // Approximate height
                .background(CommitColors.Rust)
        )
        
        Column {
             Text(
                "Careful. You've already committed to 3 hours today.",
                style = CommitTypography.Brand.copy(fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Normal)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "VIEW SCHEDULE",
                style = CommitTypography.Label.copy(
                    fontSize = 9.sp, 
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    color = CommitColors.InkSoft
                )
            )
        }
    }
}

@Composable
private fun CalendarAvailabilityGrid() {
    // 7 Columns, 4 Rows mock
    // Visual only
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val blocks = listOf(
        listOf(true, false, true, true), // M
        listOf(false, true, true, false), // T
        listOf(true, true, false, true), // W
        listOf(true, false, true, false), // T
        listOf(false, true, false, false), // F
        listOf(false, false, true, true), // S
        listOf(true, true, false, false)  // S
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CommitColors.Line)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { index, day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(day, style = CommitTypography.Label.copy(fontSize = 9.sp))
                    val dayBlocks = blocks[index]
                    dayBlocks.forEach { isBusy ->
                        Box(
                            modifier = Modifier
                                .width(30.dp) // rough width dist
                                .height(4.dp)
                                .background(if (isBusy) CommitColors.InkSoft.copy(alpha = 0.4f) else CommitColors.Line.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }
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
