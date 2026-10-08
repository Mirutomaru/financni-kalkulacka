package com.mirda.financnikalkulacky

import com.mirda.financnikalkulacky.ui.specs.ALL_SPECS
import com.mirda.financnikalkulacky.ui.specs.LIFE_STAGES
import com.mirda.financnikalkulacky.ui.specs.findSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Testy navigace podle životních etap (Tip 1) a propojení kalkulaček (Tip 3). */
class LifeStagesTest {

    @Test
    fun `kazda etapa ma aspon tri kalkulacky`() {
        LIFE_STAGES.forEach { stage ->
            assertTrue(stage.calcIds.size >= 3, "etapa ${stage.id} má jen ${stage.calcIds.size} kalkulačky")
        }
    }

    @Test
    fun `vsechna ID etap se rozlisi na specy`() {
        LIFE_STAGES.forEach { stage ->
            val specs = stage.specs()
            assertEquals(stage.calcIds.size, specs.size, "etapa ${stage.id} má neznámé ID")
            specs.forEach { assertNotNull(it) }
        }
    }

    @Test
    fun `ID etap jsou unikátni`() {
        val ids = LIFE_STAGES.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `vsechna ID kalkulaček jsou unikátní po opravě kolize renta`() {
        val ids = ALL_SPECS.map { it.id }
        val dups = ids.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertTrue(dups.isEmpty(), "duplicitní ID: $dups")
    }

    @Test
    fun `renamed annuita existuje a stara renta zbyla jen v bydleni`() {
        val annuita = findSpec("annuita")
        assertNotNull(annuita, "spec annuita chybí")
        assertTrue(annuita.title.contains("Renta"))
        // bydlenická renta (investiční byt) zůstává
        val housingRenta = findSpec("renta")
        assertNotNull(housingRenta, "spec renta chybí")
        assertEquals("Investiční byt & rentabilita nájmu", housingRenta.title)
    }

    @Test
    fun `related ID odkazuji na existujici kalkulacky`() {
        ALL_SPECS.forEach { spec ->
            spec.related.forEach { id ->
                assertNotNull(findSpec(id), "spec ${spec.id} odkazuje na neexistující $id")
            }
        }
    }

    @Test
    fun `related odkazy jsou vzajemne konzistentni pro klíčove páry`() {
        // mzda ↔ obrácená mzda, nemocenská ↔ mateřská
        val mzda = findSpec("mzda")!!
        assertTrue("obraceda-mzda" in mzda.related)
        val obracena = findSpec("obraceda-mzda")!!
        assertTrue("mzda" in obracena.related)

        val nemocenska = findSpec("nemocenska")!!
        assertTrue("mateřska" in nemocenska.related)
    }

    @Test
    fun `findSpec vrati null pro neznamé ID`() {
        assertEquals(null, findSpec("neexistujici-kalkulacka"))
    }
}
