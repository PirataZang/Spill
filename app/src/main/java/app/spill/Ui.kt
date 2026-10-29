package app.spill

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun Txt(
    text: String, modifier: Modifier = Modifier, size: TextUnit = 16.sp, weight: Int = 400, font: FontFamily = Body,
    color: Color = Fg, ls: TextUnit = TextUnit.Unspecified, lh: TextUnit = TextUnit.Unspecified, align: TextAlign? = null, maxLines: Int = Int.MAX_VALUE,
) = Txt(AnnotatedString(text), modifier, size, weight, font, color, ls, lh, align, maxLines)

@Composable
fun Txt(
    text: AnnotatedString, modifier: Modifier = Modifier, size: TextUnit = 16.sp, weight: Int = 400, font: FontFamily = Body,
    color: Color = Fg, ls: TextUnit = TextUnit.Unspecified, lh: TextUnit = TextUnit.Unspecified, align: TextAlign? = null, maxLines: Int = Int.MAX_VALUE,
) = Text(text, modifier, color, fontSize = size, fontFamily = font, fontWeight = FontWeight(weight), letterSpacing = ls, lineHeight = lh, textAlign = align, maxLines = maxLines)

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) =
    Txt(text.uppercase(), modifier, 11.sp, 500, Display, Muted, ls = .14.em)

/** Pill button with a hairline border. */
@Composable
fun Ghost(onClick: () -> Unit, modifier: Modifier = Modifier, description: String? = null, content: @Composable () -> Unit) {
    Row(
        modifier
            .clip(CircleShape)
            .border(BorderStroke(1.dp, Line), CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
fun GhostText(text: String, onClick: () -> Unit) = Ghost(onClick) { Txt(text, size = 14.sp, weight = 600, maxLines = 1) }

@Composable
fun RulesButton(label: String?, onClick: () -> Unit, description: String) {
    if (label != null) Ghost(onClick) { QMark(); Txt(label, size = 14.sp, weight = 600, maxLines = 1) }
    else Box(
        Modifier.size(38.dp).clip(CircleShape).border(1.dp, Line, CircleShape)
            .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = description },
        Alignment.Center,
    ) { QMark() }
}

@Composable
private fun QMark() = Box(Modifier.size(18.dp).border(1.5.dp, Fg, CircleShape), Alignment.Center) {
    Txt("?", size = 10.sp, weight = 700, font = Display)
}

@Composable
fun LangToggle(languages: List<Catalog.Language>, current: String, description: String, onPick: (String) -> Unit) {
    Row(
        Modifier.border(1.dp, Line, CircleShape).padding(3.dp).semantics { contentDescription = description },
    ) {
        languages.forEach { l ->
            val on = l.code == current
            Txt(
                l.label, Modifier.clip(CircleShape).background(if (on) Fg else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onPick(l.code) }.padding(horizontal = 9.dp, vertical = 5.dp),
                10.sp, 600, Display, if (on) Bg else Muted, ls = .08.em,
            )
        }
    }
}

/** Primary full-width button ("Deal the cards"). */
@Composable
fun Deal(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) = Txt(
    text, modifier.clip(RoundedCornerShape(16.dp)).background(Fg).clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 22.dp, vertical = 17.dp),
    15.sp, 700, Display, Bg, ls = .02.em, align = TextAlign.Center,
)
