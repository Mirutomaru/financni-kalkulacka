package com.mirda.financnikalkulacky.calculators.work

import com.mirda.financnikalkulacky.calculators.Legislation
/** Výsledek srovnání režimů OSVČ. */
data class OsvcResult(
    val flatTaxAnnual: Double,     // paušální daň — celková roční platba (záloha × 12)
    val regularTaxAnnual: Double,  // daň z přiznání po slevách
    val regularSocialAnnual: Double, // minimální sociální pojištění OSVČ
    val regularHealthAnnual: Double, // zdravotní pojištění (minimální)
    val regularTotalAnnual: Double,  // celkem běžný režim
    val betterMode: String,        // "paušal" | "priznani"
    val savings: Double,           // úspora lepšího režimu
)

object OsvcCalculator {
    /**
     * @param incomeAnnual roční příjmy (Kč)
     * @param expensePct paušální výdaje v % (80 řemeslo / 60 volná / 40 svobodná)
     * @param band pásmo paušální daně: 1, 2 nebo 3
     */
    fun calculate(incomeAnnual: Double, expensePct: Double, band: Int): OsvcResult {
        val flatMonthly = when (band.coerceIn(1, 3)) {
            1 -> Legislation.FLAT_TAX_BAND_1
            2 -> Legislation.FLAT_TAX_BAND_2
            else -> Legislation.FLAT_TAX_BAND_3
        }
        val flatTaxAnnual = flatMonthly * 12.0

        // Běžný režim: daň z přiznání + minimální odvody
        val taxableBase = (incomeAnnual - incomeAnnual * expensePct / 100.0).coerceAtLeast(0.0)
        val grossTax = taxableBase * Legislation.TAX_RATE_1
        val netTax = (grossTax - Legislation.BASIC_DEDUCTION_ANNUAL).coerceAtLeast(0.0)
        val socialAnnual = Legislation.OVC_MIN_SOCIAL_MONTHLY * 12.0
        val healthAnnual = Legislation.OVC_MIN_HEALTH_MONTHLY * 12.0
        val regularTotal = netTax + socialAnnual + healthAnnual

        val betterMode = if (flatTaxAnnual <= regularTotal) "paušal" else "priznani"
        val savings = kotlin.math.abs(flatTaxAnnual - regularTotal)

        return OsvcResult(
            flatTaxAnnual = flatTaxAnnual,
            regularTaxAnnual = netTax,
            regularSocialAnnual = socialAnnual,
            regularHealthAnnual = healthAnnual,
            regularTotalAnnual = regularTotal,
            betterMode = betterMode,
            savings = savings,
        )
    }
}
