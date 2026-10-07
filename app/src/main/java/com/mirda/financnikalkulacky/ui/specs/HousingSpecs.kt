package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Scale
import com.mirda.financnikalkulacky.calculators.housing.BuyVsRentCalculator
import com.mirda.financnikalkulacky.calculators.housing.MortgageCalculator
import com.mirda.financnikalkulacky.calculators.housing.RentalYieldCalculator
import com.mirda.financnikalkulacky.components.fmtCzk
import com.mirda.financnikalkulacky.ui.model.CalcResult
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.Field
import com.mirda.financnikalkulacky.ui.model.ToggleField

private fun pct(v: Double) = String.format("%.2f %%", v)

val HOUSING_SPECS: List<CalcSpec> = listOf(
    CalcSpec(
        id = "hypoteka",
        title = "Hypoteční kalkulačka",
        description = "Měsíční anuitní splátka, celkové úroky a přeplatek.",
        icon = Icons.Filled.Home,
        fields = listOf(
            Field("loan", "Výše úvěru", 100_000f, 20_000_000f, 50_000f, 3_000_000f, "Kč"),
            Field("rate", "Roční úroková sazba", 0.5f, 12f, 0.1f, 6f, "%"),
            Field("years", "Doba splácení", 5f, 40f, 1f, 30f, "let"),
        ),
    ) { v, _ ->
        val r = MortgageCalculator.calculate(v["loan"]!!.toDouble(), v["rate"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Měsíční splátka", fmtCzk(r.monthlyPayment)),
            CalcResult("Celkem zaplaceno", fmtCzk(r.totalPaid)),
            CalcResult("Úroky celkem (přeplatek)", fmtCzk(r.totalInterest)),
        )
    },

    CalcSpec(
        id = "mimoradna",
        title = "Mimořádná splátka hypotéky",
        description = "Úspora na úrocích při předčasném splacení.",
        icon = Icons.Filled.Key,
        fields = listOf(
            Field("loan", "Zůstatek jistiny", 100_000f, 20_000_000f, 50_000f, 1_500_000f, "Kč"),
            Field("rate", "Roční sazba", 0.5f, 12f, 0.1f, 6f, "%"),
            Field("months", "Zbývající doba", 12f, 480f, 12f, 300f, "měs."),
            Field("extra", "Mimořádná splátka", 10_000f, 2_000_000f, 10_000f, 300_000f, "Kč"),
        ),
        toggles = listOf(ToggleField("goalB", "Snížit splátku (B) — jinak zkrátit dobu (A)")),
    ) { v, t ->
        val r = MortgageCalculator.extraPayment(v["loan"]!!.toDouble(), v["rate"]!!.toDouble(), v["months"]!!.toInt(), v["extra"]!!.toDouble(), t["goalB"] == true)
        if (t["goalB"] == true) listOf(
            CalcResult("Nová měsíční splátka", fmtCzk(r.newMonthlyPayment)),
            CalcResult("Úspora na úrocích", fmtCzk(r.interestSaved)),
        ) else listOf(
            CalcResult("Zkrácení o (měsíce)", r.monthsShortened.toString()),
            CalcResult("Úspora na úrocích", fmtCzk(r.interestSaved)),
        )
    },

    CalcSpec(
        id = "refinancovani",
        title = "Refinancování hypotéky",
        description = "Čistá úspora, nová splátka a bod zvratu.",
        icon = Icons.Filled.SwapHoriz,
        fields = listOf(
            Field("loan", "Zůstatek úvěru", 100_000f, 20_000_000f, 50_000f, 1_500_000f, "Kč"),
            Field("months", "Zbývající doba", 12f, 480f, 12f, 240f, "měs."),
            Field("oldRate", "Původní sazba", 0.5f, 12f, 0.1f, 7f, "%"),
            Field("newRate", "Nová sazba", 0.5f, 12f, 0.1f, 4.5f, "%"),
            Field("fees", "Poplatky za refinancování", 0f, 100_000f, 1_000f, 15_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = MortgageCalculator.refinance(v["loan"]!!.toDouble(), v["months"]!!.toInt(), v["oldRate"]!!.toDouble(), v["newRate"]!!.toDouble(), v["fees"]!!.toDouble())
        listOf(
            CalcResult("Původní splátka", fmtCzk(r.oldPayment)),
            CalcResult("Nová splátka", fmtCzk(r.newPayment)),
            CalcResult("Čistá úspora", fmtCzk(r.netSavings)),
            CalcResult("Bod zvratu (měsíce)", r.breakEvenMonths.toString()),
        )
    },

    CalcSpec(
        id = "renta",
        title = "Investiční byt & rentabilita nájmu",
        description = "Cap Rate, cashflow a návratnost investice.",
        icon = Icons.Filled.TrendingUp,
        fields = listOf(
            Field("price", "Cena + investice", 500_000f, 20_000_000f, 100_000f, 6_000_000f, "Kč"),
            Field("rent", "Měsíční nájemné", 5_000f, 50_000f, 500f, 18_000f, "Kč"),
            Field("vacancy", "Neobsazenost", 0f, 30f, 1f, 8f, "%"),
            Field("opex", "Provozní náklady (měs.)", 0f, 20_000f, 500f, 4_000f, "Kč"),
            Field("tax", "Efektivní daň z nájmu", 0f, 30f, 0.5f, 10.5f, "%"),
        ),
    ) { v, _ ->
        val r = RentalYieldCalculator.calculate(v["price"]!!.toDouble(), v["rent"]!!.toDouble(), v["vacancy"]!!.toDouble(), v["opex"]!!.toDouble(), v["tax"]!!.toDouble())
        listOf(
            CalcResult("Gross Rental Yield", pct(r.grossYieldPct)),
            CalcResult("Net Cap Rate", pct(r.capRatePct)),
            CalcResult("Čistý měsíční cashflow", fmtCzk(r.monthlyCashflow)),
            CalcResult("Návratnost (roky)", if (r.paybackYears > 100) "—" else String.format("%.1f", r.paybackYears)),
        )
    },

    CalcSpec(
        id = "koupvejnajem",
        title = "Koupě vs. Nájem",
        description = "Srovnání čistého jmění po zvoleném horizontu.",
        icon = Icons.Filled.Scale,
        fields = listOf(
            Field("price", "Cena nemovitosti", 1_000_000f, 20_000_000f, 100_000f, 6_000_000f, "Kč"),
            Field("ownFunds", "Vlastní zdroje", 0f, 5_000_000f, 50_000f, 1_200_000f, "Kč"),
            Field("mortRate", "Úrok hypotéky", 0.5f, 12f, 0.1f, 6f, "%"),
            Field("mortYears", "Doba hypotéky", 5f, 40f, 1f, 30f, "let"),
            Field("buyOpex", "Provoz bytu (měs.)", 0f, 20_000f, 500f, 6_000f, "Kč"),
            Field("propGrowth", "Růst cen nemovitostí", -2f, 15f, 0.5f, 3f, "%/rok"),
            Field("rentMonthly", "Nájem srovnatelného bytu", 5_000f, 50_000f, 500f, 18_000f, "Kč"),
            Field("rentGrowth", "Růst nájmu", -2f, 15f, 0.5f, 3f, "%/rok"),
            Field("invReturn", "Výnos investice", 0f, 15f, 0.5f, 6f, "%/rok"),
            Field("horizon", "Horizont srovnání", 5f, 40f, 1f, 25f, "let"),
        ),
    ) { v, _ ->
        val r = BuyVsRentCalculator.calculate(v["price"]!!.toDouble(), v["ownFunds"]!!.toDouble(), v["mortRate"]!!.toDouble(), v["mortYears"]!!.toInt(), v["buyOpex"]!!.toDouble(), v["propGrowth"]!!.toDouble(), v["rentMonthly"]!!.toDouble(), v["rentGrowth"]!!.toDouble(), v["invReturn"]!!.toDouble(), v["horizon"]!!.toInt())
        listOf(
            CalcResult("Čisté jmění — KOUPE", fmtCzk(r.buyNetWorth)),
            CalcResult("Čisté jmění — NÁJEM", fmtCzk(r.rentNetWorth)),
            CalcResult("Výhoda (Kč)", fmtCzk(r.buyNetWorth - r.rentNetWorth)),
        )
    },
)
