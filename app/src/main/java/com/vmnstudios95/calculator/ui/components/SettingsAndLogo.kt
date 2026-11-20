package com.vmnstudios95.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vmnstudios95.calculator.model.*
import com.vmnstudios95.calculator.ui.theme.liquidGlassCard
import com.vmnstudios95.calculator.viewmodel.CalculatorViewModel

@Composable
fun VmnBrandLogo(
    prefs: AppPreferencesState,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    val primary = Color(prefs.glassPreset.primaryHex)
    val secondary = Color(prefs.glassPreset.secondaryHex)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                Brush.linearGradient(
                    colors = listOf(primary.copy(alpha = 0.9f), secondary.copy(alpha = 0.85f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(size * 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "VMN",
            color = Color.White,
            fontSize = (size.value * 0.31f).sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun SettingsModalSheet(
    vm: CalculatorViewModel,
    prefs: AppPreferencesState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VmnBrandLogo(prefs = prefs, size = 30.dp)
                    Text("Cài đặt", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chế độ nền tối", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Switch(checked = prefs.darkMode, onCheckedChange = { vm.setDarkMode(it) })
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Độ trong suốt kính lỏng: ${(prefs.glassIntensity * 100).toInt()}%", fontSize = 13.sp)
                    Slider(
                        value = prefs.glassIntensity,
                        onValueChange = { vm.setGlassIntensity(it) },
                        valueRange = 0.25f..0.95f
                    )
                }

                Text("Giao diện kính lỏng", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassPreset.entries.forEach { preset ->
                        val selected = prefs.glassPreset == preset
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { vm.setGlassPreset(preset) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(preset.primaryHex), Color(preset.secondaryHex))
                                        )
                                    )
                                    .border(
                                        width = if (selected) 2.5.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = preset.label.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Text("Ngôn ngữ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = prefs.language == lang,
                            onClick = { vm.setLanguage(lang) },
                            label = { Text(lang.label, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Text("Đơn vị tiền tệ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CurrencyUnit.entries.forEach { curr ->
                        FilterChip(
                            selected = prefs.currency == curr,
                            onClick = { vm.setCurrency(curr) },
                            label = { Text("${curr.code} (${curr.symbol})", maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Text("Bật / Tắt chức năng", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                AppTab.entries.forEach { tab ->
                    val enabled = tab in prefs.enabledTabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tab.titleVi, fontSize = 13.sp)
                        Switch(
                            checked = enabled,
                            onCheckedChange = { vm.toggleTabEnabled(tab) },
                            enabled = !(enabled && prefs.enabledTabs.size == 1)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(prefs, cornerRadius = 14.dp)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Thông tin ứng dụng", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Tên ứng dụng : VMN Máy Tính Đa Năng", fontSize = 12.sp)
                    Text("Nhà Phát Hành : VMN Studios 95", fontSize = 12.sp)
                    Text("Nhà Phát Triển : Vũ Minh Ngọc", fontSize = 12.sp)
                    Text("Bản Quyền : Mã Nguồn Mở", fontSize = 12.sp)
                    Text("Liên Hệ : Email: vuminhngocpt@gmail.com", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Xong")
            }
        }
    )
}
