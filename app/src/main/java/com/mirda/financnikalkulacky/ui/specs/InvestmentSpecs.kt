package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.TrendingUp
import com.mirda.financnikalkulacky.calculators.investments.AnnuityCalculator
import com.mirda.financnikalkulacky.calculators.investments.BondYtmCalculator
import com.mirda.financnikalkulacky.calculators.investments.BuildingSocietyCalculator
import com.mirda.financnikalkulacky.calculators.investments.CryptoTaxCalculator
import com.mirda.financnikalkulacky.calculators.investments.CurrencyConversionCalculator
import com.mirda.financnikalkulacky.calculators.investments.DipVsDpsCalculator
import com.mirda.financnikalkulacky.calculators.investments.EarlyLatePensionCalculator
import com.mirda.financnikalkulacky.calculators.investments.EmergencyFundCalculator
import com.mirda.financnikalkulacky.calculators.investments.FeeImpactCalculator
import com.mirda.financnikalkulacky.calculators.investments.GoalSavingsCalculator
import com.mirda.financnikalkulacky.calculators.investments.InvestmentCalculator
import com.mirda.financnikalkulacky.calculators.investments.InvestmentTaxCalculator
import com.mirda.financnikalkulacky.calculators.investments.LifeInsuranceCalculator
import com.mirda.financnikalkulacky.calculators.investments.LumpSumVsDcaCalculator
import com.mirda.financnikalkulacky.calculators.investments.PensionGapCalculator
import com.mirda.financnikalkulacky.calculators.investments.PensionSavingCalculator
import com.mirda.financnikalkulacky.calculators.investments.RetirementAgeCalculator
import com.mirda.financnikalkulacky.calculators.investments.SavingsAccountCalculator
import com.mirda.financnikalkulacky.calculators.investments.StatePensionCalculator
import com.mirda.financnikalkulacky.components.fmtCzk
import com.mirda.financnikalkulacky.ui.model.CalcResult
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.Field

val INVESTMENT_SPECS: List<CalcSpec> = listOf(
    // ── SPOŘENÍ ───────────────────────────────────────────────────────
    CalcSpec(
        id = "sporeni",
        title = "Složené úročení & spoření",
        description = "Budoucí hodnota vkladu a pravidelných úložek.",
        icon = Icons.Filled.Savings,
        group = "Spoření",
        fields = listOf(
            Field("principal", "Počáteční vklad", 0f, 5_000_000f, 10_000f, 100_000f, "Kč"),
            Field("pmt", "Měsíční úložka", 0f, 50_000f, 500f, 5_000f, "Kč"),
            Field("rate", "Roční zhodnocení", 0f, 15f, 0.1f, 6f, "%"),
            Field("years", "Doba spoření", 1f, 40f, 1f, 20f, "let"),
        ),
        related = listOf("fire", "dps", "statni-duchod"),
    ) { v, _ ->
        val r = InvestmentCalculator.futureValue(v["principal"]!!.toDouble(), v["pmt"]!!.toDouble(), v["rate"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Finální částka", fmtCzk(r.finalValue)),
            CalcResult("Vloženo celkem", fmtCzk(r.totalContributions)),
            CalcResult("Zisk z úroků", fmtCzk(r.profit)),
        )
    },

    CalcSpec(
        id = "sporeni-ucet",
        title = "Spořicí účet & termínovaný vklad",
        description = "Výnos po zdanění 15 % (paušální daň).",
        icon = Icons.Filled.AttachMoney,
        group = "Spoření",
        fields = listOf(
            Field("amount", "Počáteční vklad", 0f, 5_000_000f, 10_000f, 200_000f, "Kč"),
            Field("rate", "Roční úroková sazba", 0f, 10f, 0.1f, 4f, "%"),
            Field("years", "Doba vkladu", 1f, 30f, 1f, 5f, "let"),
        ),
    ) { v, _ ->
        val r = SavingsAccountCalculator.calculate(v["amount"]!!.toDouble(), v["rate"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Finální částka (po dani)", fmtCzk(r.finalValue)),
            CalcResult("Hrubé úroky", fmtCzk(r.grossInterest)),
            CalcResult("Daň 15 % z úroků", fmtCzk(r.taxOnInterest)),
            CalcResult("Čisté úroky", fmtCzk(r.netInterest)),
        )
    },

    CalcSpec(
        id = "stavebni-sporeni",
        title = "Stavební spoření & úvěr",
        description = "Výnos se státní podporou 5 % a splátka úvěru.",
        icon = Icons.Filled.House,
        group = "Spoření",
        fields = listOf(
            Field("monthly", "Měsíční vklad", 0f, 20_000f, 100f, 3_000f, "Kč"),
            Field("years", "Doba spoření", 1f, 30f, 1f, 10f, "let"),
            Field("rate", "Úročení stavebního spoření", 0f, 8f, 0.1f, 4.5f, "%"),
            Field("loanAmount", "Půjčka ze stavebního spoření", 0f, 5_000_000f, 50_000f, 1_000_000f, "Kč"),
            Field("loanYears", "Doba splácení půjčky", 1f, 30f, 1f, 20f, "let"),
            Field("loanRate", "Sazba půjčky ze stavebního spoření", 0f, 8f, 0.1f, 4.5f, "%"),
        ),
    ) { v, _ ->
        val r = BuildingSocietyCalculator.calculate(v["monthly"]!!.toDouble(), v["years"]!!.toInt(), v["rate"]!!.toDouble(), v["loanAmount"]!!.toDouble(), v["loanYears"]!!.toInt(), v["loanRate"]!!.toDouble())
        listOf(
            CalcResult("Finální hodnota spoření", fmtCzk(r.finalValue)),
            CalcResult("Státní podpora (celkem)", fmtCzk(r.stateSubsidyTotal)),
            CalcResult("Úroky ze spoření", fmtCzk(r.interestEarned)),
            CalcResult("Efektivní roční výnos", "%.1f %%".format(r.effectiveReturnPct)),
            CalcResult("Splátka půjčky (měs.)", fmtCzk(r.loanMonthlyPayment)),
        )
    },

    CalcSpec(
        id = "dps",
        title = "Penzijní spoření (DPS)",
        description = "Státní příspěvek, daňový odpočet a reálná cena.",
        icon = Icons.Filled.Groups,
        group = "Spoření",
        fields = listOf(
            Field("userMonthly", "Vklad zaměstnance (měs.)", 100f, 10_000f, 100f, 2_000f, "Kč"),
            Field("employerMonthly", "Příspěvek zaměstnavatele (měs.)", 0f, 5_000f, 100f, 1_000f, "Kč"),
            Field("incomeAnnual", "Roční příjem", 100_000f, 3_000_000f, 10_000f, 400_000f, "Kč"),
            Field("taxRate", "Daňová sazba", 0f, 40f, 1f, 15f, "%"),
        ),
    ) { v, _ ->
        val r = PensionSavingCalculator.calculate(v["userMonthly"]!!.toDouble(), v["employerMonthly"]!!.toDouble(), v["incomeAnnual"]!!.toDouble(), v["taxRate"]!!.toDouble())
        listOf(
            CalcResult("Státní příspěvek (rok)", fmtCzk(r.stateContributionTotal)),
            CalcResult("Odpočet ze základu daně", fmtCzk(r.taxDeductibleAnnual)),
            CalcResult("Daňová úspora (rok)", fmtCzk(r.annualTaxSaving)),
            CalcResult("Celkový roční kapitál", fmtCzk(r.totalCapitalAnnual)),
            CalcResult("Reálná měsíční cena", fmtCzk(r.netCostMonthly)),
        )
    },

    CalcSpec(
        id = "dip-vs-dps",
        title = "DIP vs. DPS — srovnání",
        description = "Který režim se vyplatí při stejném vkladu.",
        icon = Icons.Filled.Compare,
        group = "Spoření",
        fields = listOf(
            Field("monthly", "Měsíční vklad (stejný do obou)", 0f, 30_000f, 500f, 10_000f, "Kč"),
            Field("incomeAnnual", "Roční příjem", 100_000f, 3_000_000f, 10_000f, 400_000f, "Kč"),
            Field("taxRate", "Daňová sazba", 0f, 40f, 1f, 15f, "%"),
        ),
    ) { v, _ ->
        val r = DipVsDpsCalculator.calculate(v["monthly"]!!.toDouble(), v["monthly"]!!.toDouble(), v["incomeAnnual"]!!.toDouble(), v["taxRate"]!!.toDouble())
        listOf(
            CalcResult("Daňová úspora DIP (rok)", fmtCzk(r.dipTaxSaving)),
            CalcResult("Úspora DPS + státní příspěvek", fmtCzk(r.dpsTaxSaving + r.dpsStateContribution)),
            CalcResult("Výhodnější režim", r.betterMode),
            CalcResult("Roční výhoda lepšího režimu", fmtCzk(r.advantageAnnual)),
        )
    },

    CalcSpec(
        id = "zivotni-pojisteni",
        title = "Životní pojištění s investicí",
        description = "Odpočet od daně a reálná výhodnost.",
        icon = Icons.Filled.Shield,
        group = "Spoření",
        fields = listOf(
            Field("premiumMonthly", "Měsíční pojistné", 0f, 20_000f, 100f, 3_000f, "Kč"),
            Field("years", "Doba pojištění", 5f, 40f, 1f, 20f, "let"),
            Field("expectedReturn", "Očekávaný roční výnos", 0f, 8f, 0.1f, 3f, "%"),
            Field("taxRate", "Daňová sazba", 0f, 40f, 1f, 15f, "%"),
        ),
    ) { v, _ ->
        val r = LifeInsuranceCalculator.calculate(v["premiumMonthly"]!!.toDouble(), v["years"]!!.toInt(), v["expectedReturn"]!!.toDouble(), v["taxRate"]!!.toDouble())
        listOf(
            CalcResult("Finální hodnota pojištění", fmtCzk(r.finalValue)),
            CalcResult("Daňová úspora (rok)", fmtCzk(r.annualTaxSaving)),
            CalcResult("Reálná cena celkem", fmtCzk(r.netCostTotal)),
            CalcResult("Efektivní roční výnos", "%.1f %%".format(r.effectiveReturnPct)),
        )
    },

    CalcSpec(
        id = "nouzova-rezerva",
        title = "Nouzová rezerva",
        description = "Doporučená výše podle výdajů a situace.",
        icon = Icons.Filled.LocalFireDepartment,
        group = "Spoření",
        fields = listOf(
            Field("monthlyExpenses", "Měsíční výdaje", 5_000f, 100_000f, 1_000f, 25_000f, "Kč"),
            Field("monthsReserve", "Počet měsíců rezervy", 3f, 12f, 1f, 6f, "měs."),
            Field("currentSavings", "Aktuální úspory", 0f, 1_000_000f, 10_000f, 50_000f, "Kč"),
            Field("horizonMonths", "Doba naspoření rezervy", 1f, 60f, 1f, 12f, "měs."),
        ),
    ) { v, _ ->
        val r = EmergencyFundCalculator.calculate(v["monthlyExpenses"]!!.toDouble(), v["monthsReserve"]!!.toInt(), v["currentSavings"]!!.toDouble(), v["horizonMonths"]!!.toInt())
        listOf(
            CalcResult("Doporučená výše rezervy", fmtCzk(r.recommendedAmount)),
            CalcResult("Chybějící částka", fmtCzk(r.currentShortfall)),
            CalcResult("Měsíčně odkládat na cíl", fmtCzk(r.monthlySavingsNeeded)),
        )
    },

    CalcSpec(
        id = "sporeni-na-cil",
        title = "Spoření na cíl",
        description = "Kolik měsíčně odkládat na auto, svatbu či studium.",
        icon = Icons.Filled.Flag,
        group = "Spoření",
        fields = listOf(
            Field("targetAmount", "Cílová částka", 10_000f, 5_000_000f, 10_000f, 500_000f, "Kč"),
            Field("years", "Doba spoření", 1f, 30f, 1f, 10f, "let"),
            Field("rate", "Roční zhodnocení", 0f, 10f, 0.1f, 4f, "%"),
        ),
    ) { v, _ ->
        val r = GoalSavingsCalculator.calculate(v["targetAmount"]!!.toDouble(), v["years"]!!.toInt(), v["rate"]!!.toDouble())
        listOf(
            CalcResult("Potřebná měsíční úložka", fmtCzk(r.requiredMonthly)),
            CalcResult("Finální částka (ověření)", fmtCzk(r.finalValue)),
            CalcResult("Celkem vloženo", fmtCzk(r.totalContributions)),
            CalcResult("Zisk z úroků", fmtCzk(r.interestEarned)),
        )
    },

    // ── INVESTICE ─────────────────────────────────────────────────────
    CalcSpec(
        id = "dividendy",
        title = "Dividendové portfolio",
        description = "Pasivní příjem z dividend po srážkové dani.",
        icon = Icons.Filled.Payments,
        group = "Investice",
        fields = listOf(
            Field("portfolio", "Hodnota portfolia", 100_000f, 20_000_000f, 50_000f, 1_000_000f, "Kč"),
            Field("yield", "Dividendový výnos (DY)", 0f, 10f, 0.1f, 4f, "%"),
            Field("tax", "Srážková daň", 0f, 25f, 1f, 15f, "%"),
        ),
    ) { v, _ ->
        val r = InvestmentCalculator.dividends(v["portfolio"]!!.toDouble(), v["yield"]!!.toDouble(), v["tax"]!!.toDouble())
        listOf(
            CalcResult("Hrubé dividendy (rok)", fmtCzk(r.grossAnnual)),
            CalcResult("Čisté dividendy (rok)", fmtCzk(r.netAnnual)),
            CalcResult("Měsíční pasivní příjem", fmtCzk(r.monthlyNet)),
        )
    },

    CalcSpec(
        id = "jednorazove-vs-dca",
        title = "Jednorázově vs. pravidelně (DCA)",
        description = "Stejný kapitál: najednou nebo rozloženě do měsíců.",
        icon = Icons.Filled.SwapVert,
        group = "Investice",
        fields = listOf(
            Field("totalAmount", "Celkový kapitál", 10_000f, 5_000_000f, 10_000f, 200_000f, "Kč"),
            Field("years", "Doba investování", 1f, 30f, 1f, 10f, "let"),
            Field("rate", "Roční zhodnocení", 0f, 15f, 0.1f, 6f, "%"),
        ),
    ) { v, _ ->
        val r = LumpSumVsDcaCalculator.calculate(v["totalAmount"]!!.toDouble(), v["years"]!!.toInt(), v["rate"]!!.toDouble())
        listOf(
            CalcResult("Finální částka — jednorázově", fmtCzk(r.lumpSumFinal)),
            CalcResult("Finální částka — pravidelně (DCA)", fmtCzk(r.dcaFinal)),
            CalcResult("Rozdíl ve prospěch lepšího scénáře", fmtCzk(r.difference)),
            CalcResult("Statisticky výhodnější", r.betterMode),
        )
    },

    CalcSpec(
        id = "vplyv-poplatku",
        title = "Vliv poplatků (TER) na výsledek",
        description = "Kolik ztratíte na ročních poplatcích za 30 let.",
        icon = Icons.Filled.Percent,
        group = "Investice",
        fields = listOf(
            Field("amount", "Počáteční vklad", 10_000f, 5_000_000f, 10_000f, 200_000f, "Kč"),
            Field("rate", "Roční výnos (brutto)", 0f, 15f, 0.1f, 7f, "%"),
            Field("ter", "Roční poplatek TER", 0f, 3f, 0.05f, 0.3f, "%"),
            Field("years", "Doba držby", 1f, 40f, 1f, 30f, "let"),
        ),
    ) { v, _ ->
        val r = FeeImpactCalculator.calculate(v["amount"]!!.toDouble(), v["rate"]!!.toDouble(), v["ter"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Finální částka bez poplatků", fmtCzk(r.finalNoFees)),
            CalcResult("Finální částka s poplatky", fmtCzk(r.finalWithFees)),
            CalcResult("Ztráta na poplatcích", fmtCzk(r.lostToFees)),
            CalcResult("Efektivní roční výnos po poplatku", "%.1f %%".format(r.effectiveReturnPct)),
        )
    },

    CalcSpec(
        id = "zdaneni-investic",
        title = "Zdanění investic (časový test)",
        description = "Osvobození 3 roky, limit 30 tis/rok.",
        icon = Icons.Filled.Gavel,
        group = "Investice",
        fields = listOf(
            Field("purchaseCost", "Nákupní cena", 10_000f, 5_000_000f, 10_000f, 200_000f, "Kč"),
            Field("salePrice", "Prodejní cena", 10_000f, 6_000_000f, 10_000f, 400_000f, "Kč"),
            Field("yearsHeld", "Držba (roky)", 0f, 10f, 1f, 1f, "let"),
            Field("otherGains", "Ostatní zisky za rok", 0f, 200_000f, 5_000f, 0f, "Kč"),
        ),
    ) { v, _ ->
        val r = InvestmentTaxCalculator.calculate(v["purchaseCost"]!!.toDouble(), v["salePrice"]!!.toDouble(), v["yearsHeld"]!!.toInt(), v["otherGains"]!!.toDouble())
        listOf(
            CalcResult("Zisk ze prodeje", fmtCzk(r.gain)),
            CalcResult("Daňový základ", fmtCzk(r.taxableGain)),
            CalcResult("Daň 15 %", fmtCzk(r.tax)),
            CalcResult("Čistý zisk po dani", fmtCzk(r.netProfit)),
        )
    },

    CalcSpec(
        id = "krypto",
        title = "Kryptoměny — zisk a zdanění",
        description = "Zisk/ztráta a daň 15 % po limitu 30 tis.",
        icon = Icons.Filled.CurrencyBitcoin,
        group = "Investice",
        fields = listOf(
            Field("purchases", "Celkové náklady na koupi", 1_000f, 2_000_000f, 1_000f, 50_000f, "Kč"),
            Field("sales", "Výtěžek z prodeje", 1_000f, 3_000_000f, 1_000f, 120_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = CryptoTaxCalculator.calculate(v["purchases"]!!.toDouble(), v["sales"]!!.toDouble())
        listOf(
            CalcResult("Zisk / ztráta", fmtCzk(r.totalGain)),
            CalcResult("Daňový základ (po limitu)", fmtCzk(r.taxableGain)),
            CalcResult("Daň 15 %", fmtCzk(r.tax)),
            CalcResult("Čistý zisk po dani", fmtCzk(r.netProfit)),
        )
    },

    CalcSpec(
        id = "dluhopisy",
        title = "Dluhopisy — výnos do splatnosti",
        description = "YTM, celkový návrat a roční kupón.",
        icon = Icons.Filled.Timeline,
        group = "Investice",
        fields = listOf(
            Field("price", "Cena dluhopisu", 500f, 1_200f, 1f, 980f, "Kč"),
            Field("faceValue", "Jmenovitá hodnota", 500f, 1_200f, 1f, 1_000f, "Kč"),
            Field("couponPct", "Kupónová sazba", 0f, 10f, 0.1f, 4f, "%"),
            Field("yearsToMaturity", "Roků do splatnosti", 1f, 30f, 1f, 10f, "let"),
        ),
    ) { v, _ ->
        val r = BondYtmCalculator.calculate(v["price"]!!.toDouble(), v["faceValue"]!!.toDouble(), v["couponPct"]!!.toDouble(), v["yearsToMaturity"]!!.toInt())
        listOf(
            CalcResult("Výnos do splatnosti (YTM)", "%.1f %%".format(r.ytmPct)),
            CalcResult("Celkový návrat za dobu držby", "%.1f %%".format(r.totalReturnPct)),
            CalcResult("Roční kupón", fmtCzk(r.annualCoupon)),
        )
    },

    CalcSpec(
        id = "meny",
        title = "Přepočet měn & kurzové riziko",
        description = "Hodnota v Kč a dopad změny kurzu.",
        icon = Icons.Filled.SwapHoriz,
        group = "Investice",
        fields = listOf(
            Field("amount", "Částka v cizí měně (EUR)", 1_000f, 500_000f, 1_000f, 10_000f, "€"),
            Field("rateNow", "Aktuální kurz (Kč/EUR)", 20f, 40f, 0.1f, 26f, "Kč"),
            Field("rateChangePct", "Očekávaná změna kurzu", -30f, 30f, 1f, -5f, "%"),
        ),
    ) { v, _ ->
        val r = CurrencyConversionCalculator.calculate(v["amount"]!!.toDouble(), v["rateNow"]!!.toDouble(), v["rateChangePct"]!!.toDouble())
        listOf(
            CalcResult("Hodnota v Kč (aktuálně)", fmtCzk(r.convertedAmount)),
            CalcResult("Hodnota po změně kurzu", fmtCzk(r.futureValueAtRate)),
            CalcResult("Kurzové riziko", "%.0f %%".format(r.currencyRiskPct)),
        )
    },

    CalcSpec(
        id = "inflace",
        title = "Vliv inflace na kupní sílu",
        description = "Reálná hodnota úspor po zvoleném počtu let.",
        icon = Icons.Filled.TrendingUp,
        group = "Investice",
        fields = listOf(
            Field("amount", "Částka (Kč)", 10_000f, 5_000_000f, 10_000f, 100_000f, "Kč"),
            Field("inflation", "Roční inflace", 0f, 15f, 0.1f, 5f, "%"),
            Field("years", "Počet let", 1f, 40f, 1f, 20f, "let"),
        ),
    ) { v, _ ->
        val r = InvestmentCalculator.inflationRealValue(v["amount"]!!.toDouble(), v["inflation"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Reálná kupní síla", fmtCzk(r.realValue)),
            CalcResult("Ztráta kupní sily", fmtCzk(r.lossOfPurchasingPower)),
        )
    },

    // ── DŮCHOD A FINANČNÍ NEZÁVISLOST ────────────────────────────────
    CalcSpec(
        id = "fire",
        title = "FIRE číslo & měsíce do cíle",
        description = "Potřebné portfolio a čas k dosažení cílové částky.",
        icon = Icons.Filled.EmojiEvents,
        group = "Důchod a nezávislost",
        fields = listOf(
            Field("rent", "Požadovaný měsíční příjem", 10_000f, 200_000f, 5_000f, 40_000f, "Kč"),
            Field("swr", "Sazba výběru (SWR)", 2f, 6f, 0.1f, 4f, "%"),
            Field("principal", "Počáteční vklad", 0f, 5_000_000f, 10_000f, 500_000f, "Kč"),
            Field("pmt", "Měsíční úložka", 0f, 50_000f, 500f, 10_000f, "Kč"),
            Field("rate", "Roční zhodnocení", 0f, 15f, 0.1f, 6f, "%"),
        ),
        related = listOf("sporeni", "duchodova-mezera"),
    ) { v, _ ->
        val fire = InvestmentCalculator.fireNumber(v["rent"]!!.toDouble(), v["swr"]!!.toDouble())
        val months = InvestmentCalculator.monthsToTarget(v["principal"]!!.toDouble(), v["pmt"]!!.toDouble(), v["rate"]!!.toDouble(), fire)
        listOf(
            CalcResult("FIRE číslo", fmtCzk(fire)),
            CalcResult("Měsíce do cíle", if (months < 0) "nedosažitelné" else "${months} měs."),
        )
    },

    CalcSpec(
        id = "statni-duchod",
        title = "Odhad státního důchodu",
        description = "Orientační výpočet podle příjmů a let odpracování.",
        icon = Icons.Filled.AccountBalance,
        group = "Důchod a nezávislost",
        fields = listOf(
            Field("avgIncome", "Průměrný hrubý příjem (měs.)", 10_000f, 200_000f, 1_000f, 35_000f, "Kč"),
            Field("yearsWorked", "Roky odpracování", 10f, 45f, 1f, 35f, "let"),
        ),
        related = listOf("duchodova-mezera", "vek-odchodu"),
    ) { v, _ ->
        val r = StatePensionCalculator.calculate(v["avgIncome"]!!.toDouble(), v["yearsWorked"]!!.toInt())
        listOf(
            CalcResult("Odhad měsíčního důchodu", fmtCzk(r.estimatedMonthly)),
            CalcResult("Základní částka důchodu", fmtCzk(r.baseAmount)),
            CalcResult("Započítané roky", "%.0f let".format(r.yearsCounted)),
        )
    },

    CalcSpec(
        id = "vek-odchodu",
        title = "Věk odchodu do důchodu",
        description = "Podle data narození a počtu vychovaných dětí.",
        icon = Icons.Filled.CalendarMonth,
        group = "Důchod a nezávislost",
        fields = listOf(
            Field("birthYear", "Rok narození", 1950f, 2005f, 1f, 1980f, ""),
            Field("childrenRaised", "Vychované děti (do 4 let)", 0f, 4f, 1f, 2f, "dětí"),
        ),
    ) { v, _ ->
        val r = RetirementAgeCalculator.calculate(v["birthYear"]!!.toInt(), v["childrenRaised"]!!.toInt())
        listOf(
            CalcResult("Zákonný věk odchodu", "%.0f let".format(r.statutoryAge)),
            CalcResult("Věk po zohlednění dětí", "%.1f let".format(r.ageWithChildren)),
            CalcResult("Snížení o měsíce", "${r.monthsReduced} měs."),
        )
    },

    CalcSpec(
        id = "predcany-odlozeny",
        title = "Předčasný & odložený důchod",
        description = "Kolik se důchod krátí nebo navyšuje.",
        icon = Icons.Filled.FastForward,
        group = "Důchod a nezávislost",
        fields = listOf(
            Field("baseMonthly", "Důchod v zákonném věku (měs.)", 8_000f, 30_000f, 500f, 15_000f, "Kč"),
            Field("earlyYears", "Roků předčasně", 0f, 3f, 1f, 2f, "let"),
            Field("lateYears", "Roků odloženě", 0f, 10f, 1f, 3f, "let"),
        ),
    ) { v, _ ->
        val r = EarlyLatePensionCalculator.calculate(v["baseMonthly"]!!.toDouble(), v["earlyYears"]!!.toInt(), v["lateYears"]!!.toInt())
        listOf(
            CalcResult("Důchod při předčasném odchodu", fmtCzk(r.earlyMonthly)),
            CalcResult("Zkrácení o", "%.1f %%".format(r.reductionPct)),
            CalcResult("Důchod při odloženém odchodu", fmtCzk(r.lateMonthly)),
            CalcResult("Navýšení o", "%.1f %%".format(r.increasePct)),
        )
    },

    CalcSpec(
        id = "duchodova-mezera",
        title = "Důchodová mezera",
        description = "Kolik si naspořit pro udržení životní úrovně.",
        icon = Icons.Filled.CompareArrows,
        group = "Důchod a nezávislost",
        fields = listOf(
            Field("desiredMonthly", "Požadovaný příjem v důchodu (měs.)", 8_000f, 60_000f, 1_000f, 25_000f, "Kč"),
            Field("avgIncome", "Průměrný hrubý příjem (měs.)", 10_000f, 200_000f, 1_000f, 35_000f, "Kč"),
            Field("yearsWorked", "Roky odpracování", 10f, 45f, 1f, 35f, "let"),
        ),
        related = listOf("statni-duchod", "annuita"),
    ) { v, _ ->
        val r = PensionGapCalculator.calculate(v["desiredMonthly"]!!.toDouble(), v["avgIncome"]!!.toDouble(), v["yearsWorked"]!!.toInt())
        listOf(
            CalcResult("Odhad státního důchodu", fmtCzk(r.statePensionEstimate)),
            CalcResult("Měsíční mezera", fmtCzk(r.monthlyGap)),
            CalcResult("Portfolio pro pokrytí mezer (4 %)", fmtCzk(r.requiredPortfolio)),
        )
    },

    CalcSpec(
        id = "annuita",
        title = "Renta (anuita) — jak dlouho vydrží úspory",
        description = "Doba vyčerpání při daném výběru a výnosu.",
        icon = Icons.Filled.TimerOff,
        group = "Důchod a nezávislost",
        fields = listOf(
            Field("portfolio", "Naspořené peníze", 100_000f, 20_000_000f, 50_000f, 5_000_000f, "Kč"),
            Field("withdrawalMonthly", "Měsíční výběr", 0f, 30_000f, 500f, 15_000f, "Kč"),
            Field("annualReturnPct", "Roční výnos portfolia", 0f, 8f, 0.1f, 4f, "%"),
        ),
        related = listOf("duchodova-mezera", "fire"),
    ) { v, _ ->
        val r = AnnuityCalculator.calculate(v["portfolio"]!!.toDouble(), v["withdrawalMonthly"]!!.toDouble(), v["annualReturnPct"]!!.toDouble())
        listOf(
            CalcResult("Úspory vydrží", "%.1f let".format(r.durationYears)),
            CalcResult("Celkem vybráno do vyčerpání", fmtCzk(r.totalWithdrawn)),
        )
    },
)
