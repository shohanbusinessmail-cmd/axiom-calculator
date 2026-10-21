package com.axiom.calculator.core.math

import java.math.BigInteger
import kotlin.math.*

enum class AngleMode { DEGREES, RADIANS }

data class EvalContext(val angleMode: AngleMode = AngleMode.DEGREES)

/**
 * Numeric evaluator for the [Expr] AST produced by [ExpressionParser].
 *
 * Numeric policy (SECTION 8 / docs/MATHEMATICAL_SEMANTICS.md):
 *  - General/transcendental evaluation is performed in IEEE-754 `Double`
 *    (scientific functions have no exact decimal representation in general).
 *  - Exact integer operations (factorial, nPr, nCr) are routed through
 *    [BigInteger] via [MathFunctions.factorial] to avoid silent precision
 *    loss for inputs that still fit a sensible resource budget.
 *  - The Standard calculator additionally uses [DecimalArithmetic] (BigDecimal)
 *    for plain +,-,*,/ so that textbook cases like 0.1 + 0.2 display as
 *    exactly "0.3" instead of a binary-floating-point artifact.
 *  - All domain errors (negative sqrt, log of non-positive, tan asymptote,
 *    asin/acos out of [-1,1], atanh at +-1, division by zero, negative
 *    factorial) throw a typed [AxMathError] instead of returning NaN/Infinity.
 */
object Evaluator {
    private const val FACTORIAL_LIMIT = 5000

    fun eval(expr: Expr, ctx: EvalContext): Double = when (expr) {
        is Expr.Num -> expr.value
        is Expr.Const -> constantValue(expr.name)
        is Expr.Neg -> -eval(expr.operand, ctx)
        is Expr.PercentOf -> eval(expr.operand, ctx) / 100.0
        is Expr.Factorial -> {
            val v = eval(expr.operand, ctx)
            if (v != floor(v) || v < 0) {
                throw AxMathError.Domain("Factorial requires a non-negative whole number.")
            }
            MathFunctions.factorial(v.toInt()).toDouble()
        }
        is Expr.BinOp -> evalBinOp(expr, ctx)
        is Expr.Call -> evalCall(expr, ctx)
    }

    private fun constantValue(name: String): Double = when (name) {
        "pi" -> PI
        "e" -> E
        else -> throw AxMathError.Syntax("Unknown constant '$name'.")
    }

    private fun evalBinOp(expr: Expr.BinOp, ctx: EvalContext): Double {
        val a = eval(expr.left, ctx)
        val b = eval(expr.right, ctx)
        return when (expr.op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> {
                if (b == 0.0) throw AxMathError.DivisionByZero()
                a / b
            }
            '^' -> {
                if (a < 0 && b != floor(b)) {
                    throw AxMathError.Domain("A fractional power of a negative base is undefined in real mode.")
                }
                val result = a.pow(b)
                if (result.isInfinite()) throw AxMathError.Overflow()
                if (result.isNaN()) throw AxMathError.Domain("The result of this exponentiation is undefined.")
                result
            }
            else -> throw AxMathError.Syntax("Unknown operator '${expr.op}'.")
        }
    }

    private fun toRadians(x: Double, ctx: EvalContext) = if (ctx.angleMode == AngleMode.DEGREES) Math.toRadians(x) else x
    private fun fromRadians(x: Double, ctx: EvalContext) = if (ctx.angleMode == AngleMode.DEGREES) Math.toDegrees(x) else x

    private fun evalCall(expr: Expr.Call, ctx: EvalContext): Double {
        val args = expr.args.map { eval(it, ctx) }
        fun arg(i: Int) = args[i]
        return when (expr.name) {
            "sin" -> sin(toRadians(arg(0), ctx))
            "cos" -> cos(toRadians(arg(0), ctx))
            "tan" -> {
                val rad = toRadians(arg(0), ctx)
                if (abs(cos(rad)) < 1e-12) throw AxMathError.Domain("Tangent is undefined at this angle.")
                tan(rad)
            }
            "asin" -> {
                if (arg(0) !in -1.0..1.0) throw AxMathError.Domain("asin requires an argument between -1 and 1.")
                fromRadians(asin(arg(0)), ctx)
            }
            "acos" -> {
                if (arg(0) !in -1.0..1.0) throw AxMathError.Domain("acos requires an argument between -1 and 1.")
                fromRadians(acos(arg(0)), ctx)
            }
            "atan" -> fromRadians(atan(arg(0)), ctx)
            "atan2" -> fromRadians(atan2(arg(0), arg(1)), ctx)
            "sinh" -> sinh(arg(0))
            "cosh" -> cosh(arg(0))
            "tanh" -> tanh(arg(0))
            "asinh" -> asinh(arg(0))
            "acosh" -> {
                if (arg(0) < 1.0) throw AxMathError.Domain("acosh requires an argument >= 1.")
                acosh(arg(0))
            }
            "atanh" -> {
                if (arg(0) <= -1.0 || arg(0) >= 1.0) {
                    throw AxMathError.Domain("atanh is undefined outside (-1, 1); it diverges at +/-1.")
                }
                atanh(arg(0))
            }
            "sqrt" -> {
                if (arg(0) < 0) throw AxMathError.Domain("Square root of a negative number is undefined in real mode.")
                sqrt(arg(0))
            }
            "cbrt" -> Math.cbrt(arg(0))
            "root" -> {
                val x = arg(0); val n = arg(1)
                if (n == 0.0) throw AxMathError.Domain("The root degree cannot be zero.")
                if (x < 0 && n % 2.0 == 0.0) throw AxMathError.Domain("An even root of a negative number is undefined in real mode.")
                val magnitude = abs(x).pow(1.0 / n)
                if (x < 0) -magnitude else magnitude
            }
            "abs" -> abs(arg(0))
            "floor" -> floor(arg(0))
            "ceil" -> ceil(arg(0))
            "round" -> Math.round(arg(0)).toDouble()
            "trunc" -> arg(0).toLong().toDouble()
            "sign" -> sign(arg(0))
            "ln" -> {
                if (arg(0) <= 0) throw AxMathError.Domain("The natural logarithm requires a positive argument.")
                ln(arg(0))
            }
            "log10" -> {
                if (arg(0) <= 0) throw AxMathError.Domain("The base-10 logarithm requires a positive argument.")
                log10(arg(0))
            }
            "log2" -> {
                if (arg(0) <= 0) throw AxMathError.Domain("The base-2 logarithm requires a positive argument.")
                ln(arg(0)) / ln(2.0)
            }
            "log" -> {
                if (args.size == 1) {
                    if (arg(0) <= 0) throw AxMathError.Domain("The logarithm requires a positive argument.")
                    log10(arg(0))
                } else {
                    val base = arg(1)
                    if (arg(0) <= 0 || base <= 0 || base == 1.0) {
                        throw AxMathError.Domain("The logarithm requires a positive argument and a valid base (> 0, != 1).")
                    }
                    ln(arg(0)) / ln(base)
                }
            }
            "exp" -> exp(arg(0))
            "fact" -> MathFunctions.factorial(arg(0).toInt()).toDouble()
            else -> throw AxMathError.Syntax("Unknown function '${expr.name}'.")
        }
    }
}

/** Exact integer combinatorics backed by [BigInteger] (no silent Double rounding). */
object MathFunctions {
    private const val FACTORIAL_LIMIT = 5000

    fun factorial(n: Int): BigInteger {
        if (n < 0) throw AxMathError.Domain("Factorial is undefined for negative numbers.")
        if (n > FACTORIAL_LIMIT) throw AxMathError.ResourceLimit("Factorial input exceeds the supported limit of $FACTORIAL_LIMIT.")
        var result = BigInteger.ONE
        for (k in 2..n) result = result.multiply(BigInteger.valueOf(k.toLong()))
        return result
    }

    fun permutations(n: Int, r: Int): BigInteger {
        if (r < 0 || n < 0 || r > n) throw AxMathError.Domain("Permutations require 0 <= r <= n.")
        return factorial(n).divide(factorial(n - r))
    }

    fun combinations(n: Int, r: Int): BigInteger {
        if (r < 0 || n < 0 || r > n) throw AxMathError.Domain("Combinations require 0 <= r <= n.")
        return factorial(n).divide(factorial(r).multiply(factorial(n - r)))
    }
}

/** Convenience entry point used by the Standard/Scientific view models. */
fun evaluateExpression(source: String, ctx: EvalContext): AxResult<Double> = axCatching {
    val ast = ExpressionParser.parse(source)
    Evaluator.eval(ast, ctx)
}
