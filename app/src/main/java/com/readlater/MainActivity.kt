package com.readlater

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.readlater.data.AuthState
import com.readlater.data.SavedEvent
import com.readlater.ui.components.RescheduleDialog
import com.readlater.ui.screens.HomeScreen
import com.readlater.ui.theme.ReadLaterTheme
import com.readlater.util.UrlMetadataFetcher
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appContainer = (application as ReadLaterApp).container
            val eventRepository = appContainer.eventRepository
            val authRepository = appContainer.authRepository
            val themeRepository = appContainer.themeRepository
            val calendarRepository = appContainer.calendarRepository

            val useDarkTheme: Boolean by themeRepository.useDarkTheme.collectAsState(initial = true)

            ReadLaterTheme(useDarkTheme = useDarkTheme) {
                val authState: AuthState by authRepository.authState.collectAsState()
                val scope = rememberCoroutineScope()

                val signInLauncher =
                        rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                        ) { result -> authRepository.handleSignInResult(result.data) }

                // Event lists
                val upcomingEvents: List<SavedEvent> by
                        eventRepository.getUpcomingEvents().collectAsState(initial = emptyList())
                val completedEvents: List<SavedEvent> by
                        eventRepository.getCompletedEvents().collectAsState(initial = emptyList())
                val archivedEvents: List<SavedEvent> by
                        eventRepository.getArchivedEvents().collectAsState(initial = emptyList())

                // Summary message
                var summaryMessage: String by remember { mutableStateOf("") }

                // Loading/syncing states
                var isSyncing: Boolean by remember { mutableStateOf(false) }
                var isLoading: Boolean by remember { mutableStateOf(false) }

                // Dialog state for reschedule/schedule again
                var showRescheduleDialog: Boolean by remember { mutableStateOf(false) }
                var selectedEventForReschedule: SavedEvent? by remember { mutableStateOf(null) }
                var isScheduleAgain: Boolean by remember { mutableStateOf(false) }

                // Update summary message when events change
                LaunchedEffect(upcomingEvents, authState) {
                    if (authState is AuthState.Authenticated) {
                        summaryMessage = eventRepository.getSummaryMessage()
                    }
                }

                // Sync when authenticated
                LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated) {
                        val account = authRepository.getAccount()
                        if (account != null) {
                            isSyncing = true
                            try {
                                eventRepository.syncWithCalendar(account)
                                summaryMessage = eventRepository.getSummaryMessage()
                                eventRepository.refreshMissingImages()
                            } catch (e: Exception) {
                                // Silently fail sync
                            }
                            isSyncing = false
                        }
                    }
                }

                // Get user's first name
                val userName =
                        remember(authState) {
                            if (authState is AuthState.Authenticated) {
                                authRepository.getAccount()?.givenName?.lowercase() ?: ""
                            } else ""
                        }

                HomeScreen(
                        authState = authState,
                        userName = userName,
                        upcomingEvents = upcomingEvents,
                        completedEvents = completedEvents,
                        archivedEvents = archivedEvents,
                        summaryMessage = summaryMessage,
                        isSyncing = isSyncing,
                        useDarkTheme = useDarkTheme,
                        onToggleTheme = {
                            scope.launch { themeRepository.setUseDarkTheme(!useDarkTheme) }
                        },
                        onConnectClick = {
                            signInLauncher.launch(authRepository.getSignInIntent())
                        },
                        onDisconnectClick = {
                            scope.launch {
                                authRepository.signOut()
                                Toast.makeText(
                                                this@MainActivity,
                                                "disconnected",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                        },
                        onArchiveEvent = { event ->
                            scope.launch {
                                isLoading = true
                                val account = authRepository.getAccount()
                                if (account != null) {
                                    val result: Result<Unit> =
                                            eventRepository.archiveEvent(
                                                    account,
                                                    event.googleEventId
                                            )
                                    result
                                            .onSuccess {
                                                summaryMessage = eventRepository.getSummaryMessage()
                                                Toast.makeText(
                                                                this@MainActivity,
                                                                "event archived",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            }
                                            .onFailure { error: Throwable ->
                                                Toast.makeText(
                                                                this@MainActivity,
                                                                "failed: ${error.message}".lowercase(
                                                                        Locale.ROOT
                                                                ),
                                                                Toast.LENGTH_LONG
                                                        )
                                                        .show()
                                            }
                                }
                                isLoading = false
                            }
                        },
                        onRescheduleEvent = { event ->
                            selectedEventForReschedule = event
                            isScheduleAgain = false
                            showRescheduleDialog = true
                        },
                        onMarkDoneEvent = { event ->
                            scope.launch {
                                isLoading = true
                                val result: Result<Unit> =
                                        eventRepository.markAsCompleted(event.googleEventId)
                                result
                                        .onSuccess {
                                            summaryMessage = eventRepository.getSummaryMessage()
                                            Toast.makeText(
                                                            this@MainActivity,
                                                            "marked as done",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        }
                                        .onFailure { error: Throwable ->
                                            Toast.makeText(
                                                            this@MainActivity,
                                                            "failed: ${error.message}".lowercase(
                                                                    Locale.ROOT
                                                            ),
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        }
                                isLoading = false
                            }
                        },
                        onUndoCompleteEvent = { event ->
                            scope.launch {
                                isLoading = true
                                val result: Result<Unit> =
                                        eventRepository.undoComplete(event.googleEventId)
                                result
                                        .onSuccess {
                                            summaryMessage = eventRepository.getSummaryMessage()
                                            Toast.makeText(
                                                            this@MainActivity,
                                                            "moved back to upcoming",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        }
                                        .onFailure { error: Throwable ->
                                            Toast.makeText(
                                                            this@MainActivity,
                                                            "failed: ${error.message}".lowercase(
                                                                    Locale.ROOT
                                                            ),
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        }
                                isLoading = false
                            }
                        },
                        onScheduleAgainEvent = { event ->
                            selectedEventForReschedule = event
                            isScheduleAgain = true
                            showRescheduleDialog = true
                        },
                        onRestoreEvent = { event ->
                            scope.launch {
                                isLoading = true
                                val account = authRepository.getAccount()
                                if (account != null) {
                                    val result: Result<String> =
                                            eventRepository.restoreFromArchive(account, event)
                                    result
                                            .onSuccess {
                                                summaryMessage = eventRepository.getSummaryMessage()
                                                Toast.makeText(
                                                                this@MainActivity,
                                                                "event restored",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            }
                                            .onFailure { error: Throwable ->
                                                Toast.makeText(
                                                                this@MainActivity,
                                                                "failed: ${error.message}".lowercase(
                                                                        Locale.ROOT
                                                                ),
                                                                Toast.LENGTH_LONG
                                                        )
                                                        .show()
                                            }
                                }
                                isLoading = false
                            }
                        },
                        onDeleteForeverEvent = { event ->
                            scope.launch {
                                isLoading = true
                                val result: Result<Unit> =
                                        eventRepository.deleteEventPermanently(event.googleEventId)
                                result
                                        .onSuccess {
                                            Toast.makeText(
                                                            this@MainActivity,
                                                            "event deleted",
                                                            Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                        }
                                        .onFailure { error: Throwable ->
                                            Toast.makeText(
                                                            this@MainActivity,
                                                            "failed: ${error.message}".lowercase(
                                                                    Locale.ROOT
                                                            ),
                                                            Toast.LENGTH_LONG
                                                    )
                                                    .show()
                                        }
                                isLoading = false
                            }
                        },
                        onManualAddEvent = { url, title, date, time, duration ->
                            val account = authRepository.getAccount()
                            if (account == null) {
                                Result.failure(IllegalStateException("not connected"))
                            } else {
                                val dateTime = LocalDateTime.of(date, time)
                                val imageUrl = UrlMetadataFetcher.fetchImageUrl(url)
                                val result: Result<String> =
                                        calendarRepository.createEvent(
                                                account = account,
                                                title = title,
                                                description = url,
                                                imageUrl = imageUrl,
                                                startDateTime = dateTime,
                                                durationMinutes = duration
                                        )
                                result.fold(
                                        onSuccess = { eventId: String ->
                                            eventRepository.saveEvent(
                                                    googleEventId = eventId,
                                                    title = title,
                                                    url = url,
                                                    imageUrl = imageUrl,
                                                    scheduledDateTime = dateTime,
                                                    durationMinutes = duration
                                            )
                                            summaryMessage = eventRepository.getSummaryMessage()
                                            Result.success(Unit)
                                        },
                                        onFailure = { error -> Result.failure(error) }
                                )
                            }
                        }
                )

                // Reschedule/Schedule Again Dialog
                if (showRescheduleDialog && selectedEventForReschedule != null) {
                    val targetEvent: SavedEvent = selectedEventForReschedule!!
                    val eventDateTime =
                            Instant.ofEpochMilli(targetEvent.scheduledDateTime)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()

                    // For schedule again, default to next hour from now
                    val defaultDate =
                            if (isScheduleAgain) LocalDate.now() else eventDateTime.toLocalDate()
                    val defaultTime =
                            if (isScheduleAgain) {
                                LocalTime.now().plusHours(1).withMinute(0).withSecond(0)
                            } else {
                                eventDateTime.toLocalTime()
                            }

                    RescheduleDialog(
                            title = if (isScheduleAgain) "schedule again" else "reschedule",
                            initialDate = defaultDate,
                            initialTime = defaultTime,
                            initialDuration = targetEvent.durationMinutes,
                            onDismiss = {
                                showRescheduleDialog = false
                                selectedEventForReschedule = null
                            },
                            onConfirm = { newDateTime, newDuration ->
                                scope.launch {
                                    isLoading = true
                                    showRescheduleDialog = false
                                    val account = authRepository.getAccount()
                                    if (account != null) {
                                        if (isScheduleAgain) {
                                            val result: Result<String> =
                                                    eventRepository.scheduleAgain(
                                                            account = account,
                                                            originalEvent = targetEvent,
                                                            newDateTime = newDateTime,
                                                            durationMinutes = newDuration
                                                    )
                                            result
                                                    .onSuccess {
                                                        summaryMessage =
                                                                eventRepository.getSummaryMessage()
                                                        Toast.makeText(
                                                                        this@MainActivity,
                                                                        "event scheduled",
                                                                        Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                    }
                                                    .onFailure { error: Throwable ->
                                                        Toast.makeText(
                                                                        this@MainActivity,
                                                                        "failed: ${error.message}".lowercase(
                                                                                Locale.ROOT
                                                                        ),
                                                                        Toast.LENGTH_LONG
                                                                )
                                                                .show()
                                                    }
                                        } else {
                                            val result: Result<Unit> =
                                                    eventRepository.rescheduleEvent(
                                                            account = account,
                                                            eventId = targetEvent.googleEventId,
                                                            newDateTime = newDateTime,
                                                            durationMinutes = newDuration
                                                    )
                                            result
                                                    .onSuccess {
                                                        summaryMessage =
                                                                eventRepository.getSummaryMessage()
                                                        Toast.makeText(
                                                                        this@MainActivity,
                                                                        "event rescheduled",
                                                                        Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                    }
                                                    .onFailure { error: Throwable ->
                                                        Toast.makeText(
                                                                        this@MainActivity,
                                                                        "failed: ${error.message}".lowercase(
                                                                                Locale.ROOT
                                                                        ),
                                                                        Toast.LENGTH_LONG
                                                                )
                                                                .show()
                                                    }
                                        }
                                    }
                                    selectedEventForReschedule = null
                                    isLoading = false
                                }
                            }
                    )
                }
            }
        }
    }
}
