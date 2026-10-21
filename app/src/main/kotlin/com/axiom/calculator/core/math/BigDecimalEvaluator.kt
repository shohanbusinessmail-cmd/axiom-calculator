package com.axiom.calculator.core.math

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Evaluates the SAME [Expr] AST as [Evaluator], but using [DecimalArithmetic]
 * (BigDecimal) for +, -, *, / and percent so the Standard calculator never
 * exhibits binary floating-point artifacts (e.g. 0.1 + 0.2 must display
 * exactly as "0.3" — M005). Scientific function calls (sin, log, ...) are not
 * meaningful in exact decimal arithmetic and are rejected with a clear
 * [AxMathError.Unsupported]; the Scientific tab uses [Evaluator]/Double
 * instead. Exponentiation is supported only for non-negative integer
 * exponents, which covers every Standard-calculator use case (x^2, x^3, ...).
 */
object BigDecimalEvaluator {

    fun eval(expr: Expr): BigDecimal = when (expr) {
        is Expr.Num -> BigDecimal(expr.value.toString())
        is Expr.Const -> when (expr.name) {
            "pi" -> BigDecimal(Math.PI)
            "e" -> BigDecimal(Math.E)
            else -> throw AxMathError.Syntax("Unknown constant '${expr.name}'.")
        }
        is Expr.Neg -> eval(expr.operand).negate()
        is Expr.PercentOf -> DecimalArithmetic.percentOf(eval(expr.operand))
        is Expr.Factorial -> {
            val v = eval(expr.operand)
            if (v.stripTrailingZeros().scale() > 0 || v.signum() < 0) {
                throw AxMathError.Domain("Factorial requires a non-negative whole number.")
            }
            BigDecimal(MathFunctions.factorial(v.toInt()))
        }
        is Expr.BinOp -> evalBinOp(expr)
        is Expr.Call -> throw AxMathError.Unsupported("Scientific functions are available in the Scientific calculator, not Standard.")
    }

    private fun evalBinOp(expr: Expr.BinOp): BigDecimal {
        val a = eval(expr.left)
        val b = eval(expr.right)
        return when (expr.op) {
            '+' -> DecimalArithmetic.add(a, b)
            '-' -> DecimalArithmetic.subtract(a, b)
            '*' -> DecimalArithmetic.multiply(a, b)
            '/' -> DecimalArithmetic.divide(a, b)
            '^' -> {
                if (b.stripTrailingZeros().scale() > 0) {
                    throw AxMathError.Unsupported("Fractional exponents require the Scientific calculator.")
                }
                val exponent = b.toBigIntegerExact()
                if (exponent.signum() < 0) {
                    BigDecimal.ONE.divide(a.pow(exponent.negate().toInt(), DecimalArithmetic.CONTEXT), DecimalArithmetic.CONTEXT)
                } else {
                    if (exponent > BigInteger.valueOf(10_000)) throw AxMathError.ResourceLimit("Exponent is too large.")
                    a.pow(exponent.toInt(), DecimalArithmetic.CONTEXT)
                }
            }
            else -> throw AxMathError.Syntax("Unknown operator '${expr.op}'.")
        }
    }
}

fun evaluateStandardExpression(source: String): AxResult<BigDecimal> = axCatching {
    val ast = ExpressionParser.parse(source)
    BigDecimalEvaluator.eval(ast)
}
