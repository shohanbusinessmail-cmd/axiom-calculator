package com.axiom.calculator.feature.standard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiom.calculator.ui.theme.CalculatorExpressionStyle
import com.axiom.calculator.ui.theme.CalculatorResultStyle

private data class CalcKey(val label: String, val contentDescription: String, val isAccent: Boolean = false, val isDanger: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardScreen(viewModel: StandardViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(title = { Text("Standard") })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Memory indicator
            if (state.memoryHasValue) {
                AssistChip(onClick = {}, label = { Text("M") }, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
            }

            // Expression + result display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                BasicText(
                    text = state.expression.ifEmpty { "0" },
                    style = CalculatorExpressionStyle.copy(
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .semantics { contentDescription = "Expression: ${state.expression}" }
                )
                Spacer(Modifier.height(8.dp))
                AnimatedContent(
                    targetState = state.errorMessage ?: state.livePreview ?: "0",
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "result"
                ) { text ->
                    Text(
                        text = text,
                        style = CalculatorResultStyle,
                        textAlign = TextAlign.End,
                        color = if (state.errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = if (state.errorMessage != null) "Error: ${state.errorMessage}" else "Result: $text" }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            MemoryRow(
                onMc = viewModel::onMemoryClear, onMr = viewModel::onMemoryRecall,
                onMPlus = viewModel::onMemoryAdd, onMMinus = viewModel::onMemorySubtract, onMs = viewModel::onMemoryStore,
                onUndo = viewModel::onUndo, onRedo = viewModel::onRedo
            )

            Spacer(Modifier.height(8.dp))

            Keypad(viewModel)
        }
    }
}

@Composable
private fun MemoryRow(
    onMc: () -> Unit, onMr: () -> Unit, onMPlus: () -> Unit, onMMinus: () -> Unit, onMs: () -> Unit,
    onUndo: () -> Unit, onRedo: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("MC" to onMc, "MR" to onMr, "M+" to onMPlus, "M-" to onMMinus, "MS" to onMs, "↶" to onUndo, "↷" to onRedo).forEach { (label, action) ->
            TextButton(onClick = action, modifier = Modifier.semantics { contentDescription = "Memory action $label" }) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun Keypad(viewModel: StandardViewModel) {
    val rows: List<List<CalcKey>> = listOf(
        listOf(CalcKey("AC", "All clear", isDanger = true), CalcKey("(", "Open parenthesis"), CalcKey(")", "Close parenthesis"), CalcKey("%", "Percent")),
        listOf(CalcKey("7", "7"), CalcKey("8", "8"), CalcKey("9", "9"), CalcKey("÷", "Divide", isAccent = true)),
        listOf(CalcKey("4", "4"), CalcKey("5", "5"), CalcKey("6", "6"), CalcKey("×", "Multiply", isAccent = true)),
        listOf(CalcKey("1", "1"), CalcKey("2", "2"), CalcKey("3", "3"), CalcKey("−", "Subtract", isAccent = true)),
        listOf(CalcKey("±", "Toggle sign"), CalcKey("0", "0"), CalcKey(".", "Decimal point"), CalcKey("+", "Add", isAccent = true)),
        listOf(CalcKey("⌫", "Backspace"), CalcKey("=", "Equals", isAccent = true))
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    val weight = if (key.label == "=" ) 3f else 1f
                    CalcButton(key = key, modifier = Modifier.weight(weight).height(64.dp)) {
                        handleKey(viewModel, key.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcButton(key: CalcKey, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val containerColor = when {
        key.isDanger -> MaterialTheme.colorScheme.errorContainer
        key.isAccent -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val icon = if (key.label == "⌫") Icons.AutoMirrored.Filled.Backspace else null
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = key.contentDescription },
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = containerColor)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
        } else {
            Text(key.label, style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun handleKey(viewModel: StandardViewModel, label: String) {
    when (label) {
        in "0".."9" -> viewModel.onDigit(label[0])
        "." -> viewModel.onDecimalPoint()
        "+" -> viewModel.onOperator('+')
        "−" -> viewModel.onOperator('-')
        "×" -> viewModel.onOperator('*')
        "÷" -> viewModel.onOperator('/')
        "(" -> viewModel.onOpenParen()
        ")" -> viewModel.onCloseParen()
        "%" -> viewModel.onPercent()
        "±" -> viewModel.onToggleSign()
        "⌫" -> viewModel.onBackspace()
        "AC" -> viewModel.onClearAll()
        "=" -> viewModel.onEquals()
    }
}
