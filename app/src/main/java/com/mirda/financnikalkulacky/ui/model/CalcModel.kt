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

/** Specifikace jedné kalkulačky pro generický formulář (Level 3). */
data class CalcSpec(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val fields: List<Field>,
    val toggles: List<ToggleField> = emptyList(),
    val compute: (Map<String, Float>, Map<String, Boolean>) -> List<CalcResult>,
)

/** Kategorie (Level 1 bublina → Level 2 dlaždice). */
data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val specs: List<CalcSpec>,
)
