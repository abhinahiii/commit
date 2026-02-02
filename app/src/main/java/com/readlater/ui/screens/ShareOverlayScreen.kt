package com.readlater.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.readlater.ui.components.MetroButton
import com.readlater.ui.components.MetroDateTimePicker
import com.readlater.ui.components.MetroTextField
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShareOverlayContent(
    title: String,
    onTitleChange: (String) -> Unit,
    url: String,
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    onDateTimeSelected: (LocalDate, LocalTime) -> Unit,
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    isLoading: Boolean,
    isFetchingTitle: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durations = listOf(
        15 to "15m",
        30 to "30m",
        45 to "45m",
        60 to "1h",
        90 to "1.5h",
        120 to "2h"
    )

    fun isTimeInPast(): Boolean {
        val calendar = Calendar.getInstance()
        val deviceDate = LocalDate.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        val deviceTime = LocalTime.of(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
        return selectedDate == deviceDate && selectedTime.isBefore(deviceTime)
    }

    val timeInPast = isTimeInPast()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "schedule",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            MetroTextField(
                value = if (isFetchingTitle) "loading..." else title,
                onValueChange = onTitleChange,
                label = if (isFetchingTitle) "title (fetching...)" else "title"
            )

            Spacer(modifier = Modifier.height(20.dp))

            MetroDateTimePicker(
                selectedDate = selectedDate,
                selectedTime = selectedTime,
                onDateTimeSelected = onDateTimeSelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "duration",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durations.forEach { (minutes, label) ->
                        val isSelected = selectedDuration == minutes
                        Box(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onDurationSelected(minutes) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MetroButton(
                        text = "cancel",
                        onClick = onCancel,
                        filled = false,
                        enabled = !isLoading
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MetroButton(
                        text = if (isLoading) "saving..." else "save",
                        onClick = onSave,
                        enabled = !isLoading && title.isNotBlank() && !timeInPast
                    )
                }
            }
        }
    }
}

@Composable
fun NotConnectedOverlay(
    onOpenApp: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "not connected",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "connect your google calendar first to schedule reading time.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MetroButton(
                        text = "cancel",
                        onClick = onCancel,
                        filled = false
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MetroButton(
                        text = "open app",
                        onClick = onOpenApp
                    )
                }
            }
        }
    }
}
