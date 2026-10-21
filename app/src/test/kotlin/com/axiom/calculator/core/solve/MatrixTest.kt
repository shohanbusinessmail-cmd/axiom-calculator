package com.axiom.calculator.core.solve

import org.junit.Assert.*
import org.junit.Test

class MatrixTest {
    private val a = AxMatrix.of(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0))

    @Test
    fun transpose() {
        val t = a.transpose().toRows()
        assertArrayEquals(doubleArrayOf(1.0, 3.0), t[0], 0.0)
        assertArrayEquals(doubleArrayOf(2.0, 4.0), t[1], 0.0)
    }

    @Test fun determinant() = assertEquals(-2.0, a.determinant(), 1e-9)

    @Test
    fun inverse() {
        val inv = a.inverse().toRows()
        assertArrayEquals(doubleArrayOf(-2.0, 1.0), inv[0], 1e-9)
        assertArrayEquals(doubleArrayOf(1.5, -0.5), inv[1], 1e-9)
    }

    @Test
    fun multiplication() {
        val r = (a * a).toRows()
        assertArrayEquals(doubleArrayOf(7.0, 10.0), r[0], 1e-9)
        assertArrayEquals(doubleArrayOf(15.0, 22.0), r[1], 1e-9)
    }

    @Test(expected = com.axiom.calculator.core.math.AxMathError.SingularMatrix::class)
    fun singularMatrixThrowsOnInverse() {
        AxMatrix.of(doubleArrayOf(1.0, 1.0), doubleArrayOf(2.0, 2.0)).inverse()
    }

    @Test
    fun identityMultiplicationIsNoOp() {
        val id = AxMatrix.identity(2)
        val r = (a * id).toRows()
        assertArrayEquals(doubleArrayOf(1.0, 2.0), r[0], 1e-9)
        assertArrayEquals(doubleArrayOf(3.0, 4.0), r[1], 1e-9)
    }

    @Test(expected = com.axiom.calculator.core.math.AxMathError::class)
    fun incompatibleDimensionsThrow() {
        // a is 2x2; a 1x3 matrix cannot be right-multiplied against it (2 != 1).
        val incompatible = AxMatrix.of(doubleArrayOf(1.0, 2.0, 3.0))
        a * incompatible
    }
}
