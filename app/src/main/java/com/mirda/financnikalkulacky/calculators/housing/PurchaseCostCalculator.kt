package com.mirda.financnikalkulacky.calculators.housing

/** Výsledek vedlejších nákladů při koupi nemovitosti. */
data class PurchaseCostResult(
    val notaryEscrow: Double,      // advokátní úschova / notář (cena × sazba)
    val landRegistry: Double,       // katastrální poplatek (0,5 % ceny)
    val realEstateFee: Double,      // provize realitky (včetně DPH)
    val loanProcessing: Double,     // poplatky za zpracování úvěru
    val totalCosts: Double,         // součet všech nákladů
    val totalPurchasePrice: Double, // cena + vedlejší náklady
    val costsPctOfPrice: Double,    // náklady v % ceny (orientační 5–10 %)
)

object PurchaseCostCalculator {
    const val LAND_REGISTRY_PCT = 0.005   // katastrální poplatek 0,5 %
    const val NOTARY_ESCROW_PCT = 0.0025  // advokátní úschova ~0,25 % (min. cca 1 500 Kč)

    /**
     * Vedlejší náklady koupě: notář/úschova, katastr, provize realitky, poplatky úvěru.
     * price — cena nemovitosti, escrowPct — sazba úschovy (v %), brokerFeePct — provize (v %, s DPH),
     * loanProcessing — pevné poplatky za zpracování úvěru (Kč).
     */
    fun calculate(price: Double, escrowPct: Double, brokerFeePct: Double, loanProcessing: Double): PurchaseCostResult {
        val notary = maxOf(price * escrowPct / 100.0, 1_500.0)
        val registry = price * LAND_REGISTRY_PCT
        val broker = price * brokerFeePct / 100.0
        val total = notary + registry + broker + loanProcessing
        return PurchaseCostResult(
            notaryEscrow = notary,
            landRegistry = registry,
            realEstateFee = broker,
            loanProcessing = loanProcessing,
            totalCosts = total,
            totalPurchasePrice = price + total,
            costsPctOfPrice = if (price > 0.0) total / price * 100.0 else 0.0,
        )
    }
}
