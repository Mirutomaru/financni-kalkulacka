package com.mirda.financnikalkulacky.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirda.financnikalkulacky.R
import com.mirda.financnikalkulacky.components.LineChart
import com.mirda.financnikalkulacky.components.fmtNum
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.specs.findSpec
import com.mirda.financnikalkulacky.ui.theme.BrandGreenDark

/** Level 3 — formulář kalkulačky: slidery, přepínače a živé výsledky. */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CalcFormScreen(spec: CalcSpec, onBack: () -> Unit, onOpenCalc: (String) -> Unit = {}) {
    var values by remember { mutableStateOf(spec.fields.associate { it.key to it.default }) }
    var toggles by remember { mutableStateOf(spec.toggles.associate { it.key to false }) }

    val results = remember(values, toggles) { spec.compute(values, toggles) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = spec.title) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět")
                    }
                },
            )
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            spec.fields.forEach { f ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = f.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            val shown = values[f.key]!!
                            Text(
                                text = fmtNum(shown.toDouble()) + (if (f.unit.isEmpty()) " Kč" else " ${f.unit}"),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Slider(
                            value = values[f.key]!!,
                            onValueChange = { values = values.toMutableMap().apply { put(f.key, it) } },
                            valueRange = f.min..f.max,
                            steps = ((f.max - f.min) / f.step).toInt() - 1,
                        )
                    }
                }
            }

            spec.toggles.forEach { t ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = t.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Switch(checked = toggles[t.key] == true, onCheckedChange = { c -> toggles = toggles.toMutableMap().apply { put(t.key, c) } })
                    }
                }
            }

            spec.charts?.let { chartsFn ->
                val series = remember(values, toggles) { runCatching { chartsFn(values, toggles) }.getOrDefault(emptyList()) }
                if (series.isNotEmpty()) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Graf", style = MaterialTheme.typography.titleMedium)
                            LineChart(series = series, height = 200.dp)
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = BrandGreenDark)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Výsledek", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    results.forEach { r ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(text = r.label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = r.value, style = MaterialTheme.typography.titleSmall, color = Color.White)
                        }
                    }
                }
            }

            // Související kalkulačky — klikací chips (Tip 3).
            val relatedSpecs = remember { spec.related.mapNotNull { findSpec(it) } }
            if (relatedSpecs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.calc_related_header), style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.9f))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        relatedSpecs.forEach { rel ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                                    .clickable(onClick = { onOpenCalc(rel.id) })
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(text = rel.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Upozornění na orientační výpočet (Tip 4).
            Text(
                text = stringResource(R.string.calc_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}
