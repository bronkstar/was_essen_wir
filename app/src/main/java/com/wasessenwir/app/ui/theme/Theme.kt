package com.wasessenwir.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun WasEssenWirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = CyanPrimary,
            secondary = CyanPrimary,
            tertiary = CyanPrimary,
            onPrimary = Color.Black,
            surfaceVariant = Color(0xFF1F2A2A)
        )
    } else {
        lightColorScheme(
            primary = CyanPrimary,
            secondary = CyanPrimary,
            tertiary = CyanPrimary,
            onPrimary = Color.White,
            surface = Color.White,
            surfaceVariant = LightCyanSurface
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
