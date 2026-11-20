package com.vmnstudios95.calculator.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vmnstudios95.calculator.model.AppPreferencesState

@Composable
fun VmnGlassTheme(
    prefs: AppPreferencesState,
    content: @Composable () -> Unit
) {
    val primary = Color(prefs.glassPreset.primaryHex)
    val secondary = Color(prefs.glassPreset.secondaryHex)

    val colorScheme = if (prefs.darkMode) {
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            background = Color(0xFF061014),
            surface = Color(0xFF0C1A20),
            onPrimary = Color(0xFF04130E),
            onBackground = Color(0xFFF0FDF9),
            onSurface = Color(0xFFF0FDF9)
        )
    } else {
        lightColorScheme(
            primary = primary,
            secondary = secondary,
            background = Color(0xFFF1F7F6),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color.White,
            onBackground = Color(0xFF0B1F1A),
            onSurface = Color(0xFF0B1F1A)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun Modifier.liquidGlassCard(
    prefs: AppPreferencesState,
    cornerRadius: Dp = 20.dp,
    highlightAccent: Boolean = false
): Modifier {
    val alpha = prefs.glassIntensity.coerceIn(0.25f, 0.95f)
    val primary = Color(prefs.glassPreset.primaryHex)
    val secondary = Color(prefs.glassPreset.secondaryHex)

    val fillBrush = if (prefs.darkMode) {
        if (highlightAccent) {
            Brush.linearGradient(
                colors = listOf(
                    primary.copy(alpha = (alpha * 0.34f).coerceAtMost(0.45f)),
                    secondary.copy(alpha = (alpha * 0.18f).coerceAtMost(0.30f)),
                    Color(0xFF0D1D24).copy(alpha = alpha)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF14272F).copy(alpha = alpha),
                    Color(0xFF0B171D).copy(alpha = (alpha * 0.9f).coerceAtLeast(0.2f))
                )
            )
        }
    } else {
        if (highlightAccent) {
            Brush.linearGradient(
                colors = listOf(
                    primary.copy(alpha = 0.22f),
                    secondary.copy(alpha = 0.14f),
                    Color.White.copy(alpha = alpha)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha),
                    Color(0xFFEAF4F1).copy(alpha = (alpha * 0.85f).coerceAtLeast(0.35f))
                )
            )
        }
    }

    val borderColor = if (highlightAccent) {
        primary.copy(alpha = 0.55f)
    } else if (prefs.darkMode) {
        Color.White.copy(alpha = 0.16f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(fillBrush)
        .border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(cornerRadius)
        )
}
