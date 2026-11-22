package com.vmnstudios95.maytinhdanang.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vmnstudios95.maytinhdanang.model.AppFontStyle
import com.vmnstudios95.maytinhdanang.model.AppThemeMode

data class LiquidGlassPalette(
    val bgGrad1: Color,
    val bgGrad2: Color,
    val bgGrad3: Color,
    val bgBase: Color,
    val glassBg: Color,
    val glassStrong: Color,
    val glassBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentFrom: Color,
    val accentTo: Color,
    val accentSoft: Color,
    val cardRadius: Dp,
    val btnRadius: Dp,
    val showPetIcons: Boolean
)

val CuteGlassPalette = LiquidGlassPalette(
    bgGrad1 = Color(0xFFFFE4E6),
    bgGrad2 = Color(0xFFFEF08A),
    bgGrad3 = Color(0xFFFBCFE8),
    bgBase = Color(0xFFF8FAFC),
    glassBg = Color(0xC7FFFFFF),
    glassStrong = Color(0xEDFFFFFF),
    glassBorder = Color(0xF5FFF1F2),
    textPrimary = Color(0xFF4A1936),
    textSecondary = Color(0xFF9D5274),
    accentFrom = Color(0xFFF43F5E),
    accentTo = Color(0xFFF59E0B),
    accentSoft = Color(0x26F43F5E),
    cardRadius = 26.dp,
    btnRadius = 18.dp,
    showPetIcons = true
)

val NormalGlassPalette = LiquidGlassPalette(
    bgGrad1 = Color(0xFFE0E7FF),
    bgGrad2 = Color(0xFFFCE7F3),
    bgGrad3 = Color(0xFFE0F2FE),
    bgBase = Color(0xFFF8FAFC),
    glassBg = Color(0xBDFFFFFF),
    glassStrong = Color(0xE6FFFFFF),
    glassBorder = Color(0xEBFFFFFF),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    accentFrom = Color(0xFF4F46E5),
    accentTo = Color(0xFFEC4899),
    accentSoft = Color(0x246366F1),
    cardRadius = 22.dp,
    btnRadius = 16.dp,
    showPetIcons = false
)

val LocalGlassPalette = staticCompositionLocalOf { CuteGlassPalette }

@Composable
fun VMNCalculatorTheme(
    themeMode: AppThemeMode,
    fontStyle: AppFontStyle,
    fontScalePercent: Int,
    content: @Composable () -> Unit
) {
    val palette = if (themeMode == AppThemeMode.CUTE) CuteGlassPalette else NormalGlassPalette
    val scale = fontScalePercent / 100f

    val activeFontFamily = when (fontStyle) {
        AppFontStyle.ROUNDED -> FontFamily.Cursive
        AppFontStyle.CLEAN -> FontFamily.SansSerif
        AppFontStyle.SYSTEM -> FontFamily.Default
        AppFontStyle.MONO -> FontFamily.Monospace
    }

    val typography = Typography(
        bodyLarge = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13.5 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (16 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (14 * scale).sp
        )
    )

    val colorScheme = lightColorScheme(
        primary = palette.accentFrom,
        secondary = palette.accentTo,
        background = palette.bgBase,
        surface = palette.glassStrong,
        onPrimary = Color.White,
        onBackground = palette.textPrimary,
        onSurface = palette.textPrimary
    )

    CompositionLocalProvider(LocalGlassPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
