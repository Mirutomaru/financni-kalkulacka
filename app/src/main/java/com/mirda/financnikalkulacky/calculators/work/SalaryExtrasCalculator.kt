package com.mirda.financnikalkulacky.calculators.work

import kotlin.math.ceil
import kotlin.math.pow

/** Výsledek obrácené mzdové kalkulačky. */
data class ReverseSalaryResult(
    val grossMonthly: Double,
    val netMonthly: Double,
    val socialInsurance: Double,
    val healthInsurance: Double,
    val taxAnnual: Double,
)

/** Výsledek výpočtu přesčasů a příplatků. */
data class OvertimeResult(
    val overtimeWeekendGross: Double,
    val overtimeNightGross: Double,
    val overtimeHolidayGross: Double,
    val totalExtraGross: Double,
    val netExtraMonthly: Double,
)

/** Výsledek výpočtu dovolené. */
data class VacationResult(
    val daysEntitled: Int,
    val dailyRate: Double,
    val vacationPayTotal: Double,
    val unusedDaysValue: Double,
)

/** Výsledek cestovních náhrad a stravenek. */
data class TravelExpenseResult(
    val mealAllowanceMonthly: Double,
    val travelPerKmAnnual: Double,
    val totalAnnualGross: Double,
    val netAfterTax: Double,
)

/** Výsledek valorizace mzdy. */
data class WageValorizationResult(
    val nominalGrowthPct: Double,
    val inflationPct: Double,
    val realGrowthPct: Double,
    val purchasingPowerLossPct: Double,
    val netAfterInflation: Double,
)

object ReverseSalaryCalculator {
    /**
     * Jakou hrubou mzdu potřebuji pro požadovanou čistou.
     * Iterativní aproximace (Newton/bisection).
     */
    fun calculate(targetNet: Double, children: Int = 0): ReverseSalaryResult {
        var lo = targetNet
        var hi = targetNet * 2.5
        repeat(60) {
            val mid = (lo + hi) / 2.0
            val net = SalaryCalculator.calculate(mid, children).netMonthly
            if (net < targetNet) lo = mid else hi = mid
        }
        val gross = (lo + hi) / 2.0
        val r = SalaryCalculator.calculate(gross, children)
        return ReverseSalaryResult(
            grossMonthly = gross,
            netMonthly = r.netMonthly,
            socialInsurance = r.socialInsurance,
            healthInsurance = r.healthInsurance,
            taxAnnual = r.netTaxAnnual,
        )
    }
}

object OvertimeCalculator {
    /**
     * Přesčasy a příplatky dle zákoníku práce:
     * - víkend (sobota/neděle): +100 % (tj. 2× hodinová mzda)
     * - noc (22–6): +35 %
     * - svátek: +100 %
     */
    fun calculate(
        hourlyWage: Double,
        weekendHours: Int = 4,
        nightHours: Int = 8,
        holidayHours: Int = 2,
    ): OvertimeResult {
        val weekendGross = hourlyWage * 1.0 * weekendHours   // příplatek 100 %
        val nightGross = hourlyWage * 0.35 * nightHours      // příplatek 35 %
        val holidayGross = hourlyWage * 1.0 * holidayHours   // příplatek 100 %
        val totalExtra = weekendGross + nightGross + holidayGross
        // zdanění: sociálka 7,1 %, zdravotní 4,5 %, daň ~15 % (zjednodušeně)
        val netExtra = totalExtra * (1 - 0.071 - 0.045) * (1 - 0.15)
        return OvertimeResult(weekendGross, nightGross, holidayGross, totalExtra, netExtra)
    }
}

object VacationCalculator {
    /**
     * Nárok na dovolenou dle zákoníku práce:
     * - 20 dní (minimální)
     * +1 den po 5 letech u stejného zaměstnavatele
     * +1 den po 10 letech
     * +1 den po 15 letech
     */
    fun calculate(grossMonthly: Double, yearsEmployed: Int = 1): VacationResult {
        var days = 20
        if (yearsEmployed >= 15) days += 3
        else if (yearsEmployed >= 10) days += 2
        else if (yearsEmployed >= 5) days += 1

        val dailyRate = grossMonthly / 21.75 // průměrný denní výdělek
        val vacationPayTotal = dailyRate * days
        return VacationResult(days, dailyRate, vacationPayTotal, 0.0)
    }
}

object TravelExpenseCalculator {
    /**
     * Stravenkový paušál a cestovní náhrady (2026):
     * - stravenka: 130 Kč/den (do 80 km), 150 Kč (nad 80 km) — zjednodušeně 140 Kč
     * - jízdné: 7,90 Kč/km (osobní auto)
     */
    fun calculate(
        workDaysPerMonth: Int = 21,
        kmPerMonth: Double = 300.0,
    ): TravelExpenseResult {
        val mealAllowanceMonthly = 140.0 * workDaysPerMonth
        val travelPerKmAnnual = 7.90 * kmPerMonth * 12
        val totalAnnualGross = mealAllowanceMonthly * 12 + travelPerKmAnnual
        // stravenky a jízdné jsou daňově osvobozeny do limitu
        val netAfterTax = totalAnnualGross
        return TravelExpenseResult(mealAllowanceMonthly, travelPerKmAnnual, totalAnnualGross, netAfterTax)
    }
}

object WageValorizationCalculator {
    /**
     * Valorizace mzdy: reálný růst vs. inflace.
     */
    fun calculate(
        currentNet: Double,
        nominalRaisePct: Double,
        inflationPct: Double = 3.0,
    ): WageValorizationResult {
        val newNet = currentNet * (1 + nominalRaisePct / 100.0)
        val realGrowth = ((nominalRaisePct - inflationPct) / (100 + inflationPct)) * 100.0
        val purchasingPowerLoss = if (realGrowth < 0) -realGrowth else 0.0
        return WageValorizationResult(
            nominalGrowthPct = nominalRaisePct,
            inflationPct = inflationPct,
            realGrowthPct = realGrowth,
            purchasingPowerLossPct = purchasingPowerLoss,
            netAfterInflation = newNet / (1 + inflationPct / 100.0),
        )
    }
}
