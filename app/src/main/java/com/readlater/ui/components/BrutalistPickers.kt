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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale

data class DateOption(
    val label: String,
    val dates: List<LocalDate> // Can be single date or range (weekend = Sat + Sun)
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BrutalistDateTimePicker(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    onDateTimeSelected: (LocalDate, LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }

    // Get current device date/time
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
            today -> "Today"
            tomorrow -> "Tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Check if time is in the past for today
        val deviceDate = getDeviceDate()
        val deviceTime = getDeviceTime()
        val isTimeInPast = selectedDate == deviceDate && selectedTime.isBefore(deviceTime)

        // DATE SECTION
        Text(
            text = "DATE",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Black)
                .clickable { showDatePicker = true }
                .padding(16.dp)
        ) {
            Text(
                text = formatDateLabel(selectedDate),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TIME SECTION
        Text(
            text = "TIME",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Time quick selection chips
        data class TimeOption(val label: String, val minutesFromNow: Int)
        val timeOptions = listOf(
            TimeOption("In 10 min", 10),
            TimeOption("In 30 min", 30),
            TimeOption("In 1 hr", 60),
            TimeOption("In 2 hrs", 120)
        )

        // Check if selected time matches any quick option (only valid for today)
        fun isQuickTimeOption(option: TimeOption): Boolean {
            if (selectedDate != deviceDate) return false
            val optionTime = deviceTime.plusMinutes(option.minutesFromNow.toLong())
            // Allow 1 minute tolerance for matching
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
                        .border(2.dp, Color.Black)
                        .background(if (isSelected) Color.Black else Color.White)
                        .clickable {
                            // Calculate new time and potentially new date
                            var newTime = deviceTime.plusMinutes(option.minutesFromNow.toLong())
                            var newDate = deviceDate

                            // Handle day overflow (e.g., 11:30 PM + 1 hr = 12:30 AM next day)
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
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }

            // Custom time option
            Box(
                modifier = Modifier
                    .border(2.dp, if (isTimeInPast && isCustomTime) Color.Red else Color.Black)
                    .background(if (isCustomTime) Color.Black else Color.White)
                    .clickable { showTimePicker = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (isCustomTime) selectedTime.format(timeFormatter) else "Other",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isTimeInPast && isCustomTime -> Color.Red
                        isCustomTime -> Color.White
                        else -> Color.Black
                    }
                )
            }
        }

        // Warning message when time is in the past
        if (isTimeInPast) {
            Text(
                text = "This time has passed. Please select a future time.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    // DATE PICKER DIALOG
    if (showDatePicker) {
        val deviceDate = getDeviceDate()
        var tempSelectedDate by remember { mutableStateOf(selectedDate) }

        // Calculate date options
        val dateOptions = remember(deviceDate) {
            val today = deviceDate
            val tomorrow = today.plusDays(1)
            val thisSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            val thisSunday = thisSaturday.plusDays(1)
            val nextSaturday = thisSaturday.plusWeeks(1)
            val nextSunday = nextSaturday.plusDays(1)

            buildList {
                add(DateOption("Today", listOf(today)))
                add(DateOption("Tomorrow", listOf(tomorrow)))
                if (thisSaturday.isAfter(tomorrow)) {
                    add(DateOption("This weekend", listOf(thisSaturday, thisSunday)))
                }
                add(DateOption("Next weekend", listOf(nextSaturday, nextSunday)))
            }
        }

        // Find which option is currently selected
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
                    .background(Color.White)
                    .border(2.dp, Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "SELECT DATE",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick selection chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dateOptions.forEach { option ->
                            val isSelected = selectedOption == option
                            Box(
                                modifier = Modifier
                                    .border(2.dp, Color.Black)
                                    .background(if (isSelected) Color.Black else Color.White)
                                    .clickable {
                                        // Select first date of the option (or Saturday for weekends)
                                        tempSelectedDate = option.dates.first()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = option.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) Color.White else Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Calendar
                    CalendarView(
                        selectedDate = tempSelectedDate,
                        highlightedDates = selectedOption?.dates ?: listOf(tempSelectedDate),
                        minDate = deviceDate,
                        onDateSelected = { date ->
                            tempSelectedDate = date
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Done button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .clickable {
                                val deviceTime = getDeviceTime()
                                // If selecting today and time is in past, adjust
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
                            text = "DONE",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // TIME PICKER DIALOG
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
                    .background(Color.White)
                    .border(2.dp, Color.Black)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SELECT TIME",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )

                    if (selectedDate == deviceDate) {
                        Text(
                            text = "Must be after ${deviceTime.format(timeFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color.LightGray.copy(alpha = 0.3f),
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color.Black,
                            selectorColor = Color.Black,
                            containerColor = Color.White,
                            periodSelectorBorderColor = Color.Black,
                            periodSelectorSelectedContainerColor = Color.Black,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContainerColor = Color.White,
                            periodSelectorUnselectedContentColor = Color.Black,
                            timeSelectorSelectedContainerColor = Color.Black,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContainerColor = Color.White,
                            timeSelectorUnselectedContentColor = Color.Black
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red,
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
                                .border(2.dp, Color.Black)
                                .clickable { showTimePicker = false }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CANCEL",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black)
                                .clickable {
                                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    val freshDeviceDate = getDeviceDate()
                                    val freshDeviceTime = getDeviceTime()

                                    if (selectedDate == freshDeviceDate && !newTime.isAfter(freshDeviceTime)) {
                                        errorMessage = "Select a future time"
                                    } else {
                                        onDateTimeSelected(selectedDate, newTime)
                                        showTimePicker = false
                                    }
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DONE",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarView(
    selectedDate: LocalDate,
    highlightedDates: List<LocalDate>,
    minDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .border(2.dp, Color.Black)
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
                text = currentMonth.value.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .border(2.dp, Color.Black)
                    .clickable {
                        currentMonth.value = currentMonth.value.plusMonths(1)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = ">", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Days of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        val firstDayOfMonth = currentMonth.value.atDay(1)
        val lastDayOfMonth = currentMonth.value.atEndOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0

        val days = buildList {
            // Empty cells before first day
            repeat(startDayOfWeek) { add(null) }
            // Days of month
            var day = firstDayOfMonth
            while (!day.isAfter(lastDayOfMonth)) {
                add(day)
                day = day.plusDays(1)
            }
        }

        // Display in rows of 7
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
                                        if (isSelected) {
                                            Modifier
                                                .background(Color.Black)
                                                .border(2.dp, Color.Black)
                                        } else if (isHighlighted) {
                                            Modifier
                                                .background(Color.LightGray)
                                                .border(2.dp, Color.Black)
                                        } else {
                                            Modifier
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
                                        isSelected -> Color.White
                                        isPast -> Color.LightGray
                                        else -> Color.Black
                                    }
                                )
                            }
                        }
                    }
                }
                // Fill remaining cells if week is incomplete
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
