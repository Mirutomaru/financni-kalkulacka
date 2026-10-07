package com.mirda.financnikalkulacky.calculators.housing
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.ln

/** Výsledek základní anuitní hypotéky. */
data class MortgageResult(
    val monthlyPayment: Double,   // A — měsíční splátka
    val totalPaid: Double,        // T = A · m
    val totalInterest: Double,    // I = T − L
)

/** Výsledek mimořádné splátky. */
data class ExtraPaymentResult(
    val newMonthlyPayment: Double,   // při variantě B (snížení splátky)
    val monthsShortened: Int,        // při variantě A (zkrácení doby)
    val interestSaved: Double,       // úspora na úrocích oproti původnímu plánu
)

/** Výsledek refinancování. */
data class RefinanceResult(
    val oldPayment: Double,
    val newPayment: Double,
    val grossSavings: Double,     // (A_stara − A_nova) · m
    val netSavings: Double,       // hrubá úspora − poplatky
    val breakEvenMonths: Int,     // C_ref / (A_stara − A_nova)
)

/** Roční bod amortizačního plánu (payoff křivka + splátkový kalendář). */
data class YearlyAmortization(
    val year: Int,                    // rok 1..years
    val remainingPrincipal: Double,   // zůstatek jistiny na konci roku
    val cumulativeInterest: Double,   // kumulované úroky do konce roku
    val yearlyInterest: Double = 0.0, // úroky zaplacené v daném roce
    val yearlyPrincipal: Double = 0.0,// jistina splacená v daném roce
)

object MortgageCalculator {
    /** Měsíční anuitní splátka pro přesný počet měsíců. r_a v %. */
    fun monthlyPayment(loan: Double, annualRatePct: Double, months: Int): Double {
        val m = months.coerceAtLeast(1)
        val r = annualRatePct / 100.0 / 12.0
        if (r == 0.0) return loan / m
        val factor = (1.0 + r).pow(m.toDouble())
        return loan * r * factor / (factor - 1.0)
    }


    /** Základní výpočet: splátka, celkem zaplaceno, úroky. */
    fun calculate(loan: Double, annualRatePct: Double, years: Int): MortgageResult {
        val m = (years * 12).coerceAtLeast(1)
        val a = monthlyPayment(loan, annualRatePct, m)
        val total = a * m
        return MortgageResult(a, total, total - loan)
    }

    /** Roční amortizační plán: zůstatek jistiny + kumulované úroky na konci každého roku. */
    fun yearlyAmortization(loan: Double, annualRatePct: Double, years: Int): List<YearlyAmortization> {
        val m = (years * 12).coerceAtLeast(1)
        val a = monthlyPayment(loan, annualRatePct, m)
        val r = annualRatePct / 100.0 / 12.0
        var balance = loan
        var cumInterest = 0.0
        var yearInterest = 0.0
        var yearPrincipal = 0.0
        val out = mutableListOf<YearlyAmortization>()
        for (month in 1..m) {
            val interest = balance * r
            val principal = a - interest
            balance -= principal
            if (balance < 0.0) balance = 0.0
            cumInterest += interest
            yearInterest += interest
            yearPrincipal += principal
            if (month % 12 == 0 || month == m) {
                out.add(YearlyAmortization(month / 12 + (if (month % 12 == 0) 0 else 1), balance, cumInterest, yearInterest, yearPrincipal))
                yearInterest = 0.0
                yearPrincipal = 0.0
            }
        }
        return out
    }

    /** RPSN (roční procentuální sazba nákladů) — efektivní roční úrok z měsíční anuity. */
    fun rpsn(loan: Double, annualRatePct: Double, years: Int): Double {
        val m = (years * 12).coerceAtLeast(1)
        val a = monthlyPayment(loan, annualRatePct, m)
        if (a <= 0.0 || loan <= 0.0) return 0.0
        // Newtonova iterace po měsíční sazbě r takové, že PV splátek = loan
        var lo = 0.0
        var hi = 1.0 / 12.0
        repeat(60) {
            val mid = (lo + hi) / 2.0
            val factor = if (mid == 0.0) 1.0 else (1.0 + mid).pow(m.toDouble())
            val pv = a * (factor - 1.0) / (mid * factor)
            if (pv > loan) lo = mid else hi = mid
        }
        return ((lo + hi) / 2.0) * 12.0 * 100.0
    }

    /** Mimořádná splátka E. goalB=true → snížit splátku (stejný počet měsíců), jinak zkrátit dobu. */
    fun extraPayment(loan: Double, annualRatePct: Double, remainingMonths: Int, extra: Double, goalB: Boolean): ExtraPaymentResult {
        val r = annualRatePct / 100.0 / 12.0
        val m = remainingMonths.coerceAtLeast(1)
        val aOld = monthlyPayment(loan, annualRatePct, m)
        val newLoan = (loan - extra).coerceAtLeast(0.0)
        if (goalB) {
            // Stejný počet měsíců, nižší splátka
            val aNew = monthlyPayment(newLoan, annualRatePct, m)
            val oldInterest = aOld * m - loan
            val newInterest = aNew * m + extra - loan
            return ExtraPaymentResult(aNew, 0, (oldInterest - newInterest).coerceAtLeast(0.0))
        }
        // Stejná splátka, zkrácení doby: m_nove = -ln(1 − r·L_nova/A) / ln(1 + r)
        val newLoan2 = (loan - extra).coerceAtLeast(0.0)
        var mNew = 0
        if (newLoan2 <= 0.0) {
            mNew = 0
        } else if (r == 0.0) {
            mNew = ceil(newLoan2 / aOld).toInt()
        } else {
            val arg = 1.0 - r * newLoan2 / aOld
            mNew = if (arg <= 0.0) m else ceil(-ln(arg) / ln(1.0 + r)).toInt().coerceAtMost(m)
        }
        val oldInterest = aOld * m - loan
        val newInterest = aOld * mNew + extra - loan
        return ExtraPaymentResult(aOld, (m - mNew).coerceAtLeast(0), (oldInterest - newInterest).coerceAtLeast(0.0))
    }

    /** Refinancování: srovnání splátek a čistá úspora včetně bodu zvratu. */
    fun refinance(loan: Double, remainingMonths: Int, oldRatePct: Double, newRatePct: Double, fees: Double): RefinanceResult {
        val m = remainingMonths.coerceAtLeast(1)
        val aOld = monthlyPayment(loan, oldRatePct, m)
        val aNew = monthlyPayment(loan, newRatePct, m)
        val gross = (aOld - aNew) * m
        val net = gross - fees
        val breakEven = if (aOld > aNew) ceil(fees / (aOld - aNew)).toInt() else 0
        return RefinanceResult(aOld, aNew, gross, net, breakEven)
    }
}
