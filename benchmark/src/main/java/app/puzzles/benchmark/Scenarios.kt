package app.puzzles.benchmark

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice

const val TARGET_PACKAGE = "app.puzzles"

/** Pinch in/out and pan across the mock Mines board (the screen shown at launch). */
fun UiDevice.minesGestures() {
    val board = wait(androidx.test.uiautomator.Until.findObject(By.descStartsWith("Mines board")), 5_000)
        ?: error("Mines board not found")
    repeat(3) {
        board.pinchOpen(0.6f, 800)
        board.drag(android.graphics.Point(board.visibleCenter.x - 200, board.visibleCenter.y), 400)
        board.pinchClose(0.6f, 800)
    }
}
