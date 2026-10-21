package com.axiom.calculator.core.solve

import com.axiom.calculator.core.math.AxMathError

/**
 * A deliberately bounded parser for single-variable linear/quadratic
 * equations written in canonical textbook form, e.g.:
 *   "2x + 3 = 11"
 *   "x^2 - 5x + 6 = 0"
 *   "x² + 1 = 0"
 *
 * This is NOT a general symbolic algebra engine. It only recognizes a sum of
 * signed terms of the form `<coefficient>`, `<coefficient>x`, or
 * `<coefficient>x^2` (or the superscript `x²`) on each side of a single `=`.
 * Nested expressions, implicit grouping, or degrees above 2 are rejected
 * with an explicit [AxMathError.Unsupported] rather than silently
 * misinterpreted, per the "do not claim symbolic algebra beyond the actual
 * tested implementation" requirement in SECTION 7.
 */
object SingleVariableEquationParser {

    data class Polynomial(val a: Double, val b: Double, val c: Double)

    /** Parses "<lhs> = <rhs>" and returns the normalized a*x^2 + b*x + c = 0 form. */
    fun parseEquation(input: String): Polynomial {
        val normalized = input.replace("²", "^2").replace("X", "x")
        val sides = normalized.split("=")
        if (sides.size != 2) {
            throw AxMathError.Syntax("An equation must contain exactly one '=' sign.")
        }
        val left = parseSide(sides[0])
        val right = parseSide(sides[1])
        return Polynomial(left.a - right.a, left.b - right.b, left.c - right.c)
    }

    private val termRegex = Regex("([+-])([^+-]*)")

    private fun parseSide(raw: String): Polynomial {
        var t = raw.replace(" ", "").replace("*", "")
        if (t.isEmpty()) throw AxMathError.Syntax("One side of the equation is empty.")
        if (t[0] != '+' && t[0] != '-') t = "+$t"

        var a = 0.0; var b = 0.0; var c = 0.0
        var matchedLength = 0
        for (m in termRegex.findAll(t)) {
            matchedLength += m.value.length
            val sign = if (m.groupValues[1] == "-") -1.0 else 1.0
            val term = m.groupValues[2]
            if (term.isEmpty()) continue
            if (term.contains("x")) {
                val parts = term.split("x", limit = 2)
                val coeffStr = parts[0]
                val coeff = if (coeffStr.isEmpty()) 1.0 else coeffStr.toDoubleOrNull()
                    ?: throw AxMathError.Syntax("Could not parse the coefficient in term '$term'.")
                when (val rest = parts.getOrElse(1) { "" }) {
                    "" -> b += sign * coeff
                    "^2" -> a += sign * coeff
                    else -> throw AxMathError.Unsupported(
                        "Only linear and quadratic single-variable equations are supported (found 'x$rest')."
                    )
                }
            } else {
                val value = term.toDoubleOrNull()
                    ?: throw AxMathError.Syntax("Could not parse the number '$term'.")
                c += sign * value
            }
        }
        if (matchedLength != t.length) {
            throw AxMathError.Syntax("Could not fully parse the expression '$raw'.")
        }
        return Polynomial(a, b, c)
    }
}
