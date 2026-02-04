package com.readlater.ui.theme

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.random.Random

fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
): Modifier = this.drawBehind {
    val transparentColor = android.graphics.Color.toArgb(color.copy(alpha = 0.0f).value.toLong())
    val shadowColor = android.graphics.Color.toArgb(color.copy(alpha = alpha).value.toLong())
    
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            shadowRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}

@Composable
fun rememberNoiseTexture(): androidx.compose.ui.graphics.ImageBitmap {
    return remember {
        val size = 200 // Small tile size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        
        // Generate noise
        for (x in 0 until size) {
            for (y in 0 until size) {
                // Random alpha noise for grain, black
                if (Random.nextFloat() > 0.5f) {
                    val alpha = Random.nextInt(20) // 0-20 alpha (approx 8%)
                    val color = AndroidColor.argb(alpha, 0, 0, 0)
                    bitmap.setPixel(x, y, color)
                } else {
                    bitmap.setPixel(x, y, AndroidColor.TRANSPARENT)
                }
            }
        }
        bitmap.asImageBitmap()
    }
}
