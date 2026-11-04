package com.mathewhaug.boardgamehelper.data.remote

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

// a pull parser reads the XML one tag at a time and we ask for the next one, so it never holds the
// whole document as a tree. it is also built into Android, so there is no library to add
//
// BGG leaves fields out now and then, so anything missing falls back to a safe default instead of
// throwing. the one exception is the id: without it there is nothing to key a row on, so that item
// is skipped. XML that is broken outright still throws, and safeApiCall reports it as BadData

const val UNKNOWN_NAME = "Unknown game"

fun parseHot(input: InputStream): List<HotItemDto> {
    val parser = newParser(input)
    val items = mutableListOf<HotItemDto>()
    forEachItem(parser) { id, rank ->
        var name: String? = null
        var year: Int? = null
        var thumbnail: String? = null
        forEachChild(parser) {
            when (parser.name) {
                "name" -> name = parser.value()
                "yearpublished" -> year = parser.value()?.toIntOrNull()
                "thumbnail" -> thumbnail = parser.value()
            }
        }
        if (id != null) {
            items += HotItemDto(id, rank ?: 0, name ?: UNKNOWN_NAME, year, thumbnail)
        }
    }
    return items
}

fun parseThings(input: InputStream): List<ThingItemDto> {
    val parser = newParser(input)
    val items = mutableListOf<ThingItemDto>()
    forEachItem(parser) { id, _ ->
        var name: String? = null
        var year: Int? = null
        var minPlayers: Int? = null
        var maxPlayers: Int? = null
        var playingTime: Int? = null
        var thumbnail: String? = null
        forEachChild(parser) {
            when (parser.name) {
                // a game has one primary name and any number of alternate ones in other
                // languages, and only the primary one belongs on the list
                "name" -> if (parser.attr("type") == "primary") name = parser.value()
                "yearpublished" -> year = parser.value()?.toIntOrNull()
                "minplayers" -> minPlayers = parser.value()?.toIntOrNull()
                "maxplayers" -> maxPlayers = parser.value()?.toIntOrNull()
                "playingtime" -> playingTime = parser.value()?.toIntOrNull()
                "thumbnail" -> thumbnail = parser.nextText().trim().ifBlank { null }
            }
        }
        if (id != null) {
            items += ThingItemDto(
                id = id,
                name = name ?: UNKNOWN_NAME,
                yearPublished = year,
                minPlayers = minPlayers ?: 0,
                maxPlayers = maxPlayers ?: 0,
                playingTimeMinutes = playingTime ?: 0,
                thumbnailUrl = thumbnail,
            )
        }
    }
    return items
}

private fun newParser(input: InputStream): XmlPullParser =
    XmlPullParserFactory.newInstance().newPullParser().apply {
        // null lets the parser read the encoding from the XML declaration instead of guessing
        setInput(input, null)
    }

// calls block once per top level <item>, with the parser sitting on its start tag. thing can nest
// other <item> tags deeper down (versions, for one), so only the ones right under <items> count
private fun forEachItem(parser: XmlPullParser, block: (id: Int?, rank: Int?) -> Unit) {
    while (parser.next() != XmlPullParser.END_DOCUMENT) {
        if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item" && parser.depth == 2) {
            block(parser.attr("id")?.toIntOrNull(), parser.attr("rank")?.toIntOrNull())
        }
    }
}

// walks the direct children of the current tag and stops on its end tag. depth is what keeps a
// <name> inside a poll or a link from being mistaken for the game's own name
private fun forEachChild(parser: XmlPullParser, block: () -> Unit) {
    val itemDepth = parser.depth
    while (true) {
        when (parser.next()) {
            XmlPullParser.END_DOCUMENT -> return
            XmlPullParser.END_TAG -> if (parser.depth == itemDepth) return
            XmlPullParser.START_TAG -> if (parser.depth == itemDepth + 1) block()
        }
    }
}

private fun XmlPullParser.attr(name: String): String? = getAttributeValue(null, name)

// BGG puts almost every field in a value attribute. blank counts as missing
private fun XmlPullParser.value(): String? = attr("value")?.trim()?.ifBlank { null }
