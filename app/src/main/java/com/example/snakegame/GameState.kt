package com.example.snakegame

data class GameState(
    val food: Pair<Int, Int> = Pair(5, 5),
    val snake: List<Pair<Int, Int>> = listOf(Pair(7, 7)),
    val score: Int = 0
)