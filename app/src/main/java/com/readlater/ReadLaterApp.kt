package com.readlater

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.readlater.data.AppContainer
import com.readlater.data.DefaultAppContainer
import com.readlater.notifications.REMINDER_CHANNEL_DESC
import com.readlater.notifications.REMINDER_CHANNEL_ID
import com.readlater.notifications.REMINDER_CHANNEL_NAME

class ReadLaterApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        createReminderChannel()
    }

    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                                    REMINDER_CHANNEL_ID,
                                    REMINDER_CHANNEL_NAME,
                                    NotificationManager.IMPORTANCE_DEFAULT
                            )
                            .apply { description = REMINDER_CHANNEL_DESC }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
