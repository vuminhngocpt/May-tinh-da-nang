package com.vmnstudios95.maytinhdanang.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vmnstudios95.maytinhdanang.engine.MathExpressionEngine
import com.vmnstudios95.maytinhdanang.model.AppFontStyle
import com.vmnstudios95.maytinhdanang.model.AppTab
import com.vmnstudios95.maytinhdanang.model.AppThemeMode
import com.vmnstudios95.maytinhdanang.model.CalculatorAppState
import com.vmnstudios95.maytinhdanang.model.HistoryItem
import com.vmnstudios95.maytinhdanang.model.HoursConfig
import com.vmnstudios95.maytinhdanang.model.LiterConfig
import com.vmnstudios95.maytinhdanang.model.WeightConfig
import com.vmnstudios95.maytinhdanang.ui.components.AccentGradientButton
import com.vmnstudios95.maytinhdanang.ui.components.GlassButton
import com.vmnstudios95.maytinhdanang.ui.components.GlassInputField
import com.vmnstudios95.maytinhdanang.ui.components.GlassSubCard
import com.vmnstudios95.maytinhdanang.ui.components.LinkActionText
import com.vmnstudios95.maytinhdanang.ui.components.ResultHighlightBox
import com.vmnstudios95.maytinhdanang.ui.theme.LocalGlassPalette
import kotlin.math.roundToInt
import kotlin.math.roundToLong

// 1. TAB MÁY TÍNH CHUỖI
@Composable
fun StandardCalculatorTab(
    onRecordHistory: (category: String, detail: String) -> Unit
) {
    val palette = LocalGlassPalette.current
    var rawExpr by remember { mutableStateOf("") }
    var displayExpr by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }

    val resultFontSize = when {
        resultText.length >= 16 -> 20.sp
        resultText.length >= 10 -> 28.sp
        else -> 38.sp
    }

    fun appendToken(token: String) {
        rawExpr += MathExpressionEngine.normalizeOperator(token)
        displayExpr = MathExpressionEngine.formatExpressionForDisplay(rawExpr)
    }

    fun clearAll() {
        rawExpr = ""
        displayExpr = ""
        resultText = "0"
    }

    fun backspace() {
        if (rawExpr.isNotEmpty()) {
            rawExpr = rawExpr.dropLast(1)
            displayExpr = MathExpressionEngine.formatExpressionForDisplay(rawExpr)
        }
    }

    fun evaluate() {
        val trimmed = rawExpr.trim()
        if (trimmed.isEmpty()) return
        MathExpressionEngine.evaluate(trimmed).fold(
            onSuccess = { value ->
                val formattedRes = MathExpressionEngine.formatCleanNumber(value)
                val formattedExp = MathExpressionEngine.formatExpressionForDisplay(trimmed)
                displayExpr = formattedExp
                resultText = formattedRes
                onRecordHistory("Máy tính", "$formattedExp = $formattedRes")
            },
            onFailure = { err ->
                resultText = err.message ?: "Lỗi phép tính"
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        GlassSubCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (palette.showPetIcons) "🐶 " else ""}Phép tính chuỗi (+, -, ×, ÷, %, ngoặc)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textSecondary
                )
                Text(
                    text = "Tự lưu lịch sử",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textSecondary
                )
            }
            Text(
                text = displayExpr,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 24.dp),
                textAlign = TextAlign.End,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textSecondary
            )
            Text(
                text = resultText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = resultFontSize,
                fontWeight = FontWeight.Black,
                lineHeight = (resultFontSize.value * 1.12f).sp,
                color = palette.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val rows = listOf(
            listOf("(", ")", "⌫", "C"),
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "−"),
            listOf("%", "0", ".", "+")
        )

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    rowItems.forEach { key ->
                        val isOp = key in listOf("(", ")", "⌫", "C", "÷", "×", "−", "%", "+")
                        GlassButton(
                            onClick = {
                                when (key) {
                                    "C" -> clearAll()
                                    "⌫" -> backspace()
                                    else -> appendToken(key)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            isOperator = isOp
                        ) {
                            Text(
                                text = key,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                GlassButton(
                    onClick = { appendToken("00") },
                    modifier = Modifier.weight(1f),
                    isOperator = false
                ) {
                    Text(
                        text = "00",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }
                AccentGradientButton(
                    onClick = { evaluate() },
                    modifier = Modifier.weight(3f)
                ) {
                    if (palette.showPetIcons) {
                        Text(text = "🐰", fontSize = 16.sp)
                    }
                    Text(
                        text = "Tính kết quả (=)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 2. TAB TÍNH TIỀN THEO CÂN NẶNG
@Composable
fun WeightCalculatorTab(
    config: WeightConfig,
    onNavigateTab: (AppTab) -> Unit,
    onRecordHistory: (String, String) -> Unit
) {
    val palette = LocalGlassPalette.current
    var amountText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("kg") }
    var priceText by remember(config.defaultPrice) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(config.defaultPrice))
    }
    var formulaText by remember { mutableStateOf("Nhập khối lượng và đơn giá để tính") }
    var totalMoneyText by remember { mutableStateOf("0 đ") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val langKgStr = MathExpressionEngine.formatCleanNumber(config.langToKg)
    val units = listOf(
        "kg" to "Kilôgam (kg)",
        "lang" to "Lạng (1 lạng = $langKgStr kg)",
        "g" to "Gam (g = 0.001 kg)",
        "yen" to "Yến (10 kg)",
        "ta" to "Tạ (100 kg)",
        "tan" to "Tấn (1000 kg)"
    )

    fun calculate() {
        val amt = amountText.toDoubleOrNull()
        val price = priceText.toDoubleOrNull()
        if (amt == null || !amt.isFinite() || amt <= 0.0) {
            formulaText = "Vui lòng nhập khối lượng lớn hơn 0"
            totalMoneyText = "0 đ"
            return
        }
        if (price == null || !price.isFinite() || price < 0.0) {
            formulaText = "Đơn giá / kg không hợp lệ"
            totalMoneyText = "0 đ"
            return
        }
        val factor = when (selectedUnit) {
            "lang" -> MathExpressionEngine.cleanNumber(config.langToKg, 8)
            "g" -> 0.001
            "yen" -> 10.0
            "ta" -> 100.0
            "tan" -> 1000.0
            else -> 1.0
        }
        val kg = MathExpressionEngine.cleanNumber(amt * factor, 8)
        val total = MathExpressionEngine.cleanNumber(kg * price, 6).roundToLong().toDouble()
        val unitLabel = units.find { it.first == selectedUnit }?.second ?: selectedUnit
        val summary = "${MathExpressionEngine.formatCleanNumber(amt)} ($unitLabel) = " +
            "${MathExpressionEngine.formatCleanNumber(kg)} kg × ${MathExpressionEngine.formatCurrencyVND(price)}"
        formulaText = summary
        totalMoneyText = MathExpressionEngine.formatCurrencyVND(total)
        onRecordHistory("Cân nặng", "$summary = $totalMoneyText")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (palette.showPetIcons) "🐻 " else ""}Tính tiền theo cân nặng",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary
                )
                LinkActionText(text = "Sửa mặc định") { onNavigateTab(AppTab.CUSTOM) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                GlassInputField(
                    label = "Khối lượng",
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = "VD: 2.5",
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Đơn vị cân nặng",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary
                    )
                    Box {
                        Surface(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 54.dp),
                            shape = RoundedCornerShape(13.dp),
                            color = Color.White.copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5994A3B8))
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = units.find { it.first == selectedUnit }?.second ?: "Kilôgam (kg)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            units.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 13.5.sp) },
                                    onClick = {
                                        selectedUnit = key
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            GlassInputField(
                label = "Đơn giá / kg chuẩn (VNĐ)",
                value = priceText,
                onValueChange = { priceText = it },
                modifier = Modifier.fillMaxWidth()
            )

            ResultHighlightBox(
                formulaText = formulaText,
                moneyText = totalMoneyText
            )

            AccentGradientButton(
                onClick = { calculate() },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (palette.showPetIcons) Text("🐻", fontSize = 16.sp)
                Text("Tính & Lưu vào lịch sử", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

// 3. TAB TÍNH TIỀN THEO GIỜ LÀM
@Composable
fun HoursCalculatorTab(
    config: HoursConfig,
    onNavigateTab: (AppTab) -> Unit,
    onRecordHistory: (String, String) -> Unit
) {
    val palette = LocalGlassPalette.current
    var hoursText by remember { mutableStateOf("") }
    var minutesText by remember { mutableStateOf("") }
    var rateText by remember(config.defaultRate) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(config.defaultRate))
    }
    var factorText by remember(config.defaultFactor) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(config.defaultFactor))
    }
    var formulaText by remember { mutableStateOf("Tổng: 0.00 tiếng × hệ số 1") }
    var totalMoneyText by remember { mutableStateOf("0 đ") }

    fun calculate() {
        val rawH = if (hoursText.isBlank()) 0.0 else hoursText.toDoubleOrNull()
        val rawM = if (minutesText.isBlank()) 0.0 else minutesText.toDoubleOrNull()
        val rate = rateText.toDoubleOrNull()
        val factor = factorText.toDoubleOrNull()

        if (rawH == null || !rawH.isFinite() || rawH < 0.0 || rawM == null || !rawM.isFinite() || rawM < 0.0) {
            formulaText = "Số tiếng hoặc số phút không hợp lệ"
            totalMoneyText = "0 đ"
            return
        }
        if (rate == null || !rate.isFinite() || rate < 0.0 || factor == null || !factor.isFinite() || factor <= 0.0) {
            formulaText = "Tiền công / tiếng hoặc hệ số giờ không hợp lệ"
            totalMoneyText = "0 đ"
            return
        }

        val totalHours = MathExpressionEngine.cleanNumber(rawH + rawM / 60.0, 6)
        if (totalHours <= 0.0) {
            formulaText = "Vui lòng nhập thời gian làm việc lớn hơn 0"
            totalMoneyText = "0 đ"
            return
        }

        val total = MathExpressionEngine.cleanNumber(totalHours * rate * factor, 6).roundToLong().toDouble()
        val summary = "Tổng: ${String.format(java.util.Locale.US, "%.2f", totalHours)} tiếng × " +
            "${MathExpressionEngine.formatCurrencyVND(rate)} × hệ số ${MathExpressionEngine.formatCleanNumber(factor)}"
        formulaText = summary
        totalMoneyText = MathExpressionEngine.formatCurrencyVND(total)
        onRecordHistory("Giờ làm", "$summary = $totalMoneyText")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (palette.showPetIcons) "🦊 " else ""}Tính tiền theo giờ làm (tiếng)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary
                )
                LinkActionText(text = "Sửa mặc định") { onNavigateTab(AppTab.CUSTOM) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassInputField(
                    label = "Số tiếng làm việc",
                    value = hoursText,
                    onValueChange = { hoursText = it },
                    placeholder = "VD: 8",
                    modifier = Modifier.weight(1f)
                )
                GlassInputField(
                    label = "Số phút lẻ",
                    value = minutesText,
                    onValueChange = { minutesText = it },
                    placeholder = "VD: 30",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassInputField(
                    label = "Tiền công / tiếng (VNĐ)",
                    value = rateText,
                    onValueChange = { rateText = it },
                    modifier = Modifier.weight(1f)
                )
                GlassInputField(
                    label = "Hệ số giờ (VD: 1 / 1.5)",
                    value = factorText,
                    onValueChange = { factorText = it },
                    modifier = Modifier.weight(1f)
                )
            }

            ResultHighlightBox(
                formulaText = formulaText,
                moneyText = totalMoneyText
            )

            AccentGradientButton(
                onClick = { calculate() },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (palette.showPetIcons) Text("🦊", fontSize = 16.sp)
                Text("Tính & Lưu vào lịch sử", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

// 4. TAB TÍNH TIỀN THEO SỐ LÍT
@Composable
fun LiterCalculatorTab(
    config: LiterConfig,
    onNavigateTab: (AppTab) -> Unit,
    onRecordHistory: (String, String) -> Unit
) {
    val palette = LocalGlassPalette.current
    var amountText by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("l") }
    var priceText by remember(config.defaultPrice) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(config.defaultPrice))
    }
    var formulaText by remember { mutableStateOf("Nhập dung tích và đơn giá để tính") }
    var totalMoneyText by remember { mutableStateOf("0 đ") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val canLiterStr = MathExpressionEngine.formatCleanNumber(config.canToLiter)
    val units = listOf(
        "l" to "Lít (l)",
        "ml" to "Mililít (ml = 0.001 l)",
        "can" to "Can (1 can = $canLiterStr l)"
    )

    fun calculate() {
        val amt = amountText.toDoubleOrNull()
        val price = priceText.toDoubleOrNull()
        if (amt == null || !amt.isFinite() || amt <= 0.0) {
            formulaText = "Vui lòng nhập dung tích lớn hơn 0"
            totalMoneyText = "0 đ"
            return
        }
        if (price == null || !price.isFinite() || price < 0.0) {
            formulaText = "Đơn giá / lít không hợp lệ"
            totalMoneyText = "0 đ"
            return
        }
        val factor = when (selectedUnit) {
            "ml" -> 0.001
            "can" -> MathExpressionEngine.cleanNumber(config.canToLiter, 8)
            else -> 1.0
        }
        val liters = MathExpressionEngine.cleanNumber(amt * factor, 8)
        val total = MathExpressionEngine.cleanNumber(liters * price, 6).roundToLong().toDouble()
        val unitLabel = units.find { it.first == selectedUnit }?.second ?: selectedUnit
        val summary = "${MathExpressionEngine.formatCleanNumber(amt)} ($unitLabel) = " +
            "${MathExpressionEngine.formatCleanNumber(liters)} lít × ${MathExpressionEngine.formatCurrencyVND(price)}"
        formulaText = summary
        totalMoneyText = MathExpressionEngine.formatCurrencyVND(total)
        onRecordHistory("Số lít", "$summary = $totalMoneyText")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (palette.showPetIcons) "🐸 " else ""}Tính tiền theo lít",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary
                )
                LinkActionText(text = "Sửa mặc định") { onNavigateTab(AppTab.CUSTOM) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                GlassInputField(
                    label = "Dung tích",
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = "VD: 20",
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Đơn vị thể tích",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary
                    )
                    Box {
                        Surface(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 54.dp),
                            shape = RoundedCornerShape(13.dp),
                            color = Color.White.copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5994A3B8))
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = units.find { it.first == selectedUnit }?.second ?: "Lít (l)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            units.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 13.5.sp) },
                                    onClick = {
                                        selectedUnit = key
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            GlassInputField(
                label = "Đơn giá / lít chuẩn (VNĐ)",
                value = priceText,
                onValueChange = { priceText = it },
                modifier = Modifier.fillMaxWidth()
            )

            ResultHighlightBox(
                formulaText = formulaText,
                moneyText = totalMoneyText
            )

            AccentGradientButton(
                onClick = { calculate() },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (palette.showPetIcons) Text("🐸", fontSize = 16.sp)
                Text("Tính & Lưu vào lịch sử", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

// 5. TAB SỬA TÍNH NĂNG NÂNG CAO
@Composable
fun AdvancedSettingsTab(
    state: CalculatorAppState,
    onSaveAdvanced: (String, String, WeightConfig, HoursConfig, LiterConfig) -> Unit,
    onResetDefaults: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val palette = LocalGlassPalette.current
    var nameText by remember(state.appName) { mutableStateOf(state.appName) }
    var descText by remember(state.appDesc) { mutableStateOf(state.appDesc) }
    var wPriceText by remember(state.weight.defaultPrice) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(state.weight.defaultPrice))
    }
    var langKgText by remember(state.weight.langToKg) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(state.weight.langToKg))
    }
    var hRateText by remember(state.hours.defaultRate) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(state.hours.defaultRate))
    }
    var hFactorText by remember(state.hours.defaultFactor) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(state.hours.defaultFactor))
    }
    var lPriceText by remember(state.liter.defaultPrice) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(state.liter.defaultPrice))
    }
    var canLiterText by remember(state.liter.canToLiter) {
        mutableStateOf(MathExpressionEngine.formatCleanNumber(state.liter.canToLiter))
    }
    var statusText by remember { mutableStateOf("Tự động lưu cục bộ & hỗ trợ đồng bộ tệp.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (palette.showPetIcons) "🐼 " else ""}Sửa tất cả tính năng nâng cao",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary
                )
                LinkActionText(text = "Khôi phục mặc định") {
                    onResetDefaults()
                    statusText = "Đã khôi phục cấu hình mặc định."
                }
            }

            SectionKicker("THÔNG TIN ỨNG DỤNG")
            GlassInputField(
                label = "Tên ứng dụng",
                value = nameText,
                onValueChange = { nameText = it },
                keyboardType = KeyboardType.Text
            )
            GlassInputField(
                label = "Dòng mô tả ngắn",
                value = descText,
                onValueChange = { descText = it },
                keyboardType = KeyboardType.Text
            )

            SectionKicker("CẤU HÌNH CÂN NẶNG")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassInputField(
                    label = "Giá mặc định / kg",
                    value = wPriceText,
                    onValueChange = { wPriceText = it },
                    modifier = Modifier.weight(1f)
                )
                GlassInputField(
                    label = "1 Lạng = ? kg",
                    value = langKgText,
                    onValueChange = { langKgText = it },
                    modifier = Modifier.weight(1f)
                )
            }

            SectionKicker("CẤU HÌNH GIỜ LÀM (TIẾNG)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassInputField(
                    label = "Lương mặc định / tiếng",
                    value = hRateText,
                    onValueChange = { hRateText = it },
                    modifier = Modifier.weight(1f)
                )
                GlassInputField(
                    label = "Hệ số giờ mặc định",
                    value = hFactorText,
                    onValueChange = { hFactorText = it },
                    modifier = Modifier.weight(1f)
                )
            }

            SectionKicker("CẤU HÌNH SỐ LÍT")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassInputField(
                    label = "Giá mặc định / lít",
                    value = lPriceText,
                    onValueChange = { lPriceText = it },
                    modifier = Modifier.weight(1f)
                )
                GlassInputField(
                    label = "1 Can chuẩn = ? lít",
                    value = canLiterText,
                    onValueChange = { canLiterText = it },
                    modifier = Modifier.weight(1f)
                )
            }

            AccentGradientButton(
                onClick = {
                    val wp = wPriceText.toDoubleOrNull()
                    val lk = langKgText.toDoubleOrNull()
                    val hr = hRateText.toDoubleOrNull()
                    val hf = hFactorText.toDoubleOrNull()
                    val lp = lPriceText.toDoubleOrNull()
                    val cl = canLiterText.toDoubleOrNull()

                    if (wp == null || wp < 0 || lk == null || lk <= 0 ||
                        hr == null || hr < 0 || hf == null || hf <= 0 ||
                        lp == null || lp < 0 || cl == null || cl <= 0
                    ) {
                        statusText = "Vui lòng nhập các thông số hợp lệ (> 0 cho hệ số & đơn vị)."
                        onShowToast("Kiểm tra lại thông số nâng cao")
                    } else {
                        onSaveAdvanced(
                            nameText.trim().ifEmpty { "VMN Máy Tính Đa Năng" },
                            descText.trim().ifEmpty { "Tự động lưu lịch sử • Đồng bộ tệp • Tùy biến linh hoạt" },
                            WeightConfig(MathExpressionEngine.cleanNumber(wp, 4), MathExpressionEngine.cleanNumber(lk, 6)),
                            HoursConfig(MathExpressionEngine.cleanNumber(hr, 4), MathExpressionEngine.cleanNumber(hf, 6)),
                            LiterConfig(MathExpressionEngine.cleanNumber(lp, 4), MathExpressionEngine.cleanNumber(cl, 6))
                        )
                        statusText = "Đã lưu cấu hình nâng cao thành công!"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (palette.showPetIcons) Text("🐼", fontSize = 16.sp)
                Text("Lưu cấu hình nâng cao", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }

            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary
            )
        }
    }
}

@Composable
private fun SectionKicker(title: String) {
    val palette = LocalGlassPalette.current
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.6.sp,
        color = palette.textSecondary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// 6. TAB LỊCH SỬ TÍNH TOÁN
@Composable
fun HistoryTab(
    history: List<HistoryItem>,
    onDeleteItem: (String) -> Unit,
    onClearAll: () -> Unit
) {
    val palette = LocalGlassPalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (palette.showPetIcons) "🦉 " else ""}Lịch sử tính toán tự động",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary
                )
                GlassButton(onClick = onClearAll, minHeight = 32.dp) {
                    Text("Xóa hết", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                }
            }

            if (history.isEmpty()) {
                Text(
                    text = "Chưa có phép tính nào được lưu.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = palette.textSecondary
                )
            } else {
                history.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(1.dp, Color.White, RoundedCornerShape(15.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.category,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textSecondary
                                )
                                Text(
                                    text = item.time,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textSecondary
                                )
                            }
                            Text(
                                text = item.detail,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }

                        Surface(
                            onClick = { onDeleteItem(item.id) },
                            modifier = Modifier.size(34.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x47F43F5E))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "✕",
                                    color = Color(0xFFE11D48),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. TAB GIỚI THIỆU ỨNG DỤNG
@Composable
fun AboutTab(
    state: CalculatorAppState,
    onNavigateTab: (AppTab) -> Unit,
    onShowToast: (String) -> Unit
) {
    val palette = LocalGlassPalette.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val gradient = Brush.linearGradient(listOf(palette.accentFrom, palette.accentTo))

    fun copyAndNotify(text: String) {
        clipboard.setText(AnnotatedString(text))
        onShowToast("Đã sao chép: $text")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (palette.showPetIcons) "🐨 " else ""}Giới thiệu ứng dụng",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary
                )
                LinkActionText(text = "← Máy tính") { onNavigateTab(AppTab.CALC) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${if (palette.showPetIcons) "🐱" else ""}V",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.appName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.5.sp,
                        color = palette.textPrimary
                    )
                    Text(
                        text = state.appDesc,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(gradient)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "v2.0 • OSS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            AboutInfoRow(label = "Phiên bản phần mềm", value = "2.0")
            AboutInfoRow(label = "Nhà Phát Triển", value = "Vũ Minh Ngọc | VMN Studios 95")
            AboutInfoRow(label = "Bản Quyền", value = "Miễn Phí Mã Nguồn Mở")

            AboutLinkRow(
                label = "Github",
                linkText = "github.com/vuminhngocpt",
                onOpenLink = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vuminhngocpt"))
                    context.startActivity(intent)
                },
                onCopy = { copyAndNotify("https://github.com/vuminhngocpt") }
            )

            AboutLinkRow(
                label = "Liên hệ",
                linkText = "vuminhngocpt@gmail.com",
                onOpenLink = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:vuminhngocpt@gmail.com"))
                    context.startActivity(intent)
                },
                onCopy = { copyAndNotify("vuminhngocpt@gmail.com") }
            )
        }

        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            SectionKicker("LỜI NHẮN TỪ NHÀ PHÁT TRIỂN")
            Text(
                text = "Để thuận tiện hơn hành ngày mình tạo ra ứng dụng này để các bạn tính toán dễ dàng hơn , mình không thu phí cũng như 1 đồng nào từ ứng dụng này chỉ phát triển vì cộng đồng , nếu muốn thêm tính năng nào hoặc ứng dụng bị lỗi hãy gửi mail cho mình nhé",
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary
            )
            AccentGradientButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:vuminhngocpt@gmail.com"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✉️ Gửi mail cho Vũ Minh Ngọc", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.84f))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = palette.textSecondary)
        Text(text = value, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
    }
}

@Composable
private fun AboutLinkRow(
    label: String,
    linkText: String,
    onOpenLink: () -> Unit,
    onCopy: () -> Unit
) {
    val palette = LocalGlassPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.84f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = palette.textSecondary)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = linkText,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.accentFrom,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onOpenLink)
            )
            GlassButton(onClick = onCopy, minHeight = 30.dp) {
                Text("Sao chép", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
            }
        }
    }
}

// 8. TAB CÀI ĐẶT RIÊNG
@Composable
fun SettingsTab(
    state: CalculatorAppState,
    onThemeChange: (AppThemeMode) -> Unit,
    onFontChange: (AppFontStyle) -> Unit,
    onFontScaleChange: (Int) -> Unit,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit
) {
    val palette = LocalGlassPalette.current
    var themeMenuOpen by remember { mutableStateOf(false) }
    var fontMenuOpen by remember { mutableStateOf(false) }
    var sliderVal by remember(state.fontScale) { mutableFloatStateOf(state.fontScale.toFloat()) }

    LaunchedEffect(state.fontScale) {
        sliderVal = state.fontScale.toFloat()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            SectionKicker("GIAO DIỆN & ICON CON VẬT")
            Text(
                text = "Chọn phong cách giao diện",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textSecondary
            )
            Box {
                Surface(
                    onClick = { themeMenuOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5994A3B8))
                ) {
                    Text(
                        text = state.theme.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                }
                DropdownMenu(
                    expanded = themeMenuOpen,
                    onDismissRequest = { themeMenuOpen = false }
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.displayName, fontSize = 13.5.sp) },
                            onClick = {
                                onThemeChange(mode)
                                themeMenuOpen = false
                            }
                        )
                    }
                }
            }
        }

        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            SectionKicker("PHÔNG CHỮ & CỠ CHỮ")
            Text(
                text = "Chọn phông chữ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textSecondary
            )
            Box {
                Surface(
                    onClick = { fontMenuOpen = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5994A3B8))
                ) {
                    Text(
                        text = state.font.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary
                    )
                }
                DropdownMenu(
                    expanded = fontMenuOpen,
                    onDismissRequest = { fontMenuOpen = false }
                ) {
                    AppFontStyle.entries.forEach { fontOption ->
                        DropdownMenuItem(
                            text = { Text(fontOption.displayName, fontSize = 13.5.sp) },
                            onClick = {
                                onFontChange(fontOption)
                                fontMenuOpen = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tỷ lệ cỡ chữ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary
                )
                Text(
                    text = "${sliderVal.roundToInt()}%",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary
                )
            }
            Slider(
                value = sliderVal,
                onValueChange = { raw ->
                    val stepped = ((raw / 5f).roundToInt() * 5).coerceIn(85, 125)
                    sliderVal = stepped.toFloat()
                    onFontScaleChange(stepped)
                },
                valueRange = 85f..125f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = palette.accentFrom,
                    activeTrackColor = palette.accentFrom
                )
            )
        }

        GlassSubCard(modifier = Modifier.fillMaxWidth()) {
            SectionKicker("ĐỒNG BỘ DỮ LIỆU (BỘ NHỚ MÁY / THẺ NHỚ)")
            Text(
                text = "Lưu tệp đồng bộ .json vào bộ nhớ máy hoặc thẻ nhớ, hoặc nạp lại tệp đã lưu để khôi phục lịch sử và cấu hình.",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccentGradientButton(
                    onClick = onExportJson,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💾 Lưu tệp .json", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                GlassButton(
                    onClick = onImportJson,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📂 Nạp tệp .json", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                }
            }
        }
    }
}
