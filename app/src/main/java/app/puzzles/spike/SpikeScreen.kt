package app.puzzles.spike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.puzzles.R
import app.puzzles.design.LocalPuzzleColors

/** Week-1 technology-test host. Throwaway: delete once the go/no-go decision is recorded. */
@Composable
fun SpikeScreen() {
    val colors = LocalPuzzleColors.current
    var tab by remember { mutableStateOf(1) }
    val context = LocalContext.current
    val mines = remember { MockMines(context) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(tab == 0, { tab = 0 }, { Text(stringResource(R.string.spike_tab_switches)) })
                FilterChip(tab == 1, { tab = 1 }, { Text(stringResource(R.string.spike_tab_mines)) })
            }
            if (tab == 0) {
                SwitchesBoard(MockSwitchesLit, colors)
            } else {
                MinesBoard(mines, colors, Modifier.fillMaxWidth().weight(1f))
                Text(
                    mines.announcement,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
}
