package com.mirda.financnikalkulacky.calculators.work

import com.mirda.financnikalkulacky.calculators.Legislation
import com.mirda.financnikalkulacky.calculators.YearParams

/** Výsledek výpočtu nemocenské a náhrady mzdy. */
data class SickLeaveResult(
    val dailyBaseRaw: Double,       // denní vyměřovací základ (neredukovaný)
    val dailyBaseReduced: Double,   // redukovaný DVZ podle hranic
    val wageCompensation14d: Double,// náhrada mzdy za prvních 14 dní (zaměstnavatel)
    val sickBenefitState: Double,   // nemocenská od ČSSZ (od 15. dne)
    val totalReceived: Double,      // celkem vyplacené
    val incomeLoss: Double,         // výpadek oproti běžné mzdě
)

object SickLeaveCalculator {
    /** Redukovaný denní vyměřovací základ dle 3 redukčních hranic daného roku. */
    fun reduceDailyBase(daily: Double, p: YearParams = YearParams.YEAR_2026): Double {
        val l1 = p.sickLimit1
        val l2 = p.sickLimit2
        val l3 = p.sickLimit3
        val part1 = minOf(daily, l1) * 0.9
        val part2 = (minOf(daily, l2) - l1).coerceAtLeast(0.0) * 0.6
        val part3 = (minOf(daily, l3) - l2).coerceAtLeast(0.0) * 0.3
        return part1 + part2 + part3
    }

    /**
     * @param grossMonthlyAvg průměrný hrubý měsíční příjem za předchozí 4 čtvrtletí (Kč)
     * @param sickDays celkový počet kalendářních dnů pracovní neschopnosti
     * @param p roční parametry (redukční hranice) — výchozí aktuální rok
     */
    fun calculate(grossMonthlyAvg: Double, sickDays: Int, p: YearParams = YearParams.YEAR_2026): SickLeaveResult {
        val dailyRaw = grossMonthlyAvg / 30.0
        val dailyReduced = reduceDailyBase(dailyRaw, p)

        // Prvních 14 dní — náhrada mzdy od zaměstnavatele (pracovní dny ≈ 2/3 kalendářních)
        val firstDays = minOf(sickDays, 14)
        val workingDaysFirst = (firstDays * 5.0 / 7.0).toInt()
        val wageCompensation = dailyReduced * workingDaysFirst

        // Od 15. dne — nemocenská od ČSSZ za každý kalendářní den, sazba podle délky
        var stateBenefit = 0.0
        for (day in 15..sickDays) {
            val rate = when {
                day <= 30 -> Legislation.SICK_RATE_1
                day <= 60 -> Legislation.SICK_RATE_2
                else -> Legislation.SICK_RATE_3
            }
            stateBenefit += dailyReduced * rate
        }

        val totalReceived = wageCompensation + stateBenefit
        val incomeLoss = (grossMonthlyAvg - totalReceived).coerceAtLeast(0.0)

        return SickLeaveResult(
            dailyBaseRaw = dailyRaw,
            dailyBaseReduced = dailyReduced,
            wageCompensation14d = wageCompensation,
            sickBenefitState = stateBenefit,
            totalReceived = totalReceived,
            incomeLoss = incomeLoss,
        )
    }
}
