package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Work
import com.mirda.financnikalkulacky.calculators.work.DppDpcCalculator
import com.mirda.financnikalkulacky.calculators.work.MaternityCalculator
import com.mirda.financnikalkulacky.calculators.work.OsvcCalculator
import com.mirda.financnikalkulacky.calculators.work.SalaryCalculator
import com.mirda.financnikalkulacky.calculators.work.SickLeaveCalculator
import com.mirda.financnikalkulacky.calculators.work.WorkBenefitsCalculator
import com.mirda.financnikalkulacky.components.fmtCzk
import com.mirda.financnikalkulacky.ui.model.CalcResult
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.Field
import com.mirda.financnikalkulacky.ui.model.ToggleField

val WORK_SPECS: List<CalcSpec> = listOf(
    CalcSpec(
        id = "mzda",
        title = "Čistá mzda z hrubé",
        description = "Odvody, daň po slevách a bonus na děti.",
        icon = Icons.Filled.AccountBalanceWallet,
        fields = listOf(
            Field("gross", "Hrubá měsíční mzda", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("children", "Počet dětí", 0f, 5f, 1f, 0f, ""),
        ),
    ) { v, _ ->
        val r = SalaryCalculator.calculate(v["gross"]!!.toDouble(), v["children"]!!.toInt())
        listOf(
            CalcResult("Čistá mzda na ruce", fmtCzk(r.netMonthly)),
            CalcResult("Sociální pojištění (zam.)", fmtCzk(r.socialInsurance)),
            CalcResult("Zdravotní pojištění (zam.)", fmtCzk(r.healthInsurance)),
            CalcResult("Daň po slevách (rok)", fmtCzk(r.netTaxAnnual)),
            CalcResult("Bonus na děti (měs.)", if (r.childBonusMonthly > 0) fmtCzk(r.childBonusMonthly) else "—"),
            CalcResult("Náklady firmy (superhrubá)", fmtCzk(r.employerCost)),
        )
    },

    CalcSpec(
        id = "osvc",
        title = "OSVČ — paušál vs. přiznání",
        description = "Který daňový režim je výhodnější.",
        icon = Icons.Filled.Store,
        fields = listOf(
            Field("income", "Roční příjmy", 100_000f, 5_000_000f, 50_000f, 500_000f, "Kč"),
            Field("expense", "Paušální výdaje", 40f, 80f, 20f, 60f, "%"),
        ),
        toggles = listOf(
            ToggleField("band2", "Pásmo 2 (příjmy do 1 mil.) — jinak pásmo 1"),
            ToggleField("band3", "Pásmo 3 (příjmy nad 1 mil.)"),
        ),
    ) { v, t ->
        val band = if (t["band3"] == true) 3 else if (t["band2"] == true) 2 else 1
        val r = OsvcCalculator.calculate(v["income"]!!.toDouble(), v["expense"]!!.toDouble(), band)
        listOf(
            CalcResult("Paušální daň (rok)", fmtCzk(r.flatTaxAnnual)),
            CalcResult("Běžný režim celkem (rok)", fmtCzk(r.regularTotalAnnual)),
            CalcResult("Výhodnější režim", if (r.betterMode == "paušal") "Paušál" else "Přiznání"),
            CalcResult("Úspora lepšího režimu", fmtCzk(r.savings)),
        )
    },

    CalcSpec(
        id = "dpp",
        title = "DPP / DPČ — čistá odměna",
        description = "Odvody, daň a náklady firmy na smlouvě.",
        icon = Icons.Filled.LocalAtm,
        fields = listOf(
            Field("gross", "Hrubá měsíční odměna", 1_000f, 100_000f, 500f, 10_000f, "Kč"),
        ),
        toggles = listOf(ToggleField("decl", "Prohlášení o zdanění (růžový formulář)")),
    ) { v, t ->
        val r = DppDpcCalculator.calculate(v["gross"]!!.toDouble(), t["decl"] == true)
        listOf(
            CalcResult("Čistě vyplacené", fmtCzk(r.netAmount)),
            CalcResult("Sražená daň", fmtCzk(r.taxWithheld)),
            CalcResult("Odvody zaměstnance", if (r.fullContributions) fmtCzk(r.employeeSocial + r.employeeHealth) else "0 Kč (do limitu 4 100 Kč)"),
            CalcResult("Náklady firmy", fmtCzk(r.employerCost)),
        )
    },


    CalcSpec(
        id = "nemocenska",
        title = "Nemocenská & náhrada mzdy",
        description = "Redukovaný DVZ, náhrada a státní nemocenská.",
        icon = Icons.Filled.LocalHospital,
        fields = listOf(
            Field("gross", "Průměrný hrubý příjem (měs.)", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("days", "Dny pracovní neschopnosti", 1f, 180f, 1f, 45f, "dnů"),
        ),
    ) { v, _ ->
        val r = SickLeaveCalculator.calculate(v["gross"]!!.toDouble(), v["days"]!!.toInt())
        listOf(
            CalcResult("Redukovaný DVZ (den)", fmtCzk(r.dailyBaseReduced)),
            CalcResult("Náhrada mzdy (1.–14. den)", fmtCzk(r.wageCompensation14d)),
            CalcResult("Nemocenská od ČSSZ", fmtCzk(r.sickBenefitState)),
            CalcResult("Celkem vyplacené", fmtCzk(r.totalReceived)),
            CalcResult("Výpadek oproti mzdě", fmtCzk(r.incomeLoss)),
        )
    },

    CalcSpec(
        id = "mateřska",
        title = "Mateřská & rodičovský příspěvek",
        description = "PPM za 28 týdnů a celkem do zvoleného věku.",
        icon = Icons.Filled.ChildCare,
        fields = listOf(
            Field("gross", "Hrubá měsíční mzda", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("parental", "Celková částka rodičovského", 100_000f, 400_000f, 10_000f, 340_000f, "Kč"),
        ),
        toggles = listOf(
            ToggleField("age3", "Čerpat do 3 let — jinak do 2 let"),
            ToggleField("age4", "Čerpat do 4 let"),
        ),
    ) { v, t ->
        val age = if (t["age4"] == true) 4 else if (t["age3"] == true) 3 else 2
        val r = MaternityCalculator.calculate(v["gross"]!!.toDouble(), v["parental"]!!.toDouble(), age)
        listOf(
            CalcResult("Měsíční PPM", fmtCzk(r.ppmMonthly)),
            CalcResult("PPM celkem (28 týdnů)", fmtCzk(r.ppmTotal28w)),
            CalcResult("Rodičovský příspěvek (měs.)", fmtCzk(r.parentalBenefitMonthly)),
            CalcResult("Celkem do ${age} let", fmtCzk(r.totalUntilAge)),
        )
    },


    CalcSpec(
        id = "odstupne",
        title = "Odstupné (§ 67)",
        description = "Násobek průměrného výdělku a čistá částka.",
        icon = Icons.Filled.PersonOff,
        fields = listOf(
            Field("earning", "Průměrný měsíční výdělek", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("years", "Délka zaměstnání (roky)", 0.5f, 40f, 0.5f, 5f, ""),
        ),
    ) { v, _ ->
        val r = WorkBenefitsCalculator.severance(v["earning"]!!.toDouble(), v["years"]!!.toDouble())
        listOf(
            CalcResult("Násobek výdělku", "${r.multiplier}×"),
            CalcResult("Hrubé odstupné", fmtCzk(r.grossSeverance)),
            CalcResult("Čisté odstupné (po 15 % dani)", fmtCzk(r.netSeverance)),
        )
    },

    CalcSpec(
        id = "nezamestnanost",
        title = "Podpora v nezaměstnanosti",
        description = "Měsíční průběh podpory podle věku a způsobu ukončení.",
        icon = Icons.Filled.Work,
        fields = listOf(
            Field("earning", "Průměrný čistý výdělek (měs.)", 5_000f, 100_000f, 500f, 20_000f, "Kč"),
            Field("age", "Věk", 18f, 70f, 1f, 40f, "let"),
        ),
        toggles = listOf(ToggleField("noCause", "Dohoda bez vážného důvodu (45 % po celou dobu)")),
    ) { v, t ->
        val r = WorkBenefitsCalculator.unemployment(v["earning"]!!.toDouble(), v["age"]!!.toInt(), t["noCause"] == true)
        listOf(
            CalcResult("Podpůrná doba", "${r.months} měsíců"),
            CalcResult("První měsíc", fmtCzk(r.monthlyAmounts.firstOrNull() ?: 0.0)),
            CalcResult("Celkem za celou dobu", fmtCzk(r.totalPaid)),
        )
    },
)
