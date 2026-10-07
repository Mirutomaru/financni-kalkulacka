package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import com.mirda.financnikalkulacky.calculators.investments.InvestmentCalculator
import com.mirda.financnikalkulacky.components.fmtCzk
import com.mirda.financnikalkulacky.ui.model.CalcResult
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.Field

val INVESTMENT_SPECS: List<CalcSpec> = listOf(
    CalcSpec(
        id = "sporeni",
        title = "Složené úročení & spoření",
        description = "Budoucí hodnota vkladu a pravidelných úložek.",
        icon = Icons.Filled.Savings,
        fields = listOf(
            Field("principal", "Počáteční vklad", 0f, 5_000_000f, 10_000f, 100_000f, "Kč"),
            Field("pmt", "Měsíční úložka", 0f, 50_000f, 500f, 5_000f, "Kč"),
            Field("rate", "Roční zhodnocení", 0f, 15f, 0.1f, 6f, "%"),
            Field("years", "Doba spoření", 1f, 40f, 1f, 20f, "let"),
        ),
    ) { v, _ ->
        val r = InvestmentCalculator.futureValue(v["principal"]!!.toDouble(), v["pmt"]!!.toDouble(), v["rate"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Finální částka", fmtCzk(r.finalValue)),
            CalcResult("Vloženo celkem", fmtCzk(r.totalContributions)),
            CalcResult("Zisk z úroků", fmtCzk(r.profit)),
        )
    },

    CalcSpec(
        id = "fire",
        title = "FIRE číslo & měsíce do cíle",
        description = "Potřebné portfolio a čas k dosažení cílové částky.",
        icon = Icons.Filled.EmojiEvents,
        fields = listOf(
            Field("rent", "Požadovaný měsíční příjem", 10_000f, 200_000f, 5_000f, 40_000f, "Kč"),
            Field("swr", "Sazba výběru (SWR)", 2f, 6f, 0.1f, 4f, "%"),
            Field("principal", "Počáteční vklad", 0f, 5_000_000f, 10_000f, 500_000f, "Kč"),
            Field("pmt", "Měsíční úložka", 0f, 50_000f, 500f, 10_000f, "Kč"),
            Field("rate", "Roční zhodnocení", 0f, 15f, 0.1f, 6f, "%"),
        ),
    ) { v, _ ->
        val fire = InvestmentCalculator.fireNumber(v["rent"]!!.toDouble(), v["swr"]!!.toDouble())
        val months = InvestmentCalculator.monthsToTarget(v["principal"]!!.toDouble(), v["pmt"]!!.toDouble(), v["rate"]!!.toDouble(), fire)
        listOf(
            CalcResult("FIRE číslo", fmtCzk(fire)),
            CalcResult("Měsíce do cíle", if (months < 0) "nedosažitelné" else "${months} měs."),
        )
    },

    CalcSpec(
        id = "dividendy",
        title = "Dividendové portfolio",
        description = "Pasivní příjem z dividend po srážkové dani.",
        icon = Icons.Filled.Payments,
        fields = listOf(
            Field("portfolio", "Hodnota portfolia", 100_000f, 20_000_000f, 50_000f, 1_000_000f, "Kč"),
            Field("yield", "Dividendový výnos (DY)", 0f, 10f, 0.1f, 4f, "%"),
            Field("tax", "Srážková daň", 0f, 25f, 1f, 15f, "%"),
        ),
    ) { v, _ ->
        val r = InvestmentCalculator.dividends(v["portfolio"]!!.toDouble(), v["yield"]!!.toDouble(), v["tax"]!!.toDouble())
        listOf(
            CalcResult("Hrubé dividendy (rok)", fmtCzk(r.grossAnnual)),
            CalcResult("Čisté dividendy (rok)", fmtCzk(r.netAnnual)),
            CalcResult("Měsíční pasivní příjem", fmtCzk(r.monthlyNet)),
        )
    },

    CalcSpec(
        id = "dip",
        title = "Daňová úspora DIP / penze",
        description = "Úspora daně z odpočtu příspěvků (max 48 tis/rok).",
        icon = Icons.Filled.AccountBalance,
        fields = listOf(
            Field("userMonthly", "Příspěvek zaměstnance (měs.)", 0f, 10_000f, 500f, 2_000f, "Kč"),
            Field("employerMonthly", "Příspěvek zaměstnavatele (měs.)", 0f, 10_000f, 500f, 1_000f, "Kč"),
            Field("taxRate", "Daňová sazba", 0f, 40f, 1f, 15f, "%"),
        ),
    ) { v, _ ->
        val r = InvestmentCalculator.dipTaxSaving(v["userMonthly"]!!.toDouble(), v["employerMonthly"]!!.toDouble(), v["taxRate"]!!.toDouble())
        listOf(
            CalcResult("Odpočet ze základu (rok)", fmtCzk(r.deductible)),
            CalcResult("Daňová úspora (rok)", fmtCzk(r.annualTaxSaving)),
            CalcResult("Celkový roční kapitál", fmtCzk(r.totalAnnualCapital)),
        )
    },

    CalcSpec(
        id = "inflace",
        title = "Vliv inflace na kupní sílu",
        description = "Reálná hodnota úspor po zvoleném počtu let.",
        icon = Icons.Filled.TrendingUp,
        fields = listOf(
            Field("amount", "Částka (Kč)", 10_000f, 5_000_000f, 10_000f, 100_000f, "Kč"),
            Field("inflation", "Roční inflace", 0f, 15f, 0.1f, 5f, "%"),
            Field("years", "Počet let", 1f, 40f, 1f, 20f, "let"),
        ),
    ) { v, _ ->
        val r = InvestmentCalculator.inflationRealValue(v["amount"]!!.toDouble(), v["inflation"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Reálná kupní síla", fmtCzk(r.realValue)),
            CalcResult("Ztráta kupní sily", fmtCzk(r.lossOfPurchasingPower)),
        )
    },
)
