package com.vmnstudios95.maytinhdanang.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vmnstudios95.maytinhdanang.ui.theme.LocalGlassPalette

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(palette.cardRadius)
    Column(
        modifier = modifier
            .shadow(8.dp, shape, ambientColor = Color(0x180F172A), spotColor = Color(0x180F172A))
            .clip(shape)
            .background(palette.glassBg)
            .border(1.dp, palette.glassBorder, shape)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun GlassSubCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape((palette.cardRadius.value - 5f).coerceAtLeast(12f).dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(palette.glassStrong)
            .border(1.dp, palette.glassBorder, shape)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp,
    isOperator: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(palette.btnRadius)
    val bgColor = if (isOperator) palette.accentSoft else palette.glassStrong
    val borderColor = if (isOperator) Color.White.copy(alpha = 0.75f) else palette.glassBorder

    Surface(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = minHeight),
        shape = shape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isOperator) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun AccentGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp,
    content: @Composable RowScope.() -> Unit
) {
    val palette = LocalGlassPalette.current
    val shape = RoundedCornerShape(palette.btnRadius)
    val gradient = Brush.linearGradient(listOf(palette.accentFrom, palette.accentTo))

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = minHeight),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .background(gradient, shape)
                .border(1.dp, Color.White.copy(alpha = 0.45f), shape)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun GlassInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textSecondary
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(13.dp),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(text = placeholder, color = palette.textSecondary.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.96f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.92f),
                focusedBorderColor = palette.accentFrom,
                unfocusedBorderColor = Color(0x5994A3B8),
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary
            )
        )
    }
}

@Composable
fun ResultHighlightBox(
    formulaText: String,
    moneyText: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.94f))
            .border(1.dp, palette.glassBorder, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .background(palette.accentFrom)
                .padding(horizontal = 2.5.dp, vertical = 34.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formulaText,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textSecondary
            )
            Text(
                text = moneyText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = palette.textPrimary
            )
        }
    }
}

@Composable
fun LinkActionText(
    text: String,
    onClick: () -> Unit
) {
    val palette = LocalGlassPalette.current
    Text(
        text = text,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = palette.accentFrom,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}
