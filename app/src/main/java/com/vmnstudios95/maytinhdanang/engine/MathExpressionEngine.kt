package com.vmnstudios95.maytinhdanang.engine

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

object MathExpressionEngine {

    private sealed class Token {
        data class Num(val value: Double) : Token()
        data class Op(val symbol: Char) : Token()
    }

    fun cleanNumber(num: Double, maxDecimals: Int = 10): Double {
        if (!num.isFinite()) return Double.NaN
        if (abs(num) < 1e-12) return 0.0
        val factor = 10.0.pow(maxDecimals)
        val rounded = kotlin.math.round(num * factor) / factor
        return BigDecimal.valueOf(rounded)
            .stripTrailingZeros()
            .toDouble()
    }

    fun formatCleanNumber(num: Double): String {
        val cleaned = cleanNumber(num, 8)
        if (!cleaned.isFinite()) return "0"
        if (cleaned == 0.0) return "0"
        val bd = BigDecimal.valueOf(cleaned).stripTrailingZeros()
        return bd.toPlainString()
    }

    fun formatCurrencyVND(amount: Double): String {
        val cleaned = cleanNumber(amount, 4)
        if (!cleaned.isFinite()) return "0 đ"
        val rounded = cleaned.roundToLong()
        val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
            groupingSeparator = '.'
        }
        val formatter = DecimalFormat("#,##0", symbols)
        return "${formatter.format(rounded)} đ"
    }

    fun normalizeOperator(token: String): String = when (token) {
        "×" -> "*"
        "÷" -> "/"
        "−" -> "-"
        else -> token
    }

    fun formatExpressionForDisplay(rawExpr: String): String {
        return rawExpr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("*", " × ")
            .replace("/", " ÷ ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun evaluate(rawExpr: String): Result<Double> {
        return runCatching {
            val tokens = tokenize(rawExpr)
            if (tokens.isEmpty()) throw IllegalArgumentException("Biểu thức rỗng")
            val parser = RecursiveDescentParser(tokens)
            val result = parser.parse()
            if (!result.isFinite()) throw IllegalArgumentException("Kết quả không hợp lệ")
            cleanNumber(result, 10)
        }
    }

    private fun tokenize(exprStr: String): List<Token> {
        val normalized = exprStr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(Regex("\\s+"), "")

        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < normalized.length) {
            val ch = normalized[i]
            if (ch in '0'..'9' || ch == '.') {
                val sb = StringBuilder()
                var dotCount = 0
                while (i < normalized.length) {
                    val c = normalized[i]
                    if (c in '0'..'9') {
                        sb.append(c)
                        i++
                    } else if (c == '.') {
                        if (dotCount >= 1) throw IllegalArgumentException("Dấu thập phân không hợp lệ")
                        dotCount++
                        sb.append(c)
                        i++
                    } else {
                        break
                    }
                }
                val numStr = sb.toString()
                if (numStr == ".") throw IllegalArgumentException("Thiếu chữ số")
                val parsed = numStr.toDoubleOrNull()
                    ?: throw IllegalArgumentException("Số không hợp lệ")
                if (!parsed.isFinite()) throw IllegalArgumentException("Số vượt giới hạn")
                tokens.add(Token.Num(parsed))
            } else if (ch in charArrayOf('(', ')', '+', '-', '*', '/', '%')) {
                tokens.add(Token.Op(ch))
                i++
            } else {
                throw IllegalArgumentException("Ký tự không hỗ trợ")
            }
        }
        return tokens
    }

    private class RecursiveDescentParser(private val tokens: List<Token>) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun consume(): Token? = tokens.getOrNull(pos++)

        fun parse(): Double {
            val result = parseAddSub()
            if (pos != tokens.size) {
                throw IllegalArgumentException("Dư ký tự trong biểu thức")
            }
            return cleanNumber(result, 10)
        }

        private fun parseAddSub(): Double {
            var left = parseMulDiv()
            while (true) {
                val next = peek() as? Token.Op ?: break
                if (next.symbol == '+' || next.symbol == '-') {
                    consume()
                    val right = parseMulDiv()
                    left = if (next.symbol == '+') {
                        cleanNumber(left + right, 10)
                    } else {
                        cleanNumber(left - right, 10)
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun parseMulDiv(): Double {
            var left = parseUnary()
            while (true) {
                val next = peek() ?: break
                if (next is Token.Op && (next.symbol == '*' || next.symbol == '/')) {
                    consume()
                    val right = parseUnary()
                    left = if (next.symbol == '*') {
                        cleanNumber(left * right, 10)
                    } else {
                        if (abs(right) < 1e-15) throw ArithmeticException("Không thể chia cho 0")
                        cleanNumber(left / right, 10)
                    }
                } else if (next is Token.Num || (next is Token.Op && next.symbol == '(')) {
                    // Nhân ngầm định: 2(3+4) hoặc (1+2)(3+4)
                    val right = parseUnary()
                    left = cleanNumber(left * right, 10)
                } else {
                    break
                }
            }
            return left
        }

        private fun parseUnary(): Double {
            val tok = peek() ?: throw IllegalArgumentException("Biểu thức chưa hoàn chỉnh")
            if (tok is Token.Op && tok.symbol == '+') {
                consume()
                return parseUnary()
            }
            if (tok is Token.Op && tok.symbol == '-') {
                consume()
                return cleanNumber(-parseUnary(), 10)
            }
            var value = parsePrimary()
            while (true) {
                val next = peek()
                if (next is Token.Op && next.symbol == '%') {
                    consume()
                    value = cleanNumber(value / 100.0, 10)
                } else {
                    break
                }
            }
            return value
        }

        private fun parsePrimary(): Double {
            val tok = peek() ?: throw IllegalArgumentException("Thiếu toán hạng")
            if (tok is Token.Num) {
                consume()
                return tok.value
            }
            if (tok is Token.Op && tok.symbol == '(') {
                consume()
                val inner = parseAddSub()
                val closeTok = peek()
                if (closeTok !is Token.Op || closeTok.symbol != ')') {
                    throw IllegalArgumentException("Thiếu dấu ngoặc )")
                }
                consume()
                return inner
            }
            throw IllegalArgumentException("Cú pháp không hợp lệ")
        }
    }
}
