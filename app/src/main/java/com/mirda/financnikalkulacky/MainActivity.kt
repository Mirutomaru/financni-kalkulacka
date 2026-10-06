package com.mirda.financnikalkulacky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.mirda.financnikalkulacky.screens.CALCULATORS
import com.mirda.financnikalkulacky.screens.CleanSalaryScreen
import com.mirda.financnikalkulacky.screens.ComingSoonScreen
import com.mirda.financnikalkulacky.screens.HomeScreen
import com.mirda.financnikalkulacky.ui.theme.FinancniKalkulackyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinancniKalkulackyTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    var selected by remember { mutableStateOf<String?>(null) }
    val item = CALCULATORS.firstOrNull { it.id == selected }

    when {
        item == null -> HomeScreen(onCalculatorClick = { selected = it })
        item.id == "mzda" -> CleanSalaryScreen(onBack = { selected = null })
        else -> ComingSoonScreen(title = stringResource(item.labelRes), onBack = { selected = null })
    }
}
