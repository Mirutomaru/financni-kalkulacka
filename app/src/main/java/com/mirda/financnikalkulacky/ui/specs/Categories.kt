package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Work
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.Category

/** Rejstřík 5 kategorií (Level 1 bubliny → Level 2 dlaždice). */
val CATEGORIES: List<Category> = listOf(
    Category("bydleni", "Bydlení a nemovitosti", Icons.Filled.Home, HOUSING_SPECS),
    Category("investice", "Investice & spoření", Icons.Filled.Savings, INVESTMENT_SPECS),
    Category("prace", "Práce & daně", Icons.Filled.Work, WORK_SPECS),
    Category("rodina", "Rodina & životní události", Icons.Filled.People, FAMILY_SPECS),
    Category("uveery", "Úvěry & spotřeba", Icons.Filled.AccountBalanceWallet, LOANS_SPECS),
)

/** Globální rejstřík všech kalkulaček — pro křížové propojení podle ID (related chips, etapy). */
val ALL_SPECS: List<CalcSpec> = CATEGORIES.flatMap { it.specs }

/** Vyhledá specifikaci podle ID (první shoda; ID jsou unikátní). */
fun findSpec(id: String): CalcSpec? = ALL_SPECS.firstOrNull { it.id == id }
