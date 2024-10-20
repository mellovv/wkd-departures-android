package dev.mellovv.wkd_departures.modules

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LoadingGlyph() {
    val listColors = listOf(Color.Transparent, Color.DarkGray, Color.Transparent)
    var targetValue by remember { mutableFloatStateOf(-1f) }

    val progression by animateFloatAsState(
        targetValue = targetValue,
        label = "progression",
        animationSpec = tween(
            durationMillis = if (targetValue == 2f) 500 else 0,
            easing = EaseInOut
        )
    )

    LaunchedEffect(targetValue) {
        if (targetValue != -1f) {
            delay(1000)
            targetValue = if (targetValue == 2f) 0f
            else 2f
        }
    }

    var componentHeight by remember { mutableFloatStateOf(0f) }
    var componentWidth by remember { mutableFloatStateOf(0f) }


    val background = Brush.linearGradient(
        colors = listColors,
        tileMode = TileMode.Decal,
        start = Offset(
            componentWidth * (progression - 1f),
            componentHeight * (progression - 1f)
        ),

        end = Offset(

            componentWidth * (progression),
            componentHeight * (progression)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                if (targetValue == -1f) targetValue = 2f
                componentHeight = it.size.height.toFloat()
                componentWidth = it.size.width.toFloat()
            }
            .background(background)
    ) {

    }
}