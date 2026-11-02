package app.puzzles.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

/** Go/no-go criterion 2: pinch/pan on the 30x16 board, <= 1% janky frames (read frameOverrunMs / janky %). */
class MinesGestureBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test fun pinchAndPan() = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 8,
        setupBlock = { pressHome(); startActivityAndWait() },
    ) { device.minesGestures() }
}
