package com.mirda.financnikalkulacky.ui.specs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.School
import com.mirda.financnikalkulacky.calculators.family.BabyCostCalculator
import com.mirda.financnikalkulacky.calculators.family.BereavementBenefitCalculator
import com.mirda.financnikalkulacky.calculators.family.ChildSupportCalculator
import com.mirda.financnikalkulacky.calculators.family.DivorcePropertySplitCalculator
import com.mirda.financnikalkulacky.calculators.family.FuneralCostCalculator
import com.mirda.financnikalkulacky.calculators.family.GiftVsInheritanceCalculator
import com.mirda.financnikalkulacky.calculators.family.ForcedShareCalculator
import com.mirda.financnikalkulacky.calculators.family.InheritanceProceedingsCalculator
import com.mirda.financnikalkulacky.calculators.family.SeniorCareCalculator
import com.mirda.financnikalkulacky.calculators.family.StudyCostCalculator
import com.mirda.financnikalkulacky.calculators.family.VacationTripCalculator
import com.mirda.financnikalkulacky.calculators.family.WeddingCalculator
import com.mirda.financnikalkulacky.components.fmtCzk
import com.mirda.financnikalkulacky.ui.model.CalcResult
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.Field
import com.mirda.financnikalkulacky.ui.model.ToggleField

val FAMILY_SPECS: List<CalcSpec> = listOf(
    // ── RODINNÉ A ŽIVOTNÍ UDÁLOSTI ────────────────────────────────────
    CalcSpec(
        id = "svatba",
        title = "Rozpočet svatby",
        description = "Celkové náklady, cena na hosta a spoření.",
        icon = Icons.Filled.Celebration,
        group = "Rodinné události",
        fields = listOf(
            Field("budget", "Rozpočet na svatbu", 50_000f, 2_000_000f, 10_000f, 300_000f, "Kč"),
            Field("guests", "Počet hostů", 1f, 300f, 1f, 80f, ""),
            Field("monthsToSave", "Měsíců na spoření", 1f, 60f, 1f, 12f, ""),
        ),
    ) { v, _ ->
        val r = WeddingCalculator.calculate(v["budget"]!!.toDouble(), v["guests"]!!.toInt(), v["monthsToSave"]!!.toInt())
        listOf(
            CalcResult("Celkové náklady", fmtCzk(r.totalCost)),
            CalcResult("Cena na hosta", fmtCzk(r.costPerGuest)),
            CalcResult("Měsíční spoření", fmtCzk(r.monthlySaving)),
        )
    },

    CalcSpec(
        id = "studium",
        title = "Náklady na studium",
        description = "Školné + životní náklady, celkem a spoření.",
        icon = Icons.Filled.School,
        group = "Rodinné události",
        fields = listOf(
            Field("tuitionMonthly", "Měsíční školné", 0f, 50_000f, 500f, 10_000f, "Kč"),
            Field("livingMonthly", "Životní náklady (měs.)", 5_000f, 40_000f, 500f, 12_000f, "Kč"),
            Field("yearsOfStudy", "Délba studia (roky)", 1f, 8f, 1f, 3f, ""),
            Field("monthsToSaveInAdvance", "Měsíců předem na spoření", 6f, 72f, 6f, 24f, ""),
        ),
    ) { v, _ ->
        val r = StudyCostCalculator.calculate(
            tuitionMonthly = v["tuitionMonthly"]!!.toDouble(),
            livingMonthly = v["livingMonthly"]!!.toDouble(),
            yearsOfStudy = v["yearsOfStudy"]!!.toInt(),
            monthsToSaveInAdvance = v["monthsToSaveInAdvance"]!!.toInt(),
        )
        listOf(
            CalcResult("Školné za rok", fmtCzk(r.annualTuition)),
            CalcResult("Životní náklady (rok)", fmtCzk(r.annualLiving)),
            CalcResult("Celkem na studium", fmtCzk(r.totalStudyCost)),
            CalcResult("Měsíční spoření předem", fmtCzk(r.monthlySavingNeeded)),
        )
    },

    CalcSpec(
        id = "rozvod-sjm",
        title = "Rozvod — rozdělení majetku",
        description = "Čistý společný majetek a podíly partnerů.",
        icon = Icons.Filled.Scale,
        group = "Rodinné události",
        fields = listOf(
            Field("assets", "Společná aktiva (nemovitost, auto…)", 0f, 20_000_000f, 10_000f, 5_000_000f, "Kč"),
            Field("debts", "Společné dluhy (hypotéka…)", 0f, 5_000_000f, 10_000f, 1_000_000f, "Kč"),
            Field("partnerSharePct", "Podíl druhého partnera", 0f, 100f, 5f, 50f, "%"),
        ),
    ) { v, _ ->
        val r = DivorcePropertySplitCalculator.calculate(
            assets = v["assets"]!!.toDouble(),
            debts = v["debts"]!!.toDouble(),
            partnerSharePct = v["partnerSharePct"]!!.toDouble(),
        )
        listOf(
            CalcResult("Čistý společný majetek", fmtCzk(r.netEstate)),
            CalcResult("Podíl prvního partnera", fmtCzk(r.shareA)),
            CalcResult("Podíl druhého partnera", fmtCzk(r.shareB)),
        )
    },

    CalcSpec(
        id = "vyzivne",
        title = "Výživné na děti",
        description = "1/7–1/5 čistého příjmu, minimum dle průměrné mzdy.",
        icon = Icons.Filled.ChildCare,
        group = "Rodinné události",
        fields = listOf(
            Field("netIncome", "Čistý měsíční příjem", 5_000f, 100_000f, 1_000f, 25_000f, "Kč"),
            Field("children", "Počet dětí", 1f, 5f, 1f, 1f, ""),
            Field("avgWage", "Průměrná mzda (rok)", 20_000f, 60_000f, 1_000f, 38_000f, "Kč"),
        ),
    ) { v, _ ->
        val c = v["children"]!!.toInt()
        val r = ChildSupportCalculator.calculate(v["netIncome"]!!.toDouble(), c, v["avgWage"]!!.toDouble())
        listOf(
            CalcResult("Podíl z čistého příjmu", when (c) { 1 -> "1/7"; 2 -> "1/6"; else -> "1/5" }),
            CalcResult("Výpočetné výživné", fmtCzk(r.calculatedAmount)),
            CalcResult("Minimální výše", fmtCzk(r.minimumAmount)),
            CalcResult("Finální výživné (měs.)", fmtCzk(r.finalAmount)),
        )
    },

    CalcSpec(
        id = "miminko",
        title = "Náklady na miminko",
        description = "Jednorázové + měsíční náklady a porodné.",
        icon = Icons.Filled.PersonAdd,
        group = "Rodinné události",
        fields = listOf(
            Field("oneTimeCost", "Jednorázové výbavičky", 10_000f, 200_000f, 5_000f, 40_000f, "Kč"),
            Field("monthlyCost", "Měsíční náklady (pleny, jídlo…)", 3_000f, 30_000f, 500f, 8_000f, "Kč"),
        ),
        toggles = listOf(
            ToggleField("isSecondOrMoreChild", "Druhé a další dítě (vyšší porodné)"),
        ),
    ) { v, t ->
        val r = BabyCostCalculator.calculate(
            oneTimeCost = v["oneTimeCost"]!!.toDouble(),
            monthlyCost = v["monthlyCost"]!!.toDouble(),
            isSecondOrMoreChild = t["isSecondOrMoreChild"] == true,
        )
        listOf(
            CalcResult("Jednorázové náklady", fmtCzk(r.oneTimeCost)),
            CalcResult("Měsíční náklady", fmtCzk(r.monthlyCost)),
            CalcResult("Celkem první rok", fmtCzk(r.firstYearTotal)),
            CalcResult("Státní porodné (jednorázově)", fmtCzk(r.stateBenefit)),
        )
    },

    CalcSpec(
        id = "dovolena-cesta",
        title = "Rozpočet dovolené",
        description = "Celková cena cesty, na osobu a spoření.",
        icon = Icons.Filled.BeachAccess,
        group = "Rodinné události",
        fields = listOf(
            Field("totalBudget", "Rozpočet na cestu", 10_000f, 200_000f, 5_000f, 40_000f, "Kč"),
            Field("people", "Počet osob", 1f, 10f, 1f, 4f, ""),
            Field("monthsToSave", "Měsíců na spoření", 1f, 18f, 1f, 6f, ""),
        ),
    ) { v, _ ->
        val r = VacationTripCalculator.calculate(v["totalBudget"]!!.toDouble(), v["people"]!!.toInt(), v["monthsToSave"]!!.toInt())
        listOf(
            CalcResult("Celková cena cesty", fmtCzk(r.totalCost)),
            CalcResult("Cena na osobu", fmtCzk(r.costPerPerson)),
            CalcResult("Měsíční spoření", fmtCzk(r.monthlySaving)),
        )
    },

    CalcSpec(
        id = "pece-o-seniora",
        title = "Péče o seniora",
        description = "Domácí péče vs. pečovatelský domov.",
        icon = Icons.Filled.Elderly,
        group = "Rodinné události",
        fields = listOf(
            Field("homeCareHourly", "Cena domácí péče (h)", 200f, 1_500f, 50f, 400f, "Kč"),
            Field("careHoursPerDay", "Hodiny péče denně", 0f, 24f, 1f, 8f, ""),
            Field("facilityMonthlyFee", "Měsíční poplatek domova", 15_000f, 60_000f, 1_000f, 30_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = SeniorCareCalculator.calculate(
            homeCareHourly = v["homeCareHourly"]!!.toDouble(),
            careHoursPerDay = v["careHoursPerDay"]!!.toInt(),
            facilityMonthlyFee = v["facilityMonthlyFee"]!!.toDouble(),
        )
        listOf(
            CalcResult("Domácí péče (měs.)", fmtCzk(r.homeMonthly)),
            CalcResult("Pečovatelský domov (měs.)", fmtCzk(r.facilityMonthly)),
            CalcResult("Rozdíl za rok (+domov / −domácí)", fmtCzk(r.annualDifference)),
            CalcResult("Vyplatnější varianta", r.cheaperOption),
        )
    },

    // ── KONEC ŽIVOTA ──────────────────────────────────────────────────
    CalcSpec(
        id = "pohreb",
        title = "Rozpočet pohřbu",
        description = "Náklady a zbytek po státním pohřebném.",
        icon = Icons.Filled.LocalHospital,
        group = "Konec života",
        fields = listOf(
            Field("coffin", "Hrob / rakve", 5_000f, 40_000f, 1_000f, 12_000f, "Kč"),
            Field("ceremonyFee", "Poplatek za obřad", 3_000f, 20_000f, 1_000f, 6_000f, "Kč"),
            Field("flowers", "Květiny a věnce", 1_000f, 10_000f, 500f, 3_000f, "Kč"),
            Field("other", "Ostatní (inzerce, hostina…)", 0f, 20_000f, 1_000f, 4_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = FuneralCostCalculator.calculate(
            coffin = v["coffin"]!!.toDouble(),
            ceremonyFee = v["ceremonyFee"]!!.toDouble(),
            flowers = v["flowers"]!!.toDouble(),
            other = v["other"]!!.toDouble(),
        )
        listOf(
            CalcResult("Celkové náklady na pohřeb", fmtCzk(r.totalCost)),
            CalcResult("Státní pohřebné (jednorázově)", fmtCzk(r.stateBenefit)),
            CalcResult("Zbývá doplatit rodině", fmtCzk(r.familyBurden)),
        )
    },

    CalcSpec(
        id = "pohrebne",
        title = "Státní pohřebné",
        description = "Pokrytí nákladů jednorázovou dávkou 15 000 Kč.",
        icon = Icons.Filled.CardGiftcard,
        group = "Konec života",
        fields = listOf(
            Field("funeralCost", "Odhadované náklady na pohřeb", 5_000f, 80_000f, 1_000f, 25_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = BereavementBenefitCalculator.calculate(v["funeralCost"]!!.toDouble())
        listOf(
            CalcResult("Státní pohřebné", fmtCzk(r.benefit)),
            CalcResult("Pokrytí nákladů", "%.0f %%".format(r.coveragePct)),
            CalcResult("Zbývá doplatit", fmtCzk(r.remainingCost)),
        )
    },

    CalcSpec(
        id = "dedicke-rijeni",
        title = "Náklady dědického řízení",
        description = "Notář + katastr za vklad dědictví.",
        icon = Icons.Filled.Gavel,
        group = "Konec života",
        fields = listOf(
            Field("estateValue", "Hodnota majetku po zemřelém", 100_000f, 30_000_000f, 10_000f, 6_000_000f, "Kč"),
            Field("propertiesCount", "Počet nemovitostí v dědictví", 0f, 10f, 1f, 2f, ""),
        ),
    ) { v, _ ->
        val r = InheritanceProceedingsCalculator.calculate(v["estateValue"]!!.toDouble(), v["propertiesCount"]!!.toInt())
        listOf(
            CalcResult("Notářský poplatek", fmtCzk(r.notaryFee)),
            CalcResult("Katastrální poplatky", fmtCzk(r.registryFees)),
            CalcResult("Celkové náklady řízení", fmtCzk(r.totalCost)),
            CalcResult("Náklady jako % majetku", "%.2f %%".format(r.costPctOfEstate)),
        )
    },

    CalcSpec(
        id = "povinni-dil",
        title = "Povinný díl ze závěti",
        description = "Polovina náležitého podílu pro nejbližší.",
        icon = Icons.Filled.Description,
        group = "Konec života",
        fields = listOf(
            Field("estateValue", "Hodnota majetku", 100_000f, 30_000_000f, 10_000f, 6_000_000f, "Kč"),
            Field("legalHeirsCount", "Počet zákonných dědiců", 1f, 8f, 1f, 3f, ""),
        ),
    ) { v, _ ->
        val r = ForcedShareCalculator.calculate(v["estateValue"]!!.toDouble(), v["legalHeirsCount"]!!.toInt())
        listOf(
            CalcResult("Náležitý podíl na dědice", fmtCzk(r.statutorySharePerHeir)),
            CalcResult("Povinný díl (polovina)", fmtCzk(r.forcedSharePerHeir)),
            CalcResult("Celkový nárok povinných dílů", fmtCzk(r.totalForcedClaim)),
        )
    },

    CalcSpec(
        id = "dar-vs-dedictvi",
        title = "Dar vs. dědictví",
        description = "Daň z daru 15 % vs. náklady dědického řízení.",
        icon = Icons.Filled.Compare,
        group = "Konec života",
        fields = listOf(
            Field("assetValue", "Hodnota majetku (např. byt)", 50_000f, 10_000_000f, 10_000f, 1_000_000f, "Kč"),
            Field("propertiesCount", "Počet nemovitostí", 0f, 10f, 1f, 1f, ""),
        ),
        toggles = listOf(
            ToggleField("isCloseRelative", "Nejbližší příbuzný (osvobození 8 000 Kč)"),
        ),
    ) { v, t ->
        val r = GiftVsInheritanceCalculator.calculate(
            assetValue = v["assetValue"]!!.toDouble(),
            isCloseRelative = t["isCloseRelative"] == true,
            propertiesCount = v["propertiesCount"]!!.toInt(),
        )
        listOf(
            CalcResult("Čistě po daru (po dani)", fmtCzk(r.giftNet)),
            CalcResult("Čistě po dědictví (po nákladech)", fmtCzk(r.inheritanceNet)),
            CalcResult("Vyplatnější varianta", r.betterOption),
        )
    },
)
