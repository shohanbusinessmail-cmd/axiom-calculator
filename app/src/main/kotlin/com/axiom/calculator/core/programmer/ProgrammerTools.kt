package com.axiom.calculator.core.programmer

import com.axiom.calculator.core.math.AxMathError
import java.math.BigInteger

enum class NumberBase(val radix: Int, val label: String) {
    BINARY(2, "Binary"), OCTAL(8, "Octal"), DECIMAL(10, "Decimal"), HEXADECIMAL(16, "Hexadecimal")
}

enum class BitWidth(val bits: Int) { W8(8), W16(16), W32(32), W64(64) }

/**
 * Exact-integer programmer calculator. All operations work on [BigInteger]
 * internally and are then masked/interpreted at an explicit [BitWidth], so
 * large values are never silently coerced to floating point (SECTION 11).
 */
object ProgrammerTools {

    fun parse(text: String, base: NumberBase): BigInteger {
        if (text.isBlank()) throw AxMathError.InvalidInput("Enter a value to convert.")
        val validChars = "0123456789ABCDEF".substring(0, base.radix)
        val isNegative = text.startsWith("-")
        val body = if (isNegative) text.substring(1) else text
        if (body.isEmpty() || body.uppercase().any { it !in validChars }) {
            throw AxMathError.InvalidInput("'$text' contains a digit that is invalid for ${base.label} (base ${base.radix}).")
        }
        val magnitude = BigInteger(body, base.radix)
        return if (isNegative) magnitude.negate() else magnitude
    }

    fun format(value: BigInteger, base: NumberBase): String = value.toString(base.radix).uppercase()

    private fun mask(width: BitWidth): BigInteger = BigInteger.ONE.shiftLeft(width.bits).subtract(BigInteger.ONE)

    /** Wraps a signed value into its unsigned two's-complement representation at the given width. */
    fun toUnsignedTwosComplement(value: BigInteger, width: BitWidth): BigInteger {
        val m = mask(width)
        return value.and(m)
    }

    /** Interprets an unsigned bit pattern as a signed two's-complement value at the given width. */
    fun toSigned(unsignedValue: BigInteger, width: BitWidth): BigInteger {
        val signBit = BigInteger.ONE.shiftLeft(width.bits - 1)
        return if (unsignedValue.and(signBit) != BigInteger.ZERO) {
            unsignedValue.subtract(BigInteger.ONE.shiftLeft(width.bits))
        } else unsignedValue
    }

    fun and(a: BigInteger, b: BigInteger, width: BitWidth) = toUnsignedTwosComplement(a, width).and(toUnsignedTwosComplement(b, width)).let { toSigned(it, width) }
    fun or(a: BigInteger, b: BigInteger, width: BitWidth) = toUnsignedTwosComplement(a, width).or(toUnsignedTwosComplement(b, width)).let { toSigned(it, width) }
    fun xor(a: BigInteger, b: BigInteger, width: BitWidth) = toUnsignedTwosComplement(a, width).xor(toUnsignedTwosComplement(b, width)).let { toSigned(it, width) }
    fun not(a: BigInteger, width: BitWidth) = toUnsignedTwosComplement(a, width).not().let { toUnsignedTwosComplement(it, width) }.let { toSigned(it, width) }

    fun shiftLeft(a: BigInteger, bits: Int, width: BitWidth): BigInteger {
        if (bits < 0) throw AxMathError.InvalidInput("Shift amount must be non-negative.")
        val result = toUnsignedTwosComplement(a, width).shiftLeft(bits)
        return toSigned(toUnsignedTwosComplement(result, width), width)
    }

    /** Logical (unsigned) right shift: fills with zero bits regardless of sign. */
    fun shiftRightLogical(a: BigInteger, bits: Int, width: BitWidth): BigInteger {
        if (bits < 0) throw AxMathError.InvalidInput("Shift amount must be non-negative.")
        val unsigned = toUnsignedTwosComplement(a, width)
        return toSigned(unsigned.shiftRight(bits), width)
    }

    /** Arithmetic right shift: sign-extends, matching the mathematical value's sign. */
    fun shiftRightArithmetic(a: BigInteger, bits: Int, width: BitWidth): BigInteger {
        if (bits < 0) throw AxMathError.InvalidInput("Shift amount must be non-negative.")
        return a.shiftRight(bits)
    }

    fun bitLength(a: BigInteger): Int = a.abs().bitLength()
}
