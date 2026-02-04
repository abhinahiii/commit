package com.readlater.ui.screens

import com.readlater.data.SavedEvent
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun calculateStreak(completedEvents: List<SavedEvent>): Int {
    if (completedEvents.isEmpty()) return 0

    val zoneId = ZoneId.systemDefault()
    val sortedDates = completedEvents
        .mapNotNull { it.completedAt }
        .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate() }
        .distinct()
        .sortedDescending()

    if (sortedDates.isEmpty()) return 0

    val today = java.time.LocalDate.now(zoneId)
    val yesterday = today.minusDays(1)
    
    // If the most recent completion wasn't today or yesterday, streak is broken (0) 
    // BUT maybe they just haven't finished today's yet. So if last was yesterday, streak is alive.
    // If last was today, streak is alive.
    // If last was before yesterday, streak is 0.
    
    val lastDate = sortedDates.first()
    if (!lastDate.isEqual(today) && !lastDate.isEqual(yesterday)) {
        return 0
    }

    var streak = 1
    var currentDate = lastDate
    
    for (i in 1 until sortedDates.size) {
        val nextDate = sortedDates[i]
        if (ChronoUnit.DAYS.between(nextDate, currentDate) == 1L) {
            streak++
            currentDate = nextDate
        } else {
            break
        }
    }
    
    return streak
}
