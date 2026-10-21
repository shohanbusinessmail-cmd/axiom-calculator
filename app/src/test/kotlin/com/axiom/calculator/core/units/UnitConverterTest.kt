package com.axiom.calculator.core.units

import org.junit.Assert.*
import org.junit.Test

class UnitConverterTest {
    private fun length(id: String) = UnitConverter.length.first { it.id == id }
    private fun mass(id: String) = UnitConverter.mass.first { it.id == id }
    private fun time(id: String) = UnitConverter.time.first { it.id == id }
    private fun volume(id: String) = UnitConverter.volume.first { it.id == id }
    private fun energy(id: String) = UnitConverter.energy.first { it.id == id }
    private fun pressure(id: String) = UnitConverter.pressure.first { it.id == id }

    @Test fun u001_kmToM() = assertEquals(1000.0, UnitConverter.convert(1.0, length("km"), length("m")), 1e-9)
    @Test fun u002_mToCm() = assertEquals(100.0, UnitConverter.convert(1.0, length("m"), length("cm")), 1e-9)
    @Test fun u003_kgToG() = assertEquals(1000.0, UnitConverter.convert(1.0, mass("kg"), mass("g")), 1e-9)
    @Test fun u004_hourToSeconds() = assertEquals(3600.0, UnitConverter.convert(1.0, time("h"), time("s")), 1e-9)
    @Test fun u005_inchToCm() = assertEquals(2.54, UnitConverter.convert(1.0, length("in"), length("cm")), 1e-9)

    @Test fun u006_celsiusToFahrenheit() = assertEquals(32.0, UnitConverter.convertTemperature(0.0, UnitConverter.TemperatureUnit.CELSIUS, UnitConverter.TemperatureUnit.FAHRENHEIT), 1e-9)
    @Test fun u007_celsiusToKelvin() = assertEquals(373.15, UnitConverter.convertTemperature(100.0, UnitConverter.TemperatureUnit.CELSIUS, UnitConverter.TemperatureUnit.KELVIN), 1e-9)
    @Test fun u008_fahrenheitToCelsius() = assertEquals(0.0, UnitConverter.convertTemperature(32.0, UnitConverter.TemperatureUnit.FAHRENHEIT, UnitConverter.TemperatureUnit.CELSIUS), 1e-9)

    @Test fun u009_literToMilliliter() = assertEquals(1000.0, UnitConverter.convert(1.0, volume("L"), volume("mL")), 1e-9)
    @Test fun u010_barToPascal() = assertEquals(100_000.0, UnitConverter.convert(1.0, pressure("bar"), pressure("Pa")), 1e-6)
    @Test fun u011_kWhToJoule() = assertEquals(3_600_000.0, UnitConverter.convert(1.0, energy("kWh"), energy("J")), 1e-3)

    @Test
    fun roundTripConversionIsIdentity() {
        val original = 123.456
        val converted = UnitConverter.convert(original, length("mi"), length("km"))
        val back = UnitConverter.convert(converted, length("km"), length("mi"))
        assertEquals(original, back, 1e-9)
    }

    @Test(expected = com.axiom.calculator.core.math.AxMathError.InvalidInput::class)
    fun incompatibleCategoriesRejected() {
        UnitConverter.convert(1.0, length("m"), mass("kg"))
    }
}
