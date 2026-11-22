package com.vmnstudios95.maytinhdanang.data

import android.content.Context
import android.net.Uri
import com.vmnstudios95.maytinhdanang.model.CalculatorAppState

class LocalStateStorage(private val context: Context) {

    private val prefs = context.getSharedPreferences("vmn_calculator_v2_prefs", Context.MODE_PRIVATE)

    fun loadState(): CalculatorAppState {
        val raw = prefs.getString(KEY_STATE, null) ?: return CalculatorAppState()
        return CalculatorAppState.fromJson(raw)
    }

    fun saveState(state: CalculatorAppState) {
        prefs.edit().putString(KEY_STATE, state.toJson(wrapPayload = false)).apply()
    }

    fun exportToUri(uri: Uri, state: CalculatorAppState): Result<Unit> = runCatching {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(state.toJson(wrapPayload = true).toByteArray(Charsets.UTF_8))
        } ?: error("Không thể mở luồng ghi tệp")
    }

    fun importFromUri(uri: Uri, currentState: CalculatorAppState): Result<CalculatorAppState> = runCatching {
        val raw = context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).readText()
        } ?: error("Không thể đọc tệp đồng bộ")
        CalculatorAppState.fromJson(raw, fallback = currentState)
    }

    companion object {
        private const val KEY_STATE = "vmn_calculator_v2_state"
    }
}
