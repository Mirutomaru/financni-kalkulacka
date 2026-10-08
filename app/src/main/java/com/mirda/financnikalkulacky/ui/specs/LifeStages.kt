package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.mirda.financnikalkulacky.ui.model.CalcSpec

/**
 * Životní etapa — soubor kalkulaček, které dává smysl řešit v dané fázi života.
 * Slouží jako alternativní navigace vedle kategorií (Level 1 → etapy → dlaždice).
 */
data class LifeStage(
    val id: String,
    val name: String,
    val icon: ImageVector,
    /** ID kalkulaček v doporučeném pořadí. */
    val calcIds: List<String>,
) {
    /** Rozliší ID na konkrétní specy (v daném pořadí); neznámá ID vynechá. */
    fun specs(): List<CalcSpec> = calcIds.mapNotNull { id -> findSpec(id) }
}

/** Rejstřík životních etap (Level 1 bublina → Level 2 dlaždice). */
val LIFE_STAGES: List<LifeStage> = listOf(
    LifeStage(
        id = "prvni-prace",
        name = "První krok do života",
        icon = Icons.Filled.Work,
        calcIds = listOf("mzda", "obraceda-mzda", "dpp", "prescasy", "dovolena", "cestovne"),
    ),
    LifeStage(
        id = "koup-bydleni",
        name = "Koupě bydlení",
        icon = Icons.Filled.Home,
        calcIds = listOf("hypoteka", "bonita", "vedlejni-naklady", "vystavba-domu", "koupvejnajem"),
    ),
    LifeStage(
        id = "rodina",
        name = "Založení rodiny",
        icon = Icons.Filled.ChildCare,
        calcIds = listOf("miminko", "mateřska", "otcovska", "osetrowne", "vyzivne", "pridavky"),
    ),
    LifeStage(
        id = "podnikani",
        name = "Podnikání na vlastní nohy",
        icon = Icons.Filled.Store,
        calcIds = listOf("osvc", "dph", "hodinova-sazba", "odpisy", "srovnanie-formy"),
    ),
    LifeStage(
        id = "sporeni-budoucnost",
        name = "Spoření do budoucna",
        icon = Icons.Filled.Savings,
        calcIds = listOf("nouzova-rezerva", "sporeni", "dps", "fire", "statni-duchod", "duchodova-mezera"),
    ),
    LifeStage(
        id = "dedictvi",
        name = "Konec života a dědictví",
        icon = Icons.Filled.Gavel,
        calcIds = listOf("pohreb", "dedicke-rijeni", "dar-vs-dedictvi", "pozustalostni"),
    ),
)
