package com.example.final_project_pam.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val UnscrollColorScheme = lightColorScheme(
    primary = UnscrollPrimary,
    onPrimary = Color.White,
    primaryContainer = UnscrollPrimary.copy(alpha = 0.15f),
    onPrimaryContainer = UnscrollPrimary,
    secondary = UnscrollSecondary,
    onSecondary = Color.White,
    secondaryContainer = UnscrollSecondary.copy(alpha = 0.12f),
    onSecondaryContainer = UnscrollSecondary,
    tertiary = UnscrollTertiary,
    onTertiary = UnscrollBlack,
    background = UnscrollBackground,
    onBackground = UnscrollBlack,
    surface = UnscrollBackground,
    onSurface = UnscrollBlack,
    surfaceVariant = UnscrollTertiary,
    onSurfaceVariant = UnscrollBlack.copy(alpha = 0.7f),
    outline = UnscrollBlack.copy(alpha = 0.3f),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

@Composable
fun FinalprojectpamTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = UnscrollColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = UnscrollColorScheme,
        typography = Typography,
        content = content
    )
}