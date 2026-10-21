package com.axiom.calculator.feature.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiom.calculator.core.datetime.DateMath
import com.axiom.calculator.core.math.AxMathError
import com.axiom.calculator.core.programmer.BitWidth
import com.axiom.calculator.core.programmer.NumberBase
import com.axiom.calculator.core.programmer.ProgrammerTools
import com.axiom.calculator.core.units.UnitCategory
import com.axiom.calculator.core.units.UnitConverter
import com.axiom.calculator.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.time.LocalDate

enum class ToolsMode(val label: String) {
    UNIT_CONVERTER("Unit Converter"),
    PROGRAMMER("Programmer"),
    DATE_TIME("Date & Time"),
    BMI_BMR("BMI / BMR")
}

data class ToolsUiState(
    val mode: ToolsMode = ToolsMode.UNIT_CONVERTER,
    val unitCategory: UnitCategory = UnitCategory.LENGTH,
    val fields: Map<String, String> = emptyMap(),
    val resultLines: List<String> = emptyList(),
    val errorMessage: String? = null,
    val fromBase: NumberBase = NumberBase.DECIMAL,
    val toBase: NumberBase = NumberBase.BINARY,
    val bitWidth: BitWidth = BitWidth.W32
)

class ToolsViewModel(private val historyRepository: HistoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    fun onModeChange(mode: ToolsMode) { _uiState.value = ToolsUiState(mode = mode) }
    fun onUnitCategoryChange(category: UnitCategory) { _uiState.value = _uiState.value.copy(unitCategory = category, fields = emptyMap(), resultLines = emptyList()) }
    fun onFieldChange(key: String, value: String) { _uiState.value = _uiState.value.copy(fields = _uiState.value.fields + (key to value)) }
    fun onFromBaseChange(base: NumberBase) { _uiState.value = _uiState.value.copy(fromBase = base) }
    fun onToBaseChange(base: NumberBase) { _uiState.value = _uiState.value.copy(toBase = base) }
    fun onBitWidthChange(width: BitWidth) { _uiState.value = _uiState.value.copy(bitWidth = width) }

    fun onRun() {
        try {
            val lines = when (_uiState.value.mode) {
                ToolsMode.UNIT_CONVERTER -> runUnitConversion()
                ToolsMode.PROGRAMMER -> runProgrammer()
                ToolsMode.DATE_TIME -> runDateTime()
                ToolsMode.BMI_BMR -> runBmiBmr()
            }
            _uiState.value = _uiState.value.copy(resultLines = lines, errorMessage = null)
            viewModelScope.launch {
                historyRepository.record(
                    input = "${_uiState.value.mode.label}: ${_uiState.value.fields}",
                    result = lines.joinToString(" | "),
                    calculationType = "TOOL:${_uiState.value.mode.name}"
                )
            }
        } catch (e: AxMathError) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message, resultLines = emptyList())
        }
    }

    private fun field(key: String) = _uiState.value.fields[key]
        ?: throw AxMathError.InvalidInput("Field '$key' is required.")

    private fun runUnitConversion(): List<String> {
        val value = field("value").toDoubleOrNull() ?: throw AxMathError.InvalidInput("Enter a valid number.")
        val category = _uiState.value.unitCategory
        if (category == UnitCategory.TEMPERATURE) {
            val from = UnitConverter.TemperatureUnit.valueOf(field("from"))
            val to = UnitConverter.TemperatureUnit.valueOf(field("to"))
            val result = UnitConverter.convertTemperature(value, from, to)
            return listOf("$value ${from.name} = $result ${to.name}")
        }
        val units = UnitConverter.unitsFor(category)
        val from = units.find { it.id == field("from") } ?: throw AxMathError.InvalidInput("Unknown source unit.")
        val to = units.find { it.id == field("to") } ?: throw AxMathError.InvalidInput("Unknown target unit.")
        val result = UnitConverter.convert(value, from, to)
        return listOf("$value ${from.label} = $result ${to.label}")
    }

    private fun runProgrammer(): List<String> {
        val text = field("value")
        val value = ProgrammerTools.parse(text, _uiState.value.fromBase)
        val converted = ProgrammerTools.format(value, _uiState.value.toBase)
        val lines = mutableListOf("$text (${_uiState.value.fromBase.label}) = $converted (${_uiState.value.toBase.label})")
        val op = _uiState.value.fields["bitwiseOp"]
        val secondOperand = _uiState.value.fields["value2"]
        if (op != null && secondOperand != null) {
            val b = ProgrammerTools.parse(secondOperand, _uiState.value.fromBase)
            val width = _uiState.value.bitWidth
            val result = when (op) {
                "AND" -> ProgrammerTools.and(value, b, width)
                "OR" -> ProgrammerTools.or(value, b, width)
                "XOR" -> ProgrammerTools.xor(value, b, width)
                "NOT" -> ProgrammerTools.not(value, width)
                "SHL" -> ProgrammerTools.shiftLeft(value, secondOperand.toIntOrNull() ?: 0, width)
                "SHR_LOGICAL" -> ProgrammerTools.shiftRightLogical(value, secondOperand.toIntOrNull() ?: 0, width)
                "SHR_ARITH" -> ProgrammerTools.shiftRightArithmetic(value, secondOperand.toIntOrNull() ?: 0, width)
                else -> BigInteger.ZERO
            }
            lines += "$op result = $result (bit width ${width.bits})"
        }
        return lines
    }

    private fun runDateTime(): List<String> {
        val startText = field("start")
        val start = DateMath.parseIso(startText)
        return when (_uiState.value.fields["op"]) {
            "addDays" -> {
                val days = field("amount").toLongOrNull() ?: throw AxMathError.InvalidInput("Enter a whole number of days.")
                listOf("${DateMath.addDays(start, days)}")
            }
            "addMonths" -> {
                val months = field("amount").toLongOrNull() ?: throw AxMathError.InvalidInput("Enter a whole number of months.")
                listOf("${DateMath.addMonths(start, months)}")
            }
            else -> {
                val end = DateMath.parseIso(field("end"))
                val diff = DateMath.difference(start, end)
                listOf(
                    "${diff.years} years, ${diff.months} months, ${diff.days} days",
                    "Total days = ${diff.totalDays}",
                    "${start.year} is a leap year: ${DateMath.isLeapYear(start.year)}"
                )
            }
        }
    }

    private fun runBmiBmr(): List<String> {
        val weightKg = field("weight").toDoubleOrNull() ?: throw AxMathError.InvalidInput("Enter a valid weight in kg.")
        val heightCm = field("height").toDoubleOrNull() ?: throw AxMathError.InvalidInput("Enter a valid height in cm.")
        if (weightKg <= 0 || heightCm <= 0) throw AxMathError.InvalidInput("Weight and height must be positive.")
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
        val lines = mutableListOf("BMI = ${"%.1f".format(bmi)} ($category)")
        val age = field("age").toDoubleOrNull()
        val sex = _uiState.value.fields["sex"]
        if (age != null && sex != null) {
            // Mifflin-St Jeor equation (documented formula, SECTION 11).
            val bmr = if (sex == "male") {
                10 * weightKg + 6.25 * heightCm - 5 * age + 5
            } else {
                10 * weightKg + 6.25 * heightCm - 5 * age - 161
            }
            lines += "BMR = ${"%.0f".format(bmr)} kcal/day (Mifflin-St Jeor equation; informational only, not medical advice)"
        }
        return lines
    }
}
