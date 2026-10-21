package com.axiom.calculator.core.solve

import com.axiom.calculator.core.math.AxMathError
import kotlin.math.pow

object Sequences {
    /** Arithmetic progression: a1, a1+d, a1+2d, ... */
    fun arithmeticNthTerm(a1: Double, d: Double, n: Int): Double {
        if (n < 1) throw AxMathError.InvalidInput("The term index must be 1 or greater.")
        return a1 + (n - 1) * d
    }

    fun arithmeticSum(a1: Double, d: Double, n: Int): Double {
        if (n < 1) throw AxMathError.InvalidInput("The number of terms must be 1 or greater.")
        return n / 2.0 * (2 * a1 + (n - 1) * d)
    }

    /** Geometric progression: a1, a1*r, a1*r^2, ... */
    fun geometricNthTerm(a1: Double, r: Double, n: Int): Double {
        if (n < 1) throw AxMathError.InvalidInput("The term index must be 1 or greater.")
        return a1 * r.pow(n - 1)
    }

    fun geometricSum(a1: Double, r: Double, n: Int): Double {
        if (n < 1) throw AxMathError.InvalidInput("The number of terms must be 1 or greater.")
        if (r == 1.0) return a1 * n
        return a1 * (1 - r.pow(n)) / (1 - r)
    }
}
