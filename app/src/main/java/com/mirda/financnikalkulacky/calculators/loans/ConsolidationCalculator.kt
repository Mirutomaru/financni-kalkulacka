package com.mirda.financnikalkulacky.calculators.loans

import kotlin.math.pow

/** Výsledek konsolidace půjček. */
data class ConsolidationResult(
    val totalDebt: Double,          // součet stávajících jistin
    val oldTotalPayment: Double,    // součet stávajících splátek
    val newMonthlyPayment: Double,  // nová sjednocená splátka
    val monthlyRelief: Double,      // odlehčení rozpočtu (ΔA)
    val oldTotalCost: Double,       // celkové přeplatky před konsolidací (odhad)
    val newTotalCost: Double,       // celkem zaplaceno po konsolidaci
)

object ConsolidationCalculator {
    /**
     * @param debts seznam stávajících závazků (jistina, měsíční splátka)
     * @param newRatePct úroková sazba nové sjednocené půjčky (% p.a.)
     * @param years doba splácení nové půjčky (roky)
     */
    fun calculate(debts: List<Pair<Double, Double>>, newRatePct: Double, years: Int): ConsolidationResult {
        val totalDebt = debts.sumOf { it.first }
        val oldTotalPayment = debts.sumOf { it.second }

        // Nová splátka pro sjednocenou jistinu s novým úrokem
        val m = (years * 12).coerceAtLeast(1)
        val r = newRatePct / 100.0 / 12.0
        val factor = if (r > 0) (1.0 + r).pow(m.toDouble()) else 1.0
        val newPayment = if (totalDebt <= 0.0) 0.0
            else if (r == 0.0) totalDebt / m
            else totalDebt * r * factor / (factor - 1.0)

        val monthlyRelief = oldTotalPayment - newPayment
        // Celkem zaplaceno po konsolidaci: nová splátka × počet měsíců
        val newTotalCost = newPayment * m
        // Odhad celkových nákladů před konsolidací: stávající splátky na stejném horizontu (m měsíců)
        val oldTotalCost = oldTotalPayment * m

        return ConsolidationResult(
            totalDebt = totalDebt,
            oldTotalPayment = oldTotalPayment,
            newMonthlyPayment = newPayment,
            monthlyRelief = monthlyRelief,
            oldTotalCost = oldTotalCost,
            newTotalCost = newTotalCost,
        )
    }
}
