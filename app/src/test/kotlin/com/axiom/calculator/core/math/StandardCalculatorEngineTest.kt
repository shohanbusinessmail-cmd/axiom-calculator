package com.axiom.calculator.core.math

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class StandardCalculatorEngineTest {

    @Test
    fun m005_decimalAdditionAvoidsBinaryArtifact() {
        val result = BigDecimalEvaluator.eval(ExpressionParser.parse("0.1 + 0.2"))
        assertEquals(0, result.compareTo(BigDecimal("0.3")))
    }

    @Test
    fun editing_digitsAppend() {
        var expr = ""
        expr = StandardCalculatorEngine.appendDigit(expr, '1')
        expr = StandardCalculatorEngine.appendDigit(expr, '2')
        assertEquals("12", expr)
    }

    @Test
    fun editing_leadingZeroReplaced() {
        val expr = StandardCalculatorEngine.appendDigit("0", '5')
        assertEquals("5", expr)
    }

    @Test
    fun editing_operatorReplacesTrailingOperator() {
        val expr = StandardCalculatorEngine.appendOperator("5+", '-')
        assertEquals("5-", expr)
    }

    @Test
    fun editing_backspaceRemovesLastChar() {
        assertEquals("12", StandardCalculatorEngine.backspace("123"))
    }

    @Test
    fun editing_toggleSignWrapsInParens() {
        val expr = StandardCalculatorEngine.toggleSignOfLastToken("3+5")
        assertEquals("3+(-5)", expr)
    }

    @Test
    fun standardCalculator_divisionByZeroIsTypedError() {
        val result = StandardCalculatorEngine.evaluate("5/0")
        assertTrue(result is AxResult.Failure)
        assertTrue((result as AxResult.Failure).error is AxMathError.DivisionByZero)
    }

    @Test
    fun standardCalculator_scientificFunctionRejected() {
        val result = StandardCalculatorEngine.evaluate("sin(30)")
        assertTrue(result is AxResult.Failure)
        assertTrue((result as AxResult.Failure).error is AxMathError.Unsupported)
    }
}
