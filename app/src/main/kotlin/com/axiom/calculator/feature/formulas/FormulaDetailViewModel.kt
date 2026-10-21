package com.axiom.calculator.feature.formulas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiom.calculator.core.formula.Formula
import com.axiom.calculator.core.formula.FormulaRegistry
import com.axiom.calculator.core.formula.FormulaResult
import com.axiom.calculator.core.math.AxMathError
import com.axiom.calculator.data.repository.HistoryRepository
import com.axiom.calculator.data.repository.SavedCalculationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FormulaDetailUiState(
    val formula: Formula?,
    val fieldValues: Map<String, String> = emptyMap(),
    val result: FormulaResult? = null,
    val errorMessage: String? = null,
    val savedConfirmation: Boolean = false
)

class FormulaDetailViewModel(
    formulaId: String,
    private val historyRepository: HistoryRepository,
    private val savedCalculationRepository: SavedCalculationRepository
) : ViewModel() {
    private val formula = FormulaRegistry.byId(formulaId)
    private val _uiState = MutableStateFlow(FormulaDetailUiState(formula = formula))
    val uiState: StateFlow<FormulaDetailUiState> = _uiState.asStateFlow()

    fun onFieldChange(key: String, value: String) {
        _uiState.value = _uiState.value.copy(fieldValues = _uiState.value.fieldValues + (key to value), savedConfirmation = false)
    }

    fun onReset() {
        _uiState.value = _uiState.value.copy(fieldValues = emptyMap(), result = null, errorMessage = null, savedConfirmation = false)
    }

    fun onCompute() {
        val f = formula ?: return
        val numericValues = mutableMapOf<String, Double>()
        for (input in f.inputs) {
            val raw = _uiState.value.fieldValues[input.key]
            val parsed = raw?.toDoubleOrNull()
            if (parsed == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid number for '${input.label}'.", result = null)
                return
            }
            numericValues[input.key] = parsed
        }
        try {
            val result = f.compute(numericValues)
            _uiState.value = _uiState.value.copy(result = result, errorMessage = null)
            val summary = result.outputs.joinToString(", ") { "${it.label}: ${it.value}${it.unit?.let { u -> " $u" } ?: ""}" }
            val inputSummary = f.inputs.joinToString(", ") { "${it.label}=${numericValues[it.key]}" }
            viewModelScope.launch {
                historyRepository.record(input = inputSummary, result = summary, calculationType = "FORMULA:${f.id}")
            }
        } catch (e: AxMathError) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message, result = null)
        }
    }

    fun onSave() {
        val f = formula ?: return
        val result = _uiState.value.result ?: return
        val summary = result.outputs.joinToString(", ") { "${it.label}: ${it.value}" }
        val inputSummary = f.inputs.joinToString(", ") { "${it.label}=${_uiState.value.fieldValues[it.key] ?: ""}" }
        viewModelScope.launch {
            savedCalculationRepository.save(label = f.title, input = inputSummary, result = summary, calculationType = "FORMULA:${f.id}")
            _uiState.value = _uiState.value.copy(savedConfirmation = true)
        }
    }
}
