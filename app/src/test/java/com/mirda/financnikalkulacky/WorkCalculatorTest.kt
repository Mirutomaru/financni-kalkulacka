package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.work.DppDpcCalculator
import com.mirda.financnikalkulacky.calculators.work.MaternityCalculator
import com.mirda.financnikalkulacky.calculators.work.OsvcCalculator
import com.mirda.financnikalkulacky.calculators.work.SalaryCalculator
import com.mirda.financnikalkulacky.calculators.work.SickLeaveCalculator
import com.mirda.financnikalkulacky.calculators.work.WorkBenefitsCalculator
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue

class WorkCalculatorTest {
    @Test fun `cista mzda 30 tis bez deti`() {
        val r = SalaryCalculator.calculate(30_000.0, children = 0)
        // ZP 4,5 % = 1350; SP 7,1 % = 2130
        assertEquals(1_350.0, r.healthInsurance)
        assertEquals(2_130.0, r.socialInsurance)
        // Cista = 30000 - 1350 - 2130 - (54000-30840)/12 = 24 590
        assertEquals(24_590.0, r.netMonthly)
    }

    @Test fun `daňovy bonus na dite`() {
        // Nizka mzda 15 tis — slevy prevysuji dan, vznikne bonus
        val r = SalaryCalculator.calculate(15_000.0, children = 2)
        // Slevy (30840+18000+36000=84840) > hruba dan 27000 -> bonus 57840/rok = 4820/mes
        assertEquals(0.0, r.netTaxAnnual)
        assertTrue(r.childBonusMonthly > 4_800.0 && r.childBonusMonthly < 4_900.0)
    }

    @Test fun `OSVČ paušal vs priznani`() {
        // 500 tis prijem, 60 % vydati, pasmo 1: paušal 109 944 vs priznani 108 744 -> priznani vyhodnejsi o 1200
        val r = OsvcCalculator.calculate(500_000.0, expensePct = 60.0, band = 1)
        assertEquals(109_944.0, r.flatTaxAnnual)
        assertEquals("priznani", r.betterMode)
        assertEquals(1_200.0, r.savings)
    }

    @Test fun `DPP do limitu bez odvodu`() {
        // 4000 do limitu, s prohlášením: dan max(0,600-2570)=0, zadne odvody -> ciste 4000
        val r = DppDpcCalculator.calculate(4_000.0, hasTaxDeclaration = true)
        assertEquals(4_000.0, r.netAmount)
        assertTrue(!r.fullContributions)
    }

    @Test fun `DPP nad limitem s plnymi odvody`() {
        // 10 tis nad limitem 4100 -> plne SP+ZP, dan se slevou
        val r = DppDpcCalculator.calculate(10_000.0, hasTaxDeclaration = true)
        assertTrue(r.fullContributions)
        // SP 710, ZP min 456, dan max(0,1500-2570)=0 -> ciste 8834
        assertEquals(8_834.0, r.netAmount)
    }

    @Test fun `nemocenska s redukci a fazemi`() {
        // 30 tis mes, 45 dni: DVZ=900; nahraza mzdy 10 prac.dni=9000; stat 8640+8910=17550
        val r = SickLeaveCalculator.calculate(30_000.0, sickDays = 45)
        assertEquals(900.0, r.dailyBaseReduced)
        assertEquals(9_000.0, r.wageCompensation14d)
        assertEquals(26_550.0, r.totalReceived)
    }

    @Test fun `mateřska PPM a rodičovsky`() {
        // 30 tis mes: DVZ=900; PPM 28 tydnů = 900*0.7*196 = 123 480
        val r = MaternityCalculator.calculate(30_000.0, totalParentalBenefit = 340_000.0, childAgeYears = 3)
        assertEquals(900.0, r.dailyBaseReduced)
        assertEquals(123_480.0, r.ppmTotal28w)
        // Rodičovský 340 tis / 36 mes = 9444/mes (pod limitem PPM)
        assertTrue(r.parentalBenefitMonthly > 9_400.0 && r.parentalBenefitMonthly < 9_500.0)
    }

    @Test fun `odstupne dle delky zamestnani`() {
        // 3 roky -> 3x PV; dan 15 %
        val r = WorkBenefitsCalculator.severance(20_000.0, yearsEmployed = 3.0)
        assertEquals(3, r.multiplier)
        assertEquals(60_000.0, r.grossSeverance)
        assertEquals(51_000.0, r.netSeverance)
    }

    @Test fun `podpora v nezamestnanosti dle veiku`() {
        // 45 let -> 5 mesicu; faze 65/50/45 %
        val r = WorkBenefitsCalculator.unemployment(20_000.0, age = 45, mutualAgreementNoCause = false)
        assertEquals(5, r.months)
        // 13000+13000+10000+10000+9000 = 55 000
        assertEquals(55_000.0, r.totalPaid)
    }

    @Test fun `podpora dohodou bez vazneho duvodu 45 procent`() {
        // 52 let -> 8 mesicu; dohodou bez duvodu = 45 % po celou dobu
        val r = WorkBenefitsCalculator.unemployment(20_000.0, age = 52, mutualAgreementNoCause = true)
        assertEquals(8, r.months)
        // 9000 * 8 = 72 000
        assertEquals(72_000.0, r.totalPaid)
    }
}
