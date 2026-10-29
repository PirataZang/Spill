package app.spill

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// Master artwork: docs/icon.svg (viewBox -12 44 264 160). Cards fan out around (120, 236).
private class Face(val color: Color, val detail: Float) // detail: opacity multiplier of the white ornament
private val Faces = listOf(
    Face(Color(0xFF168048), 1f), Face(Color(0xFF2A5BE8), 1f), Face(Color(0xFFF7C531), 1.4f),
    Face(Color(0xFFD7263D), 1f), Face(Color(0xFF0D0D10), 1f),
)
private val Bubble: Path = PathParser().parsePathString("M107 97h26a6 6 0 0 1 6 6v11a6 6 0 0 1-6 6h-14l-8 7v-7h-4a6 6 0 0 1-6-6v-11a6 6 0 0 1 6-6z").toPath()
private val Pivot = Offset(120f, 236f)
private val Ease = CubicBezierEasing(.2f, .8f, .2f, 1f)

/** The Spill logo: five cards fanned out, the black one carrying a speech bubble. Fans open on first show. */
@Composable
fun Logo(modifier: Modifier = Modifier) {
    val open = remember { Faces.map { Animatable(0f) } }
    LaunchedEffect(Unit) { open.forEachIndexed { i, a -> a.animateTo(1f, tween(900, 150 + i * 70, Ease)) } }

    Canvas(modifier.aspectRatio(264f / 160f)) {
        val k = size.width / 264f
        scale(k, k, Offset.Zero) {
            translate(12f, -44f) {
                Faces.forEachIndexed { i, f ->
                    val t = open[i].value
                    translate(0f, 10f * (1 - t)) { rotate((i - 2) * 15f * t, Pivot) { card(f, i == Faces.lastIndex, k) } }
                }
            }
        }
    }
}

private fun DrawScope.card(f: Face, black: Boolean, k: Float) {
    val body = RoundRect(84f, 56f, 156f, 160f, CornerRadius(11f))
    drawIntoCanvas { c ->
        val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = f.color.toArgb()
            setShadowLayer(5f * k, 0f, 4f * k, 0x52000000)
        }
        c.nativeCanvas.drawRoundRect(84f, 56f, 156f, 160f, 11f, 11f, p)
    }
    if (black) drawRoundRect(Color.White.copy(.16f), Offset(84.5f, 56.5f), Size(71f, 103f), CornerRadius(10.5f), Stroke(1f))
    val white = { a: Float -> Color.White.copy((a * f.detail).coerceAtMost(1f)) }
    drawRoundRect(white(.6f), Offset(89.5f, 61.5f), Size(61f, 93f), CornerRadius(7f), Stroke(2f))
    if (!black) clipPath(Path().apply { addRoundRect(body) }) {
        rotate(-28f, Offset(120f, 110f)) {
            drawOval(white(.08f), Offset(74f, 93f), Size(92f, 34f))
            drawOval(white(.42f), Offset(74f, 93f), Size(92f, 34f), style = Stroke(2f))
        }
    } else {
        drawPath(Bubble, Color.White)
        listOf(113f, 120f, 127f).forEach { drawCircle(Color(0xFF0D0D10), 2.4f, Offset(it, 108.5f)) }
    }
    drawCircle(Color.White, 3.2f, Offset(97f, 74f))
}
