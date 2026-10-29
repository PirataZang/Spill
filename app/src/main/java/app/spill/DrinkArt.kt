package app.spill

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.runtime.Composable

// Line art for the Drink mode, from the design's SVGs. Each Seg is one path of the drawing; `detail` paths use the thin stroke.
private class Seg(d: String, val alpha: Float = 1f, val detail: Boolean = false) {
    val path: Path = PathParser().parsePathString(d).toPath()
}

private val Bottle = listOf(
    Seg("M22.5 5.5A2.5 2.5 0 0 1 25 3H35A2.5 2.5 0 0 1 37.5 5.5V18H22.5Z"),
    Seg("M25.5 6V15.5M28.5 6V15.5M31.5 6V15.5M34.5 6V15.5", .6f, true),
    Seg("M21.8 18H38.2V21.5H21.8Z"),
    Seg("M24.5 21.5H35.5C35.5 29 36 35 37 40C38.5 46.5 53 49 53 59V129C53 133 50.5 135.5 46.5 135.5H13.5C9.5 135.5 7 133 7 129V59C7 49 21.5 46.5 23 40C24 35 24.5 29 24.5 21.5Z"),
    Seg("M24.2 30.5C28 31.6 32 31.6 35.8 30.5M24 33.5C28 34.6 32 34.6 36 33.5", 1f, true),
    Seg("M12 56C13.5 51.5 18 49.2 22.5 47.6", .6f, true),
    Seg("M48.5 64V126", .5f, true),
    Seg("M12 72C20 68 40 68 48 72V112C40 115.5 20 115.5 12 112Z"),
    Seg("M15.5 76.5C22.5 73.5 37.5 73.5 44.5 76.5M15.5 107.5C22.5 110.5 37.5 110.5 44.5 107.5", .7f, true),
    Seg("M23 102.5H37", .7f, true),
)

private val GlassBody = Seg("M10 8H50L46 58C45.8 61 44 62.5 41 62.5H19C16 62.5 14.2 61 14 58Z")
private val GlassLines = listOf(
    Seg("M10 8C20 10.6 40 10.6 50 8", .7f, true),
    Seg("M13.5 12L16.6 50H43.4L46.5 12", .5f, true),
    Seg("M19 16L20.8 44", .7f),
)
private val GlassBase = Seg("M15.2 50H44.8L46 58C45.8 61 44 62.5 41 62.5H19C16 62.5 14.2 61 14 58Z")
private val GlassLiquid = Seg("M14.3 22C24 24.2 36 24.2 45.7 22L43.4 50H16.6Z")

private val Skull = PathParser().parsePathString(
    "M12 2C17 2 20.5 5.4 20.5 10C20.5 12.6 19.3 14.6 17.5 15.8V19A1 1 0 0 1 16.5 20H15V18H13.5V20H10.5V18H9V20H7.5A1 1 0 0 1 6.5 19V15.8C4.7 14.6 3.5 12.6 3.5 10C3.5 5.4 7 2 12 2ZM6.6 10.8a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0ZM13 10.8a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0ZM12 13.4L13.1 15.4H10.9Z",
).toPath().apply { fillType = PathFillType.EvenOdd }

private fun DrawScope.line(s: Seg, color: Color, width: Float, thin: Float) =
    drawPath(s.path, color.copy(color.alpha * s.alpha), style = Stroke(if (s.detail) thin else width, cap = StrokeCap.Butt, join = StrokeJoin.Round))

/** Level marker: a filled skull. */
@Composable
fun Skull(modifier: Modifier, color: Color) = Canvas(modifier.aspectRatio(1f)) {
    val k = size.width / 24f
    scale(k, k, Offset.Zero) { drawPath(Skull, color) }
}

/** Bottle with a skull label. Gate uses 2.2 / 1.3 strokes, the card watermark 1.4 / 1.4. */
@Composable
fun BottleArt(modifier: Modifier, color: Color, width: Float = 2.2f, thin: Float = 1.3f) = Canvas(modifier.aspectRatio(60f / 140f)) {
    val k = size.width / 60f
    scale(k, k, Offset.Zero) {
        Bottle.forEach { line(it, color, width, thin) }
        translate(23f, 84f) { scale(.58f, .58f, Offset.Zero) { drawPath(Skull, color) } }
    }
}

/** Glass of red drink. [filled] = the gate version (solid red liquid); otherwise a translucent watermark. */
@Composable
fun GlassArt(modifier: Modifier, color: Color, filled: Boolean, width: Float = 2.2f, thin: Float = 1.3f) = Canvas(modifier.aspectRatio(60f / 70f)) {
    val k = size.width / 60f
    scale(k, k, Offset.Zero) {
        drawPath(GlassBase.path, if (filled) color.copy(.2f) else color.copy(color.alpha * .16f))
        drawPath(GlassLiquid.path, if (filled) Color(0xFFD7263D) else color.copy(color.alpha * .32f))
        line(GlassBody, color, width, thin)
        GlassLines.forEach { line(it, color, width, thin) }
    }
}

// ---- the other three games' icons (24x24 viewBox)

private class Part(d: String, val fill: Boolean = false, val stroke: Boolean = true) { val path: Path = PathParser().parsePathString(d).toPath() }

private val ScaleParts = listOf(
    Part("M12 5.2V20"), Part("M7.5 20.5H16.5"), Part("M4.2 7.3L19.8 3.1"),
    Part("M12 6.4a1.2 1.2 0 1 0 0 -2.4a1.2 1.2 0 1 0 0 2.4Z", fill = true),
    Part("M4.2 7.3L1.9 13M4.2 7.3L6.5 13"),
    Part("M1.4 13H7A2.8 2.8 0 0 1 1.4 13Z", fill = true), Part("M2.9 12.6L3.3 10.2H5.1L5.5 12.6Z", fill = true),
    Part("M19.8 3.1L17.5 8.8M19.8 3.1L22.1 8.8"), Part("M17 8.8H22.6A2.8 2.8 0 0 1 17 8.8Z"),
)
private val SpiralPath = PathParser().parsePathString("M12 12a1 1 0 0 1 2 0a2 2 0 0 1 -4 0a3 3 0 0 1 6 0a4 4 0 0 1 -8 0a5 5 0 0 1 10 0a6 6 0 0 1 -12 0").toPath()
private val SkullQSkull = PathParser().parsePathString(
    "M12 1.8C15.9 1.8 18.6 4.4 18.6 7.9C18.6 11.4 15.9 13.4 12 13.4C8.1 13.4 5.4 11.4 5.4 7.9C5.4 4.4 8.1 1.8 12 1.8ZM8.1 8.2a1.7 1.7 0 1 0 3.4 0a1.7 1.7 0 1 0 -3.4 0ZM12.5 8.2a1.7 1.7 0 1 0 3.4 0a1.7 1.7 0 1 0 -3.4 0ZM12 10.4L12.8 11.9H11.2Z",
).toPath().apply { fillType = PathFillType.EvenOdd }
private val SkullQJaw = PathParser().parsePathString("M7.6 12.2H11.6V14.6H10.7V13.6H9.6V14.6H8.6V13.6H7.6Z").toPath()
private val SkullQHook = PathParser().parsePathString("M16.2 11.6C15.6 14 12.4 14.3 12.4 16.6V17.4").toPath()

/** "Faz ou bebe": a scale tipping to one side. */
@Composable
fun ScaleArt(modifier: Modifier, color: Color, width: Float = 1.3f) = Canvas(modifier.aspectRatio(1f)) {
    scale(size.width / 24f, size.width / 24f, Offset.Zero) {
        val st = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round)
        ScaleParts.forEach { p ->
            if (p.fill) drawPath(p.path, color)
            if (p.stroke) drawPath(p.path, color, style = st)
        }
    }
}

/** "Paranoia": a spiral. */
@Composable
fun SpiralArt(modifier: Modifier, color: Color, width: Float = 1.5f) = Canvas(modifier.aspectRatio(1f)) {
    scale(size.width / 24f, size.width / 24f, Offset.Zero) { drawPath(SpiralPath, color, style = Stroke(width, cap = StrokeCap.Round)) }
}

/** "Mais provável": a skull whose jaw hooks into a question mark. */
@Composable
fun SkullQuestionArt(modifier: Modifier, color: Color) = Canvas(modifier.aspectRatio(1f)) {
    scale(size.width / 24f, size.width / 24f, Offset.Zero) {
        drawPath(SkullQSkull, color); drawPath(SkullQJaw, color)
        drawPath(SkullQHook, color, style = Stroke(2.6f, cap = StrokeCap.Round))
        drawCircle(color, 1.7f, Offset(12.4f, 21f))
    }
}
