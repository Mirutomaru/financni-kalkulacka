package com.mirda.financnikalkulacky.calculators.investments

import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

/** Výsledek složeného úročení / pravidelného spoření. */
data class InvestmentResult(
    val finalValue: Double,     // FV — finální částka
    val totalContributions: Double,  // P + PMT·n
    val profit: Double,          // zisk z úroků
)

object InvestmentCalculator {
    /** Budoucí hodnota: počáteční vklad P + měsíční úložka PMT při ročním zhodnocení r_a. */
    fun futureValue(principal: Double, monthlyPmt: Double, annualRatePct: Double, years: Int): InvestmentResult {
        val n = (years * 12).coerceAtLeast(0)
        val r = annualRatePct / 100.0 / 12.0
        val fvPrincipal = if (r == 0.0) principal else principal * (1.0 + r).pow(n.toDouble())
        val fvAnnuity = if (n == 0 || monthlyPmt == 0.0) 0.0
            else if (r == 0.0) monthlyPmt * n
            else monthlyPmt * ((1.0 + r).pow(n.toDouble()) - 1.0) / r
        val finalValue = fvPrincipal + fvAnnuity
        val contributions = principal + monthlyPmt * n
        return InvestmentResult(finalValue, contributions, finalValue - contributions)
    }

    /** Počet měsíců do dosažení cílové hodnoty (FIRE). Vrátí −1, pokud nedosažitelné. */
    fun monthsToTarget(principal: Double, monthlyPmt: Double, annualRatePct: Double, target: Double): Int {
        if (target <= principal) return 0
        val r = annualRatePct / 100.0 / 12.0
        if (r == 0.0) {
            if (monthlyPmt <= 0.0) return -1
            return ceil((target - principal) / monthlyPmt).toInt()
        }
        val numerator = target * r + monthlyPmt
        val denominator = principal * r + monthlyPmt
        if (denominator <= 0.0) return -1
        val x = numerator / denominator
        if (x <= 1.0) return 0
        return ceil(ln(x) / ln(1.0 + r)).toInt()
    }

    /** FIRE číslo: cílová hodnota portfolia pro požadovanou rentu při SWR. */
    fun fireNumber(monthlyRent: Double, swrPct: Double): Double = monthlyRent * 12.0 / (swrPct / 100.0)

    /** Čistý roční dividendový tok po srážkové dani a měsíční pasivní příjem. */
    fun dividends(portfolio: Double, yieldPct: Double, withholdingTaxPct: Double): DividendResult {
        val grossAnnual = portfolio * yieldPct / 100.0
        val netAnnual = grossAnnual * (1.0 - withholdingTaxPct / 100.0)
        return DividendResult(grossAnnual, netAnnual, netAnnual / 12.0)
    }

    /** Roční daňová úspora z odpočtu DIP/penzijního spoření (max. 48 000 Kč/rok). */
    fun dipTaxSaving(userMonthly: Double, employerMonthly: Double, taxRatePct: Double): DipResult {
        val annualContribution = (userMonthly + employerMonthly) * 12.0
        val deductible = minOf(annualContribution, com.mirda.financnikalkulacky.calculators.Legislation.DIP_MAX_DEDUCTION_ANNUAL)
        val saving = deductible * taxRatePct / 100.0
        return DipResult(deductible, saving, annualContribution)
    }

    /** Reálná kupní síla úspor po t letech při inflaci i (%). */
    fun inflationRealValue(amount: Double, inflationPct: Double, years: Int): InflationResult {
        val real = amount * (1.0 + inflationPct / 100.0).pow(-years.toDouble())
        return InflationResult(real, amount - real)
    }
}

/** Výsledek dividendového portfolia. */
data class DividendResult(
    val grossAnnual: Double,
    val netAnnual: Double,
    val monthlyNet: Double,
)

/** Výsledek daňové úspory DIP/penzijního spoření. */
data class DipResult(
    val deductible: Double,   // odpočet ze základu daně (max 48 000 Kč/rok)
    val annualTaxSaving: Double,
    val totalAnnualCapital: Double,
)

/** Výsledek vlivu inflace. */
data class InflationResult(
    val realValue: Double,   // reálná kupní síla po t letech
    val lossOfPurchasingPower: Double,
)
