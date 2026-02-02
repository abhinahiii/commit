package com.readlater

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.readlater.data.AuthRepository
import com.readlater.data.AuthState
import com.readlater.data.CalendarRepository
import com.readlater.ui.screens.NotConnectedOverlay
import com.readlater.ui.screens.ShareOverlayContent
import com.readlater.ui.theme.ReadLaterTheme
import com.readlater.util.UrlMetadataFetcher
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

class ShareActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var calendarRepository: CalendarRepository

    private fun getDeviceDate(): LocalDate {
        val calendar = Calendar.getInstance()
        return LocalDate.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun getDeviceTime(): LocalTime {
        val calendar = Calendar.getInstance()
        return LocalTime.of(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
    }

    private fun getDefaultTime(): LocalTime {
        // Default to next hour, rounded to :00
        val now = getDeviceTime()
        return now.plusHours(1).withMinute(0).withSecond(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = AuthRepository(applicationContext)
        calendarRepository = CalendarRepository(applicationContext)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        val sharedUrl = UrlMetadataFetcher.extractUrl(sharedText) ?: sharedText

        // Get initial date/time using device calendar
        val initialDate = getDeviceDate()
        val initialTime = getDefaultTime()

        setContent {
            ReadLaterTheme {
                val scope = rememberCoroutineScope()

                var title by remember { mutableStateOf("") }
                var selectedDate by remember { mutableStateOf(initialDate) }
                var selectedTime by remember { mutableStateOf(initialTime) }
                var selectedDuration by remember { mutableIntStateOf(30) }
                var isLoading by remember { mutableStateOf(false) }
                var isFetchingTitle by remember { mutableStateOf(false) }

                // Fetch title on launch
                androidx.compose.runtime.LaunchedEffect(sharedUrl) {
                    if (sharedUrl.startsWith("http")) {
                        isFetchingTitle = true
                        val fetchedTitle = UrlMetadataFetcher.fetchTitle(sharedUrl)
                        if (fetchedTitle != null && title.isBlank()) {
                            title = fetchedTitle
                        }
                        isFetchingTitle = false
                    }
                }

                val authState = authRepository.authState.collectAsState().value
                val isAuthenticated = authState is AuthState.Authenticated

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isLoading) finish()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Consume clicks */ }
                    ) {
                        if (isAuthenticated) {
                            ShareOverlayContent(
                                title = title,
                                onTitleChange = { title = it },
                                url = sharedUrl,
                                selectedDate = selectedDate,
                                selectedTime = selectedTime,
                                onDateTimeSelected = { date, time ->
                                    selectedDate = date
                                    selectedTime = time
                                },
                                selectedDuration = selectedDuration,
                                onDurationSelected = { selectedDuration = it },
                                isLoading = isLoading,
                                isFetchingTitle = isFetchingTitle,
                                onCancel = { finish() },
                                onSave = {
                                    scope.launch {
                                        isLoading = true
                                        val account = authRepository.getAccount()
                                        if (account != null) {
                                            val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                                            val result = calendarRepository.createEvent(
                                                account = account,
                                                title = title,
                                                description = sharedUrl,
                                                startDateTime = dateTime,
                                                durationMinutes = selectedDuration
                                            )
                                            result.onSuccess {
                                                Toast.makeText(
                                                    this@ShareActivity,
                                                    "Event created",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish()
                                            }.onFailure { error ->
                                                Toast.makeText(
                                                    this@ShareActivity,
                                                    "Failed: ${error.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            )
                        } else {
                            NotConnectedOverlay(
                                onOpenApp = {
                                    startActivity(
                                        Intent(this@ShareActivity, MainActivity::class.java)
                                    )
                                    finish()
                                },
                                onCancel = { finish() }
                            )
                        }
                    }
                }
            }
        }
    }
}
