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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.readlater.ui.theme.CommitBorders
import com.readlater.ui.theme.CommitColors
import com.readlater.ui.theme.CommitTypography
import com.readlater.ui.theme.EditorialSpacing
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RescheduleDialog(
        title: String,
        initialDate: LocalDate,
        initialTime: LocalTime,
        initialDuration: Int,
        onDismiss: () -> Unit,
        onConfirm: (LocalDateTime, Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf(initialTime) }
    var selectedDuration by remember { mutableIntStateOf(initialDuration) }

    val durationOptions =
            listOf(
                    15 to "15 min",
                    30 to "30 min",
                    45 to "45 min",
                    60 to "1 hr",
                    90 to "1.5 hr",
                    120 to "2 hr"
            )

    fun formatDuration(minutes: Int): String {
        if (minutes < 60) return "$minutes min"
        val hours = minutes / 60
        val mins = minutes % 60
        return if (mins == 0) {
            if (hours == 1) "1 hr" else "$hours hr"
        } else {
            "$hours hr $mins min"
        }
    }

    fun isTimeInPast(): Boolean {
        val calendar = Calendar.getInstance()
        val deviceDate =
                LocalDate.of(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)
                )
        val deviceTime =
                LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        return selectedDate == deviceDate && selectedTime.isBefore(deviceTime)
    }

    val timeInPast = isTimeInPast()
    val isCustomDuration = durationOptions.none { it.first == selectedDuration }
    var showDurationPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(CommitColors.Paper)
                                .border(CommitBorders.Hairline, CommitColors.Line)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(EditorialSpacing.m)) {
                Text(
                        text = title.uppercase(),
                        style =
                                CommitTypography.CardTitle.copy(
                                        color = CommitColors.Ink,
                                        fontSize = 20.sp,
                                        letterSpacing = 0.05.em
                                ),
                        color = CommitColors.Ink
                )

                Spacer(modifier = Modifier.height(EditorialSpacing.m))

                MetroDateTimePicker(
                        selectedDate = selectedDate,
                        selectedTime = selectedTime,
                        onDateTimeSelected = { date, time ->
                            selectedDate = date
                            selectedTime = time
                        }
                )

                Spacer(modifier = Modifier.height(EditorialSpacing.m))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                            text = "DURATION",
                            style = CommitTypography.Label,
                            color = CommitColors.InkSoft,
                            modifier = Modifier.padding(bottom = EditorialSpacing.s)
                    )
                    FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durationOptions.forEach { (minutes, label) ->
                            val isSelected = selectedDuration == minutes
                            Box(
                                    modifier =
                                            Modifier.border(
                                                            CommitBorders.Hairline,
                                                            if (isSelected) CommitColors.Ink
                                                            else CommitColors.Line
                                                    )
                                                    .background(
                                                            if (isSelected) CommitColors.Ink
                                                            else Color.Transparent
                                                    )
                                                    .clickable { selectedDuration = minutes }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                        text = label,
                                        style = CommitTypography.Label.copy(fontSize = 12.sp),
                                        color =
                                                if (isSelected) CommitColors.Paper
                                                else CommitColors.Ink
                                )
                            }
                        }

                        val customLabel =
                                if (isCustomDuration) formatDuration(selectedDuration) else "custom"
                        Box(
                                modifier =
                                        Modifier.border(
                                                        CommitBorders.Hairline,
                                                        if (isCustomDuration) CommitColors.Ink
                                                        else CommitColors.Line
                                                )
                                                .background(
                                                        if (isCustomDuration) CommitColors.Ink
                                                        else Color.Transparent
                                                )
                                                .clickable { showDurationPicker = true }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                        text = customLabel.uppercase(),
                                        style = CommitTypography.Label.copy(fontSize = 12.sp),
                                        color =
                                                if (isCustomDuration) CommitColors.Paper
                                                else CommitColors.Ink
                                )
                                if (isCustomDuration) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "edit duration",
                                            tint =
                                                    if (isCustomDuration) CommitColors.Paper
                                                    else CommitColors.Ink,
                                            modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(EditorialSpacing.m))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetroButton(text = "cancel", onClick = onDismiss, filled = false)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetroButton(
                                text = "save",
                                onClick = {
                                    onConfirm(
                                            LocalDateTime.of(selectedDate, selectedTime),
                                            selectedDuration
                                    )
                                },
                                enabled = !timeInPast
                        )
                    }
                }
            }
        }
    }

    if (showDurationPicker) {
        var tempDuration by remember { mutableIntStateOf(selectedDuration) }
        Dialog(onDismissRequest = { showDurationPicker = false }) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(CommitColors.Paper)
                                    .border(CommitBorders.Hairline, CommitColors.Line)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(
                            text = "CUSTOM DURATION",
                            style =
                                    CommitTypography.CardTitle.copy(
                                            color = CommitColors.Ink,
                                            fontSize = 20.sp
                                    ),
                            color = CommitColors.Ink
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                                modifier =
                                        Modifier.border(CommitBorders.Hairline, CommitColors.Line)
                                                .clickable {
                                                    tempDuration =
                                                            (tempDuration - 15).coerceAtLeast(15)
                                                }
                                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                    text = "-",
                                    style = CommitTypography.DisplayLarge,
                                    color = CommitColors.Ink
                            )
                        }

                        Box(
                                modifier =
                                        Modifier.border(CommitBorders.Hairline, CommitColors.Line)
                                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                    text = formatDuration(tempDuration),
                                    style = CommitTypography.TaskName,
                                    color = CommitColors.Ink
                            )
                        }

                        Box(
                                modifier =
                                        Modifier.border(CommitBorders.Hairline, CommitColors.Line)
                                                .clickable {
                                                    tempDuration =
                                                            (tempDuration + 15).coerceAtMost(240)
                                                }
                                                .padding(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                    text = "+",
                                    style = CommitTypography.DisplayLarge,
                                    color = CommitColors.Ink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                            text = "Adjust in 15-minute steps.",
                            style = CommitTypography.Label,
                            color = CommitColors.InkSoft
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MetroButton(
                                    text = "cancel",
                                    onClick = { showDurationPicker = false },
                                    filled = false
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MetroButton(
                                    text = "done",
                                    onClick = {
                                        selectedDuration = tempDuration
                                        showDurationPicker = false
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
