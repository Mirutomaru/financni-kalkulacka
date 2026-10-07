package com.mirda.financnikalkulacky.calculators.family

import com.mirda.financnikalkulacky.calculators.Legislation

// ─────────────────────────────────────────────────────────────
// KONEC ŽIVOTA — 5 kalkulaček
// ─────────────────────────────────────────────────────────────

/** Výsledek rozpočtu pohřbu. */
data class FuneralCostResult(
    val totalCost: Double,
    val stateBenefit: Double,
    val familyBurden: Double,
)

object FuneralCostCalculator {
    /** Rozpočet pohřbu a zbytek po státním pohřebném. */
    fun calculate(coffin: Double, ceremonyFee: Double, flowers: Double, other: Double): FuneralCostResult {
        val total = coffin + ceremonyFee + flowers + other
        return FuneralCostResult(
            totalCost = total,
            stateBenefit = Legislation.POHRBENE_BENEFIT,
            familyBurden = maxOf(total - Legislation.POHRBENE_BENEFIT, 0.0),
        )
    }
}

/** Výsledek státního pohřebného. */
data class BereavementBenefitResult(
    val benefit: Double,
    val coveragePct: Double,
    val remainingCost: Double,
)

object BereavementBenefitCalculator {
    /** Kolik % nákladů pokryje státní pohřebné (15 000 Kč). */
    fun calculate(funeralCost: Double): BereavementBenefitResult {
        val benefit = Legislation.POHRBENE_BENEFIT
        val coverage = if (funeralCost > 0) minOf(benefit / funeralCost * 100.0, 100.0) else 100.0
        return BereavementBenefitResult(
            benefit = benefit,
            coveragePct = coverage,
            remainingCost = maxOf(funeralCost - benefit, 0.0),
        )
    }
}

/** Výsledek nákladů dědického řízení. */
data class InheritanceProceedingsResult(
    val notaryFee: Double,
    val registryFees: Double,
    val totalCost: Double,
    val costPctOfEstate: Double,
)

object InheritanceProceedingsCalculator {
    /** Odhad nákladů dědického řízení (notář + katastr). */
    fun calculate(estateValue: Double, propertiesCount: Int): InheritanceProceedingsResult {
        // notářský poplatek za osvědčení o dědictví: 3 000 Kč + 0,5 % z hodnoty, strop 200 000 Kč
        val notary = minOf(3_000.0 + estateValue * 0.005, 200_000.0)
        // katastrální poplatek za vklad dědictví: 300 Kč na nemovitost
        val registry = propertiesCount.coerceAtLeast(0) * 300.0
        val total = notary + registry
        return InheritanceProceedingsResult(
            notaryFee = notary,
            registryFees = registry,
            totalCost = total,
            costPctOfEstate = if (estateValue > 0) total / estateValue * 100.0 else 0.0,
        )
    }
}

/** Výsledek povinného dílu ze závěti. */
data class ForcedShareResult(
    val statutorySharePerHeir: Double,
    val forcedSharePerHeir: Double,
    val totalForcedClaim: Double,
)

object ForcedShareCalculator {
    /**
     * Povinný díl = polovina náležitého podílu (manželé a děti).
     * Náležitý podíl = majetek / počet zákonných dědiců.
     */
    fun calculate(estateValue: Double, legalHeirsCount: Int): ForcedShareResult {
        val n = legalHeirsCount.coerceAtLeast(1)
        val statutory = estateValue / n
        return ForcedShareResult(
            statutorySharePerHeir = statutory,
            forcedSharePerHeir = statutory * 0.5,
            totalForcedClaim = statutory * 0.5 * n,
        )
    }
}

/** Výsledek srovnání daru a dědictví. */
data class GiftVsInheritanceResult(
    val giftNet: Double,
    val inheritanceNet: Double,
    val betterOption: String,
)

object GiftVsInheritanceCalculator {
    /**
     * Dar: daň 15 % (osvobození pro nejbližší do 8 000 Kč za 5 let).
     * Dědictví: bez daně, ale náklady řízení (notář + katastr) se odečtou.
     */
    fun calculate(
        assetValue: Double,
        isCloseRelative: Boolean,
        propertiesCount: Int,
    ): GiftVsInheritanceResult {
        // dar — daň 15 %, osvobození 8 000 Kč (nejbližší)
        val giftTaxBase = if (isCloseRelative) maxOf(assetValue - 8_000.0, 0.0) else assetValue
        val giftNet = assetValue - giftTaxBase * 0.15

        // dědictví — náklady řízení na hodnotě majetku
        val proceedings = InheritanceProceedingsCalculator.calculate(assetValue, propertiesCount).totalCost
        val inheritanceNet = assetValue - proceedings

        return GiftVsInheritanceResult(
            giftNet = giftNet,
            inheritanceNet = inheritanceNet,
            betterOption = if (giftNet >= inheritanceNet) "dar" else "dědictví",
        )
    }
}
