package com.vmnstudios95.calculator.model

enum class AppTab(val id: String, val titleVi: String, val titleEn: String) {
    BASIC("basic", "Cơ bản", "Basic"),
    HOURLY("hourly", "Theo giờ", "Hourly"),
    WEIGHT("weight", "Khối lượng", "Weight"),
    ELECTRIC("electric", "Tiền điện", "Electric"),
    NOTES("notes", "Ghi chú", "Notes"),
    ATTENDANCE("attendance", "Chấm công", "Attendance"),
    HISTORY("history", "Lịch sử", "History")
}

enum class AppLanguage(val code: String, val label: String) {
    VI("vi", "Tiếng Việt"),
    EN("en", "English"),
    ZH("zh", "中文"),
    JA("ja", "日本語"),
    KO("ko", "한국어")
}

enum class CurrencyUnit(val code: String, val symbol: String, val decimals: Int) {
    VND("VND", "₫", 0),
    USD("USD", "$", 2),
    EUR("EUR", "€", 2),
    JPY("JPY", "¥", 0),
    KRW("KRW", "₩", 0),
    CNY("CNY", "¥", 2)
}

enum class GlassPreset(val id: String, val label: String, val primaryHex: Long, val secondaryHex: Long) {
    EMERALD_GLASS("emerald", "Emerald Glass", 0xFF10B981, 0xFF06B6D4),
    OCEAN_GLASS("ocean", "Ocean Glass", 0xFF0EA5E9, 0xFF6366F1),
    AMBER_GLASS("amber", "Amber Glass", 0xFFF59E0B, 0xFFEF4444),
    VIOLET_GLASS("violet", "Violet Glass", 0xFF8B5CF6, 0xFFEC4899),
    ROSE_GLASS("rose", "Rose Glass", 0xFFF43F5E, 0xFFF97316)
}

enum class WeightUnit(val id: String, val labelVi: String, val gramsPerUnit: Double) {
    GRAM("g", "Gram (g)", 1.0),
    LANG("lang", "Lạng (100g)", 100.0),
    KG("kg", "Kilôgam (kg)", 1000.0),
    YEN("yen", "Yến (10kg)", 10000.0),
    TA("ta", "Tạ (100kg)", 100000.0),
    TAN("tan", "Tấn (1000kg)", 1000000.0)
}

enum class AttendanceStatus(val id: String, val labelVi: String, val workFactor: Double) {
    FULL_DAY("full", "Đủ công", 1.0),
    OVERTIME("ot", "Tăng ca", 1.0),
    HALF_DAY("half", "Nửa công", 0.5),
    PAID_LEAVE("paid_leave", "Nghỉ phép", 1.0),
    UNPAID_LEAVE("unpaid_leave", "Nghỉ không lương", 0.0)
}

data class ChecklistItem(
    val id: String,
    val text: String,
    val done: Boolean = false
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val colorKey: String = "emerald",
    val pinned: Boolean = false,
    val checklist: List<ChecklistItem> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class StaffMember(
    val id: String,
    val name: String,
    val role: String,
    val hourlyRate: Double,
    val standardHoursPerDay: Double = 8.0
)

data class AttendanceRecord(
    val id: String,
    val staffId: String,
    val dateIso: String,
    val status: AttendanceStatus = AttendanceStatus.FULL_DAY,
    val hoursWorked: Double = 8.0,
    val overtimeHours: Double = 0.0,
    val overtimeMultiplier: Double = 1.5,
    val bonusOrDeduction: Double = 0.0,
    val note: String = ""
) {
    fun calculatePay(hourlyRate: Double): Double {
        val basePay = when (status) {
            AttendanceStatus.FULL_DAY, AttendanceStatus.PAID_LEAVE -> hoursWorked * hourlyRate
            AttendanceStatus.HALF_DAY -> hoursWorked * hourlyRate
            AttendanceStatus.OVERTIME -> hoursWorked * hourlyRate
            AttendanceStatus.UNPAID_LEAVE -> 0.0
        }
        val otPay = overtimeHours * hourlyRate * overtimeMultiplier
        return (basePay + otPay + bonusOrDeduction).coerceAtLeast(0.0)
    }
}

data class CalculationHistoryEntry(
    val id: String,
    val category: AppTab,
    val title: String,
    val detail: String,
    val resultText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppPreferencesState(
    val language: AppLanguage = AppLanguage.VI,
    val currency: CurrencyUnit = CurrencyUnit.VND,
    val glassPreset: GlassPreset = GlassPreset.EMERALD_GLASS,
    val darkMode: Boolean = true,
    val glassIntensity: Float = 0.72f,
    val enabledTabs: Set<AppTab> = AppTab.entries.toSet()
)
