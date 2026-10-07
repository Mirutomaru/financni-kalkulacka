package com.mirda.financnikalkulacky.ui.specs
import kotlin.math.pow

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Scale
import com.mirda.financnikalkulacky.calculators.housing.BonityCalculator
import com.mirda.financnikalkulacky.calculators.housing.BuyVsRentCalculator
import com.mirda.financnikalkulacky.calculators.housing.EnergyCostCalculator
import com.mirda.financnikalkulacky.calculators.housing.EnergyInvestCalculator
import com.mirda.financnikalkulacky.calculators.housing.HouseBuildCalculator
import com.mirda.financnikalkulacky.calculators.housing.MortgageCalculator
import com.mirda.financnikalkulacky.calculators.housing.NzgSubsidyCalculator
import com.mirda.financnikalkulacky.calculators.housing.PropertyInsuranceCalculator
import com.mirda.financnikalkulacky.calculators.housing.PropertyTaxCalculator
import com.mirda.financnikalkulacky.calculators.housing.PurchaseCostCalculator
import com.mirda.financnikalkulacky.calculators.housing.RealEstateSaleTaxCalculator
import com.mirda.financnikalkulacky.calculators.housing.RentalIncomeTaxCalculator
import com.mirda.financnikalkulacky.calculators.housing.RentalYieldCalculator
import com.mirda.financnikalkulacky.calculators.housing.RentSettingCalculator
import com.mirda.financnikalkulacky.calculators.housing.RenovationCalculator
import com.mirda.financnikalkulacky.calculators.housing.RoommateSplitCalculator
import com.mirda.financnikalkulacky.calculators.housing.SvjFundCalculator
import com.mirda.financnikalkulacky.components.fmtCzk
import androidx.compose.ui.graphics.Color
import com.mirda.financnikalkulacky.ui.model.CalcResult
import com.mirda.financnikalkulacky.ui.model.CalcSpec
import com.mirda.financnikalkulacky.ui.model.ChartPoint
import com.mirda.financnikalkulacky.ui.model.ChartSeries
import com.mirda.financnikalkulacky.ui.model.Field
import com.mirda.financnikalkulacky.ui.model.ToggleField

private fun pct(v: Double) = String.format("%.2f %%", v)

val HOUSING_SPECS: List<CalcSpec> = listOf(
    CalcSpec(
        id = "hypoteka",
        title = "Hypoteční kalkulačka",
        description = "Měsíční anuitní splátka, celkové úroky a přeplatek.",
        icon = Icons.Filled.Home,
        group = "Pořízení bydlení",
        fields = listOf(
            Field("loan", "Výše úvěru", 100_000f, 20_000_000f, 50_000f, 3_000_000f, "Kč"),
            Field("rate", "Roční úroková sazba", 0.5f, 12f, 0.1f, 6f, "%"),
            Field("years", "Doba splácení", 5f, 40f, 1f, 30f, "let"),
        ),
        toggles = listOf(
            ToggleField("calendar", "Zobrazit splátkový kalendář (po roce)"),
        ),
        charts = { v, _ ->
            val plan = MortgageCalculator.yearlyAmortization(v["loan"]!!.toDouble(), v["rate"]!!.toDouble(), v["years"]!!.toInt())
            listOf(
                ChartSeries("Zůstatek jistiny", Color(0xFF7CB98A),
                    listOf(ChartPoint(0f, v["loan"]!!.toDouble())) + plan.map { ChartPoint(it.year.toFloat(), it.remainingPrincipal) }),
                ChartSeries("Kumulované úroky", Color(0xFFFFC96B),
                    plan.map { ChartPoint(it.year.toFloat(), it.cumulativeInterest) }),
            )
        },
    ) { v, t ->
        val loan = v["loan"]!!.toDouble()
        val rate = v["rate"]!!.toDouble()
        val years = v["years"]!!.toInt()
        val r = MortgageCalculator.calculate(loan, rate, years)
        val results = mutableListOf(
            CalcResult("Měsíční splátka", fmtCzk(r.monthlyPayment)),
            CalcResult("RPSN (efektivní roční sazba)", String.format("%.2f %%", MortgageCalculator.rpsn(loan, rate, years))),
            CalcResult("Celkem zaplaceno", fmtCzk(r.totalPaid)),
            CalcResult("Úroky celkem (přeplatek)", fmtCzk(r.totalInterest)),
        )
        if (t["calendar"] == true) {
            results.add(CalcResult("", "")) // oddělovač
            MortgageCalculator.yearlyAmortization(loan, rate, years).forEach { y ->
                results.add(CalcResult("Rok ${y.year}: splaceno jistiny", fmtCzk(y.yearlyPrincipal)))
                results.add(CalcResult("  úroky v roce / zůstatek", "${fmtCzk(y.yearlyInterest)} / ${fmtCzk(y.remainingPrincipal)}"))
            }
        }
        results
    },

    CalcSpec(
        id = "bonita",
        title = "Bonita na hypotéku (ČNB)",
        description = "Max. úvěr podle příjmů, LTV a DSTI limitu.",
        icon = Icons.Filled.AccountBalanceWallet,
        group = "Pořízení bydlení",
        fields = listOf(
            Field("income", "Hrubý příjem domácnosti (měs.)", 10_000f, 200_000f, 5_000f, 45_000f, "Kč"),
            Field("debt", "Stávající splátky (měs.)", 0f, 30_000f, 1_000f, 2_000f, "Kč"),
            Field("price", "Cena nemovitosti", 500_000f, 20_000_000f, 100_000f, 6_000_000f, "Kč"),
            Field("ownFunds", "Vlastní zdroje (akontace)", 0f, 5_000_000f, 50_000f, 1_200_000f, "Kč"),
            Field("rate", "Očekávaná sazba úvěru", 0.5f, 12f, 0.1f, 6f, "%"),
            Field("years", "Doba splácení", 5f, 40f, 1f, 30f, "let"),
        ),
    ) { v, _ ->
        val r = BonityCalculator.calculate(v["income"]!!.toDouble(), v["debt"]!!.toDouble(), v["price"]!!.toDouble(), v["ownFunds"]!!.toDouble(), v["rate"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Max. úvěr z příjmů (DSTI)", fmtCzk(r.maxLoanByIncome)),
            CalcResult("Max. úvěr z LTV limitu", fmtCzk(r.maxLoanByLtv)),
            CalcResult("Bonita — max. půjčka", fmtCzk(r.maxLoan)),
            CalcResult("LTV při plném využití", pct(r.ltvPct)),
            CalcResult("DTI (splátka / příjem)", pct(r.dtiPct)),
            CalcResult("DSTI (splátky + dluhy / příjem)", pct(r.dstiPct)),
            CalcResult("LTV v limitu banky?", if (r.passesLtv) "ANO ✓" else "NE — chybí akontace"),
            CalcResult("DSTI v limitu ČNB (50 %)?", if (r.passesDsti) "ANO ✓" else "NE — dluhy příliš vysoké"),
            CalcResult("Doporučená vlastní částka (20 %)", fmtCzk(r.recommendedDownPayment)),
        )
    },

    CalcSpec(
        id = "vedlejni-naklady",
        title = "Vedlejší náklady koupě",
        description = "Notář/úschova, katastr, provize a poplatky úvěru.",
        icon = Icons.Filled.Payments,
        group = "Pořízení bydlení",
        fields = listOf(
            Field("price", "Cena nemovitosti", 500_000f, 20_000_000f, 100_000f, 6_000_000f, "Kč"),
            Field("escrowPct", "Sazba advokátní úschovy", 0.1f, 1f, 0.05f, 0.25f, "%"),
            Field("brokerFeePct", "Provize realitky (s DPH)", 0f, 6f, 0.1f, 3f, "%"),
            Field("loanProcessing", "Poplatky za úvěr", 0f, 50_000f, 1_000f, 8_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = PurchaseCostCalculator.calculate(v["price"]!!.toDouble(), v["escrowPct"]!!.toDouble(), v["brokerFeePct"]!!.toDouble(), v["loanProcessing"]!!.toDouble())
        listOf(
            CalcResult("Advokátní úschova / notář", fmtCzk(r.notaryEscrow)),
            CalcResult("Katastrální poplatek (0,5 %)", fmtCzk(r.landRegistry)),
            CalcResult("Provize realitky", fmtCzk(r.realEstateFee)),
            CalcResult("Poplatky za zpracování úvěru", fmtCzk(r.loanProcessing)),
            CalcResult("CELKEM vedlejší náklady", fmtCzk(r.totalCosts)),
            CalcResult("Skutečná cena pořízení", fmtCzk(r.totalPurchasePrice)),
            CalcResult("Náklady v % ceny", pct(r.costsPctOfPrice)),
        )
    },

    CalcSpec(
        id = "vystavba-domu",
        title = "Výstavba domu (rozpočet)",
        description = "Rozpočet stavby dle m², pozemek a přípojky.",
        icon = Icons.Filled.House,
        group = "Pořízení bydlení",
        fields = listOf(
            Field("sqm", "Podlahová plocha", 60f, 300f, 5f, 120f, "m²"),
            Field("costPerSqm", "Cena stavby za m²", 25_000f, 80_000f, 1_000f, 45_000f, "Kč/m²"),
            Field("landCost", "Cena pozemku", 0f, 3_000_000f, 50_000f, 800_000f, "Kč"),
            Field("utilitiesExtra", "Dodatečné přípojky", 0f, 200_000f, 10_000f, 50_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = HouseBuildCalculator.calculate(v["sqm"]!!.toDouble(), v["costPerSqm"]!!.toDouble(), v["landCost"]!!.toDouble(), v["utilitiesExtra"]!!.toDouble())
        listOf(
            CalcResult("Stavba domu (dle m²)", fmtCzk(r.buildingCost)),
            CalcResult("Pozemek", fmtCzk(r.landCost)),
            CalcResult("Přípojky a inženýrské sítě", fmtCzk(r.utilitiesCost)),
            CalcResult("CELKOVÝ rozpočet", fmtCzk(r.totalBudget)),
            CalcResult("Cena za 1 m² (celkem)", String.format("%.0f Kč/m²", r.costPerSqm)),
            CalcResult("Doporučená půjčka (80 %)", fmtCzk(r.recommendedMortgage)),
        )
    },

    CalcSpec(
        id = "mimoradna",
        title = "Mimořádná splátka hypotéky",
        description = "Úspora na úrocích při předčasném splacení.",
        icon = Icons.Filled.Key,
        group = "Pořízení bydlení",
        fields = listOf(
            Field("loan", "Zůstatek jistiny", 100_000f, 20_000_000f, 50_000f, 1_500_000f, "Kč"),
            Field("rate", "Roční sazba", 0.5f, 12f, 0.1f, 6f, "%"),
            Field("months", "Zbývající doba", 12f, 480f, 12f, 300f, "měs."),
            Field("extra", "Mimořádná splátka", 10_000f, 2_000_000f, 10_000f, 300_000f, "Kč"),
        ),
        toggles = listOf(ToggleField("goalB", "Snížit splátku (B) — jinak zkrátit dobu (A)")),
    ) { v, t ->
        val r = MortgageCalculator.extraPayment(v["loan"]!!.toDouble(), v["rate"]!!.toDouble(), v["months"]!!.toInt(), v["extra"]!!.toDouble(), t["goalB"] == true)
        if (t["goalB"] == true) listOf(
            CalcResult("Nová měsíční splátka", fmtCzk(r.newMonthlyPayment)),
            CalcResult("Úspora na úrocích", fmtCzk(r.interestSaved)),
        ) else listOf(
            CalcResult("Zkrácení o (měsíce)", r.monthsShortened.toString()),
            CalcResult("Úspora na úrocích", fmtCzk(r.interestSaved)),
        )
    },

    CalcSpec(
        id = "refinancovani",
        title = "Refinancování hypotéky",
        description = "Čistá úspora, nová splátka a bod zvratu.",
        icon = Icons.Filled.SwapHoriz,
        group = "Pořízení bydlení",
        fields = listOf(
            Field("loan", "Zůstatek úvěru", 100_000f, 20_000_000f, 50_000f, 1_500_000f, "Kč"),
            Field("months", "Zbývající doba", 12f, 480f, 12f, 240f, "měs."),
            Field("oldRate", "Původní sazba", 0.5f, 12f, 0.1f, 7f, "%"),
            Field("newRate", "Nová sazba", 0.5f, 12f, 0.1f, 4.5f, "%"),
            Field("fees", "Poplatky za refinancování", 0f, 100_000f, 1_000f, 15_000f, "Kč"),
            Field("earlyFeePct", "Penále za předčasné splacení", 0f, 3f, 0.1f, 0.5f, "% zůstatku"),
        ),
    ) { v, _ ->
        val loan = v["loan"]!!.toDouble()
        val penalty = loan * v["earlyFeePct"]!!.toDouble() / 100.0
        val r = MortgageCalculator.refinance(loan, v["months"]!!.toInt(), v["oldRate"]!!.toDouble(), v["newRate"]!!.toDouble(), v["fees"]!!.toDouble() + penalty)
        listOf(
            CalcResult("Původní splátka", fmtCzk(r.oldPayment)),
            CalcResult("Nová splátka", fmtCzk(r.newPayment)),
            CalcResult("Penále za předčasné splacení", fmtCzk(penalty)),
            CalcResult("Čistá úspora (po poplatcích)", fmtCzk(r.netSavings)),
            CalcResult("Bod zvratu (měsíce)", r.breakEvenMonths.toString()),
        )
    },

    CalcSpec(
        id = "renta",
        title = "Investiční byt & rentabilita nájmu",
        description = "Cap Rate, cashflow a návratnost investice.",
        icon = Icons.Filled.TrendingUp,
        group = "Pronájem",
        fields = listOf(
            Field("price", "Cena + investice", 500_000f, 20_000_000f, 100_000f, 6_000_000f, "Kč"),
            Field("rent", "Měsíční nájemné", 5_000f, 50_000f, 500f, 18_000f, "Kč"),
            Field("vacancy", "Neobsazenost", 0f, 30f, 1f, 8f, "%"),
            Field("opex", "Provozní náklady (měs.)", 0f, 20_000f, 500f, 4_000f, "Kč"),
            Field("tax", "Efektivní daň z nájmu", 0f, 30f, 0.5f, 10.5f, "%"),
        ),
    ) { v, _ ->
        val r = RentalYieldCalculator.calculate(v["price"]!!.toDouble(), v["rent"]!!.toDouble(), v["vacancy"]!!.toDouble(), v["opex"]!!.toDouble(), v["tax"]!!.toDouble())
        listOf(
            CalcResult("Gross Rental Yield", pct(r.grossYieldPct)),
            CalcResult("Net Cap Rate", pct(r.capRatePct)),
            CalcResult("Čistý měsíční cashflow", fmtCzk(r.monthlyCashflow)),
            CalcResult("Návratnost (roky)", if (r.paybackYears > 100) "—" else String.format("%.1f", r.paybackYears)),
        )
    },

    CalcSpec(
        id = "koupvejnajem",
        title = "Koupě vs. Nájem",
        description = "Srovnání čistého jmění po zvoleném horizontu.",
        icon = Icons.Filled.Scale,
        group = "Pořízení bydlení",
        fields = listOf(
            Field("price", "Cena nemovitosti", 1_000_000f, 20_000_000f, 100_000f, 6_000_000f, "Kč"),
            Field("ownFunds", "Vlastní zdroje", 0f, 5_000_000f, 50_000f, 1_200_000f, "Kč"),
            Field("mortRate", "Úrok hypotéky", 0.5f, 12f, 0.1f, 6f, "%"),
            Field("mortYears", "Doba hypotéky", 5f, 40f, 1f, 30f, "let"),
            Field("buyOpex", "Provoz bytu (měs.)", 0f, 20_000f, 500f, 6_000f, "Kč"),
            Field("propGrowth", "Růst cen nemovitostí", -2f, 15f, 0.5f, 3f, "%/rok"),
            Field("rentMonthly", "Nájem srovnatelného bytu", 5_000f, 50_000f, 500f, 18_000f, "Kč"),
            Field("rentGrowth", "Růst nájmu", -2f, 15f, 0.5f, 3f, "%/rok"),
            Field("invReturn", "Výnos investice", 0f, 15f, 0.5f, 6f, "%/rok"),
            Field("horizon", "Horizont srovnání", 5f, 40f, 1f, 25f, "let"),
        ),
    ) { v, _ ->
        val r = BuyVsRentCalculator.calculate(v["price"]!!.toDouble(), v["ownFunds"]!!.toDouble(), v["mortRate"]!!.toDouble(), v["mortYears"]!!.toInt(), v["buyOpex"]!!.toDouble(), v["propGrowth"]!!.toDouble(), v["rentMonthly"]!!.toDouble(), v["rentGrowth"]!!.toDouble(), v["invReturn"]!!.toDouble(), v["horizon"]!!.toInt())
        val ownFunds = v["ownFunds"]!!.toDouble()
        val invRet = v["invReturn"]!!.toDouble() / 100.0
        val years = v["horizon"]!!.toInt()
        // Ušlý výnos: co by vydělal vlastní zdroje, kdyby se investovaly místo vkladu do nemovitosti
        val opportunityCost = ownFunds * ((1.0 + invRet).pow(years.toDouble()) - 1.0)
        listOf(
            CalcResult("Čisté jmění — KOUPE", fmtCzk(r.buyNetWorth)),
            CalcResult("Čisté jmění — NÁJEM", fmtCzk(r.rentNetWorth)),
            CalcResult("Výhoda (Kč)", fmtCzk(r.buyNetWorth - r.rentNetWorth)),
            CalcResult("Ušlý výnos z vlastních zdrojů", fmtCzk(opportunityCost)),
        )
    },

    // ── PROVOZ BYDLENÍ ────────────────────────────────────────────────
    CalcSpec(
        id = "energie",
        title = "Náklady na energie",
        description = "Elektřina, plyn, topení a voda — roční i měsíčně.",
        icon = Icons.Filled.Bolt,
        group = "Provoz bydlení",
        fields = listOf(
            Field("elecKwh", "Spotřeba elektřiny (rok)", 500f, 10_000f, 250f, 3_000f, "kWh"),
            Field("elecPrice", "Cena elektřiny", 2f, 15f, 0.25f, 8f, "Kč/kWh"),
            Field("gasM3", "Spotřeba plynu (rok)", 0f, 2_000f, 50f, 400f, "m³"),
            Field("gasPrice", "Cena plynu", 100f, 600f, 10f, 250f, "Kč/m³"),
            Field("heatingCost", "Topení (rok)", 0f, 100_000f, 2_000f, 20_000f, "Kč"),
            Field("waterM3", "Voda + stočení (rok)", 0f, 500f, 10f, 80f, "m³"),
            Field("waterPrice", "Cena za m³ vody", 100f, 400f, 10f, 250f, "Kč/m³"),
        ),
    ) { v, _ ->
        val r = EnergyCostCalculator.calculate(v["elecKwh"]!!.toDouble(), v["elecPrice"]!!.toDouble(), v["gasM3"]!!.toDouble(), v["gasPrice"]!!.toDouble(), v["heatingCost"]!!.toDouble(), v["waterM3"]!!.toDouble(), v["waterPrice"]!!.toDouble())
        listOf(
            CalcResult("Elektřina (rok)", fmtCzk(r.electricityAnnual)),
            CalcResult("Plyn (rok)", fmtCzk(r.gasAnnual)),
            CalcResult("Topení (rok)", fmtCzk(r.heatingAnnual)),
            CalcResult("Voda + stočení (rok)", fmtCzk(r.waterAnnual)),
            CalcResult("CELKEM energie (rok)", fmtCzk(r.totalAnnual)),
            CalcResult("Průměr na měsíc", fmtCzk(r.totalMonthly)),
        )
    },

    CalcSpec(
        id = "svj-fond",
        title = "Fond oprav SVJ a zálohy",
        description = "Zálohy na služby a hromadění fondu oprav.",
        icon = Icons.Filled.Apartment,
        group = "Provoz bydlení",
        fields = listOf(
            Field("unitValue", "Hodnota bytu", 500_000f, 20_000_000f, 100_000f, 3_000_000f, "Kč"),
            Field("serviceCosts", "Roční služby (voda, úklid...)", 0f, 60_000f, 1_000f, 18_000f, "Kč/rok"),
            Field("fundYears", "Doba hromadění fondu", 5f, 30f, 1f, 10f, "let"),
        ),
    ) { v, _ ->
        val r = SvjFundCalculator.calculate(v["unitValue"]!!.toDouble(), v["serviceCosts"]!!.toDouble(), v["fundYears"]!!.toInt())
        listOf(
            CalcResult("Měsíční záloha na jednotku", fmtCzk(r.monthlyPerUnit)),
            CalcResult("Ročně na jednotku", fmtCzk(r.annualPerUnit)),
            CalcResult("Doporučený fond oprav (5 %)", fmtCzk(r.fundTarget)),
            CalcResult("Měsíční příspěvek do fondu", fmtCzk(r.fundMonthlyContribution)),
            CalcResult("CELKEM měsíčně na jednotku", fmtCzk(r.totalMonthly)),
        )
    },

    CalcSpec(
        id = "dan-nemovitost",
        title = "Daň z nemovitých věcí",
        description = "Odhad daně podle nového zákona (od 1/2024).",
        icon = Icons.Filled.ReceiptLong,
        group = "Provoz bydlení",
        fields = listOf(
            Field("rate", "Sazba obce", 3f, 60_000f, 1f, 8f, "Kč/m² (byt) nebo Kč/rok (dům)"),
            Field("area", "Podlahová plocha (byt)", 20f, 300f, 5f, 75f, "m²"),
        ),
        toggles = listOf(ToggleField("isHouse", "Rodinný dům (jinak byt)")),
    ) { v, t ->
        val r = PropertyTaxCalculator.calculate(if (t["isHouse"] == true) "dům" else "byt", v["rate"]!!.toDouble(), v["area"]!!.toDouble())
        listOf(
            CalcResult("Roční daň z nemovitosti", fmtCzk(r.annualTax)),
            CalcResult("Ekvivalent na měsíc", fmtCzk(r.monthlyEquivalent)),
            CalcResult("Poznámka", r.note),
        )
    },

    CalcSpec(
        id = "pojisteni-nemovitost",
        title = "Pojištění nemovitosti a domácnosti",
        description = "Odhad pojistného + kontrola podpojištění.",
        icon = Icons.Filled.Shield,
        group = "Provoz bydlení",
        fields = listOf(
            Field("buildingValue", "Obnovná cena budovy/bytu", 1_000_000f, 20_000_000f, 100_000f, 5_000_000f, "Kč"),
            Field("contentsValue", "Hodnota movitých věcí", 100_000f, 5_000_000f, 50_000f, 800_000f, "Kč"),
            Field("insuredBuilding", "Sjednané krytí budovy", 1_000_000f, 20_000_000f, 100_000f, 5_000_000f, "Kč"),
            Field("insuredContents", "Sjednané krytí věcí", 100_000f, 5_000_000f, 50_000f, 800_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = PropertyInsuranceCalculator.calculate(v["buildingValue"]!!.toDouble(), v["contentsValue"]!!.toDouble(), v["insuredBuilding"]!!.toDouble(), v["insuredContents"]!!.toDouble())
        listOf(
            CalcResult("Odhad pojistného — budova (rok)", fmtCzk(r.buildingPremium)),
            CalcResult("Odhad pojistného — věci (rok)", fmtCzk(r.contentsPremium)),
            CalcResult("CELKEM pojistné (rok)", fmtCzk(r.totalPremium)),
            CalcResult("Doporučené krytí budovy", fmtCzk(r.coverageBuilding)),
            CalcResult("Podpojištění", pct(r.underinsuredPct)),
            CalcResult("Kontrola", r.warning),
        )
    },

    CalcSpec(
        id = "spolubydlici",
        title = "Náklady mezi spolubydlící",
        description = "Rozdělení nájmu, energií a internetu.",
        icon = Icons.Filled.People,
        group = "Provoz bydlení",
        fields = listOf(
            Field("rent", "Nájem (měs.)", 0f, 100_000f, 500f, 25_000f, "Kč"),
            Field("utilities", "Energie + služby (měs.)", 0f, 30_000f, 500f, 6_000f, "Kč"),
            Field("internet", "Internet (měs.)", 0f, 2_000f, 100f, 900f, "Kč"),
            Field("people", "Počet spolubydlících", 2f, 6f, 1f, 3f, ""),
        ),
    ) { v, _ ->
        val r = RoommateSplitCalculator.calculate(v["rent"]!!.toDouble(), v["utilities"]!!.toDouble(), v["internet"]!!.toDouble(), v["people"]!!.toInt(), emptyMap())
        listOf(
            CalcResult("Celkové společné náklady", fmtCzk(r.totalMonthly)),
            CalcResult("Na osobu (stejný podíl)", fmtCzk(r.equalShare)),
        )
    },

    // ── ÚSPORY A INVESTICE DO BYDLENÍ ────────────────────────────────
    CalcSpec(
        id = "navratnost-energie",
        title = "Návratnost FV / tepelného čerpadla / zateplení",
        description = "Kdy se investice do úspor energií vrátí.",
        icon = Icons.Filled.Eco,
        group = "Úspory a investice do bydlení",
        fields = listOf(
            Field("cost", "Cena pořízení", 50_000f, 1_000_000f, 10_000f, 200_000f, "Kč"),
            Field("subsidy", "Dotace (NZÚ)", 0f, 300_000f, 5_000f, 60_000f, "Kč"),
            Field("savings", "Roční úspora na energiích", 1_000f, 80_000f, 500f, 12_000f, "Kč/rok"),
        ),
    ) { v, _ ->
        val r = EnergyInvestCalculator.calculate(v["cost"]!!.toDouble(), v["subsidy"]!!.toDouble(), v["savings"]!!.toDouble())
        listOf(
            CalcResult("Investice po dotaci", fmtCzk(r.investment)),
            CalcResult("Roční úspora", fmtCzk(r.annualSavings)),
            CalcResult("Návratnost (roky)", String.format("%.1f let", r.paybackYears)),
            CalcResult("Kumulovaná úspora za 30 let", fmtCzk(r.totalSavings30y)),
            CalcResult("Čistý zisk za 30 let", fmtCzk(r.netGain30y)),
        )
    },

    CalcSpec(
        id = "dotace-nzu",
        title = "Dotace Nová zelená úsporám",
        description = "Orientační odhad dotace dle typu opatření.",
        icon = Icons.Filled.LocalOffer,
        group = "Úspory a investice do bydlení",
        fields = listOf(
            Field("cost", "Cena pořízení", 50_000f, 1_000_000f, 10_000f, 200_000f, "Kč"),
        ),
        toggles = listOf(
            ToggleField("type_tc", "Tepelné čerpadlo (výchozí: fotovoltaika)"),
            ToggleField("type_zatepleni", "Zateplení (převýší TC i FV)"),
        ),
    ) { v, t ->
        val type = when {
            t["type_zatepleni"] == true -> "zatepleni"
            t["type_tc"] == true -> "tepelnecerpadlo"
            else -> "fotovoltaika"
        }
        val r = NzgSubsidyCalculator.calculate(type, v["cost"]!!.toDouble())
        listOf(
            CalcResult("Odhad dotace NZÚ", fmtCzk(r.estimatedSubsidy)),
            CalcResult("Cena po dotaci", fmtCzk(r.netCost)),
            CalcResult("Poznámka", r.note),
        )
    },

    CalcSpec(
        id = "navratnost-rekonstrukce",
        title = "Návratnost rekonstrukce",
        description = "Navýšení hodnoty a návratnost z vyššího nájmu.",
        icon = Icons.Filled.Restore,
        group = "Úspory a investice do bydlení",
        fields = listOf(
            Field("cost", "Cena rekonstrukce", 100_000f, 3_000_000f, 50_000f, 800_000f, "Kč"),
            Field("valuePct", "Odhad navýšení hodnoty", 0f, 100f, 5f, 30f, "% z nákladu"),
            Field("rentBefore", "Nájem před rekonstrukcí (měs.)", 0f, 60_000f, 500f, 12_000f, "Kč"),
            Field("rentAfter", "Nájem po rekonstrukci (měs.)", 0f, 80_000f, 500f, 16_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = RenovationCalculator.calculate(v["cost"]!!.toDouble(), v["valuePct"]!!.toDouble(), v["rentBefore"]!!.toDouble(), v["rentAfter"]!!.toDouble())
        listOf(
            CalcResult("Navýšení tržní hodnoty", fmtCzk(r.valueIncrease)),
            CalcResult("Návratnost investice (ROI)", pct(r.roiPct)),
            CalcResult("Navýšení nájmu na měsíc", fmtCzk(r.monthlyRentIncrease)),
            CalcResult("Návratnost z vyššího nájmu", String.format("%.1f let", r.paybackYears)),
        )
    },

    // ── PRONÁJEM ──────────────────────────────────────────────────────
    CalcSpec(
        id = "zdaneni-najmu",
        title = "Zdanění příjmů z nájmu",
        description = "Skutečné výdaje (vč. odpisů) vs. 30% paušál.",
        icon = Icons.Filled.AttachMoney,
        group = "Pronájem",
        fields = listOf(
            Field("income", "Roční nájemné", 10_000f, 500_000f, 5_000f, 120_000f, "Kč"),
            Field("expenses", "Skutečné provozní výdaje (rok)", 0f, 300_000f, 5_000f, 40_000f, "Kč"),
            Field("depreciation", "Roční odpisy nemovitosti", 0f, 200_000f, 5_000f, 30_000f, "Kč"),
        ),
    ) { v, _ ->
        val r = RentalIncomeTaxCalculator.calculate(v["income"]!!.toDouble(), v["expenses"]!!.toDouble(), v["depreciation"]!!.toDouble())
        listOf(
            CalcResult("Základ daně — skutečné výdaje", fmtCzk(r.taxableActual)),
            CalcResult("Základ daně — 30% paušál", fmtCzk(r.taxableFlat)),
            CalcResult("Daň — skutečné (15 %)", fmtCzk(r.taxActual)),
            CalcResult("Daň — paušál (15 %)", fmtCzk(r.taxFlat)),
            CalcResult("Vyplatí se", r.betterMode),
            CalcResult("Úspora správnou volbou", fmtCzk(r.savings)),
        )
    },

    CalcSpec(
        id = "zdaneni-prodeje",
        title = "Zdanění prodeje nemovitosti",
        description = "Časový test 5 let a osvobození bytovou potřebou.",
        icon = Icons.Filled.Gavel,
        group = "Pronájem",
        fields = listOf(
            Field("purchasePrice", "Cena pořízení + náklady", 1_000_000f, 20_000_000f, 100_000f, 3_000_000f, "Kč"),
            Field("salePrice", "Prodejní cena", 1_000_000f, 25_000_000f, 100_000f, 4_000_000f, "Kč"),
            Field("yearsHeld", "Doba držby", 0f, 30f, 1f, 5f, "let"),
        ),
        toggles = listOf(ToggleField("residentialNeed", "Splněna bytová potřeba / věk 60+")),
    ) { v, t ->
        val r = RealEstateSaleTaxCalculator.calculate(v["purchasePrice"]!!.toDouble(), v["salePrice"]!!.toDouble(), v["yearsHeld"]!!.toInt(), t["residentialNeed"] == true)
        listOf(
            CalcResult("Prodejní zisk", fmtCzk(r.gain)),
            CalcResult("Stav", if (r.exempt) "OSVOBOZENO od daně" else "ZDANĚNO"),
            CalcResult("Daň 15 %", fmtCzk(r.tax)),
            CalcResult("Důvod", r.reason),
        )
    },

    CalcSpec(
        id = "vyska-najemneho",
        title = "Nájemné, kauce a valorizace",
        description = "Kauce v měsících a nárůst nájmu o inflaci.",
        icon = Icons.Filled.PriceChange,
        group = "Pronájem",
        fields = listOf(
            Field("rent", "Měsíční nájemné", 5_000f, 80_000f, 500f, 15_000f, "Kč"),
            Field("depositMonths", "Kauce (počet měsíců)", 2f, 6f, 1f, 3f, ""),
            Field("inflationPct", "Roční inflace", 0f, 15f, 0.5f, 4f, "%"),
            Field("years", "Doba valorizace", 1f, 20f, 1f, 5f, "let"),
        ),
    ) { v, _ ->
        val r = RentSettingCalculator.calculate(v["rent"]!!.toDouble(), v["depositMonths"]!!.toInt(), v["inflationPct"]!!.toDouble(), v["years"]!!.toInt())
        listOf(
            CalcResult("Kauce (celkem)", fmtCzk(r.depositAmount)),
            CalcResult("Nájem po valorizaci o inflaci", fmtCzk(r.rentAfterInflation)),
            CalcResult("Celkový nárůst za ${v["years"]!!.toInt()} let", pct((r.rentAfterInflation / maxOf(v["rent"]!!.toDouble(), 1.0) - 1.0) * 100.0)),
        )
    },

)
