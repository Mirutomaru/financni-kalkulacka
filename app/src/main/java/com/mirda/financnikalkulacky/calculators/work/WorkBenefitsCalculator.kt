package com.mirda.financnikalkulacky.calculators.work

import com.mirda.financnikalkulacky.calculators.Legislation

/** Výsledek výpočtu odstupného. */
data class SeveranceResult(
    val multiplier: Int,            // násobek průměrného výdělku
    val grossSeverance: Double,     // hrubé odstupné
    val netSeverance: Double,       // čisté (po 15 % dani)
)

/** Výsledek podpory v nezaměstnanosti. */
data class UnemploymentResult(
    val months: Int,                // podpůrná doba (měsíce)
    val monthlyAmounts: List<Double>, // měsíční částky dle fáze
    val totalPaid: Double,          // celkem za celou dobu
)

object WorkBenefitsCalculator {
    /** Odstupné dle § 67 zákoníku práce. */
    fun severance(averageEarning: Double, yearsEmployed: Double): SeveranceResult {
        val multiplier = Legislation.severanceMultiplier(yearsEmployed)
        val gross = averageEarning * multiplier
        // Odstupné podléhá 15 % dani, ale ne odvodům SP/ZP
        val net = gross * (1.0 - Legislation.TAX_RATE_1)
        return SeveranceResult(multiplier, gross, net)
    }

    /** Podpora v nezaměstnanosti — měsíční průběh dle věku a způsobu ukončení. */
    fun unemployment(averageNetEarning: Double, age: Int, mutualAgreementNoCause: Boolean): UnemploymentResult {
        val months = Legislation.unemploymentMonths(age)
        val amounts = (1..months).map { month ->
            if (mutualAgreementNoCause) averageNetEarning * 0.45
            else when {
                month <= 2 -> averageNetEarning * 0.65
                month <= 4 -> averageNetEarning * 0.50
                else -> averageNetEarning * 0.45
            }
        }
        return UnemploymentResult(months, amounts, amounts.sum())
    }
}
