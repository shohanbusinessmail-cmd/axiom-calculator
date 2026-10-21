package com.axiom.calculator.feature.standard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiom.calculator.core.math.AxResult
import com.axiom.calculator.core.math.NumberFormatter
import com.axiom.calculator.core.math.StandardCalculatorEngine
import com.axiom.calculator.data.datastore.SettingsDataStore
import com.axiom.calculator.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class StandardUiState(
    val expression: String = "",
    val livePreview: String? = null,
    val lastResult: String? = null,
    val errorMessage: String? = null,
    val memoryHasValue: Boolean = false,
    val undoStack: List<String> = emptyList(),
    val redoStack: List<String> = emptyList()
)

/**
 * Owns Standard calculator UI state. Contains NO calculation algorithms
 * itself (SECTION 3) — all arithmetic is delegated to
 * [StandardCalculatorEngine] / [com.axiom.calculator.core.math.BigDecimalEvaluator].
 * A history row is committed only on an explicit "=" press.
 */
class StandardViewModel(
    private val historyRepository: HistoryRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(StandardUiState())
    val uiState: StateFlow<StandardUiState> = _uiState.asStateFlow()

    private var memoryValue: BigDecimal? = null

    init {
        viewModelScope.launch {
            val stored = settingsDataStore.memoryValueFlow
            stored.collect { raw ->
                memoryValue = raw?.toBigDecimalOrNull()
                _uiState.value = _uiState.value.copy(memoryHasValue = memoryValue != null)
            }
        }
    }

    private fun mutate(transform: (String) -> String) {
        val current = _uiState.value
        val newExpression = transform(current.expression)
        pushUndo(current.expression)
        _uiState.value = current.copy(expression = newExpression, errorMessage = null, redoStack = emptyList())
        refreshPreview()
    }

    private fun pushUndo(previous: String) {
        _uiState.value = _uiState.value.copy(undoStack = (_uiState.value.undoStack + previous).takeLast(50))
    }

    fun onDigit(digit: Char) = mutate { StandardCalculatorEngine.appendDigit(it, digit) }
    fun onDecimalPoint() = mutate { StandardCalculatorEngine.appendDecimalPoint(it) }
    fun onOperator(op: Char) = mutate { StandardCalculatorEngine.appendOperator(it, op) }
    fun onOpenParen() = mutate { StandardCalculatorEngine.appendOpenParen(it) }
    fun onCloseParen() = mutate { StandardCalculatorEngine.appendCloseParen(it) }
    fun onPercent() = mutate { StandardCalculatorEngine.appendPercent(it) }
    fun onToggleSign() = mutate { StandardCalculatorEngine.toggleSignOfLastToken(it) }
    fun onBackspace() = mutate { StandardCalculatorEngine.backspace(it) }

    fun onClearAll() {
        pushUndo(_uiState.value.expression)
        _uiState.value = _uiState.value.copy(expression = "", livePreview = null, lastResult = null, errorMessage = null, redoStack = emptyList())
    }

    fun onUndo() {
        val state = _uiState.value
        val previous = state.undoStack.lastOrNull() ?: return
        _uiState.value = state.copy(
            expression = previous,
            undoStack = state.undoStack.dropLast(1),
            redoStack = state.redoStack + state.expression
        )
        refreshPreview()
    }

    fun onRedo() {
        val state = _uiState.value
        val next = state.redoStack.lastOrNull() ?: return
        _uiState.value = state.copy(
            expression = next,
            redoStack = state.redoStack.dropLast(1),
            undoStack = state.undoStack + state.expression
        )
        refreshPreview()
    }

    private fun refreshPreview() {
        val expr = _uiState.value.expression
        if (expr.isBlank()) {
            _uiState.value = _uiState.value.copy(livePreview = null)
            return
        }
        when (val result = StandardCalculatorEngine.evaluate(expr)) {
            is AxResult.Success -> _uiState.value = _uiState.value.copy(livePreview = formatBigDecimal(result.value))
            is AxResult.Failure -> _uiState.value = _uiState.value.copy(livePreview = null)
        }
    }

    fun onEquals() {
        val expr = _uiState.value.expression
        if (expr.isBlank()) return
        when (val result = StandardCalculatorEngine.evaluate(expr)) {
            is AxResult.Success -> {
                val formatted = formatBigDecimal(result.value)
                _uiState.value = _uiState.value.copy(
                    lastResult = formatted,
                    livePreview = formatted,
                    errorMessage = null,
                    expression = formatted
                )
                viewModelScope.launch {
                    historyRepository.record(input = expr, result = formatted, calculationType = "STANDARD")
                }
            }
            is AxResult.Failure -> {
                _uiState.value = _uiState.value.copy(errorMessage = result.error.message, livePreview = null)
            }
        }
    }

    private fun formatBigDecimal(value: BigDecimal): String =
        NumberFormatter.format(value.toDouble(), com.axiom.calculator.core.math.DisplayFormatOptions())

    // --- Memory operations (MC / MR / M+ / M- / MS) ---
    fun onMemoryClear() = viewModelScope.launch { settingsDataStore.setMemoryValue(null) }

    fun onMemoryRecall() {
        val value = memoryValue ?: return
        mutate { if (it.isBlank() || it == "0") value.toPlainString() else it + value.toPlainString() }
    }

    fun onMemoryStore() = viewModelScope.launch {
        val current = currentNumericValueOrNull() ?: return@launch
        settingsDataStore.setMemoryValue(current.toPlainString())
    }

    fun onMemoryAdd() = viewModelScope.launch {
        val current = currentNumericValueOrNull() ?: return@launch
        val newValue = (memoryValue ?: BigDecimal.ZERO).add(current)
        settingsDataStore.setMemoryValue(newValue.toPlainString())
    }

    fun onMemorySubtract() = viewModelScope.launch {
        val current = currentNumericValueOrNull() ?: return@launch
        val newValue = (memoryValue ?: BigDecimal.ZERO).subtract(current)
        settingsDataStore.setMemoryValue(newValue.toPlainString())
    }

    private fun currentNumericValueOrNull(): BigDecimal? =
        (StandardCalculatorEngine.evaluate(_uiState.value.expression) as? AxResult.Success)?.value
}
