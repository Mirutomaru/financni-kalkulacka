package com.mirda.financnikalkulacky.calculators

/**
 * Roční parametry — hodnoty, které se mění každým kalendářním rokem.
 * Stabilní sazby (daň 15/23 %, odvody 7,1/4,5 % apod.) zůstávají v [Legislation].
 */
data class YearParams(
    val year: Int,
    // Nemocenská / mateřská — redukční hranice denního vyměřovacího základu
    val sickLimit1: Double,
    val sickLimit2: Double,
    val sickLimit3: Double,
    // OSVČ — paušální daň, pásmo 1 (měsíční záloha)
    val flatTaxBand1: Double,
    // OSVČ — minimální měsíční odvody (orientačně z paušálu 1. pásmo)
    val ovcMinSocialMonthly: Double,
    val ovcMinHealthMonthly: Double,
    // DPP / DPČ — měsíční limit pro vznik odvodů
    val dpcContributionLimit: Double,
) {
    companion object {
        /** Sazby platné v roce 2025. */
        val YEAR_2025 = YearParams(
            year = 2025,
            sickLimit1 = 1_552.0,
            sickLimit2 = 2_328.0,
            sickLimit3 = 4_656.0,
            flatTaxBand1 = 8_716.0,
            ovcMinSocialMonthly = 5_473.0,
            ovcMinHealthMonthly = 3_143.0,
            dpcContributionLimit = 3_900.0,
        )

        /** Sazby platné v roce 2026 (aktuální). */
        val YEAR_2026 = YearParams(
            year = 2026,
            sickLimit1 = 1_633.0,
            sickLimit2 = 2_449.0,
            sickLimit3 = 4_897.0,
            flatTaxBand1 = 9_162.0,
            ovcMinSocialMonthly = 5_756.0,
            ovcMinHealthMonthly = 3_306.0,
            dpcContributionLimit = 4_100.0,
        )

        /** Všechny dostupné roky (pro UI přepínač). */
        val ALL: List<YearParams> = listOf(YEAR_2025, YEAR_2026)

        /** Vrátí parametry pro zadaný rok; při neznámém roce vrací aktuální. */
        fun byYear(year: Int): YearParams = ALL.firstOrNull { it.year == year } ?: YEAR_2026
    }
}
