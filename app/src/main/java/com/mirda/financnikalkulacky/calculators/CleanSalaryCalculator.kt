package com.mirda.financnikalkulacky.calculators

/**
 * Výpočet čisté mzdy ze hrubé (zjednodušený odhad, sazby 2026).
 *
 * Zjednodušení:
 * - sociální pojištění zaměstnance 6,5 % z hrubé (bez horní hranice),
 * - zdravotní pojištění 4,5 % z hrubé, minimum [MIN_HEALTH],
 * - daňový základ = hrubá − sociální; daň 15 % ročně se základní slevou na poplatníka,
 *   odpočet za děti do 15 let (roční částka na dítě),
 * - bez zvláštních slev, bonusů a superhrubé mzdy.
 */
object CleanSalaryCalculator {

    const val SOCIAL_RATE = 0.065          // sociální pojištění zaměstnance
    const val HEALTH_RATE = 0.045          // zdravotní pojištění zaměstnance
    const val MIN_HEALTH = 456.0           // minimální zdravotní pojištění (Kč/měs.)
    const val TAX_RATE_1 = 0.15            // daň z příjmů, 1. sazebník
    const val BASIC_DEDUCTION = 24_840.0   // základní sleva na poplatníka (Kč/rok)
    const val CHILD_DEDUCTION_YEARLY = 18_000.0 // odpočet za dítě do 15 let (Kč/rok)

    data class Result(
        val grossMonthly: Double,
        val socialInsurance: Double,   // Kč/měs.
        val healthInsurance: Double,   // Kč/měs.
        val taxMonthly: Double,        // Kč/měs.
        val netMonthly: Double         // Kč/měs. — čistá mzda na ruce
    )

    /** Výpočet čisté mzdy za měsíc při [grossMonthly] hrubé a [children] dětech do 15 let. */
    fun calculate(grossMonthly: Double, children: Int = 0): Result {
        if (grossMonthly <= 0.0) return Result(0.0, 0.0, 0.0, 0.0, 0.0)

        val social = grossMonthly * SOCIAL_RATE
        val health = maxOf(grossMonthly * HEALTH_RATE, MIN_HEALTH)

        // Roční daň: 15 % z ročního základu (hrubá − sociální), slevy odečtené, min. nula
        val annualBase = (grossMonthly - social) * 12.0
        val annualTax = maxOf(
            annualBase * TAX_RATE_1 - BASIC_DEDUCTION - children * CHILD_DEDUCTION_YEARLY,
            0.0
        )
        val taxMonthly = annualTax / 12.0

        return Result(
            grossMonthly = grossMonthly,
            socialInsurance = social,
            healthInsurance = health,
            taxMonthly = taxMonthly,
            netMonthly = grossMonthly - social - health - taxMonthly
        )
    }
}
