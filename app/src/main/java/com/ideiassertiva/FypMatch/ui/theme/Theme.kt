package com.ideiassertiva.FypMatch.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FypColors.Primary,
    onPrimary = FypColors.OnPrimary,
    primaryContainer = FypColors.PrimaryContainer,
    onPrimaryContainer = FypColors.OnPrimaryContainer,
    secondary = FypColors.Secondary,
    onSecondary = FypColors.OnSecondary,
    secondaryContainer = FypColors.SecondaryContainer,
    onSecondaryContainer = FypColors.OnSecondaryContainer,
    tertiary = FypColors.Tertiary,
    onTertiary = FypColors.OnTertiary,
    tertiaryContainer = FypColors.TertiaryContainer,
    onTertiaryContainer = FypColors.OnTertiaryContainer,
    error = FypColors.Error,
    onError = FypColors.OnError,
    errorContainer = FypColors.ErrorContainer,
    onErrorContainer = FypColors.OnErrorContainer,
    surface = FypColors.Surface,
    onSurface = FypColors.OnSurface,
    surfaceVariant = FypColors.SurfaceVariant,
    onSurfaceVariant = FypColors.OnSurfaceVariant,
    surfaceContainerLow = FypColors.SurfaceContainerLow,
    surfaceContainer = FypColors.SurfaceContainer,
    surfaceContainerHigh = FypColors.SurfaceContainerHigh,
    background = FypColors.Background,
    onBackground = FypColors.OnBackground,
    outline = FypColors.Outline,
    outlineVariant = FypColors.OutlineVariant,
    inverseSurface = FypColors.InverseSurface,
    inverseOnSurface = FypColors.InverseOnSurface,
    inversePrimary = FypColors.InversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = FypColors.DarkPrimary,
    onPrimary = FypColors.DarkOnPrimary,
    primaryContainer = FypColors.DarkPrimaryContainer,
    onPrimaryContainer = FypColors.DarkOnPrimaryContainer,
    secondary = FypColors.DarkSecondary,
    onSecondary = FypColors.DarkOnSecondary,
    secondaryContainer = FypColors.DarkSecondaryContainer,
    onSecondaryContainer = FypColors.DarkOnSecondaryContainer,
    tertiary = FypColors.DarkTertiary,
    onTertiary = FypColors.DarkOnTertiary,
    tertiaryContainer = FypColors.DarkTertiaryContainer,
    onTertiaryContainer = FypColors.DarkOnTertiaryContainer,
    error = FypColors.DarkError,
    onError = FypColors.DarkOnError,
    errorContainer = FypColors.DarkErrorContainer,
    onErrorContainer = FypColors.DarkOnErrorContainer,
    surface = FypColors.DarkSurface,
    onSurface = FypColors.DarkOnSurface,
    surfaceVariant = FypColors.DarkSurfaceVariant,
    onSurfaceVariant = FypColors.DarkOnSurfaceVariant,
    surfaceContainerLow = FypColors.DarkSurfaceContainerLow,
    surfaceContainer = FypColors.DarkSurfaceContainer,
    surfaceContainerHigh = FypColors.DarkSurfaceContainerHigh,
    background = FypColors.DarkBackground,
    onBackground = FypColors.DarkOnBackground,
    outline = FypColors.DarkOutline,
    outlineVariant = FypColors.DarkOutlineVariant,
    inverseSurface = FypColors.DarkInverseSurface,
    inverseOnSurface = FypColors.DarkInverseOnSurface,
    inversePrimary = FypColors.DarkInversePrimary,
)

@Composable
fun FypMatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FypTypography,
        content = content
    )
}