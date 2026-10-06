package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.loans.CarTcoCalculator
import com.mirda.financnikalkulacky.calculators.loans.ConsolidationCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoansCalculatorTest {
    @Test fun `konsolidace snizi mesicni splatku`() {
        // Dve pujcky: 100 tis (splátka 3500) + 60 tis (2200) = 5700/mes
        val debts = listOf(100_000.0 to 3_500.0, 60_000.0 to 2_200.0)
        val r = ConsolidationCalculator.calculate(debts, newRatePct = 7.0, years = 5)
        assertEquals(160_000.0, r.totalDebt)
        assertEquals(5_700.0, r.oldTotalPayment)
        // Nova splátka cca 3168 < stare 5700 -> odlehčení kladne
        assertTrue(r.newMonthlyPayment < r.oldTotalPayment)
        assertTrue(r.monthlyRelief > 2_000.0)
    }

    @Test fun `TCO auta cena za km`() {
        // Auto 400 tis, prodej 150 tis po 8 letech; 15 tis km/rok, 7 l/100km, 60 Kc/l
        val r = CarTcoCalculator.calculate(
            price = 400_000.0, ownershipYears = 8, resaleValue = 150_000.0,
            annualKm = 15_000.0, consumptionPer100 = 7.0, fuelPrice = 60.0, fixedCostsAnnual = 24_000.0,
        )
        // Amortizace (400-150)/8 = 31250; palivo 150*7*60=63000; fixni 24000 -> celkem 118250/rok
        assertEquals(31_250.0, r.annualAmortization)
        assertEquals(63_000.0, r.annualFuelCost)
        assertEquals(118_250.0, r.annualTotalCost)
        // Cena za 1 km = 118250/15000 = 7.88 Kc/km
        assertTrue(r.costPerKm > 7.8 && r.costPerKm < 7.9)
    }
}
