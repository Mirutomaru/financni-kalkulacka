package com.mirda.financnikalkulacky.calculators.housing

/** Výsledek rentability investičního bytu. */
data class RentalYieldResult(
    val grossAnnualIncome: Double,   // P_roc — efektivní hrubý roční příjem
    val noi: Double,                  // čistý roční provozní zisk (NOI) po dani
    val grossYieldPct: Double,        // Gross Rental Yield %
    val capRatePct: Double,           // Net Cap Rate %
    val monthlyCashflow: Double,      // čistý měsíční cashflow
    val paybackYears: Double,         // návratnost investice v letech
)

object RentalYieldCalculator {
    /**
     * @param price pořizovací cena + počáteční investice (Kč)
     * @param rent monthly nájemné (Kč/měs.)
     * @param vacancyPct neobsazenost (%)
     * @param opexMonthly provozní náklady majitele (Kč/měs.)
     * @param taxOnRentPct efektivní daň z nájmu v % (např. 10,5 = 15 % ze zisku s 30% paušálem)
     */
    fun calculate(price: Double, rent: Double, vacancyPct: Double, opexMonthly: Double, taxOnRentPct: Double): RentalYieldResult {
        val grossAnnualIncome = rent * 12.0 * (1.0 - vacancyPct / 100.0)
        val tax = rent * 12.0 * taxOnRentPct / 100.0
        val noi = (grossAnnualIncome - opexMonthly * 12.0 - tax).coerceAtLeast(0.0)
        val grossYieldPct = if (price > 0) rent * 12.0 / price * 100.0 else 0.0
        val capRatePct = if (price > 0) noi / price * 100.0 else 0.0
        val monthlyCashflow = noi / 12.0
        val paybackYears = if (noi > 0) price / noi else Double.MAX_VALUE
        return RentalYieldResult(grossAnnualIncome, noi, grossYieldPct, capRatePct, monthlyCashflow, paybackYears)
    }
}
