package com.readlater.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar

data class DateOption(
    val label: String,
    val dates: List<LocalDate>
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MetroDateTimePicker(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    onDateTimeSelected: (LocalDate, LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }

    fun getDeviceDate(): LocalDate {
        val calendar = Calendar.getInstance()
        return LocalDate.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun getDeviceTime(): LocalTime {
        val calendar = Calendar.getInstance()
        return LocalTime.of(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
    }

    fun formatDateLabel(date: LocalDate): String {
        val today = getDeviceDate()
        val tomorrow = today.plusDays(1)
        return when (date) {
            today -> "today"
            tomorrow -> "tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d")).lowercase()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        val deviceDate = getDeviceDate()
        val deviceTime = getDeviceTime()
        val isTimeInPast = selectedDate == deviceDate && selectedTime.isBefore(deviceTime)

        Text(
            text = "date",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .clickable { showDatePicker = true }
                .padding(16.dp)
        ) {
            Text(
                text = formatDateLabel(selectedDate),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "time",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        data class TimeOption(val label: String, val minutesFromNow: Int)
        val timeOptions = listOf(
            TimeOption("in 10 min", 10),
            TimeOption("in 30 min", 30),
            TimeOption("in 1 hr", 60),
            TimeOption("in 2 hrs", 120)
        )

        fun isQuickTimeOption(option: TimeOption): Boolean {
            if (selectedDate != deviceDate) return false
            val optionTime = deviceTime.plusMinutes(option.minutesFromNow.toLong())
            return selectedTime.hour == optionTime.hour &&
                   kotlin.math.abs(selectedTime.minute - optionTime.minute) <= 1
        }

        val isCustomTime = selectedDate != deviceDate || timeOptions.none { isQuickTimeOption(it) }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeOptions.forEach { option ->
                val isSelected = isQuickTimeOption(option)
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable {
                            var newTime = deviceTime.plusMinutes(option.minutesFromNow.toLong())
                            var newDate = deviceDate
                            if (newTime.isBefore(deviceTime)) {
                                newDate = deviceDate.plusDays(1)
                            }
                            onDateTimeSelected(newDate, newTime)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .border(1.dp, if (isTimeInPast && isCustomTime) Color(0xFFE53935) else MaterialTheme.colorScheme.outline)
                    .background(if (isCustomTime) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { showTimePicker = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (isCustomTime) selectedTime.format(timeFormatter).lowercase() else "other",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isTimeInPast && isCustomTime -> Color(0xFFE53935)
                        isCustomTime -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        if (isTimeInPast) {
            Text(
                text = "this time has passed. please select a future time.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE53935),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (showDatePicker) {
        val deviceDate = getDeviceDate()
        var tempSelectedDate by remember { mutableStateOf(selectedDate) }

        val dateOptions = remember(deviceDate) {
            val today = deviceDate
            val tomorrow = today.plusDays(1)
            val thisSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            val thisSunday = thisSaturday.plusDays(1)
            val nextSaturday = thisSaturday.plusWeeks(1)
            val nextSunday = nextSaturday.plusDays(1)

            buildList {
                add(DateOption("today", listOf(today)))
                add(DateOption("tomorrow", listOf(tomorrow)))
                if (thisSaturday.isAfter(tomorrow)) {
                    add(DateOption("this weekend", listOf(thisSaturday, thisSunday)))
                }
                add(DateOption("next weekend", listOf(nextSaturday, nextSunday)))
            }
        }

        val selectedOption = dateOptions.find { option ->
            option.dates.contains(tempSelectedDate)
        }

        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "select date",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dateOptions.forEach { option ->
                            val isSelected = selectedOption == option
                            Box(
                                modifier = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.outline)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        tempSelectedDate = option.dates.first()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    MetroCalendarView(
                        selectedDate = tempSelectedDate,
                        highlightedDates = selectedOption?.dates ?: listOf(tempSelectedDate),
                        minDate = deviceDate,
                        onDateSelected = { date ->
                            tempSelectedDate = date
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                val deviceTime = getDeviceTime()
                                val newTime = if (tempSelectedDate == deviceDate && selectedTime.isBefore(deviceTime)) {
                                    deviceTime.plusHours(1).withMinute(0)
                                } else {
                                    selectedTime
                                }
                                onDateTimeSelected(tempSelectedDate, newTime)
                                showDatePicker = false
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "done",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val deviceDate = getDeviceDate()
        val deviceTime = getDeviceTime()
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )

        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "select time",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (selectedDate == deviceDate) {
                        Text(
                            text = "must be after ${deviceTime.format(timeFormatter).lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                            clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            selectorColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                            periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE53935),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .clickable { showTimePicker = false }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "cancel",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    val freshDeviceDate = getDeviceDate()
                                    val freshDeviceTime = getDeviceTime()

                                    if (selectedDate == freshDeviceDate && !newTime.isAfter(freshDeviceTime)) {
                                        errorMessage = "select a future time"
                                    } else {
                                        onDateTimeSelected(selectedDate, newTime)
                                        showTimePicker = false
                                    }
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "done",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetroCalendarView(
    selectedDate: LocalDate,
    highlightedDates: List<LocalDate>,
    minDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val daysOfWeek = listOf("s", "m", "t", "w", "t", "f", "s")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .clickable {
                        val newMonth = currentMonth.value.minusMonths(1)
                        if (!newMonth.isBefore(YearMonth.from(minDate))) {
                            currentMonth.value = newMonth
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "<", style = MaterialTheme.typography.titleMedium)
            }

            Text(
                text = currentMonth.value.format(DateTimeFormatter.ofPattern("MMMM yyyy")).lowercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .clickable {
                        currentMonth.value = currentMonth.value.plusMonths(1)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = ">", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val firstDayOfMonth = currentMonth.value.atDay(1)
        val lastDayOfMonth = currentMonth.value.atEndOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

        val days = buildList {
            repeat(startDayOfWeek) { add(null) }
            var day = firstDayOfMonth
            while (!day.isAfter(lastDayOfMonth)) {
                add(day)
                day = day.plusDays(1)
            }
        }

        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                    ) {
                        if (date != null) {
                            val isSelected = date == selectedDate
                            val isHighlighted = highlightedDates.contains(date)
                            val isPast = date.isBefore(minDate)

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .then(
                                        when {
                                            isSelected -> Modifier.background(MaterialTheme.colorScheme.primary)
                                            isHighlighted -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                            else -> Modifier
                                        }
                                    )
                                    .then(
                                        if (!isPast) {
                                            Modifier.clickable { onDateSelected(date) }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isPast -> MaterialTheme.colorScheme.outlineVariant
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// Alias for backward compatibility
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BrutalistDateTimePicker(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    onDateTimeSelected: (LocalDate, LocalTime) -> Unit,
    modifier: Modifier = Modifier
) = MetroDateTimePicker(selectedDate, selectedTime, onDateTimeSelected, modifier)
