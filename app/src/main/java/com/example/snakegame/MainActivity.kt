package com.example.snakegame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.snakegame.ui.theme.DarkGreen
import com.example.snakegame.ui.theme.LightGreen
import com.example.snakegame.ui.theme.Orange
import com.example.snakegame.ui.theme.SnakeGameTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Random
import kotlin.math.abs
import kotlin.math.atan2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnakeGameTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val game = Game(lifecycleScope)
                    Snake(game)
                }
            }
        }
    }
}

data class GameState(
    val food: Pair<Int, Int> = Pair(5, 5), val snake: List<Pair<Int, Int>> = listOf(Pair(7, 7))
)

class Game(private val scope: CoroutineScope) {
    private val mutex = Mutex()
    val uiState = MutableStateFlow(GameState())

    var move = Pair(1, 0)
        set(value) {
            scope.launch {
                mutex.withLock {
                    field = value
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
                                (poz.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (poz.second + move.second + BOARD_SIZE) % BOARD_SIZE
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
                        food = if (newPosition == it.food) calculateFoodPosition(newSnake) else it.food
                    )
                }
            }
        }
    }

    companion object {
        const val BOARD_SIZE = 16
    }
}

fun calculateFoodPosition(snake: List<Pair<Int, Int>>): Pair<Int, Int> {
    while (true) {
        val newFoodPosition = Pair(
            Random().nextInt(Game.BOARD_SIZE), Random().nextInt(Game.BOARD_SIZE)
        )

        if (!snake.contains(newFoodPosition)) {
            return newFoodPosition
        }
    }
}

@Composable
fun Snake(game: Game) {
    val uiState by game.uiState.collectAsState(initial = null)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        uiState?.let {
            Board(it)
        }
//        Buttons(game.move) {
//            game.move = it
//        }

        TouchPad(game.move) {
            game.move = it
        }

    }

}

@Composable
fun TouchPad(currentDirection: (Pair<Int, Int>), onDirectionChange: (Pair<Int, Int>) -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (x, y) = dragAmount

                    when {
                        x > 0 && abs(x) > abs(y) -> {
                            // Right
                            if (currentDirection != Pair(-1, 0)) {
                                onDirectionChange(Pair(1, 0))
                            }
                        }

                        x < 0 && abs(x) > abs(y) -> {
                            // Left
//                            if (currentDirection != Pair(1, 0)) {
                                onDirectionChange(Pair(-1, 0))
//                            }
                        }

                        y > 0 && abs(x) < abs(y) -> {
                            // Down
                            if (currentDirection != Pair(0, -1)) {
                                onDirectionChange(Pair(0, 1))
                            }
                        }

                        y < 0 && abs(x) < abs(y)-> {
                            // Up
                            if (currentDirection != Pair(0, 1)) {
                                onDirectionChange(Pair(0, -1))
                            }
                        }
                    }

                }
            }
    )
}

@Composable
fun Buttons(currentDirection: (Pair<Int, Int>), onDirectionChange: (Pair<Int, Int>) -> Unit) {
    val buttonSize = Modifier.size(64.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Button(
            onClick = {
                if (currentDirection != Pair(0, 1)) {
                    onDirectionChange(Pair(0, -1))
                }
            },
            modifier = buttonSize
        ) {
            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null)
        }
        Row {
            Button(
                onClick = {
                    if (currentDirection != Pair(1, 0)) {
                        onDirectionChange(Pair(-1, 0))
                    }
                },
                modifier = buttonSize
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = null)
            }
            Spacer(modifier = buttonSize)
            Button(
                onClick = {
                    if (currentDirection != Pair(-1, 0)) {
                        onDirectionChange(Pair(1, 0))
                    }
                },
                modifier = buttonSize
            ) {
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
        Button(
            onClick = {
                if (currentDirection != Pair(0, -1)) {
                    onDirectionChange(Pair(0, 1))
                }
            },
            modifier = buttonSize
        ) {
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}

@Composable
fun Board(state: GameState) {
    BoxWithConstraints(Modifier.padding(16.dp)) {
        val tileSize = maxWidth / Game.BOARD_SIZE

        Box(
            Modifier
                .size(maxWidth)
                .border(2.dp, Color.Gray)
        )

        Box(
            modifier = Modifier
                .offset(
                    x = tileSize * state.food.first,
                    y = tileSize * state.food.second
                )
                .size(tileSize)
                .background(
                    Orange, CircleShape
                )
        )

        state.snake.forEachIndexed { index, poz ->
            Box(
                modifier = Modifier
                    .offset(
                        x = tileSize * poz.first,
                        y = tileSize * poz.second
                    )
                    .size(tileSize)
                    .background(
                        if (index == 0) DarkGreen else LightGreen,
                        RoundedCornerShape(5.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SnakePreview() {
    SnakeGameTheme {
        Snake(Game(rememberCoroutineScope()))
    }
}