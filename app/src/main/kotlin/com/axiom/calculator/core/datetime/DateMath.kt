package com.axiom.calculator.core.datetime

import com.axiom.calculator.core.math.AxMathError
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth

data class DateDifference(val years: Int, val months: Int, val days: Int, val totalDays: Long)

/**
 * Pure date arithmetic on java.time.LocalDate. All functions take explicit
 * dates (never `LocalDate.now()` internally) so they are deterministic and
 * testable with an injectable clock at the call site (SECTION 17.13/19).
 */
object DateMath {

    fun difference(start: LocalDate, end: LocalDate): DateDifference {
        val (from, to) = if (start.isAfter(end)) end to start else start to end
        val period = Period.between(from, to)
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(from, to)
        return DateDifference(period.years, period.months, period.days, if (start.isAfter(end)) -totalDays else totalDays)
    }

    fun addDays(date: LocalDate, days: Long): LocalDate = date.plusDays(days)

    /**
     * Adds whole months using the "clamp to end of month" rule: if the
     * resulting month has fewer days than the original day-of-month, the
     * result is clamped to the last valid day of that month (matching
     * java.time.LocalDate#plusMonths, which already implements this rule).
     * Documented explicitly per SECTION 11's end-of-month requirement.
     */
    fun addMonths(date: LocalDate, months: Long): LocalDate = date.plusMonths(months)

    fun isLeapYear(year: Int): Boolean = java.time.Year.isLeap(year.toLong())

    fun daysInMonth(year: Int, month: Int): Int {
        if (month !in 1..12) throw AxMathError.InvalidInput("Month must be between 1 and 12.")
        return YearMonth.of(year, month).lengthOfMonth()
    }

    fun parseIso(text: String): LocalDate = try {
        LocalDate.parse(text)
    } catch (e: Exception) {
        throw AxMathError.Syntax("'$text' is not a valid date (expected YYYY-MM-DD).")
    }
}
