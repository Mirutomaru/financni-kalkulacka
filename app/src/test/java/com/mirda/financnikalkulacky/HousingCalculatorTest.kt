package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.housing.BuyVsRentCalculator
import com.mirda.financnikalkulacky.calculators.housing.MortgageCalculator
import com.mirda.financnikalkulacky.calculators.housing.RentalYieldCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HousingCalculatorTest {
    @Test fun `anuita 2 mil za 30 let pri 5 procentech`() {
        val r = MortgageCalculator.calculate(2_000_000.0, 5.0, 30)
        // 2 mil, 5 %, 30 let -> cca 10 736 Kc/mes.
        assertTrue(r.monthlyPayment > 10_700.0 && r.monthlyPayment < 10_800.0)
        // Celkem zaplaceno = splatka * pocet mesicu; uroky = celkem - jistina
        assertEquals(r.monthlyPayment * 360.0, r.totalPaid)
        assertTrue(r.totalInterest > 1_800_000.0) // cca 1,86 mil uroky
    }

    @Test fun `mimoradna splatka zkrati dobu`() {
        val r = MortgageCalculator.extraPayment(1_500_000.0, 4.5, 240, 300_000.0, goalB = false)
        // 300 tis mimořádné splátky musí dobu zkrátit o > 24 měsíců
        assertTrue(r.monthsShortened > 24)
        assertTrue(r.interestSaved > 100_000.0)
    }

    @Test fun `refinancovani snizi splatku a ma bod zvratu`() {
        val r = MortgageCalculator.refinance(1_000_000.0, 240, 6.5, 3.8, fees = 25_000.0)
        assertTrue(r.newPayment < r.oldPayment)
        assertTrue(r.grossSavings > 0.0)
        // Bod zvratu: poplatky / rozdil splatek — musi byt kladny a mensi nez zbyvaja mesice
        assertTrue(r.breakEvenMonths > 0)
        assertTrue(r.breakEvenMonths < 240)
    }

    @Test fun `investicni byt cap rate a cashflow`() {
        val r = RentalYieldCalculator.calculate(6_000_000.0, 18_000.0, vacancyPct = 8.33, opexMonthly = 2_500.0, taxOnRentPct = 10.5)
        // Gross yield = 18000*12/6M = 3,6 %
        assertTrue(r.grossYieldPct > 3.5 && r.grossYieldPct < 3.7)
        // Cap rate po nákladech a dani musí být menší než gross yield
        assertTrue(r.capRatePct < r.grossYieldPct)
        assertTrue(r.monthlyCashflow > 0.0)
    }

    @Test fun `koup vs najem simulace ma kladne jmeni`() {
        val r = BuyVsRentCalculator.calculate(
            price = 5_000_000.0, ownFunds = 1_000_000.0, mortgageRatePct = 4.8, mortgageYears = 25,
            buyOpexMonthly = 3_000.0, propertyGrowthPct = 3.0,
            rentMonthly = 16_000.0, rentGrowthPct = 2.0, investmentReturnPct = 4.0, horizonYears = 25,
        )
        // Po 25 letech musí mít oba scénáře kladné čisté jmění
        assertTrue(r.buyNetWorth > 0.0)
        assertTrue(r.rentNetWorth > 0.0)
    }

    @Test fun `amortizacni plan — zbytek klesa a konci na nule`() {
        val plan = MortgageCalculator.yearlyAmortization(2_000_000.0, 5.0, 30)
        assertEquals(30, plan.size)
        // Po prvním roce zůstatek klesne pod původní jistinu (cca 1,97 mil)
        assertTrue(plan[0].remainingPrincipal < 2_000_000.0)
        assertTrue(plan[0].remainingPrincipal > 1_900_000.0)
        // Zůstatek klesá monotónně a na konci je ~0 (zaokrouhlení na centy)
        for (i in 1 until plan.size) {
            assertTrue(plan[i].remainingPrincipal <= plan[i - 1].remainingPrincipal + 1e-6)
        }
        assertTrue(plan.last().remainingPrincipal < 1.0)
        // Kumulované úroky rostou a končí celkovými úroky (cca 1,865 mil)
        for (i in 1 until plan.size) {
            assertTrue(plan[i].cumulativeInterest > plan[i - 1].cumulativeInterest)
        }
        val r = MortgageCalculator.calculate(2_000_000.0, 5.0, 30)
        assertEquals(r.totalInterest, plan.last().cumulativeInterest, 1.0)
    }

    @Test fun `amortizacni plan — kratsi hypoteka`() {
        val plan = MortgageCalculator.yearlyAmortization(1_000_000.0, 4.0, 5)
        assertEquals(5, plan.size)
        assertTrue(plan.last().remainingPrincipal < 1.0)
    }
}
