# Product

**Positioning:** "Classic puzzles, nothing else." One app, many offline games. No ads, trackers, login or network permission in the Direct build (APK / F-Droid). The Play build adds an optional tip jar that unlocks nothing.

## Personas
- **Commuter:** 3–10 minute sessions, one-handed; wants to resume exactly where they left off.
- **Puzzle purist:** 35–70, plays daily; wants fair, graded puzzles, a daily puzzle, stats, pencil marks.
- **Privacy-minded FOSS user:** F-Droid or a phone without Google services; recommends the app.

## Games
| Release | Games |
|---|---|
| Walking skeleton (internal) | Switches |
| MVP v1.0 | Sudoku (unique solution, graded by technique), Mines (no-guess mode), Merge |
| v1.1 | Pixel Logic, Code Breaker |
| v1.2 | Slide 15, Solitaire (Klondike / FreeCell) |
| v1.3 | Logic pack (Bridges, Tents, Kakuro, Star Battle) |
| Deferred | Word games |

At most one heavy game per release. Generic names only; original art.

## Features
- **Must (v1.0):** Hub with Continue card; auto-save every move, survives process death; ≥3 difficulties; unlimited undo; offline daily puzzle per game; stats (played, won, best, average time); light/dark/system; en, es, pt-BR, fr, it; accessibility basics.
- **Should:** streaks, hints, haptics, Sudoku pencil marks, tip jar (Play).
- **Could:** extra free themes, local achievements, export/import, tablet/landscape.
- **Won't:** accounts, cloud sync, leaderboards, multiplayer, ads, analytics, push notifications, energy systems, paywalls.

## Targets (measured without trackers)
| Metric | Target |
|---|---|
| Crash-free users | ≥ 99.8% |
| ANR rate | < 0.2% |
| Cold start to hub (median, mid-range 2021 phone) | ≤ 1.2 s |
| Frame rate / input to feedback | 60 fps / < 50 ms |
| Download size (AAB per ABI) | ≤ 20 MB; each game ≤ 2 MB |
| Store rating | ≥ 4.6 |
