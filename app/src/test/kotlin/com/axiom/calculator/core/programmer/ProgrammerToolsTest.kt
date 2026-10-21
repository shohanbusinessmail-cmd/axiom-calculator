package com.axiom.calculator.core.programmer

import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger

class ProgrammerToolsTest {
    @Test fun b001_decimalToBinary() = assertEquals("1010", ProgrammerTools.format(BigInteger.valueOf(10), NumberBase.BINARY))
    @Test fun b002_binaryToDecimal() = assertEquals(BigInteger.valueOf(10), ProgrammerTools.parse("1010", NumberBase.BINARY))
    @Test fun b003_hexToDecimal() = assertEquals(BigInteger.valueOf(255), ProgrammerTools.parse("FF", NumberBase.HEXADECIMAL))
    @Test fun b004_and() = assertEquals(BigInteger.valueOf(2), ProgrammerTools.and(BigInteger.valueOf(6), BigInteger.valueOf(3), BitWidth.W8))
    @Test fun b005_or() = assertEquals(BigInteger.valueOf(7), ProgrammerTools.or(BigInteger.valueOf(6), BigInteger.valueOf(3), BitWidth.W8))
    @Test fun b006_xor() = assertEquals(BigInteger.valueOf(5), ProgrammerTools.xor(BigInteger.valueOf(6), BigInteger.valueOf(3), BitWidth.W8))
    @Test fun b007_shiftLeft() = assertEquals(BigInteger.valueOf(8), ProgrammerTools.shiftLeft(BigInteger.valueOf(1), 3, BitWidth.W8))

    @Test(expected = com.axiom.calculator.core.math.AxMathError.InvalidInput::class)
    fun b008_invalidDigitForBase() { ProgrammerTools.parse("2", NumberBase.BINARY) }

    @Test
    fun notIsBitWidthExplicit() {
        // NOT of 0 at 8-bit width is -1 in two's complement (all bits set, interpreted as signed).
        val result = ProgrammerTools.not(BigInteger.ZERO, BitWidth.W8)
        assertEquals(BigInteger.valueOf(-1), result)
    }

    @Test
    fun baseRoundTrip() {
        val original = BigInteger.valueOf(987654321L)
        val hex = ProgrammerTools.format(original, NumberBase.HEXADECIMAL)
        val back = ProgrammerTools.parse(hex, NumberBase.HEXADECIMAL)
        assertEquals(original, back)
    }

    @Test
    fun arithmeticVsLogicalShiftDiffer() {
        val negative = BigInteger.valueOf(-8)
        val logical = ProgrammerTools.shiftRightLogical(negative, 1, BitWidth.W8)
        val arithmetic = ProgrammerTools.shiftRightArithmetic(negative, 1, BitWidth.W8)
        assertEquals(BigInteger.valueOf(-4), arithmetic) // sign-preserving
        assertNotEquals(arithmetic, logical) // logical treats the pattern as unsigned
    }
}
