package com.mirda.financnikalkulacky.calculators.housing

import kotlin.math.pow

/** Výsledek bonity na hypotéku podle limitů ČNB (LTV, DTI, DSTI). */
data class BonityResult(
    val maxLoanByIncome: Double,   // max. úvěr z příjmů (DSTI)
    val maxLoanByLtv: Double,      // max. úvěr z LTV limitu banky
    val maxLoan: Double,           // min(obou) — skutečná bonita
    val ltvPct: Double,            // LTV při plném využití bonity
    val dtiPct: Double,            // DTI = splátky / hrubý příjem (po získání úvěru)
    val dstiPct: Double,           // DSTI = splátky + stávající dluhy / hrubý příjem
    val recommendedDownPayment: Double, // doporučená vlastní částka (20 % ceny)
    val passesLtv: Boolean,        // LTV v limitu banky?
    val passesDsti: Boolean,       // DSTI v limitu ČNB (50 %)?
)

object BonityCalculator {
    const val CNB_DSTI_LIMIT = 0.50   // ČNB: splátky + dluhy max. 50 % hrubého příjmu
    const val BANK_LTV_LIMIT = 0.80   // typický bankovní limit LTV (80 %)

    /** Měsíční anuitní faktor na 1 Kč jistiny. */
    private fun annuityFactor(annualRatePct: Double, months: Int): Double {
        val r = annualRatePct / 100.0 / 12.0
        if (r == 0.0) return 1.0 / months.coerceAtLeast(1)
        val f = (1.0 + r).pow(months.toDouble())
        return r * f / (f - 1.0)
    }

    /**
     * Bonita: kolik si lze půjčit podle příjmů a LTV limitu.
     * grossMonthly — hrubý měsíční příjem domácnosti, existingDebt — stávající splátky/měs.,
     * price — cena nemovitosti (odhad), ownFunds — vlastní zdroje, ratePct/years — podmínky úvěru.
     */
    fun calculate(
        grossMonthly: Double, existingDebt: Double, price: Double,
        ownFunds: Double, ratePct: Double, years: Int,
    ): BonityResult {
        val m = (years * 12).coerceAtLeast(1)
        val factor = annuityFactor(ratePct, m)

        // 1) Limit z příjmů: splátka úvěru + stávající dluhy ≤ 50 % hrubého příjmu
        val availableMonthly = (CNB_DSTI_LIMIT * grossMonthly - existingDebt).coerceAtLeast(0.0)
        val maxLoanByIncome = if (factor > 0.0) availableMonthly / factor else 0.0

        // 2) Limit z LTV: úvěr ≤ 80 % ceny, vlastní zdroje kryjí zbytek
        val maxLoanByLtv = (BANK_LTV_LIMIT * price - ownFunds).coerceAtLeast(0.0)

        val maxLoan = minOf(maxLoanByIncome, maxLoanByLtv)
        val ltvPct = if (price > 0.0) maxLoan / price * 100.0 else 0.0
        val newPayment = maxLoan * factor
        val dtiPct = if (grossMonthly > 0.0) newPayment / grossMonthly * 100.0 else 0.0
        val dstiPct = if (grossMonthly > 0.0) (newPayment + existingDebt) / grossMonthly * 100.0 else 0.0

        return BonityResult(
            maxLoanByIncome, maxLoanByLtv, maxLoan, ltvPct, dtiPct, dstiPct,
            price * 0.20,
            passesLtv = ownFunds >= (1.0 - BANK_LTV_LIMIT) * price,
            passesDsti = dstiPct <= CNB_DSTI_LIMIT * 100.0 + 0.001,
        )
    }
}
