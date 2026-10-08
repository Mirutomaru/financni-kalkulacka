package com.mirda.financnikalkulacky.calculators.work

import com.mirda.financnikalkulacky.calculators.Legislation
import com.mirda.financnikalkulacky.calculators.YearParams

/** Výsledek výpočtu mateřské (PPM) a rodičovského příspěvku. */
data class MaternityResult(
    val dailyBaseReduced: Double,   // redukovaný DVZ
    val ppmMonthly: Double,         // měsíční PPM (70 % DVZ)
    val ppmTotal28w: Double,        // celkem za 28 týdnů
    val parentalBenefitMonthly: Double, // měsíční rodičovský příspěvek
    val totalUntilAge: Double,      // celkem do zvoleného věku dítěte
)

object MaternityCalculator {
    /** Redukovaný DVZ (stejné hranice jako nemocenská, dle roku). */
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
     * @param grossMonthly hrubá měsíční mzda matky/otce (Kč)
     * @param childAgeYears věk dítěte, do kterého se má rodičovská čerpat (2/3/4)
     * @param totalParentalBenefit celková státem stanovená částka rodičovského příspěvku na dítě (Kč)
     * @param p roční parametry (redukční hranice) — výchozí aktuální rok
     */
    fun calculate(grossMonthly: Double, totalParentalBenefit: Double, childAgeYears: Int, p: YearParams = YearParams.YEAR_2026): MaternityResult {
        val dailyRaw = grossMonthly / 30.0
        val dailyReduced = reduceDailyBase(dailyRaw, p)

        // PPM — 70 % redukovaného DVZ za každý kalendářní den, 28 týdnů (196 dní)
        val ppmTotal28w = dailyReduced * 0.70 * 196.0
        val ppmMonthly = ppmTotal28w / 6.5

        // Rodičovský příspěvek: celková částka rozložená na měsíce do zvoleného věku,
        // omezeno maximální měsíční hranicí (výše PPM)
        val months = (childAgeYears.coerceIn(1, 4) * 12).coerceAtLeast(1)
        val rawMonthly = totalParentalBenefit / months
        // Maximální měsíční hranice odvozená z PPM
        val maxMonthly = ppmMonthly
        val parentalBenefitMonthly = minOf(rawMonthly, maxMonthly)

        // Celkem do zvoleného věku: PPM (28 týdnů) + rodičovský příspěvek na zbývající měsíce
        val ppmMonths = 6.5
        val remainingMonths = maxOf(0, months - ppmMonths.toInt())
        val totalUntilAge = ppmTotal28w + parentalBenefitMonthly * remainingMonths

        return MaternityResult(
            dailyBaseReduced = dailyReduced,
            ppmMonthly = ppmMonthly,
            ppmTotal28w = ppmTotal28w,
            parentalBenefitMonthly = parentalBenefitMonthly,
            totalUntilAge = totalUntilAge,
        )
    }
}
