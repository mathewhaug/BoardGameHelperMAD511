package com.mathewhaug.boardgamehelper.data

data class Game(
    val id: Int,
    val name: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val playTimeMinutes: Int
)

// hard coded on purpose - this is what a repository will hand back once Room and the
// BoardGameGeek API show up in a later week, so the screens below are already written
// against a plain List<Game> and will not need to change when that swap happens
val SampleGames = listOf(
    Game(id = 1, name = "Catan", minPlayers = 3, maxPlayers = 4, playTimeMinutes = 90),
    Game(id = 2, name = "Ticket to Ride", minPlayers = 2, maxPlayers = 5, playTimeMinutes = 60),
    Game(id = 3, name = "Codenames", minPlayers = 2, maxPlayers = 8, playTimeMinutes = 15),
    Game(id = 4, name = "Wingspan", minPlayers = 1, maxPlayers = 5, playTimeMinutes = 70),
    Game(id = 5, name = "Azul", minPlayers = 2, maxPlayers = 4, playTimeMinutes = 45)
)
