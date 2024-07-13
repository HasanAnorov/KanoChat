package com.ierusalem.androchat.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ierusalem.androchat.core.ui.theme.AndroChatTheme
import com.ierusalem.androchat.core.utils.log

@Composable
fun CircularProgressBar(
    percentage: Float,
    number: Int = 100,
    fontSize: TextUnit = 16.sp,
    radius: Dp = 32.dp,
    color: Color = Color.Green,
    strokeWidth: Dp = 6.dp,
    animationDuration: Int = 1000,
    animDelay: Int = 0
) {
    log("circular progress bar - $percentage")
    var animationPlayed by rememberSaveable {
        mutableStateOf(false)
    }
    val currentPercentage = animateFloatAsState(
        label = "percentage",
        targetValue = if (animationPlayed) percentage else 0F,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animDelay
        )
    )
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        modifier = Modifier
            .size(radius * 2F)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
        content = {
            Canvas(modifier = Modifier.size(radius * 2f)) {
                drawArc(
                    color = color,
                    -90F,
                    360 * currentPercentage.value,
                    useCenter = false,
                    style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = (currentPercentage.value * number).toInt().toString() + "%",
                color = Color.Black,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    )
}

@Preview
@Composable
private fun PreviewLightCircularProgressBar() {
    AndroChatTheme {
        CircularProgressBar(
            percentage = 1F,
        )
    }
}

@Preview
@Composable
private fun PreviewDarkCircularProgressBar() {
    AndroChatTheme(isDarkTheme = true) {
        CircularProgressBar(
            percentage = 0.8F,
            number = 100
        )
    }
}