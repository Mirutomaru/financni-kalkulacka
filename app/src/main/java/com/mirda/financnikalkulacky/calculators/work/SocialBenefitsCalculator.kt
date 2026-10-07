package com.mirda.financnikalkulacky.calculators.work

/** Výsledek otcovské poporodní péče. */
data class PaternityResult(
    val weeklyAmount: Double,
    val totalTwoWeeks: Double,
)

/** Výsledek ošetřovného. */
data class CareAllowanceResult(
    val dailyBaseReduced: Double,
    val ratePct: Double,
    val dailyBenefit: Double,
    val totalForDays: Double,
)

/** Výsledek invalidního důchodu. */
data class DisabilityPensionResult(
    val degree: Int,
    val pensionMonthly: Double,
    val annualTotal: Double,
)

/** Výsledek pozůstalostních důchodů. */
data class SurvivorPensionResult(
    val widowWidowerMonthly: Double, // 50 % pojištěného důchodu (min. 12 tis.)
    val orphanMonthly: Double,       // 40 % pojištěného důchodu (min. 9 600 Kč)
    val totalHouseholdMonthly: Double,
)

/** Výsledek přídavků a příspěvků na dítě. */
data class ChildBenefitsResult(
    val childAllowanceMonthly: Double,   // přídavek na dítě (dle věku a příjmu)
    val careContributionMonthly: Double, // příspěvek na péči (do 7 let vyšší)
    val housingBenefitMonthly: Double,   // příspěvek na bydlení (orientačně)
    val totalMonthly: Double,
)

object PaternityCalculator {
    /**
     * Otcovská poporodní péče: 2 týdny, 70 % denního vyměřovacího základu.
     */
    fun calculate(grossMonthly: Double): PaternityResult {
        val reduced = SickLeaveCalculator.reduceDailyBase(grossMonthly / 30.0)
        val dailyBenefit = reduced * 0.70
        return PaternityResult(dailyBenefit * 7, dailyBenefit * 14)
    }
}

object CareAllowanceCalculator {
    /**
     * Ošetřovné: péče o dítě do 10 let (nebo 15 let při chronickém onemocnění).
     * Prvních 9 dní: 60 % DVZ, od 10. dne: 70 % DVZ.
     */
    fun calculate(grossMonthly: Double, days: Int): CareAllowanceResult {
        val reduced = SickLeaveCalculator.reduceDailyBase(grossMonthly / 30.0)
        val firstPart = minOf(days, 9).toDouble() * reduced * 0.60
        val secondPart = maxOf(days - 9, 0).toDouble() * reduced * 0.70
        val total = firstPart + secondPart
        val avgRate = if (days > 0) total / days / reduced else 0.0
        return CareAllowanceResult(reduced, avgRate * 100.0, total / maxOf(days, 1), total)
    }
}

object DisabilityPensionCalculator {
    /**
     * Invalidní důchod: I. stupeň 40 %, II. stupeň 50 %, III. stupeň 80 % průměrného výdělku.
     * Minimální výše (2026 orientačně): I. 12 300, II. 15 400, III. 24 600 Kč/měs.
     */
    fun calculate(averageEarning: Double, degree: Int = 2): DisabilityPensionResult {
        val rate = when (degree) {
            1 -> 0.40
            3 -> 0.80
            else -> 0.50
        }
        val minAmount = when (degree) {
            1 -> 12_300.0
            3 -> 24_600.0
            else -> 15_400.0
        }
        val pension = maxOf(averageEarning * rate, minAmount)
        return DisabilityPensionResult(degree, pension, pension * 12)
    }
}

object SurvivorPensionCalculator {
    /**
     * Pozůstalostní důchody (orientačně):
     * - vdova/vdovec: 50 % pojištěného důchodu (min. 12 tis.)
     * - sirotek: 40 % pojištěného důchodu (min. 9 600 Kč)
     */
    fun calculate(deceasedPension: Double, hasWidow: Boolean = true, orphans: Int = 0): SurvivorPensionResult {
        val widow = if (hasWidow) maxOf(deceasedPension * 0.50, 12_000.0) else 0.0
        val orphanEach = if (orphans > 0) maxOf(deceasedPension * 0.40, 9_600.0) else 0.0
        return SurvivorPensionResult(widow, orphanEach, widow + orphanEach * orphans)
    }
}

object ChildBenefitsCalculator {
    /**
     * Přídavek na dítě a příspěvek na péči (2026 orientačně):
     * - přídavek: 350 Kč/měs. (dítě do 18 let), při nízkém příjmu vyšší
     * - příspěvek na péči: 1 400 Kč/měs. (do 7 let), 950 Kč (7–15 let)
     * - příspěvek na bydlení: orientačně podle výdajů a příjmů
     */
    fun calculate(
        childAgeYears: Int = 3,
        householdIncomeMonthly: Double = 25_000.0,
        housingCostMonthly: Double = 8_000.0,
    ): ChildBenefitsResult {
        val allowance = if (householdIncomeMonthly < 15_000) 700.0 else 350.0
        val care = if (childAgeYears <= 7) 1_400.0 else 950.0
        // příspěvek na bydlení: orientačně — pokud náklady přesahují 25 % příjmu, rozdíl
        val housingLimit = householdIncomeMonthly * 0.25
        val housingBenefit = maxOf(housingCostMonthly - housingLimit, 0.0)
        return ChildBenefitsResult(allowance, care, housingBenefit, allowance + care + housingBenefit)
    }
}
