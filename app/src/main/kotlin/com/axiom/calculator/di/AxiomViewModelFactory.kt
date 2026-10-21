package com.axiom.calculator.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Generic manual-DI ViewModel factory: wraps a no-arg constructor lambda provided by each feature. */
class AxiomViewModelFactory<T : ViewModel>(private val create: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <U : ViewModel> create(modelClass: Class<U>): U = create() as U
}
