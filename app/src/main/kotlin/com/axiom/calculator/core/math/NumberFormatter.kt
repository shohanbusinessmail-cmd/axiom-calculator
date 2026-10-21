package com.axiom.calculator.core.math

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10

enum class NotationMode { AUTO, SCIENTIFIC, ENGINEERING, PLAIN }

data class DisplayFormatOptions(
    val precision: Int = 10,
    val notation: NotationMode = NotationMode.AUTO,
    val useGrouping: Boolean = true,
    /** Threshold beyond which AUTO notation switches to scientific form. */
    val scientificUpperThreshold: Double = 1e15,
    val scientificLowerThreshold: Double = 1e-9
)

/**
 * Formats numeric results for display, keeping internal precision (the raw
 * Double/BigDecimal value used for further computation) separate from the
 * rounded string shown to the user, per SECTION 8's precision-policy
 * requirement.
 */
object NumberFormatter {

    fun format(value: Double, options: DisplayFormatOptions = DisplayFormatOptions()): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return if (value > 0) "Overflow" else "-Overflow"
        if (value == 0.0) return "0"

        val abs = abs(value)
        val useScientific = when (options.notation) {
            NotationMode.SCIENTIFIC -> true
            NotationMode.ENGINEERING -> false
            NotationMode.PLAIN -> false
            NotationMode.AUTO -> abs >= options.scientificUpperThreshold || abs < options.scientificLowerThreshold
        }

        return when {
            options.notation == NotationMode.ENGINEERING -> formatEngineering(value, options.precision)
            useScientific -> formatScientific(value, options.precision)
            else -> formatPlain(value, options)
        }
    }

    private fun formatPlain(value: Double, options: DisplayFormatOptions): String {
        var bd = BigDecimal(value).round(java.math.MathContext(options.precision, RoundingMode.HALF_UP))
        bd = bd.stripTrailingZeros()
        if (bd.scale() < 0) bd = bd.setScale(0)
        var text = bd.toPlainString()
        if (options.useGrouping) {
            text = applyGrouping(text)
        }
        return text
    }

    private fun applyGrouping(plain: String): String {
        val negative = plain.startsWith("-")
        val body = if (negative) plain.substring(1) else plain
        val parts = body.split(".")
        val intPart = parts[0]
        val sb = StringBuilder()
        for ((idx, ch) in intPart.reversed().withIndex()) {
            if (idx != 0 && idx % 3 == 0) sb.append(',')
            sb.append(ch)
        }
        var result = sb.reverse().toString()
        if (parts.size > 1) result += "." + parts[1]
        return if (negative) "-$result" else result
    }

    private fun formatScientific(value: Double, precision: Int): String {
        val exponent = floor(log10(abs(value))).toInt()
        val mantissa = value / Math.pow(10.0, exponent.toDouble())
        val mantissaStr = BigDecimal(mantissa).round(java.math.MathContext(precision, RoundingMode.HALF_UP))
            .stripTrailingZeros().toPlainString()
        return "${mantissaStr}e${if (exponent >= 0) "+" else ""}$exponent"
    }

    private fun formatEngineering(value: Double, precision: Int): String {
        if (value == 0.0) return "0e+0"
        val exponent = floor(log10(abs(value))).toInt()
        val engExponent = (exponent / 3) * 3 - (if (exponent < 0 && exponent % 3 != 0) 3 else 0)
        val mantissa = value / Math.pow(10.0, engExponent.toDouble())
        val mantissaStr = BigDecimal(mantissa).round(java.math.MathContext(precision, RoundingMode.HALF_UP))
            .stripTrailingZeros().toPlainString()
        return "${mantissaStr}e${if (engExponent >= 0) "+" else ""}$engExponent"
    }

    /** Lakh/crore-style grouping used optionally for Bangladesh-first presentation (SECTION 23). */
    fun formatIndianGrouping(plain: String): String {
        val negative = plain.startsWith("-")
        val body = if (negative) plain.substring(1) else plain
        val parts = body.split(".")
        var intPart = parts[0]
        if (intPart.length <= 3) return plain
        val last3 = intPart.takeLast(3)
        var rest = intPart.dropLast(3)
        val sb = StringBuilder()
        var count = 0
        for (ch in rest.reversed()) {
            if (count != 0 && count % 2 == 0) sb.append(',')
            sb.append(ch)
            count++
        }
        val groupedRest = sb.reverse().toString()
        var result = "$groupedRest,$last3"
        if (parts.size > 1) result += "." + parts[1]
        return if (negative) "-$result" else result
    }
}
