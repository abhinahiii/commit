package com.readlater.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class CalendarRepository(private val context: Context) {

    private fun getCalendarService(account: GoogleSignInAccount): Calendar {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR_EVENTS)
        ).apply {
            selectedAccount = account.account
        }

        return Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("ReadLater")
            .build()
    }

    suspend fun createEvent(
        account: GoogleSignInAccount,
        title: String,
        description: String,
        startDateTime: LocalDateTime,
        durationMinutes: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = getCalendarService(account)

            val startInstant = startDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
            val endInstant = startDateTime
                .plusMinutes(durationMinutes.toLong())
                .atZone(ZoneId.systemDefault())
                .toInstant()

            val event = Event().apply {
                summary = title
                this.description = description
                start = EventDateTime().apply {
                    dateTime = DateTime(Date.from(startInstant))
                    timeZone = ZoneId.systemDefault().id
                }
                end = EventDateTime().apply {
                    dateTime = DateTime(Date.from(endInstant))
                    timeZone = ZoneId.systemDefault().id
                }
            }

            val createdEvent = service.events()
                .insert("primary", event)
                .execute()

            Result.success(createdEvent.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
