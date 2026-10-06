<!--
READ WHEN : cold start · stage kickoff. Skip it otherwise — §STATE answers "where am I".
EDIT      : yours, written at /wrap.
CAP       : 50 lines. EXACTLY two blocks, newest first, 25 lines each at most.
HARD RULE : before dropping the third block, promote anything still true into FACTS.md and
            list it under FACTS.md §Promoted. Skip that and FACTS.md stays empty while this
            file quietly becomes the real memory at 400 lines.
-->

# Sessions

## 2026-10-06 — s01 · built this memory directory
- Project directory was empty. No Weka GUI code exists, and nothing about the GUI itself has
  been decided — PLAN.md is still a skeleton.
- Designed this directory by running three independent designs in parallel (context-economy,
  lifecycle, governance lenses) and then a critic pass to synthesise one hybrid.
- The critic cut, with reasons: an INDEX file, a separate STATE file, a separate Claude-rules
  file, a write-audit changelog, a questions inbox, a templates directory, a validator script,
  numbered filename prefixes, and a per-session log directory. See DECISIONS.md.
- The user settled all eight open questions: memory inside the project, router in CLAUDE.md
  with source depth split into SOURCE.md, all-English files, `/wrap` + `/stage`, PLAN and
  STAGES split, a separate DECISIONS.md, a two-block SESSION.md, and one SessionStart hook.
- Known holes, stated rather than solved: nothing enforces these rules beyond the one hook;
  SOURCE.md is a guess until source exists, so it starts almost empty on purpose; a session
  that dies abruptly loses its SESSION.md block; two parallel sessions could both write §STATE.
- NEXT: fill PLAN.md with the user — goal, scope, NON-goals, stack, success tests — then
  freeze it and run `/stage` to derive STAGES.md.
