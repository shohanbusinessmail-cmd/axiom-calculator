package com.axiom.calculator.core.math

import java.math.BigDecimal

/**
 * Pure, Android-free expression-editing logic for the Standard calculator
 * (SECTION 6). Operates purely on the textual expression so the full
 * parenthesized-expression grammar in [ExpressionParser] is reusable, while
 * keeping the editing rules (what happens when you tap "+" twice, etc.)
 * centralized and unit-testable without any UI.
 */
object StandardCalculatorEngine {
    private val trailingNumberRegex = Regex("(-?\\d*\\.?\\d+)$")
    private val trailingOperatorRegex = Regex("[+\\-*/]$")

    fun appendDigit(expression: String, digit: Char): String {
        if (expression == "0") return digit.toString()
        return expression + digit
    }

    fun appendDecimalPoint(expression: String): String {
        // Find the trailing numeric token; only allow one '.' per token.
        val match = trailingNumberRegex.find(expression)
        val currentToken = match?.value ?: ""
        if (currentToken.contains('.')) return expression
        return if (expression.isEmpty() || trailingOperatorRegex.containsMatchIn(expression.takeLast(1))) {
            expression + "0."
        } else {
            expression + "."
        }
    }

    fun appendOperator(expression: String, op: Char): String {
        if (expression.isEmpty()) {
            // Allow a leading unary minus; reject leading *, /, +.
            return if (op == '-') "-" else expression
        }
        return if (trailingOperatorRegex.containsMatchIn(expression.takeLast(1))) {
            // Replace a trailing operator rather than stacking "5+-".
            expression.dropLast(1) + op
        } else {
            expression + op
        }
    }

    fun appendOpenParen(expression: String): String {
        return if (expression.isEmpty() || trailingOperatorRegex.containsMatchIn(expression.takeLast(1)) || expression.endsWith("(")) {
            expression + "("
        } else {
            // implicit multiplication before a new group, e.g. "2(" -> "2*("
            "$expression*("
        }
    }

    fun appendCloseParen(expression: String): String {
        val opens = expression.count { it == '(' }
        val closes = expression.count { it == ')' }
        return if (opens > closes && expression.isNotEmpty() && !trailingOperatorRegex.containsMatchIn(expression.takeLast(1)) && !expression.endsWith("(")) {
            expression + ")"
        } else expression
    }

    fun appendPercent(expression: String): String {
        if (expression.isEmpty()) return expression
        return expression + "%"
    }

    fun backspace(expression: String): String = if (expression.isEmpty()) expression else expression.dropLast(1)

    fun clearAll(): String = ""

    fun toggleSignOfLastToken(expression: String): String {
        val match = trailingNumberRegex.find(expression) ?: return expression
        val token = match.value
        val start = match.range.first
        val prefix = expression.substring(0, start)
        val negated = if (token.startsWith("-")) token.substring(1) else "-$token"
        return if (negated.startsWith("-") && prefix.isNotEmpty() && !trailingOperatorRegex.containsMatchIn(prefix.takeLast(1)) && !prefix.endsWith("(")) {
            // e.g. "3" + toggled "-5" after an operator needs wrapping in parens to stay valid: 3+(-5)
            "$prefix($negated)"
        } else {
            prefix + negated
        }
    }

    fun evaluate(expression: String): AxResult<BigDecimal> = evaluateStandardExpression(expression)
}
