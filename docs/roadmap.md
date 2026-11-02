# Roadmap (starts 2026-11-02)

## Phase 0 (1 week): setup
- [ ] Final name + trademark check (EU, US, BR) and package id (temporary: `app.puzzles`)
- [x] Repo, AGPL-3.0 licence, CLAUDE.md, docs
- [x] Gradle wrapper (9.6.0) + version catalog (AGP 9.5.0, Kotlin 2.4.21, Compose BOM 2026.08.00); convention plugins still to do once there is a second module
- [x] `direct` and `play` flavours; `assembleDirectRelease` builds (658 KB, no network permissions). Not yet run on an emulator or device.
- [x] PR CI with the Direct-permission guard (`.github/workflows/pr.yml`); guards verified locally, workflow itself not yet run on GitHub
- [x] Token pipeline: `buildSrc` `GenerateTokensTask` turns `design/tokens/tokens.json` into `Tokens.kt` (colours, spacing, radii, durations). A Gradle task instead of Style Dictionary, so no Node or extra dependency.
- [ ] Buy two reference phones (Galaxy A52-class and Helio G35-class)
- [ ] Design: principles, screen map, shared game-frame wireframes

Exit: CI green; Direct APK installs with no permissions.

## Foundation (4 weeks)
**Week 1 go/no-go test** (Switches + mock 30×16 Mines board):
1. Cold start median ≤ 900 ms on the A52 (release + baseline profile) — **pending device**: `:benchmark` `StartupBenchmark` written
2. Mines pinch/pan ≤ 1% janky frames — **pending device**: `MinesGestureBenchmark` runs end to end on the emulator (numbers not valid there)
3. TalkBack over 480 cells: row-major order, Flag/Chord actions, live-region announcements, no frame > 16 ms — **partial**: 480 nodes in row-major order and live region confirmed via uiautomator on emulator; custom actions under real TalkBack and frame time still to check
4. 100 ms slide at 60 fps — **not started** (needs a tile-slide spike)
5. Direct APK ≤ 6 MB — **pass**: 0.79 MB release (R8 + shrink)
6. Founder writes Switches `reduce`, undo and save in ≤ 2 days — **pending founder** (hook: `onCellTap` in `SwitchesBoard.kt`)

If (3) fails → `ExploreByTouchHelper` fallback; if (6) fails badly → Flutter re-spike.

Weeks 2–4: `:core:engine`, `:core:board`, `:core:data`, `:core:designsystem`; Hub with Continue + thumbnails; settings; stats; daily framework; Switches to full definition of done; release pipeline to Play internal. In parallel (agents): Sudoku engine; tokens v1; locale scaffolding.

## MVP v1.0
| Milestone | Weeks | Scope | Exit criteria |
|---|---|---|---|
| M1 Sudoku | 6–8 | Board, notes, hints, 4 difficulties, bank, daily | DoD met; generation timing passes on low-end phone |
| M2 Mines | 9–10 | No-guess generator, zoom/pan, flag, chord, daily | Expert p95 ≤ 150 ms; independent solver agrees on 10,000 seeds |
| M3 Merge | 11–12 | Animations, swipe + d-pad, best scores | 60 fps at 0.5×/1×/2× animation scale |
| M4 Release | 13–14 | Tip jar (Play), export/import, accessibility audit, store listings ×5 locales, F-Droid MR | Reproducible build; 14-day closed test ≥ 12 testers; crash-free ≥ 99.8% |

v1.0 expected around the end of February 2027.

## After v1.0
v1.1 Pixel Logic + Code Breaker · v1.2 Slide 15 + Solitaire · v1.3 Logic pack · May 2027 Compose Multiplatform iOS spike (Hub + Switches) · mid-2027 iOS decision.

## Resolved disagreements
- Sudoku difficulties: 4 (Easy, Medium, Hard, Expert).
- Simon Tatham's Mines method: reimplement the approach with credit, copy no code.
- Direct tip jar: browser link to a donation page; no billing, no INTERNET permission.
- MVP length: 9 weeks + 2 release weeks.
