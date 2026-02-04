package com.readlater.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.readlater.data.AuthState
import com.readlater.data.SavedEvent
import androidx.compose.foundation.clickable
import com.readlater.ui.components.CommitmentCard
import com.readlater.ui.components.MetroButton
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography
import com.readlater.ui.theme.rememberNoiseTexture
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    authState: AuthState,
    userName: String,
    upcomingEvents: List<SavedEvent>,
    completedEvents: List<SavedEvent>,
    archivedEvents: List<SavedEvent>,
    summaryMessage: String,
    isSyncing: Boolean,
    useDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onArchiveEvent: (SavedEvent) -> Unit,
    onRescheduleEvent: (SavedEvent) -> Unit,
    onMarkDoneEvent: (SavedEvent) -> Unit,
    onUndoCompleteEvent: (SavedEvent) -> Unit,
    onScheduleAgainEvent: (SavedEvent) -> Unit,
    onRestoreEvent: (SavedEvent) -> Unit,
    onDeleteForeverEvent: (SavedEvent) -> Unit,
    onManualAddEvent: suspend (String, String, LocalDate, LocalTime, Int) -> Result<Unit>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val noiseBitmap = rememberNoiseTexture()

    Box(modifier = modifier.fillMaxSize()) {
        // Base Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CommitColors.Paper)
        )
        
        // Noise Overlay (Multiply)
        Image(
            bitmap = noiseBitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.08f), // Opacity 8%
            contentScale = ContentScale.Crop, // Tile/Crop logic needed ideally, but filling typically works for noise
            colorFilter = ColorFilter.tint(CommitColors.Ink, BlendMode.Multiply)
        )

        // Content
        when (authState) {
            is AuthState.Loading -> LoadingScreen(Modifier.fillMaxSize())
            is AuthState.NotAuthenticated -> NotAuthenticatedScreen(onConnectClick, Modifier.fillMaxSize())
            is AuthState.Authenticated -> {
                CommitHomeScreen(
                    upcomingEvents = upcomingEvents,
                    completedEvents = completedEvents,
                    onRescheduleEvent = onRescheduleEvent,
                    onMarkDoneEvent = onMarkDoneEvent,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is AuthState.Error -> ErrorScreen(authState.message, onConnectClick, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun CommitHomeScreen(
    upcomingEvents: List<SavedEvent>,
    completedEvents: List<SavedEvent>,
    onRescheduleEvent: (SavedEvent) -> Unit,
    onMarkDoneEvent: (SavedEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = Instant.now()
    val zoneId = ZoneId.systemDefault()
    val todayDate = LocalDate.now(zoneId)

    // Filter events
    val (todayEvents, futureEvents) = remember(upcomingEvents) {
         upcomingEvents
             .sortedBy { it.scheduledDateTime }
             .partition { 
                 Instant.ofEpochMilli(it.scheduledDateTime)
                     .atZone(zoneId)
                     .toLocalDate() == todayDate 
             }
    }

    val currentCommitment = todayEvents.firstOrNull() 
    val remainingToday = todayEvents.drop(1)
    val laterEvents = remainingToday + futureEvents

    // Streaks
    val streak = remember(completedEvents) { calculateStreak(completedEvents) }
    
    // Reflection Dialog State
    var eventToReflect by remember { androidx.compose.runtime.mutableStateOf<SavedEvent?>(null) }

    if (eventToReflect != null) {
        com.readlater.ui.components.ReflectionDialog(
            onDismiss = { eventToReflect = null },
            onSubmit = { wasWorthIt ->
                // Here we would log the 'wasWorthIt' result
                onMarkDoneEvent(eventToReflect!!)
                eventToReflect = null
            }
        )
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(60.dp))
            HeaderSection(remainingCount = todayEvents.size, streak = streak)
        }

        item {
            SectionLabel(text = "Now")
            if (currentCommitment != null) {
                // Stack Hint must be BEHIND the card visually.
                Box {
                    StackHint() // Drawn first = Behind
                    CommitmentCard(
                        event = currentCommitment,
                        onReschedule = { onRescheduleEvent(currentCommitment) },
                        onKeep = { eventToReflect = currentCommitment } // Trigger reflection
                    )
                }
            } else {
                 Text(
                     text = "No commitments for today.",
                     style = CommitTypography.CardSubtitle.copy(color = CommitColors.InkSoft),
                     modifier = Modifier.padding(vertical = 24.dp)
                 )
            }
        }

        if (laterEvents.isNotEmpty()) {
            item {
                SectionLabel(text = "Later")
                Column {
                    laterEvents.forEach { event ->
                        UpcomingItem(event = event)
                    }
                }
            }
        }

        if (completedEvents.isNotEmpty()) {
            item {
                SectionLabel(text = "Log")
                Column(
                    modifier = Modifier.alpha(0.5f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    completedEvents.take(5).forEach { event -> 
                        CompletedItem(event = event)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderSection(
    remainingCount: Int = 0,
    streak: Int = 0
) {
    val today = LocalDate.now()
    val now = LocalTime.now()
    val dateStr = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    
    val greeting = remember(now, remainingCount) {
        val hour = now.hour
        when {
            hour < 12 -> "Good morning.\nHere's what you committed to today."
            hour < 17 -> if (remainingCount > 0) "Good afternoon.\n$remainingCount commitments left for today." else "Good afternoon.\nYou've kept your commitments."
            else -> if (remainingCount > 0) "Good evening.\nFinish your day strong." else "Good evening.\nYou kept your commitments today."
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr.uppercase(),
                        style = CommitTypography.Date
                    )
                    if (streak > 0) {
                        Text(
                            text = "$streak DAY STREAK",
                            style = CommitTypography.Label.copy(color = CommitColors.Rust)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = greeting,
                    style = CommitTypography.Brand.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                        fontSize = 20.sp, 
                        lineHeight = 28.sp
                    )
                )
            }
        }
        HorizontalDivider(color = CommitColors.Line, thickness = 1.dp)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = CommitTypography.Label,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun StackHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Height of card roughly
            .padding(horizontal = 12.dp) // Indented
            .offset(y = 10.dp) // Pushed down to peek out
            .alpha(0.1f)
            .background(CommitColors.Ink) // Shadow/Stack color
            .zIndex(-1f) // Ensure behind if in same container scope
    )
}

@Composable
private fun UpcomingItem(event: SavedEvent) {
    val dateTime = Instant.ofEpochMilli(event.scheduledDateTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val timeStr = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeStr,
                style = CommitTypography.MonoTime,
                color = CommitColors.InkSoft,
                modifier = Modifier.width(60.dp)
            )
            Text(
                text = event.title,
                style = CommitTypography.TaskName
            )
        }
        HorizontalDivider(color = CommitColors.Line, thickness = 1.dp)
    }
}

@Composable
private fun CompletedItem(event: SavedEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(16.dp)
                .background(ColorFilter.tint(CommitColors.Rust).let { androidx.compose.ui.graphics.Color.Transparent }, androidx.compose.foundation.shape.CircleShape) 
                .run { 
                     // Simple checkmark visual
                     this 
                },
             contentAlignment = Alignment.Center
        ) {
             Text(text = "✓", color = CommitColors.Rust, style = CommitTypography.MonoTime.copy(fontSize = 12.sp))
        }
        Text(
            text = event.title,
            style = CommitTypography.TaskName.copy(
                fontSize = 14.sp,
                color = CommitColors.InkSoft,
                textDecoration = TextDecoration.LineThrough
            )
        )
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...", style = CommitTypography.Label)
    }
}

@Composable
private fun NotAuthenticatedScreen(
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation States
    var showBrand by remember { mutableStateOf(false) }
    var showDivider by remember { mutableStateOf(false) }
    var showHeadline by remember { mutableStateOf(false) }
    var showSubtext by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        showBrand = true
        kotlinx.coroutines.delay(200)
        showDivider = true
        kotlinx.coroutines.delay(100)
        showHeadline = true
        kotlinx.coroutines.delay(200)
        showSubtext = true
        kotlinx.coroutines.delay(200)
        showButtons = true
    }

    Column(
        modifier = modifier
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Brand
        androidx.compose.animation.AnimatedVisibility(
            visible = showBrand,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { 20 }
        ) {
            Text(
                "Commit.", 
                style = CommitTypography.Brand.copy(
                    fontSize = 36.sp, 
                    color = CommitColors.Ink,
                    letterSpacing = (-0.02).sp
                )
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        // Vertical Divider
        androidx.compose.animation.AnimatedVisibility(
            visible = showDivider,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically()
        ) {
             Box(
                 modifier = Modifier
                     .width(1.dp)
                     .height(40.dp)
                     .background(CommitColors.Line)
             )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Headline
        androidx.compose.animation.AnimatedVisibility(
            visible = showHeadline,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { 20 }
        ) {
            Text(
                text = "Bookmarks are intentions.\nCommitments are actions.",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light, // 400
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    color = CommitColors.Ink,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Subtext
        androidx.compose.animation.AnimatedVisibility(
            visible = showSubtext,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { 20 }
        ) {
             Text(
                text = "Stop saving. Start scheduling.",
                style = CommitTypography.CardSubtitle.copy(
                    fontSize = 15.sp, 
                    color = CommitColors.InkSoft,
                    fontFamily = CommitTypography.Serif,
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(56.dp))
        
        // Buttons
        androidx.compose.animation.AnimatedVisibility(
            visible = showButtons,
            enter = androidx.compose.animation.fadeIn()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetroButton(
                    text = "Connect Google Calendar",
                    onClick = onConnectClick,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = "SKIP FOR NOW",
                    style = CommitTypography.Label.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.1.em,
                        color = CommitColors.InkSoft.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .clickable { /* TODO: Implement skip */ }
                        .padding(12.dp)
                )
            }
        }
    }
}


@Composable
private fun ErrorScreen(
    message: String,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error", style = CommitTypography.Label)
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, style = CommitTypography.TaskName)
        Spacer(modifier = Modifier.height(24.dp))
        MetroButton(
            text = "Try Again",
            onClick = onConnectClick
        )
    }
}
