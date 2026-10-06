package com.mirda.financnikalkulacky.calculators.housing

import kotlin.math.pow

/** Výsledek srovnání koupě a nájmu po horizontu. */
data class BuyVsRentResult(
    val buyNetWorth: Double,     // čisté jmění scénář KOUPE po N letech
    val rentNetWorth: Double,    // čisté jmění scénář NÁJEM po N letech
    val crossoverMonth: Int?,    // měsíc, kdy koupě začne vést (null = nevede)
)

object BuyVsRentCalculator {
    /**
     * Měsíční simulace čistého jmění obou scénářů s reinvesticí rozdílu cashflow.
     * Koupě: vlastnictví roste o propertyGrowthPct, hypotéka se splácí anuitně; rozpočet B = A + provoz.
     * Nájem: vlastní zdroje investovány za investmentReturnPct + měsíční úspora (B − nájem).
     */
    fun calculate(
        price: Double, ownFunds: Double, mortgageRatePct: Double, mortgageYears: Int,
        buyOpexMonthly: Double, propertyGrowthPct: Double,
        rentMonthly: Double, rentGrowthPct: Double, investmentReturnPct: Double, horizonYears: Int,
    ): BuyVsRentResult {
        val months = (horizonYears * 12).coerceAtLeast(1)
        val propGrowthM = propertyGrowthPct / 100.0 / 12.0
        val invReturnM = investmentReturnPct / 100.0 / 12.0
        val rentGrowthM = rentGrowthPct / 100.0 / 12.0

        // Koupě: hodnota nemovitosti a zůstatek hypotéky
        var propertyValue = price
        var loanBalance = (price - ownFunds).coerceAtLeast(0.0)
        val rM = mortgageRatePct / 100.0 / 12.0
        val mTotal = (mortgageYears * 12).coerceAtLeast(1)
        val factor = if (rM > 0) (1.0 + rM).pow(mTotal.toDouble()) else 1.0
        val annuity = if (loanBalance <= 0.0) 0.0
            else if (rM == 0.0) loanBalance / mTotal
            else loanBalance * rM * factor / (factor - 1.0)

        // Rozpočet: nájem srovnatelného bytu + provoz = maximální měsíční rozpočet na bydlení
        val budget = rentMonthly + buyOpexMonthly

        var buyInvested = ownFunds.coerceAtLeast(0.0) * 0.0   // vlastní zdroje jdou do nemovitosti
        var rentInvested = ownFunds.coerceAtLeast(0.0)
        var currentRent = rentMonthly
        var crossover: Int? = null

        for (month in 1..months) {
            propertyValue *= (1.0 + propGrowthM)
            if (loanBalance > 0 && month <= mTotal) {
                val interestPart = loanBalance * rM
                loanBalance = (loanBalance + annuity - interestPart).coerceAtLeast(0.0)
    }
            // Koupě: rozpočet = nájem + provoz; přebytek nad splátkou+provoz investujeme
            val buySurplus = rentMonthly - annuity
            buyInvested = buyInvested * (1.0 + invReturnM) + buySurplus.coerceAtLeast(0.0)
            // Nájem: nájemné roste; přebytek rozpočtu investujeme
            currentRent *= (1.0 + rentGrowthM)
            val rentSurplus = rentMonthly + buyOpexMonthly - currentRent
            rentInvested = rentInvested * (1.0 + invReturnM) + rentSurplus.coerceAtLeast(0.0)

            val buyNetWorth = propertyValue - loanBalance + buyInvested
            if (crossover == null && buyNetWorth > rentInvested) crossover = month
        }
        val finalBuyNetWorth = propertyValue - loanBalance + buyInvested
        return BuyVsRentResult(finalBuyNetWorth, rentInvested, crossover)
    }
}
