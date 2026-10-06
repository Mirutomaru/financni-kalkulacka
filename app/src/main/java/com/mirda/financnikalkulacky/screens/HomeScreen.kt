package com.mirda.financnikalkulacky.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirda.financnikalkulacky.R
import com.mirda.financnikalkulacky.components.BubbleButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

/** Popis jedné kalkulačky v bublinovém menu. */
data class CalculatorItem(
    val id: String,
    val labelRes: Int,
    val icon: ImageVector,
)

val CALCULATORS = listOf(
    CalculatorItem("mzda", R.string.calc_cista_mzda, Icons.Filled.AccountBalanceWallet),
    CalculatorItem("nemocenska", R.string.calc_nemocenska, Icons.Filled.LocalHospital),
    CalculatorItem("sporeni", R.string.calc_sporeni, Icons.Filled.Pets),
    CalculatorItem("hypoteka", R.string.calc_hypoteka, Icons.Filled.Home),
    CalculatorItem("osvc", R.string.calc_osvc, Icons.Filled.AddChart),
    CalculatorItem("penze", R.string.calc_penze, Icons.Filled.Groups),
    CalculatorItem("inflace", R.string.calc_inflace, Icons.Filled.TrendingUp),
    CalculatorItem("investice", R.string.calc_investice, Icons.Filled.Savings),
)

/** Hlavní obrazovka — mřížka bublinových tlačítek kalkulaček. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onCalculatorClick: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var dialog by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.drawer_calculators), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    CALCULATORS.forEach { item ->
                        TextButton(onClick = { scope.launch { drawerState.close() }; onCalculatorClick(item.id) }) {
                            Icon(item.icon, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(item.labelRes))
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
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(CALCULATORS) { item ->
                    BubbleButton(icon = item.icon, label = stringResource(item.labelRes)) { onCalculatorClick(item.id) }
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
