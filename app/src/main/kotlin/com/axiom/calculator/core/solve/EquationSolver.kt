package com.axiom.calculator.core.solve

import com.axiom.calculator.core.math.AxMathError
import com.axiom.calculator.core.math.Complex
import kotlin.math.sqrt

sealed class LinearResult {
    data class Unique(val x: Double) : LinearResult()
    object NoSolution : LinearResult()
    object InfiniteSolutions : LinearResult()
}

sealed class QuadraticRealResult {
    data class TwoRoots(val x1: Double, val x2: Double) : QuadraticRealResult()
    data class OneRoot(val x: Double) : QuadraticRealResult()
    object NoRealRoots : QuadraticRealResult()
}

data class QuadraticComplexResult(val x1: Complex, val x2: Complex)

sealed class System2x2Result {
    data class Unique(val x: Double, val y: Double) : System2x2Result()
    object Singular : System2x2Result()
}

sealed class System3x3Result {
    data class Unique(val x: Double, val y: Double, val z: Double) : System3x3Result()
    object Singular : System3x3Result()
}

/**
 * Pure numerical equation solvers. All inputs here are explicit numeric
 * coefficients (a, b, c, ...); see [SingleVariableEquationParser] for the
 * bounded string-to-coefficient front end used by the Quick Solve UI.
 */
object EquationSolver {

    /** Solves ax + b = 0. */
    fun solveLinear(a: Double, b: Double): LinearResult = when {
        a != 0.0 -> LinearResult.Unique(-b / a)
        b == 0.0 -> LinearResult.InfiniteSolutions
        else -> LinearResult.NoSolution
    }

    /** Solves ax^2 + bx + c = 0 in the real domain. */
    fun solveQuadraticReal(a: Double, b: Double, c: Double): QuadraticRealResult {
        if (a == 0.0) {
            return when (val lin = solveLinear(b, c)) {
                is LinearResult.Unique -> QuadraticRealResult.OneRoot(lin.x)
                LinearResult.InfiniteSolutions -> throw AxMathError.InvalidInput("Every real number is a solution (0 = 0).")
                LinearResult.NoSolution -> QuadraticRealResult.NoRealRoots
            }
        }
        val discriminant = b * b - 4 * a * c
        return when {
            discriminant > 0 -> {
                val sq = sqrt(discriminant)
                val r1 = (-b - sq) / (2 * a)
                val r2 = (-b + sq) / (2 * a)
                QuadraticRealResult.TwoRoots(minOf(r1, r2), maxOf(r1, r2))
            }
            discriminant == 0.0 -> QuadraticRealResult.OneRoot(-b / (2 * a))
            else -> QuadraticRealResult.NoRealRoots
        }
    }

    /** Solves ax^2 + bx + c = 0 over the complex numbers (always returns two roots, possibly equal). */
    fun solveQuadraticComplex(a: Double, b: Double, c: Double): QuadraticComplexResult {
        if (a == 0.0) throw AxMathError.InvalidInput("The leading coefficient must be non-zero for a quadratic equation.")
        val discriminant = b * b - 4 * a * c
        return if (discriminant >= 0) {
            val sq = sqrt(discriminant)
            QuadraticComplexResult(Complex((-b - sq) / (2 * a), 0.0), Complex((-b + sq) / (2 * a), 0.0))
        } else {
            val sq = sqrt(-discriminant)
            QuadraticComplexResult(
                Complex(-b / (2 * a), -sq / (2 * a)),
                Complex(-b / (2 * a), sq / (2 * a))
            )
        }
    }

    /** Solves a1 x + b1 y = c1 ; a2 x + b2 y = c2 via Cramer's rule. */
    fun solveSystem2x2(a1: Double, b1: Double, c1: Double, a2: Double, b2: Double, c2: Double): System2x2Result {
        val det = a1 * b2 - a2 * b1
        if (det == 0.0) return System2x2Result.Singular
        val x = (c1 * b2 - c2 * b1) / det
        val y = (a1 * c2 - a2 * c1) / det
        return System2x2Result.Unique(x, y)
    }

    /** Solves a 3-variable linear system via Gaussian elimination with partial pivoting. */
    fun solveSystem3x3(m: Array<DoubleArray>): System3x3Result {
        require(m.size == 3 && m.all { it.size == 4 }) { "A 3x3 system requires a 3x4 augmented matrix." }
        val a = Array(3) { m[it].copyOf() }
        for (col in 0..2) {
            var pivotRow = col
            for (r in col + 1..2) if (kotlin.math.abs(a[r][col]) > kotlin.math.abs(a[pivotRow][col])) pivotRow = r
            if (kotlin.math.abs(a[pivotRow][col]) < 1e-12) return System3x3Result.Singular
            val tmp = a[col]; a[col] = a[pivotRow]; a[pivotRow] = tmp
            for (r in 0..2) {
                if (r == col) continue
                val factor = a[r][col] / a[col][col]
                for (c in col..3) a[r][c] -= factor * a[col][c]
            }
        }
        val x = a[0][3] / a[0][0]
        val y = a[1][3] / a[1][1]
        val z = a[2][3] / a[2][2]
        return System3x3Result.Unique(x, y, z)
    }
}
