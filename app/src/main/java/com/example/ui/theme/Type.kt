package com.example.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Brutalist Typography specifications using built-in system fonts for reliability and zero file weights
val BrutalistDisplayFont = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))
val BrutalistMonoFont = FontFamily(Typeface.MONOSPACE)
val BrutalistBodyFont = FontFamily.Default

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = BrutalistDisplayFont,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = BrutalistDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BrutalistDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BrutalistBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BrutalistBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BrutalistMonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BrutalistMonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
