package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.family.BabyCostCalculator
import com.mirda.financnikalkulacky.calculators.family.BereavementBenefitCalculator
import com.mirda.financnikalkulacky.calculators.family.ChildSupportCalculator
import com.mirda.financnikalkulacky.calculators.family.DivorcePropertySplitCalculator
import com.mirda.financnikalkulacky.calculators.family.FuneralCostCalculator
import com.mirda.financnikalkulacky.calculators.family.GiftVsInheritanceCalculator
import com.mirda.financnikalkulacky.calculators.family.ForcedShareCalculator
import com.mirda.financnikalkulacky.calculators.family.InheritanceProceedingsCalculator
import com.mirda.financnikalkulacky.calculators.family.SeniorCareCalculator
import com.mirda.financnikalkulacky.calculators.family.StudyCostCalculator
import com.mirda.financnikalkulacky.calculators.family.VacationTripCalculator
import com.mirda.financnikalkulacky.calculators.family.WeddingCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Testy nové kategorie Rodina & životní události (12 kalkulaček). */
class FamilyExpansionTest {

    @Test
    fun `svatba — rozpočet 300 tis na 80 hostů a 12 měsíců spoření`() {
        val r = WeddingCalculator.calculate(300_000.0, 80, 12)
        assertEquals(300_000.0, r.totalCost)
        assertEquals(3_750.0, r.costPerGuest) // 300 000 / 80
        assertEquals(25_000.0, r.monthlySaving) // 300 000 / 12
    }

    @Test
    fun `studium — skolne 10 tis + zivotni 12 tis mesicne, 3 roky`() {
        val r = StudyCostCalculator.calculate(10_000.0, 12_000.0, 3, 24)
        assertEquals(120_000.0, r.annualTuition) // 10 000 × 12
        assertEquals(144_000.0, r.annualLiving) // 12 000 × 12
        assertEquals(792_000.0, r.totalStudyCost) // (120 000 + 144 000) × 3
        assertEquals(33_000.0, r.monthlySavingNeeded) // 792 000 / 24
    }

    @Test
    fun `rozvod — cisty majetek 4 mil se rozdeli po polovinach`() {
        val r = DivorcePropertySplitCalculator.calculate(5_000_000.0, 1_000_000.0, 50.0)
        assertEquals(4_000_000.0, r.netEstate) // 5 mil − 1 mil dluhů
        assertEquals(2_000_000.0, r.shareA)
        assertEquals(2_000_000.0, r.shareB)
    }

    @Test
    fun `vyzivne — nizky prijem podlozi minimum dle prumerni mzdy`() {
        // čistý 25 tis: 1/7 = 3 571 < minimum 25 % × 38 tis = 9 500 → platí minimum
        val r = ChildSupportCalculator.calculate(25_000.0, 1, 38_000.0)
        assertEquals(1.0 / 7.0, r.fraction)
        assertTrue(r.calculatedAmount < r.minimumAmount)
        assertEquals(9_500.0, r.finalAmount) // minimum platí
    }

    @Test
    fun `vyzivne — vysoky prijem plati pocetny podil`() {
        // čistý 70 tis: 1/7 = 10 000 > minimum 9 500 → platí výpočetná částka
        val r = ChildSupportCalculator.calculate(70_000.0, 1, 38_000.0)
        assertEquals(10_000.0, r.finalAmount)
    }

    @Test
    fun `miminko — prvni dite porodne 13 tis, druhe 30 tis`() {
        val first = BabyCostCalculator.calculate(40_000.0, 8_000.0, isSecondOrMoreChild = false)
        assertEquals(136_000.0, first.firstYearTotal) // 40 000 + 8 000 × 12
        assertEquals(13_000.0, first.stateBenefit)

        val second = BabyCostCalculator.calculate(40_000.0, 8_000.0, isSecondOrMoreChild = true)
        assertEquals(30_000.0, second.stateBenefit)
    }

    @Test
    fun `dovolena — cena na osobu a mesicni sporeni`() {
        val r = VacationTripCalculator.calculate(40_000.0, 4, 6)
        assertEquals(10_000.0, r.costPerPerson) // 40 000 / 4
        assertTrue(r.monthlySaving > 6_500.0 && r.monthlySaving < 6_800.0) // 40 000 / 6 ≈ 6 667
    }

    @Test
    fun `pece o seniora — draha domaci pece prohraje proti domovu`() {
        val r = SeniorCareCalculator.calculate(400.0, 8, 30_000.0)
        // domácí: 400 × 8 h × 30,42 = 97 344 Kč/měs. > domov 30 tis → domov levnější
        assertTrue(r.homeMonthly > r.facilityMonthly)
        assertEquals("pečovatelský domov", r.cheaperOption)
        assertTrue(r.annualDifference < -800_000.0) // rozdíl za rok ve prospěch domova
    }

    @Test
    fun `pece o seniora — lehkka domaci pece vyhraje`() {
        val r = SeniorCareCalculator.calculate(150.0, 4, 30_000.0)
        // domácí: 150 × 4 h × 30,42 = 18 252 Kč/měs. < domov 30 tis → domácí levnější
        assertTrue(r.homeMonthly < r.facilityMonthly)
        assertEquals("domácí péče", r.cheaperOption)
        assertTrue(r.annualDifference > 100_000.0) // úspora za rok
    }

    @Test
    fun `pohreb — naklady 25 tis, pohrebne pokryje 15 tis`() {
        val r = FuneralCostCalculator.calculate(12_000.0, 6_000.0, 3_000.0, 4_000.0)
        assertEquals(25_000.0, r.totalCost)
        assertEquals(15_000.0, r.stateBenefit)
        assertEquals(10_000.0, r.familyBurden) // doplatí rodina
    }

    @Test
    fun `pohrebne — pokryti nakladu a zbytek`() {
        val over = BereavementBenefitCalculator.calculate(25_000.0)
        assertEquals(60.0, over.coveragePct) // 15 000 / 25 000
        assertEquals(10_000.0, over.remainingCost)

        val under = BereavementBenefitCalculator.calculate(10_000.0)
        assertEquals(100.0, under.coveragePct) // strop 100 %
        assertEquals(0.0, under.remainingCost)
    }

    @Test
    fun `dedicke rijeni — notar a katastr za majetek 6 mil s 2 nemovitostmi`() {
        val r = InheritanceProceedingsCalculator.calculate(6_000_000.0, 2)
        assertEquals(33_000.0, r.notaryFee) // 3 000 + 0,5 % × 6 mil
        assertEquals(600.0, r.registryFees) // 2 × 300 Kč
        assertEquals(33_600.0, r.totalCost)
        assertTrue(r.costPctOfEstate > 0.5 && r.costPctOfEstate < 0.7) // ≈ 0,56 %
    }

    @Test
    fun `povinni dil — polovina nalezhiteho podilu pro kazdeho dedice`() {
        val r = ForcedShareCalculator.calculate(6_000_000.0, 3)
        assertEquals(2_000_000.0, r.statutorySharePerHeir) // 6 mil / 3
        assertEquals(1_000_000.0, r.forcedSharePerHeir) // polovina
        assertEquals(3_000_000.0, r.totalForcedClaim) // celkem pro všechny povinné
    }

    @Test
    fun `dar vs dedictvi — velky majetek vyjde lepe jako dedictvi`() {
        val r = GiftVsInheritanceCalculator.calculate(1_000_000.0, isCloseRelative = true, propertiesCount = 1)
        // dar: (1 mil − 8 tis osvobození) × 15 % daň → čistě 851 200
        assertEquals(851_200.0, r.giftNet)
        // dědictví: jen náklady řízení (3 000 + 5 000 + 300 = 8 300) → čistě 991 700
        assertEquals(991_700.0, r.inheritanceNet)
        assertEquals("dědictví", r.betterOption)
    }

    @Test
    fun `dar vs dedictvi — maly majetek vyjde lepe jako dar`() {
        val r = GiftVsInheritanceCalculator.calculate(20_000.0, isCloseRelative = true, propertiesCount = 0)
        // dar: (20 tis − 8 tis osvobození) × 15 % = 1 800 → čistě 18 200
        assertEquals(18_200.0, r.giftNet)
        // dědictví: notář 3 000 + 0,5 % × 20 tis = 3 100 → čistě 16 900
        assertEquals(16_900.0, r.inheritanceNet)
        assertTrue(r.inheritanceNet < r.giftNet)
        assertEquals("dar", r.betterOption)
    }
}
