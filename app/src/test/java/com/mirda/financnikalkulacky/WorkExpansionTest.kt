package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.work.AnnualTaxSettlementCalculator
import com.mirda.financnikalkulacky.calculators.work.CareAllowanceCalculator
import com.mirda.financnikalkulacky.calculators.work.ChildBenefitsCalculator
import com.mirda.financnikalkulacky.calculators.work.ChildBonusCalculator
import com.mirda.financnikalkulacky.calculators.work.DepreciationCalculator
import com.mirda.financnikalkulacky.calculators.work.DisabilityPensionCalculator
import com.mirda.financnikalkulacky.calculators.work.EmploymentCompareCalculator
import com.mirda.financnikalkulacky.calculators.work.HourlyRateCalculator
import com.mirda.financnikalkulacky.calculators.work.OvertimeCalculator
import com.mirda.financnikalkulacky.calculators.work.OvcAdvanceCalculator
import com.mirda.financnikalkulacky.calculators.work.PaternityCalculator
import com.mirda.financnikalkulacky.calculators.work.ReverseSalaryCalculator
import com.mirda.financnikalkulacky.calculators.work.SurvivorPensionCalculator
import com.mirda.financnikalkulacky.calculators.work.TravelExpenseCalculator
import com.mirda.financnikalkulacky.calculators.work.VacationCalculator
import com.mirda.financnikalkulacky.calculators.work.VatCalculator
import com.mirda.financnikalkulacky.calculators.work.WageValorizationCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Testy rozšíření Práce a daně (17 nových kalkulaček). */
class WorkExpansionTest {

    // ── MZDA A ZAMĚSTNÁNÍ ────────────────────────────────────────────────

    @Test
    fun `obracena mzda — cista 30 tis vyjde na hrubou okolo 37 400`() {
        val r = ReverseSalaryCalculator.calculate(30_000.0)
        // simulace: gross ≈ 37 375 Kč pro čistou 30 000 (zaokrouhlení základu na stovky)
        assertTrue(r.grossMonthly > 37_300.0 && r.grossMonthly < 37_500.0, "gross=${r.grossMonthly}")
        // kontrola zpět: čistá se musí rovnat cíli (tolerance bisection)
        assertTrue(Math.abs(r.netMonthly - 30_000.0) < 5.0, "net=${r.netMonthly}")
    }

    @Test
    fun `obracena mzda — sleva na dite snizi potrebnou hrubou`() {
        val noChild = ReverseSalaryCalculator.calculate(30_000.0)
        val withChild = ReverseSalaryCalculator.calculate(30_000.0, children = 1)
        // sleva na dítě → menší daň → stačí nižší hrubá mzda
        assertTrue(withChild.grossMonthly < noChild.grossMonthly, "s=${withChild.grossMonthly} bez=${noChild.grossMonthly}")
    }

    @Test
    fun `prescasy — vikend 100 % noc 35 % svatek 100 %`() {
        val r = OvertimeCalculator.calculate(150.0, weekendHours = 4, nightHours = 8, holidayHours = 2)
        assertEquals(600.0, r.overtimeWeekendGross, 1.0)   // 150 * 1.0 * 4
        assertEquals(420.0, r.overtimeNightGross, 1.0)     // 150 * 0.35 * 8
        assertEquals(300.0, r.overtimeHolidayGross, 1.0)   // 150 * 1.0 * 2
        assertEquals(1_320.0, r.totalExtraGross, 1.0)
        // čistka po odvedení pojistného a daně ~15 %
        assertTrue(r.netExtraMonthly > 950.0 && r.netExtraMonthly < 1_050.0, "net=${r.netExtraMonthly}")
    }

    @Test
    fun `dovolena — zakladni 20 dni a navyseni podle stazu`() {
        val r = VacationCalculator.calculate(30_000.0, yearsEmployed = 3)
        assertEquals(20, r.daysEntitled)
        // denní sazda = hrubá / 21,75
        assertEquals(1_379.31, r.dailyRate, 1.0)
        assertEquals(27_586.21, r.vacationPayTotal, 1.0)

        val r10 = VacationCalculator.calculate(30_000.0, yearsEmployed = 10)
        assertEquals(22, r10.daysEntitled) // +2 dny po 10 letech
    }

    @Test
    fun `cestovne — stravenka a jizdne za rok`() {
        val r = TravelExpenseCalculator.calculate(workDaysPerMonth = 21, kmPerMonth = 300.0)
        assertEquals(2_940.0, r.mealAllowanceMonthly, 1.0)      // 140 * 21
        assertEquals(28_440.0, r.travelPerKmAnnual, 1.0)        // 7,90 * 300 * 12
        assertEquals(63_720.0, r.totalAnnualGross, 1.0)         // stravenky + jízdné
    }

    @Test
    fun `valorizace — realny rust pod inflaci je ztrata kupniho sily`() {
        val r = WageValorizationCalculator.calculate(30_000.0, nominalRaisePct = 5.0, inflationPct = 3.0)
        // reálný růst = (5-3)/(100+3) ≈ 1,94 %
        assertTrue(r.realGrowthPct > 1.9 && r.realGrowthPct < 2.0, "real=${r.realGrowthPct}")
        assertEquals(0.0, r.purchasingPowerLossPct, 0.01) // růst nad inflací → žádná ztráta

        val below = WageValorizationCalculator.calculate(30_000.0, nominalRaisePct = 2.0, inflationPct = 4.0)
        assertTrue(below.realGrowthPct < 0.0, "real=${below.realGrowthPct}")
        assertTrue(below.purchasingPowerLossPct > 1.5 && below.purchasingPowerLossPct < 2.5, "loss=${below.purchasingPowerLossPct}")
    }

    // ── OSVČ A PODNIKÁNÍ ─────────────────────────────────────────────────

    @Test
    fun `ovc zalohy — prijmem 50 tis vychazi socialka a zdravotni`() {
        val r = OvcAdvanceCalculator.calculate(50_000.0)
        // základ = 80 % příjmu = 40 000 (nad minimem)
        assertEquals(11_680.0, r.socialMonthly, 1.0)   // 40 000 * 29,2 %
        assertEquals(5_400.0, r.healthMonthly, 1.0)    // 40 000 * 13,5 %
        assertEquals(204_960.0, r.totalAnnual, 1.0)
    }

    @Test
    fun `ovc zalohy — pri nizkem prijmu plati minimalni zaklad`() {
        val r = OvcAdvanceCalculator.calculate(1_000.0)
        // min. sociální základ → záloha 5 756 Kč/měs., zdravotní 3 306 Kč/měs.
        assertEquals(5_756.0, r.socialMonthly, 2.0)
        assertEquals(3_306.0, r.healthMonthly, 2.0)
    }

    @Test
    fun `srovnani zam ovc sro — pri 60 tis a vysokych nakladech vyhraje zamestnanec`() {
        val r = EmploymentCompareCalculator.calculate(60_000.0, expensePct = 60.0)
        // zaměstnanec: čistka ~46 610 Kč/měs. (zaokrouhlení základu daně na stovky)
        assertTrue(r.employeeNet > 46_500.0 && r.employeeNet < 46_700.0, "emp=${r.employeeNet}")
        // OSVČ s 60 % náklady a plnými zálohami má minimální čistý příjem
        assertTrue(r.ovcNet < 1_000.0, "ovc=${r.ovcNet}")
        // s.r.o.: zisk 288 tis − daň PO 21 % − daň z dividend 15 % → ~16 116 Kč/měs.
        assertTrue(r.sroNet > 16_000.0 && r.sroNet < 16_300.0, "sro=${r.sroNet}")
        assertEquals("zaměstnanec", r.bestMode)
    }

    @Test
    fun `dph — cena s dph a bez ni`() {
        val fromBase = VatCalculator.calculate(priceWithoutVat = 10_000.0, ratePct = 21.0)
        assertEquals(10_000.0, fromBase.priceWithoutVat, 1.0)
        assertEquals(2_100.0, fromBase.vatAmount, 1.0)
        assertEquals(12_100.0, fromBase.priceWithVat, 1.0)
        assertTrue(!fromBase.registrationThresholdReached) // 500 tis < 1 mil

        val fromGross = VatCalculator.calculate(priceWithVat = 12_100.0, ratePct = 21.0)
        assertEquals(10_000.0, fromGross.priceWithoutVat, 1.0) // zpětný výpočet

        val big = VatCalculator.calculate(priceWithVat = 12_100.0, annualTurnover = 1_500_000.0)
        assertTrue(big.registrationThresholdReached)
    }

    @Test
    fun `hodinova sazba — pokryti zateze ovc`() {
        val r = HourlyRateCalculator.calculate(40_000.0, hoursPerMonth = 120)
        // potřebný obrat = 40 000 / 0,55 ≈ 72 727 Kč/měs. → sazba ~606 Kč/hod
        assertTrue(r.hourlyRateGross > 600.0 && r.hourlyRateGross < 615.0, "rate=${r.hourlyRateGross}")
        assertEquals(40_000.0, r.equivalentSalaryNet, 1.0)
    }

    @Test
    fun `odpisy — linearni odpis a dane uspora`() {
        val r = DepreciationCalculator.calculate(100_000.0, usefulLifeYears = 5)
        assertEquals(20_000.0, r.annualDepreciation, 1.0)
        assertEquals(3_000.0, r.taxSavingAnnual, 1.0)    // 15 % z odpisu
        assertEquals(15_000.0, r.totalTaxSavingOverLife, 1.0)
        assertEquals(0.0, r.bookValueAfterYears, 1.0)
    }

    // ── DANĚ ──────────────────────────────────────────────────────────────

    @Test
    fun `rocni zucotovani — odpocty hypoteka a penzini snizi dan`() {
        val r = AnnualTaxSettlementCalculator.calculate(
            annualIncome = 600_000.0,
            withheldTax = 60_000.0,
            mortgageInterest = 200_000.0,
            pensionSavings = 48_000.0,
        )
        // základ po odpočtech: 600 tis − 200 tis − 48 tis = 352 tis → daň 52 800
        assertEquals(52_800.0, r.finalTaxAnnual, 1.0)
        // přeplatek: strženo 60 tis, daň 52,8 tis → vráceno 7 200
        assertEquals(7_200.0, r.refundOrOwed, 1.0)
    }

    @Test
    fun `rocni zucotovani — sleva na manzela a invaliditu`() {
        val spouse = AnnualTaxSettlementCalculator.calculate(
            annualIncome = 300_000.0,
            withheldTax = 45_000.0,
            spouseDeduction = true,
        )
        // daň 45 000 − sleva 30 840 = 14 160 → přeplatek 30 840
        assertEquals(14_160.0, spouse.finalTaxAnnual, 1.0)
        assertEquals(30_840.0, spouse.refundOrOwed, 1.0)

        val disabled = AnnualTaxSettlementCalculator.calculate(
            annualIncome = 200_000.0,
            disabilityDeduction = 2, // III. stupeň → sleva 36 tis
        )
        // daň 30 000 − sleva 36 000 → 0 (slevy převyšují daň)
        assertEquals(0.0, disabled.finalTaxAnnual, 1.0)
    }

    @Test
    fun `dany bonus na deti — castky dle poradi`() {
        val two = ChildBonusCalculator.calculate(2)
        assertEquals(40_800.0, two.child1Annual, 1.0)   // 3 400 * 12
        assertEquals(56_400.0, two.child2Annual, 1.0)   // 4 700 * 12
        assertEquals(97_200.0, two.totalAnnual, 1.0)

        val three = ChildBonusCalculator.calculate(3)
        assertEquals(69_600.0, three.child3PlusAnnual, 1.0) // 5 800 * 12
        assertEquals(166_800.0, three.totalAnnual, 1.0)
    }

    // ── SOCIÁLNÍ DÁVKY ────────────────────────────────────────────────────

    @Test
    fun `otcovska — 70 % redukovaneho dvz na dva tydny`() {
        val r = PaternityCalculator.calculate(30_000.0)
        // DVZ = 1 000 Kč/den (pod prahem → redukce 90 %) → 900; dávka 70 % = 630 Kč/den
        assertEquals(4_410.0, r.weeklyAmount, 1.0)     // 630 * 7
        assertEquals(8_820.0, r.totalTwoWeeks, 1.0)    // 630 * 14
    }

    @Test
    fun `oetrevne — prvni 9 dni 60 % pak 70 %`() {
        val r = CareAllowanceCalculator.calculate(30_000.0, days = 15)
        // redukovaný DVZ 900; 9 dní * 900 * 0,6 + 6 dní * 900 * 0,7 = 4 860 + 3 780
        assertEquals(8_640.0, r.totalForDays, 1.0)
    }

    @Test
    fun `invalidni duchod — minimalni vysle dle stupne`() {
        val low = DisabilityPensionCalculator.calculate(20_000.0, degree = 2)
        // 50 % z 20 tis = 10 tis < minimální 15 400 → platí minimum
        assertEquals(15_400.0, low.pensionMonthly, 1.0)

        val high = DisabilityPensionCalculator.calculate(40_000.0, degree = 3)
        // 80 % z 40 tis = 32 tis > minimum 24 600 → platí výpočet
        assertEquals(32_000.0, high.pensionMonthly, 1.0)
    }

    @Test
    fun `pozustalostni duchody — vdova a sirotek s minimy`() {
        val r = SurvivorPensionCalculator.calculate(20_000.0, hasWidow = true, orphans = 1)
        // vdova: max(50 % z 20 tis, 12 tis) = 12 tis; sirotek: max(40 %, 9 600) = 9 600
        assertEquals(12_000.0, r.widowWidowerMonthly, 1.0)
        assertEquals(9_600.0, r.orphanMonthly, 1.0)
        assertEquals(21_600.0, r.totalHouseholdMonthly, 1.0)
    }

    @Test
    fun `pridavky na dite — pri nizkem prijmu vyssi castka`() {
        val normal = ChildBenefitsCalculator.calculate(childAgeYears = 3, householdIncomeMonthly = 25_000.0, housingCostMonthly = 8_000.0)
        assertEquals(350.0, normal.childAllowanceMonthly, 1.0)
        assertEquals(1_400.0, normal.careContributionMonthly, 1.0) // do 7 let
        // příspěvek na bydlení: náklady 8 tis − limit 25 % příjmu (6 250) = 1 750
        assertEquals(1_750.0, normal.housingBenefitMonthly, 1.0)

        val lowIncome = ChildBenefitsCalculator.calculate(childAgeYears = 3, householdIncomeMonthly = 10_000.0, housingCostMonthly = 8_000.0)
        assertEquals(700.0, lowIncome.childAllowanceMonthly, 1.0) // nízký příjem → vyšší přídavek
    }
}
