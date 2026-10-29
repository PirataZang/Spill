package app.spill

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {
    // Fails if any card/deck lacks a declared language or a kind has no translation.
    @Test fun catalogIsComplete() {
        val root = File("src/main/assets/catalog")
        val catalog = Catalog.load { File(root, it).readText() }
        assertTrue(catalog.decks.size >= 2)
        catalog.decks.forEach { assertTrue("${it.id} is empty", it.cards.isNotEmpty()) }
        assertEquals(catalog.decks.sumOf { it.cards.size }, catalog.cards.size)
        catalog.languages.forEach { l -> listOf("rules", "deal", "count", "tagline").forEach { catalog.t(l.code, it) } }
    }
}
