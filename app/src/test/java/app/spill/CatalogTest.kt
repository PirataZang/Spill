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
        assertEquals(false, catalog.decks.first { it.id == "pink" }.inRandom) // Cantadas only play in Temático
        // every conversation deck has 50 cards; no prompt repeats inside a deck
        catalog.decks.forEach { assertEquals(it.id, 50, it.cards.size) }
        catalog.decks.forEach { d -> catalog.languages.forEach { l -> assertEquals("${d.id}/${l.code}", d.cards.size, d.cards.map { it.text.getValue(l.code).prompt }.toSet().size) } }
        catalog.languages.forEach { l -> listOf("rules", "deal", "count", "tagline").forEach { catalog.t(l.code, it) } }
    }
}
