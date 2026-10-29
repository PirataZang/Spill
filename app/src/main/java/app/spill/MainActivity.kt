package app.spill

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.graphics.Color as AndroidColor
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

class SpillViewModel(app: Application) : AndroidViewModel(app) {
    val catalog = Catalog.load { app.assets.open("catalog/$it").bufferedReader().use { r -> r.readText() } }
    private val prefs = app.getSharedPreferences("spill", 0)

    var lang by mutableStateOf(
        (prefs.getString("lang", null) ?: Locale.getDefault().language).takeIf { c -> catalog.languages.any { it.code == c } }
            ?: catalog.languages.first().code,
    )
        private set

    val drink = DrinkCatalog.load { app.assets.open("catalog/$it").bufferedReader().use { r -> r.readText() } }

    var screen by mutableStateOf(Screen.Home); private set
    fun go(to: Screen) { screen = to }

    // ponytail: rounds live in memory; they are lost if Android kills the process, persist them if that hurts.
    var deck: List<Catalog.Card> = emptyList(); private set
    var index by mutableIntStateOf(0); private set
    var round: Catalog.Deck? = null; private set

    var drinkCfg by mutableStateOf(DrinkConfig())
    var drinkDeck: List<DrinkCard> = emptyList(); private set
    var drinkIndex by mutableIntStateOf(0); private set
    val drinkCount get() = drink.deal(drinkCfg).size

    fun pickLang(code: String) { lang = code; prefs.edit().putString("lang", code).apply() }

    /** [round] null = every deck shuffled together. */
    fun deal(round: Catalog.Deck?) {
        this.round = round
        deck = (round?.cards ?: catalog.cards).shuffled()
        index = 0
        screen = Screen.Game
    }

    fun next() { index++ }
    fun menu() { screen = Screen.Home }

    fun startDrink() { drinkDeck = drink.deal(drinkCfg); drinkIndex = 0; screen = Screen.DrinkGame }
    fun nextDrink() { drinkIndex++ }
    fun t(key: String) = catalog.t(lang, key)
}

enum class Screen { Home, Game, DrinkGate, DrinkSetup, DrinkGame }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(SystemBarStyle.dark(AndroidColor.TRANSPARENT), SystemBarStyle.dark(AndroidColor.TRANSPARENT))
        setContent { Root() }
    }
}

@Composable
private fun Root(vm: SpillViewModel = viewModel()) {
    val screen = vm.screen
    val drinkMode = screen.name.startsWith("Drink")
    val drinkTop = vm.drinkDeck.getOrNull(vm.drinkIndex)
    // Glow: color (null = cycle the deck colors), opacity, vertical center and height of the ellipse.
    val glow = when (screen) {
        Screen.Home -> Glow(null, .28f, .42f, .55f)
        Screen.Game -> Glow(vm.deck.getOrNull(vm.index)?.deck?.glow?.let { Color(it) } ?: Neutral, .28f, .42f, .55f)
        Screen.DrinkGate -> Glow(Wine, .35f, .38f, .50f)
        Screen.DrinkSetup -> Glow(vm.drinkCfg.game.glow, .30f, .18f, .45f)
        Screen.DrinkGame -> Glow(drinkTop?.kind?.palette?.glow ?: Neutral, .34f, .42f, .55f)
    }
    Box(Modifier.fillMaxSize().background(if (drinkMode) DrinkBg else Bg)) {
        Glow(glow)
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 18.dp)) {
            Crossfade(screen, animationSpec = tween(0), label = "screen") {
                when (it) {
                    Screen.Home -> Home(vm)
                    Screen.Game -> Game(vm)
                    Screen.DrinkGate -> DrinkGate(vm)
                    Screen.DrinkSetup -> DrinkSetup(vm)
                    Screen.DrinkGame -> DrinkRound(vm)
                }
            }
        }
    }
}

private class Glow(val color: Color?, val alpha: Float, val cy: Float, val ry: Float)

/** Ambient light: cycles the five deck colors on the home screen, otherwise follows the screen / top card. */
@Composable
private fun Glow(g: Glow) {
    val cycle = rememberInfiniteTransition(label = "glow")
    val colors = listOf(Color(0xFF168048), Color(0xFF2A5BE8), Color(0xFFF7C531), Color(0xFFD7263D), Neutral)
    val home by cycle.animateColor(colors[0], colors[0], infiniteRepeatable(keyframes {
        durationMillis = 25_000
        colors.forEachIndexed { i, c -> c at i * 5_000 using LinearEasing }
        colors[0] at 25_000
    }, RepeatMode.Restart), label = "home")
    val color by animateColorAsState(g.color ?: home, tween(if (g.color == null) 0 else 700), label = "color")
    Canvas(Modifier.fillMaxSize()) {
        // CSS: radial-gradient(70% ry at 50% cy, color, transparent 72%)
        val rx = size.width * .7f * .72f
        val ry = size.height * g.ry * .72f
        val c = Offset(size.width / 2, size.height * g.cy)
        withTransform({ scale(1f, ry / rx, c) }) {
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = g.alpha), Color.Transparent), c, rx), rx, c)
        }
    }
}
