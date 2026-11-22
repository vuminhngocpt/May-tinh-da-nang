package com.vmnstudios95.maytinhdanang.model

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppTab(val key: String, val petIcon: String, val label: String) {
    CALC("calc", "🐶", "Máy tính"),
    WEIGHT("weight", "🐻", "Cân nặng"),
    HOURS("hours", "🦊", "Giờ làm"),
    LITER("liter", "🐸", "Số lít"),
    CUSTOM("custom", "🐼", "Sửa nâng cao"),
    HISTORY("history", "🦉", "Lịch sử"),
    ABOUT("about", "🐨", "Giới thiệu"),
    SETTINGS("settings", "⚙️", "Cài đặt")
}

enum class AppThemeMode(val key: String, val displayName: String) {
    CUTE("cute", "Dễ thương (icon con vật 🐱🐶🐻🦊🐸🐼🦉🐨)"),
    NORMAL("normal", "Giao diện bình thường")
}

enum class AppFontStyle(val key: String, val displayName: String) {
    ROUNDED("rounded", "Bo tròn dễ thương"),
    CLEAN("clean", "Gọn hiện đại"),
    SYSTEM("system", "Mặc định hệ thống"),
    MONO("mono", "Chữ đều (Mono)")
}

data class WeightConfig(
    val defaultPrice: Double = 50000.0,
    val langToKg: Double = 0.1
)

data class HoursConfig(
    val defaultRate: Double = 30000.0,
    val defaultFactor: Double = 1.0
)

data class LiterConfig(
    val defaultPrice: Double = 22000.0,
    val canToLiter: Double = 20.0
)

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val detail: String,
    val time: String = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale("vi", "VN")).format(Date())
)

data class CalculatorAppState(
    val theme: AppThemeMode = AppThemeMode.CUTE,
    val font: AppFontStyle = AppFontStyle.ROUNDED,
    val fontScale: Int = 100,
    val appName: String = "VMN Máy Tính Đa Năng",
    val appDesc: String = "Tự động lưu lịch sử • Đồng bộ tệp • Tùy biến linh hoạt",
    val weight: WeightConfig = WeightConfig(),
    val hours: HoursConfig = HoursConfig(),
    val liter: LiterConfig = LiterConfig(),
    val history: List<HistoryItem> = emptyList()
) {
    fun toJson(wrapPayload: Boolean = false): String {
        val stateObj = JSONObject().apply {
            put("theme", theme.key)
            put("font", font.key)
            put("fontScale", fontScale)
            put("appName", appName)
            put("appDesc", appDesc)
            put("weight", JSONObject().apply {
                put("defaultPrice", weight.defaultPrice)
                put("langToKg", weight.langToKg)
            })
            put("hours", JSONObject().apply {
                put("defaultRate", hours.defaultRate)
                put("defaultFactor", hours.defaultFactor)
            })
            put("liter", JSONObject().apply {
                put("defaultPrice", liter.defaultPrice)
                put("canToLiter", liter.canToLiter)
            })
            val histArr = JSONArray()
            history.take(250).forEach { item ->
                histArr.put(JSONObject().apply {
                    put("id", item.id)
                    put("category", item.category)
                    put("detail", item.detail)
                    put("time", item.time)
                })
            }
            put("history", histArr)
        }

        return if (wrapPayload) {
            val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
            JSONObject().apply {
                put("app", "VMN Máy Tính Đa Năng")
                put("version", "2.0")
                put("exportedAt", isoTime)
                put("data", stateObj)
            }.toString(2)
        } else {
            stateObj.toString()
        }
    }

    companion object {
        fun fromJson(rawJson: String, fallback: CalculatorAppState = CalculatorAppState()): CalculatorAppState {
            return try {
                val root = JSONObject(rawJson)
                val obj = if (root.has("data") && root.optJSONObject("data") != null) {
                    root.getJSONObject("data")
                } else {
                    root
                }

                val themeMode = if (obj.optString("theme") == "normal") AppThemeMode.NORMAL else AppThemeMode.CUTE
                val fontKey = obj.optString("font", "rounded")
                val fontStyle = AppFontStyle.entries.find { it.key == fontKey } ?: AppFontStyle.ROUNDED
                val scale = obj.optInt("fontScale", 100).coerceIn(85, 125)
                val name = obj.optString("appName", fallback.appName).trim().ifEmpty { fallback.appName }
                val desc = obj.optString("appDesc", fallback.appDesc).trim().ifEmpty { fallback.appDesc }

                val wObj = obj.optJSONObject("weight")
                val wPrice = wObj?.optDouble("defaultPrice", fallback.weight.defaultPrice)
                    ?.takeIf { it.isFinite() && it >= 0 } ?: fallback.weight.defaultPrice
                val langKg = wObj?.optDouble("langToKg", fallback.weight.langToKg)
                    ?.takeIf { it.isFinite() && it > 0 } ?: fallback.weight.langToKg

                val hObj = obj.optJSONObject("hours")
                val hRate = hObj?.optDouble("defaultRate", fallback.hours.defaultRate)
                    ?.takeIf { it.isFinite() && it >= 0 } ?: fallback.hours.defaultRate
                val hFactor = hObj?.optDouble("defaultFactor", fallback.hours.defaultFactor)
                    ?.takeIf { it.isFinite() && it > 0 } ?: fallback.hours.defaultFactor

                val lObj = obj.optJSONObject("liter")
                val lPrice = lObj?.optDouble("defaultPrice", fallback.liter.defaultPrice)
                    ?.takeIf { it.isFinite() && it >= 0 } ?: fallback.liter.defaultPrice
                val canLiter = lObj?.optDouble("canToLiter", fallback.liter.canToLiter)
                    ?.takeIf { it.isFinite() && it > 0 } ?: fallback.liter.canToLiter

                val histArr = obj.optJSONArray("history")
                val parsedHistory = if (histArr != null) {
                    buildList {
                        val limit = minOf(histArr.length(), 250)
                        for (i in 0 until limit) {
                            val itemObj = histArr.optJSONObject(i) ?: continue
                            add(
                                HistoryItem(
                                    id = itemObj.optString("id").ifEmpty { UUID.randomUUID().toString() },
                                    category = itemObj.optString("category", "Tính toán"),
                                    detail = itemObj.optString("detail", ""),
                                    time = itemObj.optString("time", "")
                                )
                            )
                        }
                    }
                } else {
                    fallback.history
                }

                CalculatorAppState(
                    theme = themeMode,
                    font = fontStyle,
                    fontScale = scale,
                    appName = name,
                    appDesc = desc,
                    weight = WeightConfig(defaultPrice = wPrice, langToKg = langKg),
                    hours = HoursConfig(defaultRate = hRate, defaultFactor = hFactor),
                    liter = LiterConfig(defaultPrice = lPrice, canToLiter = canLiter),
                    history = parsedHistory
                )
            } catch (_: Exception) {
                fallback
            }
        }
    }
}
