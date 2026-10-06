<!--
READ WHEN : architecture question · refactor · unplanned feature · stage kickoff.
EDIT      : appending is yours. Never edit or delete an existing line.
CAP       : 80 lines. Over cap: move superseded lines to the bottom under §Retired.
HARD RULE : append-only. Changed your mind? Add a new line ending in `supersedes D-NNN`.
            A rewritten decision log is unrecoverable, and re-litigating a settled decision
            every few sessions is exactly what this file exists to stop.
-->

# Decisions

Format: `D-NNN | yyyy-mm-dd | decision | why | rejected`

D-001 | 2026-10-06 | Project memory lives in `D:\Weka-GUI\memory\`, tracked by git | memory travels with the code, every automatic edit becomes a reviewable diff, and the user can open RULES.md themselves | Claude Code's native per-project store, which sits outside the repo and whose one-fact-per-file convention costs one read per fact; mirroring into both, which guarantees two sources of truth with no rule to arbitrate between them

D-002 | 2026-10-06 | §ROUTER lives in CLAUDE.md; source read-depth lives in SOURCE.md | the router must be free to consult, so it belongs in the only auto-loaded file, while the source map grows with `src\` and the auto-loaded file must not grow | a single POLICY.md for criteria 8+9+10, which costs a ~2.3k-token read before any work can start and is simply skipped by sessions in a hurry; putting everything in CLAUDE.md, which pushes it past 200 lines until it gets skimmed

D-003 | 2026-10-06 | Every memory file is written in English | the facts are about Java identifiers, so English keeps a grep for a class name working; routing keywords stay short | all-Vietnamese, where a `[gotcha]` about a Weka API would not be found by grepping its English class name; mixing languages inside a line, which makes diffs noisy

D-004 | 2026-10-06 | Stage detail is generated just-in-time — one CURRENT.md, for the current or next stage only | detail written after the previous stage's real outcome is far less often wrong, and the read cost stays flat however many stages the project grows | pre-generating two stages ahead; a `stages\` directory with every stage detailed up front, where most content goes stale before it is used and is wrong *confidently*

D-005 | 2026-10-06 | One SessionStart hook that prints §STATE; no PostToolUse hook | it recovers the state cursor when a long session's compaction drops CLAUDE.md out of context, for about five lines of config and no interruption to any write | a PostToolUse write-log, since `git diff` already answers "what did you change" and an append nothing enforces becomes a partial record that looks authoritative; no hooks at all, which would leave the whole system resting on compliance alone

D-006 | 2026-10-06 | PLAN.md and STAGES.md stay separate files | they have opposite edit rhythms — the plan must freeze, stage status changes several times per session — and a clean authority boundary is what gives the §AUTHORITY table any force | one file with a stage table at the end, where Claude would be writing into the same file that holds the project's scope, and "the plan is inviolable" stops meaning anything

D-007 | 2026-10-06 | Decisions get their own append-only DECISIONS.md rather than a `[dec]` tag inside FACTS.md | a decision carries a rejected alternative and a fact does not, and append-only is natural here while facts stay editable | folding them into FACTS.md, which saves a file and a read but mixes two write modes in one file

D-008 | 2026-10-06 | SESSION.md keeps exactly two blocks, and the third may only be dropped after its still-true lines are promoted into FACTS.md | forgetting is deliberate and read cost stays flat, with a funnel into long-term memory so nothing useful is lost silently | one file per session in a `log\` directory, which grows without bound and which no router row reads

## §Retired
<!-- Superseded lines move here, unchanged, so the reasoning survives without cluttering the
     live list. Never delete one. -->
