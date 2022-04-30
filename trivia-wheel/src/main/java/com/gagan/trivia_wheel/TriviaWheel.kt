package com.gagan.trivia_wheel

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TriviaWheel(
    canvasSize: Dp = 200.dp,
    numberOfSlices: Int = 10,
    groutWidth: Float = 5f,
    rotation: Float = 0f,
    animationFinished: (Int) -> Unit,
    setRotation: () -> Unit,
    animationDurationMillis: Int = 2000
) {

    val arcAngle = (360 / numberOfSlices).toFloat()

    val startAnglesList = (0..((360 - arcAngle).toInt()) step arcAngle.toInt()).map { it.toFloat() }
        .map { Pair(it, it + arcAngle) }

    val animatedRotationValue by animateFloatAsState(
        targetValue = rotation, tween(
            durationMillis = animationDurationMillis
        ), finishedListener = {

            val afterAnimationRotations = startAnglesList.map { it.addToDegreeValue(rotation) }

            val indexOfSelectedSlice = getIndexOfSelectedDegree(afterAnimationRotations)

            animationFinished.invoke(indexOfSelectedSlice)

        }
    )

    Box(
        modifier = Modifier
            .size(canvasSize)
            .drawBehind {

                val radius = size.width / 2.0f

                withTransform(
                    {
                        //Spins the Wheel
                        rotate(animatedRotationValue)
                    }
                ) {
                    // Draw Slices
                    for ((index, startAngle) in (0..numberOfSlices).zip(0..((360 - arcAngle).toInt()) step arcAngle.toInt())) {
                        val slicePath = makeSlicePath(startAngle, radius, arcAngle)
                        val color =
                            if (index % 2 == 0) Color.White else Color.Green.copy(alpha = 0.5f)
                        drawPath(slicePath, color = color)
                    }

                    // Draw Text
                    for ((index, startAngle) in (0..numberOfSlices).zip((arcAngle / 2).toInt()..((360).toInt()) step arcAngle.toInt())) {

                        val startAngleRadians = Math.toRadians(startAngle.toDouble())
                        val textCircularPathRadius = size.width / 4f + 60.dp.value

                        val xPointOnCircle = center.x + (cos(startAngleRadians) * textCircularPathRadius).toFloat()
                        val yPointOnCircle = center.y + (sin(startAngleRadians) * textCircularPathRadius).toFloat()


                        val textSize = 72f
                        val paint = Paint().apply {
                            this.textSize = textSize
                            color = android.graphics.Color.BLACK
                            textAlign = Paint.Align.CENTER
                        }

                        val rect = android.graphics.Rect()

                        paint.getTextBounds("$index", 0, 1, rect)

                        rotate(
                            startAngle.toFloat() - 90f,
                            pivot = Offset(xPointOnCircle, yPointOnCircle + rect.exactCenterY())
                        ) {
                            drawText(
                                index,
                                topLeft = Offset(xPointOnCircle, yPointOnCircle),
                                paint
                            )
                        }
                    }

                    drawGrout(arcAngle, center, radius, groutWidth)
                }

                drawPath(drawPin(), color = Color.Red)

            }, contentAlignment = Alignment.Center
    ) {
        Button(
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(2.dp),
            onClick = {
                      setRotation()
            }, shape = CircleShape
        ) {
            Text(text = "SPIN", fontSize = 8.sp)
        }
    }
}

private fun getIndexOfSelectedDegree(listOfDegreePairs: List<Pair<Float, Float>>): Int {

    val degree = 270f

    for ((index, degreePair) in listOfDegreePairs.indices.zip(listOfDegreePairs)) {
        if (degree isBetween degreePair) {
            return index
        } else {
            continue
        }
    }
    return -1
}

private infix fun Float.isBetween(pair: Pair<Float, Float>) = this >= pair.first && this < pair.second

private fun Pair<Float, Float>.addToDegreeValue(value: Float): Pair<Float, Float> {
    val newStartAngle = (this.first + value) % 360
    val newEndAngle = (this.second + value) % 360

    val newPair = Pair(newStartAngle, newEndAngle)
    return if (newPair.first < newPair.second) {
        Pair(newStartAngle, newEndAngle)
    } else {
        Pair(newStartAngle, newEndAngle + 360)
    }
}

private fun DrawScope.drawText(
    index: Int,
    topLeft: Offset = Offset.Zero,
    paint: Paint
) {
    drawContext.canvas.nativeCanvas.drawText(index.toString(), topLeft.x, topLeft.y, paint)
}

private fun DrawScope.drawPin() = Path().apply {

    val sweepAngle = 20f

    val startAngle = 270f - (sweepAngle / 2f)

    arcTo(
        Rect(topLeft = Offset.Zero, bottomRight = Offset(size.width, size.height)),
        startAngle,
        sweepAngle,
        true
    )
    lineTo(center.x, 40f)
    close()
}

private fun DrawScope.makeSlicePath(startAngle: Int, radius: Float, arcAngle: Float) =
    Path().apply {

        val radiansStartAngle = (startAngle * PI / 180).toFloat()

        val xPointOnCircle = center.x + cos(radiansStartAngle) * radius
        val yPointOnCircle = center.y + sin(radiansStartAngle) * radius


        moveTo(center.x, center.y)
        lineTo(xPointOnCircle, yPointOnCircle)
        arcTo(
            rect = Rect(
                topLeft = Offset.Zero, bottomRight =
                Offset(size.width, size.height)
            ),
            startAngleDegrees = startAngle.toFloat(),
            sweepAngleDegrees = arcAngle,
            forceMoveTo = false
        )
        close()
    }

private fun DrawScope.drawGrout(arcAngle: Float, center: Offset, radius: Float, groutWidth: Float) =
    apply {
        for (startAngle in 0..((360 - arcAngle).toInt()) step arcAngle.toInt()) {

            drawPath(path = Path().apply {

                val radiansStartAngle = (startAngle * PI / 180).toFloat()

                val xPointOnCircle = center.x + cos(radiansStartAngle) * radius
                val yPointOnCircle = center.y + sin(radiansStartAngle) * radius

                moveTo(center.x, center.y)
                lineTo(xPointOnCircle, yPointOnCircle)
            }, color = Color.Black, style = Stroke(width = groutWidth))
        }

        drawCircle(color = Color.Black, center = center, style = Stroke(width = groutWidth))
    }