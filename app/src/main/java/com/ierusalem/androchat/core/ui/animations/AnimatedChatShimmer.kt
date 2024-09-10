package com.ierusalem.androchat.core.ui.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ierusalem.androchat.features_local.tcp_conversation.presentation.components.ChatBubbleShapeEnd
import com.ierusalem.androchat.features_local.tcp_conversation.presentation.components.ChatBubbleShapeStart

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = ChatBubbleShapeEnd,
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "chat_transition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "chat_translate_anim"
    )

    var boxWidth by remember { mutableFloatStateOf(0f) }
    var boxHeight by remember { mutableFloatStateOf(0f) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if(shape == ChatBubbleShapeEnd) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = modifier
                .height(66.dp)
                .onGloballyPositioned {
                    boxWidth = it.size.width.toFloat()
                    boxHeight = it.size.height.toFloat()
                }
                .background(
                    brush = Brush.linearGradient(
                        colors = shimmerColors,
                        start = Offset(translateAnim.value - boxWidth, 0f),
                        end = Offset(translateAnim.value, boxHeight)
                    ),
                    shape = shape
                )
        )
    }
}

@Composable
fun ChatShimmerItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Shimmer for Image Placeholder
        for (i in 1..10) {
            ShimmerEffect(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(if (i % 2 == 0) 0.9F else 0.9F),
                shape = if (i % 2 == 0) ChatBubbleShapeEnd else ChatBubbleShapeStart
            )
        }
        repeat(10) {
            ShimmerEffect(
                modifier = Modifier.padding(bottom = 8.dp),
                shape = ChatBubbleShapeEnd
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shimmer for Text Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(20.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun ShimmerList() {
    Column {
        repeat(5) {
            ChatShimmerItem()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShimmerPreview() {
    ShimmerList()
}