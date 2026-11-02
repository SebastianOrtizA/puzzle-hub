package app.puzzles.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

/** Go/no-go criterion 1: cold start median <= 900 ms on the A52-class phone (Partial = with baseline profile). */
class StartupBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test fun coldNoCompilation() = run(CompilationMode.None())
    @Test fun coldBaselineProfile() = run(CompilationMode.Partial())

    private fun run(mode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        startupMode = StartupMode.COLD,
        iterations = 10,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
