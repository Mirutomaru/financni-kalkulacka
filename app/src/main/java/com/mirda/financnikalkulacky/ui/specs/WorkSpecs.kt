package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import com.mirda.financnikalkulacky.calculators.work.AnnualTaxSettlementCalculator
import com.mirda.financnikalkulacky.calculators.work.CareAllowanceCalculator
import com.mirda.financnikalkulacky.calculators.work.ChildBenefitsCalculator
import com.mirda.financnikalkulacky.calculators.work.ChildBonusCalculator
import com.mirda.financnikalkulacky.calculators.work.DepreciationCalculator
import com.mirda.financnikalkulacky.calculators.work.DisabilityPensionCalculator
import com.mirda.financnikalkulacky.calculators.work.DppDpcCalculator
import com.mirda.financnikalkulacky.calculators.work.EmploymentCompareCalculator
import com.mirda.financnikalkulacky.calculators.work.HourlyRateCalculator
import com.mirda.financnikalkulacky.calculators.work.MaternityCalculator
import com.mirda.financnikalkulacky.calculators.work.OvertimeCalculator
import com.mirda.financnikalkulacky.calculators.work.OvcAdvanceCalculator
import com.mirda.financnikalkulacky.calculators.work.OsvcCalculator
import com.mirda.financnikalkulacky.calculators.work.PaternityCalculator
import com.mirda.financnikalkulacky.calculators.work.ReverseSalaryCalculator
import com.mirda.financnikalkulacky.calculators.work.SalaryCalculator
import com.mirda.financnikalkulacky.calculators.work.SickLeaveCalculator
import com.mirda.financnikalkulacky.calculators.work.SurvivorPensionCalculator
import com.mirda.financnikalkulacky.calculators.work.TravelExpenseCalculator
import com.mirda.financnikalkulacky.calculators.work.VacationCalculator
import com.mirda.financnikalkulacky.calculators.work.VatCalculator
import com.mirda.financnikalkulacky.calculators.work.WageValorizationCalculator
import com.mirda.financnikalkulacky.calculators.work.WorkBenefitsCalculator
import com.mirda.financnikalkulacky.calculators.YearParams
import com.mirda.financnikalkulacky.components.fmtCzk
import com.mirda.financnikalkulacky.components.fmtNum
import com.mirda.financnikalkulacky.ui.model.CalcResult
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.Field
import com.mirda.financnikalkulacky.ui.model.ToggleField

val WORK_SPECS: List<CalcSpec> = listOf(
    // ── MZDA A ZAMĚSTNÁNÍ ─────────────────────────────────────────────
    CalcSpec(
        id = "mzda",
        title = "Čistá mzda z hrubé",
        description = "Odvody, daň po slevách a bonus na děti.",
        icon = Icons.Filled.AccountBalanceWallet,
        group = "Mzda a zaměstnání",
        fields = listOf(
            Field("gross", "Hrubá měsíční mzda", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("children", "Počet dětí", 0f, 5f, 1f, 0f, ""),
        ),
        related = listOf("obraceda-mzda", "dpp", "prescasy", "dovolena"),
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
        id = "obraceda-mzda",
        title = "Obrácená mzda — kolik hrubé?",
        description = "Jakou hrubou potřebuji pro požadovanou čistou.",
        icon = Icons.Filled.SwapVert,
        group = "Mzda a zaměstnání",
        fields = listOf(
            Field("targetNet", "Požadovaná čistá mzda", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("children", "Počet dětí", 0f, 5f, 1f, 0f, ""),
        ),
        related = listOf("mzda"),
    ) { v, _ ->
        val r = ReverseSalaryCalculator.calculate(v["targetNet"]!!.toDouble(), v["children"]!!.toInt())
        listOf(
            CalcResult("Potřebná hrubá mzda", fmtCzk(r.grossMonthly)),
            CalcResult("Čistá na ruce (ověření)", fmtCzk(r.netMonthly)),
            CalcResult("Sociální pojištění", fmtCzk(r.socialInsurance)),
            CalcResult("Zdravotní pojištění", fmtCzk(r.healthInsurance)),
            CalcResult("Daň (rok)", fmtCzk(r.taxAnnual)),
        )
    },

    CalcSpec(
        id = "prescasy",
        title = "Přesčasy & příplatky",
        description = "Víkend +100 %, noc +35 %, svátek +100 %.",
        icon = Icons.Filled.FastForward,
        group = "Mzda a zaměstnání",
        fields = listOf(
            Field("hourlyWage", "Hodinová mzda", 100f, 5_000f, 50f, 150f, "Kč"),
            Field("weekendHours", "Přesčasy o víkendu (h)", 0f, 40f, 1f, 4f, ""),
            Field("nightHours", "Noční směny (h)", 0f, 80f, 2f, 8f, ""),
            Field("holidayHours", "Práce ve svátek (h)", 0f, 50f, 1f, 2f, ""),
        ),
    ) { v, _ ->
        val r = OvertimeCalculator.calculate(
            hourlyWage = v["hourlyWage"]!!.toDouble(),
            weekendHours = v["weekendHours"]!!.toInt(),
            nightHours = v["nightHours"]!!.toInt(),
            holidayHours = v["holidayHours"]!!.toInt(),
        )
        listOf(
            CalcResult("Příplatek víkend (hrubé)", fmtCzk(r.overtimeWeekendGross)),
            CalcResult("Příplatek noc (hrubé)", fmtCzk(r.overtimeNightGross)),
            CalcResult("Příplatek svátek (hrubé)", fmtCzk(r.overtimeHolidayGross)),
            CalcResult("Celkem příplatky (hrubé)", fmtCzk(r.totalExtraGross)),
            CalcResult("Čistě na ruce", fmtCzk(r.netExtraMonthly)),
        )
    },

    CalcSpec(
        id = "dovolena",
        title = "Dovolená — nárok a platba",
        description = "Počet dní dle délky zaměstnání a denní sazba.",
        icon = Icons.Filled.EmojiEvents,
        group = "Mzda a zaměstnání",
        fields = listOf(
            Field("gross", "Hrubá měsíční mzda", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("yearsEmployed", "Délka u zaměstnavatele (roky)", 1f, 40f, 1f, 3f, ""),
        ),
    ) { v, _ ->
        val r = VacationCalculator.calculate(v["gross"]!!.toDouble(), v["yearsEmployed"]!!.toInt())
        listOf(
            CalcResult("Nárok na dovolenou", "${r.daysEntitled} dní"),
            CalcResult("Průměrný denní výdělek", fmtCzk(r.dailyRate)),
            CalcResult("Hodnota celé dovolené", fmtCzk(r.vacationPayTotal)),
        )
    },

    CalcSpec(
        id = "cestovne",
        title = "Stravenky & cestovní náhrady",
        description = "Stravenkový paušál a jízdné za služební jízdy.",
        icon = Icons.Filled.AttachMoney,
        group = "Mzda a zaměstnání",
        fields = listOf(
            Field("workDays", "Pracovní dny v měsíci", 10f, 25f, 1f, 21f, ""),
            Field("kmPerMonth", "Služební jízdy (km/měs.)", 0f, 3_000f, 50f, 300f, ""),
        ),
    ) { v, _ ->
        val r = TravelExpenseCalculator.calculate(v["workDays"]!!.toInt(), v["kmPerMonth"]!!.toDouble())
        listOf(
            CalcResult("Stravenky (měs.)", fmtCzk(r.mealAllowanceMonthly)),
            CalcResult("Jízdné (rok)", fmtCzk(r.travelPerKmAnnual)),
            CalcResult("Celkem (rok, hrubé)", fmtCzk(r.totalAnnualGross)),
            CalcResult("Čistě (osvobozeno od daně)", fmtCzk(r.netAfterTax)),
        )
    },

    CalcSpec(
        id = "valorizace",
        title = "Valorizace mzdy vs. inflace",
        description = "Reálný růst mzdy a ztráta kupní síly.",
        icon = Icons.Filled.TrendingUp,
        group = "Mzda a zaměstnání",
        fields = listOf(
            Field("currentNet", "Aktuální čistá mzda", 10_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("raisePct", "Navýšení mzdy", 0f, 30f, 0.5f, 5f, "%"),
            Field("inflationPct", "Inflace", 0f, 15f, 0.5f, 3f, "%"),
        ),
    ) { v, _ ->
        val r = WageValorizationCalculator.calculate(
            currentNet = v["currentNet"]!!.toDouble(),
            nominalRaisePct = v["raisePct"]!!.toDouble(),
            inflationPct = v["inflationPct"]!!.toDouble(),
        )
        listOf(
            CalcResult("Reálný růst mzdy", "%.1f %%".format(r.realGrowthPct)),
            CalcResult("Ztráta kupní síly", if (r.purchasingPowerLossPct > 0) "%.1f %%" .format(r.purchasingPowerLossPct) else "—"),
            CalcResult("Čistá po inflaci (ekvivalent)", fmtCzk(r.netAfterInflation)),
        )
    },

    CalcSpec(
        id = "dpp",
        title = "DPP / DPČ — čistá odměna",
        description = "Odvody, daň a náklady firmy na smlouvě.",
        icon = Icons.Filled.LocalAtm,
        group = "Mzda a zaměstnání",
        fields = listOf(
            Field("gross", "Hrubá měsíční odměna", 1_000f, 100_000f, 500f, 10_000f, "Kč"),
        ),
        toggles = listOf(
            ToggleField("decl", "Prohlášení o zdanění (růžový formulář)"),
            ToggleField("year2025", "Sazby roku 2025 — jinak aktuální 2026"),
        ),
        related = listOf("mzda", "osvc"),
    ) { v, t ->
        val p = if (t["year2025"] == true) YearParams.YEAR_2025 else YearParams.YEAR_2026
        val r = DppDpcCalculator.calculate(v["gross"]!!.toDouble(), t["decl"] == true, p)
        listOf(
            CalcResult("Čistě vyplacené", fmtCzk(r.netAmount)),
            CalcResult("Sražená daň", fmtCzk(r.taxWithheld)),
            CalcResult("Odvody zaměstnance", if (r.fullContributions) fmtCzk(r.employeeSocial + r.employeeHealth) else "0 Kč (do limitu ${fmtNum(p.dpcContributionLimit)} Kč)"),
            CalcResult("Náklady firmy", fmtCzk(r.employerCost)),
        )
    },

    CalcSpec(
        id = "odstupne",
        title = "Odstupné (§ 67)",
        description = "Násobek průměrného výdělku a čistá částka.",
        icon = Icons.Filled.PersonOff,
        group = "Mzda a zaměstnání",
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

    // ── OSVČ A PODNIKÁNÍ ───────────────────────────────────────────────
    CalcSpec(
        id = "osvc",
        title = "OSVČ — paušál vs. přiznání",
        description = "Který daňový režim je výhodnější.",
        icon = Icons.Filled.Store,
        group = "OSVČ a podnikání",
        fields = listOf(
            Field("income", "Roční příjmy", 100_000f, 5_000_000f, 50_000f, 500_000f, "Kč"),
            Field("expense", "Paušální výdaje", 40f, 80f, 20f, 60f, "%"),
        ),
        toggles = listOf(
            ToggleField("band2", "Pásmo 2 (příjmy do 1 mil.) — jinak pásmo 1"),
            ToggleField("band3", "Pásmo 3 (příjmy nad 1 mil.)"),
            ToggleField("year2025", "Sazby roku 2025 — jinak aktuální 2026"),
        ),
        related = listOf("srovnanie-formy", "dph", "hodinova-sazba"),
    ) { v, t ->
        val band = if (t["band3"] == true) 3 else if (t["band2"] == true) 2 else 1
        val p = if (t["year2025"] == true) YearParams.YEAR_2025 else YearParams.YEAR_2026
        val r = OsvcCalculator.calculate(v["income"]!!.toDouble(), v["expense"]!!.toDouble(), band, p)
        listOf(
            CalcResult("Paušální daň (rok)", fmtCzk(r.flatTaxAnnual)),
            CalcResult("Běžný režim celkem (rok)", fmtCzk(r.regularTotalAnnual)),
            CalcResult("Výhodnější režim", if (r.betterMode == "paušal") "Paušál" else "Přiznání"),
            CalcResult("Úspora lepšího režimu", fmtCzk(r.savings)),
        )
    },

    CalcSpec(
        id = "osvc-zahlohy",
        title = "Zálohy na pojištění OSVČ",
        description = "Sociálka a zdravotní — kolik měsíčně odvést.",
        icon = Icons.Filled.Payments,
        group = "OSVČ a podnikání",
        fields = listOf(
            Field("monthlyIncome", "Měsíční příjem", 10_000f, 500_000f, 5_000f, 50_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = OvcAdvanceCalculator.calculate(v["monthlyIncome"]!!.toDouble())
        listOf(
            CalcResult("Sociální pojištění (měs.)", fmtCzk(r.socialMonthly)),
            CalcResult("Zdravotní pojištění (měs.)", fmtCzk(r.healthMonthly)),
            CalcResult("Celkem (měs.)", fmtCzk(r.totalMonthly)),
            CalcResult("Celkem (rok)", fmtCzk(r.totalAnnual)),
        )
    },

    CalcSpec(
        id = "srovnanie-formy",
        title = "Zaměstnanec × OSVČ × s.r.o.",
        description = "Která forma zaměstnání přinese víc čistého.",
        icon = Icons.Filled.Compare,
        group = "OSVČ a podnikání",
        fields = listOf(
            Field("income", "Měsíční příjem", 15_000f, 300_000f, 5_000f, 60_000f, "Kč"),
            Field("expensePct", "Náklady / paušál", 40f, 80f, 5f, 60f, "%"),
        ),
    ) { v, _ ->
        val r = EmploymentCompareCalculator.calculate(v["income"]!!.toDouble(), v["expensePct"]!!.toDouble())
        listOf(
            CalcResult("Zaměstnanec (čisté/měs.)", fmtCzk(r.employeeNet)),
            CalcResult("OSVČ (čisté/měs.)", fmtCzk(r.ovcNet)),
            CalcResult("S.r.o. (dividendy/měs.)", fmtCzk(r.sroNet)),
            CalcResult("Nejvýhodnější forma", r.bestMode),
        )
    },

    CalcSpec(
        id = "dph",
        title = "DPH — výpočet a registrace",
        description = "Daň z přidané hodnoty a hranice povinné registrace.",
        icon = Icons.Filled.Percent,
        group = "OSVČ a podnikání",
        fields = listOf(
            Field("price", "Cena bez DPH", 100f, 500_000f, 100f, 10_000f, "Kč"),
            Field("turnover", "Roční obrat", 100_000f, 5_000_000f, 100_000f, 800_000f, "Kč"),
        ),
        toggles = listOf(
            ToggleField("rate12", "Sazba 12 % (strava, knihy)"),
            ToggleField("rate5", "Sazba 5 % (základní potraviny)"),
        ),
    ) { v, t ->
        val rate = if (t["rate5"] == true) 5.0 else if (t["rate12"] == true) 12.0 else 21.0
        val r = VatCalculator.calculate(priceWithoutVat = v["price"]!!.toDouble(), ratePct = rate, annualTurnover = v["turnover"]!!.toDouble())
        listOf(
            CalcResult("DPH", fmtCzk(r.vatAmount)),
            CalcResult("Cena s DPH", fmtCzk(r.priceWithVat)),
            CalcResult("Registrace povinná (obrat ≥ 1 mil.)", if (r.registrationThresholdReached) "ANO" else "ne"),
        )
    },

    CalcSpec(
        id = "hodinova-sazba",
        title = "Hodinová sazba OSVČ",
        description = "Jakou sazbu si účtovat pro ekvivalent mzdy.",
        icon = Icons.Filled.HourglassBottom,
        group = "OSVČ a podnikání",
        fields = listOf(
            Field("targetGrossSalary", "Cíl — ekvivalent hrubé mzdy", 20_000f, 150_000f, 1_000f, 40_000f, "Kč"),
            Field("hoursPerMonth", "Pracovní hodiny (měs.)", 40f, 160f, 8f, 120f, ""),
        ),
    ) { v, _ ->
        val r = HourlyRateCalculator.calculate(v["targetGrossSalary"]!!.toDouble(), v["hoursPerMonth"]!!.toInt())
        listOf(
            CalcResult("Doporučená hodinová sazba", fmtCzk(r.hourlyRateGross)),
            CalcResult("Měsíční obrat při této sazbě", fmtCzk(r.monthlyRevenueAtRate)),
            CalcResult("Čistě po odvodech a dani (měs.)", fmtCzk(r.netAfterContributionsTax)),
        )
    },

    CalcSpec(
        id = "odpisy",
        title = "Odpisy majetku",
        description = "Lineární odpis a daňová úspora.",
        icon = Icons.Filled.TrendingFlat,
        group = "OSVČ a podnikání",
        fields = listOf(
            Field("assetValue", "Cena majetku", 5_000f, 2_000_000f, 5_000f, 100_000f, "Kč"),
            Field("usefulLifeYears", "Životnost (roky)", 3f, 10f, 1f, 5f, ""),
        ),
    ) { v, _ ->
        val r = DepreciationCalculator.calculate(v["assetValue"]!!.toDouble(), v["usefulLifeYears"]!!.toInt())
        listOf(
            CalcResult("Roční odpis", fmtCzk(r.annualDepreciation)),
            CalcResult("Daňová úspora (rok)", fmtCzk(r.taxSavingAnnual)),
            CalcResult("Úspora za celou životnost", fmtCzk(r.totalTaxSavingOverLife)),
        )
    },

    // ── DANĚ ───────────────────────────────────────────────────────────
    CalcSpec(
        id = "rocni-zuctovani",
        title = "Roční zúčtování daně",
        description = "Odpočty (hypotéka, penze) a slevy — přeplatek či nedoplatek.",
        icon = Icons.Filled.Gavel,
        group = "Daně",
        fields = listOf(
            Field("annualIncome", "Roční příjem", 100_000f, 2_000_000f, 10_000f, 480_000f, "Kč"),
            Field("withheldTax", "Stržené zálohy na daň (rok)", 0f, 300_000f, 5_000f, 60_000f, "Kč"),
            Field("mortgageInterest", "Úroky z hypotéky", 0f, 100_000f, 2_000f, 40_000f, "Kč"),
            Field("pensionSavings", "Penzijní spoření / DIP", 0f, 50_000f, 2_000f, 24_000f, "Kč"),
        ),
        toggles = listOf(
            ToggleField("spouseDeduction", "Sleva na manžela/manželku (bez příjmu)"),
            ToggleField("studentDeduction", "Sleva na studenta do 25 let"),
            ToggleField("disability1", "Invalidita II. stupeň"),
            ToggleField("disability2", "Invalidita III. stupeň"),
        ),
    ) { v, t ->
        val disability = if (t["disability2"] == true) 2 else if (t["disability1"] == true) 1 else 0
        val r = AnnualTaxSettlementCalculator.calculate(
            annualIncome = v["annualIncome"]!!.toDouble(),
            withheldTax = v["withheldTax"]!!.toDouble(),
            mortgageInterest = v["mortgageInterest"]!!.toDouble(),
            pensionSavings = v["pensionSavings"]!!.toDouble(),
            spouseDeduction = t["spouseDeduction"] == true,
            studentDeduction = t["studentDeduction"] == true,
            disabilityDeduction = disability,
        )
        listOf(
            CalcResult("Daň bez odpočtů (rok)", fmtCzk(r.taxBeforeDeductions)),
            CalcResult("Odpočty ze základu", fmtCzk(r.totalDeductions)),
            CalcResult("Slevy na dani", fmtCzk(r.totalSlevy)),
            CalcResult("Daň po zúčtování (rok)", fmtCzk(r.finalTaxAnnual)),
            CalcResult("Přeplatek / nedoplatek", if (r.refundOrOwed >= 0) "vrátí se " + fmtCzk(r.refundOrOwed) else "nedoplatí se " + fmtCzk(-r.refundOrOwed)),
        )
    },

    CalcSpec(
        id = "bonus-na-deti",
        title = "Daňový bonus na děti",
        description = "Roční částka dle pořadí dítěte.",
        icon = Icons.Filled.ChildCare,
        group = "Daně",
        fields = listOf(
            Field("children", "Počet dětí", 0f, 5f, 1f, 2f, ""),
        ),
    ) { v, _ ->
        val r = ChildBonusCalculator.calculate(v["children"]!!.toInt())
        listOf(
            CalcResult("1. dítě (rok)", fmtCzk(r.child1Annual)),
            CalcResult("2. dítě (rok)", fmtCzk(r.child2Annual)),
            CalcResult("3. a další (rok)", fmtCzk(r.child3PlusAnnual)),
            CalcResult("Celkem (rok)", fmtCzk(r.totalAnnual)),
            CalcResult("Ekvivalent měsíčně", fmtCzk(r.monthlyEquivalent)),
        )
    },

    // ── SOCIÁLNÍ DÁVKY ────────────────────────────────────────────────
    CalcSpec(
        id = "nemocenska",
        title = "Nemocenská & náhrada mzdy",
        description = "Redukovaný DVZ, náhrada a státní nemocenská.",
        icon = Icons.Filled.LocalHospital,
        group = "Sociální dávky",
        fields = listOf(
            Field("gross", "Průměrný hrubý příjem (měs.)", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("days", "Dny pracovní neschopnosti", 1f, 180f, 1f, 45f, "dnů"),
        ),
        toggles = listOf(ToggleField("year2025", "Sazby roku 2025 — jinak aktuální 2026")),
        related = listOf("mateřska", "osetrowne"),
    ) { v, t ->
        val p = if (t["year2025"] == true) YearParams.YEAR_2025 else YearParams.YEAR_2026
        val r = SickLeaveCalculator.calculate(v["gross"]!!.toDouble(), v["days"]!!.toInt(), p)
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
        group = "Sociální dávky",
        fields = listOf(
            Field("gross", "Hrubá měsíční mzda", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("parental", "Celková částka rodičovského", 100_000f, 400_000f, 10_000f, 340_000f, "Kč"),
        ),
        toggles = listOf(
            ToggleField("age3", "Čerpat do 3 let — jinak do 2 let"),
            ToggleField("age4", "Čerpat do 4 let"),
            ToggleField("year2025", "Sazby roku 2025 — jinak aktuální 2026"),
        ),
        related = listOf("nemocenska", "miminko", "pridavky"),
    ) { v, t ->
        val age = if (t["age4"] == true) 4 else if (t["age3"] == true) 3 else 2
        val p = if (t["year2025"] == true) YearParams.YEAR_2025 else YearParams.YEAR_2026
        val r = MaternityCalculator.calculate(v["gross"]!!.toDouble(), v["parental"]!!.toDouble(), age, p)
        listOf(
            CalcResult("Měsíční PPM", fmtCzk(r.ppmMonthly)),
            CalcResult("PPM celkem (28 týdnů)", fmtCzk(r.ppmTotal28w)),
            CalcResult("Rodičovský příspěvek (měs.)", fmtCzk(r.parentalBenefitMonthly)),
            CalcResult("Celkem do ${age} let", fmtCzk(r.totalUntilAge)),
        )
    },

    CalcSpec(
        id = "nezamestnanost",
        title = "Podpora v nezaměstnanosti",
        description = "Měsíční průběh podpory podle věku a způsobu ukončení.",
        icon = Icons.Filled.Work,
        group = "Sociální dávky",
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

    CalcSpec(
        id = "otcovska",
        title = "Otcovská poporodní péče",
        description = "2 týdny, 70 % denního vyměřovacího základu.",
        icon = Icons.Filled.Groups,
        group = "Sociální dávky",
        fields = listOf(
            Field("gross", "Hrubá měsíční mzda", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
        ),
        related = listOf("mateřska"),
    ) { v, _ ->
        val r = PaternityCalculator.calculate(v["gross"]!!.toDouble())
        listOf(
            CalcResult("Týdenní částka", fmtCzk(r.weeklyAmount)),
            CalcResult("Celkem za 2 týdny", fmtCzk(r.totalTwoWeeks)),
        )
    },

    CalcSpec(
        id = "osetrowne",
        title = "Ošetřovné (péče o dítě)",
        description = "60 % do 9. dne, pak 70 % denního základu.",
        icon = Icons.Filled.Shield,
        group = "Sociální dávky",
        fields = listOf(
            Field("gross", "Hrubá měsíční mzda", 5_000f, 200_000f, 1_000f, 30_000f, "Kč"),
            Field("days", "Dny ošetřovného", 1f, 60f, 1f, 14f, ""),
        ),
        related = listOf("nemocenska", "mateřska"),
    ) { v, _ ->
        val r = CareAllowanceCalculator.calculate(v["gross"]!!.toDouble(), v["days"]!!.toInt())
        listOf(
            CalcResult("Redukovaný DVZ (den)", fmtCzk(r.dailyBaseReduced)),
            CalcResult("Průměrná sazba", "%.0f %%".format(r.ratePct)),
            CalcResult("Denní dávka", fmtCzk(r.dailyBenefit)),
            CalcResult("Celkem za ${v["days"]!!.toInt()} dní", fmtCzk(r.totalForDays)),
        )
    },

    CalcSpec(
        id = "invalidni-duchod",
        title = "Invalidní důchod",
        description = "Výše dle stupně invalidity a průměrného výdělku.",
        icon = Icons.Filled.Groups,
        group = "Sociální dávky",
        fields = listOf(
            Field("averageEarning", "Průměrný měsíční výdělek", 10_000f, 200_000f, 1_000f, 30_000f, "Kč"),
        ),
        toggles = listOf(
            ToggleField("deg1", "I. stupeň (40 % výdělku)"),
            ToggleField("deg3", "III. stupeň (80 % výdělku)"),
        ),
    ) { v, t ->
        val degree = if (t["deg3"] == true) 3 else if (t["deg1"] == true) 1 else 2
        val r = DisabilityPensionCalculator.calculate(v["averageEarning"]!!.toDouble(), degree)
        listOf(
            CalcResult("Stupeň invalidity", "II. stupeň".let { if (r.degree == 1) "I. stupeň" else if (r.degree == 3) "III. stupeň" else it }),
            CalcResult("Důchod (měs.)", fmtCzk(r.pensionMonthly)),
            CalcResult("Celkem (rok)", fmtCzk(r.annualTotal)),
        )
    },

    CalcSpec(
        id = "pozustalostni",
        title = "Pozůstalostní důchody",
        description = "Vdova/vdovec a sirotci — výše a celkem v domácnosti.",
        icon = Icons.Filled.Flag,
        group = "Sociální dávky",
        fields = listOf(
            Field("pension", "Důchod pojištěného (měs.)", 10_000f, 80_000f, 2_000f, 30_000f, "Kč"),
            Field("orphans", "Počet sirotků", 0f, 3f, 1f, 1f, ""),
        ),
        toggles = listOf(ToggleField("widow", "Vdova / vdovec")),
    ) { v, t ->
        val r = SurvivorPensionCalculator.calculate(v["pension"]!!.toDouble(), t["widow"] == true, v["orphans"]!!.toInt())
        listOf(
            CalcResult("Vdova/vdovec (měs.)", fmtCzk(r.widowWidowerMonthly)),
            CalcResult("Sirotek — každý (měs.)", fmtCzk(r.orphanMonthly)),
            CalcResult("Celkem v domácnosti (měs.)", fmtCzk(r.totalHouseholdMonthly)),
        )
    },

    CalcSpec(
        id = "pridavky",
        title = "Přídavek, péče a bydlení",
        description = "Státní příspěvky na dítě a domácnost.",
        icon = Icons.Filled.House,
        group = "Sociální dávky",
        fields = listOf(
            Field("childAgeYears", "Věk dítěte", 0f, 15f, 1f, 3f, "let"),
            Field("householdIncomeMonthly", "Příjem domácnosti (měs.)", 5_000f, 80_000f, 1_000f, 25_000f, "Kč"),
            Field("housingCostMonthly", "Náklady na bydlení (měs.)", 2_000f, 30_000f, 500f, 8_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = ChildBenefitsCalculator.calculate(
            childAgeYears = v["childAgeYears"]!!.toInt(),
            householdIncomeMonthly = v["householdIncomeMonthly"]!!.toDouble(),
            housingCostMonthly = v["housingCostMonthly"]!!.toDouble(),
        )
        listOf(
            CalcResult("Přídavek na dítě (měs.)", fmtCzk(r.childAllowanceMonthly)),
            CalcResult("Příspěvek na péči (měs.)", fmtCzk(r.careContributionMonthly)),
            CalcResult("Příspěvek na bydlení (měs.)", fmtCzk(r.housingBenefitMonthly)),
            CalcResult("Celkem (měs.)", fmtCzk(r.totalMonthly)),
        )
    },
)
