package com.axiom.calculator.core.solve

import com.axiom.calculator.core.math.AxMathError
import kotlin.math.sqrt

/**
 * Quartile convention: inclusive median method (also called Tukey's hinges) —
 * the median splits the sorted dataset in half (excluding the overall median
 * for odd n), and Q1/Q3 are the medians of the lower/upper halves. This is
 * documented explicitly because multiple valid quartile conventions exist.
 */
data class StatisticsSummary(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val min: Double,
    val max: Double,
    val range: Double,
    val populationVariance: Double,
    val populationStdDev: Double,
    val sampleVariance: Double?,
    val sampleStdDev: Double?,
    val modes: List<Double>,
    val q1: Double,
    val q3: Double
)

object Statistics {
    fun summarize(data: List<Double>): StatisticsSummary {
        if (data.isEmpty()) throw AxMathError.InvalidInput("Statistics require a non-empty dataset.")
        val n = data.size
        val sum = data.sum()
        val mean = sum / n
        val sorted = data.sorted()
        val median = median(sorted)

        // Numerically stable two-pass variance (mean computed first).
        val popVar = data.sumOf { (it - mean) * (it - mean) } / n
        val sampleVar = if (n > 1) data.sumOf { (it - mean) * (it - mean) } / (n - 1) else null

        val counts = data.groupingBy { it }.eachCount()
        val maxCount = counts.values.max()
        val modes = if (maxCount > 1) counts.filterValues { it == maxCount }.keys.sorted() else emptyList()

        val lowerHalf: List<Double>
        val upperHalf: List<Double>
        if (n % 2 == 0) {
            lowerHalf = sorted.subList(0, n / 2)
            upperHalf = sorted.subList(n / 2, n)
        } else {
            lowerHalf = sorted.subList(0, n / 2)
            upperHalf = sorted.subList(n / 2 + 1, n)
        }
        val q1 = if (lowerHalf.isEmpty()) sorted.first() else median(lowerHalf)
        val q3 = if (upperHalf.isEmpty()) sorted.last() else median(upperHalf)

        return StatisticsSummary(
            count = n, sum = sum, mean = mean, median = median,
            min = sorted.first(), max = sorted.last(), range = sorted.last() - sorted.first(),
            populationVariance = popVar, populationStdDev = sqrt(popVar),
            sampleVariance = sampleVar, sampleStdDev = sampleVar?.let { sqrt(it) },
            modes = modes, q1 = q1, q3 = q3
        )
    }

    private fun median(sorted: List<Double>): Double {
        val n = sorted.size
        return if (n % 2 == 1) sorted[n / 2] else (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
    }

    /** Linear interpolation percentile, 0..100, requires a pre-sorted or unsorted list (sorted internally). */
    fun percentile(data: List<Double>, p: Double): Double {
        if (data.isEmpty()) throw AxMathError.InvalidInput("Percentile requires a non-empty dataset.")
        if (p < 0 || p > 100) throw AxMathError.InvalidInput("Percentile must be between 0 and 100.")
        val sorted = data.sorted()
        if (sorted.size == 1) return sorted[0]
        val rank = p / 100.0 * (sorted.size - 1)
        val lowIndex = kotlin.math.floor(rank).toInt()
        val highIndex = kotlin.math.ceil(rank).toInt()
        if (lowIndex == highIndex) return sorted[lowIndex]
        val fraction = rank - lowIndex
        return sorted[lowIndex] + (sorted[highIndex] - sorted[lowIndex]) * fraction
    }
}
