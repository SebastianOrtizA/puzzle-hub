package app.puzzles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.puzzles.design.PuzzlesTheme
import app.puzzles.spike.SpikeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PuzzlesTheme {
                SpikeScreen() // week-1 technology test; replaced by the Hub in Foundation
            }
        }
    }
}
