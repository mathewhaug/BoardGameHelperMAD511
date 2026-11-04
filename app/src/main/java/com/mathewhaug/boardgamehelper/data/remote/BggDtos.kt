package com.mathewhaug.boardgamehelper.data.remote

// these mirror what BGG sends, not what the app shows. the repository maps them onto Game, so a
// change in the XML stops here and never reaches a screen
//
// one row of the hot list. every field in hot lives in a value attribute, the thumbnail included
data class HotItemDto(
    val id: Int,
    val rank: Int,
    val name: String,
    val yearPublished: Int?,
    val thumbnailUrl: String?,
)

// one game from thing. same idea, except the thumbnail here is the text inside the tag, not an
// attribute, which is the easy one to get wrong
data class ThingItemDto(
    val id: Int,
    val name: String,
    val yearPublished: Int?,
    val minPlayers: Int,
    val maxPlayers: Int,
    val playingTimeMinutes: Int,
    val thumbnailUrl: String?,
)
