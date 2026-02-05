package com.readlater.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em

object CommitColors {
    val Paper = Color(0xFFDCD9D5)
    val Rust = Color(0xFF964F30)
    val Ink = Color(0xFF2B2624)
    val InkSoft = Color(0xFF5C5854)
    val Cream = Color(0xFFF2F0EB)
    val Line = Color(0xFFBDB9B5)
    val RedAccent = Color(0xFF8B3A3A) // From React 4
    val DarkCard = Color(0xFF1A1A1A) // From React 4
}

object CommitTypography {
    val Serif = FontFamily.Serif
    val Mono = FontFamily.Monospace
    val Sans = FontFamily.SansSerif

    val DisplayLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = CommitColors.Ink
    )

    val CardTitle = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.01).em,
        color = CommitColors.Cream
    )

    val CardSubtitle = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = CommitColors.Cream.copy(alpha = 0.9f)
    )

    val Label = TextStyle(
        fontFamily = Sans,
        fontSize = 10.sp,
        letterSpacing = 0.15.em,
        color = CommitColors.InkSoft
    )

    val MonoTime = TextStyle(
        fontFamily = Mono,
        fontSize = 13.sp,
        letterSpacing = (-0.02).em,
        color = CommitColors.InkSoft
    )
    
    val Brand = TextStyle(
        fontFamily = Serif,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        fontSize = 16.sp,
        color = CommitColors.Ink
    )

    val Date = TextStyle(
        fontFamily = Sans,
        fontSize = 11.sp,
        letterSpacing = 0.1.em,
        color = CommitColors.Ink
    )
    
    val TaskName = TextStyle(
        fontFamily = Serif,
        fontSize = 16.sp,
        color = CommitColors.Ink
    )
}
