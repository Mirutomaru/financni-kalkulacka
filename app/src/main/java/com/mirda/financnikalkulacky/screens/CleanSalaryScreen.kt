package com.mirda.financnikalkulacky.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mirda.financnikalkulacky.components.fmtCzk
import com.mirda.financnikalkulacky.R
import com.mirda.financnikalkulacky.calculators.CleanSalaryCalculator
import com.mirda.financnikalkulacky.components.HighlightResult
import com.mirda.financnikalkulacky.components.ResultRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanSalaryScreen(onBack: () -> Unit) {
    var grossText by remember { mutableStateOf("45000") }
    var children by remember { mutableStateOf(0f) }
    val gross = grossText.toDoubleOrNull() ?: 0.0
    val result = CleanSalaryCalculator.calculate(gross, children.toInt())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calc_cista_mzda)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.mzda_label_gross), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = grossText,
                        onValueChange = { grossText = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    Text(
                        "${stringResource(R.string.mzda_label_children)}: ${children.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Slider(value = children, onValueChange = { children = it }, valueRange = 0f..5f, steps = 4)
                }
            }
            if (gross > 0) {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HighlightResult(stringResource(R.string.mzda_result_net), fmtCzk(result.netMonthly))
                        ResultRow(stringResource(R.string.mzda_row_tax), "- " + fmtCzk(result.taxMonthly))
                        ResultRow(stringResource(R.string.mzda_row_social), "- " + fmtCzk(result.socialInsurance))
                        ResultRow(stringResource(R.string.mzda_row_health), "- " + fmtCzk(result.healthInsurance))
                    }
                }
            } else {
                Text(stringResource(R.string.mzda_error_salary), color = MaterialTheme.colorScheme.error)
            }

            Text(stringResource(R.string.mzda_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
