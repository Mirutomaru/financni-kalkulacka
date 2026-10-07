package com.mirda.financnikalkulacky.calculators.work

import com.mirda.financnikalkulacky.calculators.Legislation

/** Výsledek ročního daňového zúčtování. */
data class AnnualTaxSettlementResult(
    val taxableIncome: Double,
    val taxBeforeDeductions: Double,
    val totalDeductions: Double,
    val totalSlevy: Double,
    val finalTaxAnnual: Double,
    val withheldTax: Double,
    val refundOrOwed: Double, // > 0 = přeplatek (vráceno), < 0 = nedoplatek
)

/** Výsledek daňového bonusu na děti. */
data class ChildBonusResult(
    val child1Annual: Double,
    val child2Annual: Double,
    val child3PlusAnnual: Double,
    val totalAnnual: Double,
    val monthlyEquivalent: Double,
)

object AnnualTaxSettlementCalculator {
    /**
     * Roční zúčtování daně z příjmů fyzických osob.
     * @param annualIncome roční hrubý příjem (mzda/odměna)
     * @param withheldTax daň stržená za rok (zálohy)
     * @param mortgageInterest úroky z hypotéky na bydlení (max. 300 tis./rok)
     * @param pensionSavings penzijní spoření / DIP (max. 48 tis.)
     * @param lifeInsurance životní pojištění s investiční složkou (max. 10 tis.)
     * @param gifts dary na charitu (max. 15 % základu)
     * @param spouseDeduction sleva na manžela/manželku (30 840, pokud nemá příjem)
     * @param studentDeduction sleva na studenta do 25 let (30 840)
     * @param disabilityDeduction sleva na invaliditu (18 000 / 36 000 dle stupně)
     */
    fun calculate(
        annualIncome: Double,
        withheldTax: Double = 0.0,
        mortgageInterest: Double = 0.0,
        pensionSavings: Double = 0.0,
        lifeInsurance: Double = 0.0,
        gifts: Double = 0.0,
        spouseDeduction: Boolean = false,
        studentDeduction: Boolean = false,
        disabilityDeduction: Int = 0, // 0=ne, 1=II. stupeň, 2=III. stupeň
    ): AnnualTaxSettlementResult {
        val basicDeduction = Legislation.BASIC_DEDUCTION_ANNUAL

        // Odpočty ze základu daně (s limity)
        val mortgageDed = minOf(mortgageInterest, 300_000.0)
        val pensionDed = minOf(pensionSavings, Legislation.DIP_MAX_DEDUCTION_ANNUAL)
        val insuranceDed = minOf(lifeInsurance, 10_000.0)

        // Základ daně po odpočtech (minimálně základní sleva)
        val baseAfterDeductions = maxOf(annualIncome - mortgageDed - pensionDed - insuranceDed, basicDeduction)

        // Daň 15 % z vyššího ze základu a základní slevy
        var tax = maxOf(baseAfterDeductions, basicDeduction) * Legislation.TAX_RATE_1

        // Slevy na dani (počítají se i když daň je nižší → přeplatek)
        var slevy = 0.0
        if (spouseDeduction) slevy += 30_840.0
        if (studentDeduction) slevy += 30_840.0
        when (disabilityDeduction) {
            1 -> slevy += 18_000.0
            2 -> slevy += 36_000.0
        }

        // Dary: odpočet ze základu do 15 % základu (zjednodušeně)
        val giftDed = minOf(gifts, baseAfterDeductions * 0.15)
        tax -= giftDed * Legislation.TAX_RATE_1

        val finalTax = maxOf(tax - slevy, 0.0)
        return AnnualTaxSettlementResult(
            taxableIncome = annualIncome,
            taxBeforeDeductions = annualIncome * Legislation.TAX_RATE_1,
            totalDeductions = mortgageDed + pensionDed + insuranceDed + giftDed,
            totalSlevy = slevy,
            finalTaxAnnual = finalTax,
            withheldTax = withheldTax,
            refundOrOwed = withheldTax - finalTax,
        )
    }
}

object ChildBonusCalculator {
    /**
     * Daňový bonus na děti (2026) — roční částka dle pořadí dítěte.
     * 1. dítě: 3 400 Kč/měs., 2. dítě: 4 700 Kč/měs., 3. a další: 5 800 Kč/měs.
     */
    fun calculate(children: Int): ChildBonusResult {
        val c1 = if (children >= 1) 3_400.0 * 12 else 0.0
        val c2 = if (children >= 2) 4_700.0 * 12 else 0.0
        val extra = maxOf(children - 2, 0) * 5_800.0 * 12
        val total = c1 + c2 + extra
        return ChildBonusResult(c1, c2, extra, total, total / 12.0)
    }
}
