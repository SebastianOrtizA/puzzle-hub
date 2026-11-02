# Design

## Principles
The board is the product (~70% of the screen) · two taps or fewer from launch to playing · calm (no nagging, badges, streak guilt) · one family of games · colour is never the only cue · accessibility is a feature · forgiving (auto-save, undo, no "are you sure?").

## Navigation
- Pages: Hub (root), Game, Stats, Settings, About.
- Bottom sheets: New game/difficulty, game menu, results, how to play.
- Back always saves and never asks. Predictive back closes a sheet first, then goes game → Hub.
- Tapping a game resumes it, or starts one at the last-used difficulty (Easy on first run).

```
HUB                                  GAME (same frame for every game)
┌────────────────────────────────┐   ┌────────────────────────────────┐
│ Puzzles                   ⚙  ⋯ │   │ ←  Sudoku · Medium   12:04  ⋯  │
│ ┌────────────────────────────┐ │   ├────────────────────────────────┤
│ │ ▶ CONTINUE                 │ │   │      ┌──────────────────┐      │
│ │ Sudoku · Medium · 12:04    │ │   │      │      BOARD       │      │
│ │ [thumbnail]      Resume ▸  │ │   │      │ (square, as big  │      │
│ └────────────────────────────┘ │   │      │   as possible)   │      │
│ Today                  Nov 1   │   │      └──────────────────┘      │
│ ┌────┐┌────┐┌────┐┌────┐       │   │   [ 1 ][ 2 ][ 3 ][ 4 ][ 5 ]    │
│ │Sudo││Mine││Merg││Swit│  ◂▸   │   │   [ 6 ][ 7 ][ 8 ][ 9 ][ ⌫ ]    │
│ │✓4:31││ ○ ││ ◐ ││ ✓  │       │   ├────────────────────────────────┤
│ └────┘└────┘└────┘└────┘       │   │  ↶ Undo   ✎ Notes   ? Hint     │
│ All games (grid)  ·  Stats ▸   │   └────────────────────────────────┘
└────────────────────────────────┘
○ not played  ◐ in progress  ✓ done
```

## Design system
- Material 3 structure, custom-drawn boards. ~14 colour tokens in `design/tokens/tokens.json` (DTCG), converted with Style Dictionary. A theme is one JSON file.
- Per-game accents at equal lightness: Sudoku indigo, Mines teal, Merge amber, Switches violet.
- Wallpaper (dynamic) colour is opt-in, default off, chrome only; boards use tested board tokens.
- Type: Inter (OFL), Latin subset, tabular figures. Icons: Material Symbols Rounded.
- 4dp spacing grid. Radius: cells 4, keys 8, cards 12, sheets 28.
- Motion: 100 / 200 / 300 / 600 ms. Reduced motion = short fade, nothing loops.
- Haptics: Android constants (tap, long-press, reject, confirm), at most one per input. Sounds off by default.

## Games
- **Sudoku:** tap cell then number; long-press a digit to lock it. 2×5 pad. Notes toggle. 3-step hints (name technique, highlight cells, fill). Easy / Medium / Hard / Expert.
- **Mines:** taller-than-wide board fitted to width, optional pinch-zoom, "Large cells" ≥48dp. Tap reveals, 350 ms long-press flags, Reveal/Flag toggle for one hand. No double-tap. First tap safe; no-guess mode on by default.
- **Merge:** swipe anywhere or arrow keys. Slide 100 ms, merge pop 120 ms. 4×4, 5×5, 6×6.
- **Switches:** tap toggles a cell and its neighbours. Lit cells show ● (not colour alone). Move counter vs par. 4×4 to 6×6.

## Accessibility
TalkBack plays every board ("Row 3, column 5, 7, given"; custom actions such as Flag, Move up; Merge announces what changed). Targets ≥48dp, text contrast ≥4.5:1, chrome text scales to 200%, colour-blindness checked. Per release: TalkBack run with screen off per game, Accessibility Scanner with zero errors.

## Tip jar
Row in Settings and About, plus an optional line on the results sheet after every 25th win. Direct build opens a donation page in the browser (no billing code, no INTERNET permission).

## Localization
Design for strings ~40% longer (fr / pt). Use start/end, never left/right. Locale formatters for numbers and dates. Plurals via resources. No text inside images.
