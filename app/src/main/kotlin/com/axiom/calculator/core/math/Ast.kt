package com.axiom.calculator.core.math

/** Expression AST produced by [ExpressionParser]. */
sealed class Expr {
    data class Num(val value: Double) : Expr()
    data class Const(val name: String) : Expr()
    data class Neg(val operand: Expr) : Expr()
    data class Factorial(val operand: Expr) : Expr()
    data class PercentOf(val operand: Expr) : Expr()
    data class BinOp(val op: Char, val left: Expr, val right: Expr) : Expr() // + - * / ^
    data class Call(val name: String, val args: List<Expr>) : Expr()
}
