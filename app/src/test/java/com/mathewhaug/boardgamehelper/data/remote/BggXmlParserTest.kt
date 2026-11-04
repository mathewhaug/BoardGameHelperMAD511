package com.mathewhaug.boardgamehelper.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// small inline XML for each edge case, so the test shows exactly what went in, plus the real
// sample files, so the shape the app actually loads is covered too
class BggXmlParserTest {

    private fun String.stream() = byteInputStream()

    @Test fun parseHot_readsValueAttributes() {
        val xml = """
            <items>
              <item id="13" rank="1">
                <thumbnail value="https://example.com/13.jpg"/>
                <name value="Catan"/>
                <yearpublished value="1995"/>
              </item>
            </items>
        """.trimIndent()

        assertEquals(
            listOf(HotItemDto(13, 1, "Catan", 1995, "https://example.com/13.jpg")),
            parseHot(xml.stream()),
        )
    }

    @Test fun parseThings_readsThumbnailAsTextAndPrimaryName() {
        val xml = """
            <items>
              <item type="boardgame" id="13">
                <thumbnail>  https://example.com/13.jpg  </thumbnail>
                <name type="alternate" value="Die Siedler von Catan"/>
                <name type="primary" value="Catan"/>
                <yearpublished value="1995"/>
                <minplayers value="3"/>
                <maxplayers value="4"/>
                <playingtime value="120"/>
              </item>
            </items>
        """.trimIndent()

        assertEquals(
            listOf(ThingItemDto(13, "Catan", 1995, 3, 4, 120, "https://example.com/13.jpg")),
            parseThings(xml.stream()),
        )
    }

    @Test fun parseThings_missingTagsFallBackToDefaults() {
        val xml = """<items><item type="boardgame" id="13"></item></items>"""

        val game = parseThings(xml.stream()).single()

        assertEquals(UNKNOWN_NAME, game.name)
        assertEquals(0, game.minPlayers)
        assertEquals(0, game.maxPlayers)
        assertEquals(0, game.playingTimeMinutes)
        assertNull(game.yearPublished)
        assertNull(game.thumbnailUrl)
    }

    @Test fun parseHot_missingTagsFallBackToDefaults() {
        val xml = """<items><item id="13"><name value="Catan"/></item></items>"""

        assertEquals(listOf(HotItemDto(13, 0, "Catan", null, null)), parseHot(xml.stream()))
    }

    @Test fun itemWithoutAnId_isSkipped() {
        val xml = """<items><item rank="1"><name value="Mystery"/></item></items>"""

        assertTrue(parseHot(xml.stream()).isEmpty())
    }

    @Test fun emptyList_parsesToEmpty() {
        assertTrue(parseHot("<items></items>".stream()).isEmpty())
        assertTrue(parseThings("""<items termsofuse="x"/>""".stream()).isEmpty())
    }

    // a <name> inside a nested tag must not overwrite the game's own name
    @Test fun parseThings_ignoresNestedNames() {
        val xml = """
            <items><item type="boardgame" id="13">
              <name type="primary" value="Catan"/>
              <poll name="suggested_numplayers"><results><name type="primary" value="Wrong"/></results></poll>
            </item></items>
        """.trimIndent()

        assertEquals("Catan", parseThings(xml.stream()).single().name)
    }

    @Test fun sampleHot_hasTwentyGamesWithThumbnails() {
        val items = parseHot(sampleAsset("sample_hot.xml").stream())

        assertEquals(20, items.size)
        assertEquals((1..20).toList(), items.map { it.rank })
        assertTrue(items.all { it.thumbnailUrl != null && it.name != UNKNOWN_NAME })
    }

    @Test fun sampleThings_matchesTheHotIds() {
        val hotIds = parseHot(sampleAsset("sample_hot.xml").stream()).map { it.id }
        val things = parseThings(sampleAsset("sample_things.xml").stream())

        assertEquals(hotIds, things.map { it.id })
        val catan = things.single { it.id == 13 }
        assertEquals(ThingItemDto(13, "Catan", 1995, 3, 4, 120, "https://example.com/bgg-sample/13_thumb.jpg"), catan)
    }
}
