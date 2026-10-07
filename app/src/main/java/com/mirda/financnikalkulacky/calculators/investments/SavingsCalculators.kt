package com.mirda.financnikalkulacky.calculators.investments

import kotlin.math.pow

/** Výsledek spořicího účtu / termínovaného vkladu po zdanění. */
data class SavingsAccountResult(
    val finalValue: Double,        // finální částka po zdanění úroků
    val grossInterest: Double,     // hrubé úroky
    val taxOnInterest: Double,     // daň 15 % z úroků
    val netInterest: Double,       // čisté úroky
)

object SavingsAccountCalculator {
    /** Složené úročení vkladu; úroky zdaněny 15 % (paušální daň). */
    fun calculate(amount: Double, annualRatePct: Double, years: Int): SavingsAccountResult {
        val gross = amount * ((1.0 + annualRatePct / 100.0).pow(years.toDouble()) - 1.0)
        val tax = gross * 0.15
        return SavingsAccountResult(amount + gross - tax, gross, tax, gross - tax)
    }
}

/** Výsledek stavebního spoření včetně státní podpory a úvěru. */
data class BuildingSocietyResult(
    val totalSaved: Double,            // celkem uloženo (vlastní vklady)
    val stateSubsidyTotal: Double,     // celková státní podpora
    val interestEarned: Double,        // úroky ze spoření
    val finalValue: Double,            // finální hodnota spořicího účtu
    val loanRatePct: Double,           // sazba úvěru ze stavebního spoření
    val loanMonthlyPayment: Double,    // měsíční splátka úvěru (anuita)
    val effectiveReturnPct: Double,    // efektivní roční výnos spoření v %
)

object BuildingSocietyCalculator {
    const val STATE_SUBSIDY_PCT = 0.05      // státní podpora 5 % z ročních vkladů
    const val STATE_SUBSIDY_CAP = 30_000.0  // max 30 tis Kč/rok

    fun calculate(monthlyContribution: Double, years: Int, savingsRatePct: Double, loanAmount: Double, loanYears: Int, loanRatePct: Double): BuildingSocietyResult {
        val annual = monthlyContribution * 12.0
        val subsidyPerYear = minOf(annual * STATE_SUBSIDY_PCT, STATE_SUBSIDY_CAP)
        // Složené úročení vkladů + podpory (orientačně: podpora připadá na konci roku)
        var balance = 0.0
        for (y in 1..years) {
            balance *= (1.0 + savingsRatePct / 100.0)
            balance += annual * (1.0 + savingsRatePct / 200.0) // vklady po celý rok, úrok od poloviny
            balance += subsidyPerYear
        }
        val totalSaved = annual * years
        val interestEarned = balance - totalSaved - subsidyPerYear * years
        val m = (loanYears * 12).coerceAtLeast(1)
        val r = loanRatePct / 100.0 / 12.0
        val payment = if (r > 0.0) loanAmount * r * (1.0 + r).pow(m.toDouble()) / ((1.0 + r).pow(m.toDouble()) - 1.0) else loanAmount / m
        val effectiveReturnPct = if (totalSaved > 0.0) (interestEarned + subsidyPerYear * years) / totalSaved / years * 100.0 else 0.0
        return BuildingSocietyResult(totalSaved, subsidyPerYear * years, interestEarned, balance, loanRatePct, payment, effectiveReturnPct)
    }
}

/** Výsledek penzijního spoření (DPS). */
data class PensionSavingResult(
    val stateContributionTotal: Double,  // celková státní příspěvek (240 Kč/měs.)
    val taxDeductibleAnnual: Double,     // odpočet ze základu daně (rok)
    val annualTaxSaving: Double,         // daňová úspora (rok)
    val totalCapitalAnnual: Double,      // celkový roční kapitál (vklad + stát + zaměstnavatel)
    val netCostMonthly: Double,          // reálná měsíční cena po daňové úspoře a státním příspěvku
)

object PensionSavingCalculator {
    const val STATE_CONTRIBUTION_MONTHLY = 240.0   // státní příspěvek (pevný)
    const val MIN_USER_CONTRIBUTION = 100.0        // minimální vklad pro státní příspěvek

    /** userMonthly: vklad zaměstnance; employerMonthly: příspěvek zaměstnavatele; incomeAnnual: roční příjem; taxRatePct: sazba daně. */
    fun calculate(userMonthly: Double, employerMonthly: Double, incomeAnnual: Double, taxRatePct: Double): PensionSavingResult {
        val state = if (userMonthly >= MIN_USER_CONTRIBUTION) STATE_CONTRIBUTION_MONTHLY else 0.0
        // Odpočet ze základu daně: max 12 % ročního příjmu
        val deductible = minOf(userMonthly * 12.0, incomeAnnual * 0.12)
        val taxSaving = deductible * taxRatePct / 100.0
        return PensionSavingResult(
            stateContributionTotal = state * 12.0,
            taxDeductibleAnnual = deductible,
            annualTaxSaving = taxSaving,
            totalCapitalAnnual = (userMonthly + employerMonthly) * 12.0 + state * 12.0,
            netCostMonthly = userMonthly - taxSaving / 12.0 - state,
        )
    }
}

/** Výsledek srovnání DIP vs DPS. */
data class DipVsDpsResult(
    val dipTaxSaving: Double,      // daňová úspora DIP (rok)
    val dpsTaxSaving: Double,      // daňová úspora DPS (rok)
    val dpsStateContribution: Double, // státní příspěvek DPS (rok)
    val betterMode: String,        // "DIP" | "DPS"
    val advantageAnnual: Double,   // roční výhoda lepšího režimu
)

object DipVsDpsCalculator {
    const val DIP_DEDUCTION_CAP = 350_000.0  // strop odpočtu DIP (rok)

    /** dipMonthly: měsíční vklad do DIP; dpsMonthly: měsíční vklad do DPS; incomeAnnual; taxRatePct. */
    fun calculate(dipMonthly: Double, dpsMonthly: Double, incomeAnnual: Double, taxRatePct: Double): DipVsDpsResult {
        val dipSaving = minOf(dipMonthly * 12.0, DIP_DEDUCTION_CAP) * taxRatePct / 100.0
        val dps = PensionSavingCalculator.calculate(dpsMonthly, 0.0, incomeAnnual, taxRatePct)
        // DPS: daňová úspora + státní příspěvek vs DIP: jen daňová úspora (při stejném vkladu)
        val dipTotal = dipSaving
        val dpsTotal = dps.annualTaxSaving + dps.stateContributionTotal
        return DipVsDpsResult(
            dipTaxSaving = dipSaving,
            dpsTaxSaving = dps.annualTaxSaving,
            dpsStateContribution = dps.stateContributionTotal,
            betterMode = if (dpsTotal >= dipTotal) "DPS" else "DIP",
            advantageAnnual = kotlin.math.abs(dpsTotal - dipTotal),
        )
    }
}

/** Výsledek životního pojištění s investiční složkou. */
data class LifeInsuranceResult(
    val totalPremiums: Double,         // celkem zaplacené pojistné
    val taxDeductibleAnnual: Double,   // odpočet ze základu (rok) — max 30 tis/rok
    val annualTaxSaving: Double,       // daňová úspora (rok)
    val finalValue: Double,            // finální hodnota pojištění (orientační výnos)
    val netCostTotal: Double,          // reálná cena celkem (pojistné − daňové úspory)
    val effectiveReturnPct: Double,    // efektivní roční výnos v %
)

object LifeInsuranceCalculator {
    const val TAX_DEDUCTION_CAP = 30_000.0  // strop odpočtu životního pojištění (rok)

    fun calculate(premiumMonthly: Double, years: Int, expectedReturnPct: Double, taxRatePct: Double): LifeInsuranceResult {
        val totalPremiums = premiumMonthly * 12.0 * years
        // Složené zhodnocení měsíčních pojistných
        var balance = 0.0
        repeat(years * 12) { balance = (balance + premiumMonthly) * (1.0 + expectedReturnPct / 100.0 / 12.0) }
        val deductible = minOf(premiumMonthly * 12.0, TAX_DEDUCTION_CAP)
        val taxSaving = deductible * taxRatePct / 100.0
        val netCostTotal = totalPremiums - taxSaving * years
        val effectiveReturnPct = if (netCostTotal > 0.0) ((balance - netCostTotal) / netCostTotal / years * 100.0).coerceAtLeast(-100.0) else 0.0
        return LifeInsuranceResult(totalPremiums, deductible, taxSaving, balance, netCostTotal, effectiveReturnPct)
    }
}

/** Výsledek výpočtu nouzové rezervy. */
data class EmergencyFundResult(
    val recommendedAmount: Double,   // doporučená výše rezervy (3–6 měsíců výdajů)
    val currentShortfall: Double,    // chybějící částka
    val monthlySavingsNeeded: Double, // kolik měsíčně odkládat na dosažení cíle
)

object EmergencyFundCalculator {
    fun calculate(monthlyExpenses: Double, monthsOfReserve: Int, currentSavings: Double, horizonMonths: Int): EmergencyFundResult {
        val target = monthlyExpenses * monthsOfReserve.coerceIn(1, 24)
        val shortfall = (target - currentSavings).coerceAtLeast(0.0)
        val perMonth = if (horizonMonths > 0) shortfall / horizonMonths else 0.0
        return EmergencyFundResult(target, shortfall, perMonth)
    }
}

/** Výsledek spoření na cíl. */
data class GoalSavingsResult(
    val requiredMonthly: Double,     // potřebná měsíční úložka
    val finalValue: Double,          // finální částka (ověření)
    val totalContributions: Double,  // celkem vloženo
    val interestEarned: Double,      // zisk z úroků
)

object GoalSavingsCalculator {
    /** Kolik měsíčně odkládat na cílovou částku za daný čas při zhodnocení. */
    fun calculate(targetAmount: Double, years: Int, annualRatePct: Double): GoalSavingsResult {
        val m = (years * 12).coerceAtLeast(1)
        val r = annualRatePct / 100.0 / 12.0
        // Budoucí hodnota anuity: pmt * (((1+r)^m - 1)/r)
        val factor = if (r > 0.0) ((1.0 + r).pow(m.toDouble()) - 1.0) / r else m.toDouble()
        val pmt = targetAmount / factor
        return GoalSavingsResult(pmt, targetAmount, pmt * m, targetAmount - pmt * m)
    }
}
