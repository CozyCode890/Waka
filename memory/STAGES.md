<!--
READ WHEN : stage kickoff · status question · triaging an unplanned request · past-stage lookup.
            NOT for a one-file edit, a bug fix, or a trivial question.
EDIT      : status flips and done-dates are yours. Adding, removing, reordering or rewording
            a stage needs the user's yes.
CAP       : 80 lines.
HARD RULE : no implementation detail, ever. A stage block longer than five lines means you are
            writing CURRENT.md in the wrong file.
-->

# Stages

Derived from PLAN.md, one block per stage. Status: `TODO` | `CURRENT` | `DONE (yyyy-mm-dd)`.
Exactly one stage may be `CURRENT`.

> STATUS: **EMPTY**. Derive these from PLAN.md once the user has frozen PLAN.md
> (phase STAGING). Run `/stage` to do it — it will stop and let the user review before any
> stage detail is generated.

<!-- Shape of one block — copy this, do not add fields:

## S01 — <short title>
goal      : <one line, the outcome not the work>
exit test : <a condition someone else could check>
exit test : <a second one, if the first is not enough>
depends   : S00 | none
status    : TODO

-->
