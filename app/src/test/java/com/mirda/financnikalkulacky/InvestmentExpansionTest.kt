package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.investments.AnnuityCalculator
import com.mirda.financnikalkulacky.calculators.investments.BondYtmCalculator
import com.mirda.financnikalkulacky.calculators.investments.BuildingSocietyCalculator
import com.mirda.financnikalkulacky.calculators.investments.CurrencyConversionCalculator
import com.mirda.financnikalkulacky.calculators.investments.CryptoTaxCalculator
import com.mirda.financnikalkulacky.calculators.investments.DipVsDpsCalculator
import com.mirda.financnikalkulacky.calculators.investments.EarlyLatePensionCalculator
import com.mirda.financnikalkulacky.calculators.investments.EmergencyFundCalculator
import com.mirda.financnikalkulacky.calculators.investments.FeeImpactCalculator
import com.mirda.financnikalkulacky.calculators.investments.GoalSavingsCalculator
import com.mirda.financnikalkulacky.calculators.investments.InvestmentTaxCalculator
import com.mirda.financnikalkulacky.calculators.investments.LifeInsuranceCalculator
import com.mirda.financnikalkulacky.calculators.investments.LumpSumVsDcaCalculator
import com.mirda.financnikalkulacky.calculators.investments.PensionGapCalculator
import com.mirda.financnikalkulacky.calculators.investments.PensionSavingCalculator
import com.mirda.financnikalkulacky.calculators.investments.RetirementAgeCalculator
import com.mirda.financnikalkulacky.calculators.investments.SavingsAccountCalculator
import com.mirda.financnikalkulacky.calculators.investments.StatePensionCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InvestmentExpansionTest {

    // ── SPOŘENÍ ────────────────────────────────────────────────

    @Test
    fun `sporici uct — slozene uroceni po zdaneni`() {
        val r = SavingsAccountCalculator.calculate(100_000.0, 4.0, 5)
        // hrube uroky = 100000 * (1.04^5 - 1) ≈ 21665
        assertTrue(r.grossInterest > 21_000.0 && r.grossInterest < 22_000.0, "gross=${r.grossInterest}")
        assertEquals(r.grossInterest * 0.15, r.taxOnInterest, 1.0)
        assertEquals(r.grossInterest - r.taxOnInterest, r.netInterest, 1.0)
        assertTrue(r.finalValue > 118_000.0 && r.finalValue < 119_000.0, "final=${r.finalValue}")
    }

    @Test
    fun `sporici uct — nulova sazba zname jen vklad`() {
        val r = SavingsAccountCalculator.calculate(50_000.0, 0.0, 3)
        assertEquals(50_000.0, r.finalValue, 1.0)
        assertEquals(0.0, r.grossInterest, 1.0)
    }

    @Test
    fun `stavebni sporeni — statni podpora a uver`() {
        val r = BuildingSocietyCalculator.calculate(3_000.0, 10, 3.0, 800_000.0, 10, 4.0)
        assertEquals(360_000.0, r.totalSaved, 1.0)
        // podpora 5 % z 36 tis/rok = 1800/rok → 18 tis za 10 let
        assertEquals(18_000.0, r.stateSubsidyTotal, 1.0)
        assertTrue(r.finalValue > r.totalSaved + r.stateSubsidyTotal, "final=${r.finalValue}")
        // splátka úvěru 800 tis / 10 let @ 4 % ≈ 8100 Kč/měs.
        assertTrue(r.loanMonthlyPayment > 7_500.0 && r.loanMonthlyPayment < 9_000.0, "pmt=${r.loanMonthlyPayment}")
        assertTrue(r.effectiveReturnPct > 2.0 && r.effectiveReturnPct < 4.0, "eff=${r.effectiveReturnPct}")
    }

    @Test
    fun `stavebni sporeni — strop statni podpory`() {
        // 10 tis/měs = 120 tis/rok → 5 % = 6 tis > strop 30 tis? ne, 6 tis < 30 tis; vezmi 700 tis/rok
        val r = BuildingSocietyCalculator.calculate(70_000.0, 1, 3.0, 0.0, 1, 4.0)
        assertEquals(BuildingSocietyCalculator.STATE_SUBSIDY_CAP, r.stateSubsidyTotal, 1.0)
    }

    @Test
    fun `DPS — statni prispevek a dane odpocty`() {
        val r = PensionSavingCalculator.calculate(2_000.0, 1_000.0, 400_000.0, 15.0)
        assertEquals(2_880.0, r.stateContributionTotal, 1.0) // 240 Kč/měs × 12
        assertEquals(24_000.0, r.taxDeductibleAnnual, 1.0)   // min(24 tis, 12 % z 400 tis)
        assertEquals(3_600.0, r.annualTaxSaving, 1.0)        // 15 % z 24 tis
        assertEquals(38_880.0, r.totalCapitalAnnual, 1.0)    // (2+1 tis)*12 + 2880
        assertEquals(1_460.0, r.netCostMonthly, 1.0)         // 2000 - 300 - 240
    }

    @Test
    fun `DPS — pod minimum neni statni prispevek`() {
        val r = PensionSavingCalculator.calculate(50.0, 0.0, 100_000.0, 15.0)
        assertEquals(0.0, r.stateContributionTotal, 1.0)
    }

    @Test
    fun `DIP vs DPS — vyssi vklad do DIP vyhraje`() {
        val r = DipVsDpsCalculator.calculate(5_000.0, 2_000.0, 400_000.0, 15.0)
        assertEquals("DIP", r.betterMode)
        assertEquals(9_000.0, r.dipTaxSaving, 1.0)   // 60 tis × 15 %
        assertTrue(r.advantageAnnual > 2_000.0, "adv=${r.advantageAnnual}")
    }

    @Test
    fun `DIP vs DPS — pri malych vkladech vyhrali DPS`() {
        val r = DipVsDpsCalculator.calculate(500.0, 1_000.0, 400_000.0, 15.0)
        assertEquals("DPS", r.betterMode) // stát přispěje 2880/rok navíc
    }

    @Test
    fun `zivotni pojisteni — dane odpocty snizuji cenu`() {
        val r = LifeInsuranceCalculator.calculate(1_000.0, 10, 3.0, 15.0)
        assertEquals(120_000.0, r.totalPremiums, 1.0)
        assertEquals(12_000.0, r.taxDeductibleAnnual, 1.0) // pod stropem 30 tis
        assertEquals(1800.0, r.annualTaxSaving, 1.0)       // 15 % z 12 tis
        assertEquals(102_000.0, r.netCostTotal, 1.0)       // 120 tis − 18 tis úspor
        assertTrue(r.finalValue > r.totalPremiums, "final=${r.finalValue}")
        assertTrue(r.effectiveReturnPct > 2.0, "eff=${r.effectiveReturnPct}")
    }

    @Test
    fun `zivotni pojisteni — strop odpoctu 30 tis`() {
        val r = LifeInsuranceCalculator.calculate(5_000.0, 10, 2.0, 15.0) // 60 tis/rok > 30 tis
        assertEquals(LifeInsuranceCalculator.TAX_DEDUCTION_CAP, r.taxDeductibleAnnual, 1.0)
    }

    @Test
    fun `nouzova rezerva — chybici castka a mensicni ulozka`() {
        val r = EmergencyFundCalculator.calculate(25_000.0, 6, 50_000.0, 12)
        assertEquals(150_000.0, r.recommendedAmount, 1.0)
        assertEquals(100_000.0, r.currentShortfall, 1.0)
        assertEquals(8_333.33, r.monthlySavingsNeeded, 1.0)
    }

    @Test
    fun `nouzova rezerva — uz dosazeno`() {
        val r = EmergencyFundCalculator.calculate(25_000.0, 6, 200_000.0, 12)
        assertEquals(0.0, r.currentShortfall, 1.0)
    }

    @Test
    fun `sporeni na cil — mensicni ulozka dosahuje cile`() {
        val r = GoalSavingsCalculator.calculate(1_000_000.0, 10, 4.0)
        assertTrue(r.requiredMonthly > 6_000.0 && r.requiredMonthly < 8_000.0, "pmt=${r.requiredMonthly}")
        assertEquals(1_000_000.0, r.finalValue, 1.0)
        assertTrue(r.interestEarned > 50_000.0, "interest=${r.interestEarned}")
    }

    // ── INVESTICE ──────────────────────────────────────────────

    @Test
    fun `jednorazove vs DCA — pri rucicem trhu vyhrali jednorazove`() {
        val r = LumpSumVsDcaCalculator.calculate(100_000.0, 10, 7.0)
        assertEquals("jednorázově", r.betterMode)
        assertTrue(r.lumpSumFinal > 190_000.0 && r.lumpSumFinal < 210_000.0, "lump=${r.lumpSumFinal}")
        assertTrue(r.difference > 30_000.0, "diff=${r.difference}")
    }

    @Test
    fun `jednorazove vs DCA — stejny celkovy kapital`() {
        val r = LumpSumVsDcaCalculator.calculate(120_000.0, 5, 3.0)
        // oba scénáře investují stejnou částku; rozdíl je jen v načasování
        assertTrue(r.lumpSumFinal > 120_000.0 && r.dcaFinal > 120_000.0)
    }

    @Test
    fun `vliv poplatku TER — ztrata narasta s dobou`() {
        val short = FeeImpactCalculator.calculate(100_000.0, 7.0, 0.5, 5)
        val long = FeeImpactCalculator.calculate(100_000.0, 7.0, 0.5, 30)
        assertTrue(long.lostToFees > short.lostToFees * 2, "long=${long.lostToFees} short=${short.lostToFees}")
        assertTrue(long.effectiveReturnPct < 7.0, "eff=${long.effectiveReturnPct}")
    }

    @Test
    fun `zdaneni investic — casovy test 3 roky osvobodi`() {
        val r = InvestmentTaxCalculator.calculate(100_000.0, 200_000.0, 4, 0.0)
        assertEquals(100_000.0, r.gain, 1.0)
        assertEquals(0.0, r.tax, 1.0)
    }

    @Test
    fun `zdaneni investic — limit 30 tis a daň 15 %`() {
        val r = InvestmentTaxCalculator.calculate(100_000.0, 200_000.0, 1, 0.0)
        assertEquals(70_000.0, r.taxableGain, 1.0) // 100 tis − limit 30 tis
        assertEquals(10_500.0, r.tax, 1.0)
    }

    @Test
    fun `zdaneni investic — ostatni zisky snizi limit`() {
        val r = InvestmentTaxCalculator.calculate(100_000.0, 200_000.0, 1, 30_000.0)
        assertEquals(100_000.0, r.taxableGain, 1.0) // limit vyčerpaný jinými zisky
    }

    @Test
    fun `krypto — zisk nad limitem se zdaňuje`() {
        val r = CryptoTaxCalculator.calculate(200_000.0, 300_000.0)
        assertEquals(100_000.0, r.totalGain, 1.0)
        assertEquals(70_000.0, r.taxableGain, 1.0)
        assertEquals(10_500.0, r.tax, 1.0)
    }

    @Test
    fun `krypto — zisk pod limitem je osvobozen`() {
        val r = CryptoTaxCalculator.calculate(100_000.0, 120_000.0)
        assertEquals(0.0, r.tax, 1.0)
    }

    @Test
    fun `dluhopis YTM — diskontovany dluhopis ma vyssi ytm nez kupon`() {
        val r = BondYtmCalculator.calculate(950.0, 1_000.0, 3.0, 5)
        assertEquals(30.0, r.annualCoupon, 1.0)
        assertTrue(r.ytmPct > 4.0 && r.ytmPct < 4.2, "ytm=${r.ytmPct}") // ≈ 4,1 %
        assertEquals(21.05, r.totalReturnPct, 0.1)
    }

    @Test
    fun `prepočet men — oslabeni kurzu snizi hodnotu v Kc`() {
        val r = CurrencyConversionCalculator.calculate(1_000.0, 26.0, -5.0)
        assertEquals(26_000.0, r.convertedAmount, 1.0)
        assertEquals(24_700.0, r.futureValueAtRate, 1.0)
        assertEquals(-5.0, r.currencyRiskPct, 0.01)
    }

    // ── DŮCHOD A FINANČNÍ NEZÁVISLOST ──────────────────────────

    @Test
    fun `statni duchod — odhad nad zakladni castkou`() {
        val r = StatePensionCalculator.calculate(30_000.0, 35)
        assertTrue(r.estimatedMonthly > StatePensionCalculator.BASE_PENSION_2026, "est=${r.estimatedMonthly}")
        assertEquals(35.0, r.yearsCounted, 1.0)
    }

    @Test
    fun `statni duchod — strop prichodu`() {
        val low = StatePensionCalculator.calculate(20_000.0, 35).estimatedMonthly
        val high = StatePensionCalculator.calculate(200_000.0, 35).estimatedMonthly // nad stropem
        assertTrue(high > low)
        assertEquals(StatePensionCalculator.INCOME_CAP / 100.0 * 35 * 0.013 + StatePensionCalculator.BASE_PENSION_2026 + (StatePensionCalculator.INCOME_CAP / StatePensionCalculator.INCOME_CAP) * StatePensionCalculator.BASE_PENSION_2026 * 0.5, high, 1.0)
    }

    @Test
    fun `vek odchodu — deti snizuji vek`() {
        val r = RetirementAgeCalculator.calculate(1980, 2)
        assertEquals(63.0, r.statutoryAge, 0.01)
        assertEquals(24, r.monthsReduced)
        assertEquals(61.0, r.ageWithChildren, 0.01)
    }

    @Test
    fun `predcasny a odlozeny duchod — kraceni a navyseni`() {
        val r = EarlyLatePensionCalculator.calculate(15_000.0, 2, 3)
        assertEquals(12.8, r.reductionPct, 0.01)
        assertEquals(13_080.0, r.earlyMonthly, 1.0)
        assertEquals(21.6, r.increasePct, 0.01)
        assertEquals(18_240.0, r.lateMonthly, 1.0)
    }

    @Test
    fun `duchodova mezera — portfolio podle pravidla 4 %`() {
        val r = PensionGapCalculator.calculate(25_000.0, 30_000.0, 35)
        assertTrue(r.monthlyGap > 15_000.0 && r.monthlyGap < 20_000.0, "gap=${r.monthlyGap}")
        assertEquals(r.monthlyGap * 12.0 / 0.04, r.requiredPortfolio, 1.0)
    }

    @Test
    fun `duchodova mezera — pri dosazeni prichodu nula`() {
        val r = PensionGapCalculator.calculate(5_000.0, 30_000.0, 35)
        assertEquals(0.0, r.monthlyGap, 1.0)
    }

    @Test
    fun `renta — vyssi vynos prodlouzi vydrzeni`() {
        val withReturn = AnnuityCalculator.calculate(1_000_000.0, 20_000.0, 4.0)
        val noReturn = AnnuityCalculator.calculate(1_000_000.0, 20_000.0, 0.0)
        assertTrue(withReturn.durationYears > noReturn.durationYears, "with=${withReturn.durationYears} no=${noReturn.durationYears}")
    }

    @Test
    fun `renta — vyssi vyber vyčerpa dříve`() {
        val low = AnnuityCalculator.calculate(5_000_000.0, 20_000.0, 4.0)
        val high = AnnuityCalculator.calculate(5_000_000.0, 30_000.0, 4.0)
        assertTrue(low.durationYears > high.durationYears)
    }

    @Test
    fun `renta — vyber nad udrzitelny zkrati vydrzeni`() {
        val r = AnnuityCalculator.calculate(5_000_000.0, 30_000.0, 4.0)
        // udržitelný výběr při 4 % je ~16 700 Kč/měs; 30 tis vyčerpá portfolio za ~20 let
        assertTrue(r.durationYears > 15.0 && r.durationYears < 25.0, "years=${r.durationYears}")
    }
}
