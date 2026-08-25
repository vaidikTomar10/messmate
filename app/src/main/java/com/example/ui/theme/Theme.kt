package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SleekTerracottaDark,
    onPrimary = Color(0xFF542111),
    primaryContainer = Color(0xFF713620),
    onPrimaryContainer = SleekPrimaryContainer,
    secondary = Color(0xFFDCC2B8),
    onSecondary = Color(0xFF3E2D27),
    secondaryContainer = Color(0xFF55433C),
    onSecondaryContainer = SleekSecondaryContainer,
    tertiary = Color(0xFFE2BEA8),
    background = WarmBackgroundDark,
    surface = WarmSurfaceDark,
    surfaceVariant = WarmSurfaceVariantDark,
    onBackground = Color(0xFFEDE0DB),
    onSurface = Color(0xFFEDE0DB),
    onSurfaceVariant = Color(0xFFD7C2B9),
    outline = Color(0xFFA08C84),
    outlineVariant = Color(0xFF52443D)
)

private val LightColorScheme = lightColorScheme(
    primary = SleekTerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekOnPrimaryContainer,
    secondary = SleekSecondary,
    onSecondary = Color.White,
    secondaryContainer = SleekSecondaryContainer,
    onSecondaryContainer = SleekOnSecondaryContainer,
    tertiary = SleekTerracottaPrimary,
    background = SleekBackground,
    surface = SleekSurface,
    surfaceVariant = SleekSurfaceVariant,
    onBackground = SleekTextPrimary,
    onSurface = Color(0xFF1D1B1B),
    onSurfaceVariant = SleekTextSecondary,
    outline = Color(0xFFD8C2B8),
    outlineVariant = SleekBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
