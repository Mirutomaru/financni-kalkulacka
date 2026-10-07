package com.mirda.financnikalkulacky.calculators

/** Legislativní konstanty ČR (2026) — aktualizují se jednou ročně bez zásahu do logiky. */
object Legislation {
    // Daň z příjmů fyzických osob
    const val TAX_RATE_1 = 0.15
    const val TAX_RATE_2 = 0.23
    const val BASIC_DEDUCTION_ANNUAL = 30_840.0

    // Sleva na dítě (ročně) dle pořadí
    const val CHILD_DEDUCTION_1 = 18_000.0
    const val CHILD_DEDUCTION_2 = 36_000.0
    const val CHILD_DEDUCTION_3_PLUS = 54_000.0

    // Odvody zaměstnance (příspěvek na pojištění)
    const val SOCIAL_INSURANCE_EMPLOYEE = 0.071   // 7,1 % (zahrnuje nemocenské 0,6 %)
    const val HEALTH_INSURANCE_EMPLOYEE = 0.045   // 4,5 %
    const val HEALTH_INSURANCE_MIN_MONTHLY = 456.0

    // Odvody zaměstnavatele (superhrubá zátěž)
    const val SOCIAL_INSURANCE_EMPLOYER = 0.248   // 24,8 %
    const val HEALTH_INSURANCE_EMPLOYER = 0.09    // 9 %

    // Nemocenská — redukční hranice denního vyměřovacího základu (2026)
    const val SICK_LIMIT_1 = 1_633.0   // do této hranice se započítává 90 %
    const val SICK_LIMIT_2 = 2_449.0   // mezi 1. a 2. hranicí 60 %
    const val SICK_LIMIT_3 = 4_897.0   // mezi 2. a 3. hranicí 30 %, nad to nic

    // Nemocenská — sazby podle délky trvání (od 15. dne)
    const val SICK_RATE_1 = 0.60   // 15.–30. den
    const val SICK_RATE_2 = 0.66   // 31.–60. den
    const val SICK_RATE_3 = 0.72   // od 61. dne

    // OSVČ — paušální daň, měsíční záloha (2026) dle pásma obratu
    const val FLAT_TAX_BAND_1 = 9_162.0   // měsíční záloha, příjmy do 1 mil. Kč
    const val FLAT_TAX_BAND_2 = 16_745.0  // měsíční záloha, příjmy do 1,5 mil. Kč
    const val FLAT_TAX_BAND_3 = 27_139.0  // měsíční záloha, příjmy do 2 mil. Kč

    // OSVČ — paušální výdaje podle typu činnosti
    const val PAUSAL_EXPENSE_TRADE = 0.80   // řemeslné živnosti
    const val PAUSAL_EXPENSE_BUSINESS = 0.60 // volné živnosti
    const val PAUSAL_EXPENSE_FREE = 0.40    // svobodná povolání

    // OSVČ — minimální měsíční odvody (paušál 1. pásmo jako orientační minimum)
    const val OVC_MIN_SOCIAL_MONTHLY = 5_756.0
    const val OVC_MIN_HEALTH_MONTHLY = 3_306.0

    // DIP / penzijní připojištění — maximální roční odpočet ze základu daně
    const val DIP_MAX_DEDUCTION_ANNUAL = 48_000.0

    // DPP / DPČ — měsíční limit pro vznik odvodů (10 % průměrné mzdy, 2026)
    const val DPC_CONTRIBUTION_LIMIT = 4_100.0

    // Rodinné události — porodné (jednorázově, 2026)
    const val PORODNE_FIRST = 13_000.0   // první dítě
    const val PORODNE_NEXT = 30_000.0    // druhé a další děti

    // Konec života — státní pohřebné (jednorázová dávka, 2026)
    const val POHRBENE_BENEFIT = 15_000.0

    // Podpora v nezaměstnanosti — měsíce podpůrné doby podle věku
    fun unemploymentMonths(age: Int): Int = when {
        age < 50 -> 5
        age <= 55 -> 8
        else -> 11
    }

    // Odstupné (§ 67 zákoníku práce) — násobek průměrného výdělku
    fun severanceMultiplier(years: Double): Int = when {
        years < 1.0 -> 1
        years < 2.0 -> 2
        else -> 3
    }
}
