import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Generates `Tokens.kt` from the DTCG file `design/tokens/tokens.json` (the source of truth). */
@Suppress("UNCHECKED_CAST")
@CacheableTask
abstract class GenerateTokensTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val tokensFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        @Suppress("UNCHECKED_CAST")
        val root = JsonSlurper().parse(tokensFile.get().asFile) as Map<String, Any?>
        val color = root.getValue("color") as Map<String, Any?>
        val light = flatten(color.getValue("light"))
        val dark = flatten(color.getValue("dark"))
        check(light.keys == dark.keys) { "light and dark colour tokens differ: ${light.keys xor dark.keys}" }

        val out = StringBuilder()
        out.appendLine("// GENERATED from design/tokens/tokens.json by :app:generateTokens. Do not edit.")
        out.appendLine("package app.puzzles.design")
        out.appendLine()
        out.appendLine("import androidx.compose.ui.graphics.Color")
        out.appendLine("import androidx.compose.ui.unit.dp")
        out.appendLine()
        out.appendLine("data class PuzzleColors(")
        light.keys.forEach { out.appendLine("    val ${name(it)}: Color,") }
        out.appendLine(")")
        out.appendLine()
        for ((label, map) in listOf("LightPuzzleColors" to light, "DarkPuzzleColors" to dark)) {
            out.appendLine("val $label = PuzzleColors(")
            map.forEach { (k, v) -> out.appendLine("    ${name(k)} = Color(0xFF${hex(v)}),") }
            out.appendLine(")")
            out.appendLine()
        }
        emitDimensions(out, root.getValue("dimension") as Map<String, Any?>)
        emitDurations(out, root.getValue("duration") as Map<String, Any?>)

        val dir = outputDir.get().asFile.resolve("app/puzzles/design").apply { mkdirs() }
        dir.resolve("Tokens.kt").writeText(out.toString())
    }

    private infix fun Set<String>.xor(o: Set<String>) = (this - o) + (o - this)

    private fun hex(v: String): String {
        require(Regex("#[0-9A-Fa-f]{6}").matches(v)) { "Unsupported colour value: $v" }
        return v.substring(1).uppercase()
    }

    /** "board.lineMinor" -> "boardLineMinor". */
    private fun name(path: String) =
        path.split('.').mapIndexed { i, s -> if (i == 0) s else s.replaceFirstChar(Char::uppercase) }.joinToString("")

    @Suppress("UNCHECKED_CAST")
    private fun flatten(node: Any?, prefix: String = "", into: MutableMap<String, String> = linkedMapOf()): Map<String, String> {
        val m = node as Map<String, Any?>
        if (m.containsKey("\$value")) {
            into[prefix] = m["\$value"] as String
        } else {
            m.forEach { (k, v) -> flatten(v, if (prefix.isEmpty()) k else "$prefix.$k", into) }
        }
        return into
    }

    private fun emitDimensions(out: StringBuilder, dim: Map<String, Any?>) {
        for ((group, _) in dim) {
            val items = flatten(dim.getValue(group))
            out.appendLine("object ${group.replaceFirstChar(Char::uppercase)}Tokens {")
            items.forEach { (k, v) ->
                require(v.endsWith("dp")) { "Unsupported dimension: $v" }
                val id = if (k.first().isDigit()) "s$k" else name(k)
                out.appendLine("    val $id = ${v.removeSuffix("dp")}.dp")
            }
            out.appendLine("}")
            out.appendLine()
        }
    }

    private fun emitDurations(out: StringBuilder, dur: Map<String, Any?>) {
        out.appendLine("/** Milliseconds; multiply by the system animator scale at the call site. */")
        out.appendLine("object DurationTokens {")
        flatten(dur).forEach { (k, v) ->
            require(v.endsWith("ms")) { "Unsupported duration: $v" }
            out.appendLine("    const val ${name(k)}Ms = ${v.removeSuffix("ms")}")
        }
        out.appendLine("}")
    }
}
