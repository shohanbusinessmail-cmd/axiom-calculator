package com.axiom.calculator.core.math

import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Complex number type with documented branch conventions:
 *  - [arg] returns the principal value in (-pi, pi], matching atan2 semantics.
 *  - [pow] uses the principal branch of the complex logarithm.
 */
data class Complex(val re: Double, val im: Double) {
    operator fun plus(o: Complex) = Complex(re + o.re, im + o.im)
    operator fun minus(o: Complex) = Complex(re - o.re, im - o.im)
    operator fun times(o: Complex) = Complex(re * o.re - im * o.im, re * o.im + im * o.re)

    operator fun div(o: Complex): Complex {
        val denom = o.re * o.re + o.im * o.im
        if (denom == 0.0) throw AxMathError.DivisionByZero("Division by zero complex number is undefined.")
        return Complex((re * o.re + im * o.im) / denom, (im * o.re - re * o.im) / denom)
    }

    fun abs(): Double = hypot(re, im)
    fun arg(): Double = atan2(im, re)
    fun conjugate(): Complex = Complex(re, -im)

    fun pow(n: Double): Complex {
        if (re == 0.0 && im == 0.0) {
            return if (n > 0) Complex(0.0, 0.0) else throw AxMathError.DivisionByZero("Zero raised to a non-positive power is undefined.")
        }
        val r = abs().pow(n)
        val theta = arg() * n
        return Complex(r * cos(theta), r * sin(theta))
    }

    fun format(precision: Int = 10): String {
        val reRounded = NumberFormatter.format(re, DisplayFormatOptions(precision = precision, notation = NotationMode.PLAIN, useGrouping = false))
        if (im == 0.0) return reRounded
        val imAbsRounded = NumberFormatter.format(kotlin.math.abs(im), DisplayFormatOptions(precision = precision, notation = NotationMode.PLAIN, useGrouping = false))
        val sign = if (im >= 0) "+" else "-"
        return "$reRounded$sign${imAbsRounded}i"
    }

    companion object {
        val ZERO = Complex(0.0, 0.0)
        val I = Complex(0.0, 1.0)
    }
}
