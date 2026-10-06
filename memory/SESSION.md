<!--
READ WHEN : cold start · stage kickoff. Skip it otherwise — §STATE answers "where am I".
EDIT      : yours, written at /wrap.
CAP       : 50 lines. EXACTLY two blocks, newest first, 25 lines each at most.
HARD RULE : before dropping the third block, promote anything still true into FACTS.md and
            list it under FACTS.md §Promoted. Skip that and FACTS.md stays empty while this
            file quietly becomes the real memory at 400 lines.
-->

# Sessions

## 2026-10-06 — s02 · froze the plan, derived seven stages
- The session that filled PLAN.md and appended D-009..D-019 died without /wrap. Its reasoning
  lives in those two files; nothing was reconstructed into this log, and the s01 block below
  still reads "PLAN.md is still a skeleton" — true when written, read it that way.
- Derived STAGES.md S00..S06 from the frozen PLAN.md. The user approved all seven and started
  none: no stage is CURRENT, and CURRENT.md is still the untouched template.
- S00 is a risk spike the user added ahead of the shell. Offered and rejected: folding the three
  checks into S01's exit tests, which is what PLAN.md's own "it is a 20-minute test" remark
  implies. The user wanted them proven before anything rests on them.
- The jpackage app-image rehearsal sits in S01, not S06, because PLAN.md asks for packaging to
  be rehearsed early enough that "it only runs from the IDE" is never true for long.
- S03 was deliberately not split despite carrying four proofs — PLAN.md says that one vertical
  slice exists to prove the engine split, run model, cancel and history at once.
- FACTS.md: the false clause of "No Weka GUI source exists yet; PLAN.md is still empty" was
  dropped with the user's yes; the true clause stays. That closed the only open question.
- Router gap found: a "what is left to decide" request matches no row in §ROUTER. This session
  declared `where`, then had to read the kickoff set anyway. Adding a row is an ask-first edit
  to CLAUDE.md — proposed to the user, not done.
- Committed as eebde5f.
- NEXT: do nothing until the user says go. Then /stage details S00 only, never a later stage.

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
