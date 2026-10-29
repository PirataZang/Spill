package app.spill

import org.json.JSONArray
import org.json.JSONObject

/**
 * Everything the app shows lives in assets/catalog (see README, "Como adicionar perguntas").
 * Loading validates that every deck and card has text for every declared language, so a
 * forgotten translation fails the unit test instead of crashing a player mid-game.
 */
class Catalog private constructor(
    val languages: List<Language>,
    val decks: List<Deck>,
    private val strings: Map<String, JSONObject>,
) {
    class Language(val code: String, val label: String)
    class Text(val title: String, val prompt: String)
    class Meta(val name: String, val label: String, val blurb: String)
    class Deck(val id: String, val color: Long, val ink: Long, val glow: Long, val meta: Map<String, Meta>) {
        val cards = ArrayList<Card>()
    }
    class Card(val deck: Deck, val n: Int, val kind: String, val by: String?, val text: Map<String, Text>)

    val cards: List<Card> = decks.flatMap { it.cards }

    fun t(lang: String, key: String): String = strings.getValue(lang).getString(key)
    fun kind(lang: String, kind: String): String = strings.getValue(lang).getJSONObject("kinds").getString(kind)
    fun steps(lang: String) = strings.getValue(lang).getJSONArray("steps").objects().map { it.getString("b") to it.getString("t") }

    companion object {
        fun load(read: (path: String) -> String): Catalog {
            val index = JSONObject(read("index.json"))
            val langs = index.getJSONArray("languages").objects().map { Language(it.getString("code"), it.getString("label")) }
            val strings = langs.associate { it.code to JSONObject(read("i18n/${it.code}.json")) }

            val decks = (0 until index.getJSONArray("decks").length()).map { index.getJSONArray("decks").getString(it) }.map { id ->
                val j = JSONObject(read("decks/$id.json"))
                val color = hex(j.getString("color"))
                val meta = langs.associate { l ->
                    l.code to j.getJSONObject("meta").getJSONObject(l.code).let { Meta(it.getString("name"), it.getString("label"), it.getString("blurb")) }
                }
                Deck(id, color, hex(j.getString("ink")), j.optString("glow").takeIf { it.isNotEmpty() }?.let(::hex) ?: color, meta).also { deck ->
                    j.getJSONArray("cards").objects().forEachIndexed { i, c ->
                        val kind = c.getString("kind")
                        langs.forEach { check(strings.getValue(it.code).getJSONObject("kinds").has(kind)) { "$id #${i + 1}: kind '$kind' missing in i18n/${it.code}.json" } }
                        val text = langs.associate { l ->
                            l.code to try { c.getJSONObject(l.code).let { Text(it.getString("title"), it.getString("prompt")) } }
                            catch (e: Exception) { error("$id #${i + 1}: missing '${l.code}' title/prompt") }
                        }
                        deck.cards += Card(deck, i + 1, kind, c.optString("by").takeIf { it.isNotEmpty() }, text)
                    }
                }
            }
            return Catalog(langs, decks, strings)
        }

        private fun hex(s: String) = s.removePrefix("#").toLong(16) or 0xFF000000
        private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    }
}
