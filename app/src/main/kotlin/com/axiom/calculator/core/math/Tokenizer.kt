package com.axiom.calculator.core.math

enum class TokenKind {
    NUMBER, IDENT, PLUS, MINUS, STAR, SLASH, CARET, BANG, PERCENT,
    LPAREN, RPAREN, COMMA, SUP2, SUP3, END
}

data class Token(val kind: TokenKind, val text: String, val value: Double = 0.0)

/**
 * Converts a raw expression string into a flat token stream.
 *
 * Supports: decimal literals, scientific notation (1.2e-3), identifiers
 * (function/constant names), the operators + - * / ^ ! %, parentheses,
 * argument commas, and the superscript glyphs ² ³ as sugar for ^2 / ^3.
 *
 * Resource limits (SECTION 8): expressions longer than [MAX_EXPRESSION_LENGTH]
 * or with parenthesis nesting deeper than [MAX_NESTING] are rejected before
 * parsing, protecting the evaluator from pathological input.
 */
object Tokenizer {
    const val MAX_EXPRESSION_LENGTH = 500
    const val MAX_NESTING = 64

    fun tokenize(source: String): List<Token> {
        if (source.length > MAX_EXPRESSION_LENGTH) {
            throw AxMathError.ResourceLimit("Expression exceeds the maximum supported length of $MAX_EXPRESSION_LENGTH characters.")
        }
        val tokens = mutableListOf<Token>()
        var i = 0
        var depth = 0
        val n = source.length
        while (i < n) {
            val c = source[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || (c == '.' && i + 1 < n && source[i + 1].isDigit()) -> {
                    var j = i
                    var seenDot = false
                    while (j < n && (source[j].isDigit() || (source[j] == '.' && !seenDot))) {
                        if (source[j] == '.') seenDot = true
                        j++
                    }
                    if (j < n && (source[j] == 'e' || source[j] == 'E')) {
                        var k = j + 1
                        if (k < n && (source[k] == '+' || source[k] == '-')) k++
                        val digitsStart = k
                        while (k < n && source[k].isDigit()) k++
                        if (k > digitsStart) j = k
                    }
                    val text = source.substring(i, j)
                    tokens += Token(TokenKind.NUMBER, text, text.toDouble())
                    i = j
                }
                c.isLetter() || c == '_' -> {
                    var j = i
                    while (j < n && (source[j].isLetterOrDigit() || source[j] == '_')) j++
                    tokens += Token(TokenKind.IDENT, source.substring(i, j))
                    i = j
                }
                c == '²' -> { tokens += Token(TokenKind.SUP2, "²"); i++ }
                c == '³' -> { tokens += Token(TokenKind.SUP3, "³"); i++ }
                c == '+' -> { tokens += Token(TokenKind.PLUS, "+"); i++ }
                c == '-' || c == '\u2212' -> { tokens += Token(TokenKind.MINUS, "-"); i++ }
                c == '*' || c == '\u00D7' -> { tokens += Token(TokenKind.STAR, "*"); i++ }
                c == '/' || c == '\u00F7' -> { tokens += Token(TokenKind.SLASH, "/"); i++ }
                c == '^' -> { tokens += Token(TokenKind.CARET, "^"); i++ }
                c == '!' -> { tokens += Token(TokenKind.BANG, "!"); i++ }
                c == '%' -> { tokens += Token(TokenKind.PERCENT, "%"); i++ }
                c == '(' -> {
                    depth++
                    if (depth > MAX_NESTING) {
                        throw AxMathError.ResourceLimit("Expression nesting exceeds the supported limit of $MAX_NESTING.")
                    }
                    tokens += Token(TokenKind.LPAREN, "(")
                    i++
                }
                c == ')' -> { depth--; tokens += Token(TokenKind.RPAREN, ")"); i++ }
                c == ',' -> { tokens += Token(TokenKind.COMMA, ","); i++ }
                else -> throw AxMathError.Syntax("Unexpected character '$c' in the expression.")
            }
        }
        if (depth != 0) throw AxMathError.Syntax("Mismatched parentheses: a closing ')' is missing.")
        tokens += Token(TokenKind.END, "")
        return tokens
    }
}
