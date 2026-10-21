package com.axiom.calculator.feature.formulas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaDetailScreen(viewModel: FormulaDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val formula = state.formula

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(formula?.title ?: "Formula not found") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            }
        )
    }) { padding ->
        if (formula == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("This formula could not be found.")
            }
            return@Scaffold
        }
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(formula.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            formula.inputs.forEach { input ->
                OutlinedTextField(
                    value = state.fieldValues[input.key] ?: "",
                    onValueChange = { viewModel.onFieldChange(input.key, it) },
                    label = { Text(input.unit?.let { "${input.label} ($it)" } ?: input.label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).semantics { contentDescription = "Input ${input.label}" }
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = viewModel::onCompute) { Text("Calculate") }
                OutlinedButton(onClick = viewModel::onReset) { Text("Reset") }
            }

            Spacer(Modifier.height(16.dp))

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { contentDescription = "Error: $it" })
            }

            state.result?.let { result ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        result.outputs.forEach { output ->
                            Text(
                                "${output.label}: ${output.value}${output.unit?.let { " $it" } ?: ""}",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        if (result.steps.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            result.steps.forEach { step -> Text(step, style = MaterialTheme.typography.bodySmall) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = viewModel::onSave) {
                                Text(if (state.savedConfirmation) "Saved \u2713" else "Save")
                            }
                        }
                    }
                }
            }
        }
    }
}
