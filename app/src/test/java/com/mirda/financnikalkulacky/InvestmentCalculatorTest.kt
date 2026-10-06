package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.investments.InvestmentCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InvestmentCalculatorTest {
    @Test fun `slozene uroceni s mesicni ulozkou`() {
        // 100 tis start + 5 tis/mes, 6 % p.a., 20 let
        val r = InvestmentCalculator.futureValue(100_000.0, 5_000.0, 6.0, 20)
        // Ocekavane cca 3,64 mil (FV vkladu + FV anuity)
        assertTrue(r.finalValue > 2_600_000.0 && r.finalValue < 2_700_000.0)
        // Celkove vklady = 100 tis + 5 tis * 240 = 1,3 mil; zisk je polovina finálni castky
        assertEquals(1_300_000.0, r.totalContributions)
        assertTrue(r.profit > 1_300_000.0)
    }

    @Test fun `fire cislo pro rentu 40 tis pri SWR 4 procentech`() {
        // 40 tis/mes pri SWR 4 % = 12 mil cislo
        assertEquals(12_000_000.0, InvestmentCalculator.fireNumber(40_000.0, 4.0))
    }

    @Test fun `mesice do dosazeni cilove hodnoty`() {
        // 500 tis + 10 tis/mes pri 6 % do cilu 2 mil — cca 95 mesicu
        val n = InvestmentCalculator.monthsToTarget(500_000.0, 10_000.0, 6.0, 2_000_000.0)
        assertTrue(n in 90..100)
    }

    @Test fun `dividendy po srážkove dani`() {
        // 1 mil, DY 4 %, srážková daň 15 % -> cisty roční tok 34 tis, měsíčně ~2833
        val d = InvestmentCalculator.dividends(1_000_000.0, 4.0, 15.0)
        assertEquals(40_000.0, d.grossAnnual)
        assertEquals(34_000.0, d.netAnnual)
        assertTrue(d.monthlyNet > 2800.0 && d.monthlyNet < 2900.0)
    }

    @Test fun `inflace snizi kupni silu`() {
        // 100 tis pri inflaci 5 % po 20 letech — realna hodnota cca 37,6 tis
        val i = InvestmentCalculator.inflationRealValue(100_000.0, 5.0, 20)
        assertTrue(i.realValue > 37_000.0 && i.realValue < 38_000.0)
        // Ztrata kupni sily = pocatecni - realna
        assertEquals(100_000.0 - i.realValue, i.lossOfPurchasingPower)
    }
}
