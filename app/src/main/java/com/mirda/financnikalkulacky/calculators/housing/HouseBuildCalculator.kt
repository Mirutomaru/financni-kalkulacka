package com.mirda.financnikalkulacky.calculators.housing

/** Výsledek rozpočtu výstavby rodinného domu. */
data class HouseBuildResult(
    val buildingCost: Double,      // stavba dle m² × cena/m² (hrubá + dokončení)
    val landCost: Double,          // pozemek
    val utilitiesCost: Double,     // přípojky (elektřina, plyn, voda, kanalizace)
    val totalBudget: Double,       // celkový rozpočet
    val costPerSqm: Double,        // cena za 1 m² celkově (budova + pozemek + přípojky)
    val recommendedMortgage: Double, // doporučená půjčka při 20 % vlastních zdrojů
)

object HouseBuildCalculator {
    const val DEFAULT_COST_PER_SQM = 45_000.0   // orientační cena stavby Kč/m² (hrubá + dokončení)
    const val UTILITIES_BASE = 350_000.0        // orientační přípojky (elektro, plyn, voda, kanalizace)

    /**
     * Rozpočet výstavby: budova dle m² a ceny/m² + pozemek + přípojky.
     * sqm — podlahová plocha, costPerSqm — cena stavby za m² (Kč), landCost — cena pozemku,
     * utilitiesExtra — dodatečné náklady na přípojky nad základní částku.
     */
    fun calculate(sqm: Double, costPerSqm: Double, landCost: Double, utilitiesExtra: Double): HouseBuildResult {
        val building = sqm * costPerSqm
        val utilities = UTILITIES_BASE + maxOf(utilitiesExtra, 0.0)
        val total = building + landCost + utilities
        return HouseBuildResult(
            buildingCost = building,
            landCost = landCost,
            utilitiesCost = utilities,
            totalBudget = total,
            costPerSqm = if (sqm > 0.0) total / sqm else 0.0,
            recommendedMortgage = total * 0.80,
        )
    }
}
