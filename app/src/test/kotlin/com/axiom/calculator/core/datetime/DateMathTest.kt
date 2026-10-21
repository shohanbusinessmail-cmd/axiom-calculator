package com.axiom.calculator.core.datetime

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DateMathTest {
    @Test fun leapYear2024() = assertTrue(DateMath.isLeapYear(2024))
    @Test fun nonLeapYear2023() = assertFalse(DateMath.isLeapYear(2023))
    @Test fun centuryNonLeapYear1900() = assertFalse(DateMath.isLeapYear(1900))
    @Test fun centuryLeapYear2000() = assertTrue(DateMath.isLeapYear(2000))

    @Test fun februaryLeapYearHas29Days() = assertEquals(29, DateMath.daysInMonth(2024, 2))
    @Test fun februaryNonLeapYearHas28Days() = assertEquals(28, DateMath.daysInMonth(2023, 2))

    @Test
    fun endOfMonthAdditionClamps() {
        // Jan 31 + 1 month must clamp to Feb 28/29, not overflow into March.
        val result = DateMath.addMonths(LocalDate.of(2023, 1, 31), 1)
        assertEquals(LocalDate.of(2023, 2, 28), result)
    }

    @Test
    fun yearTransitionDifference() {
        val diff = DateMath.difference(LocalDate.of(2023, 12, 25), LocalDate.of(2024, 1, 5))
        assertEquals(11, diff.totalDays)
    }

    @Test
    fun negativeDurationWhenEndBeforeStart() {
        val diff = DateMath.difference(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 1))
        assertEquals(-9, diff.totalDays)
    }

    @Test(expected = com.axiom.calculator.core.math.AxMathError.Syntax::class)
    fun invalidDateStringRejected() { DateMath.parseIso("2024-13-40") }

    @Test
    fun addDaysAcrossMonthBoundary() {
        assertEquals(LocalDate.of(2024, 3, 1), DateMath.addDays(LocalDate.of(2024, 2, 29), 1))
    }
}
