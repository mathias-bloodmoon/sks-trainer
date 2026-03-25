package com.sks.trainer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SksBlue, // <-- Hier habe ich das SKS-Blau als primäre Farbe gesetzt
    onPrimary = White, // Text auf Buttons ist damit automatisch weiß
    secondary = SksYellow, // <-- Akzentfarbe (Leuchtturm-Gelb)
    onSecondary = Black,
    tertiary = Cyan40,
    background = AppBackground, 
    surface = White,
    onBackground = Black,
    onSurface = Black,
)

@Composable
fun SKSTrainerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Keep it light as requested

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
