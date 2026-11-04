package com.mathewhaug.boardgamehelper.data.remote

import java.io.File

// gradle runs unit tests with the module folder as the working directory, so this path reaches
// the same files the app ships in its assets, and the tests can never drift from them
fun sampleAsset(name: String): String = File("src/main/assets/$name").readText()
