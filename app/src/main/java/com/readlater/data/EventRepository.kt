package com.readlater.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.readlater.util.UrlMetadataFetcher
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.Flow

class EventRepository(context: Context, private val calendarRepository: CalendarRepository) {
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.savedEventDao()

    // Get all scheduled (not completed) events - sorted with overdue first
    fun getUpcomingEvents(): Flow<List<SavedEvent>> {
        return dao.getScheduledEvents()
    }

    // Get completed events
    fun getCompletedEvents(): Flow<List<SavedEvent>> {
        return dao.getCompletedEvents()
    }

    // Get archived events
    fun getArchivedEvents(): Flow<List<SavedEvent>> {
        return dao.getArchivedEvents()
    }

    // Get summary message for header
    suspend fun getSummaryMessage(): String {
        val currentTime = System.currentTimeMillis()
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay =
                today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val eventsToday = dao.countEventsForToday(startOfDay, endOfDay)
        val overdueCount = dao.countOverdueEvents(currentTime)
        val nextEvent = dao.getNextScheduledEvent(currentTime)

        return when {
            eventsToday == 0 && overdueCount == 0 && nextEvent == null -> {
                "no events scheduled."
            }
            eventsToday == 0 && overdueCount == 0 && nextEvent != null -> {
                val nextDate =
                        java.time.Instant.ofEpochMilli(nextEvent.scheduledDateTime)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                val formatter = DateTimeFormatter.ofPattern("EEEE")
                val dayName = nextDate.format(formatter).lowercase(Locale.ROOT)
                "no events today. next scheduled on $dayName."
            }
            eventsToday > 0 && overdueCount == 0 -> {
                if (eventsToday == 1) "you have 1 event today."
                else "you have $eventsToday events today."
            }
            eventsToday > 0 && overdueCount > 0 -> {
                val todayText =
                        if (eventsToday == 1) "1 event today" else "$eventsToday events today"
                val overdueText = if (overdueCount == 1) "1 overdue" else "$overdueCount overdue"
                "you have $todayText and $overdueText."
            }
            eventsToday == 0 && overdueCount > 0 -> {
                if (overdueCount == 1) "you have 1 overdue event."
                else "you have $overdueCount overdue events."
            }
            else -> "all caught up."
        }
    }

    suspend fun saveEvent(
            googleEventId: String,
            title: String,
            url: String,
            scheduledDateTime: LocalDateTime,
            durationMinutes: Int,
            imageUrl: String? = null
    ) {
        val epochMillis =
                scheduledDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val event =
                SavedEvent(
                        googleEventId = googleEventId,
                        title = title,
                        url = url,
                        imageUrl = imageUrl,
                        scheduledDateTime = epochMillis,
                        durationMinutes = durationMinutes,
                        createdAt = System.currentTimeMillis(),
                        status = EventStatus.SCHEDULED
                )
        dao.insertEvent(event)
    }

    // Mark event as completed
    suspend fun markAsCompleted(eventId: String): Result<Unit> {
        return try {
            dao.markAsCompleted(eventId, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Undo completion - move back to upcoming
    suspend fun undoComplete(eventId: String): Result<Unit> {
        return try {
            dao.undoComplete(eventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Archive event (also deletes from Google Calendar)
    suspend fun archiveEvent(account: GoogleSignInAccount, eventId: String): Result<Unit> {
        // Delete from Google Calendar
        val result = calendarRepository.deleteEvent(account, eventId)

        return result.map {
            // Mark as archived locally
            dao.markAsArchived(eventId, System.currentTimeMillis())
        }
    }

    // Restore from archive (creates new calendar event)
    suspend fun restoreFromArchive(
            account: GoogleSignInAccount,
            event: SavedEvent
    ): Result<String> {
        // Create a new event in Google Calendar
        val scheduledDateTime =
                java.time.Instant.ofEpochMilli(event.scheduledDateTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

        val result =
                calendarRepository.createEvent(
                        account = account,
                        title = event.title,
                        description = event.url,
                        imageUrl = event.imageUrl,
                        startDateTime = scheduledDateTime,
                        durationMinutes = event.durationMinutes
                )

        return result.map { newEventId ->
            // Update the event with new Google Event ID and restore status
            dao.deleteEvent(event.googleEventId)
            saveEvent(
                    googleEventId = newEventId,
                    title = event.title,
                    url = event.url,
                    scheduledDateTime = scheduledDateTime,
                    durationMinutes = event.durationMinutes,
                    imageUrl = event.imageUrl
            )
            newEventId
        }
    }

    // Delete event permanently
    suspend fun deleteEventPermanently(eventId: String): Result<Unit> {
        return try {
            dao.deleteEvent(eventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reschedule event
    suspend fun rescheduleEvent(
            account: GoogleSignInAccount,
            eventId: String,
            newDateTime: LocalDateTime,
            durationMinutes: Int
    ): Result<Unit> {
        // Update in Google Calendar
        val result = calendarRepository.updateEvent(account, eventId, newDateTime, durationMinutes)

        return result.map {
            // Update locally
            val epochMillis = newDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dao.updateEventDateTime(eventId, epochMillis)
        }
    }

    // Schedule again (from completed)
    suspend fun scheduleAgain(
            account: GoogleSignInAccount,
            originalEvent: SavedEvent,
            newDateTime: LocalDateTime,
            durationMinutes: Int
    ): Result<String> {
        // Create a new event in Google Calendar
        val result =
                calendarRepository.createEvent(
                        account = account,
                        title = originalEvent.title,
                        description = originalEvent.url,
                        imageUrl = originalEvent.imageUrl,
                        startDateTime = newDateTime,
                        durationMinutes = durationMinutes
                )

        return result.map { newEventId ->
            // Save the new event locally
            saveEvent(
                    googleEventId = newEventId,
                    title = originalEvent.title,
                    url = originalEvent.url,
                    scheduledDateTime = newDateTime,
                    durationMinutes = durationMinutes,
                    imageUrl = originalEvent.imageUrl
            )
            newEventId
        }
    }

    suspend fun syncWithCalendar(account: GoogleSignInAccount) {
        val allEvents = dao.getAllEventsOnce()

        for (event in allEvents) {
            // Skip archived or completed events
            if (event.status == EventStatus.ARCHIVED || event.status == EventStatus.COMPLETED) {
                continue
            }

            // Check if event still exists in Google Calendar
            val result = calendarRepository.getEvent(account, event.googleEventId)
            result.onSuccess { calendarEvent ->
                if (calendarEvent == null) {
                    // Event was deleted from Google Calendar
                    dao.updateEventStatus(event.googleEventId, EventStatus.DELETED_FROM_CALENDAR)
                }
            }
        }
    }

    suspend fun refreshMissingImages(limit: Int = 8) {
        val missing = dao.getEventsMissingImage(limit)
        if (missing.isEmpty()) return

        for (event in missing) {
            if (!event.url.startsWith("http")) continue
            val imageUrl = UrlMetadataFetcher.fetchImageUrl(event.url)
            if (!imageUrl.isNullOrBlank()) {
                dao.updateEventImage(event.googleEventId, imageUrl)
            }
        }
    }

    suspend fun getEventById(eventId: String): SavedEvent? {
        return dao.getEventById(eventId)
    }
}
