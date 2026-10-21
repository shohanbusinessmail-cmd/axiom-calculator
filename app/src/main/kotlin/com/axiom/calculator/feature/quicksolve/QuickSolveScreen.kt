package com.axiom.calculator.feature.quicksolve

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSolveScreen(viewModel: QuickSolveViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Quick Solve") }) }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickSolveMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.mode == mode,
                        onClick = { viewModel.onModeChange(mode) },
                        label = { Text(mode.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            FieldsForMode(state.mode, state.fields, viewModel::onFieldChange)

            Spacer(Modifier.height(12.dp))
            Button(onClick = viewModel::onSolve, modifier = Modifier.semantics { contentDescription = "Solve" }) { Text("Solve") }
            Spacer(Modifier.height(16.dp))

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { contentDescription = "Error: $it" })
            }

            if (state.resultLines.isNotEmpty()) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        state.resultLines.forEach { line -> Text(line, style = MaterialTheme.typography.bodyLarge) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldsForMode(mode: QuickSolveMode, fields: Map<String, String>, onChange: (String, String) -> Unit) {
    val keys: List<Pair<String, String>> = when (mode) {
        QuickSolveMode.LINEAR -> listOf("a" to "a (coefficient of x)", "b" to "b (constant)")
        QuickSolveMode.QUADRATIC -> listOf("a" to "a", "b" to "b", "c" to "c")
        QuickSolveMode.SYSTEM_2X2 -> listOf(
            "a1" to "a1", "b1" to "b1", "c1" to "c1 (a1*x + b1*y = c1)",
            "a2" to "a2", "b2" to "b2", "c2" to "c2 (a2*x + b2*y = c2)"
        )
        QuickSolveMode.NUMBER_THEORY -> listOf("a" to "A", "b" to "B")
        QuickSolveMode.SEQUENCE -> listOf("a1" to "First term", "d" to "Common difference / ratio", "n" to "Term index n")
        QuickSolveMode.STATISTICS -> listOf("data" to "Data (comma-separated)")
        QuickSolveMode.MATRIX -> listOf("a11" to "a11", "a12" to "a12", "a21" to "a21", "a22" to "a22")
    }
    keys.forEach { (key, label) ->
        OutlinedTextField(
            value = fields[key] ?: "",
            onValueChange = { onChange(key, it) },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).semantics { contentDescription = "Input $label" }
        )
    }
}
