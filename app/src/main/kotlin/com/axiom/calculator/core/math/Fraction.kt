package com.axiom.calculator.core.math

import java.math.BigInteger

/**
 * Exact rational fraction type with automatic simplification via GCD.
 * Used for fraction-mode scientific calculator input and ratio formulas.
 */
data class AxFraction(val numerator: BigInteger, val denominator: BigInteger) {
    init {
        if (denominator.signum() == 0) throw AxMathError.DivisionByZero("A fraction cannot have a zero denominator.")
    }

    companion object {
        fun of(numerator: Long, denominator: Long): AxFraction =
            simplify(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))

        fun simplify(numerator: BigInteger, denominator: BigInteger): AxFraction {
            var n = numerator
            var d = denominator
            if (d.signum() < 0) { n = n.negate(); d = d.negate() }
            val g = n.gcd(d).let { if (it.signum() == 0) BigInteger.ONE else it }
            return AxFraction(n.divide(g), d.divide(g))
        }

        /**
         * Approximates a decimal value as a fraction using a bounded
         * continued-fraction expansion. [maxDenominator] caps the search so
         * the approximation always terminates (resource protection).
         */
        fun fromDecimal(value: Double, maxDenominator: Long = 1_000_000L): AxFraction {
            if (value == 0.0) return AxFraction(BigInteger.ZERO, BigInteger.ONE)
            val negative = value < 0
            var x = kotlin.math.abs(value)
            var h1 = 1L; var h0 = 0L
            var k1 = 0L; var k0 = 1L
            var b = x
            var iterations = 0
            while (iterations < 64) {
                val a = kotlin.math.floor(b).toLong()
                val h2 = a * h1 + h0
                val k2 = a * k1 + k0
                if (k2 > maxDenominator) break
                h0 = h1; h1 = h2
                k0 = k1; k1 = k2
                val frac = b - a
                if (frac < 1e-12) break
                b = 1.0 / frac
                iterations++
            }
            val numerator = if (negative) -h1 else h1
            return simplify(BigInteger.valueOf(numerator), BigInteger.valueOf(k1))
        }
    }

    operator fun plus(o: AxFraction) = simplify(
        numerator.multiply(o.denominator).add(o.numerator.multiply(denominator)),
        denominator.multiply(o.denominator)
    )

    operator fun minus(o: AxFraction) = simplify(
        numerator.multiply(o.denominator).subtract(o.numerator.multiply(denominator)),
        denominator.multiply(o.denominator)
    )

    operator fun times(o: AxFraction) = simplify(numerator.multiply(o.numerator), denominator.multiply(o.denominator))

    operator fun div(o: AxFraction): AxFraction {
        if (o.numerator.signum() == 0) throw AxMathError.DivisionByZero()
        return simplify(numerator.multiply(o.denominator), denominator.multiply(o.numerator))
    }

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    fun format(): String = if (denominator == BigInteger.ONE) numerator.toString() else "$numerator/$denominator"
}
