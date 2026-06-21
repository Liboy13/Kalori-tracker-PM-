package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BrutalistColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = SolidWhite,
    background = BrutalistSurface,
    onBackground = PureBlack,
    surface = SolidWhite,
    onSurface = PureBlack,
    secondary = DarkGreyAccent,
    onSecondary = SolidWhite,
    surfaceVariant = LightGreyAccent,
    onSurfaceVariant = PureBlack,
    error = AccentMutedRed,
    onError = SolidWhite
)

@Composable
fun FUEL_TRACK_Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrutalistColorScheme,
        typography = Typography,
        content = content
    )
}
