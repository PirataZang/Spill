package app.spill

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp


@Composable
fun Home(vm: SpillViewModel) {
    val c = vm.catalog
    var rules by rememberSaveable { mutableStateOf(false) }
    var themed by rememberSaveable { mutableStateOf(false) }
    var pick by rememberSaveable { mutableStateOf(0) }
    val deck = c.decks[pick.coerceIn(c.decks.indices)]

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val maxW = maxWidth; val maxH = maxHeight
        val wide = maxW >= 900.dp
        val gap = (maxH * .06f).coerceIn(28.dp, 56.dp)
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Eyebrow(vm.t("count").replace("{d}", "${c.decks.size}").replace("{n}", "${c.cards.size}"), Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LangToggle(c.languages, vm.lang, vm.t("language"), vm::pickLang)
                    RulesButton(vm.t("rules"), { rules = true }, vm.t("rules"))
                }
            }
            val hero = @Composable { Hero(vm, maxW) }
            val setup = @Composable {
                Setup(vm, themed, { themed = it }, deck, { pick = c.decks.indexOf(it) }, { vm.go(Screen.DrinkGate) }) { vm.deal(if (themed) deck else null) }
            }
            Box(Modifier.fillMaxWidth().heightIn(min = maxH - 44.dp).padding(vertical = 16.dp), Alignment.Center) {
                if (wide) Row(Modifier.widthIn(max = 1040.dp), Arrangement.spacedBy(gap), Alignment.CenterVertically) {
                    Box(Modifier.weight(1f), Alignment.Center) { hero() }
                    Box(Modifier.weight(1f), Alignment.Center) { setup() }
                } else Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(gap)) { hero(); setup() }
            }
        }
    }
    if (rules) Rules(vm) { rules = false }
}

@Composable
private fun Hero(vm: SpillViewModel, width: Dp) = Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Logo(Modifier.width(250.dp))
    Txt("spill", size = (width.value * .26f).coerceIn(80f, 160f).sp, weight = 900, font = Display, ls = (-.04).em, lh = (((width.value * .26f).coerceIn(80f, 160f)) * .85f).sp)
    Column(Modifier.widthIn(max = 290.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val size = (width.value * .044f).coerceIn(17f, 20f).sp
        Txt(vm.t("tagline"), size = size, weight = 600, align = TextAlign.Center)
        Txt(vm.t("tagline2"), Modifier.padding(top = 6.dp), size * .82f, 400, color = Muted, align = TextAlign.Center)
    }
}

@Composable
private fun Setup(vm: SpillViewModel, themed: Boolean, onMode: (Boolean) -> Unit, deck: Catalog.Deck, onPick: (Catalog.Deck) -> Unit, onDrink: () -> Unit, onDeal: () -> Unit) {
    val c = vm.catalog
    Column(Modifier.widthIn(max = 440.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow(vm.t("mode"))
            Row(Modifier.selectableGroup(), Arrangement.spacedBy(10.dp)) {
                Mode(Modifier.weight(1f), !themed, { onMode(false) }, c.decks.map { Color(it.color) }, vm.t("random"), vm.t("randomDesc"))
                Mode(Modifier.weight(1f), themed, { onMode(true) }, listOf(Color(deck.color)), vm.t("themed"), vm.t("themedDesc"))
            }
        }
        if (themed) Column {
            Eyebrow(vm.t("color"), Modifier.padding(bottom = 10.dp))
            Row(Modifier.fillMaxWidth().selectableGroup(), Arrangement.spacedBy(6.dp)) {
                c.decks.forEach { d -> Swatch(Modifier.weight(1f), d, d == deck, vm.lang) { onPick(d) } }
            }
            val m = deck.meta.getValue(vm.lang)
            Column(Modifier.padding(top = 12.dp).heightIn(min = 62.dp)) {
                Txt(m.label, size = 14.sp, weight = 700, lh = 1.45.em)
                Txt(m.blurb, size = 14.sp, color = Muted, lh = 1.45.em)
            }
        }
        Deal(vm.t("deal"), Modifier.fillMaxWidth(), onDeal)
        DrinkEntry(vm.t("drinkTitle"), vm.t("drinkDesc"), onDrink)
    }
}

/** Entry to the +18 Drink mode, tinted wine so it reads as a different game. */
@Composable
private fun DrinkEntry(title: String, desc: String, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().heightIn(min = 60.dp).clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF1E0F14)).border(1.dp, Color(0xFF5A1A28), RoundedCornerShape(16.dp))
        .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
    Arrangement.spacedBy(12.dp), Alignment.CenterVertically,
) {
    Box(Modifier.size(34.dp, 40.dp)) {
        Box(Modifier.offset(0.dp, 2.dp).size(22.dp, 32.dp).graphicsLayer { rotationZ = -10f }.background(Color(0xFF0D0D10), RoundedCornerShape(4.dp)).border(1.dp, Color.White.copy(.35f), RoundedCornerShape(4.dp)))
        Box(Modifier.offset(11.dp, 4.dp).size(22.dp, 32.dp).graphicsLayer { rotationZ = 10f }.background(Wine, RoundedCornerShape(4.dp)).border(1.dp, Color.White.copy(.35f), RoundedCornerShape(4.dp)))
    }
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Txt(title, size = 13.sp, weight = 700, font = Display)
        Txt(desc, size = 13.sp, color = Color(0xFFB9B4C4))
    }
    Txt("+18", Modifier.border(1.5.dp, Blood, CircleShape).padding(horizontal = 8.dp, vertical = 4.dp), 10.sp, 700, Display)
}

@Composable
private fun Mode(modifier: Modifier, on: Boolean, onClick: () -> Unit, dots: List<Color>, name: String, desc: String) = Column(
    modifier.clip(RoundedCornerShape(16.dp))
        .background(if (on) Raised else Surface)
        .border(1.dp, if (on) Fg else Line, RoundedCornerShape(16.dp))
        .selectable(on, role = Role.RadioButton, onClick = onClick)
        .padding(14.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
) {
    Row(Modifier.padding(bottom = 4.dp), Arrangement.spacedBy((-4).dp)) {
        dots.forEach { Box(Modifier.size(14.dp).background(it, CircleShape).border(1.dp, Fg.copy(.22f), CircleShape)) }
    }
    Txt(name, size = 16.sp, weight = 700, font = Display)
    Txt(desc, size = 14.sp, color = Muted, lh = 1.35.em)
}

@Composable
private fun Swatch(modifier: Modifier, deck: Catalog.Deck, on: Boolean, lang: String, onClick: () -> Unit) {
    val lift by animateFloatAsState(if (on) 1f else 0f, tween(200), label = "swatch")
    Column(
        modifier.selectable(on, role = Role.RadioButton, onClick = onClick).padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Mini(
            Color(deck.color), Color(deck.ink), 44.dp, 62.dp,
            Modifier.graphicsLayer { translationY = -6.dp.toPx() * lift; rotationZ = -6f * lift }
                .border(2.dp, Fg.copy(lift), RoundedCornerShape(9.dp)),
        )
        Txt(deck.meta.getValue(lang).name, size = 13.sp, weight = 600, color = if (on) Fg else Muted)
    }
}
