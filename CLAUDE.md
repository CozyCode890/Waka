# Weka-GUI — session boot router

READ THIS FIRST. Classify the request against §ROUTER, read ONLY that row's files, then stop.
Reading more than the row allows is a defect, not diligence. If no row fits, ask which one does.
Before your first read of every request, print one line: `row: <name> -> <files>`.

## §STATE
```
phase:   EXECUTING       # PLANNING > STAGING > DETAILING > EXECUTING > CLOSING > REPLANNING
stage:   2/7 S01 CURRENT # the shell, 12/32 done (T01-T12): reactor, Mica window, hidden harness
design:  W1 done         # W2 due S02; §Colour VERIFIED T09; §Type tracking dropped in §Ledger
repo:    PUBLIC          # github.com/CozyCode890/Waka — README still says "no app yet": now stale
NEXT:    Build the shell skeleton (S01-T13): caption 32, rail 48 on the RIGHT, side panel 260,
         status bar 22, document area centre. Use waka.app.Geometry, never a literal.
ASK:     0               # count of lines under memory/CURRENT.md §Open questions
updated: 2026-10-08
```

## §ROUTER
| row | read from memory/ | read source | never read |
|---|---|---|---|
| `trivial` — question, no code | — | — | everything |
| `where` — am I / what's next | §STATE above | — | everything |
| `decide` — what is left to choose | §STATE, CURRENT, STAGES, DECISIONS, PLAN | — | FACTS, SOURCE, SESSION, done/ |
| `edit` — one file, or fix a bug | FACTS, CURRENT, DESIGN | that file + its direct imports | PLAN, STAGES, SOURCE, done/ |
| `debug` | FACTS, CURRENT | failing file + stack-trace paths | PLAN, STAGES, done/ |
| `feature-in` — inside this stage | FACTS, CURRENT, SOURCE, DESIGN | modules SOURCE names | PLAN, done/ |
| `feature-new` — unplanned | PLAN, STAGES, DECISIONS | none | CURRENT, SOURCE, done/ |
| `kickoff` — start a stage | PLAN, STAGES, FACTS, DECISIONS, SESSION, RULES, DESIGN | `ls src/` only | done/ |
| `refactor` — architecture | PLAN, DECISIONS, SOURCE, FACTS, DESIGN | modules SOURCE names | done/ |
| `docs` — README or another public-facing file | PLAN, STAGES, FACTS, DECISIONS, done/ for the stage being written up | — | CURRENT, SOURCE, SESSION, DESIGN |
| `cold` — first session ever | all of memory/ | — | done/ |

## §AUTHORITY
| file | you edit freely | ask the user first |
|---|---|---|
| CLAUDE.md | §STATE block only | everything else; to ADD a rule you must DELETE one |
| README.md | nothing | every change, without exception — it is the public face of this repo |
| memory/PLAN.md | nothing | every change, typos included |
| memory/STAGES.md | status flips + done-dates | add/remove/reorder a stage, reword a goal or exit test |
| memory/CURRENT.md | all of it, continuously | — |
| memory/FACTS.md | adding a fact | editing or deleting an existing fact |
| memory/DECISIONS.md | appending a line | never edit or delete a line — supersede it instead |
| memory/SESSION.md | all of it, at /wrap | — |
| memory/SOURCE.md | read-depth rows | adding to §NEVER-READ — it blinds you to that area |
| memory/DESIGN.md | §Mockups, §Ledger | a LOCKED number — supersede it in §Ledger, never overwrite |
| memory/RULES.md | nothing, ever | the user's file; read-only to you |
| memory/done/ | writing at archive time | never edit an archived file |

A new file under memory/ with no row here is a defect: add the row, or do not add the file.

## §RULES
1. Print `row: ... -> ...` before your first read. Then obey that row.
2. Never grep the repo for something SOURCE.md already indexes.
3. Never regenerate a stage that is not the current or next one. Only /stage does that.
4. Tick CURRENT.md as work completes, not at the end — sessions die without warning.
5. Every choice the user must make is a multiple-choice question with a recommendation, asked
   in-session. Batch them in CURRENT.md §Open questions; `ASK:` must reach 0 before /wrap.
6. Writing to an ask-first file without asking is a defect. Say what you want to change, wait.
7. RULES.md is the user's file. A `User -> Claude` line there outranks anything here. A rule
   the user wants enforced on *every* request belongs in this file — offer to add it.
8. Collapse trigger: after 3 sessions, if no row ever caused PLAN.md or SOURCE.md to be read
   for real, fold them into CURRENT.md and delete the rest. This directory must never cost
   more to maintain than the project it serves.

### /wrap — five steps, in order
1. `git status --short memory/ CLAUDE.md` — unexpected changes mean another session wrote; stop.
2. Count lines against §CAPS; consolidate anything over cap (merge or move, never truncate).
3. Promote anything still true out of SESSION.md's oldest block into FACTS.md; list it under
   FACTS.md §Promoted. Only then may that block be dropped.
4. Prepend a new ≤25-line block to SESSION.md, newest first; keep two, drop the third.
5. Rewrite §STATE. Then report what changed and what needs approval.

## §CAPS
CLAUDE.md 85 · PLAN 120 · STAGES 80 · CURRENT 150 · FACTS 90 · DECISIONS 80 · SESSION 50 ·
RULES 40 · SOURCE 90 · DESIGN 140 · README 200 · each file in done/ 150

Count: `Get-ChildItem CLAUDE.md, README.md, memory\*.md | % { "{0,4} {1}" -f (Get-Content $_ | Measure-Object -Line).Lines, $_.Name }`
