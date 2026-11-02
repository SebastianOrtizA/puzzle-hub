package app.puzzles.spike

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.puzzles.R
import app.puzzles.design.PuzzleColors

/**
 * Week-1 spike: Switches 5x5 drawn on one Canvas.
 *
 * The board is a pure function of [lit] -> drawing (screenshot-testable). Lit cells carry a
 * filled dot, so state is never colour-only.
 *
 * Deliberately NOT here: reduce / undo / save. Go/no-go criterion 6 is the founder writing those
 * in <= 2 days, so [lit] is a fixed mock and taps are not wired. Hook point: [onCellTap].
 */
const val SWITCHES_SIZE = 5

val MockSwitchesLit: BooleanArray = BooleanArray(SWITCHES_SIZE * SWITCHES_SIZE).also {
    // A plus shape plus one corner: obviously not a solved board.
    intArrayOf(2, 7, 10, 11, 12, 13, 14, 17, 22, 24).forEach { i -> it[i] = true }
}

@Composable
fun SwitchesBoard(
    lit: BooleanArray,
    colors: PuzzleColors,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") onCellTap: (Int) -> Unit = {}, // founder: wire to reduce()
) {
    val description = stringResource(R.string.spike_switches_board, lit.count { it })
    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = description },
    ) {
        val n = SWITCHES_SIZE
        val gap = 6.dp.toPx()
        val cell = (size.width - gap * (n + 1)) / n
        val radius = CornerRadius(4.dp.toPx())
        drawRoundRect(colors.boardBg, size = size, cornerRadius = CornerRadius(12.dp.toPx()))
        for (r in 0 until n) for (c in 0 until n) {
            val topLeft = Offset(gap + c * (cell + gap), gap + r * (cell + gap))
            val sz = Size(cell, cell)
            if (lit[r * n + c]) {
                drawRoundRect(colors.gameSwitches, topLeft, sz, radius)
                drawCircle(colors.boardBg, cell * 0.16f, topLeft + Offset(cell / 2, cell / 2))
            } else {
                drawRoundRect(colors.surfaceContainer, topLeft, sz, radius)
                drawRoundRect(colors.boardLineMinor, topLeft, sz, radius, style = Stroke(2.dp.toPx()))
            }
        }
    }
}
