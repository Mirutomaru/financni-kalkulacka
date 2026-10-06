package com.mirda.financnikalkulacky.calculators.work

import com.mirda.financnikalkulacky.calculators.Legislation
import kotlin.math.ceil
import kotlin.math.roundToLong

/** Výsledek výpočtu čisté mzdy dle specifikace. */
data class SalaryResult(
    val netMonthly: Double,        // čistá mzda na ruce
    val healthInsurance: Double,   // zdravotní pojištění (zaměstnanec)
    val socialInsurance: Double,   // sociální pojištění (zaměstnanec)
    val taxBaseAnnual: Double,     // základ daně (ročně)
    val grossTaxAnnual: Double,    // daň hrubá (15 %)
    val deductionsAnnual: Double,  // slevy na poplatníka + děti
    val netTaxAnnual: Double,      // daň po slevách (min. 0)
    val childBonusMonthly: Double, // daňový bonus na dítě (měsíčně)
    val employerCost: Double,      // superhrubá zátěž — náklady firmy
)

object SalaryCalculator {
    /** Výpočet čisté mzdy dle specifikace (zjednodušený model). */
    fun calculate(grossMonthly: Double, children: Int): SalaryResult {
        val health = grossMonthly * Legislation.HEALTH_INSURANCE_EMPLOYEE
        val social = grossMonthly * Legislation.SOCIAL_INSURANCE_EMPLOYEE

        // Základ daně: hrubá mzda zaokrouhlená nahoru na celé stovky, ročně
        val roundedGross = ceil(grossMonthly / 100.0) * 100.0
        val taxBaseAnnual = roundedGross * 12.0

        val grossTaxAnnual = taxBaseAnnual * Legislation.TAX_RATE_1
        // Slevy: na poplatníka + na děti (1., 2., 3.+ dle pořadí)
        var childDeduction = 0.0
        for (i in 0 until children.coerceAtMost(5)) {
            childDeduction += when (i) {
                0 -> Legislation.CHILD_DEDUCTION_1
                1 -> Legislation.CHILD_DEDUCTION_2
                else -> Legislation.CHILD_DEDUCTION_3_PLUS
            }
        }
        val deductionsAnnual = Legislation.BASIC_DEDUCTION_ANNUAL + childDeduction

        val netTaxAnnual = (grossTaxAnnual - deductionsAnnual).coerceAtLeast(0.0)
        // Daňový bonus na dítě: pokud slevy převyšují daň, rozdíl se vyplácí
        val childBonusAnnual = (deductionsAnnual - grossTaxAnnual).coerceAtLeast(0.0)

        val netMonthly = grossMonthly - health - social - netTaxAnnual / 12.0 + childBonusAnnual / 12.0

        // Náklady zaměstnavatele: HM + SP zaměstnavatele (24,8 %) + ZP zaměstnavatele (9 %)
        val employerCost = grossMonthly * (1.0 + Legislation.SOCIAL_INSURANCE_EMPLOYER + Legislation.HEALTH_INSURANCE_EMPLOYER)

        return SalaryResult(
            netMonthly = netMonthly.roundToLong().toDouble(),
            healthInsurance = health,
            socialInsurance = social,
            taxBaseAnnual = taxBaseAnnual,
            grossTaxAnnual = grossTaxAnnual,
            deductionsAnnual = deductionsAnnual,
            netTaxAnnual = netTaxAnnual,
            childBonusMonthly = childBonusAnnual / 12.0,
            employerCost = employerCost,
        )
    }
}
