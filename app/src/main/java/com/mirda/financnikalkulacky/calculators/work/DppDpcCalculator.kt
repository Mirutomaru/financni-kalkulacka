package com.mirda.financnikalkulacky.calculators.work

import com.mirda.financnikalkulacky.calculators.Legislation

/** Výsledek výpočtu DPP/DPČ. */
data class DppResult(
    val netAmount: Double,          // čistě vyplacené
    val taxWithheld: Double,        // sražená daň
    val employeeSocial: Double,     // SP zaměstnance (0 do limitu)
    val employeeHealth: Double,     // ZP zaměstnance (0 do limitu)
    val employerCost: Double,       // náklady firmy včetně odvodů nad limitem
    val fullContributions: Boolean, // zda vznikly plné odvody
)

object DppDpcCalculator {
    /**
     * @param grossMonthly hrubá měsíční odměna (Kč)
     * @param hasTaxDeclaration „růžový formulář“ — ano/ne
     */
    fun calculate(grossMonthly: Double, hasTaxDeclaration: Boolean): DppResult {
        val fullContributions = grossMonthly > Legislation.DPC_CONTRIBUTION_LIMIT

        var social = 0.0
        var health = 0.0
        if (fullContributions) {
            social = grossMonthly * Legislation.SOCIAL_INSURANCE_EMPLOYEE
            health = maxOf(grossMonthly * Legislation.HEALTH_INSURANCE_EMPLOYEE,
                Legislation.HEALTH_INSURANCE_MIN_MONTHLY)
        }

        // Daň: s prohlášením záloha se slevou na poplatníka (2 570 Kč/měs.), bez něj srážková 15 %
        val tax = if (hasTaxDeclaration) {
            maxOf(0.0, grossMonthly * Legislation.TAX_RATE_1 - 2_570.0)
        } else {
            grossMonthly * Legislation.TAX_RATE_1
        }

        val netAmount = grossMonthly - social - health - tax

        // Náklady firmy: odměna + odvody zaměstnavatele (pouze nad limitem)
        val employerCost = if (fullContributions) {
            grossMonthly * (1.0 + Legislation.SOCIAL_INSURANCE_EMPLOYER + Legislation.HEALTH_INSURANCE_EMPLOYER)
        } else {
            grossMonthly
        }

        return DppResult(
            netAmount = netAmount,
            taxWithheld = tax,
            employeeSocial = social,
            employeeHealth = health,
            employerCost = employerCost,
            fullContributions = fullContributions,
        )
    }
}
