package com.axiom.calculator.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiom.calculator.data.datastore.AngleModePreference
import com.axiom.calculator.data.datastore.NumberGroupingStyle
import com.axiom.calculator.data.datastore.ThemePreference
import com.axiom.calculator.data.repository.RetentionPolicy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit, onOpenAbout: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var showClearSavedConfirm by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Settings") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            SectionTitle("Appearance")
            SegmentedRow(ThemePreference.entries.map { it.name }, state.preferences.theme.name) {
                viewModel.setTheme(ThemePreference.valueOf(it))
            }

            SectionTitle("Calculation")
            Text("Angle mode", style = MaterialTheme.typography.labelLarge)
            SegmentedRow(AngleModePreference.entries.map { it.name }, state.preferences.angleMode.name) {
                viewModel.setAngleMode(AngleModePreference.valueOf(it))
            }
            Text("Number grouping", style = MaterialTheme.typography.labelLarge)
            SegmentedRow(NumberGroupingStyle.entries.map { it.name }, state.preferences.numberGrouping.name) {
                viewModel.setGrouping(NumberGroupingStyle.valueOf(it))
            }
            SwitchRow("Scientific notation for very large/small results", state.preferences.useScientificNotation, viewModel::setScientificNotation)
            SwitchRow("Engineering notation", state.preferences.useEngineeringNotation, viewModel::setEngineeringNotation)

            SectionTitle("Interaction")
            SwitchRow("Haptic feedback", state.preferences.hapticFeedbackEnabled, viewModel::setHaptics)
            SwitchRow("Key sound", state.preferences.keySoundEnabled, viewModel::setKeySound)
            SwitchRow("Reduced motion", state.preferences.reducedMotion, viewModel::setReducedMotion)

            SectionTitle("Privacy and data")
            Text("History retention", style = MaterialTheme.typography.labelLarge)
            SegmentedRow(RetentionPolicy.entries.map { it.displayName }, state.retentionPolicy.displayName) { label ->
                RetentionPolicy.entries.find { it.displayName == label }?.let(viewModel::setRetention)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = viewModel::runCleanupNow, modifier = Modifier.fillMaxWidth()) { Text("Run cleanup now") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showClearHistoryConfirm = true }, modifier = Modifier.fillMaxWidth()) { Text("Clear history") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { showClearSavedConfirm = true }, modifier = Modifier.fillMaxWidth()) { Text("Clear saved calculations") }
            Spacer(Modifier.height(8.dp))
            Text(
                "All calculations, history, and preferences are stored only on this device. Axiom Calculator has no network access and no account system.",
                style = MaterialTheme.typography.bodySmall
            )

            SectionTitle("About")
            OutlinedButton(onClick = onOpenAbout, modifier = Modifier.fillMaxWidth()) { Text("About Axiom Calculator") }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text("Clear all history?") },
            text = { Text("This cannot be undone. Saved calculations are not affected.") },
            confirmButton = { TextButton(onClick = { viewModel.clearHistory(); showClearHistoryConfirm = false }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { showClearHistoryConfirm = false }) { Text("Cancel") } }
        )
    }
    if (showClearSavedConfirm) {
        AlertDialog(
            onDismissRequest = { showClearSavedConfirm = false },
            title = { Text("Clear all saved calculations?") },
            text = { Text("This permanently deletes every saved/favorited calculation. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.clearSavedCalculations(); showClearSavedConfirm = false }) { Text("Clear") } },
            dismissButton = { TextButton(onClick = { showClearSavedConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SegmentedRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(selected = option == selected, onClick = { onSelect(option) }, label = { Text(option) })
        }
    }
}
