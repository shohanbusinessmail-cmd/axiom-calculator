package com.axiom.calculator.core.solve

import com.axiom.calculator.core.math.AxMathError
import kotlin.math.abs

/** Immutable dense matrix of Doubles with row-major storage. */
class AxMatrix(val rows: Int, val cols: Int, private val data: DoubleArray) {
    companion object {
        fun of(vararg rowsIn: DoubleArray): AxMatrix {
            val r = rowsIn.size
            val c = if (r == 0) 0 else rowsIn[0].size
            require(rowsIn.all { it.size == c }) { "All matrix rows must have the same length." }
            val flat = DoubleArray(r * c)
            for (i in 0 until r) System.arraycopy(rowsIn[i], 0, flat, i * c, c)
            return AxMatrix(r, c, flat)
        }

        fun identity(n: Int): AxMatrix {
            val flat = DoubleArray(n * n)
            for (i in 0 until n) flat[i * n + i] = 1.0
            return AxMatrix(n, n, flat)
        }

        fun zero(rows: Int, cols: Int): AxMatrix = AxMatrix(rows, cols, DoubleArray(rows * cols))
    }

    operator fun get(r: Int, c: Int): Double = data[r * cols + c]
    private operator fun set(r: Int, c: Int, value: Double) { data[r * cols + c] = value }

    fun toRows(): List<DoubleArray> = (0 until rows).map { r -> DoubleArray(cols) { c -> this[r, c] } }

    operator fun plus(o: AxMatrix): AxMatrix {
        if (rows != o.rows || cols != o.cols) throw AxMathError.InvalidInput("Matrix dimensions do not match for addition.")
        return AxMatrix(rows, cols, DoubleArray(data.size) { data[it] + o.data[it] })
    }

    operator fun minus(o: AxMatrix): AxMatrix {
        if (rows != o.rows || cols != o.cols) throw AxMathError.InvalidInput("Matrix dimensions do not match for subtraction.")
        return AxMatrix(rows, cols, DoubleArray(data.size) { data[it] - o.data[it] })
    }

    operator fun times(scalar: Double): AxMatrix = AxMatrix(rows, cols, DoubleArray(data.size) { data[it] * scalar })

    operator fun times(o: AxMatrix): AxMatrix {
        if (cols != o.rows) throw AxMathError.InvalidInput("Matrix dimensions are incompatible for multiplication (${rows}x$cols times ${o.rows}x${o.cols}).")
        val result = zero(rows, o.cols)
        for (i in 0 until rows) {
            for (j in 0 until o.cols) {
                var sum = 0.0
                for (k in 0 until cols) sum += this[i, k] * o[k, j]
                result[i, j] = sum
            }
        }
        return result
    }

    fun transpose(): AxMatrix {
        val result = zero(cols, rows)
        for (i in 0 until rows) for (j in 0 until cols) result[j, i] = this[i, j]
        return result
    }

    fun determinant(): Double {
        if (rows != cols) throw AxMathError.InvalidInput("Determinant is only defined for a square matrix.")
        val n = rows
        val m = toRows().map { it.copyOf() }.toTypedArray()
        var det = 1.0
        for (col in 0 until n) {
            var pivot = col
            for (r in col until n) if (abs(m[r][col]) > abs(m[pivot][col])) pivot = r
            if (abs(m[pivot][col]) < 1e-12) return 0.0
            if (pivot != col) { val tmp = m[col]; m[col] = m[pivot]; m[pivot] = tmp; det = -det }
            det *= m[col][col]
            for (r in col + 1 until n) {
                val factor = m[r][col] / m[col][col]
                for (c in col until n) m[r][c] -= factor * m[col][c]
            }
        }
        return det
    }

    fun inverse(): AxMatrix {
        if (rows != cols) throw AxMathError.InvalidInput("Only a square matrix can be inverted.")
        val n = rows
        val aug = Array(n) { r -> DoubleArray(2 * n) { c -> if (c < n) this[r, c] else if (c - n == r) 1.0 else 0.0 } }
        for (col in 0 until n) {
            var pivot = col
            for (r in col until n) if (abs(aug[r][col]) > abs(aug[pivot][col])) pivot = r
            if (abs(aug[pivot][col]) < 1e-12) throw AxMathError.SingularMatrix()
            val tmp = aug[col]; aug[col] = aug[pivot]; aug[pivot] = tmp
            val pv = aug[col][col]
            for (c in 0 until 2 * n) aug[col][c] /= pv
            for (r in 0 until n) {
                if (r == col) continue
                val factor = aug[r][col]
                for (c in 0 until 2 * n) aug[r][c] -= factor * aug[col][c]
            }
        }
        val flat = DoubleArray(n * n)
        for (r in 0 until n) for (c in 0 until n) flat[r * n + c] = aug[r][c + n]
        return AxMatrix(n, n, flat)
    }

    /** Rank via Gaussian elimination row-echelon reduction with a numeric tolerance. */
    fun rank(tolerance: Double = 1e-9): Int {
        val m = toRows().map { it.copyOf() }.toMutableList()
        var rank = 0
        for (col in 0 until cols) {
            var pivotRow = -1
            for (r in rank until rows) if (abs(m[r][col]) > tolerance) { pivotRow = r; break }
            if (pivotRow == -1) continue
            val tmp = m[rank]; m[rank] = m[pivotRow]; m[pivotRow] = tmp
            for (r in 0 until rows) {
                if (r == rank) continue
                val factor = m[r][col] / m[rank][col]
                for (c in col until cols) m[r][c] -= factor * m[rank][c]
            }
            rank++
            if (rank == rows) break
        }
        return rank
    }
}
