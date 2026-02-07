package com.readlater.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.readlater.data.AuthState
import com.readlater.data.SavedEvent
import com.readlater.ui.components.MetroButton
import com.readlater.ui.theme.CommitBorders
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography
import com.readlater.ui.theme.premiumShadow
import com.readlater.ui.theme.rememberNoiseTexture
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

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
                Box(modifier = Modifier.fillMaxSize().background(CommitColors.Paper))

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
                        is AuthState.NotAuthenticated ->
                                NotAuthenticatedScreen(onConnectClick, Modifier.fillMaxSize())
                        is AuthState.Authenticated -> {
                                DashboardScreen(
                                        upcomingEvents = upcomingEvents,
                                        completedEvents = completedEvents,
                                        onRescheduleEvent = onRescheduleEvent,
                                        onMarkDoneEvent = onMarkDoneEvent,
                                        onUndoCompleteEvent = onUndoCompleteEvent, // Pass it down
                                        modifier = Modifier.fillMaxSize()
                                )
                        }
                        is AuthState.Error ->
                                ErrorScreen(
                                        authState.message,
                                        onConnectClick,
                                        Modifier.fillMaxSize()
                                )
                }
        }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun DashboardScreen(
        upcomingEvents: List<SavedEvent>,
        completedEvents: List<SavedEvent>,
        onRescheduleEvent: (SavedEvent) -> Unit,
        onMarkDoneEvent: (SavedEvent) -> Unit,
        onUndoCompleteEvent: (SavedEvent) -> Unit,
        modifier: Modifier = Modifier
) {
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

        // Sync tab selection with pager
        val selectedTab = pagerState.currentPage

        // Toast State
        var showCompleteMessage by remember { mutableStateOf(false) }

        // Auto-hide toast
        androidx.compose.runtime.LaunchedEffect(showCompleteMessage) {
                if (showCompleteMessage) {
                        kotlinx.coroutines.delay(3000)
                        showCompleteMessage = false
                }
        }

        val handleMarkDone = { event: SavedEvent ->
                showCompleteMessage = true
                onMarkDoneEvent(event)
        }

        Box(modifier = modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                        // Header (Logo)
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(top = 48.dp, bottom = 24.dp),
                                contentAlignment = Alignment.Center
                        ) {
                                Text(
                                        "Commit.",
                                        style =
                                                CommitTypography.Brand.copy(
                                                        fontSize = 36.sp,
                                                        color = CommitColors.Ink,
                                                        letterSpacing = (-0.02).sp
                                                )
                                )
                        }

                        // Tabs
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth().drawBehind {
                                                drawLine(
                                                        CommitColors.Line,
                                                        Offset(0f, size.height),
                                                        Offset(size.width, size.height),
                                                        strokeWidth = 1.dp.toPx()
                                                )
                                        },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom
                        ) {
                                Box(modifier = Modifier.weight(1f)) {
                                        TabItem("UPCOMING", selectedTab == 0) {
                                                coroutineScope.launch {
                                                        pagerState.animateScrollToPage(0)
                                                }
                                        }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                        TabItem("COMPLETED", selectedTab == 1) {
                                                coroutineScope.launch {
                                                        pagerState.animateScrollToPage(1)
                                                }
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Swipeable Content
                        androidx.compose.foundation.pager.HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.weight(1f)
                        ) { page ->
                                val events =
                                        if (page == 0) {
                                                upcomingEvents.sortedBy { it.scheduledDateTime }
                                        } else {
                                                completedEvents.sortedByDescending {
                                                        it.completedAt
                                                }
                                        }

                                LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding =
                                                androidx.compose.foundation.layout.PaddingValues(
                                                        bottom = 120.dp
                                                )
                                ) {
                                        items(events) { event ->
                                                EventListItem(
                                                        event = event,
                                                        isCompleted = page == 1,
                                                        onMarkDone = { handleMarkDone(event) },
                                                        onReschedule = { onRescheduleEvent(event) },
                                                        onUndo = { onUndoCompleteEvent(event) }
                                                )
                                        }

                                        if (events.isEmpty()) {
                                                item {
                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(48.dp),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                Text(
                                                                        if (page == 0)
                                                                                "No upcoming commitments."
                                                                        else
                                                                                "No completed commitments yet.",
                                                                        style =
                                                                                CommitTypography
                                                                                        .CardSubtitle
                                                                                        .copy(
                                                                                                color =
                                                                                                        CommitColors
                                                                                                                .InkSoft,
                                                                                                fontStyle =
                                                                                                        FontStyle
                                                                                                                .Italic
                                                                                        )
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }

                // React 4 Toast
                androidx.compose.animation.AnimatedVisibility(
                        visible = showCompleteMessage,
                        enter =
                                androidx.compose.animation.fadeIn() +
                                        androidx.compose.animation.slideInVertically(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier =
                                Modifier.align(Alignment.TopCenter).padding(top = 96.dp).zIndex(10f)
                ) {
                        Box(
                                modifier =
                                        Modifier.background(CommitColors.DarkCard, CircleShape)
                                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                                .shadow(8.dp, CircleShape)
                        ) {
                                Text(
                                        "✓ Task completed!",
                                        style =
                                                CommitTypography.Label.copy(
                                                        color = CommitColors.Surface,
                                                        fontSize = 12.sp,
                                                        letterSpacing = 0.05.em
                                                )
                                )
                        }
                }
        }
}

@Composable
private fun TabItem(text: String, isActive: Boolean, onClick: () -> Unit) {
        // Fixed height box to prevent jumping
        Box(
                modifier = Modifier.clickable { onClick() }.height(34.dp),
                contentAlignment = Alignment.TopCenter
        ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                ) {
                        Text(
                                text,
                                style =
                                        CommitTypography.Label.copy(
                                                fontSize = 13.sp,
                                                letterSpacing = 1.2.sp,
                                                color =
                                                        if (isActive) CommitColors.Ink
                                                        else CommitColors.InkSoft,
                                                fontWeight =
                                                        if (isActive) FontWeight.Medium
                                                        else FontWeight.Normal
                                        )
                        )
                        if (isActive) {
                                Box(
                                        modifier =
                                                Modifier.width(24.dp)
                                                        .height(2.dp)
                                                        .background(CommitColors.RedAccent)
                                )
                        } else {
                                Spacer(modifier = Modifier.height(2.dp))
                        }
                }
        }
}

@Composable
private fun EventListItem(
        event: SavedEvent,
        isCompleted: Boolean,
        onMarkDone: () -> Unit,
        onReschedule: () -> Unit,
        onUndo: () -> Unit
) {
        val date = Instant.ofEpochMilli(event.scheduledDateTime).atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .drawBehind {
                                        drawLine(
                                                CommitColors.Line,
                                                Offset(0f, size.height),
                                                Offset(size.width, size.height),
                                                strokeWidth = CommitBorders.Hairline.toPx()
                                        )
                                }
                                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
                // Meta
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        Text(
                                date.format(formatter).uppercase(),
                                style =
                                        CommitTypography.MonoTime.copy(
                                                fontSize = 11.sp,
                                                letterSpacing = 0.8.sp,
                                                color = CommitColors.Ink.copy(alpha = 0.6f)
                                        )
                        )
                        Text(
                                "${date.format(timeFormatter)} — ${date.plusMinutes(event.durationMinutes.toLong()).format(timeFormatter)}",
                                style =
                                        CommitTypography.MonoTime.copy(
                                                fontSize = 11.sp,
                                                letterSpacing = 0.8.sp,
                                                color = CommitColors.Ink
                                        )
                        )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                        event.title,
                        style =
                                CommitTypography.DisplayLarge.copy(
                                        fontSize = 22.sp,
                                        lineHeight = 28.sp
                                )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Desc (Mock for now or use real if available)
                // Type | Source
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        // Type Tag
                        Box(
                                modifier =
                                        Modifier.border(1.dp, CommitColors.Rust.copy(alpha = 0.5f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                                val type =
                                        if (event.url.contains("youtube") ||
                                                        event.url.contains("youtu.be")
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

                        // Source
                        val domain =
                                try {
                                        Uri.parse(event.url).host?.removePrefix("www.") ?: event.url
                                } catch (e: Exception) {
                                        event.url
                                }
                        Text(
                                domain,
                                style =
                                        CommitTypography.Label.copy(
                                                fontSize = 11.sp,
                                                color = CommitColors.InkSoft
                                        )
                        )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!isCompleted) {
                                // Done Button (Primary)
                                Box(
                                        modifier =
                                                Modifier.background(CommitColors.RedAccent)
                                                        .border(
                                                                CommitBorders.Hairline,
                                                                CommitColors.RedAccent
                                                        )
                                                        .clickable { onMarkDone() }
                                                        .padding(
                                                                horizontal = 14.dp,
                                                                vertical = 7.dp
                                                        )
                                ) {
                                        Text(
                                                "DONE",
                                                style =
                                                        CommitTypography.Label.copy(
                                                                color = CommitColors.Surface,
                                                                fontSize = 11.sp,
                                                                letterSpacing = 0.8.sp
                                                        )
                                        )
                                }

                                // Reschedule Button (Secondary)
                                Box(
                                        modifier =
                                                Modifier.border(
                                                                CommitBorders.Hairline,
                                                                CommitColors.Line
                                                        )
                                                        .clickable { onReschedule() }
                                                        .padding(
                                                                horizontal = 14.dp,
                                                                vertical = 7.dp
                                                        )
                                ) {
                                        Text(
                                                "RESCHEDULE",
                                                style =
                                                        CommitTypography.Label.copy(
                                                                color = CommitColors.Ink,
                                                                fontSize = 11.sp,
                                                                letterSpacing = 0.8.sp
                                                        )
                                        )
                                }
                        } else {
                                // Undo Button (Secondary)
                                Box(
                                        modifier =
                                                Modifier.border(
                                                                CommitBorders.Hairline,
                                                                CommitColors.Line
                                                        )
                                                        .clickable { onUndo() }
                                                        .padding(
                                                                horizontal = 14.dp,
                                                                vertical = 7.dp
                                                        )
                                ) {
                                        Text(
                                                "UNDO COMPLETION",
                                                style =
                                                        CommitTypography.Label.copy(
                                                                color = CommitColors.Ink,
                                                                fontSize = 11.sp,
                                                                letterSpacing = 0.8.sp
                                                        )
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun BottomNav(modifier: Modifier = Modifier, onNewClick: () -> Unit) {
        // Floating Pill
        Box(
                modifier =
                        modifier.clip(RoundedCornerShape(50))
                                .background(CommitColors.DarkCard.copy(alpha = 0.95f))
                                .border(
                                        CommitBorders.Hairline,
                                        CommitColors.Surface.copy(alpha = 0.1f),
                                        RoundedCornerShape(50)
                                )
                                .premiumShadow(RoundedCornerShape(50))
                                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                        // Menu Icon
                        Text("☰", color = CommitColors.Surface.copy(alpha = 0.5f), fontSize = 20.sp)

                        // New Button
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable { onNewClick() }
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(28.dp)
                                                        .background(
                                                                CommitColors.Surface,
                                                                CircleShape
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) { Text("+", fontSize = 18.sp, color = CommitColors.Ink) }
                                Text(
                                        "New",
                                        style =
                                                CommitTypography.Brand.copy(
                                                        color = CommitColors.Surface,
                                                        fontSize = 18.sp
                                                )
                                )
                        }

                        // Notification Icon
                        Box {
                                Text(
                                        "Bell",
                                        color = CommitColors.Surface.copy(alpha = 0.5f),
                                        fontSize = 12.sp
                                ) // Simplify icon
                                Box(
                                        modifier =
                                                Modifier.size(6.dp)
                                                        .background(
                                                                CommitColors.RedAccent,
                                                                CircleShape
                                                        )
                                                        .align(Alignment.TopEnd)
                                )
                        }
                }
        }
}

// --- KEEPING NOT AUTHENTICATED SCREEN AS IS ---
@Composable
private fun NotAuthenticatedScreen(onConnectClick: () -> Unit, modifier: Modifier = Modifier) {
        Column(
                modifier = modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
                // Brand & Icon
                val context = LocalContext.current
                val imageBitmap =
                        remember(context) {
                                BitmapFactory.decodeResource(
                                                context.resources,
                                                com.readlater.R.drawable.app_icon_c
                                        )
                                        .asImageBitmap()
                        }
                androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(180.dp).padding(bottom = 24.dp)
                ) {
                        drawImage(
                                image = imageBitmap,
                                dstSize =
                                        androidx.compose.ui.unit.IntSize(
                                                size.width.toInt(),
                                                size.height.toInt()
                                        ),
                                blendMode = BlendMode.Multiply
                        )
                }

                Text(
                        "Commit.",
                        style =
                                CommitTypography.Brand.copy(
                                        fontSize = 36.sp,
                                        color = CommitColors.Ink,
                                        letterSpacing = (-0.02).sp
                                )
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Vertical Divider
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(CommitColors.Line))

                Spacer(modifier = Modifier.height(32.dp))

                // Headline
                Text(
                        text = "Bookmarks are intentions.\nCommitments are actions.",
                        style =
                                androidx.compose.ui.text.TextStyle(
                                        fontFamily =
                                                androidx.compose.ui.text.font.FontFamily.SansSerif,
                                        fontWeight =
                                                androidx.compose.ui.text.font.FontWeight
                                                        .Light, // 400
                                        fontSize = 26.sp,
                                        lineHeight = 32.sp,
                                        color = CommitColors.Ink,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Subtext
                Text(
                        text = "Stop saving. Start scheduling.",
                        style =
                                CommitTypography.CardSubtitle.copy(
                                        fontSize = 15.sp,
                                        color = CommitColors.InkSoft,
                                        fontFamily = CommitTypography.Serif,
                                ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(56.dp))

                // Buttons
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
                                style =
                                        CommitTypography.Label.copy(
                                                fontSize = 10.sp,
                                                letterSpacing = 0.1.em,
                                                color = CommitColors.InkSoft.copy(alpha = 0.6f)
                                        ),
                                modifier =
                                        Modifier.clickable { /* TODO: Implement skip */}
                                                .padding(12.dp)
                        )
                }
        }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
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
                modifier = modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
                Text("Error", style = CommitTypography.Label)
                Spacer(modifier = Modifier.height(12.dp))
                Text(message, style = CommitTypography.TaskName)
                Spacer(modifier = Modifier.height(24.dp))
                MetroButton(text = "Try Again", onClick = onConnectClick)
        }
}
