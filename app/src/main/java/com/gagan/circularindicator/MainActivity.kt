package com.gagan.circularindicator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gagan.trivia_wheel.TriviaWheel
import kotlin.random.Random

private const val TAG = "MainActivity::"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var rotation by remember {
                mutableStateOf(0f)
            }

            var infoText by remember {
                mutableStateOf("")
            }

            val random = Random(System.currentTimeMillis())

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.size(80.dp))
                TriviaWheel(canvasSize = 300.dp, rotation = rotation, animationFinished = {
                    infoText = "Index Selected $it"
                }, setRotation = {
                    rotation += (random.nextFloat() * 2000f).coerceAtLeast(1000f)
                }, animationDurationMillis = 4000)
                Spacer(modifier = Modifier.size(20.dp))
                Text(text = infoText, style = TextStyle(fontSize = 24.sp))
            }
        }
    }
}