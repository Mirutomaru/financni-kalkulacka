package com.mirda.financnikalkulacky.ui.model

import androidx.compose.ui.graphics.vector.ImageVector

/** Číselný vstup (posuvník) kalkulačky. */
data class Field(
    val key: String,
    val label: String,
    val min: Float,
    val max: Float,
    val step: Float = 1f,
    val default: Float,
    val unit: String = "",
)

/** Přepínač (toggle) kalkulačky. */
data class ToggleField(
    val key: String,
    val label: String,
    val default: Boolean = false,
)

/** Jeden výstupní řádek výsledku. */
data class CalcResult(val label: String, val value: String)

/** Bod grafu (x = osa vpravo dole, y = hodnota). */
data class ChartPoint(val x: Float, val y: Double)

/** Série pro line chart — jedna křivka. */
data class ChartSeries(
    val label: String,
    val color: androidx.compose.ui.graphics.Color,
    val points: List<ChartPoint>,
)

/** Specifikace jedné kalkulačky pro generický formulář (Level 3). */
data class CalcSpec(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    /** Volitelná podskupina v rámci kategorie (nadpis na Level 2). */
    val group: String = "",
    val fields: List<Field>,
    val toggles: List<ToggleField> = emptyList(),
    /** ID souvisejících kalkulaček — zobrazí se jako chips pod výsledkem. */
    val related: List<String> = emptyList(),
    /** Volitelné grafy (payoff křivka apod.) — volané při každém přepočtu. */
    val charts: ((Map<String, Float>, Map<String, Boolean>) -> List<ChartSeries>)? = null,
    val compute: (Map<String, Float>, Map<String, Boolean>) -> List<CalcResult>,
)

/** Kategorie (Level 1 bublina → Level 2 dlaždice). */
data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val specs: List<CalcSpec>,
)
