package com.readlater.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedEventDao {

    // Get all scheduled (not completed, not archived) events
    @Query("SELECT * FROM saved_events WHERE status = :status ORDER BY scheduledDateTime ASC")
    fun getScheduledEvents(status: EventStatus = EventStatus.SCHEDULED): Flow<List<SavedEvent>>

    // Get completed events
    @Query("SELECT * FROM saved_events WHERE status = :status ORDER BY completedAt DESC")
    fun getCompletedEvents(status: EventStatus = EventStatus.COMPLETED): Flow<List<SavedEvent>>

    // Get archived events
    @Query("SELECT * FROM saved_events WHERE status = :status ORDER BY archivedAt DESC")
    fun getArchivedEvents(status: EventStatus = EventStatus.ARCHIVED): Flow<List<SavedEvent>>

    // Get event by ID
    @Query("SELECT * FROM saved_events WHERE googleEventId = :eventId")
    suspend fun getEventById(eventId: String): SavedEvent?

    // Insert event
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SavedEvent)

    // Update event
    @Update
    suspend fun updateEvent(event: SavedEvent)

    // Update event status
    @Query("UPDATE saved_events SET status = :status WHERE googleEventId = :eventId")
    suspend fun updateEventStatus(eventId: String, status: EventStatus)

    // Update event image
    @Query("UPDATE saved_events SET imageUrl = :imageUrl WHERE googleEventId = :eventId")
    suspend fun updateEventImage(eventId: String, imageUrl: String?)

    // Mark as completed
    @Query("UPDATE saved_events SET status = :status, completedAt = :completedAt WHERE googleEventId = :eventId")
    suspend fun markAsCompleted(eventId: String, completedAt: Long, status: EventStatus = EventStatus.COMPLETED)

    // Mark as archived
    @Query("UPDATE saved_events SET status = :status, archivedAt = :archivedAt WHERE googleEventId = :eventId")
    suspend fun markAsArchived(eventId: String, archivedAt: Long, status: EventStatus = EventStatus.ARCHIVED)

    // Restore from completed (undo)
    @Query("UPDATE saved_events SET status = :status, completedAt = NULL WHERE googleEventId = :eventId")
    suspend fun undoComplete(eventId: String, status: EventStatus = EventStatus.SCHEDULED)

    // Restore from archived
    @Query("UPDATE saved_events SET status = :status, archivedAt = NULL WHERE googleEventId = :eventId")
    suspend fun restoreFromArchive(eventId: String, status: EventStatus = EventStatus.SCHEDULED)

    // Update scheduled date/time
    @Query("UPDATE saved_events SET scheduledDateTime = :newDateTime WHERE googleEventId = :eventId")
    suspend fun updateEventDateTime(eventId: String, newDateTime: Long)

    // Delete event permanently
    @Query("DELETE FROM saved_events WHERE googleEventId = :eventId")
    suspend fun deleteEvent(eventId: String)

    // Get all events (for sync)
    @Query("SELECT * FROM saved_events")
    suspend fun getAllEventsOnce(): List<SavedEvent>

    // Get events missing thumbnails
    @Query("SELECT * FROM saved_events WHERE imageUrl IS NULL OR imageUrl = '' LIMIT :limit")
    suspend fun getEventsMissingImage(limit: Int): List<SavedEvent>

    // Count scheduled events for today
    @Query("SELECT COUNT(*) FROM saved_events WHERE status = :status AND scheduledDateTime >= :startOfDay AND scheduledDateTime < :endOfDay")
    suspend fun countEventsForToday(startOfDay: Long, endOfDay: Long, status: EventStatus = EventStatus.SCHEDULED): Int

    // Count overdue events (scheduled time passed but not completed)
    @Query("SELECT COUNT(*) FROM saved_events WHERE status = :status AND scheduledDateTime < :currentTime")
    suspend fun countOverdueEvents(currentTime: Long, status: EventStatus = EventStatus.SCHEDULED): Int

    // Get next scheduled event
    @Query("SELECT * FROM saved_events WHERE status = :status AND scheduledDateTime > :currentTime ORDER BY scheduledDateTime ASC LIMIT 1")
    suspend fun getNextScheduledEvent(currentTime: Long, status: EventStatus = EventStatus.SCHEDULED): SavedEvent?
}
