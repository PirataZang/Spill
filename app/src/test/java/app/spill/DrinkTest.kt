package app.spill

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkTest {
    private val drink = DrinkCatalog.load { File("src/main/assets/catalog", it).readText() }

    // 200 per game, split 50/100/50 by level.
    @Test fun catalogShape() {
        for (kind in DrinkKind.entries) {
            val of = drink.cards.filter { it.kind == kind }
            assertEquals("$kind", 200, of.size)
            assertEquals("$kind", listOf(50, 100, 50), (1..3).map { l -> of.count { it.level == l } })
            assertEquals("$kind has duplicates", of.size, of.map { it.main }.toSet().size)
            assertTrue(of.all { (it.all != null) == (kind == DrinkKind.Dare) })
        }
    }

    // every ui string a screen asks for must exist, or the app would crash at runtime
    @Test fun uiKeys() {
        val keys = listOf(
            "gateBack", "gateTitle", "gateBody", "gateNote", "gateYes", "gateNo", "menu", "eyebrow", "setupTitle", "game", "gameCount",
            "levels", "level1", "level2", "level3", "climb", "climbDesc", "light", "lightDesc", "rulesTitle", "start",
            "unitShot", "unitShots", "unitSip", "unitSips", "level", "hint", "next", "done", "endTitle", "endText", "again", "menuShort", "climbTag",
            "answerOr", "forAll", "skipped", "neverLead", "neverDrinks", "neverSolo", "doitLead", "doitRefuse", "doitJudge",
            "holdTitle", "holdBody", "secretLabel", "revealedLabel", "nameOnly", "namedDrinks", "nobody", "drank", "pass", "holdHint",
            "likelyLead", "likelyPoint", "likelyDrinks", "howTo", "close", "rulesLabel", "howLevels", "howSafe",
        ) + DrinkGame.entries.flatMap { g -> listOf(g.key, g.key + "Desc") + (if (g == DrinkGame.Mix) emptyList() else listOf("rule" + g.key.replaceFirstChar { it.uppercase() })) } +
            DrinkKind.entries.flatMap { k -> listOf("tag" + k.key.replaceFirstChar { it.uppercase() }, "foot" + k.key.replaceFirstChar { it.uppercase() }) }
        keys.forEach { drink.t(it) }
        // the rules dialog reads a lead per game and steps per kind
        DrinkGame.entries.forEach { drink.t("how" + it.key.replaceFirstChar { c -> c.uppercase() }) }
        DrinkKind.entries.forEach { assertTrue(drink.list("how" + it.key.replaceFirstChar { c -> c.uppercase() } + "Steps").isNotEmpty()) }
    }

    @Test fun dealRespectsConfig() {
        assertEquals(400, drink.deal(DrinkConfig()).size)
        assertEquals(200, drink.deal(DrinkConfig(game = DrinkGame.Dare)).size)
        assertEquals(200, drink.deal(DrinkConfig(game = DrinkGame.Likely)).size)
        assertEquals(100, drink.deal(DrinkConfig(game = DrinkGame.Paranoia, levels = setOf(2))).size)
        val climb = drink.deal(DrinkConfig()).map { it.level }
        assertEquals(climb.sorted(), climb)                          // Escalada: 1s, then 2s, then 3s
        assertTrue(drink.deal(DrinkConfig(climb = false)).map { it.level } != climb)
        val mixed = drink.deal(DrinkConfig(levels = setOf(1)))
        assertTrue(mixed.zipWithNext().all { (a, b) -> a.kind != b.kind })  // alternated
        assertEquals("1 gole", drink.amount(1, light = true))
        assertEquals("3 shots", drink.amount(3, light = false))
    }
}
