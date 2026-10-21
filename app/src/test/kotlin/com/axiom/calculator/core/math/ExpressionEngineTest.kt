package com.axiom.calculator.core.math

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI

/**
 * Kotlin/JUnit port of the mandatory SECTION 17.1-17.4 vectors. This mirrors
 * scripts/verification/run_vectors.py (executed against the Python reference
 * and currently 113/113 passing — see docs/QUALITY_REPORT.md). This Kotlin
 * suite could NOT be compiled or run in the authoring sandbox (no JDK/Android
 * SDK/Gradle network access; see docs/BUILD_AND_RELEASE.md) and is reported
 * as such, not as an executed/passing suite.
 */
class ExpressionEngineTest {
    private fun eval(expr: String, deg: Boolean = true): Double =
        Evaluator.eval(ExpressionParser.parse(expr), EvalContext(if (deg) AngleMode.DEGREES else AngleMode.RADIANS))

    // 17.1 Arithmetic
    @Test fun m001_addition() = assertEquals(5.0, eval("2 + 3"), 0.0)
    @Test fun m002_precedence() = assertEquals(14.0, eval("2 + 3 * 4"), 0.0)
    @Test fun m003_parentheses() = assertEquals(20.0, eval("(2 + 3) * 4"), 0.0)
    @Test fun m004_division() = assertEquals(2.5, eval("10 / 4"), 0.0)
    @Test fun m006_power() = assertEquals(8.0, eval("2^3"), 0.0)
    @Test fun m007_rightAssociativePower() = assertEquals(512.0, eval("2^3^2"), 0.0)
    @Test fun m008_unaryMinusPrecedence() = assertEquals(-4.0, eval("-2^2"), 0.0)
    @Test fun m009_parenthesizedNegativeBase() = assertEquals(4.0, eval("(-2)^2"), 0.0)
    @Test fun m010_sqrt() = assertEquals(9.0, eval("sqrt(81)"), 0.0)
    @Test fun m011_abs() = assertEquals(12.0, eval("abs(-12)"), 0.0)

    @Test(expected = AxMathError.DivisionByZero::class)
    fun m012_divisionByZero() { eval("1/0") }

    @Test(expected = AxMathError.Domain::class)
    fun m013_sqrtNegativeDomainError() { eval("sqrt(-1)") }

    @Test(expected = AxMathError.Syntax::class)
    fun m014_trailingOperatorSyntaxError() { eval("2 +") }

    @Test(expected = AxMathError.Syntax::class)
    fun m015_mismatchedParenSyntaxError() { eval("(2 + 3") }

    @Test(expected = AxMathError.Syntax::class)
    fun m016_emptyExpression() { eval("") }

    @Test(expected = AxMathError.ResourceLimit::class)
    fun m017_excessiveNesting() { eval("(".repeat(200) + "1" + ")".repeat(200)) }

    @Test fun implicitMultiplication() = assertEquals(14.0, eval("2(3+4)"), 0.0)
    @Test fun implicitMultiplicationConvention() = assertEquals(2.0, eval("1/2(4)"), 0.0)
    @Test fun negativeExponent() = assertEquals(0.25, eval("2^-2"), 0.0)
    @Test fun scientificNotation() = assertEquals(151.0, eval("1.5e2 + 1"), 0.0)

    // 17.2 Trigonometry
    @Test fun t001_sinDeg() = assertEquals(0.5, eval("sin(30)"), 1e-10)
    @Test fun t002_cosDeg() = assertEquals(0.5, eval("cos(60)"), 1e-10)
    @Test fun t003_tanDeg() = assertEquals(1.0, eval("tan(45)"), 1e-10)
    @Test fun t004_sinRad() = assertEquals(0.5, eval("sin(pi/6)", deg = false), 1e-10)
    @Test fun t005_cosRad() = assertEquals(-1.0, eval("cos(pi)", deg = false), 1e-10)
    @Test fun t006_asinDeg() = assertEquals(30.0, eval("asin(0.5)"), 1e-9)
    @Test fun t007_asinRad() = assertEquals(PI / 6, eval("asin(0.5)", deg = false), 1e-9)
    @Test fun t008_acosh() = assertEquals(0.0, eval("acosh(1)"), 1e-10)

    @Test(expected = AxMathError.Domain::class)
    fun t009_atanhSingularity() { eval("atanh(1)") }

    // 17.3 Logs and combinatorics
    @Test fun s001_log10() = assertEquals(3.0, eval("log10(1000)"), 1e-9)
    @Test fun s002_ln() = assertEquals(1.0, eval("ln(e)"), 1e-9)
    @Test fun s003_exp() = assertEquals(1.0, eval("exp(0)"), 0.0)
    @Test fun s004_factorial() = assertEquals(120.0, eval("5!"), 0.0)
    @Test fun s005_zeroFactorial() = assertEquals(1.0, eval("0!"), 0.0)
    @Test fun s006_threeFactorial() = assertEquals(6.0, eval("3!"), 0.0)
    @Test fun s007_permutations() = assertEquals(20L, MathFunctions.permutations(5, 2).toLong())
    @Test fun s008_combinations() = assertEquals(10L, MathFunctions.combinations(5, 2).toLong())

    @Test(expected = AxMathError.Domain::class)
    fun s009_log10Zero() { eval("log10(0)") }

    @Test(expected = AxMathError.Domain::class)
    fun s010_negativeFactorial() { eval("(-3)!") }

    // 17.4 Complex arithmetic
    @Test fun c001_complexAddition() {
        val r = Complex(2.0, 3.0) + Complex(1.0, 4.0)
        assertEquals(3.0, r.re, 0.0); assertEquals(7.0, r.im, 0.0)
    }

    @Test fun c002_complexMultiplication() {
        val r = Complex(2.0, 3.0) * Complex(1.0, 4.0)
        assertEquals(-10.0, r.re, 0.0); assertEquals(11.0, r.im, 0.0)
    }

    @Test fun c003_complexAbs() = assertEquals(5.0, Complex(3.0, 4.0).abs(), 0.0)

    @Test fun c004_conjugate() {
        val r = Complex(2.0, 3.0).conjugate()
        assertEquals(2.0, r.re, 0.0); assertEquals(-3.0, r.im, 0.0)
    }

    @Test(expected = AxMathError.DivisionByZero::class)
    fun c005_divisionByZeroComplex() { Complex(1.0, 1.0) / Complex(0.0, 0.0) }
}
