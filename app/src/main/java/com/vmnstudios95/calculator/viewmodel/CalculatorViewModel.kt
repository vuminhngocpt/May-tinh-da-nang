package com.vmnstudios95.calculator.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vmnstudios95.calculator.model.*
import com.vmnstudios95.calculator.util.CalculationEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsStorage = application.getSharedPreferences("vmn_calc_prefs", Context.MODE_PRIVATE)

    private val _prefs = MutableStateFlow(AppPreferencesState())
    val prefs: StateFlow<AppPreferencesState> = _prefs.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.BASIC)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewText = MutableStateFlow("")
    val previewText: StateFlow<String> = _previewText.asStateFlow()

    private val _lastResultText = MutableStateFlow("0")
    val lastResultText: StateFlow<String> = _lastResultText.asStateFlow()

    private val _stopwatchRunning = MutableStateFlow(false)
    val stopwatchRunning: StateFlow<Boolean> = _stopwatchRunning.asStateFlow()

    private val _stopwatchMillis = MutableStateFlow(0L)
    val stopwatchMillis: StateFlow<Long> = _stopwatchMillis.asStateFlow()

    private var stopwatchJob: Job? = null
    private var stopwatchStartEpoch = 0L

    private val _notes = MutableStateFlow<List<NoteItem>>(emptyList())
    val notes: StateFlow<List<NoteItem>> = _notes.asStateFlow()

    private val _staffList = MutableStateFlow<List<StaffMember>>(emptyList())
    val staffList: StateFlow<List<StaffMember>> = _staffList.asStateFlow()

    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()

    private val _history = MutableStateFlow<List<CalculationHistoryEntry>>(emptyList())
    val history: StateFlow<List<CalculationHistoryEntry>> = _history.asStateFlow()

    init {
        loadFromStorage()
        seedDefaultDataIfEmpty()
    }

    fun openSettings(open: Boolean) {
        _showSettings.value = open
    }

    fun selectTab(tab: AppTab) {
        if (tab in _prefs.value.enabledTabs) {
            _currentTab.value = tab
        }
    }

    fun toggleTabEnabled(tab: AppTab) {
        _prefs.update { current ->
            val updatedSet = current.enabledTabs.toMutableSet()
            if (tab in updatedSet) {
                if (updatedSet.size > 1) updatedSet.remove(tab)
            } else {
                updatedSet.add(tab)
            }
            if (_currentTab.value !in updatedSet) {
                _currentTab.value = updatedSet.first()
            }
            current.copy(enabledTabs = updatedSet)
        }
        savePreferences()
    }

    fun setLanguage(language: AppLanguage) {
        _prefs.update { it.copy(language = language) }
        savePreferences()
    }

    fun setCurrency(currency: CurrencyUnit) {
        _prefs.update { it.copy(currency = currency) }
        savePreferences()
    }

    fun setGlassPreset(preset: GlassPreset) {
        _prefs.update { it.copy(glassPreset = preset) }
        savePreferences()
    }

    fun setDarkMode(dark: Boolean) {
        _prefs.update { it.copy(darkMode = dark) }
        savePreferences()
    }

    fun setGlassIntensity(intensity: Float) {
        _prefs.update { it.copy(glassIntensity = intensity.coerceIn(0.25f, 0.95f)) }
        savePreferences()
    }

    fun appendToken(token: String) {
        val updated = _expression.value + token
        _expression.value = updated
        updatePreview(updated)
    }

    fun appendParenthesis() {
        val expr = _expression.value
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        val lastChar = expr.lastOrNull()
        val token = if (openCount > closeCount && lastChar != null && (lastChar.isDigit() || lastChar == ')')) {
            ")"
        } else if (lastChar != null && (lastChar.isDigit() || lastChar == ')')) {
            "*("
        } else {
            "("
        }
        appendToken(token)
    }

    fun clearExpression() {
        _expression.value = ""
        _previewText.value = ""
    }

    fun backspaceExpression() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            val updated = current.dropLast(1)
            _expression.value = updated
            updatePreview(updated)
        }
    }

    fun evaluateBasic() {
        val expr = _expression.value
        if (expr.isBlank()) return
        CalculationEngine.evaluateExpression(expr)
            .onSuccess { value ->
                val formatted = CalculationEngine.formatNumber(value)
                _lastResultText.value = formatted
                addHistory(
                    category = AppTab.BASIC,
                    title = expr,
                    detail = "Biểu thức cơ bản",
                    resultText = formatted
                )
                _expression.value = formatted.replace(",", "")
                _previewText.value = ""
            }
            .onFailure {
                _previewText.value = "Lỗi biểu thức"
            }
    }

    private fun updatePreview(expr: String) {
        if (expr.isBlank()) {
            _previewText.value = ""
            return
        }
        CalculationEngine.evaluateExpression(expr)
            .onSuccess { value ->
                _previewText.value = "= ${CalculationEngine.formatNumber(value)}"
            }
            .onFailure {
                _previewText.value = ""
            }
    }

    fun toggleStopwatch() {
        if (_stopwatchRunning.value) {
            _stopwatchRunning.value = false
            stopwatchJob?.cancel()
        } else {
            _stopwatchRunning.value = true
            stopwatchStartEpoch = System.currentTimeMillis() - _stopwatchMillis.value
            stopwatchJob = viewModelScope.launch {
                while (_stopwatchRunning.value) {
                    _stopwatchMillis.value = System.currentTimeMillis() - stopwatchStartEpoch
                    delay(50L)
                }
            }
        }
    }

    fun resetStopwatch() {
        _stopwatchRunning.value = false
        stopwatchJob?.cancel()
        _stopwatchMillis.value = 0L
    }

    fun upsertNote(note: NoteItem) {
        _notes.update { current ->
            val exists = current.any { it.id == note.id }
            if (exists) current.map { if (it.id == note.id) note else it }
            else listOf(note) + current
        }
        saveNotes()
    }

    fun toggleNotePin(noteId: String) {
        _notes.update { current ->
            current.map {
                if (it.id == noteId) it.copy(pinned = !it.pinned, updatedAt = System.currentTimeMillis()) else it
            }
        }
        saveNotes()
    }

    fun toggleChecklistItem(noteId: String, itemId: String) {
        _notes.update { current ->
            current.map { note ->
                if (note.id == noteId) {
                    note.copy(
                        checklist = note.checklist.map { ci ->
                            if (ci.id == itemId) ci.copy(done = !ci.done) else ci
                        },
                        updatedAt = System.currentTimeMillis()
                    )
                } else note
            }
        }
        saveNotes()
    }

    fun deleteNote(noteId: String) {
        _notes.update { current -> current.filterNot { it.id == noteId } }
        saveNotes()
    }

    fun upsertStaff(staff: StaffMember) {
        _staffList.update { current ->
            val exists = current.any { it.id == staff.id }
            if (exists) current.map { if (it.id == staff.id) staff else it }
            else current + staff
        }
        saveAttendanceData()
    }

    fun deleteStaff(staffId: String) {
        _staffList.update { current -> current.filterNot { s -> s.id == staffId } }
        _attendanceRecords.update { it.filterNot { r -> r.staffId == staffId } }
        saveAttendanceData()
    }

    fun upsertAttendanceRecord(record: AttendanceRecord) {
        _attendanceRecords.update { current ->
            val exists = current.any { it.id == record.id }
            if (exists) current.map { if (it.id == record.id) record else it }
            else listOf(record) + current
        }
        saveAttendanceData()
    }

    fun deleteAttendanceRecord(recordId: String) {
        _attendanceRecords.update { current -> current.filterNot { it.id == recordId } }
        saveAttendanceData()
    }

    fun addHistory(category: AppTab, title: String, detail: String, resultText: String) {
        val entry = CalculationHistoryEntry(
            id = UUID.randomUUID().toString(),
            category = category,
            title = title,
            detail = detail,
            resultText = resultText
        )
        _history.update { current -> (listOf(entry) + current).take(200) }
        saveHistory()
    }

    fun deleteHistoryItem(id: String) {
        _history.update { current -> current.filterNot { it.id == id } }
        saveHistory()
    }

    fun clearHistory(categoryFilter: AppTab? = null) {
        _history.update { current ->
            if (categoryFilter == null) emptyList()
            else current.filterNot { it.category == categoryFilter }
        }
        saveHistory()
    }

    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("app", "VMN Máy Tính Đa Năng")
        root.put("publisher", "VMN Studios 95")
        root.put("developer", "Vũ Minh Ngọc")
        root.put("license", "Mã Nguồn Mở")
        root.put("contact", "Email: vuminhngocpt@gmail.com")
        root.put("exportedAt", System.currentTimeMillis())

        val notesArr = JSONArray()
        _notes.value.forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("title", n.title)
            obj.put("content", n.content)
            obj.put("colorKey", n.colorKey)
            obj.put("pinned", n.pinned)
            obj.put("updatedAt", n.updatedAt)
            val clArr = JSONArray()
            n.checklist.forEach { c ->
                clArr.put(JSONObject().apply {
                    put("id", c.id)
                    put("text", c.text)
                    put("done", c.done)
                })
            }
            obj.put("checklist", clArr)
            notesArr.put(obj)
        }
        root.put("notes", notesArr)

        val staffArr = JSONArray()
        _staffList.value.forEach { s ->
            staffArr.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("role", s.role)
                put("hourlyRate", s.hourlyRate)
                put("standardHoursPerDay", s.standardHoursPerDay)
            })
        }
        root.put("staff", staffArr)

        val attArr = JSONArray()
        _attendanceRecords.value.forEach { r ->
            attArr.put(JSONObject().apply {
                put("id", r.id)
                put("staffId", r.staffId)
                put("dateIso", r.dateIso)
                put("status", r.status.id)
                put("hoursWorked", r.hoursWorked)
                put("overtimeHours", r.overtimeHours)
                put("overtimeMultiplier", r.overtimeMultiplier)
                put("bonusOrDeduction", r.bonusOrDeduction)
                put("note", r.note)
            })
        }
        root.put("attendance", attArr)

        val histArr = JSONArray()
        _history.value.forEach { h ->
            histArr.put(JSONObject().apply {
                put("id", h.id)
                put("category", h.category.id)
                put("title", h.title)
                put("detail", h.detail)
                put("resultText", h.resultText)
                put("timestamp", h.timestamp)
            })
        }
        root.put("history", histArr)

        return root.toString(2)
    }

    fun restoreFromBackupJson(jsonText: String): Boolean {
        return try {
            val root = JSONObject(jsonText)

            if (root.has("notes")) {
                val arr = root.getJSONArray("notes")
                val parsedNotes = mutableListOf<NoteItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val clList = mutableListOf<ChecklistItem>()
                    if (o.has("checklist")) {
                        val clArr = o.getJSONArray("checklist")
                        for (j in 0 until clArr.length()) {
                            val c = clArr.getJSONObject(j)
                            clList.add(
                                ChecklistItem(
                                    id = c.optString("id", UUID.randomUUID().toString()),
                                    text = c.optString("text", ""),
                                    done = c.optBoolean("done", false)
                                )
                            )
                        }
                    }
                    parsedNotes.add(
                        NoteItem(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            title = o.optString("title", ""),
                            content = o.optString("content", ""),
                            colorKey = o.optString("colorKey", "emerald"),
                            pinned = o.optBoolean("pinned", false),
                            checklist = clList,
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                _notes.value = parsedNotes
                saveNotes()
            }

            if (root.has("staff")) {
                val arr = root.getJSONArray("staff")
                val parsedStaff = mutableListOf<StaffMember>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    parsedStaff.add(
                        StaffMember(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            name = o.optString("name", ""),
                            role = o.optString("role", ""),
                            hourlyRate = o.optDouble("hourlyRate", 30000.0),
                            standardHoursPerDay = o.optDouble("standardHoursPerDay", 8.0)
                        )
                    )
                }
                _staffList.value = parsedStaff
            }

            if (root.has("attendance")) {
                val arr = root.getJSONArray("attendance")
                val parsedAtt = mutableListOf<AttendanceRecord>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val stId = o.optString("status", "full")
                    val status = AttendanceStatus.entries.firstOrNull { it.id == stId } ?: AttendanceStatus.FULL_DAY
                    parsedAtt.add(
                        AttendanceRecord(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            staffId = o.optString("staffId", ""),
                            dateIso = o.optString("dateIso", todayIso()),
                            status = status,
                            hoursWorked = o.optDouble("hoursWorked", 8.0),
                            overtimeHours = o.optDouble("overtimeHours", 0.0),
                            overtimeMultiplier = o.optDouble("overtimeMultiplier", 1.5),
                            bonusOrDeduction = o.optDouble("bonusOrDeduction", 0.0),
                            note = o.optString("note", "")
                        )
                    )
                }
                _attendanceRecords.value = parsedAtt
            }
            saveAttendanceData()

            if (root.has("history")) {
                val arr = root.getJSONArray("history")
                val parsedHist = mutableListOf<CalculationHistoryEntry>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val catId = o.optString("category", "basic")
                    val cat = AppTab.entries.firstOrNull { it.id == catId } ?: AppTab.BASIC
                    parsedHist.add(
                        CalculationHistoryEntry(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            category = cat,
                            title = o.optString("title", ""),
                            detail = o.optString("detail", ""),
                            resultText = o.optString("resultText", ""),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _history.value = parsedHist
                saveHistory()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun todayIso(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun currentDeviceDateTimeFormatted(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun savePreferences() {
        val p = _prefs.value
        prefsStorage.edit()
            .putString("language", p.language.code)
            .putString("currency", p.currency.code)
            .putString("glassPreset", p.glassPreset.id)
            .putBoolean("darkMode", p.darkMode)
            .putFloat("glassIntensity", p.glassIntensity)
            .putStringSet("enabledTabs", p.enabledTabs.map { it.id }.toSet())
            .apply()
    }

    private fun saveNotes() {
        val arr = JSONArray()
        _notes.value.forEach { n ->
            arr.put(JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("content", n.content)
                put("colorKey", n.colorKey)
                put("pinned", n.pinned)
                put("updatedAt", n.updatedAt)
                put("checklist", JSONArray().apply {
                    n.checklist.forEach { c ->
                        put(JSONObject().apply {
                            put("id", c.id)
                            put("text", c.text)
                            put("done", c.done)
                        })
                    }
                })
            })
        }
        prefsStorage.edit().putString("notes_json", arr.toString()).apply()
    }

    private fun saveAttendanceData() {
        val sArr = JSONArray()
        _staffList.value.forEach { s ->
            sArr.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("role", s.role)
                put("hourlyRate", s.hourlyRate)
                put("standardHoursPerDay", s.standardHoursPerDay)
            })
        }
        val rArr = JSONArray()
        _attendanceRecords.value.forEach { r ->
            rArr.put(JSONObject().apply {
                put("id", r.id)
                put("staffId", r.staffId)
                put("dateIso", r.dateIso)
                put("status", r.status.id)
                put("hoursWorked", r.hoursWorked)
                put("overtimeHours", r.overtimeHours)
                put("overtimeMultiplier", r.overtimeMultiplier)
                put("bonusOrDeduction", r.bonusOrDeduction)
                put("note", r.note)
            })
        }
        prefsStorage.edit()
            .putString("staff_json", sArr.toString())
            .putString("attendance_json", rArr.toString())
            .apply()
    }

    private fun saveHistory() {
        val arr = JSONArray()
        _history.value.forEach { h ->
            arr.put(JSONObject().apply {
                put("id", h.id)
                put("category", h.category.id)
                put("title", h.title)
                put("detail", h.detail)
                put("resultText", h.resultText)
                put("timestamp", h.timestamp)
            })
        }
        prefsStorage.edit().putString("history_json", arr.toString()).apply()
    }

    private fun loadFromStorage() {
        val langCode = prefsStorage.getString("language", AppLanguage.VI.code)
        val lang = AppLanguage.entries.firstOrNull { it.code == langCode } ?: AppLanguage.VI

        val currCode = prefsStorage.getString("currency", CurrencyUnit.VND.code)
        val curr = CurrencyUnit.entries.firstOrNull { it.code == currCode } ?: CurrencyUnit.VND

        val presetId = prefsStorage.getString("glassPreset", GlassPreset.EMERALD_GLASS.id)
        val preset = GlassPreset.entries.firstOrNull { it.id == presetId } ?: GlassPreset.EMERALD_GLASS

        val dark = prefsStorage.getBoolean("darkMode", true)
        val intensity = prefsStorage.getFloat("glassIntensity", 0.72f)
        val tabIds = prefsStorage.getStringSet("enabledTabs", AppTab.entries.map { it.id }.toSet())
        val tabs = tabIds?.mapNotNull { id -> AppTab.entries.firstOrNull { it.id == id } }?.toSet()
            ?.ifEmpty { AppTab.entries.toSet() } ?: AppTab.entries.toSet()

        _prefs.value = AppPreferencesState(
            language = lang,
            currency = curr,
            glassPreset = preset,
            darkMode = dark,
            glassIntensity = intensity,
            enabledTabs = tabs
        )

        prefsStorage.getString("notes_json")?.let { raw ->
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<NoteItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val cl = mutableListOf<ChecklistItem>()
                    if (o.has("checklist")) {
                        val cArr = o.getJSONArray("checklist")
                        for (j in 0 until cArr.length()) {
                            val c = cArr.getJSONObject(j)
                            cl.add(
                                ChecklistItem(
                                    id = c.optString("id", UUID.randomUUID().toString()),
                                    text = c.optString("text", ""),
                                    done = c.optBoolean("done", false)
                                )
                            )
                        }
                    }
                    list.add(
                        NoteItem(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            title = o.optString("title", ""),
                            content = o.optString("content", ""),
                            colorKey = o.optString("colorKey", "emerald"),
                            pinned = o.optBoolean("pinned", false),
                            checklist = cl,
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                _notes.value = list
            } catch (_: Exception) {
            }
        }

        prefsStorage.getString("staff_json")?.let { raw ->
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<StaffMember>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        StaffMember(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            name = o.optString("name", ""),
                            role = o.optString("role", ""),
                            hourlyRate = o.optDouble("hourlyRate", 30000.0),
                            standardHoursPerDay = o.optDouble("standardHoursPerDay", 8.0)
                        )
                    )
                }
                _staffList.value = list
            } catch (_: Exception) {
            }
        }

        prefsStorage.getString("attendance_json")?.let { raw ->
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<AttendanceRecord>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val st = AttendanceStatus.entries.firstOrNull { it.id == o.optString("status", "full") }
                        ?: AttendanceStatus.FULL_DAY
                    list.add(
                        AttendanceRecord(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            staffId = o.optString("staffId", ""),
                            dateIso = o.optString("dateIso", todayIso()),
                            status = st,
                            hoursWorked = o.optDouble("hoursWorked", 8.0),
                            overtimeHours = o.optDouble("overtimeHours", 0.0),
                            overtimeMultiplier = o.optDouble("overtimeMultiplier", 1.5),
                            bonusOrDeduction = o.optDouble("bonusOrDeduction", 0.0),
                            note = o.optString("note", "")
                        )
                    )
                }
                _attendanceRecords.value = list
            } catch (_: Exception) {
            }
        }

        prefsStorage.getString("history_json")?.let { raw ->
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<CalculationHistoryEntry>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val cat = AppTab.entries.firstOrNull { it.id == o.optString("category", "basic") }
                        ?: AppTab.BASIC
                    list.add(
                        CalculationHistoryEntry(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            category = cat,
                            title = o.optString("title", ""),
                            detail = o.optString("detail", ""),
                            resultText = o.optString("resultText", ""),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _history.value = list
            } catch (_: Exception) {
            }
        }
    }

    private fun seedDefaultDataIfEmpty() {
        if (_notes.value.isEmpty()) {
            _notes.value = listOf(
                NoteItem(
                    id = UUID.randomUUID().toString(),
                    title = "Bảng giá dịch vụ & cân hàng",
                    content = "Kiểm tra cân điện tử đầu ca, chốt đơn theo kg / tạ / tấn.",
                    colorKey = "emerald",
                    pinned = true,
                    checklist = listOf(
                        ChecklistItem(UUID.randomUUID().toString(), "Hiệu chuẩn cân điện tử", true),
                        ChecklistItem(UUID.randomUUID().toString(), "Cập nhật đơn giá mới", false)
                    )
                )
            )
            saveNotes()
        }
        if (_staffList.value.isEmpty()) {
            val s1 = StaffMember(UUID.randomUUID().toString(), "Nguyễn Văn An", "Kỹ thuật viên", 45000.0, 8.0)
            val s2 = StaffMember(UUID.randomUUID().toString(), "Trần Thị Bình", "Thu ngân & Kho", 35000.0, 8.0)
            _staffList.value = listOf(s1, s2)
            _attendanceRecords.value = listOf(
                AttendanceRecord(
                    id = UUID.randomUUID().toString(),
                    staffId = s1.id,
                    dateIso = todayIso(),
                    status = AttendanceStatus.FULL_DAY,
                    hoursWorked = 8.0,
                    overtimeHours = 1.5,
                    overtimeMultiplier = 1.5,
                    bonusOrDeduction = 0.0,
                    note = "Ca sáng"
                )
            )
            saveAttendanceData()
        }
    }
}
