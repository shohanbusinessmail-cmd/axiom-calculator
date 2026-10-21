package com.axiom.calculator.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiom.calculator.core.formula.FormulaRegistry
import com.axiom.calculator.data.db.HistoryEntity
import com.axiom.calculator.data.repository.RetentionPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: LibraryViewModel, onOpenSettings: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showRetentionMenu by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Library") },
            actions = {
                IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                if (state.tab == LibraryTab.HISTORY) {
                    Box {
                        TextButton(onClick = { showRetentionMenu = true }) { Text(state.retentionPolicy.displayName) }
                        DropdownMenu(expanded = showRetentionMenu, onDismissRequest = { showRetentionMenu = false }) {
                            RetentionPolicy.entries.forEach { policy ->
                                DropdownMenuItem(text = { Text(policy.displayName) }, onClick = {
                                    viewModel.setRetentionPolicy(policy); showRetentionMenu = false
                                })
                            }
                        }
                    }
                    IconButton(onClick = { showClearAllConfirm = true }) { Icon(Icons.Filled.Delete, contentDescription = "Clear all history") }
                }
            }
        )
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.tab.ordinal) {
                LibraryTab.entries.forEach { tab ->
                    Tab(selected = state.tab == tab, onClick = { viewModel.onTabChange(tab) }, text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) })
                }
            }

            if (state.tab != LibraryTab.REFERENCE) {
                OutlinedTextField(
                    value = state.query, onValueChange = viewModel::onQueryChange,
                    label = { Text("Search") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).semantics { contentDescription = "Search library" }
                )
            }

            when (state.tab) {
                LibraryTab.HISTORY -> HistoryList(state, viewModel)
                LibraryTab.SAVED -> SavedList(state, viewModel)
                LibraryTab.REFERENCE -> ReferenceList()
            }
        }
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Clear all history?") },
            text = { Text("This permanently deletes every history entry. Saved and favorited calculations are not affected.") },
            confirmButton = { TextButton(onClick = { viewModel.clearAllHistory(); showClearAllConfirm = false }) { Text("Clear all") } },
            dismissButton = { TextButton(onClick = { showClearAllConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HistoryList(state: LibraryUiState, viewModel: LibraryViewModel) {
    if (state.history.isEmpty()) {
        EmptyState("No history yet. Calculations you complete will appear here.")
        return
    }
    Column {
        AnimatedVisibility(visible = state.selectedHistoryIds.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${state.selectedHistoryIds.size} selected")
                Row {
                    TextButton(onClick = viewModel::clearSelection) { Text("Cancel") }
                    TextButton(onClick = viewModel::deleteSelected) { Text("Delete") }
                }
            }
        }
        LazyColumn(Modifier.weight(1f, fill = false).fillMaxWidth()) {
            items(state.history, key = { it.id }) { entry ->
                HistoryRow(entry, entry.id in state.selectedHistoryIds, viewModel)
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntity, selected: Boolean, viewModel: LibraryViewModel) {
    ListItem(
        headlineContent = { Text(entry.input) },
        supportingContent = { Text("= ${entry.result}  \u2022  ${entry.calculationType}") },
        trailingContent = {
            Row {
                IconButton(onClick = { viewModel.saveHistoryEntry(entry) }) { Icon(Icons.Filled.FavoriteBorder, contentDescription = "Save calculation") }
                IconButton(onClick = { viewModel.deleteHistoryEntry(entry.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete entry") }
            }
        },
        modifier = Modifier
            .semantics { contentDescription = "History entry ${entry.input} equals ${entry.result}" }
            .let { base ->
                base
            }
    )
    HorizontalDivider()
}

@Composable
private fun SavedList(state: LibraryUiState, viewModel: LibraryViewModel) {
    if (state.saved.isEmpty()) {
        EmptyState("No saved calculations yet. Save a result from History or a Formula to keep it here permanently.")
        return
    }
    LazyColumn {
        items(state.saved, key = { it.id }) { entry ->
            ListItem(
                headlineContent = { Text(entry.label) },
                supportingContent = { Text("${entry.input} = ${entry.result}") },
                trailingContent = {
                    Row {
                        IconButton(onClick = { viewModel.togglePinned(entry) }) { Icon(Icons.Filled.PushPin, contentDescription = if (entry.isPinned) "Unpin" else "Pin") }
                        IconButton(onClick = { viewModel.toggleFavorite(entry) }) {
                            Icon(if (entry.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = if (entry.isFavorite) "Unfavorite" else "Favorite")
                        }
                        IconButton(onClick = { viewModel.deleteSaved(entry.id) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete saved calculation") }
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun ReferenceList() {
    LazyColumn(Modifier.padding(16.dp)) {
        item { Text("Scientific constants", style = MaterialTheme.typography.titleLarge) }
        item { Text("\u03c0 (pi) \u2248 3.14159265358979") }
        item { Text("e \u2248 2.71828182845905") }
        item { Text("Standard gravity g = 9.80665 m/s\u00b2") }
        item { Spacer(Modifier.height(16.dp)) }
        item { Text("Formula catalog (${FormulaRegistry.all.size} formulas)", style = MaterialTheme.typography.titleLarge) }
        items(FormulaRegistry.all, key = { it.id }) { formula ->
            Text("\u2022 ${formula.title} (${formula.category.displayName})", modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
    }
}
