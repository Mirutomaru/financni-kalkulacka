package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.calculators.housing.BonityCalculator
import com.mirda.financnikalkulacky.calculators.housing.EnergyCostCalculator
import com.mirda.financnikalkulacky.calculators.housing.EnergyInvestCalculator
import com.mirda.financnikalkulacky.calculators.housing.HouseBuildCalculator
import com.mirda.financnikalkulacky.calculators.housing.MortgageCalculator
import com.mirda.financnikalkulacky.calculators.housing.NzgSubsidyCalculator
import com.mirda.financnikalkulacky.calculators.housing.PropertyInsuranceCalculator
import com.mirda.financnikalkulacky.calculators.housing.PropertyTaxCalculator
import com.mirda.financnikalkulacky.calculators.housing.RealEstateSaleTaxCalculator
import com.mirda.financnikalkulacky.calculators.housing.RentSettingCalculator
import com.mirda.financnikalkulacky.calculators.housing.RentalIncomeTaxCalculator
import com.mirda.financnikalkulacky.calculators.housing.RenovationCalculator
import com.mirda.financnikalkulacky.calculators.housing.RoommateSplitCalculator
import com.mirda.financnikalkulacky.calculators.housing.PurchaseCostCalculator
import com.mirda.financnikalkulacky.calculators.housing.SvjFundCalculator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HousingExpansionTest {

    @Test fun `rpsn je blizky nominalni sazb`() {
        // Bez poplatku RPSN = efektivni rocní úrok, u anuity o málo nad nominální sazbu.
        val r = MortgageCalculator.rpsn(2_000_000.0, 5.0, 30)
        assertTrue(r > 5.0 && r < 5.4, "RPSN=$r")
    }

    @Test fun `bonita — limit z prichodu a LTV`() {
        // Hrubý příjem 60 tis/měs., bez dluhů, cena 8 mil, vlastní 1,6 mil (20 %).
        val r = BonityCalculator.calculate(
            grossMonthly = 60_000.0, existingDebt = 0.0, price = 8_000_000.0,
            ownFunds = 1_600_000.0, ratePct = 5.0, years = 30,
        )
        // LTV limit: úvěr ≤ 80 % ceny = 6,4 mil; vlastní kryjí zbytek → maxLoanByLtv = 6,4 - 1,6 = 4,8 mil
        assertEquals(4_800_000.0, r.maxLoanByLtv, 1.0)
        // DSTI limit: splátka ≤ 50 % příjmu = 30 tis/měs. → úvěr z příjmů > LTV limitu?
        assertTrue(r.maxLoanByIncome > 4_800_000.0, "maxLoanByIncome=${r.maxLoanByIncome}")
        // Skutečná bonita = min(obou) = LTV limit
        assertEquals(4_800_000.0, r.maxLoan, 1.0)
        assertTrue(r.passesLtv)
        assertTrue(r.passesDsti)
    }

    @Test fun `bonita — omezeni prichodem`() {
        // Nízký příjem: bonita musí být určena příjmem, ne LTV.
        val r = BonityCalculator.calculate(
            grossMonthly = 25_000.0, existingDebt = 0.0, price = 8_000_000.0,
            ownFunds = 1_600_000.0, ratePct = 5.0, years = 30,
        )
        assertTrue(r.maxLoanByIncome < r.maxLoanByLtv)
        assertEquals(r.maxLoanByIncome, r.maxLoan, 1.0)
    }

    @Test fun `vedlejni naklady koupe`() {
        val r = PurchaseCostCalculator.calculate(5_000_000.0, escrowPct = 2.0, brokerFeePct = 3.0, loanProcessing = 15_000.0)
        // Notář/úschova 2 % = 100 tis; katastr 0,5 % = 25 tis; provize 3 % = 150 tis; úvěr 15 tis
        assertEquals(100_000.0, r.notaryEscrow, 1.0)
        assertEquals(25_000.0, r.landRegistry, 1.0)
        assertEquals(150_000.0, r.realEstateFee, 1.0)
        assertEquals(290_000.0, r.totalCosts, 1.0)
        // Náklady ~5,8 % ceny (orientační 5–10 %)
        assertTrue(r.costsPctOfPrice > 5.0 && r.costsPctOfPrice < 7.0)
    }

    @Test fun `vystavba domu — rozpočet`() {
        val r = HouseBuildCalculator.calculate(sqm = 120.0, costPerSqm = 45_000.0, landCost = 1_500_000.0, utilitiesExtra = 50_000.0)
        // Stavba 120 × 45 tis = 5,4 mil; přípojky základ 350 tis + 50 tis = 400 tis
        assertEquals(5_400_000.0, r.buildingCost, 1.0)
        assertEquals(400_000.0, r.utilitiesCost, 1.0)
        // Celkem = 5,4 + 1,5 + 0,4 = 7,3 mil
        assertEquals(7_300_000.0, r.totalBudget, 1.0)
        // Doporučená půjčka při 20 % vlastních zdrojů = 80 % rozpočtu
        assertEquals(5_840_000.0, r.recommendedMortgage, 1.0)
    }

    @Test fun `naklady na energie`() {
        val r = EnergyCostCalculator.calculate(elecKwh = 3_000.0, elecPrice = 8.0, gasM3 = 1_200.0, gasPrice = 90.0, heatingCost = 40_000.0, waterM3 = 60.0, waterPrice = 150.0)
        // Elektřina 3000×8=24 tis; plyn 1200×90=108 tis; voda 60×150=9 tis; topení 40 tis
        assertEquals(24_000.0, r.electricityAnnual, 1.0)
        assertEquals(108_000.0, r.gasAnnual, 1.0)
        assertEquals(9_000.0, r.waterAnnual, 1.0)
        // Celkem = 24+108+40+9 = 181 tis/rok; měsíčně ~15 083
        assertEquals(181_000.0, r.totalAnnual, 1.0)
        assertTrue(r.totalMonthly > 15_000.0 && r.totalMonthly < 16_000.0)
    }

    @Test fun `fond oprav SVJ a zalohy`() {
        val r = SvjFundCalculator.calculate(unitValue = 4_000_000.0, serviceCosts = 24_000.0, fundYears = 10)
        // Měsíční záloha na služby = 24 tis/12 = 2 tis; fond cíl = 5 % hodnoty = 200 tis
        assertEquals(2_000.0, r.monthlyPerUnit, 1.0)
        assertEquals(200_000.0, r.fundTarget, 1.0)
        // Měsíční příspěvek do fondu (10 let) = 200 tis / 120 = 1667
        assertTrue(r.fundMonthlyContribution > 1_600.0 && r.fundMonthlyContribution < 1_750.0)
    }

    @Test fun `dan z nemovitych veci — byt`() {
        val r = PropertyTaxCalculator.calculate(type = "byt", rate = 8.0, area = 90.0)
        // Daň = sazba × plocha = 8 × 90 = 720 Kč/rok
        assertEquals(720.0, r.annualTax, 1.0)
    }

    @Test fun `pojisteni nemovitosti — podpojisteni`() {
        // Budova hodnota 5 mil, pojištěno jen 3 mil → podpojištění 40 %
        val r = PropertyInsuranceCalculator.calculate(buildingValue = 5_000_000.0, contentsValue = 1_000_000.0, insuredBuilding = 3_000_000.0, insuredContents = 1_000_000.0)
        assertEquals(40.0, r.underinsuredPct, 0.5)
        assertTrue(r.warning.contains("PODPOJIŠTĚNO"))
    }

    @Test fun `pojisteni nemovitosti — v poradku`() {
        val r = PropertyInsuranceCalculator.calculate(buildingValue = 5_000_000.0, contentsValue = 1_000_000.0, insuredBuilding = 5_000_000.0, insuredContents = 1_000_000.0)
        assertEquals(0.0, r.underinsuredPct, 0.5)
    }

    @Test fun `spolubydlici — rovny podil`() {
        val r = RoommateSplitCalculator.calculate(rent = 24_000.0, utilities = 6_000.0, internet = 1_000.0, n = 3, extraShares = emptyMap())
        // Celkem 31 tis / 3 osoby = ~10 333 na osobu
        assertEquals(31_000.0, r.totalMonthly, 1.0)
        assertTrue(r.equalShare > 10_200.0 && r.equalShare < 10_500.0)
    }

    @Test fun `navratnost energeticke investice`() {
        // Investice 100 tis, dotace 40 tis → po dotaci 60 tis; úspora 12 tis/rok → návratnost 5 let
        val r = EnergyInvestCalculator.calculate(cost = 100_000.0, subsidy = 40_000.0, annualSavings = 12_000.0)
        assertEquals(60_000.0, r.investment, 1.0)
        assertTrue(r.paybackYears > 4.9 && r.paybackYears < 5.1)
    }

    @Test fun `dotace NZU — fotovoltaika`() {
        val r = NzgSubsidyCalculator.calculate(type = "fotovoltaika", cost = 200_000.0)
        // 50 % z 200 tis = 100 tis (pod stropem 120 tis)
        assertEquals(100_000.0, r.estimatedSubsidy, 1.0)
        assertEquals(100_000.0, r.netCost, 1.0)
    }

    @Test fun `dotace NZU — tepelne cerpadlo strop`() {
        val r = NzgSubsidyCalculator.calculate(type = "tepelnecerpadlo", cost = 600_000.0)
        // 40 % z 600 tis = 240 tis, ale strop 180 tis
        assertEquals(180_000.0, r.estimatedSubsidy, 1.0)
    }

    @Test fun `navratnost rekonstrukce`() {
        // Náklad 500 tis, navýšení hodnoty 30 % z nákladu = 150 tis; ROI = 30 %
        val r = RenovationCalculator.calculate(cost = 500_000.0, valueIncreasePct = 30.0, rentBefore = 12_000.0, rentAfter = 16_000.0)
        assertEquals(150_000.0, r.valueIncrease, 1.0)
        assertTrue(r.roiPct > 29.0 && r.roiPct < 31.0)
        // Návratnost z vyššího nájmu: 500 tis / (4 tis×12) = ~10,4 let
        assertTrue(r.paybackYears > 10.0 && r.paybackYears < 11.0)
    }

    @Test fun `zdaneni najmu — paušal vyhodnejsi`() {
        // Příjem 120 tis/rok, skutečné výdaje jen 30 tis → paušál (36 tis) je vyšší odpočet
        val r = RentalIncomeTaxCalculator.calculate(income = 120_000.0, expenses = 30_000.0, depreciation = 5_000.0)
        // Paušál 30 % = 36 tis > skutečné 35 tis → paušál daňově výhodnější
        assertTrue(r.taxFlat < r.taxActual)
    }

    @Test fun `zdaneni najmu — skutecne vyhodnejsi`() {
        // Vysoké výdaje + odpisy > 30 % → skutečné výdaje daňově výhodnější
        val r = RentalIncomeTaxCalculator.calculate(income = 120_000.0, expenses = 60_000.0, depreciation = 40_000.0)
        assertTrue(r.taxActual < r.taxFlat)
    }

    @Test fun `zdaneni prodeje — casovy test`() {
        // Držba 5+ let → osvobozeno
        val r = RealEstateSaleTaxCalculator.calculate(purchasePrice = 3_000_000.0, salePrice = 4_500_000.0, yearsHeld = 6, residentialNeed = false)
        assertTrue(r.exempt)
        assertEquals(1_500_000.0, r.gain, 1.0)
    }

    @Test fun `zdaneni prodeje — zdaneno`() {
        // Držba < 5 let a bez bytové potřeby → daň 15 % ze zisku
        val r = RealEstateSaleTaxCalculator.calculate(purchasePrice = 3_000_000.0, salePrice = 4_500_000.0, yearsHeld = 2, residentialNeed = false)
        assertTrue(!r.exempt)
        assertEquals(225_000.0, r.tax, 1.0) // 15 % z 1,5 mil
    }

    @Test fun `zdaneni prodeje — bytova potreba`() {
        val r = RealEstateSaleTaxCalculator.calculate(purchasePrice = 3_000_000.0, salePrice = 4_500_000.0, yearsHeld = 1, residentialNeed = true)
        assertTrue(r.exempt)
    }

    @Test fun `vyska najemneho a kauce`() {
        val r = RentSettingCalculator.calculate(rent = 15_000.0, depositMonths = 3, inflationPct = 4.0, years = 5)
        // Kauce = 3 měsíce = 45 tis
        assertEquals(45_000.0, r.depositAmount, 1.0)
        // Nájem po valorizaci o inflaci za 5 let > aktuální
        assertTrue(r.rentAfterInflation > 15_000.0)
    }
}
