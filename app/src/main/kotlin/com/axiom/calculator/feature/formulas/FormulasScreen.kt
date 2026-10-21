package com.axiom.calculator.feature.formulas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiom.calculator.core.formula.FormulaCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulasScreen(viewModel: FormulasViewModel, onOpenFormula: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Formulas") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search formulas") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).semantics { contentDescription = "Search formulas" }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (category in listOf<FormulaCategory?>(null) + FormulaCategory.entries) {
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category?.displayName ?: "All") }
                    )
                }
            }

            if (state.results.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No formulas match your search.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.results, key = { it.id }) { formula ->
                        ElevatedCard(
                            onClick = { onOpenFormula(formula.id) },
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Open formula ${formula.title}" }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(formula.title, style = MaterialTheme.typography.titleLarge)
                                Text(formula.category.displayName, style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.height(4.dp))
                                Text(formula.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}
