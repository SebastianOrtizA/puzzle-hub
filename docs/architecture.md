# Architecture

## Technology decision: Kotlin + Jetpack Compose
| Option | Weighted score /100 |
|---|---|
| **Kotlin + Compose** | **90.6** |
| Flutter | 82.6 |
| .NET MAUI + SkiaSharp | 56.8 |
| Godot | 53.8 |

Why: built-in TalkBack support for custom boards, predictive back, edge-to-edge; the only option that clearly meets F-Droid, size (~5 MB) and startup goals. Trade-off: Kotlin is new to the founder (2–3 weeks to fluency; maps closely to C#).

Fallbacks: if TalkBack on boards fails the week-1 test → classic Android `View` + `ExploreByTouchHelper`; if Kotlin velocity is unacceptable → re-run the test in Flutter.

## Modules
```
build-logic/            convention plugins
:app                    direct/play flavours, navigation, DI root
:core:engine     (KMP)  Pcg32, SplitMix64, DailySeed, GameEngine, History, SaveEnvelope, A11yModel
:core:board             canvas, hit-testing, gestures, TalkBack semantics layer
:core:designsystem      generated tokens, theme, fonts, icons
:core:data              saves (JSON), stats (Room), settings (DataStore)
:core:testing           golden-seed harness, fixtures, fake clock
:feature:hub|stats|settings|tipjar
:tipjar:play | :tipjar:noop
:games:<id>:engine (KMP)  rules, generator, solver, grader
:games:<id>:ui            renderer, a11y mapping, GameRegistration
:benchmark              Macrobenchmark + baseline profile
```

## Engine contract
```kotlin
interface GameEngine<S : Any, A : Any, C : Any> {
  val id: GameId; val generatorVersion: Int; val saveVersion: Int
  fun newGame(config: C, seed: Long, isActive: () -> Boolean = { true }): S
  fun reduce(state: S, action: A): Reduction<S>   // new state + events
  fun isSolved(state: S): Boolean
  fun hint(state: S): Hint<A>?
  fun describe(state: S, focus: CellRef?): A11yModel   // string keys, never text
  val stateSerializer: KSerializer<S>; val actionSerializer: KSerializer<A>
  fun migrate(from: Int, json: JsonElement): JsonElement
}
```
Events: `Moved`, `Rejected`, `Merged`, `Won`, `Lost`, `Stat`. MVI: ViewModel holds `StateFlow<UiState>`; events map to haptics, animations, announcements, stats writes. Games register through Hilt `@IntoSet`; adding a game = one module + one line in `:app`.

**Undo** is event-sourced: save = initial state + action log + cursor; in memory a snapshot ring. RNG state lives in the game state, so undo cannot re-roll Merge spawns.

## Puzzle generation
- PRNG: in-house `SplitMix64` (seeding) + `Pcg32` (streams), Lemire bounded ints, Fisher–Yates; frozen.
- Daily seed = `splitMix64(fnv1a64("daily|$gameId|v$generatorVersion|$localDate"))`. Per-game `DailySchedule` maps date ranges to generator versions.
- Generators live in `generators/vN/`, never edited once released (`frozen.txt`, CI check). 200 golden seeds per version.
- **Sudoku:** bitmask backtracking with MRV counts solutions up to 2. Clues removed in symmetric pairs. Human-technique grader (singles → subsets → pointing/box-line → X-Wing/Swordfish → XY-Wing → chains) also drives hints. Budget on a low-end phone: Easy/Medium p95 ≤ 300 ms, Hard ≤ 2 s; Expert from a bundled bank (~3,000 puzzles, ~120 KB) built by the same frozen generator; background warm queue.
- **Mines:** generated on first tap with a 3×3 safe zone. Solver: single-cell rules, subset rules, bounded frontier enumeration. If stuck, move mines across the frontier (approach of Simon Tatham's `mines.c`, reimplemented, credited). Expert 30×16/99 p95 ≤ 150 ms.
- **Merge:** spawns use the `Pcg32` stored in the state.

## Storage
- Saves: one JSON file per game via `AtomicFile`, written by one conflated writer coroutine after every move; flush on `ON_STOP`; thumbnail PNG on stop/exit.
- `hub.json` (~1 KB) for fast cold start; Room not opened at startup.
- Stats/streaks: Room. Settings: DataStore.
- Envelope `{format, gameId, saveVersion, generatorVersion, payload}`; pure JSON migrations tested against fixtures from every release; unmigratable saves go to `saves/broken/`, game starts fresh.
- Auto Backup rules (saves, DB, DataStore; not thumbnails/caches); zip export/import via SAF.

## Build and distribution
- Version catalog, convention plugins, dependency verification. Flavour dimension `distribution`: `direct` and `play`; only `play` links Play Billing.
- CI guards: no `INTERNET`/`ACCESS_NETWORK_STATE` in the Direct APK (`aapt2 dump permissions`); no `com.google.android.gms`, `com.android.billingclient`, `firebase` on the Direct classpath or in its dex; Direct APK ≤ 8 MB.
- R8 full mode, resource shrinking, `localeFilters`, per-icon vectors, Inter subset with `tnum`, baseline + startup profiles.
- F-Droid: `dependenciesInfo` off, `vcsInfo` off, pinned JDK and Gradle checksum, double-build reproducibility check.
- From MoneyRecord: `cut-release.yml`, `backport.yml`, `release-version.ps1`, signing step, mapping check, Play upload, fastlane layout. Sentry dropped; Direct build keeps a local crash log with "Copy report".

## Quality
Property tests (unique Sudoku, independent Mines solver, replay determinism, serialize round-trip); golden seeds; Roborazzi screenshots (light/dark × 3 font scales × 2 locales) with accessibility checks; Compose semantics tests; Macrobenchmark on a Galaxy A52 and a Helio G35-class phone; detekt, ktlint, Android Lint; translation parity test (port of MoneyRecord's ResourceParityTests).
