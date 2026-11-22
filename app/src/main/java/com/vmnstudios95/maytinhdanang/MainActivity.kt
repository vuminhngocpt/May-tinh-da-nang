@file:OptIn(ExperimentalFoundationApi::class)

package com.vmnstudios95.maytinhdanang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vmnstudios95.maytinhdanang.data.LocalStateStorage
import com.vmnstudios95.maytinhdanang.model.AppTab
import com.vmnstudios95.maytinhdanang.model.CalculatorAppState
import com.vmnstudios95.maytinhdanang.model.HistoryItem
import com.vmnstudios95.maytinhdanang.ui.components.GlassPanel
import com.vmnstudios95.maytinhdanang.ui.screens.AboutTab
import com.vmnstudios95.maytinhdanang.ui.screens.AdvancedSettingsTab
import com.vmnstudios95.maytinhdanang.ui.screens.HistoryTab
import com.vmnstudios95.maytinhdanang.ui.screens.HoursCalculatorTab
import com.vmnstudios95.maytinhdanang.ui.screens.LiterCalculatorTab
import com.vmnstudios95.maytinhdanang.ui.screens.SettingsTab
import com.vmnstudios95.maytinhdanang.ui.screens.StandardCalculatorTab
import com.vmnstudios95.maytinhdanang.ui.screens.WeightCalculatorTab
import com.vmnstudios95.maytinhdanang.ui.theme.LocalGlassPalette
import com.vmnstudios95.maytinhdanang.ui.theme.VMNCalculatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val storage = remember { LocalStateStorage(context) }
            var appState by remember { mutableStateOf(storage.loadState()) }

            fun updateState(transform: (CalculatorAppState) -> CalculatorAppState) {
                val updated = transform(appState)
                appState = updated
                storage.saveState(updated)
            }

            VMNCalculatorTheme(
                themeMode = appState.theme,
                fontStyle = appState.font,
                fontScalePercent = appState.fontScale
            ) {
                VMNCalculatorAppScreen(
                    state = appState,
                    onUpdateState = ::updateState,
                    storage = storage
                )
            }
        }
    }
}

@Composable
private fun VMNCalculatorAppScreen(
    state: CalculatorAppState,
    onUpdateState: ((CalculatorAppState) -> CalculatorAppState) -> Unit,
    storage: LocalStateStorage
) {
    val palette = LocalGlassPalette.current
    val tabs = AppTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val tabRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var toastMessage by remember { mutableStateOf<String?>(null) }

    fun showToast(msg: String) {
        toastMessage = msg
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2200)
            toastMessage = null
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        tabRowState.animateScrollToItem(pagerState.currentPage)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            storage.exportToUri(uri, state).fold(
                onSuccess = { showToast("Đã xuất tệp đồng bộ .json") },
                onFailure = { showToast("Không thể xuất tệp .json") }
            )
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            storage.importFromUri(uri, state).fold(
                onSuccess = { imported ->
                    onUpdateState { imported }
                    showToast("Đã nạp dữ liệu đồng bộ thành công")
                },
                onFailure = { showToast("Tệp .json không hợp lệ") }
            )
        }
    }

    fun recordHistory(category: String, detail: String) {
        val entry = HistoryItem(category = category, detail = detail)
        onUpdateState { prev ->
            prev.copy(history = (listOf(entry) + prev.history).take(250))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Nền gradient Liquid Glass
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = palette.bgBase)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.bgGrad1, Color.Transparent),
                    center = Offset(size.width * 0.12f, size.height * 0.14f),
                    radius = size.maxDimension * 0.55f
                ),
                center = Offset(size.width * 0.12f, size.height * 0.14f),
                radius = size.maxDimension * 0.55f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.bgGrad2, Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.18f),
                    radius = size.maxDimension * 0.55f
                ),
                center = Offset(size.width * 0.88f, size.height * 0.18f),
                radius = size.maxDimension * 0.55f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.bgGrad3, Color.Transparent),
                    center = Offset(size.width * 0.50f, size.height * 0.92f),
                    radius = size.maxDimension * 0.60f
                ),
                center = Offset(size.width * 0.50f, size.height * 0.92f),
                radius = size.maxDimension * 0.60f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .widthIn(max = 780.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thanh điều hướng 8 tab
            LazyRow(
                state = tabRowState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                itemsIndexed(tabs) { index, tab ->
                    val isSelected = pagerState.currentPage == index
                    val activeGradient = Brush.linearGradient(listOf(palette.accentFrom, palette.accentTo))

                    Surface(
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        shape = CircleShape,
                        color = if (isSelected) Color.Transparent else palette.glassStrong,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color.White.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .then(
                                    if (isSelected) Modifier.background(activeGradient, CircleShape)
                                    else Modifier
                                )
                                .padding(horizontal = 13.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (palette.showPetIcons || tab == AppTab.SETTINGS) {
                                Text(text = tab.petIcon, fontSize = 13.sp)
                            }
                            Text(
                                text = if (tab == AppTab.HISTORY) "${tab.label} (${state.history.size})" else tab.label,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else palette.textSecondary
                            )
                        }
                    }
                }
            }

            // Khung nội dung vuốt ngang đồng bộ với tab
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(12.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    when (tabs[pageIndex]) {
                        AppTab.CALC -> StandardCalculatorTab(
                            onRecordHistory = ::recordHistory
                        )
                        AppTab.WEIGHT -> WeightCalculatorTab(
                            config = state.weight,
                            onNavigateTab = { target ->
                                scope.launch { pagerState.animateScrollToPage(tabs.indexOf(target)) }
                            },
                            onRecordHistory = ::recordHistory
                        )
                        AppTab.HOURS -> HoursCalculatorTab(
                            config = state.hours,
                            onNavigateTab = { target ->
                                scope.launch { pagerState.animateScrollToPage(tabs.indexOf(target)) }
                            },
                            onRecordHistory = ::recordHistory
                        )
                        AppTab.LITER -> LiterCalculatorTab(
                            config = state.liter,
                            onNavigateTab = { target ->
                                scope.launch { pagerState.animateScrollToPage(tabs.indexOf(target)) }
                            },
                            onRecordHistory = ::recordHistory
                        )
                        AppTab.CUSTOM -> AdvancedSettingsTab(
                            state = state,
                            onSaveAdvanced = { name, desc, w, h, l ->
                                onUpdateState {
                                    it.copy(appName = name, appDesc = desc, weight = w, hours = h, liter = l)
                                }
                                showToast("Đã lưu cấu hình nâng cao")
                            },
                            onResetDefaults = {
                                val def = CalculatorAppState()
                                onUpdateState {
                                    it.copy(
                                        appName = def.appName,
                                        appDesc = def.appDesc,
                                        weight = def.weight,
                                        hours = def.hours,
                                        liter = def.liter
                                    )
                                }
                                showToast("Đã khôi phục mặc định")
                            },
                            onShowToast = ::showToast
                        )
                        AppTab.HISTORY -> HistoryTab(
                            history = state.history,
                            onDeleteItem = { itemId ->
                                onUpdateState { prev ->
                                    prev.copy(history = prev.history.filterNot { it.id == itemId })
                                }
                                showToast("Đã xóa mục lịch sử")
                            },
                            onClearAll = {
                                onUpdateState { it.copy(history = emptyList()) }
                                showToast("Đã xóa toàn bộ lịch sử")
                            }
                        )
                        AppTab.ABOUT -> AboutTab(
                            state = state,
                            onNavigateTab = { target ->
                                scope.launch { pagerState.animateScrollToPage(tabs.indexOf(target)) }
                            },
                            onShowToast = ::showToast
                        )
                        AppTab.SETTINGS -> SettingsTab(
                            state = state,
                            onThemeChange = { mode -> onUpdateState { it.copy(theme = mode) } },
                            onFontChange = { font -> onUpdateState { it.copy(font = font) } },
                            onFontScaleChange = { scale -> onUpdateState { it.copy(fontScale = scale) } },
                            onExportJson = { exportLauncher.launch("vmn-may-tinh-dong-bo.json") },
                            onImportJson = { importLauncher.launch(arrayOf("application/json", "*/*")) }
                        )
                    }
                }
            }
        }

        // Toast thông báo nổi
        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .systemBarsPadding()
                    .padding(bottom = 18.dp)
                    .clip(CircleShape)
                    .background(Color(0xEB0F172A))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
