package app.spill

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun Rules(vm: SpillViewModel, onClose: () -> Unit) {
    val c = vm.catalog
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
                Txt(vm.t("howTo"), size = 22.sp, weight = 700, font = Display)
                GhostText(vm.t("close"), onClose)
            }
            Txt(vm.t("lead"), size = 17.sp, lh = 1.5.em)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                c.steps(vm.lang).forEachIndexed { i, (b, t) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(30.dp).background(Raised, CircleShape), Alignment.Center) { Txt("${i + 1}", size = 12.sp, weight = 700, font = Display) }
                        Txt(buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(b) }; append(" $t") }, lh = 1.45.em)
                    }
                }
            }
            Column(Modifier.fillMaxWidth().background(Raised, RoundedCornerShape(16.dp)).padding(16.dp), Arrangement.spacedBy(6.dp)) {
                Txt(vm.t("points"), size = 13.sp, weight = 700, font = Display, ls = .02.em)
                Txt(vm.t("pointsText"), lh = 1.5.em)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Eyebrow(vm.t("colors"))
                c.decks.forEach { d ->
                    val m = d.meta.getValue(vm.lang)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Mini(Color(d.color), Color(d.ink), 30.dp, 42.dp, radius = 6.dp, inset = 3.dp, frameRadius = 4.dp)
                        Txt(buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Fg)) { append("${m.name} · ${m.label}") }
                            append("\n${m.blurb}")
                        }, size = 14.sp, color = Muted, lh = 1.4.em)
                    }
                }
            }
            Txt(vm.t("note"), size = 14.sp, color = Muted, lh = 1.5.em)
        }
    }
}
