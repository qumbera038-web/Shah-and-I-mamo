package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val StudioDarkColorScheme = darkColorScheme(
    primary = StudioCyan,
    onPrimary = StudioNight,
    primaryContainer = StudioCyanDim,
    onPrimaryContainer = StudioTextPrimary,
    secondary = StudioAmber,
    onSecondary = StudioNight,
    secondaryContainer = StudioAmberDim,
    onSecondaryContainer = StudioAmber,
    tertiary = StudioMint,
    onTertiary = StudioNight,
    background = StudioNight,
    onBackground = StudioTextPrimary,
    surface = StudioSurface,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioCard,
    onSurfaceVariant = StudioTextSecondary,
    surfaceContainerHigh = StudioCardElevated,
    outline = StudioBorder,
    outlineVariant = StudioBorder.copy(alpha = 0.5f),
    error = StudioCoral,
    onError = StudioNight
)

private val StudioShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun ALiRaZaStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioDarkColorScheme,
        typography = StudioTypography,
        shapes = StudioShapes,
        content = content
    )
}
