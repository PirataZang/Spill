package app.spill

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Bg = Color(0xFF16151B)
val Surface = Color(0xFF201E27)
val Raised = Color(0xFF2B2834)
val Line = Color(0xFF38343F)
val Fg = Color(0xFFF2F0F7)
val Muted = Color(0xFF9C97AB)
val Neutral = Color(0xFF4A4560) // ambient glow when no deck is in play

private fun family(res: Int, weights: IntRange) = FontFamily(weights.step(100).map {
    Font(res, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it)))
})
val Display = family(R.font.unbounded, 400..900)
val Body = family(R.font.bricolage, 400..800)

/** The card ornament shared by the logo fan, swatches, legend and the full card: inner frame + tilted ring. */
fun Modifier.cardArt(
    ink: Color, radius: Dp, frameInset: Dp, frameRadius: Dp, frameStroke: Dp, ringStroke: Dp,
    ringTop: Float, framed: Boolean = true, frameAlpha: Float, ringAlpha: Float, ringFill: Float = 0f, ring: Boolean = true,
) = clip(RoundedCornerShape(radius)).drawBehind {
    val inset = frameInset.toPx()
    if (framed) drawRoundRect(
        ink.copy(frameAlpha), Offset(inset, inset), Size(size.width - 2 * inset, size.height - 2 * inset),
        CornerRadius(frameRadius.toPx()), Stroke(frameStroke.toPx()),
    )
    if (!ring) return@drawBehind
    // CSS: inset: ringTop% -22%; border-radius 50%; rotate(-28deg)
    val ringSize = Size(size.width * 1.44f, size.height * (1 - 2 * ringTop))
    val topLeft = Offset(-size.width * .22f, size.height * ringTop)
    withTransform({ rotate(-28f, Offset(size.width / 2, size.height / 2)) }) {
        if (ringFill > 0f) drawOval(ink.copy(ringFill), topLeft, ringSize)
        drawOval(ink.copy(ringAlpha), topLeft, ringSize, style = Stroke(ringStroke.toPx()))
    }
}

/** Small card (theme swatches, rules legend): just the inner frame, like the new logo. */
@Composable
fun Mini(color: Color, ink: Color, w: Dp, h: Dp, modifier: Modifier = Modifier, radius: Dp = 9.dp, inset: Dp = 4.dp, frameRadius: Dp = 6.dp) {
    Box(
        modifier
            .size(w, h)
            .shadow(10.dp, RoundedCornerShape(radius), ambientColor = Color.Black, spotColor = Color.Black)
            .background(color, RoundedCornerShape(radius))
            .border(1.dp, ink.copy(.16f), RoundedCornerShape(radius))
            .cardArt(ink, radius, inset, frameRadius, 1.5.dp, 1.5.dp, .28f, frameAlpha = .55f, ringAlpha = 0f, ring = false)
    )
}
