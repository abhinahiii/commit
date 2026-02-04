package com.readlater.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CommitColorScheme = lightColorScheme(
    primary = CommitColors.Rust,
    onPrimary = CommitColors.Cream,
    background = CommitColors.Paper,
    onBackground = CommitColors.Ink,
    surface = CommitColors.Paper,
    onSurface = CommitColors.Ink,
    surfaceVariant = CommitColors.Rust, // Using Rust for cards potentially
    onSurfaceVariant = CommitColors.Cream,
)

private val CommitTypographySystem = Typography(
    displayLarge = CommitTypography.DisplayLarge,
    bodyLarge = CommitTypography.TaskName,
    labelSmall = CommitTypography.Label
)

private val CommitShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
    extraLarge = RoundedCornerShape(2.dp)
)

@Composable
fun ReadLaterTheme(
    useDarkTheme: Boolean = false, // Force light theme for this design largely
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CommitColorScheme,
        typography = CommitTypographySystem,
        shapes = CommitShapes,
        content = content
    )
}
