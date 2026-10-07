package com.mirda.financnikalkulacky.calculators.housing

import kotlin.math.abs
import kotlin.math.pow

/** Výsledek návratnosti energetické investice (FV, tepelné čerpadlo, zateplení). */
data class EnergyInvestResult(
    val investment: Double,          // cena investice po dotaci (Kč)
    val annualSavings: Double,       // roční úspora na energiích (Kč)
    val paybackYears: Double,        // návratnost v letech
    val totalSavings30y: Double,     // kumulovaná úspora za 30 let (bez inflace)
    val netGain30y: Double,          // čistý zisk za 30 let (úspora − investice)
)

object EnergyInvestCalculator {
    /**
     * Návratnost energetické investice.
     * cost — cena pořízení, subsidy — dotace (Kč), annualSavings — roční úspora na energiích (Kč).
     */
    fun calculate(cost: Double, subsidy: Double, annualSavings: Double): EnergyInvestResult {
        val net = maxOf(cost - subsidy, 0.0)
        val payback = if (annualSavings > 0.0) net / annualSavings else 999.0
        return EnergyInvestResult(
            investment = net,
            annualSavings = annualSavings,
            paybackYears = payback,
            totalSavings30y = annualSavings * 30.0,
            netGain30y = annualSavings * 30.0 - net,
        )
    }
}

/** Výsledek odhadu dotace Nová zelená úsporám (NZÚ). */
data class NzgSubsidyResult(
    val estimatedSubsidy: Double,    // orientační dotace (Kč)
    val netCost: Double,             // cena po dotaci
    val note: String,                // poznámka k programu
)

object NzgSubsidyCalculator {
    /**
     * Orientační odhad dotace NZÚ dle typu opatření.
     * type: "fotovoltaika" | "tepelnecerpadlo" | "zatepleni", cost — cena pořízení (Kč).
     */
    fun calculate(type: String, cost: Double): NzgSubsidyResult {
        val subsidy = when (type) {
            "fotovoltaika" -> minOf(cost * 0.50, 120_000.0)   // do ~50 %, strop orientační
            "tepelnecerpadlo" -> minOf(cost * 0.40, 180_000.0)
            "zatepleni" -> minOf(cost * 0.30, 250_000.0)
            else -> 0.0
        }
        return NzgSubsidyResult(
            estimatedSubsidy = subsidy,
            netCost = maxOf(cost - subsidy, 0.0),
            note = "Orientační odhad — skutečnou výši určí program NZÚ a splněné podmínky",
        )
    }
}

/** Výsledek návratnosti rekonstrukce (vylepšení hodnoty nemovitosti). */
data class RenovationResult(
    val cost: Double,                // cena rekonstrukce
    val valueIncrease: Double,       // navýšení tržní hodnoty
    val roiPct: Double,              // návratnost investice v % (navýšení / náklad)
    val monthlyRentIncrease: Double, // odhad navýšení nájmu (měs.)
    val paybackYears: Double,        // návratnost z vyššího nájmu (roky), 0 = bez pronájmu
)

object RenovationCalculator {
    /**
     * Návratnost rekonstrukce.
     * cost — náklad, valueIncreasePct — navýšení hodnoty v %, rentBefore/rentAfter — nájem před/po (měs.).
     */
    fun calculate(cost: Double, valueIncreasePct: Double, rentBefore: Double, rentAfter: Double): RenovationResult {
        val valueInc = valueIncreasePct / 100.0 * cost // navýšení hodnoty dle % z nákladu (orientační)
        val roi = if (cost > 0.0) valueInc / cost * 100.0 else 0.0
        val rentDiff = maxOf(rentAfter - rentBefore, 0.0)
        val payback = if (rentDiff > 0.0) cost / (rentDiff * 12.0) else 999.0
        return RenovationResult(cost, valueInc, roi, rentDiff, payback)
    }
}

/** Výsledek zdanění příjmů z nájmu (skutečné výdaje vs. 30% paušál). */
data class RentalTaxResult(
    val grossIncome: Double,         // roční hrubý příjem z nájmu
    val actualDeduction: Double,     // odpočet skutečných výdajů (včetně odpisů)
    val flatDeduction: Double,       // 30% paušální výdaje
    val taxableActual: Double,       // základ daně — skutečné
    val taxableFlat: Double,         // základ daně — paušál
    val taxActual: Double,           // daň — skutečné (15 %)
    val taxFlat: Double,             // daň — paušál (15 %)
    val betterMode: String,          // "skutečne" | "paušál"
    val savings: Double,             // úspora zvolením lepšího režimu
)

object RentalIncomeTaxCalculator {
    /**
     * Zdanění nájmu: skutečné výdaje (vč. odpisů) vs. 30% paušál, daň 15 %.
     * income — roční nájemné, expenses — skutečné provozní výdaje, depreciation — roční odpisy.
     */
    fun calculate(income: Double, expenses: Double, depreciation: Double): RentalTaxResult {
        val actualDed = minOf(expenses + depreciation, income)
        val flatDed = income * 0.30
        val taxActual = maxOf(income - actualDed, 0.0) * 0.15
        val taxFlat = maxOf(income - flatDed, 0.0) * 0.15
        return RentalTaxResult(
            grossIncome = income,
            actualDeduction = actualDed,
            flatDeduction = flatDed,
            taxableActual = maxOf(income - actualDed, 0.0),
            taxableFlat = maxOf(income - flatDed, 0.0),
            taxActual = taxActual,
            taxFlat = taxFlat,
            betterMode = if (taxActual <= taxFlat) "skutečné výdaje" else "30% paušál",
            savings = abs(taxActual - taxFlat),
        )
    }
}

/** Výsledek zdanění prodeje nemovitosti. */
data class SaleTaxResult(
    val gain: Double,                // prodejní zisk (Kč)
    val exempt: Boolean,             // je osvobozen od daně?
    val tax: Double,                 // daň 15 % z nezdanitelného zisku
    val reason: String,              // důvod osvobození / zdanění
)

object RealEstateSaleTaxCalculator {
    /**
     * Zdanění prodeje nemovitosti (časový test 5 let, bytová potřeba).
     * purchasePrice — cena pořízení + náklady, salePrice — prodejní cena,
     * yearsHeld — roky držby, residentialNeed — splněna bytová potřeba / dobytí věku 60.
     */
    fun calculate(purchasePrice: Double, salePrice: Double, yearsHeld: Int, residentialNeed: Boolean): SaleTaxResult {
        val gain = maxOf(salePrice - purchasePrice, 0.0)
        return if (residentialNeed || yearsHeld >= 5) {
            SaleTaxResult(gain, true, 0.0, "Osvobozeno — ${if (residentialNeed) "bytová potřeba / věk 60" else "časový test 5 let"}")
        } else {
            val tax = gain * 0.15
            SaleTaxResult(gain, false, tax, "Zdaněno 15 % — nedodán časový test ani bytová potřeba")
        }
    }
}

/** Výsledek výpočtu nájemného, kauce a valorizace o inflaci. */
data class RentSettingResult(
    val suggestedRent: Double,       // doporučené měsíční nájemné (Kč)
    val depositMonths: Int,          // kauce v měsících
    val depositAmount: Double,       // částka kauce
    val rentAfterInflation: Double,  // nájem po valorizaci o inflaci za zvolený počet let
    val annualIncreasePct: Double,   // roční nárůst (inflace)
)

object RentSettingCalculator {
    /**
     * Výše nájemného a kauce + valorizace o inflaci.
     * rent — aktuální měsíční nájemné, depositMonths — kauce v měsících, inflationPct — roční inflace, years — let.
     */
    fun calculate(rent: Double, depositMonths: Int, inflationPct: Double, years: Int): RentSettingResult {
        val after = rent * (1.0 + inflationPct / 100.0).pow(years.toDouble())
        return RentSettingResult(
            suggestedRent = rent,
            depositMonths = depositMonths,
            depositAmount = rent * depositMonths,
            rentAfterInflation = after,
            annualIncreasePct = inflationPct,
        )
    }
}
