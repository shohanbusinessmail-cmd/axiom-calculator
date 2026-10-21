package com.axiom.calculator.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiom.calculator.data.db.HistoryEntity
import com.axiom.calculator.data.db.SavedCalculationEntity
import com.axiom.calculator.data.repository.HistoryRepository
import com.axiom.calculator.data.repository.RetentionPolicy
import com.axiom.calculator.data.repository.SavedCalculationRepository
import com.axiom.calculator.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab { HISTORY, SAVED, REFERENCE }

data class LibraryUiState(
    val tab: LibraryTab = LibraryTab.HISTORY,
    val query: String = "",
    val history: List<HistoryEntity> = emptyList(),
    val saved: List<SavedCalculationEntity> = emptyList(),
    val selectedHistoryIds: Set<Long> = emptySet(),
    val retentionPolicy: RetentionPolicy = RetentionPolicy.DEFAULT
)

class LibraryViewModel(
    private val historyRepository: HistoryRepository,
    private val savedCalculationRepository: SavedCalculationRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val tab = MutableStateFlow(LibraryTab.HISTORY)
    private val selected = MutableStateFlow<Set<Long>>(emptySet())

    private val historyFlow = query.flatMapLatest { q -> if (q.isBlank()) historyRepository.observeAll() else historyRepository.search(q) }
    private val savedFlow = query.flatMapLatest { q -> if (q.isBlank()) savedCalculationRepository.observeAll() else savedCalculationRepository.search(q) }

    val uiState: StateFlow<LibraryUiState> = combine(
        tab, query, historyFlow, savedFlow, selected, settingsRepository.retentionPolicy
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        LibraryUiState(
            tab = values[0] as LibraryTab,
            query = values[1] as String,
            history = values[2] as List<HistoryEntity>,
            saved = values[3] as List<SavedCalculationEntity>,
            selectedHistoryIds = values[4] as Set<Long>,
            retentionPolicy = values[5] as RetentionPolicy
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    init {
        // Opening the Library screen is an explicit "repository access point" cleanup trigger (SECTION 12).
        viewModelScope.launch {
            val policy = settingsRepository.retentionPolicy.first()
            historyRepository.cleanupExpired(policy)
        }
    }

    fun onTabChange(newTab: LibraryTab) { tab.value = newTab; selected.value = emptySet() }
    fun onQueryChange(q: String) { query.value = q }

    fun toggleSelection(id: Long) {
        selected.value = if (id in selected.value) selected.value - id else selected.value + id
    }

    fun clearSelection() { selected.value = emptySet() }

    fun deleteSelected() = viewModelScope.launch {
        historyRepository.deleteMany(selected.value.toList())
        selected.value = emptySet()
    }

    fun deleteHistoryEntry(id: Long) = viewModelScope.launch { historyRepository.delete(id) }
    fun clearAllHistory() = viewModelScope.launch { historyRepository.clearAll() }

    fun setRetentionPolicy(policy: RetentionPolicy) = viewModelScope.launch {
        settingsRepository.setRetentionPolicy(policy)
    }

    fun runCleanupNow() = viewModelScope.launch {
        val policy = settingsRepository.retentionPolicy.first()
        historyRepository.cleanupExpired(policy)
    }

    fun saveHistoryEntry(entry: HistoryEntity) = viewModelScope.launch {
        savedCalculationRepository.save(label = entry.input.take(40), input = entry.input, result = entry.result, calculationType = entry.calculationType)
    }

    fun toggleFavorite(entity: SavedCalculationEntity) = viewModelScope.launch { savedCalculationRepository.toggleFavorite(entity) }
    fun togglePinned(entity: SavedCalculationEntity) = viewModelScope.launch { savedCalculationRepository.togglePinned(entity) }
    fun deleteSaved(id: Long) = viewModelScope.launch { savedCalculationRepository.delete(id) }
    fun renameSaved(entity: SavedCalculationEntity, newLabel: String) = viewModelScope.launch { savedCalculationRepository.rename(entity, newLabel) }
}
