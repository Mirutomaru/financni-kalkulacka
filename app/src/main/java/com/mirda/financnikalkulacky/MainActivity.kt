package com.mirda.financnikalkulacky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mirda.financnikalkulacky.screens.CalcFormScreen
import com.mirda.financnikalkulacky.screens.CategoryScreen
import com.mirda.financnikalkulacky.screens.HomeScreen
import com.mirda.financnikalkulacky.ui.specs.CATEGORIES
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

/** Kořenová navigace: Home (kategorie) → Category (dlaždice) → CalcForm. */
@Composable
fun AppRoot() {
    var category by remember { mutableStateOf<String?>(null) }
    var calcId by remember { mutableStateOf<String?>(null) }

    val cat = CATEGORIES.firstOrNull { it.id == category }
    val spec = cat?.specs?.firstOrNull { it.id == calcId } ?:
        CATEGORIES.flatMap { c -> c.specs }.firstOrNull { s -> s.id == calcId }

    when {
        spec != null -> CalcFormScreen(spec = spec, onBack = { calcId = null })
        cat != null -> CategoryScreen(category = cat, onBack = { category = null }, onOpenCalc = { calcId = it })
        else -> HomeScreen(
            onCategoryClick = { c -> category = c; calcId = null },
            onCalcClick = { c -> calcId = c },
        )
    }
}
