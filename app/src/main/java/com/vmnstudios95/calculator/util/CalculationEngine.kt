package com.vmnstudios95.calculator.util

import com.vmnstudios95.calculator.model.CurrencyUnit
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

object CalculationEngine {

    fun cleanDouble(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        val rounded = round(value * 1e10) / 1e10
        return if (abs(rounded) < 1e-11) 0.0 else rounded
    }

    fun formatNumber(value: Double, maxDecimals: Int = 6): String {
        val cleaned = cleanDouble(value)
        if (abs(cleaned - round(cleaned)) < 1e-9 && abs(cleaned) < 1e15) {
            val nf = NumberFormat.getNumberInstance(Locale.US)
            nf.maximumFractionDigits = 0
            return nf.format(round(cleaned).toLong())
        }
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.maximumFractionDigits = maxDecimals
        nf.minimumFractionDigits = 0
        return nf.format(cleaned)
    }

    fun formatCurrency(amount: Double, currency: CurrencyUnit): String {
        val cleaned = cleanDouble(amount)
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.minimumFractionDigits = currency.decimals
        nf.maximumFractionDigits = currency.decimals
        return "${nf.format(cleaned)} ${currency.symbol}"
    }

    fun evaluateExpression(rawExpr: String): Result<Double> {
        val normalized = rawExpr
            .replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace(',', '.')
            .replace(" ", "")

        if (normalized.isBlank()) return Result.failure(IllegalArgumentException("Empty"))

        val expanded = expandPercentages(normalized)
        return try {
            val parser = ExpressionParser(expanded)
            val result = parser.parse()
            if (result.isNaN() || result.isInfinite()) {
                Result.failure(ArithmeticException("Math error"))
            } else {
                Result.success(cleanDouble(result))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun expandPercentages(expr: String): String {
        val sb = StringBuilder()
        for (ch in expr) {
            if (ch == '%') sb.append("/100") else sb.append(ch)
        }
        return sb.toString()
    }

    fun calculateEvnTieredElectricity(kwh: Double, includeVat: Boolean = true): Pair<Double, List<TierBreakdown>> {
        if (kwh <= 0.0) return 0.0 to emptyList()
        val tiers = listOf(
            Triple("Bậc 1 (0–50 kWh)", 50.0, 1893.0),
            Triple("Bậc 2 (51–100 kWh)", 50.0, 1956.0),
            Triple("Bậc 3 (101–200 kWh)", 100.0, 2271.0),
            Triple("Bậc 4 (201–300 kWh)", 100.0, 2860.0),
            Triple("Bậc 5 (301–400 kWh)", 100.0, 3197.0),
            Triple("Bậc 6 (401+ kWh)", Double.POSITIVE_INFINITY, 3302.0)
        )

        var remaining = kwh
        var subtotal = 0.0
        val breakdowns = mutableListOf<TierBreakdown>()

        for ((label, capacity, price) in tiers) {
            if (remaining <= 0.0) break
            val used = if (capacity.isInfinite()) remaining else minOf(remaining, capacity)
            val cost = cleanDouble(used * price)
            subtotal += cost
            remaining -= used
            breakdowns.add(TierBreakdown(label, cleanDouble(used), price, cost))
        }

        val finalAmount = if (includeVat) cleanDouble(subtotal * 1.08) else cleanDouble(subtotal)
        return finalAmount to breakdowns
    }

    data class TierBreakdown(
        val tierName: String,
        val kwhUsed: Double,
        val unitPrice: Double,
        val cost: Double
    )

    private class ExpressionParser(private val input: String) {
        private var pos = 0

        fun parse(): Double {
            val value = parseAddSub()
            if (pos < input.length) {
                throw IllegalArgumentException("Unexpected char: ${input[pos]}")
            }
            return value
        }

        private fun parseAddSub(): Double {
            var value = parseMulDiv()
            while (pos < input.length) {
                when (input[pos]) {
                    '+' -> {
                        pos++
                        value += parseMulDiv()
                    }
                    '-' -> {
                        pos++
                        value -= parseMulDiv()
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parseMulDiv(): Double {
            var value = parsePower()
            while (pos < input.length) {
                when (input[pos]) {
                    '*' -> {
                        pos++
                        value *= parsePower()
                    }
                    '/' -> {
                        pos++
                        val divisor = parsePower()
                        if (abs(divisor) < 1e-15) throw ArithmeticException("Division by zero")
                        value /= divisor
                    }
                    '(' -> value *= parsePower()
                    else -> break
                }
            }
            return value
        }

        private fun parsePower(): Double {
            var base = parseUnary()
            if (pos < input.length && input[pos] == '^') {
                pos++
                val exp = parsePower()
                base = base.pow(exp)
            }
            return base
        }

        private fun parseUnary(): Double {
            if (pos < input.length) {
                when (input[pos]) {
                    '+' -> {
                        pos++
                        return parseUnary()
                    }
                    '-' -> {
                        pos++
                        return -parseUnary()
                    }
                }
            }
            return parsePrimary()
        }

        private fun parsePrimary(): Double {
            if (pos >= input.length) throw IllegalArgumentException("Unexpected end")

            if (input[pos] == '(') {
                pos++
                val inner = parseAddSub()
                if (pos < input.length && input[pos] == ')') {
                    pos++
                }
                return inner
            }

            val start = pos
            var hasDot = false
            while (pos < input.length) {
                val ch = input[pos]
                if (ch.isDigit()) {
                    pos++
                } else if (ch == '.' && !hasDot) {
                    hasDot = true
                    pos++
                } else {
                    break
                }
            }
            if (start == pos) throw IllegalArgumentException("Expected number at $pos")
            return input.substring(start, pos).toDouble()
        }
    }
}
