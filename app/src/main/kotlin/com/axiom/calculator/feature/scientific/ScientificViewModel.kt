package com.axiom.calculator.feature.scientific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiom.calculator.core.math.*
import com.axiom.calculator.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScientificUiState(
    val expression: String = "",
    val livePreview: String? = null,
    val errorMessage: String? = null,
    val angleMode: AngleMode = AngleMode.DEGREES
)

/**
 * Scientific calculator ViewModel. Reuses the same textual-expression editing
 * primitives as Standard ([StandardCalculatorEngine]) but evaluates with the
 * Double-based [Evaluator], which supports the full scientific function set.
 */
class ScientificViewModel(private val historyRepository: HistoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ScientificUiState())
    val uiState: StateFlow<ScientificUiState> = _uiState.asStateFlow()

    fun toggleAngleMode() {
        _uiState.value = _uiState.value.copy(
            angleMode = if (_uiState.value.angleMode == AngleMode.DEGREES) AngleMode.RADIANS else AngleMode.DEGREES
        )
        refreshPreview()
    }

    fun appendRaw(text: String) {
        _uiState.value = _uiState.value.copy(expression = _uiState.value.expression + text, errorMessage = null)
        refreshPreview()
    }

    fun appendFunction(name: String) = appendRaw("$name(")

    fun backspace() {
        val e = _uiState.value.expression
        if (e.isNotEmpty()) _uiState.value = _uiState.value.copy(expression = e.dropLast(1), errorMessage = null)
        refreshPreview()
    }

    fun clearAll() {
        _uiState.value = _uiState.value.copy(expression = "", livePreview = null, errorMessage = null)
    }

    private fun refreshPreview() {
        val expr = _uiState.value.expression
        if (expr.isBlank()) {
            _uiState.value = _uiState.value.copy(livePreview = null)
            return
        }
        when (val r = evaluateExpression(expr, EvalContext(_uiState.value.angleMode))) {
            is AxResult.Success -> _uiState.value = _uiState.value.copy(livePreview = NumberFormatter.format(r.value))
            is AxResult.Failure -> _uiState.value = _uiState.value.copy(livePreview = null)
        }
    }

    fun onEquals() {
        val expr = _uiState.value.expression
        if (expr.isBlank()) return
        when (val r = evaluateExpression(expr, EvalContext(_uiState.value.angleMode))) {
            is AxResult.Success -> {
                val formatted = NumberFormatter.format(r.value)
                _uiState.value = _uiState.value.copy(expression = formatted, livePreview = formatted, errorMessage = null)
                viewModelScope.launch {
                    historyRepository.record(
                        input = expr, result = formatted, calculationType = "SCIENTIFIC",
                        angleMode = _uiState.value.angleMode.name
                    )
                }
            }
            is AxResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = r.error.message, livePreview = null)
        }
    }
}
