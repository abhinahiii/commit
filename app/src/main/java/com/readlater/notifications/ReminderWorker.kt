package com.readlater.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.readlater.MainActivity
import com.readlater.R

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: "read later"
        val url = inputData.getString(KEY_URL).orEmpty()
        val timeText = inputData.getString(KEY_TIME_TEXT).orEmpty()

        val intent = if (url.isNotBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        } else {
            Intent(applicationContext, MainActivity::class.java)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (timeText.isNotBlank()) {
            timeText
        } else {
            "scheduled to read"
        }

        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    if (url.isNotBlank()) "$contentText\n$url" else contentText
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(eventId.hashCode(), notification)

        return Result.success()
    }
}
