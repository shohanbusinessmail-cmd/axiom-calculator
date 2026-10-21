package com.axiom.calculator.feature.formulas

import androidx.lifecycle.ViewModel
import com.axiom.calculator.core.formula.Formula
import com.axiom.calculator.core.formula.FormulaCategory
import com.axiom.calculator.core.formula.FormulaRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FormulasListUiState(
    val query: String = "",
    val selectedCategory: FormulaCategory? = null,
    val results: List<Formula> = FormulaRegistry.all
)

class FormulasViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FormulasListUiState())
    val uiState: StateFlow<FormulasListUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        refresh()
    }

    fun onCategorySelected(category: FormulaCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        refresh()
    }

    private fun refresh() {
        val state = _uiState.value
        var list = FormulaRegistry.search(state.query)
        if (state.selectedCategory != null) list = list.filter { it.category == state.selectedCategory }
        _uiState.value = state.copy(results = list)
    }
}
