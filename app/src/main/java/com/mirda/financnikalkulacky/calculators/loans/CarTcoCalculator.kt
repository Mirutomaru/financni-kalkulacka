package com.mirda.financnikalkulacky.calculators.loans

/** Výsledek výpočtu provozních nákladů auta (TCO). */
data class CarTcoResult(
    val annualAmortization: Double, // roční amortizace
    val annualFuelCost: Double,     // roční náklady na palivo/energi
    val annualFixedCosts: Double,   // roční fixní náklady (pojištění, servis…)
    val annualTotalCost: Double,    // celkové roční náklady TCO
    val monthlyTotalCost: Double,   // měsíční náklady na auto
    val costPerKm: Double,          // cena za 1 km jízdy
)

object CarTcoCalculator {
    /**
     * @param price pořizovací cena vozu (Kč)
     * @param ownershipYears plánovaná doba držení (roky)
     * @param resaleValue odhadovaná zůstatková cena při prodeji (Kč)
     * @param annualKm roční nájezd (km)
     * @param consumptionPer100 spotřeba na 100 km (l nebo kWh)
     * @param fuelPrice cena za litr/kWh (Kč)
     * @param annualFixedCosts roční fixní náklady (pojištění, dálniční známka, servis, pneumatiky)
     */
    fun calculate(
        price: Double, ownershipYears: Int, resaleValue: Double,
        annualKm: Double, consumptionPer100: Double, fuelPrice: Double,
        fixedCostsAnnual: Double
    ): CarTcoResult {
        val years = ownershipYears.coerceAtLeast(1)
        // Roční amortizace: (pořizovací − prodejní) / doba držení
        val amortization = (price - resaleValue) / years
        // Roční náklady na palivo: (roční km / 100) × spotřeba × cena
        val fuelCost = (annualKm / 100.0) * consumptionPer100 * fuelPrice

        // Celkové roční náklady TCO
        val totalAnnual = amortization + fuelCost + fixedCostsAnnual
        val monthlyTotal = totalAnnual / 12.0
        // Cena za 1 km jízdy (roční)
        val costPerKm = if (annualKm > 0) totalAnnual / annualKm else 0.0

        return CarTcoResult(
            annualAmortization = amortization,
            annualFuelCost = fuelCost,
            annualFixedCosts = fixedCostsAnnual,
            annualTotalCost = totalAnnual,
            monthlyTotalCost = monthlyTotal,
            costPerKm = costPerKm,
        )
    }
}
