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
import androidx.compose.ui.draw.drawBehind
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
    val (overdueEvents, todayEvents, futureEvents) = remember(upcomingEvents) {
        val sorted = upcomingEvents.sortedBy { it.scheduledDateTime }
        val now = Instant.now()
        
        val overdue = sorted.filter { 
            val dt = Instant.ofEpochMilli(it.scheduledDateTime).atZone(zoneId)
             dt.toLocalDate().isBefore(todayDate) || (dt.toLocalDate() == todayDate && dt.toLocalTime().isBefore(LocalTime.now()))
        }
        
        val today = sorted.filter { 
            val dt = Instant.ofEpochMilli(it.scheduledDateTime).atZone(zoneId).toLocalDate()
            dt == todayDate && !overdue.contains(it)
        }
        
        val future = sorted.filter { 
            val dt = Instant.ofEpochMilli(it.scheduledDateTime).atZone(zoneId).toLocalDate()
            dt.isAfter(todayDate)
        }
        
        Triple(overdue, today, future)
    }

    val streak = remember(completedEvents) { calculateStreak(completedEvents) }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header()

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
            ) {
                // Progress Card
                item {
                    ProgressCard(
                        completedCount = completedEvents.count { 
                             val dt = Instant.ofEpochMilli(it.completedAt ?: 0).atZone(zoneId).toLocalDate()
                             dt.isAfter(todayDate.minusDays(7))
                        },
                        totalGoal = 10,
                        streak = streak
                    )
                }

                // Overdue Section
                if (overdueEvents.isNotEmpty()) {
                    items(overdueEvents) { event ->
                        OverdueCard(event, onReschedule = { onRescheduleEvent(event) }, onComplete = { onMarkDoneEvent(event) })
                    }
                }

                // Today's Focus
                item {
                    TodaysFocusSection(todayEvents, onMarkDoneEvent)
                }

                // Looking Ahead
                if (futureEvents.isNotEmpty()) {
                    item {
                        LookingAheadSection(futureEvents)
                    }
                }
                
                // Footer Dots
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .padding(horizontal = 2.dp)
                                    .background(CommitColors.Line, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        }
        
        // Floating Bottom Nav
        BottomNav(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            onNewClick = { /* TODO */ }
        )
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("commit.", style = CommitTypography.Brand.copy(fontSize = 36.sp, letterSpacing = (-0.05).em))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val today = LocalDate.now()
            Text(
                today.format(DateTimeFormatter.ofPattern("MMMM d")).uppercase(),
                style = CommitTypography.Label.copy(color = CommitColors.InkSoft, fontSize = 10.sp, letterSpacing = 0.15.em)
            )
            Box(modifier = Modifier.size(4.dp).background(CommitColors.InkSoft, CircleShape))
            Text(
                today.format(DateTimeFormatter.ofPattern("EEEE")).uppercase(),
                style = CommitTypography.Label.copy(color = CommitColors.InkSoft, fontSize = 10.sp, letterSpacing = 0.15.em)
            )
        }
    }
}

@Composable
private fun ProgressCard(completedCount: Int, totalGoal: Int, streak: Int) {
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
        // Dark Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CommitColors.DarkCard)
                .clip(androidx.compose.ui.graphics.RectangleShape) // Sharp corners per design
        ) {
            // Noise Overlay
            Image(
                bitmap = rememberNoiseTexture(),
                contentDescription = null,
                modifier = Modifier.matchParentSize().alpha(0.1f),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(androidx.compose.ui.graphics.Color.White, BlendMode.SrcAtop)
            )
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "WEEKLY PROGRESS", 
                            style = CommitTypography.Label.copy(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // "7 of 10"
                        Row(verticalAlignment = Alignment.Bottom) {
                             Text(
                                 "$completedCount", 
                                 style = CommitTypography.DisplayLarge.copy(color = androidx.compose.ui.graphics.Color.White, fontSize = 32.sp)
                             )
                             Text(
                                 " of $totalGoal", 
                                 style = CommitTypography.Brand.copy(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
                             )
                        }
                    }
                    
                    Text(
                        "${(completedCount.toFloat() / totalGoal * 100).toInt()}%",
                        style = CommitTypography.Brand.copy(color = androidx.compose.ui.graphics.Color.White, fontSize = 42.sp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Bar
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f))) {
                   Box(modifier = Modifier.fillMaxWidth(completedCount / totalGoal.toFloat()).height(4.dp).background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.4f)))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Streak Footer
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Text("STREAK: ", style = CommitTypography.Label.copy(color = CommitColors.InkSoft))
                Text("$streak DAYS", style = CommitTypography.Label.copy(color = CommitColors.Ink, textDecoration = TextDecoration.Underline))
            }
            Text("VIEW HISTORY →", style = CommitTypography.Label.copy(color = CommitColors.InkSoft))
        }
    }
}

@Composable
private fun OverdueCard(event: SavedEvent, onReschedule: () -> Unit, onComplete: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        // Warning Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(6.dp).background(CommitColors.RedAccent, CircleShape))
            Text("ATTENTION REQUIRED", style = CommitTypography.Label.copy(color = CommitColors.RedAccent, fontWeight = FontWeight.Bold))
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Card content
        Box(
             modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Color.White)
                .border(1.dp, CommitColors.Line)
        ) {
            // Red Bar Left
            Box(modifier = Modifier.width(4.dp).matchParentSize().background(CommitColors.RedAccent).align(Alignment.CenterStart))
            
            Column(modifier = Modifier.padding(20.dp).padding(start = 12.dp)) { // Padding for red bar
                 Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                     Text("Overdue since Yesterday", style = CommitTypography.Brand.copy(color = CommitColors.RedAccent, fontSize = 18.sp))
                     // Menu Icon placeholder (...)
                     Text("...", style = CommitTypography.Label)
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 Text(event.title, style = CommitTypography.DisplayLarge.copy(fontSize = 24.sp))
                 Spacer(modifier = Modifier.height(8.dp))
                 Text("Explore how the digital world...", style = CommitTypography.CardSubtitle.copy(color = CommitColors.InkSoft, fontSize = 14.sp))
                 
                 Spacer(modifier = Modifier.height(16.dp))
                 HorizontalDivider(color = CommitColors.Line, modifier = Modifier.padding(vertical = 16.dp))
                 
                 Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                     // Secondary Button
                     Box(
                         modifier = Modifier
                             .weight(1f)
                             .border(1.dp, CommitColors.RedAccent)
                             .clickable { onReschedule() }
                             .padding(vertical = 10.dp),
                         contentAlignment = Alignment.Center
                     ) {
                         Text("RESCHEDULE", style = CommitTypography.Label.copy(color = CommitColors.RedAccent))
                     }
                     
                     // Primary Button
                     Box(
                         modifier = Modifier
                             .weight(1f)
                             .background(CommitColors.Ink)
                             .clickable { onComplete() }
                             .padding(vertical = 10.dp),
                         contentAlignment = Alignment.Center
                     ) {
                         Text("COMPLETE", style = CommitTypography.Label.copy(color = androidx.compose.ui.graphics.Color.White))
                     }
                 }
            }
        }
    }
}

@Composable
private fun TodaysFocusSection(events: List<SavedEvent>, onMarkDone: (SavedEvent) -> Unit) {
    if (events.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().border(width = 0.dp, color = androidx.compose.ui.graphics.Color.Transparent) // Reset
                .drawBehind { 
                    drawLine(
                        CommitColors.Line, 
                        Offset(0f, size.height), 
                        Offset(size.width, size.height), 
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TODAY'S FOCUS", style = CommitTypography.Label.copy(color = CommitColors.InkSoft, fontWeight = FontWeight.Bold))
            Text("${events.size} remaining", style = CommitTypography.Brand.copy(color = CommitColors.InkSoft))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Hero Card (First Item)
        val hero = events.first()
        HeroCard(hero, onMarkDone)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Scheduled Cards (Rest)
        events.drop(1).forEach { 
            ScheduledCard(it) 
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroCard(event: SavedEvent, onMarkDone: (SavedEvent) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Taller per design
            .background(androidx.compose.ui.graphics.Color.White)
            .border(1.dp, CommitColors.Line)
    ) {
        // Image Top Half
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
             Image(
                bitmap = rememberNoiseTexture(), // Placeholder for Unsplash
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.2f),
                colorFilter = ColorFilter.tint(CommitColors.Ink, BlendMode.Multiply)
             )
             Box(
                 modifier = Modifier.padding(12.dp).background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f))
                     .padding(horizontal = 8.dp, vertical = 4.dp)
             ) {
                 Text("DUE NOW", style = CommitTypography.Label.copy(color = CommitColors.Ink, fontSize = 9.sp))
             }
        }
        
        // Content Bottom Half
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(event.title, style = CommitTypography.DisplayLarge.copy(fontSize = 28.sp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Analyzing Nicholas Carr's argument...", style = CommitTypography.CardSubtitle.copy(color = CommitColors.InkSoft, fontSize = 15.sp))
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Avatars placeholder
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    Box(modifier = Modifier.size(24.dp).background(CommitColors.Line, CircleShape).border(1.dp, androidx.compose.ui.graphics.Color.White, CircleShape))
                    Box(modifier = Modifier.size(24.dp).background(CommitColors.Ink, CircleShape).border(1.dp, androidx.compose.ui.graphics.Color.White, CircleShape)) {
                         Text("+", color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.align(Alignment.Center), fontSize = 12.sp)
                    }
                }
                
                Row(modifier = Modifier.clickable { onMarkDone(event) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("START READING", style = CommitTypography.Label.copy(color = CommitColors.Ink, letterSpacing = 0.1.em))
                    Text("→", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ScheduledCard(event: SavedEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color(0xFFFCFBF9)) // Slight off-white
            .border(1.dp, CommitColors.Line) // Dashed ideally
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Time Circle
            Column(
                modifier = Modifier
                    .size(42.dp)
                    .background(androidx.compose.ui.graphics.Color.White, CircleShape)
                    .border(1.dp, CommitColors.Line, CircleShape),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 val time = Instant.ofEpochMilli(event.scheduledDateTime).atZone(ZoneId.systemDefault())
                 Text(if (time.hour >= 12) "PM" else "AM", style = CommitTypography.Label.copy(fontSize = 8.sp))
                 Text(time.format(DateTimeFormatter.ofPattern("hh")), style = CommitTypography.DisplayLarge.copy(fontSize = 16.sp, lineHeight = 16.sp))
            }
            
            Column {
                Text(event.title, style = CommitTypography.Brand.copy(fontSize = 20.sp, fontStyle = FontStyle.Normal))
                Text("${event.durationMinutes} min • Reflection", style = CommitTypography.CardSubtitle.copy(fontStyle = FontStyle.Italic, fontSize = 12.sp, color = CommitColors.InkSoft))
            }
        }
    }
}

@Composable
private fun LookingAheadSection(events: List<SavedEvent>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
        Text(
            "LOOKING AHEAD", 
            style = CommitTypography.Label.copy(color = CommitColors.InkSoft, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 24.dp)
        )
        
        Column(modifier = Modifier.padding(start = 24.dp)) { // Indent for timeline
             Box {
                 // Vertical Line
                 Box(
                     modifier = Modifier
                         .width(1.dp)
                         .matchParentSize()
                         .background(CommitColors.Line)
                         .offset(x = (-32).dp) // Shift logic needed
                 )
                 
                 Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                     events.take(3).forEach { event ->
                         TimelineItem(event)
                     }
                 }
             }
        }
    }
}

@Composable
private fun TimelineItem(event: SavedEvent) {
    val date = Instant.ofEpochMilli(event.scheduledDateTime).atZone(ZoneId.systemDefault())
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .offset(x = (-29).dp) // Align with line
                .background(androidx.compose.ui.graphics.Color.White, CircleShape)
                .border(1.dp, CommitColors.InkSoft, CircleShape)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(date.format(DateTimeFormatter.ofPattern("EEEE")).uppercase(), style = CommitTypography.Label.copy(color = CommitColors.InkSoft, fontSize = 9.sp))
                Text(event.title, style = CommitTypography.DisplayLarge.copy(fontSize = 20.sp))
            }
            Text("read", style = CommitTypography.Brand.copy(color = CommitColors.Line, fontSize = 18.sp))
        }
    }
}

@Composable
private fun BottomNav(modifier: Modifier = Modifier, onNewClick: () -> Unit) {
    // Floating Pill
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(CommitColors.DarkCard.copy(alpha = 0.95f))
            .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Menu Icon
            Text("☰", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f), fontSize = 20.sp)
            
            // New Button
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onNewClick() }
            ) {
                 Box(
                     modifier = Modifier.size(28.dp).background(androidx.compose.ui.graphics.Color.White, CircleShape),
                     contentAlignment = Alignment.Center
                 ) {
                     Text("+", fontSize = 18.sp, color = CommitColors.Ink)
                 }
                 Text("New", style = CommitTypography.Brand.copy(color = androidx.compose.ui.graphics.Color.White, fontSize = 18.sp))
            }
            
            // Notification Icon
            Box {
                Text("Bell", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f), fontSize = 12.sp) // Simplify icon
                Box(modifier = Modifier.size(6.dp).background(CommitColors.RedAccent, CircleShape).align(Alignment.TopEnd))
            }
        }
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
