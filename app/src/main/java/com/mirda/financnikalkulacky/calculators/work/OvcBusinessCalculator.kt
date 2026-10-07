package com.mirda.financnikalkulacky.calculators.work

/** Výsledek výpočtu záloh na pojištění OSVČ. */
data class OvcAdvanceResult(
    val socialMonthly: Double,
    val healthMonthly: Double,
    val totalMonthly: Double,
    val totalAnnual: Double,
)

/** Výsledek srovnání zaměstnanec × OSVČ × s.r.o. */
data class EmploymentCompareResult(
    val employeeNet: Double,
    val ovcNet: Double,
    val sroNet: Double,
    val bestMode: String,
)

/** Výsledek DPH výpočtu. */
data class VatResult(
    val priceWithoutVat: Double,
    val vatAmount: Double,
    val priceWithVat: Double,
    val registrationThresholdReached: Boolean,
)

/** Výsledek výpočtu hodinové sazby. */
data class HourlyRateResult(
    val hourlyRateGross: Double,
    val monthlyRevenueAtRate: Double,
    val netAfterContributionsTax: Double,
    val equivalentSalaryNet: Double,
)

/** Výsledek odpisů majetku. */
data class DepreciationResult(
    val annualDepreciation: Double,
    val taxSavingAnnual: Double,
    val totalTaxSavingOverLife: Double,
    val bookValueAfterYears: Double,
)

object OvcAdvanceCalculator {
    /**
     * Zálohy na sociální a zdravotní pojištění OSVČ.
     * Sociálka: 29,2 % z vyměřovacího základu (minimálně 5 756 Kč/měs.)
     * Zdravotní: 13,5 % z vyměřovacího základu (minimálně 3 306 Kč/měs. při nízkém příjmu)
     */
    fun calculate(monthlyIncome: Double): OvcAdvanceResult {
        val socialBase = maxOf(monthlyIncome * 0.8, 5_756.0 / 0.292) // min. základ
        val healthBase = maxOf(monthlyIncome * 0.8, 3_306.0 / 0.135)
        val socialMonthly = socialBase * 0.292
        val healthMonthly = healthBase * 0.135
        return OvcAdvanceResult(socialMonthly, healthMonthly, socialMonthly + healthMonthly, (socialMonthly + healthMonthly) * 12)
    }
}

object EmploymentCompareCalculator {
    /**
     * Srovnání čistého příjmu: zaměstnanec vs. OSVČ vs. s.r.o.
     * Zaměstnanec: hrubá mzda → čistka (SalaryCalculator).
     * OSVČ: příjem − paušální výdaje − zálohy na pojistění − daň 15 %.
     * S.r.o.: zisk firmy − daň z příjmů právnických osob 21 % − odvod majiteli.
     */
    fun calculate(monthlyIncome: Double, expensePct: Double = 60.0): EmploymentCompareResult {
        // Zaměstnanec — příjem jako hrubá mzda
        val employeeNet = SalaryCalculator.calculate(monthlyIncome, 0).netMonthly

        // OSVČ
        val ovcRevenue = monthlyIncome * 12
        val ovcExpenses = ovcRevenue * expensePct / 100.0
        val ovcTaxable = maxOf(ovcRevenue - ovcExpenses, 0.0)
        val adv = OvcAdvanceCalculator.calculate(monthlyIncome)
        val ovcContributionsAnnual = adv.totalAnnual
        val ovcTax = maxOf(ovcTaxable - 30_840.0, 0.0) * 0.15
        val ovcNet = (ovcRevenue - ovcExpenses - ovcContributionsAnnual - ovcTax) / 12.0

        // S.r.o. — zisk firmy = příjem − náklady; daň PO 21 %; majitel si vyplatí dividendu
        val sroProfit = monthlyIncome * 12 * (1 - expensePct / 100.0)
        val sroTax = maxOf(sroProfit, 0.0) * 0.21
        // dividendy: daň 15 % z výplaty (zjednodušeně bez odpočtu)
        val sroDividend = sroProfit - sroTax
        val sroNet = (sroDividend * 0.85) / 12.0

        val bestMode = when {
            employeeNet >= ovcNet && employeeNet >= sroNet -> "zaměstnanec"
            ovcNet >= sroNet -> "OSVČ"
            else -> "s.r.o."
        }
        return EmploymentCompareResult(employeeNet, ovcNet, sroNet, bestMode)
    }
}

object VatCalculator {
    /**
     * DPH: cena s DPH a bez něj, hranice registrace 1 mil. Kč / rok.
     */
    fun calculate(priceWithVat: Double = 0.0, priceWithoutVat: Double = 0.0, ratePct: Double = 21.0, annualTurnover: Double = 500_000.0): VatResult {
        val base = if (priceWithVat > 0) priceWithVat / (1 + ratePct / 100.0) else priceWithoutVat
        val vatAmount = base * ratePct / 100.0
        return VatResult(base, vatAmount, base + vatAmount, annualTurnover >= 1_000_000.0)
    }
}

object HourlyRateCalculator {
    /**
     * Jakou hodinovou sazbu si účtovat, aby vyšlo jako hrubá mzda.
     * Sazba musí pokrýt: příjem + odvody OSVČ + daň.
     */
    fun calculate(targetGrossSalary: Double, hoursPerMonth: Int = 120): HourlyRateResult {
        // potřebný měsíční obrat tak, aby čistý výsledek ≈ hrubá mzda (orientačně)
        val requiredRevenue = targetGrossSalary / 0.55 // orientační koeficient zátěže OSVČ ~45 %
        val hourlyRateGross = requiredRevenue / hoursPerMonth
        val monthlyRevenueAtRate = hourlyRateGross * hoursPerMonth
        val adv = OvcAdvanceCalculator.calculate(monthlyRevenueAtRate)
        val taxable = maxOf(monthlyRevenueAtRate - monthlyRevenueAtRate * 0.6, 0.0)
        val taxAnnual = maxOf(taxable * 12 - 30_840.0, 0.0) * 0.15
        val netAfterContributionsTax = (monthlyRevenueAtRate * 12 - adv.totalAnnual - taxAnnual) / 12.0
        return HourlyRateResult(hourlyRateGross, monthlyRevenueAtRate, netAfterContributionsTax, targetGrossSalary)
    }
}

object DepreciationCalculator {
    /**
     * Lineární odpisy majetku a daňová úspora (daň OSVČ 15 %).
     */
    fun calculate(assetValue: Double, usefulLifeYears: Int = 5): DepreciationResult {
        val annualDepreciation = assetValue / usefulLifeYears.coerceAtLeast(1)
        val taxSavingAnnual = annualDepreciation * 0.15
        return DepreciationResult(
            annualDepreciation,
            taxSavingAnnual,
            taxSavingAnnual * usefulLifeYears,
            0.0, // po uplynutí životnosti je účetní hodnota 0
        )
    }
}
