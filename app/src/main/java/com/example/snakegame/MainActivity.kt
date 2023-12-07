package com.example.snakegame

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.snakegame.ui.theme.DarkGreen
import com.example.snakegame.ui.theme.LightGreen
import com.example.snakegame.ui.theme.Orange
import com.example.snakegame.ui.theme.SnakeGameTheme
import kotlin.math.abs

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

@Composable
fun Snake(game: Game) {
    val uiState by game.uiState.collectAsState(initial = null)
    val move = game.move.collectAsState()
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MyPreferences", 0) }
    var highScore by remember { mutableStateOf(sharedPreferences.getInt("highScore", 0)) }

    LaunchedEffect(uiState?.score) {
        if ((uiState?.score ?: 0) > highScore) {
            sharedPreferences.edit().putInt("highScore", uiState?.score ?: 0).apply()
            highScore = uiState?.score ?: 0
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        uiState?.let {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Score: ${it.score}"
                )
                Text(
                    "High Score: $highScore"
                )
            }

            Board(it)
        }

        TouchPad(currentDirection = move, onDirectionChange = game::setMove)
    }

}

@Composable
fun TouchPad(
    currentDirection: State<(Pair<Int, Int>)>,
    onDirectionChange: (Pair<Int, Int>) -> Unit
) {

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
                            if (currentDirection.value != Pair(-1, 0)) {
                                onDirectionChange(Pair(1, 0))
                            }
                        }

                        x < 0 && abs(x) > abs(y) -> {
                            // Left
                            if (currentDirection.value != Pair(1, 0)) {
                                onDirectionChange(Pair(-1, 0))
                            }
                        }

                        y > 0 && abs(x) < abs(y) -> {
                            // Down
                            if (currentDirection.value != Pair(0, -1)) {
                                onDirectionChange(Pair(0, 1))
                            }
                        }

                        y < 0 && abs(x) < abs(y) -> {
                            // Up
                            if (currentDirection.value != Pair(0, 1)) {
                                onDirectionChange(Pair(0, -1))
                            }
                        }
                    }

                }
            }
    )
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

