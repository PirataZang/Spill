package app.spill

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

private val Ease = CubicBezierEasing(.2f, .8f, .2f, 1f)
private val Exit = CubicBezierEasing(.4f, 0f, 1f, 1f)

/** Piecewise-linear lookup across deck positions 0..3 (top card, two peeking cards, hidden card). */
private fun at(e: Float, v0: Float, v1: Float, v2: Float, v3: Float): Float {
    val x = e.coerceIn(0f, 3f)
    val i = min(floor(x).toInt(), 2)
    val v = floatArrayOf(v0, v1, v2, v3)
    return v[i] + (v[i + 1] - v[i]) * (x - i)
}

@Composable
fun Game(vm: SpillViewModel) {
    var rules by remember { mutableStateOf(false) }
    val top = vm.deck.getOrNull(vm.index)
    val flinger = remember { Flinger() }
    BackHandler(onBack = vm::menu)

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            GhostText(vm.t("menu"), vm::menu)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Eyebrow(vm.round?.meta?.getValue(vm.lang)?.label ?: vm.t("random"))
                Txt(
                    if (top != null) "%02d / %02d".format(vm.index + 1, vm.deck.size) else vm.t("done"),
                    size = 13.sp, weight = 600, font = Display,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                LangToggle(vm.catalog.languages, vm.lang, vm.t("language"), vm::pickLang)
                RulesButton(null, { rules = true }, vm.t("rules"))
            }
        }
        DeckStage(
            Modifier.weight(1f).padding(top = 14.dp), vm.deck.size, vm.index, flinger, vm::next,
            cardWidth = { w, h -> min(min(w.value, (h.value - 44f) * .7f), 470f).dp },
            end = { End(vm) },
        ) { i, w, e, mod -> DeckCard(vm.deck[i], vm, w, e, mod) }
        Controls(vm.t("hint"), vm.t("next")) { flinger.fling() }
    }
    if (rules) Rules(vm) { rules = false }
}

/** Lets a button outside the stage fling the top card the same way a drag does. */
class Flinger { var fling: () -> Unit = {} }

/**
 * Stacked deck: the top card follows the finger and is flung off any edge; the next two peek out
 * below and rise as you drag. [card] draws card `i` at deck position [e] (0 = top). With [draggable] off the
 * card only leaves through [Flinger] (a button), which is how Paranoia keeps "press and hold" free for reading.
 */
@Composable
fun DeckStage(
    modifier: Modifier, count: Int, index: Int, flinger: Flinger, onNext: () -> Unit,
    cardWidth: (Dp, Dp) -> Dp, end: @Composable () -> Unit, draggable: Boolean = true,
    card: @Composable (i: Int, w: Dp, e: Float, modifier: Modifier) -> Unit,
) = BoxWithConstraints(modifier.fillMaxWidth()) {
    val dens = LocalDensity.current
    val scope = rememberCoroutineScope()
    val cardW = cardWidth(maxWidth, maxHeight)
    val limit = with(dens) { minOf(maxWidth, maxHeight).toPx() * .38f }
    val far = with(dens) { maxOf(maxWidth, maxHeight).toPx() * 1.3f }

    // Top card state, reset per card (keyed by index). `p` = drag progress that lifts the cards behind it.
    val off = remember(index) { Animatable(Offset.Zero, Offset.VectorConverter) }
    val spin = remember(index) { Animatable(0f) }
    val fade = remember(index) { Animatable(1f) }
    var flying by remember(index) { mutableStateOf(false) }
    val p = min(1f, hypot(off.value.x, off.value.y) / limit)

    fun fling(dir: Offset) {
        if (flying || index >= count) return
        flying = true
        val len = hypot(dir.x, dir.y).coerceAtLeast(1e-3f)
        scope.launch { spin.animateTo(dir.x / len * 35f, tween(400, easing = Exit)) }
        scope.launch { fade.animateTo(0f, tween(400)) }
        scope.launch {
            off.animateTo(dir / len * far, tween(400, easing = Exit))
            onNext()
        }
    }
    flinger.fling = { fling(Offset(1f, -.15f)) }

    Box(Modifier.fillMaxSize().padding(bottom = 44.dp), Alignment.Center) {
        if (index >= count) end() else
            (3 downTo 0).forEach { r ->
                val i = index + r
                if (i >= count) return@forEach
                key(i) {
                    val settle = remember { Animatable(1f) }
                    LaunchedEffect(r) { if (r == 3) settle.snapTo(1f) else if (settle.value > 0f) settle.animateTo(0f, tween(450, easing = Ease)) }
                    val e = if (r == 3) 3f else r - (if (r == 0) 0f else p) + settle.value
                    card(
                        i, cardW, e,
                        if (r != 0) Modifier else Modifier
                            .graphicsLayer {
                                translationX = off.value.x; translationY = off.value.y
                                rotationZ = off.value.x * .05f + spin.value
                                alpha = fade.value
                            }
                            .then(if (!draggable) Modifier else Modifier.pointerInput(index) {
                                detectDragGestures(
                                    onDrag = { change, d -> if (!flying) { change.consume(); scope.launch { off.snapTo(off.value + d) } } },
                                    onDragEnd = {
                                        if (flying) return@detectDragGestures
                                        val o = off.value
                                        if (hypot(o.x, o.y) > limit) fling(o) else scope.launch { off.animateTo(Offset.Zero, tween(450, easing = Ease)) }
                                    },
                                    onDragCancel = { if (!flying) scope.launch { off.animateTo(Offset.Zero, tween(450, easing = Ease)) } },
                                )
                            }),
                    )
                }
            }
    }
}

/** Card chrome shared by every deck: stack transform, shadow, ring, dimming for cards behind the top. */
@Composable
fun CardShell(w: Dp, e: Float, color: Color, ring: Color, description: String, modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(w * .07f)
    val dim = 1f - at(e, 1f, .7f, .48f, .48f)
    Box(
        modifier
            .size(w, w / .7f)
            .graphicsLayer {
                transformOrigin = TransformOrigin(.5f, 1f)
                translationY += at(e, 0f, 18f, 36f, 54f).dp.toPx()
                val s = at(e, 1f, .95f, .9f, .85f); scaleX = s; scaleY = s
                alpha *= at(e, 1f, 1f, 1f, 0f)
            }
            .semantics(mergeDescendants = true) {
                if (e < .5f) { liveRegion = LiveRegionMode.Polite; contentDescription = description }
            }
            .shadow(24.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .background(color, shape)
            .border(1.dp, ring, shape)
            .drawWithContent { drawContent(); if (dim > 0f) drawRect(Color.Black.copy(dim)) },
        content = content,
    )
}

@Composable
private fun DeckCard(card: Catalog.Card, vm: SpillViewModel, w: Dp, e: Float, modifier: Modifier) {
    val deck = card.deck
    val ink = Color(deck.ink)
    val text = card.text.getValue(vm.lang)
    val meta = deck.meta.getValue(vm.lang)
    val u = w.value / 100 // 1cqi
    val kind = vm.catalog.kind(vm.lang, card.kind) + (card.by?.let { " · $it" } ?: "")

    CardShell(w, e, Color(deck.color), ink.copy(.14f), "${meta.label}. ${text.title}. ${text.prompt}", modifier) {
        Box(
            Modifier.fillMaxSize().padding((3.4f * u).dp)
                .border(2.dp, ink.copy(.5f), RoundedCornerShape((5f * u).dp))
                .cardArt(ink, (5f * u).dp, 0.dp, 0.dp, 0.dp, 2.dp, .24f, framed = false, frameAlpha = 0f, ringAlpha = .2f, ringFill = .06f),
        ) {
            val no = "%02d".format(card.n)
            Txt(no, Modifier.align(Alignment.TopStart).padding(start = (4.5f * u).dp, top = (4f * u).dp), (6.5f * u).sp, 800, Display, ink)
            Txt(no, Modifier.align(Alignment.BottomEnd).padding(end = (4.5f * u).dp, bottom = (4f * u).dp).graphicsLayer { rotationZ = 180f }, (6.5f * u).sp, 800, Display, ink)
            Column(Modifier.fillMaxSize().padding(start = (7f * u).dp, end = (7f * u).dp, top = (6f * u).dp, bottom = (5f * u).dp), Arrangement.SpaceBetween, Alignment.CenterHorizontally) {
                Column(Modifier.weight(1f), Arrangement.Center, Alignment.CenterHorizontally) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy((4.5f * u).dp)) {
                        Txt(
                            kind.uppercase(),
                            Modifier.border(1.5.dp, ink.copy(.45f), CircleShape).padding(horizontal = (3.4f * u).dp, vertical = (1.6f * u).dp),
                            max(10f, 2.9f * u).sp, 500, Display, ink, ls = .14.em,
                        )
                        Txt(text.title, size = (if (text.title.length > 14) 9f * u else 12f * u).sp, weight = 800, color = ink, ls = (-.025).em, lh = 0.95.em, align = TextAlign.Center)
                        val ps = max(15f, 5.2f * u)
                        Txt(text.prompt, Modifier.widthIn(max = (ps * 13.2f).dp), ps.sp, 500, color = ink, lh = 1.32.em, align = TextAlign.Center)
                    }
                }
                Txt("${meta.name} · ${meta.label}".uppercase(), Modifier.graphicsLayer { alpha = .85f }, max(9f, 2.6f * u).sp, 400, Display, ink, ls = .2.em)
            }
        }
    }
}

@Composable
private fun End(vm: SpillViewModel) = EndCard(vm.t("endTitle"), vm.t("endText"), vm.t("again"), vm.t("menuShort"), { vm.deal(vm.round) }, vm::menu)

/** "That's the whole deck" screen, shared by every mode. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EndCard(title: String, text: String, again: String, menu: String, onAgain: () -> Unit, onMenu: () -> Unit) = Column(
    Modifier.fillMaxSize().padding(16.dp), Arrangement.spacedBy(14.dp, Alignment.CenterVertically), Alignment.CenterHorizontally,
) {
    Txt(title, size = 28.sp, weight = 700, font = Display, align = TextAlign.Center)
    Txt(text, Modifier.widthIn(max = 320.dp), color = Muted, lh = 1.45.em, align = TextAlign.Center)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Deal(again, onClick = onAgain)
        GhostText(menu, onMenu)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Controls(hint: String, next: String, onNext: () -> Unit) = FlowRow(
    Modifier.fillMaxWidth().padding(top = 6.dp),
    Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally), Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically,
) {
    Txt(hint, size = 13.sp, color = Muted, align = TextAlign.Center)
    GhostText(next, onNext)
}
