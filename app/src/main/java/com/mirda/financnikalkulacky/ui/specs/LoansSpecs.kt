package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MergeType
import com.mirda.financnikalkulacky.calculators.loans.CarTcoCalculator
import com.mirda.financnikalkulacky.calculators.loans.ConsolidationCalculator
import com.mirda.financnikalkulacky.components.fmtCzk
import com.mirda.financnikalkulacky.ui.model.CalcResult
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.Field

val LOANS_SPECS: List<CalcSpec> = listOf(
    CalcSpec(
        id = "konsolidace",
        title = "Konsolidace půjček",
        description = "Sjednocení dluhů do jedné půjčky s nižší splátkou.",
        icon = Icons.Filled.MergeType,
        fields = listOf(
            Field("d1debt", "Půjčka 1 — jistina", 0f, 2_000_000f, 10_000f, 100_000f, "Kč"),
            Field("d1pay", "Půjčka 1 — měsíční splátka", 0f, 50_000f, 500f, 3_500f, "Kč"),
            Field("d2debt", "Půjčka 2 — jistina", 0f, 2_000_000f, 10_000f, 60_000f, "Kč"),
            Field("d2pay", "Půjčka 2 — měsíční splátka", 0f, 50_000f, 500f, 2_200f, "Kč"),
            Field("rate", "Úrok nové půjčky", 3f, 15f, 0.1f, 7f, "%"),
            Field("years", "Doba splácení", 1f, 10f, 1f, 5f, "let"),
        ),
    ) { v, _ ->
        val debts = listOf(
            v["d1debt"]!!.toDouble() to v["d1pay"]!!.toDouble(),
            v["d2debt"]!!.toDouble() to v["d2pay"]!!.toDouble(),
        ).filter { it.first > 0.0 }
        if (debts.isEmpty()) listOf(CalcResult("Zadejte alespoň jednu půjčku", "—")) else {
            val r = ConsolidationCalculator.calculate(debts, v["rate"]!!.toDouble(), v["years"]!!.toInt())
            listOf(
                CalcResult("Součet dluhů", fmtCzk(r.totalDebt)),
                CalcResult("Stávající splátky (měs.)", fmtCzk(r.oldTotalPayment)),
                CalcResult("Nová sjednocená splátka", fmtCzk(r.newMonthlyPayment)),
                CalcResult("Odlehčení rozpočtu (měs.)", fmtCzk(r.monthlyRelief)),
            )
        }
    },

    CalcSpec(
        id = "auto",
        title = "Skutečná cena auta (TCO)",
        description = "Amortizace, palivo a fixní náklady — cena za 1 km.",
        icon = Icons.Filled.DirectionsCar,
        fields = listOf(
            Field("price", "Pořizovací cena", 100_000f, 2_000_000f, 50_000f, 400_000f, "Kč"),
            Field("years", "Doba držení", 1f, 15f, 1f, 8f, "let"),
            Field("resale", "Zůstatková cena při prodeji", 0f, 1_500_000f, 25_000f, 150_000f, "Kč"),
            Field("km", "Roční nájezd", 1_000f, 60_000f, 1_000f, 15_000f, "km"),
            Field("consumption", "Spotřeba na 100 km", 2f, 30f, 0.5f, 7f, ""),
            Field("fuelPrice", "Cena paliva/energie (l/kWh)", 10f, 100f, 1f, 60f, "Kč"),
            Field("fixed", "Fixní náklady (rok)", 5_000f, 80_000f, 1_000f, 24_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = CarTcoCalculator.calculate(v["price"]!!.toDouble(), v["years"]!!.toInt(), v["resale"]!!.toDouble(), v["km"]!!.toDouble(), v["consumption"]!!.toDouble(), v["fuelPrice"]!!.toDouble(), v["fixed"]!!.toDouble())
        listOf(
            CalcResult("Roční amortizace", fmtCzk(r.annualAmortization)),
            CalcResult("Roční palivo/energie", fmtCzk(r.annualFuelCost)),
            CalcResult("Celkové roční náklady (TCO)", fmtCzk(r.annualTotalCost)),
            CalcResult("Měsíční náklady na auto", fmtCzk(r.monthlyTotalCost)),
            CalcResult("Cena za 1 km", String.format("%.2f Kč/km", r.costPerKm)),
        )
    },
)
