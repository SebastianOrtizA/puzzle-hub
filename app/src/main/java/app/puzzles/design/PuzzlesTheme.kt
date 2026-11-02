package app.puzzles.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalPuzzleColors = staticCompositionLocalOf { LightPuzzleColors }

/** Material 3 theme fed only from generated tokens (no hard-coded colours). */
@Composable
fun PuzzlesTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val t = if (dark) DarkPuzzleColors else LightPuzzleColors
    val scheme = if (dark) {
        darkColorScheme(
            primary = t.primary, surface = t.surface, background = t.surface,
            surfaceContainer = t.surfaceContainer, onSurface = t.onSurface,
            onSurfaceVariant = t.onSurfaceVariant, outline = t.outline, error = t.stateError,
        )
    } else {
        lightColorScheme(
            primary = t.primary, surface = t.surface, background = t.surface,
            surfaceContainer = t.surfaceContainer, onSurface = t.onSurface,
            onSurfaceVariant = t.onSurfaceVariant, outline = t.outline, error = t.stateError,
        )
    }
    CompositionLocalProvider(LocalPuzzleColors provides t) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
