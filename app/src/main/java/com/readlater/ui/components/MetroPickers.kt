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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale

data class DateOption(val label: String, val dates: List<LocalDate>)

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
        return LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    fun formatDateLabel(date: LocalDate): String {
        val today = getDeviceDate()
        val tomorrow = today.plusDays(1)
        return when (date) {
            today -> "today"
            tomorrow -> "tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d")).lowercase(Locale.ROOT)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        val deviceDate = getDeviceDate()
        val deviceTime = getDeviceTime()
        val isTimeInPast = selectedDate == deviceDate && selectedTime.isBefore(deviceTime)

        Text(
                text = "DATE",
                style = CommitTypography.Label,
                color = CommitColors.InkSoft,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .border(
                                        1.dp,
                                        CommitColors.Line
                                ) // Date is always "custom" conceptually here, but let's keep it
                                // simple line
                                .clickable { showDatePicker = true }
                                .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = formatDateLabel(selectedDate),
                    style = CommitTypography.MonoTime.copy(fontSize = 13.sp),
                    color = CommitColors.Ink
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
                text = "START TIME",
                style = CommitTypography.Label,
                color = CommitColors.InkSoft,
                modifier = Modifier.padding(bottom = 12.dp)
        )

        data class TimeOption(val label: String, val minutesFromNow: Int)
        val timeOptions =
                listOf(
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

        val selectedQuickOption =
                if (selectedDate == deviceDate) {
                    timeOptions.firstOrNull { isQuickTimeOption(it) }
                } else {
                    null
                }
        val isCustomTime = selectedQuickOption == null

        FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeOptions.forEach { option ->
                val isSelected = isQuickTimeOption(option)
                Box(
                        modifier =
                                Modifier.border(
                                                1.dp,
                                                if (isSelected) CommitColors.Rust
                                                else CommitColors.Line
                                        )
                                        .background(
                                                if (isSelected) CommitColors.Rust
                                                else Color.Transparent
                                        )
                                        .clickable {
                                            var newTime =
                                                    deviceTime.plusMinutes(
                                                            option.minutesFromNow.toLong()
                                                    )
                                            var newDate = deviceDate
                                            if (newTime.isBefore(deviceTime)) {
                                                newDate = deviceDate.plusDays(1)
                                            }
                                            onDateTimeSelected(newDate, newTime)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Text(
                            text = option.label,
                            style = CommitTypography.MonoTime.copy(fontSize = 12.sp),
                            color = if (isSelected) CommitColors.Cream else CommitColors.InkSoft
                    )
                }
            }

            val customLabel =
                    if (isCustomTime) selectedTime.format(timeFormatter).lowercase(Locale.ROOT)
                    else "custom"
            val customBorderColor =
                    if (isTimeInPast && isCustomTime) {
                        CommitColors.RedAccent
                    } else if (isCustomTime) {
                        CommitColors.Rust
                    } else {
                        CommitColors.Line
                    }
            val customBackground = if (isCustomTime) CommitColors.Rust else Color.Transparent
            val customContentColor = if (isCustomTime) CommitColors.Cream else CommitColors.InkSoft

            Box(
                    modifier =
                            Modifier.border(1.dp, customBorderColor)
                                    .background(customBackground)
                                    .clickable { showTimePicker = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                            text = customLabel,
                            style = CommitTypography.MonoTime.copy(fontSize = 12.sp),
                            color =
                                    if (isTimeInPast && isCustomTime) CommitColors.RedAccent
                                    else customContentColor
                    )
                    if (isCustomTime) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "edit time",
                                tint =
                                        if (isTimeInPast) CommitColors.RedAccent
                                        else customContentColor,
                                modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (isTimeInPast) {
            Text(
                    text = "selected time has already passed. choose a future time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CommitColors.RedAccent,
                    modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (showDatePicker) {
        val deviceDate = getDeviceDate()
        var tempSelectedDate by remember { mutableStateOf(selectedDate) }

        val dateOptions =
                remember(deviceDate) {
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

        val selectedOption = dateOptions.find { option -> option.dates.contains(tempSelectedDate) }

        Dialog(
                onDismissRequest = { showDatePicker = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(16.dp)
                                    .background(CommitColors.Paper)
                                    .border(1.dp, CommitColors.Line)
            ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                            text = "select date",
                            style =
                                    CommitTypography.CardTitle.copy(
                                            color = CommitColors.Ink,
                                            fontSize = 24.sp
                                    ),
                            color = CommitColors.Ink
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
                                    modifier =
                                            Modifier.border(1.dp, CommitColors.Line)
                                                    .background(
                                                            if (isSelected) CommitColors.Ink
                                                            else Color.Transparent
                                                    )
                                                    .clickable {
                                                        tempSelectedDate = option.dates.first()
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color =
                                                if (isSelected) CommitColors.Paper
                                                else CommitColors.Ink
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    MetroCalendarView(
                            selectedDate = tempSelectedDate,
                            highlightedDates = selectedOption?.dates ?: listOf(tempSelectedDate),
                            minDate = deviceDate,
                            onDateSelected = { date -> tempSelectedDate = date }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .background(CommitColors.Ink)
                                            .clickable {
                                                val deviceTime = getDeviceTime()
                                                val newTime =
                                                        if (tempSelectedDate == deviceDate &&
                                                                        selectedTime.isBefore(
                                                                                deviceTime
                                                                        )
                                                        ) {
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
                                color = CommitColors.Paper
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

        // Manual State for Custom Picker
        var selectedHour by remember {
            androidx.compose.runtime.mutableIntStateOf(selectedTime.hour)
        }
        var selectedMinute by remember {
            androidx.compose.runtime.mutableIntStateOf(selectedTime.minute / 5 * 5)
        }

        val hourListState =
                androidx.compose.foundation.lazy.rememberLazyListState(
                        initialFirstVisibleItemIndex = if (selectedHour > 2) selectedHour - 2 else 0
                )
        val minuteListState =
                androidx.compose.foundation.lazy.rememberLazyListState(
                        initialFirstVisibleItemIndex =
                                if (selectedMinute / 5 > 2) (selectedMinute / 5) - 2 else 0
                )

        Dialog(
                onDismissRequest = { showTimePicker = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(16.dp)
                                    .background(CommitColors.Paper)
                                    .border(1.dp, CommitColors.Line)
            ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                            text = "set time",
                            style =
                                    CommitTypography.CardTitle.copy(
                                            color = CommitColors.Ink,
                                            fontSize = 24.sp
                                    ),
                            color = CommitColors.Ink
                    )

                    if (selectedDate == deviceDate) {
                        Text(
                                text =
                                        "must be after ${deviceTime.format(timeFormatter).lowercase(Locale.ROOT)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CommitColors.InkSoft,
                                modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Custom Time Picker
                    Row(
                            modifier = Modifier.fillMaxWidth().height(240.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                    "HOUR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CommitColors.InkSoft
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.foundation.lazy.LazyColumn(
                                    state = hourListState,
                                    modifier =
                                            Modifier.width(60.dp)
                                                    .fillMaxHeight()
                                                    .border(1.dp, CommitColors.Line),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding =
                                            androidx.compose.foundation.layout.PaddingValues(
                                                    vertical = 8.dp
                                            )
                            ) {
                                items(24) { hour ->
                                    val isSelected = hour == selectedHour
                                    Box(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .background(
                                                                    if (isSelected) CommitColors.Ink
                                                                    else Color.Transparent
                                                            )
                                                            .clickable { selectedHour = hour }
                                                            .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                                text = "%02d".format(hour),
                                                style = MaterialTheme.typography.titleMedium,
                                                color =
                                                        if (isSelected) CommitColors.Paper
                                                        else CommitColors.Ink
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                                ":",
                                style = MaterialTheme.typography.displayMedium,
                                color = CommitColors.InkSoft,
                                modifier = Modifier.padding(top = 24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        // Minutes
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                    "MIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CommitColors.InkSoft
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.foundation.lazy.LazyColumn(
                                    state = minuteListState,
                                    modifier =
                                            Modifier.width(60.dp)
                                                    .fillMaxHeight()
                                                    .border(1.dp, CommitColors.Line),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding =
                                            androidx.compose.foundation.layout.PaddingValues(
                                                    vertical = 8.dp
                                            )
                            ) {
                                items(12) { i ->
                                    val minute = i * 5
                                    val isSelected = minute == selectedMinute
                                    Box(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .background(
                                                                    if (isSelected) CommitColors.Ink
                                                                    else Color.Transparent
                                                            )
                                                            .clickable { selectedMinute = minute }
                                                            .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                                text = "%02d".format(minute),
                                                style = MaterialTheme.typography.titleMedium,
                                                color =
                                                        if (isSelected) CommitColors.Paper
                                                        else CommitColors.Ink
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CommitColors.RedAccent,
                                modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                                modifier =
                                        Modifier.weight(1f)
                                                .border(1.dp, CommitColors.Line)
                                                .clickable { showTimePicker = false }
                                                .padding(16.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = "cancel",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CommitColors.Ink
                            )
                        }
                        Box(
                                modifier =
                                        Modifier.weight(1f)
                                                .background(CommitColors.Ink)
                                                .clickable {
                                                    val newTime =
                                                            LocalTime.of(
                                                                    selectedHour,
                                                                    selectedMinute
                                                            )
                                                    val freshDeviceDate = getDeviceDate()
                                                    val freshDeviceTime = getDeviceTime()

                                                    if (selectedDate == freshDeviceDate &&
                                                                    !newTime.isAfter(
                                                                            freshDeviceTime
                                                                    )
                                                    ) {
                                                        errorMessage = "select a future time."
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
                                    color = CommitColors.Paper
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
                    modifier =
                            Modifier.border(1.dp, CommitColors.Line)
                                    .clickable {
                                        val newMonth = currentMonth.value.minusMonths(1)
                                        if (!newMonth.isBefore(YearMonth.from(minDate))) {
                                            currentMonth.value = newMonth
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                        text = "<",
                        style = MaterialTheme.typography.titleMedium,
                        color = CommitColors.Ink
                )
            }

            Text(
                    text =
                            currentMonth
                                    .value
                                    .format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                                    .lowercase(Locale.ROOT),
                    style = MaterialTheme.typography.titleMedium,
                    color = CommitColors.Ink
            )

            Box(
                    modifier =
                            Modifier.border(1.dp, CommitColors.Line)
                                    .clickable {
                                        currentMonth.value = currentMonth.value.plusMonths(1)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                        text = ">",
                        style = MaterialTheme.typography.titleMedium,
                        color = CommitColors.Ink
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = CommitColors.InkSoft
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
                    Box(modifier = Modifier.weight(1f).padding(2.dp)) {
                        if (date != null) {
                            val isSelected = date == selectedDate
                            val isHighlighted = highlightedDates.contains(date)
                            val isPast = date.isBefore(minDate)

                            Box(
                                    modifier =
                                            Modifier.size(40.dp)
                                                    .then(
                                                            when {
                                                                isSelected ->
                                                                        Modifier.background(
                                                                                CommitColors.Ink
                                                                        )
                                                                isHighlighted ->
                                                                        Modifier.background(
                                                                                CommitColors.Line
                                                                        )
                                                                else -> Modifier
                                                            }
                                                    )
                                                    .then(
                                                            if (!isPast) {
                                                                Modifier.clickable {
                                                                    onDateSelected(date)
                                                                }
                                                            } else {
                                                                Modifier
                                                            }
                                                    ),
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color =
                                                when {
                                                    isSelected -> CommitColors.Paper
                                                    isPast ->
                                                            CommitColors.InkSoft.copy(alpha = 0.4f)
                                                    else -> CommitColors.Ink
                                                }
                                )
                            }
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BrutalistDateTimePicker(
        selectedDate: LocalDate,
        selectedTime: LocalTime,
        onDateTimeSelected: (LocalDate, LocalTime) -> Unit,
        modifier: Modifier = Modifier
) = MetroDateTimePicker(selectedDate, selectedTime, onDateTimeSelected, modifier)
