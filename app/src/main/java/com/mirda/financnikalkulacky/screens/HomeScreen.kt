package com.mirda.financnikalkulacky.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirda.financnikalkulacky.R
import com.mirda.financnikalkulacky.components.BubbleButton
import com.mirda.financnikalkulacky.ui.specs.CATEGORIES
import com.mirda.financnikalkulacky.ui.specs.LIFE_STAGES
import kotlinx.coroutines.launch

/** Hlavní obrazovka — bubliny kategorií (Level 1) + sekce životních etap. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onCategoryClick: (String) -> Unit, onStageClick: (String) -> Unit, onCalcClick: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var dialog by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.drawer_calculators), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    CATEGORIES.forEach { cat ->
                        Spacer(Modifier.height(8.dp))
                        Text(cat.name, style = MaterialTheme.typography.titleSmall)
                        cat.specs.forEach { spec ->
                            TextButton(onClick = { scope.launch { drawerState.close() }; onCalcClick(spec.id) }) {
                                Icon(spec.icon, null, Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(spec.title)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { scope.launch { drawerState.close() }; dialog = "stats" }) {
                        Icon(Icons.Filled.Insights, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.drawer_stats))
                    }
                    TextButton(onClick = { scope.launch { drawerState.close() }; dialog = "feedback" }) {
                        Icon(Icons.Filled.Feedback, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.drawer_feedback))
                    }
                    TextButton(onClick = { scope.launch { drawerState.close() }; dialog = "privacy" }) {
                        Icon(Icons.Filled.Lock, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.drawer_privacy))
                    }
                }
            }
        },
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.MenuOpen, stringResource(R.string.menu_open))
                        }
                    },
                    actions = {
                        IconButton(onClick = { dialog = "settings" }) {
                            Icon(Icons.Filled.Settings, stringResource(R.string.action_settings))
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                // Kategorie — po dvou bublinách na řádek.
                items(CATEGORIES.chunked(2)) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { cat ->
                            BubbleButton(icon = cat.icon, label = cat.name) { onCategoryClick(cat.id) }
                        }
                    }
                }
                item { Spacer(Modifier.height(28.dp)) }
                // Životní etapy — alternativní navigace (Level 1 → etapa → dlaždice).
                item {
                    Text(stringResource(R.string.life_stages_header), style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                items(LIFE_STAGES) { stg ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        BubbleButton(icon = stg.icon, label = stg.name) { onStageClick(stg.id) }
                    }
                }
            }
        }

        val key = dialog ?: ""
        val (title, body) = when (key) {
            "settings" -> stringResource(R.string.about_title) to stringResource(R.string.privacy_text)
            "stats" -> stringResource(R.string.drawer_stats) to stringResource(R.string.stats_text)
            "feedback" -> stringResource(R.string.drawer_feedback) to stringResource(R.string.feedback_text)
            else -> stringResource(R.string.drawer_privacy) to stringResource(R.string.privacy_text)
        }

        if (key.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text(title) },
                text = { Text(body) },
                confirmButton = {
                    TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.ok)) }
                },
            )
        }
    }
}
