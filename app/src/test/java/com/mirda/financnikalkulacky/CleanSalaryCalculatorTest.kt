package com.mirda.financnikalkulacky.calculators

import org.junit.Assert.assertEquals
import org.junit.Test

class CleanSalaryCalculatorTest {

    private fun approx(expected: Double, actual: Double) =
        assertEquals(expected, actual, 0.5) // zaokrouhlení na korunu stačí

    @Test
    fun `nulova mzda vraci nuly`() {
        val r = CleanSalaryCalculator.calculate(0.0)
        approx(0.0, r.netMonthly)
        approx(0.0, r.taxMonthly)
    }

    @Test
    fun `socialni pojisteni je 65 procent z hrube`() {
        val r = CleanSalaryCalculator.calculate(30_000.0)
        approx(1_950.0, r.socialInsurance)
    }

    @Test
    fun `zdravotni pojisteni je 45 procent z hrube`() {
        val r = CleanSalaryCalculator.calculate(30_000.0)
        approx(1_350.0, r.healthInsurance)
    }

    @Test
    fun `minimalni zdravotni pojisteni se uplatni pri nizke mzde`() {
        val r = CleanSalaryCalculator.calculate(2_000.0) // 4,5 % = 90 < 456
        approx(CleanSalaryCalculator.MIN_HEALTH, r.healthInsurance)
    }

    @Test
    fun `cista mzda bez deti`() {
        // hrubá 30 000: sociální 1 950, zdravotní 1 350,
        // daň = (28 050 × 12 × 15 % − 24 840) / 12 = 2 137,5
        val r = CleanSalaryCalculator.calculate(30_000.0)
        approx(2_137.5, r.taxMonthly)
        approx(24_562.5, r.netMonthly)
    }

    @Test
    fun `odpocet za dite snizi dan`() {
        // hrubá 50 000: roční daň bez dětí 84 150 − 24 840 = 59 310; s 2 dětmi −36 000 → 23 310
        val bezDite = CleanSalaryCalculator.calculate(50_000.0, children = 0)
        val sDeti = CleanSalaryCalculator.calculate(50_000.0, children = 2)
        // 2 děti → −36 000 Kč/rok daně → −3 000 Kč/měs.
        approx(bezDite.taxMonthly - 3_000.0, sDeti.taxMonthly)
    }

    @Test
    fun `pri nizke mzde je dan nulova`() {
        // hrubá 12 000: základ 11 220 × 12 = 134 640; 15 % = 20 196 < sleva 24 840 → daň 0
        val r = CleanSalaryCalculator.calculate(12_000.0)
        approx(0.0, r.taxMonthly)
        approx(12_000.0 - 780.0 - 540.0, r.netMonthly)
    }

    @Test
    fun `cista mzda je mensi nez hruba`() {
        val r = CleanSalaryCalculator.calculate(50_000.0)
        assert(r.netMonthly < r.grossMonthly)
        assert(r.netMonthly > 0.0)
    }
}
