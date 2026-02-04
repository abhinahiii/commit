package com.readlater.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.max

class NotificationScheduler(private val context: Context) {

    fun scheduleReminder(
        eventId: String,
        title: String,
        url: String,
        scheduledAtMillis: Long
    ) {
        val now = System.currentTimeMillis()
        if (scheduledAtMillis <= now) return

        val triggerAt = scheduledAtMillis - REMINDER_OFFSET_MILLIS
        val delayMillis = max(0L, triggerAt - now)
        val timeText = formatTimeText(scheduledAtMillis)

        val data = Data.Builder()
            .putString(KEY_EVENT_ID, eventId)
            .putString(KEY_TITLE, title)
            .putString(KEY_URL, url)
            .putString(KEY_TIME_TEXT, timeText)
            .putLong(KEY_SCHEDULED_AT, scheduledAtMillis)
            .build()

        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(eventId)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(eventId), ExistingWorkPolicy.REPLACE, work)
    }

    fun cancelReminder(eventId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(eventId))
    }

    private fun workName(eventId: String) = "readlater_reminder_$eventId"

    private fun formatTimeText(millis: Long): String {
        val dateTime = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        return dateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a"))
    }

    companion object {
        private const val REMINDER_OFFSET_MILLIS = 15 * 60 * 1000L
    }
}
