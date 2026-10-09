package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GaunGreenPrimaryDark,
    onPrimary = GaunGreenOnPrimaryDark,
    primaryContainer = GaunGreenContainerDark,
    onPrimaryContainer = GaunGreenOnContainerDark,
    secondary = GaunCrimsonSecondaryDark,
    onSecondary = GaunCrimsonOnSecondaryDark,
    secondaryContainer = GaunCrimsonContainerDark,
    onSecondaryContainer = GaunCrimsonOnContainerDark,
    tertiary = GaunAmberTertiary,
    background = GaunBackgroundDark,
    surface = GaunSurfaceDark,
    surfaceVariant = GaunSurfaceVariantDark,
    onSurface = GaunOnSurfaceDark,
    outline = GaunOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = GaunGreenPrimary,
    onPrimary = GaunGreenOnPrimary,
    primaryContainer = GaunGreenContainer,
    onPrimaryContainer = GaunGreenOnContainer,
    secondary = GaunCrimsonSecondary,
    onSecondary = GaunCrimsonOnSecondary,
    secondaryContainer = GaunCrimsonContainer,
    onSecondaryContainer = GaunCrimsonOnContainer,
    tertiary = GaunAmberTertiary,
    background = GaunBackgroundLight,
    surface = GaunSurfaceLight,
    surfaceVariant = GaunSurfaceVariant,
    onSurface = GaunOnSurfaceLight,
    outline = GaunOutlineLight
)

@Composable
fun SmartGaunTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
