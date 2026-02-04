package com.readlater.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventStatus {
    SCHEDULED,              // Active, in upcoming (not completed)
    COMPLETED,              // User marked as done
    ARCHIVED,               // User archived (soft delete)
    DELETED_FROM_CALENDAR   // Deleted externally from Google Calendar
}

@Entity(tableName = "saved_events")
data class SavedEvent(
    @PrimaryKey val googleEventId: String,
    val title: String,
    val url: String,
    val imageUrl: String? = null,
    val scheduledDateTime: Long,  // epoch millis
    val durationMinutes: Int,
    val createdAt: Long,
    val status: EventStatus = EventStatus.SCHEDULED,
    val completedAt: Long? = null,   // when marked complete
    val archivedAt: Long? = null     // when archived
)
