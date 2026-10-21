package com.axiom.calculator.core.units

import com.axiom.calculator.core.math.AxMathError

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"), AREA("Area"), VOLUME("Volume"), MASS("Mass"), TIME("Time"),
    SPEED("Speed"), PRESSURE("Pressure"), ENERGY("Energy"), POWER("Power"), TEMPERATURE("Temperature")
}

/** A convertible unit. [toBaseFactor] converts 1 unit to the category's SI base unit (ignored for TEMPERATURE, which is affine). */
data class UnitDef(val id: String, val label: String, val category: UnitCategory, val toBaseFactor: Double)

/**
 * Offline, table-driven unit conversion (SECTION 11). Ordinary units use a
 * multiplicative factor to a common base unit per category. Temperature is
 * handled separately with explicit affine formulas because it is NOT a
 * multiplicative conversion (0 degC != 0 degF != 0 K).
 */
object UnitConverter {
    // Base units: meter, sq-meter, cubic-meter, kilogram, second, m/s, pascal, joule, watt.
    val length = listOf(
        UnitDef("mm", "Millimeter", UnitCategory.LENGTH, 0.001),
        UnitDef("cm", "Centimeter", UnitCategory.LENGTH, 0.01),
        UnitDef("m", "Meter", UnitCategory.LENGTH, 1.0),
        UnitDef("km", "Kilometer", UnitCategory.LENGTH, 1000.0),
        UnitDef("in", "Inch", UnitCategory.LENGTH, 0.0254),
        UnitDef("ft", "Foot", UnitCategory.LENGTH, 0.3048),
        UnitDef("yd", "Yard", UnitCategory.LENGTH, 0.9144),
        UnitDef("mi", "Mile", UnitCategory.LENGTH, 1609.344)
    )

    val area = listOf(
        UnitDef("mm2", "Square millimeter", UnitCategory.AREA, 1e-6),
        UnitDef("cm2", "Square centimeter", UnitCategory.AREA, 1e-4),
        UnitDef("m2", "Square meter", UnitCategory.AREA, 1.0),
        UnitDef("ha", "Hectare", UnitCategory.AREA, 10_000.0),
        UnitDef("km2", "Square kilometer", UnitCategory.AREA, 1_000_000.0),
        UnitDef("in2", "Square inch", UnitCategory.AREA, 0.00064516),
        UnitDef("ft2", "Square foot", UnitCategory.AREA, 0.09290304),
        UnitDef("acre", "Acre", UnitCategory.AREA, 4046.8564224)
    )

    val volume = listOf(
        UnitDef("m3", "Cubic meter", UnitCategory.VOLUME, 1.0),
        UnitDef("L", "Liter", UnitCategory.VOLUME, 0.001),
        UnitDef("mL", "Milliliter", UnitCategory.VOLUME, 0.000001),
        UnitDef("cm3", "Cubic centimeter", UnitCategory.VOLUME, 0.000001),
        UnitDef("in3", "Cubic inch", UnitCategory.VOLUME, 0.000016387064),
        UnitDef("ft3", "Cubic foot", UnitCategory.VOLUME, 0.028316846592),
        UnitDef("galUS", "US gallon", UnitCategory.VOLUME, 0.003785411784),
        UnitDef("galImp", "Imperial gallon", UnitCategory.VOLUME, 0.00454609)
    )

    val mass = listOf(
        UnitDef("mg", "Milligram", UnitCategory.MASS, 0.000001),
        UnitDef("g", "Gram", UnitCategory.MASS, 0.001),
        UnitDef("kg", "Kilogram", UnitCategory.MASS, 1.0),
        UnitDef("t", "Metric tonne", UnitCategory.MASS, 1000.0),
        UnitDef("oz", "Ounce", UnitCategory.MASS, 0.028349523125),
        UnitDef("lb", "Pound", UnitCategory.MASS, 0.45359237)
    )

    val time = listOf(
        UnitDef("ms", "Millisecond", UnitCategory.TIME, 0.001),
        UnitDef("s", "Second", UnitCategory.TIME, 1.0),
        UnitDef("min", "Minute", UnitCategory.TIME, 60.0),
        UnitDef("h", "Hour", UnitCategory.TIME, 3600.0),
        UnitDef("day", "Day", UnitCategory.TIME, 86400.0),
        UnitDef("week", "Week", UnitCategory.TIME, 604800.0)
    )

    val speed = listOf(
        UnitDef("mps", "Meters per second", UnitCategory.SPEED, 1.0),
        UnitDef("kph", "Kilometers per hour", UnitCategory.SPEED, 1000.0 / 3600.0),
        UnitDef("mph", "Miles per hour", UnitCategory.SPEED, 1609.344 / 3600.0),
        UnitDef("knot", "Knot", UnitCategory.SPEED, 1852.0 / 3600.0)
    )

    val pressure = listOf(
        UnitDef("Pa", "Pascal", UnitCategory.PRESSURE, 1.0),
        UnitDef("kPa", "Kilopascal", UnitCategory.PRESSURE, 1000.0),
        UnitDef("bar", "Bar", UnitCategory.PRESSURE, 100_000.0),
        UnitDef("atm", "Atmosphere", UnitCategory.PRESSURE, 101_325.0),
        UnitDef("psi", "PSI", UnitCategory.PRESSURE, 6894.757293168)
    )

    val energy = listOf(
        UnitDef("J", "Joule", UnitCategory.ENERGY, 1.0),
        UnitDef("kJ", "Kilojoule", UnitCategory.ENERGY, 1000.0),
        UnitDef("cal", "Calorie (thermochemical)", UnitCategory.ENERGY, 4.184),
        UnitDef("kcal", "Kilocalorie", UnitCategory.ENERGY, 4184.0),
        UnitDef("Wh", "Watt-hour", UnitCategory.ENERGY, 3600.0),
        UnitDef("kWh", "Kilowatt-hour", UnitCategory.ENERGY, 3_600_000.0),
        UnitDef("eV", "Electronvolt", UnitCategory.ENERGY, 1.602176634e-19)
    )

    val power = listOf(
        UnitDef("W", "Watt", UnitCategory.POWER, 1.0),
        UnitDef("kW", "Kilowatt", UnitCategory.POWER, 1000.0),
        UnitDef("hp", "Horsepower (mechanical, 550 ft*lbf/s)", UnitCategory.POWER, 745.6998715822702)
    )

    fun unitsFor(category: UnitCategory): List<UnitDef> = when (category) {
        UnitCategory.LENGTH -> length
        UnitCategory.AREA -> area
        UnitCategory.VOLUME -> volume
        UnitCategory.MASS -> mass
        UnitCategory.TIME -> time
        UnitCategory.SPEED -> speed
        UnitCategory.PRESSURE -> pressure
        UnitCategory.ENERGY -> energy
        UnitCategory.POWER -> power
        UnitCategory.TEMPERATURE -> emptyList() // handled separately, see convertTemperature
    }

    fun convert(value: Double, from: UnitDef, to: UnitDef): Double {
        if (from.category != to.category) {
            throw AxMathError.InvalidInput("Cannot convert between ${from.category.displayName} and ${to.category.displayName}: incompatible unit categories.")
        }
        return value * from.toBaseFactor / to.toBaseFactor
    }

    enum class TemperatureUnit { CELSIUS, FAHRENHEIT, KELVIN }

    fun convertTemperature(value: Double, from: TemperatureUnit, to: TemperatureUnit): Double {
        val celsius = when (from) {
            TemperatureUnit.CELSIUS -> value
            TemperatureUnit.FAHRENHEIT -> (value - 32.0) * 5.0 / 9.0
            TemperatureUnit.KELVIN -> value - 273.15
        }
        if (from == TemperatureUnit.KELVIN && value < 0) {
            throw AxMathError.Domain("Kelvin temperature cannot be negative (below absolute zero).")
        }
        return when (to) {
            TemperatureUnit.CELSIUS -> celsius
            TemperatureUnit.FAHRENHEIT -> celsius * 9.0 / 5.0 + 32.0
            TemperatureUnit.KELVIN -> {
                val k = celsius + 273.15
                if (k < 0) throw AxMathError.Domain("The result would be below absolute zero, which is physically impossible.")
                k
            }
        }
    }
}
