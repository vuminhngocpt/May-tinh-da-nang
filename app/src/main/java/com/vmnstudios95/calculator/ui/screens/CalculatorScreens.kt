package com.vmnstudios95.calculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vmnstudios95.calculator.model.*
import com.vmnstudios95.calculator.ui.theme.liquidGlassCard
import com.vmnstudios95.calculator.util.CalculationEngine
import com.vmnstudios95.calculator.viewmodel.CalculatorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun BasicCalculatorScreen(
    vm: CalculatorViewModel,
    prefs: AppPreferencesState,
    isCompactWatch: Boolean,
    modifier: Modifier = Modifier
) {
    val expr by vm.expression.collectAsState()
    val preview by vm.previewText.collectAsState()
    val lastResult by vm.lastResultText.collectAsState()
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(if (isCompactWatch) 4.dp else 10.dp),
        verticalArrangement = Arrangement.spacedBy(if (isCompactWatch) 4.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isCompactWatch) 0.32f else 0.28f)
                .liquidGlassCard(prefs, cornerRadius = if (isCompactWatch) 12.dp else 22.dp, highlightAccent = true)
                .padding(horizontal = if (isCompactWatch) 8.dp else 16.dp, vertical = if (isCompactWatch) 6.dp else 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCompactWatch) "VMN" else "VMN CALC • ${prefs.currency.code}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isCompactWatch) {
                    IconButton(
                        onClick = {
                            val textToCopy = expr.ifBlank { lastResult }
                            clipboard.setText(AnnotatedString(textToCopy))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expr.ifBlank { lastResult },
                    fontSize = if (isCompactWatch) 20.sp else 32.sp,
                    lineHeight = if (isCompactWatch) 24.sp else 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (preview.isNotBlank() && !isCompactWatch) {
                    Text(
                        text = preview,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        val rows = listOf(
            listOf("AC", "()", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "DEL", "=")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isCompactWatch) 0.68f else 0.72f),
            verticalArrangement = Arrangement.spacedBy(if (isCompactWatch) 3.dp else 7.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompactWatch) 3.dp else 7.dp)
                ) {
                    row.forEach { key ->
                        val isEquals = key == "="
                        val isOp = key in listOf("÷", "×", "-", "+", "%", "()")
                        val isClear = key == "AC"

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .liquidGlassCard(
                                    prefs = prefs,
                                    cornerRadius = if (isCompactWatch) 10.dp else 16.dp,
                                    highlightAccent = isEquals
                                )
                                .background(
                                    when {
                                        isEquals -> MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                                        isClear -> Color(0xFFEF4444).copy(alpha = 0.22f)
                                        isOp -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    when (key) {
                                        "AC" -> vm.clearExpression()
                                        "DEL" -> vm.backspaceExpression()
                                        "()" -> vm.appendParenthesis()
                                        "=" -> vm.evaluateBasic()
                                        else -> vm.appendToken(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "DEL") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(if (isCompactWatch) 15.dp else 20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = if (isCompactWatch) 13.sp else 20.sp,
                                    fontWeight = if (isEquals || isOp || isClear) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isEquals) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyBillingScreen(
    vm: CalculatorViewModel,
    prefs: AppPreferencesState,
    modifier: Modifier = Modifier
) {
    var rateText by remember { mutableStateOf("50000") }
    var hoursText by remember { mutableStateOf("2") }
    var minutesText by remember { mutableStateOf("30") }
    var multiplier by remember { mutableStateOf(1.0) }
    var deviceClockText by remember { mutableStateOf(vm.currentDeviceDateTimeFormatted()) }

    val running by vm.stopwatchRunning.collectAsState()
    val elapsedMillis by vm.stopwatchMillis.collectAsState()

    val rate = rateText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val stopwatchHours = elapsedMillis / 3_600_000.0
    val manualHours = ((hoursText.toDoubleOrNull() ?: 0.0) + ((minutesText.toDoubleOrNull() ?: 0.0) / 60.0)).coerceAtLeast(0.0)

    val activeHours = if (elapsedMillis > 0L || running) stopwatchHours else manualHours
    val totalAmount = CalculationEngine.cleanDouble(activeHours * rate * multiplier)

    val secTotal = elapsedMillis / 1000
    val hh = secTotal / 3600
    val mm = (secTotal % 3600) / 60
    val ss = secTotal % 60
    val cs = (elapsedMillis % 1000) / 10
    val stopwatchFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d.%02d", hh, mm, ss, cs)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ngày giờ thiết bị", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(deviceClockText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            FilledTonalButton(
                onClick = { deviceClockText = vm.currentDeviceDateTimeFormatted() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Bây giờ", fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs, highlightAccent = true)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Bấm giờ tính tiền trực tiếp", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = stopwatchFormatted,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.toggleStopwatch() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (running) "Tạm dừng" else "Bắt đầu")
                }
                OutlinedButton(
                    onClick = { vm.resetStopwatch() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Đặt lại")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = rateText,
                onValueChange = { rateText = it },
                label = { Text("Đơn giá / giờ (${prefs.currency.symbol})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = {
                        hoursText = it
                        vm.resetStopwatch()
                    },
                    label = { Text("Số giờ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = {
                        minutesText = it
                        vm.resetStopwatch()
                    },
                    label = { Text("Số phút") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1.0, 1.5, 2.0, 3.0).forEach { mult ->
                    FilterChip(
                        selected = multiplier == mult,
                        onClick = { multiplier = mult },
                        label = { Text("Hệ số ${mult}x") }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs, highlightAccent = true)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Tổng thời gian: ${CalculationEngine.formatNumber(activeHours, 3)} giờ • Hệ số ${multiplier}x", fontSize = 13.sp)
            Text(
                text = CalculationEngine.formatCurrency(totalAmount, prefs.currency),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = {
                    vm.addHistory(
                        category = AppTab.HOURLY,
                        title = "${CalculationEngine.formatNumber(activeHours, 3)} giờ × ${CalculationEngine.formatCurrency(rate, prefs.currency)}",
                        detail = "Hệ số ${multiplier}x • $deviceClockText",
                        resultText = CalculationEngine.formatCurrency(totalAmount, prefs.currency)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Lưu vào lịch sử")
            }
        }
    }
}

@Composable
fun WeightBillingScreen(
    vm: CalculatorViewModel,
    prefs: AppPreferencesState,
    modifier: Modifier = Modifier
) {
    var quantityText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(WeightUnit.KG) }
    var unitPriceText by remember { mutableStateOf("25000") }
    var priceUnit by remember { mutableStateOf(WeightUnit.KG) }

    val qty = quantityText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val unitPrice = unitPriceText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

    val totalGrams = qty * selectedUnit.gramsPerUnit
    val pricePerGram = unitPrice / priceUnit.gramsPerUnit
    val totalCost = CalculationEngine.cleanDouble(totalGrams * pricePerGram)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Tính tiền & quy đổi khối lượng", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Khối lượng") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it },
                    label = { Text("Đơn giá (${prefs.currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Đơn vị khối lượng nhập:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WeightUnit.entries.forEach { u ->
                    FilterChip(
                        selected = selectedUnit == u,
                        onClick = { selectedUnit = u },
                        label = { Text(u.labelVi) }
                    )
                }
            }

            Text("Đơn giá tính theo:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WeightUnit.entries.forEach { u ->
                    FilterChip(
                        selected = priceUnit == u,
                        onClick = { priceUnit = u },
                        label = { Text("/ ${u.id}") }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Quy đổi đồng thời các đơn vị", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            WeightUnit.entries.forEach { unit ->
                val converted = totalGrams / unit.gramsPerUnit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(unit.labelVi, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Text(CalculationEngine.formatNumber(converted, 4), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs, highlightAccent = true)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Thành tiền (${CalculationEngine.formatNumber(qty, 3)} ${selectedUnit.id} @ ${prefs.currency.symbol}/${priceUnit.id})", fontSize = 13.sp)
            Text(
                text = CalculationEngine.formatCurrency(totalCost, prefs.currency),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = {
                    vm.addHistory(
                        category = AppTab.WEIGHT,
                        title = "${CalculationEngine.formatNumber(qty, 3)} ${selectedUnit.id}",
                        detail = "Đơn giá ${CalculationEngine.formatCurrency(unitPrice, prefs.currency)} / ${priceUnit.id}",
                        resultText = CalculationEngine.formatCurrency(totalCost, prefs.currency)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Lưu vào lịch sử")
            }
        }
    }
}

@Composable
fun ElectricBillingScreen(
    vm: CalculatorViewModel,
    prefs: AppPreferencesState,
    modifier: Modifier = Modifier
) {
    var modeByMeter by remember { mutableStateOf(true) }
    var oldMeterText by remember { mutableStateOf("1200") }
    var newMeterText by remember { mutableStateOf("1345") }

    var wattText by remember { mutableStateOf("1500") }
    var hoursPerDayText by remember { mutableStateOf("4") }
    var daysText by remember { mutableStateOf("30") }

    var useEvnTiered by remember { mutableStateOf(true) }
    var includeVat by remember { mutableStateOf(true) }
    var flatRateText by remember { mutableStateOf("2500") }

    val kwh = if (modeByMeter) {
        val oldM = oldMeterText.toDoubleOrNull() ?: 0.0
        val newM = newMeterText.toDoubleOrNull() ?: 0.0
        (newM - oldM).coerceAtLeast(0.0)
    } else {
        val w = wattText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val h = hoursPerDayText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val d = daysText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        CalculationEngine.cleanDouble((w * h * d) / 1000.0)
    }

    val (totalBill, breakdowns) = if (useEvnTiered) {
        CalculationEngine.calculateEvnTieredElectricity(kwh, includeVat)
    } else {
        val flat = flatRateText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        val base = kwh * flat
        val finalVal = if (includeVat) CalculationEngine.cleanDouble(base * 1.08) else CalculationEngine.cleanDouble(base)
        finalVal to emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = modeByMeter,
                onClick = { modeByMeter = true },
                label = { Text("Chỉ số công tơ") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !modeByMeter,
                onClick = { modeByMeter = false },
                label = { Text("Công suất (Watt)") },
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (modeByMeter) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oldMeterText,
                        onValueChange = { oldMeterText = it },
                        label = { Text("Chỉ số cũ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newMeterText,
                        onValueChange = { newMeterText = it },
                        label = { Text("Chỉ số mới") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                OutlinedTextField(
                    value = wattText,
                    onValueChange = { wattText = it },
                    label = { Text("Công suất thiết bị (W)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hoursPerDayText,
                        onValueChange = { hoursPerDayText = it },
                        label = { Text("Giờ / ngày") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = daysText,
                        onValueChange = { daysText = it },
                        label = { Text("Số ngày") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Biểu giá bậc thang EVN", fontSize = 14.sp)
                Switch(checked = useEvnTiered, onCheckedChange = { useEvnTiered = it })
            }

            if (!useEvnTiered) {
                OutlinedTextField(
                    value = flatRateText,
                    onValueChange = { flatRateText = it },
                    label = { Text("Đơn giá / kWh (${prefs.currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Thuế VAT 8%", fontSize = 14.sp)
                Switch(checked = includeVat, onCheckedChange = { includeVat = it })
            }
        }

        if (useEvnTiered && breakdowns.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(prefs)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Chi tiết bậc thang EVN", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                breakdowns.forEach { b ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${b.tierName} (${CalculationEngine.formatNumber(b.kwhUsed, 1)} kWh)", fontSize = 12.sp)
                        Text(CalculationEngine.formatCurrency(b.cost, prefs.currency), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs, highlightAccent = true)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Điện năng tiêu thụ: ${CalculationEngine.formatNumber(kwh, 2)} kWh", fontSize = 13.sp)
            Text(
                text = CalculationEngine.formatCurrency(totalBill, prefs.currency),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = {
                    vm.addHistory(
                        category = AppTab.ELECTRIC,
                        title = "${CalculationEngine.formatNumber(kwh, 2)} kWh",
                        detail = if (useEvnTiered) "Bậc thang EVN${if (includeVat) " + VAT 8%" else ""}" else "Giá cố định${if (includeVat) " + VAT 8%" else ""}",
                        resultText = CalculationEngine.formatCurrency(totalBill, prefs.currency)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Lưu vào lịch sử")
            }
        }
    }
}

@Composable
fun NotesScreen(
    vm: CalculatorViewModel,
    prefs: AppPreferencesState,
    modifier: Modifier = Modifier
) {
    val notes by vm.notes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<NoteItem?>(null) }
    var showEditorDialog by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, searchQuery) {
        notes
            .filter { n ->
                searchQuery.isBlank() ||
                    n.title.contains(searchQuery, ignoreCase = true) ||
                    n.content.contains(searchQuery, ignoreCase = true) ||
                    n.checklist.any { it.text.contains(searchQuery, ignoreCase = true) }
            }
            .sortedWith(compareByDescending<NoteItem> { it.pinned }.thenByDescending { it.updatedAt })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm ghi chú...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            FloatingActionButton(
                onClick = {
                    editingNote = null
                    showEditorDialog = true
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo ghi chú")
            }
        }

        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquidGlassCard(prefs),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có ghi chú nào. Nhấn + để tạo mới.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(prefs, highlightAccent = note.pinned)
                            .clickable {
                                editingNote = note
                                showEditorDialog = true
                            }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title.ifBlank { "Ghi chú không tiêu đề" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row {
                                IconButton(
                                    onClick = { vm.toggleNotePin(note.id) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Ghim",
                                        tint = if (note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { vm.deleteNote(note.id) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Xóa",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        if (note.content.isNotBlank()) {
                            Text(
                                text = note.content,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (note.checklist.isNotEmpty()) {
                            val doneCount = note.checklist.count { it.done }
                            Text(
                                text = "Checklist: $doneCount/${note.checklist.size} hoàn tất",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            note.checklist.forEach { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { vm.toggleChecklistItem(note.id, item.id) }
                                ) {
                                    Checkbox(
                                        checked = item.done,
                                        onCheckedChange = { vm.toggleChecklistItem(note.id, item.id) },
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = item.text,
                                        fontSize = 13.sp,
                                        textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (item.done) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditorDialog) {
        NoteEditorDialog(
            initialNote = editingNote,
            onDismiss = { showEditorDialog = false },
            onSave = { saved ->
                vm.upsertNote(saved)
                showEditorDialog = false
            }
        )
    }
}

@Composable
private fun NoteEditorDialog(
    initialNote: NoteItem?,
    onDismiss: () -> Unit,
    onSave: (NoteItem) -> Unit
) {
    var title by remember(initialNote) { mutableStateOf(initialNote?.title ?: "") }
    var content by remember(initialNote) { mutableStateOf(initialNote?.content ?: "") }
    var pinned by remember(initialNote) { mutableStateOf(initialNote?.pinned ?: false) }
    var checklist by remember(initialNote) {
        mutableStateOf(initialNote?.checklist ?: emptyList())
    }
    var newTaskText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialNote == null) "Tạo ghi chú mới" else "Chỉnh sửa ghi chú") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Nội dung") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ghim lên đầu", fontSize = 13.sp)
                    Switch(checked = pinned, onCheckedChange = { pinned = it })
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("Thêm mục checklist...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                checklist = checklist + ChecklistItem(UUID.randomUUID().toString(), newTaskText.trim(), false)
                                newTaskText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = "Thêm mục")
                    }
                }

                checklist.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = item.done,
                                onCheckedChange = { checked ->
                                    checklist = checklist.map { if (it.id == item.id) it.copy(done = checked) else it }
                                }
                            )
                            Text(item.text, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(
                            onClick = { checklist = checklist.filterNot { it.id == item.id } },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa mục", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val item = NoteItem(
                        id = initialNote?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        content = content.trim(),
                        colorKey = initialNote?.colorKey ?: "emerald",
                        pinned = pinned,
                        checklist = checklist,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(item)
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
fun AttendanceScreen(
    vm: CalculatorViewModel,
    prefs: AppPreferencesState,
    modifier: Modifier = Modifier
) {
    val staffList by vm.staffList.collectAsState()
    val records by vm.attendanceRecords.collectAsState()

    var selectedStaffId by remember(staffList) {
        mutableStateOf(staffList.firstOrNull()?.id)
    }

    var editingStaff by remember { mutableStateOf<StaffMember?>(null) }
    var showStaffDialog by remember { mutableStateOf(false) }

    var editingRecord by remember { mutableStateOf<AttendanceRecord?>(null) }
    var showRecordDialog by remember { mutableStateOf(false) }

    val activeStaff = staffList.firstOrNull { it.id == selectedStaffId } ?: staffList.firstOrNull()
    val staffRecords = remember(records, activeStaff) {
        if (activeStaff == null) emptyList()
        else records.filter { it.staffId == activeStaff.id }.sortedByDescending { it.dateIso }
    }

    val totalHours = staffRecords.sumOf { it.hoursWorked + it.overtimeHours }
    val totalPay = if (activeStaff == null) 0.0 else staffRecords.sumOf { it.calculatePay(activeStaff.hourlyRate) }
    val totalFundAllStaff = remember(staffList, records) {
        staffList.sumOf { s ->
            records.filter { it.staffId == s.id }.sumOf { it.calculatePay(s.hourlyRate) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    editingStaff = null
                    showStaffDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Thêm nhân sự", maxLines = 1, softWrap = false)
            }
            Button(
                onClick = {
                    if (activeStaff != null) {
                        editingRecord = null
                        showRecordDialog = true
                    }
                },
                enabled = activeStaff != null,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Chấm công", maxLines = 1, softWrap = false)
            }
        }

        if (staffList.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                staffList.forEach { s ->
                    FilterChip(
                        selected = activeStaff?.id == s.id,
                        onClick = { selectedStaffId = s.id },
                        label = { Text(s.name, maxLines = 1, softWrap = false) }
                    )
                }
            }
        }

        if (activeStaff != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(prefs, highlightAccent = true)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(activeStaff.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${activeStaff.role.ifBlank { "Nhân sự" }} • ${CalculationEngine.formatCurrency(activeStaff.hourlyRate, prefs.currency)}/giờ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                    Row {
                        IconButton(onClick = {
                            editingStaff = activeStaff
                            showStaffDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa nhân sự")
                        }
                        if (staffList.size > 1) {
                            IconButton(onClick = { vm.deleteStaff(activeStaff.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa nhân sự", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng công: ${staffRecords.size} ngày • ${CalculationEngine.formatNumber(totalHours, 1)}h", fontSize = 13.sp)
                    Text(
                        text = CalculationEngine.formatCurrency(totalPay, prefs.currency),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassCard(prefs)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tổng quỹ lương toàn đội", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                CalculationEngine.formatCurrency(totalFundAllStaff, prefs.currency),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Text("Chi tiết chấm công (nhấn để sửa)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        if (staffRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassCard(prefs)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có bản ghi chấm công cho nhân sự này.", fontSize = 13.sp)
            }
        } else {
            staffRecords.forEach { rec ->
                val pay = activeStaff?.let { rec.calculatePay(it.hourlyRate) } ?: 0.0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassCard(prefs)
                        .clickable {
                            editingRecord = rec
                            showRecordDialog = true
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${rec.dateIso} • ${rec.status.labelVi}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "Làm: ${rec.hoursWorked}h + OT: ${rec.overtimeHours}h (${rec.overtimeMultiplier}x)" +
                                if (rec.note.isNotBlank()) " • ${rec.note}" else "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            CalculationEngine.formatCurrency(pay, prefs.currency),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { vm.deleteAttendanceRecord(rec.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showStaffDialog) {
        StaffEditorDialog(
            initialStaff = editingStaff,
            onDismiss = { showStaffDialog = false },
            onSave = { staff ->
                vm.upsertStaff(staff)
                selectedStaffId = staff.id
                showStaffDialog = false
            }
        )
    }

    if (showRecordDialog && activeStaff != null) {
        AttendanceRecordDialog(
            staff = activeStaff,
            initialRecord = editingRecord,
            defaultDateIso = vm.todayIso(),
            onDismiss = { showRecordDialog = false },
            onSave = { rec ->
                vm.upsertAttendanceRecord(rec)
                showRecordDialog = false
            }
        )
    }
}

@Composable
private fun StaffEditorDialog(
    initialStaff: StaffMember?,
    onDismiss: () -> Unit,
    onSave: (StaffMember) -> Unit
) {
    var name by remember(initialStaff) { mutableStateOf(initialStaff?.name ?: "") }
    var role by remember(initialStaff) { mutableStateOf(initialStaff?.role ?: "") }
    var hourlyRateText by remember(initialStaff) { mutableStateOf((initialStaff?.hourlyRate ?: 35000.0).toString()) }
    var stdHoursText by remember(initialStaff) { mutableStateOf((initialStaff?.standardHoursPerDay ?: 8.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialStaff == null) "Thêm nhân sự" else "Sửa thông tin nhân sự") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Chức vụ / Bộ phận") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hourlyRateText,
                    onValueChange = { hourlyRateText = it },
                    label = { Text("Lương theo giờ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = stdHoursText,
                    onValueChange = { stdHoursText = it },
                    label = { Text("Giờ chuẩn / ngày") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim().ifBlank { "Nhân sự mới" }
                    val rate = hourlyRateText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 35000.0
                    val stdH = stdHoursText.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 8.0
                    onSave(
                        StaffMember(
                            id = initialStaff?.id ?: UUID.randomUUID().toString(),
                            name = cleanName,
                            role = role.trim(),
                            hourlyRate = rate,
                            standardHoursPerDay = stdH
                        )
                    )
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
private fun AttendanceRecordDialog(
    staff: StaffMember,
    initialRecord: AttendanceRecord?,
    defaultDateIso: String,
    onDismiss: () -> Unit,
    onSave: (AttendanceRecord) -> Unit
) {
    var dateIso by remember(initialRecord) { mutableStateOf(initialRecord?.dateIso ?: defaultDateIso) }
    var status by remember(initialRecord) { mutableStateOf(initialRecord?.status ?: AttendanceStatus.FULL_DAY) }
    var hoursText by remember(initialRecord) { mutableStateOf((initialRecord?.hoursWorked ?: staff.standardHoursPerDay).toString()) }
    var otHoursText by remember(initialRecord) { mutableStateOf((initialRecord?.overtimeHours ?: 0.0).toString()) }
    var otMultText by remember(initialRecord) { mutableStateOf((initialRecord?.overtimeMultiplier ?: 1.5).toString()) }
    var bonusText by remember(initialRecord) { mutableStateOf((initialRecord?.bonusOrDeduction ?: 0.0).toString()) }
    var note by remember(initialRecord) { mutableStateOf(initialRecord?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialRecord == null) "Chấm công • ${staff.name}" else "Sửa chấm công • ${staff.name}") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dateIso,
                    onValueChange = { dateIso = it },
                    label = { Text("Ngày (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AttendanceStatus.entries.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = {
                                status = st
                                if (st == AttendanceStatus.HALF_DAY) {
                                    hoursText = (staff.standardHoursPerDay / 2.0).toString()
                                } else if (st == AttendanceStatus.FULL_DAY || st == AttendanceStatus.PAID_LEAVE) {
                                    hoursText = staff.standardHoursPerDay.toString()
                                } else if (st == AttendanceStatus.UNPAID_LEAVE) {
                                    hoursText = "0.0"
                                }
                            },
                            label = { Text(st.labelVi) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it },
                        label = { Text("Giờ làm") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = otHoursText,
                        onValueChange = { otHoursText = it },
                        label = { Text("Giờ OT") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = otMultText,
                        onValueChange = { otMultText = it },
                        label = { Text("Hệ số OT") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bonusText,
                        onValueChange = { bonusText = it },
                        label = { Text("Thưởng/Phạt") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AttendanceRecord(
                            id = initialRecord?.id ?: UUID.randomUUID().toString(),
                            staffId = staff.id,
                            dateIso = dateIso.trim().ifBlank { defaultDateIso },
                            status = status,
                            hoursWorked = hoursText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                            overtimeHours = otHoursText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0,
                            overtimeMultiplier = otMultText.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.5,
                            bonusOrDeduction = bonusText.toDoubleOrNull() ?: 0.0,
                            note = note.trim()
                        )
                    )
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
fun HistoryScreen(
    vm: CalculatorViewModel,
    prefs: AppPreferencesState,
    modifier: Modifier = Modifier
) {
    val history by vm.history.collectAsState()
    var filterTab by remember { mutableStateOf<AppTab?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupText by remember { mutableStateOf("") }
    var backupStatus by remember { mutableStateOf<String?>(null) }

    val displayed = remember(history, filterTab) {
        if (filterTab == null) history else history.filter { it.category == filterTab }
    }

    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    backupText = vm.exportBackupJson()
                    backupStatus = null
                    showBackupDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sao lưu / Khôi phục", maxLines = 1, softWrap = false, fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { vm.clearHistory(filterTab) },
                enabled = displayed.isNotEmpty(),
                modifier = Modifier.weight(0.75f)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Xóa lịch sử", maxLines = 1, softWrap = false, fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = filterTab == null,
                onClick = { filterTab = null },
                label = { Text("Tất cả") }
            )
            listOf(AppTab.BASIC, AppTab.HOURLY, AppTab.WEIGHT, AppTab.ELECTRIC).forEach { tab ->
                FilterChip(
                    selected = filterTab == tab,
                    onClick = { filterTab = tab },
                    label = { Text(tab.titleVi) }
                )
            }
        }

        if (displayed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquidGlassCard(prefs),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có dữ liệu lịch sử.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayed, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassCard(prefs)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.category.titleVi} • ${fmt.format(Date(item.timestamp))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.detail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(item.resultText, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            IconButton(
                                onClick = { vm.deleteHistoryItem(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa mục", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Sao lưu & Khôi phục dữ liệu (JSON)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = backupText,
                        onValueChange = { backupText = it },
                        minLines = 6,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (backupStatus != null) {
                        Text(
                            text = backupStatus!!,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = vm.restoreFromBackupJson(backupText)
                        backupStatus = if (ok) "Đã khôi phục dữ liệu thành công!" else "JSON không hợp lệ."
                    }
                ) {
                    Text("Khôi phục")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) { Text("Đóng") }
            }
        )
    }
}
