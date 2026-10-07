package com.mirda.financnikalkulacky.calculators.family

import kotlin.math.ceil

// ─────────────────────────────────────────────────────────────
// RODINNÉ A ŽIVOTNÍ UDÁLOSTI — 7 kalkulaček
// ─────────────────────────────────────────────────────────────

/** Výsledek rozpočtu svatby. */
data class WeddingResult(
    val totalCost: Double,
    val costPerGuest: Double,
    val monthlySaving: Double,
)

object WeddingCalculator {
    /** Rozpočet svatby + měsíční spoření na cíl v daném čase. */
    fun calculate(budget: Double, guests: Int, monthsToSave: Int): WeddingResult {
        val g = guests.coerceAtLeast(1)
        return WeddingResult(
            totalCost = budget,
            costPerGuest = budget / g,
            monthlySaving = if (monthsToSave > 0) budget / monthsToSave else 0.0,
        )
    }
}

/** Výsledek nákladů na studium. */
data class StudyCostResult(
    val annualTuition: Double,
    val annualLiving: Double,
    val totalStudyCost: Double,
    val monthlySavingNeeded: Double,
)

object StudyCostCalculator {
    /** Roční a celkové náklady na studium + měsíční spoření předem. */
    fun calculate(
        tuitionMonthly: Double,
        livingMonthly: Double,
        yearsOfStudy: Int,
        monthsToSaveInAdvance: Int,
    ): StudyCostResult {
        val y = yearsOfStudy.coerceAtLeast(1)
        val annualTuition = tuitionMonthly * 12.0
        val annualLiving = livingMonthly * 12.0
        val total = (annualTuition + annualLiving) * y
        return StudyCostResult(
            annualTuition = annualTuition,
            annualLiving = annualLiving,
            totalStudyCost = total,
            monthlySavingNeeded = if (monthsToSaveInAdvance > 0) total / monthsToSaveInAdvance else 0.0,
        )
    }
}

/** Výsledek rozdělení majetku při rozvodu. */
data class DivorceSplitResult(
    val netEstate: Double,
    val shareA: Double,
    val shareB: Double,
)

object DivorcePropertySplitCalculator {
    /** Rozdělí čistý společný majetek (aktiva − dluhy) podle podílu. */
    fun calculate(assets: Double, debts: Double, partnerSharePct: Double): DivorceSplitResult {
        val net = assets - debts
        // podíl druhého 0–100 %, zbytek připadá prvnímu
        val p = (partnerSharePct / 100.0).coerceIn(0.0, 1.0)
        return DivorceSplitResult(netEstate = net, shareA = net * (1 - p), shareB = net * p)
    }
}

/** Výsledek výživného na děti. */
data class ChildSupportResult(
    val fraction: Double,
    val calculatedAmount: Double,
    val minimumAmount: Double,
    val finalAmount: Double,
)

object ChildSupportCalculator {
    /**
     * Výživné: 1/7 čistého příjmu (1 dítě), 1/6 (2 děti), 1/5 (3+).
     * Minimum = 25 % / 33 % / 40 % průměrné mzdy.
     */
    fun calculate(netIncome: Double, children: Int, avgWage: Double): ChildSupportResult {
        val c = children.coerceAtLeast(1)
        val fraction = when (c) {
            1 -> 1.0 / 7.0
            2 -> 1.0 / 6.0
            else -> 1.0 / 5.0
        }
        val minFraction = when (c) {
            1 -> 0.25
            2 -> 0.33
            else -> 0.40
        }
        val calculated = netIncome * fraction
        val minimum = avgWage * minFraction
        return ChildSupportResult(
            fraction = fraction,
            calculatedAmount = calculated,
            minimumAmount = minimum,
            finalAmount = maxOf(calculated, minimum),
        )
    }
}

/** Výsledek nákladů na miminko. */
data class BabyCostResult(
    val oneTimeCost: Double,
    val monthlyCost: Double,
    val firstYearTotal: Double,
    val stateBenefit: Double,
)

object BabyCostCalculator {
    /** Jednorázové + měsíční náklady na miminko a státní porodné. */
    fun calculate(
        oneTimeCost: Double,
        monthlyCost: Double,
        isSecondOrMoreChild: Boolean,
    ): BabyCostResult {
        val benefit = if (isSecondOrMoreChild) 30_000.0 else 13_000.0 // porodné 2026
        return BabyCostResult(
            oneTimeCost = oneTimeCost,
            monthlyCost = monthlyCost,
            firstYearTotal = oneTimeCost + monthlyCost * 12.0,
            stateBenefit = benefit,
        )
    }
}

/** Výsledek rozpočtu dovolené. */
data class VacationTripResult(
    val totalCost: Double,
    val costPerPerson: Double,
    val monthlySaving: Double,
)

object VacationTripCalculator {
    /** Celková cena cesty na osobu a měsíční spoření předem. */
    fun calculate(totalBudget: Double, people: Int, monthsToSave: Int): VacationTripResult {
        val p = people.coerceAtLeast(1)
        return VacationTripResult(
            totalCost = totalBudget,
            costPerPerson = totalBudget / p,
            monthlySaving = if (monthsToSave > 0) totalBudget / monthsToSave else 0.0,
        )
    }
}

/** Výsledek nákladů na péči o seniora. */
data class SeniorCareResult(
    val homeMonthly: Double,
    val facilityMonthly: Double,
    val annualDifference: Double,
    val cheaperOption: String,
)

object SeniorCareCalculator {
    /** Srovnání domácí péče vs. pečovatelský domov za rok. */
    fun calculate(
        homeCareHourly: Double,
        careHoursPerDay: Int,
        facilityMonthlyFee: Double,
    ): SeniorCareResult {
        val homeMonthly = homeCareHourly * careHoursPerDay * 30.42 // průměrný měsíc
        val annualDiff = (facilityMonthlyFee - homeMonthly) * 12.0
        return SeniorCareResult(
            homeMonthly = homeMonthly,
            facilityMonthly = facilityMonthlyFee,
            annualDifference = annualDiff,
            cheaperOption = if (homeMonthly <= facilityMonthlyFee) "domácí péče" else "pečovatelský domov",
        )
    }
}
