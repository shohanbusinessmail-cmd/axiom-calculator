package com.axiom.calculator.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val AxiomDarkColors = darkColorScheme(
    primary = VioletPrimary,
    secondary = LavenderSecondary,
    background = GraphiteBackground,
    surface = GraphiteSurface,
    surfaceVariant = GraphiteSurfaceVariant,
    onBackground = OnGraphiteHigh,
    onSurface = OnGraphiteHigh,
    onSurfaceVariant = OnGraphiteMedium,
    error = ErrorRed
)

private val AxiomLightColors = lightColorScheme(
    primary = VioletPrimaryLight,
    secondary = VioletPrimary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = OnLightHigh,
    onSurface = OnLightHigh,
    onSurfaceVariant = OnLightMedium,
    error = ErrorRed
)

enum class AxiomThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Root theme composable. Dynamic color (Material You) is intentionally NOT
 * used by default so the brand's midnight-graphite + violet identity stays
 * consistent across devices; it can be revisited later as an opt-in setting.
 */
@Composable
fun AxiomCalculatorTheme(
    themeMode: AxiomThemeMode = AxiomThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AxiomThemeMode.SYSTEM -> isSystemInDarkTheme()
        AxiomThemeMode.LIGHT -> false
        AxiomThemeMode.DARK -> true
    }
    val colors = if (isDark) AxiomDarkColors else AxiomLightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AxiomTypography,
        shapes = AxiomShapes,
        content = content
    )
}
