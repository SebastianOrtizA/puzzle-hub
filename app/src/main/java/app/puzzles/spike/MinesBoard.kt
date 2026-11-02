package app.puzzles.spike

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.puzzles.R
import app.puzzles.design.PuzzleColors

const val MINES_COLS = 30
const val MINES_ROWS = 16
private const val MINES_CELLS = MINES_COLS * MINES_ROWS
private val CellSize = 44.dp
private const val MAX_ZOOM = 4f

/**
 * Mock Mines state: 480 cells, deterministic pattern, no real game logic. Exists to stress
 * rendering, gestures and the TalkBack layer. [number] 0 = empty, 1..8 = adjacent mines.
 */
@Stable
class MockMines(private val context: Context) {
    private val number = IntArray(MINES_CELLS) { i -> (hash(i) / 3) % 9 }
    val revealed = mutableStateListOf<Boolean>().apply { repeat(MINES_CELLS) { add(hash(it) % 3 == 0) } }
    val flagged = mutableStateListOf<Boolean>().apply { repeat(MINES_CELLS) { add(!revealed[it] && hash(it) % 11 == 0) } }

    /** Spoken through a polite live region. */
    var announcement by mutableStateOf("")
        private set

    fun number(i: Int) = number[i]

    fun toggleFlag(i: Int) {
        if (revealed[i]) return say(R.string.spike_said_nothing, i)
        flagged[i] = !flagged[i]
        say(if (flagged[i]) R.string.spike_said_flagged else R.string.spike_said_unflagged, i)
    }

    fun reveal(i: Int) {
        if (revealed[i] || flagged[i]) return say(R.string.spike_said_nothing, i)
        revealed[i] = true
        say(R.string.spike_said_revealed, i)
    }

    /** Mock chord: open every hidden, unflagged neighbour of a revealed numbered cell. */
    fun chord(i: Int) {
        if (!revealed[i] || number[i] == 0) return say(R.string.spike_said_nothing, i)
        var opened = 0
        neighbours(i).forEach { n -> if (!revealed[n] && !flagged[n]) { revealed[n] = true; opened++ } }
        announcement = context.getString(R.string.spike_said_chord, row(i) + 1, col(i) + 1, opened)
    }

    fun tap(i: Int) = if (revealed[i]) chord(i) else reveal(i)

    private fun say(res: Int, i: Int) {
        // Alternate a trailing zero-width space so identical repeated text still re-announces.
        val text = context.getString(res, row(i) + 1, col(i) + 1)
        announcement = if (announcement == text) text + "​" else text
    }

    private fun neighbours(i: Int): List<Int> {
        val r = row(i); val c = col(i)
        return buildList {
            for (dr in -1..1) for (dc in -1..1) {
                val nr = r + dr; val nc = c + dc
                if ((dr != 0 || dc != 0) && nr in 0 until MINES_ROWS && nc in 0 until MINES_COLS) add(nr * MINES_COLS + nc)
            }
        }
    }

    private fun hash(i: Int): Int = ((i * 2654435761L) ushr 7).toInt() and 0x7fffffff
}

fun row(i: Int) = i / MINES_COLS
fun col(i: Int) = i % MINES_COLS

@Composable
fun MinesBoard(mines: MockMines, colors: PuzzleColors, modifier: Modifier = Modifier) {
    val cellPx = with(androidx.compose.ui.platform.LocalDensity.current) { CellSize.toPx() }
    val boardW = (MINES_COLS * cellPx).toInt()
    val boardH = (MINES_ROWS * cellPx).toInt()

    // Transform lives in state read ONLY inside graphicsLayer {} -> pinch/pan never recomposes.
    var scale by remember { mutableFloatStateOf(0f) }
    var offX by remember { mutableFloatStateOf(0f) }
    var offY by remember { mutableFloatStateOf(0f) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }

    fun clamp() {
        val w = boardW * scale; val h = boardH * scale
        offX = if (w <= viewport.width) (viewport.width - w) / 2 else offX.coerceIn(viewport.width - w, 0f)
        offY = if (h <= viewport.height) 0f else offY.coerceIn(viewport.height - h, 0f)
    }

    val fitScale = if (viewport.width == 0) 1f else viewport.width / boardW.toFloat()

    val digits = rememberDigitLayouts(cellPx, colors)
    val boardDescription = androidx.compose.ui.res.stringResource(R.string.spike_mines_board)

    Box(
        modifier
            .clipToBounds()
            .onSizeChanged {
                viewport = it
                if (scale == 0f) { scale = it.width / boardW.toFloat(); clamp() }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(fitScale, MAX_ZOOM * fitScale)
                    val k = newScale / scale
                    // Keep the point under the fingers fixed while zooming, then apply pan.
                    offX = centroid.x - (centroid.x - offX) * k + pan.x
                    offY = centroid.y - (centroid.y - offY) * k + pan.y
                    scale = newScale
                    clamp()
                }
            }
            .pointerInput(Unit) {
                fun hit(p: Offset): Int? {
                    val c = ((p.x - offX) / scale / cellPx).toInt()
                    val r = ((p.y - offY) / scale / cellPx).toInt()
                    return if (c in 0 until MINES_COLS && r in 0 until MINES_ROWS) r * MINES_COLS + c else null
                }
                detectTapGestures(
                    onTap = { hit(it)?.let(mines::tap) },
                    onLongPress = { hit(it)?.let(mines::toggleFlag) },
                )
            }
            .semantics { contentDescription = boardDescription; isTraversalGroup = true },
    ) {
        Box(
            Modifier
                .layout { m, c ->
                    val p = m.measure(Constraints.fixed(boardW, boardH))
                    layout(c.maxWidth, c.maxHeight) { p.place(0, 0) }
                }
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = scale; scaleY = scale
                    translationX = offX; translationY = offY
                },
        ) {
            Canvas(Modifier.fillMaxSize()) { drawMines(mines, colors, digits, cellPx) }
            MinesSemanticsLayer(mines, cellPx)
        }
    }
}

@Composable
private fun rememberDigitLayouts(cellPx: Float, colors: PuzzleColors): List<TextLayoutResult> {
    val measurer: TextMeasurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    return remember(cellPx, colors, density) {
        (1..8).map { d ->
            measurer.measure(
                d.toString(),
                TextStyle(
                    color = colors.cellUser,
                    fontWeight = FontWeight.Bold,
                    fontSize = with(density) { (cellPx * 0.5f).toSp() },
                ),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMines(
    mines: MockMines,
    colors: PuzzleColors,
    digits: List<TextLayoutResult>,
    cellPx: Float,
) {
    val inset = cellPx * 0.04f
    val sz = Size(cellPx - 2 * inset, cellPx - 2 * inset)
    val flagPath = Path()
    for (i in 0 until MINES_COLS * MINES_ROWS) {
        val tl = Offset(col(i) * cellPx + inset, row(i) * cellPx + inset)
        if (mines.revealed[i]) {
            drawRect(colors.boardBg, tl, sz)
            drawRect(colors.boardLineMinor, tl, sz, style = Stroke(1f))
            val n = mines.number(i)
            if (n > 0) {
                val t = digits[n - 1]
                drawText(t, topLeft = tl + Offset((sz.width - t.size.width) / 2f, (sz.height - t.size.height) / 2f))
            }
        } else {
            drawRect(colors.cellPeer, tl, sz)
            drawRect(colors.outline, tl, sz, style = Stroke(2f))
            if (mines.flagged[i]) {
                // Flag = pole + triangle: shape, not just colour.
                flagPath.reset()
                val x = tl.x + sz.width * 0.38f
                flagPath.moveTo(x, tl.y + sz.height * 0.2f)
                flagPath.lineTo(x + sz.width * 0.38f, tl.y + sz.height * 0.38f)
                flagPath.lineTo(x, tl.y + sz.height * 0.56f)
                flagPath.close()
                drawPath(flagPath, colors.stateFlag)
                drawRect(colors.onSurface, Offset(x - 2f, tl.y + sz.height * 0.2f), Size(4f, sz.height * 0.6f))
            }
        }
    }
}

/**
 * One semantics node per cell, laid out in row-major order over the canvas. They draw nothing; they
 * exist so TalkBack can focus each cell, read it, and offer Flag / Chord custom actions.
 */
@Composable
private fun MinesSemanticsLayer(mines: MockMines, cellPx: Float) {
    val cell = cellPx.toInt()
    Layout(
        content = { repeat(MINES_COLS * MINES_ROWS) { MineCellNode(mines, it) } },
        modifier = Modifier.fillMaxSize().semantics { isTraversalGroup = true },
    ) { measurables, _ ->
        val placeables = measurables.map { it.measure(Constraints.fixed(cell, cell)) }
        layout(cell * MINES_COLS, cell * MINES_ROWS) {
            placeables.forEachIndexed { i, p -> p.place(col(i) * cell, row(i) * cell) }
        }
    }
}

@Composable
private fun MineCellNode(mines: MockMines, i: Int) {
    val r = row(i) + 1; val c = col(i) + 1
    // Reads happen in this composable only, so a state change recomposes just this one node.
    val description = when {
        mines.flagged[i] -> androidx.compose.ui.res.stringResource(R.string.spike_cell_flagged, r, c)
        !mines.revealed[i] -> androidx.compose.ui.res.stringResource(R.string.spike_cell_hidden, r, c)
        mines.number(i) == 0 -> androidx.compose.ui.res.stringResource(R.string.spike_cell_empty, r, c)
        else -> androidx.compose.ui.res.stringResource(R.string.spike_cell_number, r, c, mines.number(i))
    }
    val flagLabel = androidx.compose.ui.res.stringResource(R.string.spike_action_flag)
    val chordLabel = androidx.compose.ui.res.stringResource(R.string.spike_action_chord)
    Box(
        Modifier.semantics {
            contentDescription = description
            role = Role.Button
            onClick { mines.tap(i); true }
            customActions = listOf(
                CustomAccessibilityAction(flagLabel) { mines.toggleFlag(i); true },
                CustomAccessibilityAction(chordLabel) { mines.chord(i); true },
            )
        },
    )
}
