package com.mirda.financnikalkulacky.calculators.investments

import kotlin.math.pow

/** Výsledek srovnání jednorázového vkladu vs. pravidelného DCA. */
data class LumpSumVsDcaResult(
    val lumpSumFinal: Double,      // finální částka — jednorázový vklad
    val dcaFinal: Double,          // finální částka — pravidelné měsíční vklady (celkem stejně)
    val difference: Double,        // rozdíl ve prospěch lepšího scénáře
    val betterMode: String,        // "jednorázově" | "pravidelně"
)

object LumpSumVsDcaCalculator {
    /** Stejný celkový kapitál investovaný jednorázově vs. rozložený do měsíců (DCA). */
    fun calculate(totalAmount: Double, years: Int, annualRatePct: Double): LumpSumVsDcaResult {
        val m = (years * 12).coerceAtLeast(1)
        val r = annualRatePct / 100.0 / 12.0
        // Jednorázově: celý kapitál zhodnocen po celou dobu
        val lumpSumFinal = totalAmount * (1.0 + r).pow(m.toDouble())
        // DCA: měsíční vklad = totalAmount / m, anuita
        val pmt = totalAmount / m
        val dcaFactor = if (r > 0.0) ((1.0 + r).pow(m.toDouble()) - 1.0) / r else m.toDouble()
        val dcaFinal = pmt * dcaFactor
        return LumpSumVsDcaResult(
            lumpSumFinal, dcaFinal,
            kotlin.math.abs(lumpSumFinal - dcaFinal),
            if (lumpSumFinal >= dcaFinal) "jednorázově" else "pravidelně",
        )
    }
}

/** Výsledek vlivu poplatků na výnos investice. */
data class FeeImpactResult(
    val finalNoFees: Double,       // finální částka bez poplatků
    val finalWithFees: Double,     // finální částka s ročním poplatkem TER
    val lostToFees: Double,        // ztráta na poplatcích
    val effectiveReturnPct: Double, // efektivní roční výnos po poplatku v %
)

object FeeImpactCalculator {
    /** Roční poplatek (TER) snižuje efektivní výnos; efekt je složený. */
    fun calculate(amount: Double, annualRatePct: Double, terPct: Double, years: Int): FeeImpactResult {
        val finalNoFees = amount * (1.0 + annualRatePct / 100.0).pow(years.toDouble())
        // Efektivní výnos po TER: (1+rate)(1-ter) - 1
        val netRate = (1.0 + annualRatePct / 100.0) * (1.0 - terPct / 100.0) - 1.0
        val finalWithFees = amount * (1.0 + netRate).pow(years.toDouble())
        val effectiveReturnPct = if (amount > 0.0 && years > 0) ((finalWithFees / amount).pow(1.0 / years) - 1.0) * 100.0 else 0.0
        return FeeImpactResult(finalNoFees, finalWithFees, finalNoFees - finalWithFees, effectiveReturnPct)
    }
}

/** Výsledek zdanění investic (časový test + limit osvobození). */
data class InvestmentTaxResult(
    val gain: Double,              // zisk ze prodeje
    val taxableGain: Double,       // daňový základ po odepsání nákladů a limitu
    val tax: Double,               // daň 15 %
    val netProfit: Double,         // čistý zisk po dani
)

object InvestmentTaxCalculator {
    const val TAX_FREE_LIMIT = 30_000.0   // roční limit osvobození od daně (Kč)

    /** purchaseCost: nákupní cena; salePrice: prodejní cena; yearsHeld: držba v letech; otherGainsThisYear: ostatní zisky za rok. */
    fun calculate(purchaseCost: Double, salePrice: Double, yearsHeld: Int, otherGainsThisYear: Double): InvestmentTaxResult {
        val gain = (salePrice - purchaseCost).coerceAtLeast(0.0)
        // Časový test 3 roky osvobozuje jen při držbě > 3 let a prodeji do 1 roku od splatnosti — orientačně: > 3 let = osvobozeno
        if (yearsHeld > 3) return InvestmentTaxResult(gain, 0.0, 0.0, gain)
        // Limit osvobození 30 tis/rok se snižuje o ostatní zisky
        val remainingLimit = (TAX_FREE_LIMIT - otherGainsThisYear).coerceAtLeast(0.0)
        val taxableGain = (gain - remainingLimit).coerceAtLeast(0.0)
        val tax = taxableGain * 0.15
        return InvestmentTaxResult(gain, taxableGain, tax, gain - tax)
    }
}

/** Výsledek zdanění kryptoměn. */
data class CryptoTaxResult(
    val totalGain: Double,         // celkový zisk/ztráta (Kč)
    val taxableGain: Double,       // daňový základ po limitu 30 tis
    val tax: Double,               // daň 15 %
    val netProfit: Double,         // čistý zisk
)

object CryptoTaxCalculator {
    const val TAX_FREE_LIMIT = 30_000.0   // stejný roční limit jako u investic

    /** purchases: celkové náklady na koupi; sales: výtěžek z prodeje (Kč). */
    fun calculate(purchases: Double, sales: Double): CryptoTaxResult {
        val gain = (sales - purchases).coerceAtLeast(0.0)
        val taxableGain = (gain - TAX_FREE_LIMIT).coerceAtLeast(0.0)
        val tax = taxableGain * 0.15
        return CryptoTaxResult(gain, taxableGain, tax, gain - tax)
    }
}

/** Výsledek výnosu dluhopisu do splatnosti (YTM). */
data class BondYtmResult(
    val ytmPct: Double,            // roční výnos do splatnosti v %
    val totalReturnPct: Double,    // celkový návrat za dobu držby v %
    val annualCoupon: Double,      // roční kupón (Kč)
)

object BondYtmCalculator {
    /** price: cena dluhopisu; faceValue: jmenovitá hodnota; couponPct: kupónová sazba; yearsToMaturity. */
    fun calculate(price: Double, faceValue: Double, couponPct: Double, yearsToMaturity: Int): BondYtmResult {
        val n = yearsToMaturity.coerceAtLeast(1)
        val annualCoupon = faceValue * couponPct / 100.0
        // Přibližný YTM (standardní aproximace)
        val ytmApprox = ((annualCoupon + (faceValue - price) / n) / ((faceValue + price) / 2.0)) * 100.0
        // Celkový návrat: kupóny + splacení jmenovité hodnoty vs cena
        val totalReturnPct = if (price > 0.0) ((annualCoupon * n + faceValue - price) / price * 100.0) else 0.0
        return BondYtmResult(ytmApprox, totalReturnPct, annualCoupon)
    }
}

/** Výsledek přepočtu měn a kurzového rizika. */
data class CurrencyConversionResult(
    val convertedAmount: Double,   // částka v cílové měně
    val futureValueAtRate: Double, // hodnota po změně kurzu (Kč)
    val currencyRiskPct: Double,   // ztráta/zisk na kurzu v %
)

object CurrencyConversionCalculator {
    /** amount: částka v cizí měně; rateNow: aktuální kurz (Kč za 1 jednotku); rateChangePct: očekávaná změna kurzu. */
    fun calculate(amount: Double, rateNow: Double, rateChangePct: Double): CurrencyConversionResult {
        val converted = amount * rateNow
        val newRate = rateNow * (1.0 + rateChangePct / 100.0)
        val futureValue = amount * newRate
        return CurrencyConversionResult(converted, futureValue, rateChangePct)
    }
}
