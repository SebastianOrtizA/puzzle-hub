package app.puzzles.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test fun generate() = rule.collect(TARGET_PACKAGE) {
        pressHome()
        startActivityAndWait()
        device.minesGestures()
    }
}
