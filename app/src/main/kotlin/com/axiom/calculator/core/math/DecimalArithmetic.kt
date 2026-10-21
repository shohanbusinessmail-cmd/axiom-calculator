package com.axiom.calculator.core.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * BigDecimal-backed arithmetic used by the Standard calculator so that
 * ordinary decimal arithmetic (addition, subtraction, multiplication,
 * division, percentage) never exhibits binary floating-point artifacts.
 *
 * Example: 0.1 + 0.2 must display as exactly "0.3" (M005), which fails under
 * naive Double arithmetic (0.1 + 0.2 == 0.30000000000000004). BigDecimal with
 * a bounded [MathContext] and explicit trailing-zero stripping avoids this.
 */
object DecimalArithmetic {
    /** 34 significant digits (IEEE 754 decimal128-equivalent precision) with half-up rounding. */
    val CONTEXT: MathContext = MathContext(34, RoundingMode.HALF_UP)

    fun add(a: BigDecimal, b: BigDecimal): BigDecimal = a.add(b, CONTEXT).stripTrailingZerosSafe()
    fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal = a.subtract(b, CONTEXT).stripTrailingZerosSafe()
    fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal = a.multiply(b, CONTEXT).stripTrailingZerosSafe()

    fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.signum() == 0) throw AxMathError.DivisionByZero()
        return a.divide(b, CONTEXT).stripTrailingZerosSafe()
    }

    /** Contextual calculator percent: `a % of nothing` = a/100 (used when % is a lone postfix op). */
    fun percentOf(a: BigDecimal): BigDecimal = a.divide(BigDecimal(100), CONTEXT).stripTrailingZerosSafe()

    /**
     * Calculator-style "operator aware" percentage, matching the behavior of
     * mainstream calculator apps: for + and -, "a <op> b%" means "b% OF a";
     * for * and /, "a <op> b%" means plain "b/100".
     */
    fun contextualPercent(pendingOp: Char?, a: BigDecimal, b: BigDecimal): BigDecimal = when (pendingOp) {
        '+', '-' -> multiply(a, divide(b, BigDecimal(100)))
        else -> divide(b, BigDecimal(100))
    }

    private fun BigDecimal.stripTrailingZerosSafe(): BigDecimal =
        if (this.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else this.stripTrailingZeros()
}
