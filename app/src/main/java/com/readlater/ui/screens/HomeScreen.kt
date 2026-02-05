package com.readlater.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
import java.time.temporal.ChronoUnit

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
            modifier = Modifier.fillMaxSize().alpha(0.08f),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(CommitColors.Ink, BlendMode.Multiply)
        )

        // Content
        when (authState) {
            is AuthState.Loading -> LoadingScreen(Modifier.fillMaxSize())
            is AuthState.NotAuthenticated -> NotAuthenticatedScreen(onConnectClick, Modifier.fillMaxSize())
            is AuthState.Authenticated -> {
                DashboardScreen(
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
private fun DashboardScreen(
    upcomingEvents: List<SavedEvent>,
    completedEvents: List<SavedEvent>,
    onRescheduleEvent: (SavedEvent) -> Unit,
    onMarkDoneEvent: (SavedEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneId = ZoneId.systemDefault()
    val todayDate = LocalDate.now(zoneId)

    // Filter Logic
    val (todayEvents, otherEvents) = remember(upcomingEvents) {
        upcomingEvents
            .sortedBy { it.scheduledDateTime }
            .partition { 
                val dt = Instant.ofEpochMilli(it.scheduledDateTime)
                    .atZone(zoneId)
                    .toLocalDate()
                dt == todayDate || dt.isBefore(todayDate) // Include overdue in today for now or separate
            }
    }
    
    // Split Today into Overdue and Today
    val (overdueEvents, actualTodayEvents) = todayEvents.partition { 
         val dt = Instant.ofEpochMilli(it.scheduledDateTime).atZone(zoneId)
         dt.toLocalDate().isBefore(todayDate) || (dt.toLocalDate() == todayDate && dt.toLocalTime().isBefore(LocalTime.now()))
    }

    // Future
    val futureEvents = otherEvents.filter { 
         Instant.ofEpochMilli(it.scheduledDateTime).atZone(zoneId).toLocalDate().isAfter(todayDate)
    }

    // Streaks Calc
    val streak = remember(completedEvents) { calculateStreak(completedEvents) }
    
    // FAB Logic placeholder
    var showNewCommitment by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("commit.", style = CommitTypography.Brand.copy(fontSize = 32.sp))
                Text(
                    "SETTINGS", 
                    style = CommitTypography.Label.copy(
                        color = CommitColors.InkSoft,
                        letterSpacing = 0.2.em
                    ),
                    modifier = Modifier.clickable { /* Settings */ }
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp) // space for FAB
            ) {
                // Stats Header
                item {
                    StatsHeader(
                        completedCount = completedEvents.count { 
                            val dt = Instant.ofEpochMilli(it.completedAt ?: 0).atZone(zoneId).toLocalDate()
                            dt.isAfter(todayDate.minusDays(7)) 
                        },
                        totalGoal = 10, // Mock goal
                        streak = streak
                    )
                }

                // Today Section
                item {
                    SectionLabel("Today, ${todayDate.format(DateTimeFormatter.ofPattern("MMM d"))}")
                }
                
                if (overdueEvents.isNotEmpty()) {
                    items(overdueEvents) { event ->
                         OverdueCard(event, onReschedule = { onRescheduleEvent(event) }, onMarkDone = { onMarkDoneEvent(event) })
                         Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (actualTodayEvents.isNotEmpty()) {
                     val dueNow = actualTodayEvents.firstOrNull() // Simplify: First is hero
                     val others = actualTodayEvents.drop(1)
                     
                     if (dueNow != null) {
                         item {
                             HeroCard(dueNow, onMarkDone = { onMarkDoneEvent(dueNow) })
                             Spacer(modifier = Modifier.height(16.dp))
                         }
                     }
                     
                     items(others) { event ->
                         ScheduledCard(event)
                         Spacer(modifier = Modifier.height(12.dp))
                     }
                } else if (overdueEvents.isEmpty()) {
                    item {
                        Text(
                            "No commitments left for today.", 
                            style = CommitTypography.CardSubtitle.copy(color = CommitColors.InkSoft),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
                
                // Upcoming Section
                if (futureEvents.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        SectionLabel("Upcoming")
                    }
                    items(futureEvents) { event ->
                        UpcomingTimelineItem(event)
                    }
                }
            }
        }
        
        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            FloatingNewButton(onClick = { /* TODO: Open Share/Add */ })
        }
    }
}

@Composable
private fun StatsHeader(completedCount: Int, totalGoal: Int, streak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                buildAnnotatedString {
                    append("You kept ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, textDecoration = TextDecoration.Underline)) {
                        append("$completedCount of $totalGoal")
                    }
                    append("\ncommitments this week.")
                },
                style = CommitTypography.DisplayLarge.copy(fontSize = 28.sp, lineHeight = 32.sp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Bolt Icon placeholder
                Text("⚡", fontSize = 12.sp)
                Text(
                    "Current Streak: $streak Days", 
                    style = CommitTypography.Label.copy(color = CommitColors.InkSoft)
                )
            }
        }
        
        // Stat Ring
        StatRing(percentage = (completedCount.toFloat() / totalGoal.toFloat()).coerceIn(0f, 1f))
    }
}

@Composable
private fun StatRing(percentage: Float) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Track
            drawArc(
                color = CommitColors.Line,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Progress
            drawArc(
                color = CommitColors.Ink,
                startAngle = -90f,
                sweepAngle = 360f * percentage,
                useCenter = false,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${(percentage * 100).toInt()}%",
            style = CommitTypography.Label.copy(fontSize = 10.sp)
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            title.uppercase(),
            style = CommitTypography.Label.copy(color = CommitColors.InkSoft),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(color = CommitColors.Line.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OverdueCard(event: SavedEvent, onReschedule: () -> Unit, onMarkDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(CommitColors.Cream) // Slight contrast
            .border(1.dp, CommitColors.Line)
    ) {
        // Left Rust Border
        Box(
            modifier = Modifier
                .width(4.dp)
                .matchParentSize()
                .background(CommitColors.Rust)
                .align(Alignment.CenterStart)
        )
        
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(CommitColors.Rust, CircleShape))
                    Text("OVERDUE", style = CommitTypography.Label.copy(color = CommitColors.Rust))
                }
                Text(
                    "Reschedule", 
                    style = CommitTypography.Label.copy(color = CommitColors.InkSoft),
                    modifier = Modifier.clickable { onReschedule() }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(event.title, style = CommitTypography.CardTitle.copy(color = CommitColors.Ink, fontSize = 22.sp))
            // Spacer(modifier = Modifier.height(4.dp))
            // Text("Description placeholder", style = CommitTypography.CardSubtitle.copy(color = CommitColors.InkSoft, fontSize = 15.sp))
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = CommitColors.Line.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val date = Instant.ofEpochMilli(event.scheduledDateTime).atZone(ZoneId.systemDefault())
                Text(
                    if (date.toLocalDate() == LocalDate.now().minusDays(1)) "YESTERDAY" else date.format(DateTimeFormatter.ofPattern("MMM d")).uppercase(),
                    style = CommitTypography.Label.copy(color = CommitColors.Rust)
                )
                
                Box(
                    modifier = Modifier
                        .border(1.dp, CommitColors.Ink)
                        .clickable { onMarkDone() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("MARK KEPT", style = CommitTypography.Label.copy(color = CommitColors.Ink))
                }
            }
        }
    }
}

@Composable
private fun HeroCard(event: SavedEvent, onMarkDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(androidx.compose.ui.graphics.Color.White)
            .border(1.dp, CommitColors.Line.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.height(180.dp)) {
            // Image Placeholder (Left)
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxSize()
                    .background(CommitColors.Paper)
            ) {
                // Placeholder pattern or gray
                Image(
                    bitmap = rememberNoiseTexture(), // Just reusing noise for now
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.5f)
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "DUE NOW", 
                        style = CommitTypography.Label.copy(color = androidx.compose.ui.graphics.Color.White),
                        modifier = Modifier.background(CommitColors.Ink).padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    event.title, 
                    style = CommitTypography.DisplayLarge.copy(fontSize = 24.sp, lineHeight = 26.sp),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action
                MetroButton(
                    text = "READ & KEEP",
                    onClick = onMarkDone,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ScheduledCard(event: SavedEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f))
            .border(1.dp, CommitColors.Line, shape = androidx.compose.foundation.shape.GenericShape { size, _ -> 
                // Dashed border simulator if possible, or just solid for now
                moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }) // Simplified to solid for Compose without custom dash path effect easily inline
            .padding(20.dp)
    ) {
         val time = Instant.ofEpochMilli(event.scheduledDateTime).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("h:mm a"))
         Text(time, style = CommitTypography.Label.copy(color = CommitColors.InkSoft))
         Spacer(modifier = Modifier.height(8.dp))
         Text(event.title, style = CommitTypography.CardTitle.copy(color = CommitColors.Ink, fontSize = 20.sp))
    }
}

@Composable
private fun UpcomingTimelineItem(event: SavedEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(start = 8.dp) // shift for timeline
    ) {
        // Timeline Line
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) {
             Box(
                 modifier = Modifier
                     .size(11.dp)
                     .background(CommitColors.Paper)
                     .border(1.dp, CommitColors.Line, CircleShape)
             )
             Box(
                 modifier = Modifier
                     .width(1.dp)
                     .height(60.dp) // min height
                     .background(CommitColors.Line)
             )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.padding(top = -4.dp, bottom = 24.dp)) {
            val date = Instant.ofEpochMilli(event.scheduledDateTime).atZone(ZoneId.systemDefault())
            Text(
                date.format(DateTimeFormatter.ofPattern("EEEE")).uppercase(),
                style = CommitTypography.Label.copy(color = CommitColors.InkSoft)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                event.title,
                style = CommitTypography.CardTitle.copy(color = CommitColors.Ink, fontSize = 18.sp)
            )
        }
    }
}

@Composable
private fun FloatingNewButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(androidx.compose.ui.graphics.Color(0xCCFFFFFF)) // Blurred glass sim
            .border(1.dp, androidx.compose.ui.graphics.Color(0x1A000000), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(
            "+ new commitment",
            style = CommitTypography.Brand.copy(
                fontSize = 20.sp,
                fontStyle = FontStyle.Italic
            )
        )
    }
}




// --- KEEPING NOT AUTHENTICATED SCREEN AS IS ---
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
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...", style = CommitTypography.Label)
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
