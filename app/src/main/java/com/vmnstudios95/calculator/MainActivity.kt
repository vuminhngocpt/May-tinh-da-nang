app/src/main/java/com/vmnstudios95/calculator/MainActivity.kt
```[span_2](start_span)[span_2](end_span)[span_3](start_span)[span_3](end_span)

### 2. Nội dung mã nguồn `MainActivity.kt` (dán vào khung soạn thảo)
```kotlin
package com.vmnstudios95.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vmnstudios95.calculator.model.AppTab
import com.vmnstudios95.calculator.ui.components.SettingsModalSheet
import com.vmnstudios95.calculator.ui.components.VmnBrandLogo
import com.vmnstudios95.calculator.ui.screens.*
import com.vmnstudios95.calculator.ui.theme.VmnGlassTheme
import com.vmnstudios95.calculator.ui.theme.liquidGlassCard
import com.vmnstudios95.calculator.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: CalculatorViewModel = viewModel()
            val prefs by vm.prefs.collectAsState()
            val currentTab by vm.currentTab.collectAsState()
            val showSettings by vm.showSettings.collectAsState()

            VmnGlassTheme(prefs = prefs) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = if (prefs.darkMode) {
                                    listOf(
                                        Color(prefs.glassPreset.primaryHex).copy(alpha = 0.22f),
                                        Color(0xFF061014),
                                        Color(prefs.glassPreset.secondaryHex).copy(alpha = 0.16f)
                                    )
                                } else {
                                    listOf(
                                        Color(prefs.glassPreset.primaryHex).copy(alpha = 0.14f),
                                        Color(0xFFF1F7F6),
                                        Color(prefs.glassPreset.secondaryHex).copy(alpha = 0.12f)
                                    )
                                }
                            )
                        )
                ) {
                    val widthDp = maxWidth
                    val isCompactWatch = widthDp < 300.dp
                    val isWideScreen = widthDp >= 600.dp

                    val activeTab = if (currentTab in prefs.enabledTabs) currentTab else prefs.enabledTabs.first()
                    val visibleTabs = AppTab.entries.filter { it in prefs.enabledTabs }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (isCompactWatch) 6.dp else 12.dp, vertical = if (isCompactWatch) 4.dp else 8.dp)
                                .liquidGlassCard(prefs, cornerRadius = 16.dp)
                                .padding(horizontal = if (isCompactWatch) 8.dp else 12.dp, vertical = if (isCompactWatch) 4.dp else 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                VmnBrandLogo(prefs = prefs, size = if (isCompactWatch) 24.dp else 32.dp)
                                Column {
                                    Text(
                                        text = if (isCompactWatch) "VMN Calc" else "VMN Máy Tính Đa Năng",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isCompactWatch) 12.sp else 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!isCompactWatch) {
                                        Text(
                                            text = "${activeTab.titleVi} • ${prefs.currency.code} • ${prefs.language.label}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { vm.openSettings(true) },
                                modifier = Modifier.size(if (isCompactWatch) 28.dp else 36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Cài đặt",
                                    modifier = Modifier.size(if (isCompactWatch) 18.dp else 22.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (isWideScreen) {
                                Column(
                                    modifier = Modifier
                                        .width(104.dp)
                                        .fillMaxHeight()
                                        .padding(start = 10.dp, bottom = 10.dp)
                                        .liquidGlassCard(prefs, cornerRadius = 20.dp)
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    visibleTabs.forEach { tab ->
                                        val selected = tab == activeTab
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                                    else Color.Transparent
                                                )
                                                .clickable { vm.selectTab(tab) }
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = tabIcon(tab),
                                                contentDescription = tab.titleVi,
                                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = tab.titleVi,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                when (activeTab) {
                                    AppTab.BASIC -> BasicCalculatorScreen(vm, prefs, isCompactWatch)
                                    AppTab.HOURLY -> HourlyBillingScreen(vm, prefs)
                                    AppTab.WEIGHT -> WeightBillingScreen(vm, prefs)
                                    AppTab.ELECTRIC -> ElectricBillingScreen(vm, prefs)
                                    AppTab.NOTES -> NotesScreen(vm, prefs)
                                    AppTab.ATTENDANCE -> AttendanceScreen(vm, prefs)
                                    AppTab.HISTORY -> HistoryScreen(vm, prefs)
                                }
                            }
                        }

                        if (!isWideScreen) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = if (isCompactWatch) 4.dp else 10.dp, vertical = if (isCompactWatch) 4.dp else 8.dp)
                                    .liquidGlassCard(prefs, cornerRadius = 18.dp)
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 6.dp, vertical = if (isCompactWatch) 4.dp else 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(if (isCompactWatch) 4.dp else 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                visibleTabs.forEach { tab ->
                                    val selected = tab == activeTab
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                                else Color.Transparent
                                            )
                                            .clickable { vm.selectTab(tab) }
                                            .padding(
                                                horizontal = if (isCompactWatch) 8.dp else 12.dp,
                                                vertical = if (isCompactWatch) 4.dp else 6.dp
                                            ),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = tabIcon(tab),
                                            contentDescription = tab.titleVi,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                            modifier = Modifier.size(if (isCompactWatch) 16.dp else 20.dp)
                                        )
                                        if (!isCompactWatch) {
                                            Text(
                                                text = tab.titleVi,
                                                fontSize = 11.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showSettings) {
                        SettingsModalSheet(
                            vm = vm,
                            prefs = prefs,
                            onDismiss = { vm.openSettings(false) }
                        )
                    }
                }
            }
        }
    }

    private fun tabIcon(tab: AppTab): ImageVector = when (tab) {
        AppTab.BASIC -> Icons.Default.Calculate
        AppTab.HOURLY -> Icons.Default.Schedule
        AppTab.WEIGHT -> Icons.Default.Scale
        AppTab.ELECTRIC -> Icons.Default.Bolt
        AppTab.NOTES -> Icons.Default.Description
        AppTab.ATTENDANCE -> Icons.Default.Badge
        AppTab.HISTORY -> Icons.Default.History
    }
}
```[span_4](start_span)[span_4](end_span)[span_5](start_span)[span_5](end_span)[span_6](start_span)[span_6](end_span)

### 3. Lời nhắn commit gợi ý
```text
Add app/src/main/java/com/vmnstudios95/calculator/MainActivity.kt
```[span_7](start_span)[span_7](end_span)[span_8](start_span)[span_8](end_span)

Bạn đã dán và lưu xong `MainActivity.kt` chưa, hay muốn làm tiếp tệp nào tiếp theo?
