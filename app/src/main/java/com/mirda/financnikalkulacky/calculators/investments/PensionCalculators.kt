package com.mirda.financnikalkulacky.calculators.investments

import kotlin.math.pow

/** Výsledek odhadu státního důchodu. */
data class StatePensionResult(
    val estimatedMonthly: Double,   // orientační měsíční důchod (Kč)
    val baseAmount: Double,         // základní částka důchodu
    val yearsCounted: Double,       // započítané roky odpracování
)

object StatePensionCalculator {
    const val BASE_PENSION_2026 = 5_984.0   // základní částka starobního důchodu (orientační 2026)
    const val INCOME_CAP = 135_000.0        // strop průměrného měsíčního příjmu pro výpočet

    /** avgMonthlyIncome: průměrný hrubý měsíční příjem; yearsWorked: roky odpracování (max ~40). */
    fun calculate(avgMonthlyIncome: Double, yearsWorked: Int): StatePensionResult {
        val income = minOf(avgMonthlyIncome, INCOME_CAP)
        // Orientační vzorec: základ + přirážka za každý rok odpracování (~1,3 % stropu příjmu/rok)
        val years = yearsWorked.coerceIn(0, 45).toDouble()
        val additional = years * (INCOME_CAP / 100.0) * 0.013
        // Přirážka za výši příjmů nad průměr (zjednodušeně: podíl příjmu na stropu × základ)
        val incomeFactor = if (income > 0.0) (income / INCOME_CAP) * BASE_PENSION_2026 * 0.5 else 0.0
        return StatePensionResult(BASE_PENSION_2026 + additional + incomeFactor, BASE_PENSION_2026, years)
    }
}

/** Výsledek výpočtu věku odchodu do důchodu. */
data class RetirementAgeResult(
    val statutoryAge: Double,       // zákonný věk odchodu (roky + měsíce jako desetinné)
    val ageWithChildren: Double,    // věk po zohlednění vychovaných dětí
    val monthsReduced: Int,         // o kolik měsíců se věk snižuje
)

object RetirementAgeCalculator {
    /** birthYear: rok narození; childrenRaised: počet vychovaných dětí (do 4 let). */
    fun calculate(birthYear: Int, childrenRaised: Int): RetirementAgeResult {
        // Zákonný věk: narození do konce 1967 → 65 (muži) / 60–62 (ženy); od 1968 → 63 + měsíce za děti
        val base = if (birthYear <= 1967) 65.0 else 63.0
        // Za každé vychované dítě se věk snižuje o 12 měsíců (max 4 roky)
        val monthsReduced = childrenRaised.coerceIn(0, 4) * 12
        return RetirementAgeResult(base, base - monthsReduced / 12.0, monthsReduced)
    }
}

/** Výsledek předčasného/odloženého důchodu. */
data class EarlyLatePensionResult(
    val earlyMonthly: Double,       // důchod při předčasném odchodu (zkrácený)
    val reductionPct: Double,       // o kolik % se důchod krátí
    val lateMonthly: Double,        // důchod při odloženém odchodu (navýšený)
    val increasePct: Double,        // o kolik % se důchod navyšuje
)

object EarlyLatePensionCalculator {
    /** baseMonthly: výše důchodu v zákonném věku; earlyYears: roky předčasně; lateYears: roky odloženě. */
    fun calculate(baseMonthly: Double, earlyYears: Int, lateYears: Int): EarlyLatePensionResult {
        // Předčasný důchod: zkrácení ~6,4 % za každý rok (max 3 roky)
        val reductionPct = minOf(earlyYears.coerceIn(0, 3), 3).toDouble() * 6.4
        val earlyMonthly = baseMonthly * (1.0 - reductionPct / 100.0)
        // Odložený důchod: navýšení ~7,2 % za každý rok odložení
        val increasePct = lateYears.coerceIn(0, 10).toDouble() * 7.2
        val lateMonthly = baseMonthly * (1.0 + increasePct / 100.0)
        return EarlyLatePensionResult(earlyMonthly, reductionPct, lateMonthly, increasePct)
    }
}


/** Výsledek důchodové mezer
 */
data class PensionGapResult(
    val desiredMonthly: Double,     // požadovaný měsíční příjem v důchodu
    val statePensionEstimate: Double, // odhad státního důchodu
    val monthlyGap: Double,         // mezera (Kč/měs.)
    val requiredPortfolio: Double,  // portfolio pro pokrytí mezer (pravidlo 4 %)
)

object PensionGapCalculator {
    /** desiredMonthly: požadovaný příjem; avgIncome: průměrný hrubý příjem; yearsWorked. */
    fun calculate(desiredMonthly: Double, avgIncome: Double, yearsWorked: Int): PensionGapResult {
        val state = StatePensionCalculator.calculate(avgIncome, yearsWorked).estimatedMonthly
        val gap = (desiredMonthly - state).coerceAtLeast(0.0)
        // Pravidlo 4 %: portfolio = mezera × 12 / 0.04
        val requiredPortfolio = if (gap > 0.0) gap * 12.0 / 0.04 else 0.0
        return PensionGapResult(desiredMonthly, state, gap, requiredPortfolio)
    }
}

/** Výsledek renty — jak dlouho vydrží naspořené peníze. */
data class AnnuityDurationResult(
    val durationYears: Double,      // kolik let vydrží (při zhodnocení a výběru)
    val totalWithdrawn: Double,     // celkem vybráno do vyčerpání
)

object AnnuityCalculator {
    /** portfolio: naspořené; withdrawalMonthly: měsíční výběr; annualReturnPct: roční výnos. */
    fun calculate(portfolio: Double, withdrawalMonthly: Double, annualReturnPct: Double): AnnuityDurationResult {
        var balance = portfolio
        var months = 0
        val r = annualReturnPct / 100.0 / 12.0
        while (balance > 0.0 && months < 600) {
            balance = balance * (1.0 + r) - withdrawalMonthly
            months++
        }
        return AnnuityDurationResult(months / 12.0, minOf(portfolio, withdrawalMonthly * months))
    }
}
