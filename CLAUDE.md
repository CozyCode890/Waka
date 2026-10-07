# Weka-GUI — session boot router

READ THIS FIRST. Classify the request against §ROUTER, read ONLY that row's files, then stop.
Reading more than the row allows is a defect, not diligence. If no row fits, ask which one does.
Before your first read of every request, print one line: `row: <name> -> <files>`.

## §STATE
```
phase:   EXECUTING       # PLANNING > STAGING > DETAILING > EXECUTING > CLOSING > REPLANNING
stage:   1/7 S00 CURRENT # T01+T03 done, T02 install done; wrapper + T04..T15 remain
design:  W1 done         # shell graded 2026-10-07; W2 (options panel) due S02, host model pre-set
NEXT:    Write spike\pom.xml for S00-T04 — JavaFX plain `27`, AtlantaFX 3.0.0 — then T02's mvnw.
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
| `cold` — first session ever | all of memory/ | — | done/ |

## §AUTHORITY
| file | you edit freely | ask the user first |
|---|---|---|
| CLAUDE.md | §STATE block only | everything else; to ADD a rule you must DELETE one |
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
RULES 40 · SOURCE 90 · DESIGN 110 · each file in done/ 150

Count: `Get-ChildItem CLAUDE.md, memory\*.md | % { "{0,4} {1}" -f (Get-Content $_ | Measure-Object -Line).Lines, $_.Name }`
