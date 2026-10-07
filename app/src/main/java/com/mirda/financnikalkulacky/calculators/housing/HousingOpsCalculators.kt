package com.mirda.financnikalkulacky.calculators.housing

/** Výsledek nákladů na energie v domácnosti. */
data class EnergyCostResult(
    val electricityAnnual: Double,   // roční elektřina (Kč)
    val gasAnnual: Double,           // roční plyn (Kč)
    val heatingAnnual: Double,       // roční topení (Kč)
    val waterAnnual: Double,         // roční voda + stočení (Kč)
    val totalAnnual: Double,         // celkem za rok
    val totalMonthly: Double,        // průměr na měsíc
    val tariffComparison: Double,    // rozdíl mezi dražším a levnějším tarifem elektřiny (ročně)
)

object EnergyCostCalculator {
    /**
     * Roční náklady na energie.
     * elecKwh — roční spotřeba elektřiny (kWh), elecPrice — cena/kWh,
     * gasM3 — roční plyn (m³), gasPrice — cena/m³, heatingCost — roční topení (Kč, pevné),
     * waterM3 — roční voda (m³), waterPrice — cena za 1 m³ vody + stočení (Kč).
     */
    fun calculate(elecKwh: Double, elecPrice: Double, gasM3: Double, gasPrice: Double, heatingCost: Double, waterM3: Double, waterPrice: Double): EnergyCostResult {
        val electricity = elecKwh * elecPrice
        val gas = gasM3 * gasPrice
        val water = waterM3 * waterPrice
        return EnergyCostResult(
            electricityAnnual = electricity,
            gasAnnual = gas,
            heatingAnnual = heatingCost,
            waterAnnual = water,
            totalAnnual = electricity + gas + heatingCost + water,
            totalMonthly = (electricity + gas + heatingCost + water) / 12.0,
            tariffComparison = elecKwh * maxOf(elecPrice - 3.50, 0.0), // srovnání s levným tarifem ~3,50 Kč/kWh
        )
    }
}

/** Výsledek fondu oprav SVJ a záloh na služby. */
data class SvjFundResult(
    val monthlyPerUnit: Double,      // měsíční záloha na jednotku (Kč)
    val annualPerUnit: Double,       // ročně na jednotku
    val fundTarget: Double,          // doporučený fond oprav (5 % hodnoty bytu)
    val fundMonthlyContribution: Double, // měsíční příspěvek do fondu (10leté hromadění)
    val totalMonthly: Double,        // celkem měsíčně na jednotku (záloha + fond)
)

object SvjFundCalculator {
    /**
     * Zálohy a fond oprav SVJ.
     * unitValue — tržní hodnota bytu (Kč), serviceCosts — roční služby na jednotku (voda, úklid, výtah...),
     * fundYears — doba hromadění fondu (roky).
     */
    fun calculate(unitValue: Double, serviceCosts: Double, fundYears: Int): SvjFundResult {
        val monthlyService = serviceCosts / 12.0
        val fundTarget = unitValue * 0.05
        val fundMonthly = if (fundYears > 0) fundTarget / (fundYears * 12.0) else 0.0
        return SvjFundResult(
            monthlyPerUnit = monthlyService,
            annualPerUnit = serviceCosts,
            fundTarget = fundTarget,
            fundMonthlyContribution = fundMonthly,
            totalMonthly = monthlyService + fundMonthly,
        )
    }
}

/** Výsledek daně z nemovitých věcí (nový zákon od 1. 1. 2024). */
data class PropertyTaxResult(
    val annualTax: Double,           // roční daň (Kč)
    val monthlyEquivalent: Double,   // ekvivalent na měsíc
    val note: String,                // poznámka k sazbě
)

object PropertyTaxCalculator {
    const val APARTMENT_MIN = 3.0     // Kč/m² — byty
    const val APARTMENT_MAX = 45.0
    const val HOUSE_MIN = 1_500.0     // Kč/rok — rodinné domy
    const val HOUSE_MAX = 60_000.0

    /**
     * Daň z nemovitých věcí podle nového zákona (9/2024 Sb.).
     * type: "byt" | "dům", rate — sazba zvolená obcí (Kč/m² pro byt, Kč/rok pro dům),
     * area — podlahová plocha (m²) u bytu.
     */
    fun calculate(type: String, rate: Double, area: Double): PropertyTaxResult {
        return if (type == "byt") {
            val tax = rate * area
            PropertyTaxResult(tax, tax / 12.0, "Sazba obce ${String.format("%.1f", rate)} Kč/m² (limit 3–45 Kč/m²)")
        } else {
            val tax = rate
            PropertyTaxResult(tax, tax / 12.0, "Pevná sazba obce (limit 1 500–60 000 Kč/rok)")
        }
    }
}

/** Výsledek pojištění nemovitosti a domácnosti s kontrolou podpojištění. */
data class PropertyInsuranceResult(
    val buildingPremium: Double,     // roční pojistné na budovu (odhad)
    val contentsPremium: Double,     // roční pojistné na věci (odhad)
    val totalPremium: Double,        // celkem ročně
    val coverageBuilding: Double,    // doporučené krytí budovy (obnovná cena)
    val coverageContents: Double,    // doporučené krytí věcí
    val underinsuredPct: Double,     // podpojištění v % (0 = OK)
    val warning: String,             // upozornění na podpojištění
)

object PropertyInsuranceCalculator {
    const val BUILDING_PREMIUM_PER_M2 = 8.0   // orientační Kč/m²/rok na budovu
    const val CONTENTS_PREMIUM_PCT = 0.0015   // 0,15 % hodnoty věcí ročně

    /**
     * Pojištění nemovitosti + domácnost a kontrola podpojištění (doctrine proporcionality).
     * buildingValue — obnovná cena budovy, contentsValue — hodnota movitých věcí,
     * insuredBuilding — sjednané krytí budovy, insuredContents — sjednané krytí věcí.
     */
    fun calculate(buildingValue: Double, contentsValue: Double, insuredBuilding: Double, insuredContents: Double): PropertyInsuranceResult {
        val buildingPremium = buildingValue / 10_000.0 * BUILDING_PREMIUM_PER_M2 // odhad dle hodnoty
        val contentsPremium = contentsValue * CONTENTS_PREMIUM_PCT
        val underB = if (buildingValue > 0) maxOf(0.0, (1.0 - insuredBuilding / buildingValue) * 100.0) else 0.0
        val underC = if (contentsValue > 0) maxOf(0.0, (1.0 - insuredContents / contentsValue) * 100.0) else 0.0
        val worst = maxOf(underB, underC)
        return PropertyInsuranceResult(
            buildingPremium = buildingPremium,
            contentsPremium = contentsPremium,
            totalPremium = buildingPremium + contentsPremium,
            coverageBuilding = buildingValue,
            coverageContents = contentsValue,
            underinsuredPct = worst,
            warning = when {
                worst >= 20.0 -> "PODPOJIŠTĚNO o ${String.format("%.0f", worst)} % — při škodě dostaneš méně (proporční klauzule)"
                worst > 0.0 -> "Mírné podpojištění (${String.format("%.0f", worst)} %) — zvaž navýšení krytí"
                else -> "Krytí v pořádku — pojištěno na obnovnou hodnotu"
            },
        )
    }
}

/** Výsledek rozdělení nákladů mezi spolubydlící. */
data class RoommateSplitResult(
    val totalMonthly: Double,        // celkové společné náklady (Kč)
    val equalShare: Double,          // stejný podíl na osobu
    val perPerson: List<Pair<String, Double>>, // rozdělení dle dohody (jména + částky)
)

object RoommateSplitCalculator {
    /**
     * Rozpočítání nákladů mezi spolubydlící.
     * rent — nájem, utilities — energie+služby, internet — internet, n — počet osob,
     * extraShares — volitelné individuální částky (jméno -> měsíční příplatek).
     */
    fun calculate(rent: Double, utilities: Double, internet: Double, n: Int, extraShares: Map<String, Double>): RoommateSplitResult {
        val total = rent + utilities + internet
        val base = if (n > 0) total / n else 0.0
        // Každý platí základní podíl + svůj případný příplatek; zbytek se rozdělí rovnoměrně
        val extrasTotal = extraShares.values.sum()
        val remaining = maxOf(total - extrasTotal, 0.0)
        val perPersonBase = if (n > 0) remaining / n else 0.0
        val people = mutableListOf<Pair<String, Double>>()
        for ((name, extra) in extraShares) {
            people.add(name to (perPersonBase + extra))
        }
        var idx = extraShares.size
        while (people.size < n) {
            people.add("Osoba ${idx + 1}" to perPersonBase)
            idx++
        }
        return RoommateSplitResult(total, base, people)
    }
}
