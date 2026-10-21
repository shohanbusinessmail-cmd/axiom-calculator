package com.axiom.calculator.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiom.calculator.data.datastore.AngleModePreference
import com.axiom.calculator.data.datastore.AxiomPreferences
import com.axiom.calculator.data.datastore.NumberGroupingStyle
import com.axiom.calculator.data.datastore.SettingsDataStore
import com.axiom.calculator.data.datastore.ThemePreference
import com.axiom.calculator.data.repository.HistoryRepository
import com.axiom.calculator.data.repository.RetentionPolicy
import com.axiom.calculator.data.repository.SavedCalculationRepository
import com.axiom.calculator.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: AxiomPreferences = AxiomPreferences(),
    val retentionPolicy: RetentionPolicy = RetentionPolicy.DEFAULT
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dataStore: SettingsDataStore,
    private val historyRepository: HistoryRepository,
    private val savedCalculationRepository: SavedCalculationRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.preferences, settingsRepository.retentionPolicy
    ) { prefs, retention -> SettingsUiState(prefs, retention) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(theme: ThemePreference) = viewModelScope.launch { dataStore.setTheme(theme) }
    fun setAngleMode(mode: AngleModePreference) = viewModelScope.launch { dataStore.setAngleMode(mode) }
    fun setPrecision(value: Int) = viewModelScope.launch { dataStore.setPrecision(value) }
    fun setScientificNotation(enabled: Boolean) = viewModelScope.launch { dataStore.setScientificNotation(enabled) }
    fun setEngineeringNotation(enabled: Boolean) = viewModelScope.launch { dataStore.setEngineeringNotation(enabled) }
    fun setGrouping(style: NumberGroupingStyle) = viewModelScope.launch { dataStore.setNumberGrouping(style) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { dataStore.setHaptics(enabled) }
    fun setKeySound(enabled: Boolean) = viewModelScope.launch { dataStore.setKeySound(enabled) }
    fun setReducedMotion(enabled: Boolean) = viewModelScope.launch { dataStore.setReducedMotion(enabled) }
    fun setRetention(policy: RetentionPolicy) = viewModelScope.launch { settingsRepository.setRetentionPolicy(policy) }

    fun runCleanupNow() = viewModelScope.launch { historyRepository.cleanupExpired(uiState.value.retentionPolicy) }
    fun clearHistory() = viewModelScope.launch { historyRepository.clearAll() }
    fun clearSavedCalculations() = viewModelScope.launch {
        val all = savedCalculationRepository.observeAll().first()
        savedCalculationRepository.deleteMany(all.map { it.id })
    }
}
