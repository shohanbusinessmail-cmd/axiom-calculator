package com.axiom.calculator.core.solve

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sqrt

class StatisticsTest {
    private val data = listOf(1.0, 2.0, 3.0, 4.0, 5.0)

    @Test fun count() = assertEquals(5, Statistics.summarize(data).count)
    @Test fun sum() = assertEquals(15.0, Statistics.summarize(data).sum, 0.0)
    @Test fun mean() = assertEquals(3.0, Statistics.summarize(data).mean, 0.0)
    @Test fun median() = assertEquals(3.0, Statistics.summarize(data).median, 0.0)
    @Test fun range() = assertEquals(4.0, Statistics.summarize(data).range, 0.0)
    @Test fun populationVariance() = assertEquals(2.0, Statistics.summarize(data).populationVariance, 1e-12)
    @Test fun populationStdDev() = assertEquals(sqrt(2.0), Statistics.summarize(data).populationStdDev, 1e-12)
    @Test fun sampleVariance() = assertEquals(2.5, Statistics.summarize(data).sampleVariance!!, 1e-12)
    @Test fun sampleStdDev() = assertEquals(sqrt(2.5), Statistics.summarize(data).sampleStdDev!!, 1e-12)

    @Test fun singleValueDataset_sampleVarianceUndefined() {
        val s = Statistics.summarize(listOf(42.0))
        assertNull(s.sampleVariance)
        assertNull(s.sampleStdDev)
    }

    @Test fun multipleModes() {
        val s = Statistics.summarize(listOf(1.0, 1.0, 2.0, 2.0, 3.0))
        assertEquals(listOf(1.0, 2.0), s.modes)
    }

    @Test fun noModeWhenAllUnique() {
        val s = Statistics.summarize(listOf(1.0, 2.0, 3.0))
        assertTrue(s.modes.isEmpty())
    }

    @Test(expected = com.axiom.calculator.core.math.AxMathError::class)
    fun emptyDatasetThrows() { Statistics.summarize(emptyList()) }
}
