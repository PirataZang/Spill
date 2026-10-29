package app.spill

import org.json.JSONObject
import kotlin.random.Random

/** The five kinds of Drink card; [key] is the array name in drink.json. */
enum class DrinkKind(val key: String) { Dare("dare"), Never("never"), DoIt("doit"), Paranoia("paranoia"), Likely("likely"), Prefer("prefer") }

/** What the player picks on the setup screen; Mix plays two kinds, alternating. */
enum class DrinkGame(val key: String, val kinds: List<DrinkKind>) {
    Mix("mix", listOf(DrinkKind.Dare, DrinkKind.Never)),
    Dare("dare", listOf(DrinkKind.Dare)),
    Never("never", listOf(DrinkKind.Never)),
    DoIt("doit", listOf(DrinkKind.DoIt)),
    Paranoia("paranoia", listOf(DrinkKind.Paranoia)),
    Likely("likely", listOf(DrinkKind.Likely)),
    Prefer("prefer", listOf(DrinkKind.Prefer)),
}

/** [extra] is the "Para todos" prompt of a Desafio card and the second option of a Prefere card; null for the other kinds. */
class DrinkCard(val kind: DrinkKind, val level: Int, val main: String, val extra: String? = null)

data class DrinkConfig(
    val game: DrinkGame = DrinkGame.Mix,
    val levels: Set<Int> = setOf(1, 2, 3),
    val climb: Boolean = true,
    val light: Boolean = false,
)

/** Drink mode (+18): leveled 1..3 cards, one skull = one shot. Content: assets/catalog/drink.json. */
class DrinkCatalog private constructor(private val ui: JSONObject, val cards: List<DrinkCard>) {
    fun t(key: String): String = ui.getString(key)
    fun list(key: String): List<String> = ui.getJSONArray(key).let { a -> (0 until a.length()).map { a.getString(it) } }

    /** "2 shots" / "1 gole": unit follows the light-mode switch, plural follows [n]. */
    fun amount(n: Int, light: Boolean) = "$n " + t(if (light) (if (n == 1) "unitSip" else "unitSips") else (if (n == 1) "unitShot" else "unitShots"))
    fun unit(light: Boolean) = t(if (light) "unitSip" else "unitShot")

    /** Build one round: levels filtered, shuffled, kinds interleaved; [DrinkConfig.climb] orders by level. */
    fun deal(cfg: DrinkConfig, rnd: Random = Random): List<DrinkCard> {
        val pools = cfg.game.kinds.map { k -> cards.filter { it.kind == k && it.level in cfg.levels }.shuffled(rnd) }
        return if (cfg.climb) cfg.levels.sorted().flatMap { l -> weave(pools.map { p -> p.filter { it.level == l } }) } else weave(pools)
    }

    companion object {
        fun load(read: (String) -> String): DrinkCatalog {
            val j = JSONObject(read("drink.json"))
            val cards = DrinkKind.entries.flatMap { kind ->
                val arr = j.getJSONArray(kind.key)
                (0 until arr.length()).map { i ->
                    val c = arr.getJSONObject(i)
                    // Prefere has two options (a, b); every other kind has one text (main)
                    val main = c.getString(if (kind == DrinkKind.Prefer) "a" else "main")
                    val extra = when (kind) { DrinkKind.Dare -> c.getString("all"); DrinkKind.Prefer -> c.getString("b"); else -> null }
                    DrinkCard(kind, c.getInt("level").also { check(it in 1..3) { "drink.json ${kind.key}[$i]: level must be 1..3" } }, main, extra)
                }
            }
            return DrinkCatalog(j.getJSONObject("ui"), cards)
        }

        private fun <T> weave(lists: List<List<T>>): List<T> =
            (0 until (lists.maxOfOrNull { it.size } ?: 0)).flatMap { i -> lists.mapNotNull { it.getOrNull(i) } }
    }
}
