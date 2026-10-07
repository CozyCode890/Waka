<!--
READ WHEN : any request that draws, lays out or styles UI — rows edit, feature-in, kickoff, refactor.
EDIT      : §Mockups and §Ledger are yours. A LOCKED number is append-only: supersede it in
            §Ledger with a reason, never overwrite it in place.
CAP       : 110 lines. Over cap means a wave's detail belongs in its own mockup file, not here.
HARD RULE : this file holds numbers the USER graded. A number with no §Ledger row was never
            graded and is not binding — say so rather than defending it.
-->

# Design contract

Source of truth: `design/shell-w1.html` → https://claude.ai/artifact/UDBpGJDxXgEoTzz6xt1Tar
Shell graded 2026-10-07. Dialogs, task tabs and the package manager are not drawn yet.

## Review protocol
Mockups are HTML under `design/`, git-tracked, published to one artifact URL per wave and
re-published to the SAME url when edited. Restricted to constructs JavaFX can express: nothing
HBox/VBox/GridPane cannot do, no `calc()` chains, no multi-layer shadow, system fonts only.
Numbered marks (`G-NNN`) drawn on the page carry the question; the user answers with the id.

| wave | covers | when |
|---|---|---|
| W1 | shell — caption, rail, document tabs, segmented bar, panel, status bar, palette | DONE 2026-10-07 |
| W2 | the options editor panel, replacing GenericObjectEditor | S02 kickoff |
| W3 | Preprocess + Classify with real data | S03 kickoff |
| W4 | the four remaining task tabs, package manager, About | S04 / S05 |

## §Geometry — LOCKED 2026-10-07
| token | value | applies to |
|---|---|---|
| `--h-caption` | 32px | native Win11 caption; not ours to change |
| `--h-doctab` | 35px | dataset tabs |
| `--h-segbar` | 32px | the six Weka tasks |
| `--h-toolbar` | 40px | per-task action row, 28px controls inside |
| `--h-statusbar` | 22px | status bar |
| `--h-panel` | 224px | bottom panel open, incl. its 34px tab strip |
| `--h-row` | 26px | attribute rows, tree rows |
| `--w-rail` | 48px | rail, 20px icon centred |
| `--w-side` | 260px | side panel |
| `--r-ctl` / `--r-card` | 4px / 8px | controls / cards, flyouts, window. No third radius. |
| `--sp` | 4px | every gap and pad is a multiple of 4 |

## §Colour — LOCKED 2026-10-07
Real Fluent 2 values. The AtlantaFX column is a **guess, unverified** — AtlantaFX 3.0.0 is not
on this machine. S01 confirms the names and corrects this table; it does not re-guess them.

| token | dark | light | role | AtlantaFX target (verify S01) |
|---|---|---|---|---|
| `--mica` | `#202020` | `#F3F3F3` | opaque base when Mica is off | `-color-bg-default` |
| `--card` | `#FFF 5.12%` | `#FFF 70%` | cards, active document tab | `-color-bg-subtle` |
| `--ctl` | `#FFF 6.05%` | `#FFF 70%` | buttons, inputs, segment bar | `-color-bg-inset` |
| `--divider` | `#FFF 8.37%` | `#000 8.03%` | every structural line | `-color-border-muted` |
| `--stroke-ctl` | `#FFF 6.98%` | `#000 5.78%` | control borders | `-color-border-default` |
| `--fg1` | `#FFFFFF` | `#000 89.56%` | primary text | `-color-fg-default` |
| `--fg2` | `#FFF 78.6%` | `#000 60.63%` | secondary text, icons | `-color-fg-muted` |
| `--fg3` | `#FFF 54.42%` | `#000 44.58%` | captions, metadata | `-color-fg-subtle` |
| `--accent` | `#4CC2FF` | `#005FB8` | selection, focus, primary action | `-color-accent-emphasis` |
| `--accent-sub` | accent 16% | accent 10% | selected row, status bar tint | `-color-accent-muted` |
| `--ok/warn/bad` | `#6CCB5F` `#FCE100` `#FF99A4` | `#0F7B0F` `#9D5D00` `#C42B1C` | semantic state | `-color-{success,warning,danger}-emphasis` |
| `--cls1..3` | `#5B8FF9` `#E8684A` `#5AD8A6` | `#3A6FD8` `#C9502F` `#2E9E74` | class values in plots | app-owned, no equivalent |

Mica OFF swaps to solid Layer fills, it does not simply drop the blur: dark `--card` 9.8%,
`--ctl` 8.4%, `--divider` 11.8%; light `--card` `#FAFAFA`, `--ctl` `#FDFDFD`, `--divider` 11.2%.
Theme default is **auto** — follows Windows. Light and dark both ship.

## §Type — LOCKED 2026-10-07
No webfont, ever: the app renders in the Windows system stack, so mockups must too.
Body 14/400 · body small 13/400 (rows, tabs) · caption 12/400 (status bar) ·
label 11/600 .06em caps (panel headers) · subtitle 20/600 (empty states, dialog titles) —
all `Segoe UI Variable Text`, Display at subtitle. Mono `Cascadia Mono → Consolas` 12/1.55
for Weka output and option strings.

## §Components — LOCKED 2026-10-07
- **Rail** 48px, RIGHT edge (D-013), app modes only: Datasets · Search · Runs · Packages, with
  Settings pinned to the bottom. Active mode is a 2px accent bar on the rail's INNER edge.
- **Document tab** = one dataset. 2px accent on the TOP edge, `--card` fill when active.
  Unsaved work is a dot that becomes the close X on hover.
- **Segmented bar** inside the tab, six full labels in Explorer's exact words. The bar scrolls
  horizontally when narrow; labels are never shortened and never become icons.
- **Apply / Stop** are a fixed pair in the toolbar that swap state — idle: Apply accent, Stop
  grey; running: Apply grey, Stop accent. The control never moves or appears. The status bar
  reports progress only and carries no run control.
- **Option string** (`-M 99.0`) is always visible beside the scheme button, in mono, editable.
- **Bottom panel** = four providers: Weka Output · Terminal · Package Log · Problems.
- **Status bar** tinted with `--accent-sub`. Left = what the app is doing; right = what the
  dataset is, plus a heap meter.
- **Undo toast** bottom right, above the status bar, auto-dismissing. Nothing asks "are you sure?".
- **Accent is reserved** for selection, focus and ONE primary action per surface. Plot colour
  uses `--cls*` and never borrows the accent.
- **Attributes are rows, never columns.** No view maps one TableView column per attribute.

## §Mockups
| wave | file | artifact | graded |
|---|---|---|---|
| W1 | `design/shell-w1.html` | `claude.ai/artifact/UDBpGJDxXgEoTzz6xt1Tar` | 2026-10-07 |

## §Ledger
Format: `G-NNN | yyyy-mm-dd | verdict`. Append only; supersede with a reason.

G-001 | 2026-10-07 | native caption kept, recoloured — accepted as proposed
G-002 | 2026-10-07 | **changed** — Search added as a fourth rail mode
G-003 | 2026-10-07 | active-mode bar on the rail's inner edge, facing the panel
G-004 | 2026-10-07 | side panel 260px
G-005 | 2026-10-07 | document tab accent on the top edge
G-006 | 2026-10-07 | unsaved = dot, becomes close X on hover
G-007 | 2026-10-07 | six full labels, bar scrolls when narrow; Explorer's words are untouchable
G-008 | 2026-10-07 | option string always visible on the toolbar, editable
G-009 | 2026-10-07 | attributes as rows, 26px
G-010 | 2026-10-07 | **changed** — Terminal added as a fourth provider; plain console in v1
G-011 | 2026-10-07 | Weka's verbatim mono output accepted
G-012 | 2026-10-07 | status bar tinted with accent-subtle
G-013 | 2026-10-07 | **replaced** — the user's own design: Apply/Stop toolbar pair, state-swapped
G-014 | 2026-10-07 | command palette 600px, 76px from top
G-015 | 2026-10-07 | undo toast, bottom right
G-016 | 2026-10-07 | **changed** — Mica off swaps to solid Layer fills, not just blur removed
G-017 | 2026-10-07 | 4px controls / 8px cards, no third radius
G-018 | 2026-10-07 | accent reserved; plot colour is a separate ramp
theme | 2026-10-07 | default auto, follows Windows
dialog | 2026-10-07 | **user's own design, pre-graded** — no modal dialogs; hosted panels like VS Code Settings, header carries close · pop out to a window · open as a tab (D-027). W2 draws it, it does not re-ask it
output | 2026-10-07 | **user's own design, pre-graded** — Weka Output is collapsible run blocks; each header states dataset file · scheme · full option string · start time (D-028)
about | 2026-10-07 | About screen — Weka attribution, GPLv3 notice, player for "Waka Waka" over a user-supplied file in `assets\audio\` (D-026). Drawn in W4 with the package manager
