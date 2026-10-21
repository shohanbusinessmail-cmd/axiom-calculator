package com.axiom.calculator.feature.tools

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiom.calculator.core.programmer.BitWidth
import com.axiom.calculator.core.programmer.NumberBase
import com.axiom.calculator.core.units.UnitCategory
import com.axiom.calculator.core.units.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(viewModel: ToolsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Tools") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolsMode.entries.forEach { mode ->
                    FilterChip(selected = state.mode == mode, onClick = { viewModel.onModeChange(mode) }, label = { Text(mode.label) })
                }
            }
            Spacer(Modifier.height(16.dp))

            when (state.mode) {
                ToolsMode.UNIT_CONVERTER -> UnitConverterForm(state, viewModel)
                ToolsMode.PROGRAMMER -> ProgrammerForm(state, viewModel)
                ToolsMode.DATE_TIME -> DateTimeForm(state, viewModel)
                ToolsMode.BMI_BMR -> BmiBmrForm(state, viewModel)
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = viewModel::onRun, modifier = Modifier.semantics { contentDescription = "Calculate" }) { Text("Calculate") }
            Spacer(Modifier.height(16.dp))

            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.resultLines.isNotEmpty()) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        state.resultLines.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitConverterForm(state: ToolsUiState, viewModel: ToolsViewModel) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UnitCategory.entries.forEach { cat ->
            FilterChip(selected = state.unitCategory == cat, onClick = { viewModel.onUnitCategoryChange(cat) }, label = { Text(cat.displayName) })
        }
    }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = state.fields["value"] ?: "", onValueChange = { viewModel.onFieldChange("value", it) },
        label = { Text("Value") }, singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    if (state.unitCategory == UnitCategory.TEMPERATURE) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UnitConverter.TemperatureUnit.entries.forEach { u ->
                FilterChip(selected = state.fields["from"] == u.name, onClick = { viewModel.onFieldChange("from", u.name) }, label = { Text(u.name) })
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UnitConverter.TemperatureUnit.entries.forEach { u ->
                FilterChip(selected = state.fields["to"] == u.name, onClick = { viewModel.onFieldChange("to", u.name) }, label = { Text(u.name) })
            }
        }
    } else {
        val units = UnitConverter.unitsFor(state.unitCategory)
        Text("From:", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            units.forEach { u -> FilterChip(selected = state.fields["from"] == u.id, onClick = { viewModel.onFieldChange("from", u.id) }, label = { Text(u.label) }) }
        }
        Spacer(Modifier.height(4.dp))
        Text("To:", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            units.forEach { u -> FilterChip(selected = state.fields["to"] == u.id, onClick = { viewModel.onFieldChange("to", u.id) }, label = { Text(u.label) }) }
        }
    }
}

@Composable
private fun ProgrammerForm(state: ToolsUiState, viewModel: ToolsViewModel) {
    OutlinedTextField(value = state.fields["value"] ?: "", onValueChange = { viewModel.onFieldChange("value", it) }, label = { Text("Value") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Text("From base:", style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberBase.entries.forEach { b -> FilterChip(selected = state.fromBase == b, onClick = { viewModel.onFromBaseChange(b) }, label = { Text(b.label) }) }
    }
    Spacer(Modifier.height(4.dp))
    Text("To base:", style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberBase.entries.forEach { b -> FilterChip(selected = state.toBase == b, onClick = { viewModel.onToBaseChange(b) }, label = { Text(b.label) }) }
    }
    Spacer(Modifier.height(8.dp))
    Text("Bit width:", style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BitWidth.entries.forEach { w -> FilterChip(selected = state.bitWidth == w, onClick = { viewModel.onBitWidthChange(w) }, label = { Text("${w.bits}-bit") }) }
    }
    Spacer(Modifier.height(8.dp))
    Text("Optional bitwise operation:", style = MaterialTheme.typography.labelLarge)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("AND", "OR", "XOR", "NOT", "SHL", "SHR_LOGICAL", "SHR_ARITH").forEach { op ->
            FilterChip(selected = state.fields["bitwiseOp"] == op, onClick = { viewModel.onFieldChange("bitwiseOp", op) }, label = { Text(op) })
        }
    }
    Spacer(Modifier.height(4.dp))
    OutlinedTextField(value = state.fields["value2"] ?: "", onValueChange = { viewModel.onFieldChange("value2", it) }, label = { Text("Second operand / shift amount") }, singleLine = true, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun DateTimeForm(state: ToolsUiState, viewModel: ToolsViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("difference" to "Difference", "addDays" to "Add days", "addMonths" to "Add months").forEach { (key, label) ->
            FilterChip(selected = (state.fields["op"] ?: "difference") == key, onClick = { viewModel.onFieldChange("op", key) }, label = { Text(label) })
        }
    }
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(value = state.fields["start"] ?: "", onValueChange = { viewModel.onFieldChange("start", it) }, label = { Text("Start date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    when (state.fields["op"] ?: "difference") {
        "difference" -> OutlinedTextField(value = state.fields["end"] ?: "", onValueChange = { viewModel.onFieldChange("end", it) }, label = { Text("End date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        else -> OutlinedTextField(value = state.fields["amount"] ?: "", onValueChange = { viewModel.onFieldChange("amount", it) }, label = { Text("Amount") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun BmiBmrForm(state: ToolsUiState, viewModel: ToolsViewModel) {
    OutlinedTextField(value = state.fields["weight"] ?: "", onValueChange = { viewModel.onFieldChange("weight", it) }, label = { Text("Weight (kg)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(value = state.fields["height"] ?: "", onValueChange = { viewModel.onFieldChange("height", it) }, label = { Text("Height (cm)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Text("Optional, for BMR:", style = MaterialTheme.typography.labelLarge)
    OutlinedTextField(value = state.fields["age"] ?: "", onValueChange = { viewModel.onFieldChange("age", it) }, label = { Text("Age (years)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("male" to "Male", "female" to "Female").forEach { (key, label) ->
            FilterChip(selected = state.fields["sex"] == key, onClick = { viewModel.onFieldChange("sex", key) }, label = { Text(label) })
        }
    }
}
