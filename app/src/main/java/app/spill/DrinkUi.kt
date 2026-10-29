package app.spill

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.layout.layout
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val DrinkBg = Color(0xFF0B0A0E)
val Wine = Color(0xFF7A1022)
val Blood = Color(0xFFD7263D)
private val Panel = Color(0xFF17151C)
private val Soft = Color(0xFFC9C5D4)
private val DareInk = Color(0xFFF1EFF6)
private val NeverInk = Color(0xFFFFF1F3)
private val DareBlack = Color(0xFF0D0D10)

@Composable
private fun PlusBadge(size: Dp, stroke: Dp, font: Float) = Box(Modifier.size(size).border(stroke, Blood, CircleShape), Alignment.Center) {
    Txt("+18", size = font.sp, weight = 800, font = Display)
}

/** Ghost button that also fills the slot of a plain-text link. */
@Composable
private fun Outline(text: String, minHeight: Dp, onClick: () -> Unit) = Txt(
    text, Modifier.fillMaxWidth().heightIn(min = minHeight).clip(RoundedCornerShape(16.dp)).border(1.dp, Line, RoundedCornerShape(16.dp))
        .clickable(role = Role.Button, onClick = onClick).padding(vertical = 14.dp),
    15.sp, 600, align = TextAlign.Center,
)

// ---------------------------------------------------------------- +18 gate

@Composable
fun DrinkGate(vm: SpillViewModel) {
    val d = vm.drink
    BackHandler { vm.go(Screen.Home) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) { GhostText(d.t("gateBack")) { vm.go(Screen.Home) } }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
            Arrangement.spacedBy(22.dp, Alignment.CenterVertically), Alignment.CenterHorizontally,
        ) {
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Bottom) {
                BottleArt(Modifier.width(56.dp), Fg)
                GlassArt(Modifier.width(52.dp), Fg, filled = true)
            }
            Box(Modifier.size(84.dp).border(3.dp, Blood, CircleShape), Alignment.Center) { Txt("+18", size = 28.sp, weight = 900, font = Display, ls = (-.03).em) }
            Txt(d.t("gateTitle"), size = 40.sp, weight = 900, font = Display, ls = (-.03).em, lh = .95.em, align = TextAlign.Center)
            Txt(d.t("gateBody"), Modifier.widthIn(max = 280.dp), color = Soft, lh = 1.45.em, align = TextAlign.Center)
            Txt(d.t("gateNote"), Modifier.widthIn(max = 290.dp), size = 14.sp, color = Muted, lh = 1.45.em, align = TextAlign.Center)
        }
        }
        Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Deal(d.t("gateYes"), Modifier.fillMaxWidth().heightIn(min = 56.dp)) { vm.go(Screen.DrinkSetup) }
            Outline(d.t("gateNo"), 48.dp) { vm.go(Screen.Home) }
        }
    }
}

// ---------------------------------------------------------------- palette

/** Colors of one game: card face, ink for text and line art, ring around the card, ambient glow. */
class Palette(val color: Color, val ink: Color, val line: Color, val ring: Color, val glow: Color)

private val DareP = Palette(DareBlack, DareInk, Color.White, Color.White.copy(.18f), Color(0xFF5D5873))
private val NeverP = Palette(Wine, NeverInk, Color.White, Color.White.copy(.16f), Wine)
private val DoItP = Palette(Color(0xFF168048), Color(0xFFF2FFF7), Color.White, Color.White.copy(.12f), Color(0xFF168048))
private val ParanoiaP = Palette(Color(0xFF2A5BE8), Color(0xFFF4F6FF), Color.White, Color.White.copy(.12f), Color(0xFF2A5BE8))
private val LikelyInk = Color(0xFF1B1608)
private val LikelyP = Palette(Color(0xFFF7C531), LikelyInk, LikelyInk, LikelyInk.copy(.12f), Color(0xFFF7C531))

val DrinkKind.palette get() = when (this) {
    DrinkKind.Dare -> DareP; DrinkKind.Never -> NeverP; DrinkKind.DoIt -> DoItP; DrinkKind.Paranoia -> ParanoiaP; DrinkKind.Likely -> LikelyP
}
/** Ambient glow behind the setup screen: the mix is wine, every other game takes its own color. */
val DrinkGame.glow get() = if (this == DrinkGame.Mix) Wine else kinds.first().palette.glow

// ---------------------------------------------------------------- setup

@Composable
fun DrinkSetup(vm: SpillViewModel) {
    val d = vm.drink
    val cfg = vm.drinkCfg
    var rules by remember { mutableStateOf(false) }
    BackHandler { vm.go(Screen.Home) }
    if (rules) DrinkRules(vm) { rules = false }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            GhostText(d.t("menu")) { vm.go(Screen.Home) }
            Eyebrow(d.t("eyebrow"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RulesButton(null, { rules = true }, d.t("howTo"))
                PlusBadge(38.dp, 2.dp, 11f)
            }
        }
        Txt(d.t("setupTitle"), Modifier.padding(top = 4.dp), 30.sp, 900, Display, ls = (-.03).em, lh = 1.em)

        Column {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), Arrangement.SpaceBetween, Alignment.Bottom) {
                Eyebrow(d.t("game"))
                Txt(d.t("gameCount").replace("{n}", "${DrinkGame.entries.size}"), size = 12.sp, color = Muted)
            }
            // bleeds past the screen padding so the tiles scroll edge to edge
            Row(
                Modifier.bleed(16.dp).horizontalScroll(rememberScrollState()).selectableGroup().padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp),
                Arrangement.spacedBy(10.dp),
            ) {
                DrinkGame.entries.forEach { g -> GameTile(g, cfg.game == g, d.t(g.key), d.t(g.key + "Desc")) { vm.drinkCfg = cfg.copy(game = g) } }
            }
        }

        Column {
            Eyebrow(d.t("levels"), Modifier.padding(bottom = 10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..3).forEach { l ->
                    val on = l in cfg.levels
                    Column(
                        Modifier.weight(1f).heightIn(min = 76.dp).alpha(if (on) 1f else .55f)
                            .clip(RoundedCornerShape(14.dp)).background(if (on) Raised else Panel, RoundedCornerShape(14.dp))
                            .border(1.dp, if (on) Fg else Line, RoundedCornerShape(14.dp))
                            // at least one level must stay on
                            .clickable(role = Role.Checkbox) { val next = if (on) cfg.levels - l else cfg.levels + l; if (next.isNotEmpty()) vm.drinkCfg = cfg.copy(levels = next) }
                            .semantics { stateDescription = if (on) "on" else "off" }
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        Arrangement.spacedBy(6.dp), Alignment.CenterHorizontally,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) { repeat(l) { Skull(Modifier.size(18.dp), Color.White) } }
                        Txt(d.t("level$l"), size = 12.sp, weight = 700, font = Display)
                        Txt(d.amount(l, cfg.light), size = 11.sp, color = Muted)
                    }
                }
            }
        }

        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Panel).border(1.dp, Line, RoundedCornerShape(16.dp))) {
            Switch(d.t("climb"), d.t("climbDesc"), cfg.climb, { vm.drinkCfg = cfg.copy(climb = it) }, divider = true)
            Switch(d.t("light"), d.t("lightDesc"), cfg.light, { vm.drinkCfg = cfg.copy(light = it) })
        }

        val unit = d.unit(cfg.light)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp), Arrangement.spacedBy(6.dp)) {
            Txt(d.t("rulesTitle").uppercase(), size = 11.sp, weight = 400, font = Display, ls = .1.em)
            // the mix explains both of its games
            val rules = if (cfg.game == DrinkGame.Mix) listOf("ruleDare", "ruleNever") else listOf("rule" + cfg.game.key.replaceFirstChar { it.uppercase() })
            rules.forEach { Txt(d.t(it).replace("{unit}", unit), size = 13.sp, color = Soft, lh = 1.45.em) }
        }

        Deal(d.t("start").replace("{n}", "${vm.drinkCount}"), Modifier.fillMaxWidth().heightIn(min = 56.dp)) { vm.startDrink() }
    }
}

/** Lets a child ignore [by] of the parent's horizontal padding on both sides. */
private fun Modifier.bleed(by: Dp) = layout { m, c ->
    val extra = by.roundToPx()
    val p = m.measure(c.copy(maxWidth = c.maxWidth + 2 * extra))
    layout(c.maxWidth, p.height) { p.place(-extra, 0) }
}

@Composable
private fun GameTile(game: DrinkGame, on: Boolean, name: String, desc: String, onClick: () -> Unit) = Column(
    Modifier.width(128.dp).clip(RoundedCornerShape(18.dp)).background(if (on) Raised else Panel, RoundedCornerShape(18.dp))
        .border(1.5.dp, if (on) Fg else Line, RoundedCornerShape(18.dp))
        .selectable(on, role = Role.RadioButton, onClick = onClick).padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
    Arrangement.spacedBy(8.dp),
) {
    val kind = game.kinds.first()
    Box(
        Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(12.dp))
            .background(if (game == DrinkGame.Mix) Brush.horizontalGradient(0f to DareBlack, .5f to DareBlack, .5f to Wine, 1f to Wine) else Brush.linearGradient(listOf(kind.palette.color, kind.palette.color)))
            .then(if (game == DrinkGame.Dare) Modifier.border(1.dp, Line, RoundedCornerShape(12.dp)) else Modifier),
        Alignment.Center,
    ) {
        val ink = kind.palette.ink
        when (game) {
            DrinkGame.Mix -> Box(Modifier.fillMaxSize()) {
                Box(Modifier.offset(32.dp, 14.dp).size(30.dp, 44.dp).rotate(-10f).background(DareBlack, RoundedCornerShape(5.dp)).border(1.5.dp, Color.White.copy(.55f), RoundedCornerShape(5.dp)))
                Box(Modifier.offset(48.dp, 14.dp).size(30.dp, 44.dp).rotate(10f).background(Wine, RoundedCornerShape(5.dp)).border(1.5.dp, Color.White.copy(.55f), RoundedCornerShape(5.dp)))
            }
            DrinkGame.Dare -> BottleArt(Modifier.height(60.dp), ink, 3f, 3f)
            DrinkGame.Never -> GlassArt(Modifier.width(46.dp), ink, filled = false, width = 2.6f, thin = 2.6f)
            DrinkGame.DoIt -> ScaleArt(Modifier.size(52.dp), ink, 1.5f)
            DrinkGame.Paranoia -> SpiralArt(Modifier.size(54.dp), ink, 1.6f)
            DrinkGame.Likely -> SkullQuestionArt(Modifier.size(54.dp), ink)
        }
    }
    Txt(name, Modifier.padding(horizontal = 4.dp), 13.sp, 700, Display)
    Txt(desc, Modifier.padding(horizontal = 4.dp), 12.sp, color = Muted, lh = 1.3.em)
}

@Composable
private fun Switch(title: String, desc: String, on: Boolean, onChange: (Boolean) -> Unit, divider: Boolean = false) {
    val track by animateColorAsState(if (on) Blood else Line, tween(150), label = "track")
    val knob by animateDpAsState(if (on) 21.dp else 3.dp, tween(150), label = "knob")
    Row(
        Modifier.fillMaxWidth().heightIn(min = 60.dp).clickable(role = Role.Switch) { onChange(!on) }
            .drawBehind { if (divider) drawLine(Line, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), 1.dp.toPx()) }
            .semantics { stateDescription = if (on) "on" else "off" }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        Arrangement.spacedBy(12.dp), Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
            Txt(title, size = 15.sp, weight = 700)
            Txt(desc, size = 13.sp, color = Muted)
        }
        Box(Modifier.size(44.dp, 26.dp).clip(CircleShape).background(track)) {
            Box(Modifier.offset(knob, 3.dp).size(20.dp).background(Fg, CircleShape))
        }
    }
}

// ---------------------------------------------------------------- game

@Composable
private fun DrinkHeader(vm: SpillViewModel, counter: String, onRules: () -> Unit) {
    val d = vm.drink
    Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        GhostText(d.t("menu")) { vm.go(Screen.DrinkSetup) }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Eyebrow(d.t(vm.drinkCfg.game.key) + if (vm.drinkCfg.climb) " · " + d.t("climbTag") else "")
            Txt(counter, size = 13.sp, weight = 600, font = Display)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            RulesButton(null, onRules, d.t("howTo"))
            PlusBadge(38.dp, 2.dp, 11f)
        }
    }
}

private fun counter(vm: SpillViewModel) =
    if (vm.drinkDeck.getOrNull(vm.drinkIndex) != null) "%02d / %02d".format(vm.drinkIndex + 1, vm.drinkDeck.size) else vm.drink.t("done")

@Composable
fun DrinkRound(vm: SpillViewModel) {
    BackHandler { vm.go(Screen.DrinkSetup) }
    var rules by remember { mutableStateOf(false) }
    if (rules) DrinkRules(vm) { rules = false }
    if (vm.drinkCfg.game == DrinkGame.Paranoia) { ParanoiaGame(vm) { rules = true }; return }
    val d = vm.drink
    val flinger = remember { Flinger() }
    Column(Modifier.fillMaxSize()) {
        DrinkHeader(vm, counter(vm)) { rules = true }
        DeckStage(
            Modifier.weight(1f).padding(top = 18.dp), vm.drinkDeck.size, vm.drinkIndex, flinger, vm::nextDrink,
            cardWidth = { w, h -> minOf(minOf(w - 28.dp, (h - 44.dp) * .7f), 470.dp) },
            end = { EndCard(d.t("endTitle"), d.t("endText"), d.t("again"), d.t("menuShort"), vm::startDrink) { vm.go(Screen.DrinkSetup) } },
        ) { i, w, e, mod -> DrinkCardView(vm.drinkDeck[i], d, vm.drinkCfg.light, w, e, mod) }
        Controls(d.t("hint"), d.t("next")) { flinger.fling() }
    }
}

/** Dashed rule above a block, like the card design's "Para todos" divider. */
private fun Modifier.dashedTop(color: Color, stroke: Dp, on: Dp) = drawBehind {
    drawLine(color, Offset(0f, 0f), Offset(size.width, 0f), stroke.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(on.toPx(), on.toPx() * .7f)))
}

/**
 * The frame every Drink card shares (tag, level skulls, centered content, dashed footer block, game name).
 * Sizes are drawn at 330 wide and scaled by [s].
 */
@Composable
private fun DrinkFace(
    p: Palette, s: Float, d: DrinkCatalog, kind: DrinkKind, level: Int,
    watermark: @Composable BoxScope.() -> Unit, center: @Composable ColumnScope.() -> Unit, bottom: @Composable ColumnScope.() -> Unit,
) {
    val k = kind.key.replaceFirstChar { it.uppercase() }
    Box(
        Modifier.fillMaxSize().padding((11 * s).dp).clip(RoundedCornerShape((16 * s).dp))
            .border(2.dp, p.line.copy(.5f), RoundedCornerShape((16 * s).dp)),
    ) {
        watermark()
        Column(Modifier.fillMaxSize().padding(start = (18 * s).dp, end = (18 * s).dp, top = (16 * s).dp, bottom = (14 * s).dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Txt(
                    d.t("tag$k").uppercase(), Modifier.border(1.5.dp, p.line.copy(.45f), CircleShape).padding(horizontal = (11 * s).dp, vertical = (5 * s).dp),
                    10.sp, 500, Display, p.ink, ls = .14.em,
                )
                Row(Modifier.semantics { contentDescription = d.t("level").replace("{n}", "$level") }, Arrangement.spacedBy(3.dp)) {
                    (1..3).forEach { Skull(Modifier.size((20 * s).dp).alpha(if (it <= level) 1f else .22f), p.line) }
                }
            }
            Column(Modifier.weight(1f).fillMaxWidth(), Arrangement.spacedBy((12 * s).dp, Alignment.CenterVertically), Alignment.CenterHorizontally, content = center)
            Column(
                Modifier.fillMaxWidth().dashedTop(p.line.copy(if (kind == DrinkKind.Likely) .4f else .35f), 1.5.dp, 6.dp).padding(top = (14 * s).dp),
                Arrangement.spacedBy(6.dp), Alignment.CenterHorizontally, content = bottom,
            )
            Txt(d.t("foot$k").uppercase(), Modifier.padding(top = 10.dp).alpha(.85f), 9.sp, 400, Display, p.ink, ls = .2.em)
        }
    }
}

private fun String.fill(amount: String) = replace("{amount}", amount)

@Composable
private fun DrinkCardView(card: DrinkCard, d: DrinkCatalog, light: Boolean, w: Dp, e: Float, modifier: Modifier) {
    val s = w.value / 330f // the design is drawn at 330 wide
    val p = card.kind.palette
    val shots = d.amount(card.level, light)
    val level = d.t("level").replace("{n}", "${card.level}")
    val tag = d.t("tag" + card.kind.key.replaceFirstChar { it.uppercase() })

    CardShell(w, e, p.color, p.ring, "$tag. $level. ${card.main}", modifier) {
        DrinkFace(
            p, s, d, card.kind, card.level,
            watermark = {
                when (card.kind) {
                    DrinkKind.Dare -> BottleArt(Modifier.align(Alignment.Center).height((382 * s).dp).alpha(.13f), p.line, 1.4f, 1.4f)
                    DrinkKind.Never -> GlassArt(Modifier.align(Alignment.Center).width((240 * s).dp).alpha(.13f), p.line, filled = false, width = 1.4f, thin = 1.4f)
                    DrinkKind.DoIt -> ScaleArt(Modifier.align(Alignment.Center).size((250 * s).dp).alpha(.14f), p.line, .55f)
                    DrinkKind.Likely -> SkullQuestionArt(Modifier.align(Alignment.Center).size((260 * s).dp).alpha(.1f), p.line)
                    DrinkKind.Paranoia -> Unit // has its own screen
                }
            },
            center = {
                when (card.kind) {
                    DrinkKind.Dare -> {
                        Txt(d.t("answerOr").fill(shots).uppercase(), Modifier.alpha(.8f), 10.sp, 500, Display, p.ink, ls = .16.em, align = TextAlign.Center)
                        Txt(card.main, size = (26 * s).sp, weight = 800, color = p.ink, ls = (-.015).em, lh = 1.08.em, align = TextAlign.Center)
                    }
                    DrinkKind.Never -> {
                        Txt(d.t("neverLead"), size = (16 * s).sp, weight = 700, font = Display, color = p.ink, ls = .02.em)
                        Txt(card.main, size = (32 * s).sp, weight = 800, color = p.ink, ls = (-.02).em, lh = 1.05.em, align = TextAlign.Center)
                    }
                    DrinkKind.DoIt -> {
                        Txt(d.t("doitLead").fill(shots).uppercase(), Modifier.alpha(.85f), 10.sp, 500, Display, p.ink, ls = .16.em, align = TextAlign.Center)
                        Txt(card.main, size = (27 * s).sp, weight = 800, color = p.ink, ls = (-.015).em, lh = 1.08.em, align = TextAlign.Center)
                    }
                    DrinkKind.Likely -> {
                        Txt(d.t("likelyLead"), size = (14 * s).sp, weight = 700, font = Display, color = p.ink, align = TextAlign.Center)
                        Txt(card.main, size = (34 * s).sp, weight = 800, color = p.ink, ls = (-.02).em, lh = 1.04.em, align = TextAlign.Center)
                    }
                    DrinkKind.Paranoia -> Unit
                }
            },
            bottom = {
                when (card.kind) {
                    DrinkKind.Dare -> {
                        Txt(d.t("forAll").uppercase(), size = 10.sp, weight = 600, font = Display, color = p.ink, ls = .16.em)
                        Txt(card.all!!, size = (16 * s).sp, weight = 500, color = p.ink, lh = 1.3.em, align = TextAlign.Center)
                        Txt(d.t("skipped").replace("{unit}", d.unit(light)), Modifier.alpha(.72f), 12.sp, color = p.ink, align = TextAlign.Center)
                    }
                    DrinkKind.Never -> {
                        Txt(d.t("neverDrinks").fill(shots).uppercase(), size = 10.sp, weight = 600, font = Display, color = p.ink, ls = .16.em, align = TextAlign.Center)
                        Txt(d.t("neverSolo"), Modifier.alpha(.78f), 12.sp, color = p.ink, align = TextAlign.Center)
                    }
                    DrinkKind.DoIt -> {
                        Txt(d.t("doitRefuse").fill(shots).uppercase(), size = 10.sp, weight = 600, font = Display, color = p.ink, ls = .16.em, align = TextAlign.Center)
                        Txt(d.t("doitJudge"), Modifier.alpha(.78f), 12.sp, color = p.ink, align = TextAlign.Center)
                    }
                    DrinkKind.Likely -> {
                        Txt(d.t("likelyPoint").uppercase(), size = 10.sp, weight = 600, font = Display, color = p.ink, ls = .16.em, align = TextAlign.Center)
                        Txt(d.t("likelyDrinks").fill(shots), Modifier.alpha(.85f), 12.sp, color = p.ink, align = TextAlign.Center)
                    }
                    DrinkKind.Paranoia -> Unit
                }
            },
        )
    }
}

// ---------------------------------------------------------------- paranoia

/**
 * Paranoia: the question stays hidden until someone presses and holds the card, so only the phone's
 * holder reads it. Then the named person either drinks (and the question is revealed) or nobody does.
 * The card leaves with the same fling as the other games, triggered by the buttons.
 */
@Composable
private fun ParanoiaGame(vm: SpillViewModel, onRules: () -> Unit) {
    val d = vm.drink
    val index = vm.drinkIndex
    val card = vm.drinkDeck.getOrNull(index)
    val flinger = remember { Flinger() }
    var revealed by remember(index) { mutableStateOf(false) }
    var holding by remember(index) { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        DrinkHeader(vm, counter(vm), onRules)
        DeckStage(
            Modifier.weight(1f).padding(top = 18.dp), vm.drinkDeck.size, index, flinger, vm::nextDrink,
            cardWidth = { w, h -> minOf(minOf(w - 28.dp, (h - 44.dp) * .7f), 470.dp) },
            end = { EndCard(d.t("endTitle"), d.t("endText"), d.t("again"), d.t("menuShort"), vm::startDrink) { vm.go(Screen.DrinkSetup) } },
            draggable = false,
        ) { i, w, e, mod ->
            val active = i == index
            ParanoiaCard(
                vm.drinkDeck[i], vm, w, e, mod, show = active && (revealed || holding), revealed = active && revealed,
                gestures = active && !revealed, onHold = { holding = it }, onToggle = { if (!revealed) holding = !holding },
            )
        }
        // fixed height so the card does not jump when the buttons change
        Column(Modifier.fillMaxWidth().heightIn(min = 96.dp).padding(top = 6.dp), Arrangement.spacedBy(8.dp)) {
            if (card != null && revealed) Deal(d.t("next"), Modifier.fillMaxWidth().heightIn(min = 52.dp)) { flinger.fling() }
            else if (card != null && !holding) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Txt(
                        d.t("nobody"), Modifier.weight(1f).heightIn(min = 52.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Line, RoundedCornerShape(16.dp))
                            .clickable(role = Role.Button) { flinger.fling() }.padding(horizontal = 12.dp, vertical = 15.dp),
                        14.sp, 600, align = TextAlign.Center,
                    )
                    Txt(
                        d.t("drank").replace("{n}", "${card.level}"), Modifier.weight(1f).heightIn(min = 52.dp).clip(RoundedCornerShape(16.dp)).background(Fg)
                            .clickable(role = Role.Button) { revealed = true; holding = false }.padding(horizontal = 12.dp, vertical = 17.dp),
                        13.sp, 700, Display, Bg, align = TextAlign.Center,
                    )
                }
                Txt(d.t("pass"), Modifier.fillMaxWidth(), 13.sp, color = Muted, align = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ParanoiaCard(
    card: DrinkCard, vm: SpillViewModel, w: Dp, e: Float, modifier: Modifier,
    show: Boolean, revealed: Boolean, gestures: Boolean, onHold: (Boolean) -> Unit, onToggle: () -> Unit,
) {
    val d = vm.drink
    val p = DrinkKind.Paranoia.palette
    val s = w.value / 330f
    val hint = d.t("holdHint")
    val press by animateFloatAsState(if (show && !revealed) .98f else 1f, tween(150), label = "press")
    CardShell(
        w, e, p.color, p.ring, if (show) card.main else hint,
        modifier.graphicsLayer { scaleX = press; scaleY = press }
            .then(if (!gestures) Modifier else Modifier
                .pointerInput(card) { detectTapGestures(onPress = { onHold(true); tryAwaitRelease(); onHold(false) }) }
                .semantics { role = Role.Button; onClick(hint) { onToggle(); true } }),
    ) {
        DrinkFace(
            p, s, d, DrinkKind.Paranoia, card.level,
            watermark = { if (show) SpiralArt(Modifier.align(Alignment.Center).size((320 * s).dp).alpha(.14f), p.line, .5f) },
            center = {
                if (show) {
                    Txt((if (revealed) d.t("revealedLabel") else d.t("secretLabel")).uppercase(), Modifier.alpha(.85f), 10.sp, 500, Display, p.ink, ls = .16.em, align = TextAlign.Center)
                    Txt(card.main, size = (34 * s).sp, weight = 800, color = p.ink, ls = (-.02).em, lh = 1.04.em, align = TextAlign.Center)
                } else {
                    SpiralArt(Modifier.size((140 * s).dp), p.line, 1.2f)
                    Txt(d.t("holdTitle"), size = 17.sp, weight = 700, font = Display, color = p.ink)
                    Txt(d.t("holdBody"), Modifier.widthIn(max = 180.dp).alpha(.85f), 14.sp, color = p.ink, lh = 1.35.em, align = TextAlign.Center)
                }
            },
            bottom = {
                Txt(d.t("nameOnly").uppercase(), size = 10.sp, weight = 600, font = Display, color = p.ink, ls = .16.em, align = TextAlign.Center)
                Txt(d.t("namedDrinks").fill(d.amount(card.level, vm.drinkCfg.light)), Modifier.alpha(.78f), 12.sp, color = p.ink, align = TextAlign.Center)
            },
        )
    }
}

// ---------------------------------------------------------------- rules

private fun String.cap() = replaceFirstChar { it.uppercase() }

/** "Como jogar" for the game being played (or about to be): lead, numbered steps, drinking scale. */
@Composable
private fun DrinkRules(vm: SpillViewModel, onClose: () -> Unit) {
    val d = vm.drink
    val game = vm.drinkCfg.game
    val unit = d.unit(vm.drinkCfg.light)
    val maxH = (LocalConfiguration.current.screenHeightDp * .86f).coerceAtMost(760f).dp
    Dialog(onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.padding(16.dp).widthIn(max = 560.dp).fillMaxWidth().heightIn(max = maxH)
                .background(Surface, RoundedCornerShape(22.dp))
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp),
            Arrangement.spacedBy(20.dp),
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f), Arrangement.spacedBy(4.dp)) {
                    Eyebrow(d.t("howTo"))
                    Txt(d.t(game.key), size = 22.sp, weight = 700, font = Display)
                }
                GhostText(d.t("close"), onClose)
            }
            Txt(d.t("how" + game.key.cap()), size = 17.sp, lh = 1.5.em)
            game.kinds.forEach { kind ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (game.kinds.size > 1) Eyebrow(d.t(kind.key))
                    d.list("how" + kind.key.cap() + "Steps").forEachIndexed { i, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(30.dp).background(Raised, CircleShape), Alignment.Center) { Txt("${i + 1}", size = 12.sp, weight = 700, font = Display) }
                            Txt(step.replace("{unit}", unit), lh = 1.45.em)
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().background(Raised, RoundedCornerShape(16.dp)).padding(16.dp), Arrangement.spacedBy(6.dp)) {
                Txt(d.t("rulesTitle"), size = 13.sp, weight = 700, font = Display, ls = .02.em)
                Txt(d.t("howLevels").replace("{unit}", unit), lh = 1.5.em)
            }
            Txt(d.t("howSafe"), size = 14.sp, color = Muted, lh = 1.5.em)
        }
    }
}
