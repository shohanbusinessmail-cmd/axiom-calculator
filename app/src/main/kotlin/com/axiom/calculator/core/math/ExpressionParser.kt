package com.axiom.calculator.core.math

/**
 * Recursive-descent / precedence-climbing parser for the Axiom expression
 * grammar. This is a structural port of the Python reference implementation
 * in scripts/verification/engine_reference.py, which was executed against
 * all SECTION 17 mandatory vectors (see docs/MATHEMATICAL_SEMANTICS.md and
 * docs/QUALITY_REPORT.md for the evidence record).
 *
 * Grammar (lowest to highest precedence):
 * ```
 * expr    := addsub
 * addsub  := muldiv (('+'|'-') muldiv)*
 * muldiv  := unary ( ('*'|'/'|implicit) unary )*
 * unary   := ('+'|'-') unary | power
 * power   := postfix ('^' unary)?                 // right-associative
 * postfix := primary ('!' | '%' | '²' | '³')*
 * primary := NUMBER | CONST | FUNC '(' args ')' | '(' expr ')'
 * ```
 *
 * Documented ambiguity resolutions (SECTION 8):
 *  - "2^3^2"  = 2^(3^2) = 512          (right-associative exponentiation)
 *  - "-2^2"   = -(2^2) = -4            (unary minus binds looser than ^)
 *  - "(-2)^2" = 4                      (parentheses force the base to be -2)
 *  - "2(3+4)" = 14                     (implicit multiplication, same precedence as '*')
 *  - "1/2x"   = (1/2)*x, i.e. x/2      (implicit multiplication is left-to-right at '*' precedence,
 *                                        NOT given higher precedence than the preceding '/')
 */
class ExpressionParser(private val tokens: List<Token>) {
    private var pos = 0

    companion object {
        val KNOWN_FUNCTIONS = setOf(
            "sin", "cos", "tan", "asin", "acos", "atan", "atan2",
            "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
            "sqrt", "cbrt", "root", "abs", "floor", "ceil", "round", "trunc",
            "ln", "log10", "log2", "log", "exp", "fact", "sign"
        )
        val KNOWN_CONSTANTS = setOf("pi", "e")

        fun parse(source: String): Expr {
            val tokens = Tokenizer.tokenize(source)
            val parser = ExpressionParser(tokens)
            val expr = parser.parseAddSub()
            parser.expect(TokenKind.END, "Unexpected trailing input.")
            return expr
        }
    }

    private fun peek(): Token = tokens[pos]
    private fun advance(): Token = tokens[pos++]

    private fun expect(kind: TokenKind, message: String): Token {
        if (peek().kind != kind) throw AxMathError.Syntax(message)
        return advance()
    }

    private fun parseAddSub(): Expr {
        if (peek().kind == TokenKind.END) {
            throw AxMathError.Syntax("The expression is empty.")
        }
        var node = parseMulDiv()
        while (peek().kind == TokenKind.PLUS || peek().kind == TokenKind.MINUS) {
            val op = if (advance().kind == TokenKind.PLUS) '+' else '-'
            val rhs = parseMulDiv()
            node = Expr.BinOp(op, node, rhs)
        }
        return node
    }

    private fun startsImplicitFactor(): Boolean = peek().kind in setOf(
        TokenKind.NUMBER, TokenKind.LPAREN, TokenKind.IDENT
    )

    private fun parseMulDiv(): Expr {
        var node = parseUnary()
        while (true) {
            when {
                peek().kind == TokenKind.STAR -> { advance(); node = Expr.BinOp('*', node, parseUnary()) }
                peek().kind == TokenKind.SLASH -> { advance(); node = Expr.BinOp('/', node, parseUnary()) }
                startsImplicitFactor() -> node = Expr.BinOp('*', node, parseUnary())
                else -> return node
            }
        }
    }

    private fun parseUnary(): Expr {
        if (peek().kind == TokenKind.PLUS) { advance(); return parseUnary() }
        if (peek().kind == TokenKind.MINUS) { advance(); return Expr.Neg(parseUnary()) }
        return parsePower()
    }

    private fun parsePower(): Expr {
        val base = parsePostfix()
        if (peek().kind == TokenKind.CARET) {
            advance()
            val exponent = parseUnary() // right-associative via recursive call
            return Expr.BinOp('^', base, exponent)
        }
        return base
    }

    private fun parsePostfix(): Expr {
        var node = parsePrimary()
        loop@ while (true) {
            when (peek().kind) {
                TokenKind.BANG -> { advance(); node = Expr.Factorial(node) }
                TokenKind.PERCENT -> { advance(); node = Expr.PercentOf(node) }
                TokenKind.SUP2 -> { advance(); node = Expr.BinOp('^', node, Expr.Num(2.0)) }
                TokenKind.SUP3 -> { advance(); node = Expr.BinOp('^', node, Expr.Num(3.0)) }
                else -> break@loop
            }
        }
        return node
    }

    private fun parsePrimary(): Expr {
        val t = peek()
        return when (t.kind) {
            TokenKind.NUMBER -> { advance(); Expr.Num(t.value) }
            TokenKind.LPAREN -> {
                advance()
                val inner = parseAddSub()
                expect(TokenKind.RPAREN, "Missing a closing ')'.")
                inner
            }
            TokenKind.IDENT -> {
                val name = t.text
                advance()
                if (peek().kind == TokenKind.LPAREN) {
                    advance()
                    val args = mutableListOf(parseAddSub())
                    while (peek().kind == TokenKind.COMMA) {
                        advance()
                        args += parseAddSub()
                    }
                    expect(TokenKind.RPAREN, "Missing a closing ')' in the call to '$name'.")
                    if (name !in KNOWN_FUNCTIONS) {
                        throw AxMathError.Syntax("Unknown function '$name'.")
                    }
                    Expr.Call(name, args)
                } else if (name in KNOWN_CONSTANTS) {
                    Expr.Const(name)
                } else {
                    throw AxMathError.Syntax("Unknown identifier '$name'.")
                }
            }
            TokenKind.END -> throw AxMathError.Syntax("The expression ended unexpectedly.")
            else -> throw AxMathError.Syntax("Unexpected token '${t.text}'.")
        }
    }
}
