package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.YearParams
import com.mirda.financnikalkulacky.calculators.work.DppDpcCalculator
import com.mirda.financnikalkulacky.calculators.work.OsvcCalculator
import com.mirda.financnikalkulacky.calculators.work.SickLeaveCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Testy ročních parametrů (Tip 2) — hodnoty 2025/2026 a jejich dopad na výpočty. */
class YearParamsTest {

    @Test
    fun `rok 2025 ma spravne redukci hranice nemocenske`() {
        val p = YearParams.YEAR_2025
        assertEquals(1_552.0, p.sickLimit1)
        assertEquals(2_328.0, p.sickLimit2)
        assertEquals(4_656.0, p.sickLimit3)
    }

    @Test
    fun `rok 2026 ma spravne redukci hranice nemocenske`() {
        val p = YearParams.YEAR_2026
        assertEquals(1_633.0, p.sickLimit1)
        assertEquals(2_449.0, p.sickLimit2)
        assertEquals(4_897.0, p.sickLimit3)
    }

    @Test
    fun `paušal 1 pásmo se mezi roky liší`() {
        // MF ČR: 2025 = 8 716 Kč/měs., 2026 = 9 162 Kč/měs.
        assertEquals(8_716.0, YearParams.YEAR_2025.flatTaxBand1)
        assertEquals(9_162.0, YearParams.YEAR_2026.flatTaxBand1)
    }

    @Test
    fun `limit DPČ DPP se mezi roky liší`() {
        // 10 % průměrné mzdy: 2025 = 3 900 Kč, 2026 = 4 100 Kč
        assertEquals(3_900.0, YearParams.YEAR_2025.dpcContributionLimit)
        assertEquals(4_100.0, YearParams.YEAR_2026.dpcContributionLimit)
    }

    @Test
    fun `byYear vrati spravny rok a fallback na aktualni`() {
        assertEquals(YearParams.YEAR_2025, YearParams.byYear(2025))
        assertEquals(YearParams.YEAR_2026, YearParams.byYear(2026))
        // neznámý rok → aktuální (2026)
        assertEquals(YearParams.YEAR_2026, YearParams.byYear(1999))
    }

    @Test
    fun `nemocenska se pocita s hranicemi daneho roku`() {
        // Denní DVZ 3 000 Kč — mezi 1. a 2. hranicí; 2025 i 2026 dají jiný výsledek.
        val daily = 3_000.0
        val r2025 = SickLeaveCalculator.reduceDailyBase(daily, YearParams.YEAR_2025)
        val r2026 = SickLeaveCalculator.reduceDailyBase(daily, YearParams.YEAR_2026)
        // 2025: 1552*0.9 + (2328-1552)*0.6 + (3000-2328)*0.3 = 1396.8+465.6+201.6
        assertEquals(2_064.0, r2025, 0.01)
        // 2026: 1633*0.9 + (2449-1633)*0.6 + (3000-2449)*0.3 = 1469.7+489.6+165.3
        assertEquals(2_124.6, r2026, 0.01)
        assertTrue(r2026 > r2025, "vyšší hranice 2026 musí dát vyšší redukovaný základ")
    }

    @Test
    fun `DPP pod limitem daneho roku nema odvody`() {
        // 4 000 Kč: v roce 2025 (limit 3 900) už jsou plné odvody, v 2026 (limit 4 100) ne.
        val r2025 = DppDpcCalculator.calculate(4_000.0, hasTaxDeclaration = true, p = YearParams.YEAR_2025)
        val r2026 = DppDpcCalculator.calculate(4_000.0, hasTaxDeclaration = true, p = YearParams.YEAR_2026)
        assertTrue(r2025.fullContributions)
        assertFalse(r2026.fullContributions)
    }

    @Test
    fun `OSVČ paušal se liší podle roku`() {
        val r2025 = OsvcCalculator.calculate(1_000_000.0, 80.0, band = 1, p = YearParams.YEAR_2025)
        val r2026 = OsvcCalculator.calculate(1_000_000.0, 80.0, band = 1, p = YearParams.YEAR_2026)
        assertEquals(8_716.0 * 12.0, r2025.flatTaxAnnual, 0.01)
        assertEquals(9_162.0 * 12.0, r2026.flatTaxAnnual, 0.01)
    }

    @Test
    fun `ALL obsahuje oba roky v poradí`() {
        assertEquals(listOf(2025, 2026), YearParams.ALL.map { it.year })
    }
}
