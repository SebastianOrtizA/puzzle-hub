# Puzzles (working title)

Offline Android-first hub of classic logic games. Free, AGPL-3.0, no ads, no trackers, no login. Plan and rationale: `docs/`. Final name and package id are decided in Phase 0 (`docs/roadmap.md`); until then the package is `app.puzzles`.

## Stack
Kotlin + Jetpack Compose (Material 3). Game logic in pure Kotlin Multiplatform modules (`commonMain` only, no Android imports). Hilt for DI, Navigation 3, Room (stats), DataStore (settings), kotlinx.serialization (saves).

## Module layout (target)
`:app` · `:core:engine` (KMP) · `:core:board` · `:core:designsystem` · `:core:data` · `:core:testing` · `:feature:hub|stats|settings|tipjar` · `:tipjar:play` / `:tipjar:noop` · `:games:<id>:engine` (KMP) · `:games:<id>:ui` · `:benchmark`. Only `:app` exists so far.

## Rules
- Engines are pure `commonMain`. A game implements `GameEngine<S, A, C>`; `reduce` is synchronous, pure and under 1 ms.
- Randomness only through our own `Pcg32` / `SplitMix64`. `kotlin.random` and `java.util.Random` are forbidden (detekt `ForbiddenImport`).
- Never edit a released `generators/vN/` folder (listed in `frozen.txt`). A generator change is a new `vN+1` with a `DailySchedule` cut-over date. Golden-seed tests (200 seeds per version) must pass.
- No user-visible string outside resources. Accessibility text goes through `A11yModel` keys. Languages: en, es, pt-BR, fr, it; a missing translation fails the build.
- Every save-format change needs a migration and a fixture from the previous version. Fixtures are kept forever.
- Money-free app: no analytics, crash-reporting SDKs or network code. The `direct` flavour must have no `INTERNET` permission and no Google Play Services / billing classes (CI guards).
- Every new dependency needs a licence check (must be FOSS-compatible for F-Droid).
- Generic game names only (Sudoku, Mines, Merge, Switches, Pixel Logic, Code Breaker, Slide 15, Solitaire). Never Minesweeper, 2048, Picross, Mastermind or Lights Out. All art is original.
- Colours come from `design/tokens/tokens.json` (source of truth), never hard-coded in UI code. Colour is never the only cue.
- Business logic is tested in Core/engine modules; boards are pure functions from state to drawing so they can be screenshot-tested.

## Definition of done (per game)
Engine property tests + golden seeds · matches the design spec with screenshot tests · undo/redo work and the game survives `adb shell am kill` · daily puzzle and stats wired · TalkBack walkthrough passes · all 5 locales complete · adds ≤ 2 MB per ABI · frame-time benchmark passes · store screenshots refreshed.

## Workflow
Trunk-based on `main`; `release/x.y` branches; tags only from release branches; backports by PR label. Pattern borrowed from `D:\Projects\MoneyRecord` (`cut-release.yml`, `backport.yml`, fastlane layout).
