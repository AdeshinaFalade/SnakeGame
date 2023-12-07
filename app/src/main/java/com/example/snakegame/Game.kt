package com.example.snakegame

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Random

class Game(private val scope: CoroutineScope) {
    private val mutex = Mutex()
    val uiState = MutableStateFlow(GameState())

    private val _move = MutableStateFlow(Pair(1, 0))
    val move = _move.asStateFlow()

    fun setMove(newMove: Pair<Int, Int>) {
        scope.launch {
            mutex.withLock {
                _move.value = newMove
            }
        }
    }

    init {
        scope.launch {
            var snakeLength = 4

            while (true) {
                delay(150)
                uiState.update {
                    val newPosition = it.snake.first().let { poz ->
                        mutex.withLock {
                            Pair(
                                (poz.first + move.value.first + BOARD_SIZE) % BOARD_SIZE,
                                (poz.second + move.value.second + BOARD_SIZE) % BOARD_SIZE
                            )
                        }
                    }

                    if (newPosition == it.food) {
                        snakeLength++
                    }

                    if (it.snake.contains(newPosition)) {
                        snakeLength = 4
                    }
                    val newSnake = listOf(newPosition) + it.snake.take(snakeLength - 1)

                    it.copy(
                        snake = newSnake,
                        food = if (newPosition == it.food) calculateFoodPosition(newSnake) else it.food,
                        score = if (newPosition == it.food) {
                            it.score + 100
                        } else if (it.snake.contains(newPosition)) {
                            0
                        } else it.score
                    )
                }
            }
        }
    }

    private fun calculateFoodPosition(snake: List<Pair<Int, Int>>): Pair<Int, Int> {
        while (true) {
            val newFoodPosition = Pair(
                Random().nextInt(Game.BOARD_SIZE), Random().nextInt(Game.BOARD_SIZE)
            )

            if (!snake.contains(newFoodPosition)) {
                return newFoodPosition
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 16
    }
}