package com.axiom.calculator.feature.scientific

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiom.calculator.core.math.AngleMode
import com.axiom.calculator.ui.theme.CalculatorExpressionStyle
import com.axiom.calculator.ui.theme.CalculatorResultStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScientificScreen(viewModel: ScientificViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Scientific") },
            actions = {
                FilterChip(
                    selected = state.angleMode == AngleMode.DEGREES,
                    onClick = { viewModel.toggleAngleMode() },
                    label = { Text(if (state.angleMode == AngleMode.DEGREES) "DEG" else "RAD") },
                    modifier = Modifier.padding(end = 12.dp).semantics { contentDescription = "Angle mode, currently ${state.angleMode.name}" }
                )
            }
        )
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(12.dp)) {
            Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.End) {
                Text(
                    state.expression.ifEmpty { "0" },
                    style = CalculatorExpressionStyle,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    state.errorMessage ?: state.livePreview ?: "0",
                    style = CalculatorResultStyle.copy(fontSize = 36.sp),
                    textAlign = TextAlign.End,
                    color = if (state.errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = state.errorMessage ?: "Result ${state.livePreview ?: "0"}" }
                )
            }

            Spacer(Modifier.height(8.dp))
            ScientificFunctionRow(viewModel)
            Spacer(Modifier.height(8.dp))
            ScientificKeypad(viewModel)
        }
    }
}

@Composable
private fun ScientificFunctionRow(viewModel: ScientificViewModel) {
    val functions = listOf("sin", "cos", "tan", "ln", "log10", "sqrt", "exp", "asin", "acos", "atan")
    LazyRowOfChips(items = functions) { fn -> viewModel.appendFunction(fn) }
}

@Composable
private fun LazyRowOfChips(items: List<String>, onClick: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items.size) { idx ->
            val label = items[idx]
            SuggestionChip(onClick = { onClick(label) }, label = { Text(label) })
        }
    }
}

@Composable
private fun ScientificKeypad(viewModel: ScientificViewModel) {
    val rows = listOf(
        listOf("(", ")", "π", "e", "^"),
        listOf("7", "8", "9", "÷", "!"),
        listOf("4", "5", "6", "×", "%"),
        listOf("1", "2", "3", "−", "AC"),
        listOf("0", ".", "⌫", "+", "=")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    FilledTonalButton(
                        onClick = { handleScientificKey(viewModel, key) },
                        modifier = Modifier.weight(1f).height(56.dp).semantics { contentDescription = "Key $key" }
                    ) { Text(key) }
                }
            }
        }
    }
}

private fun handleScientificKey(viewModel: ScientificViewModel, key: String) {
    when (key) {
        "AC" -> viewModel.clearAll()
        "⌫" -> viewModel.backspace()
        "=" -> viewModel.onEquals()
        "÷" -> viewModel.appendRaw("/")
        "×" -> viewModel.appendRaw("*")
        "−" -> viewModel.appendRaw("-")
        "π" -> viewModel.appendRaw("pi")
        else -> viewModel.appendRaw(key)
    }
}
