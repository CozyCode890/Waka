---
description: Close the current stage and generate detail for the next one
---

Regenerating CURRENT.md is the only destructive operation in this directory. Follow this order.

**If STAGES.md is still empty**, this command does something different — skip to *Deriving
stages* at the bottom.

### 1. Verify the exit tests for real

Read the current stage's block in STAGES.md and check each exit test against reality — run it,
or read the code that satisfies it. If any exit test fails, **stop**: name which one, and
archive nothing. "Looks done" is not an exit test passing, and a stage closed early poisons
the next stage's task list.

### 2. Check memory against the disk

List the actual tree:

```
Get-ChildItem D:\Weka-GUI\src -Recurse -File -Name
```

Compare it with CURRENT.md's *Files to touch*. If they disagree — a renamed package, a class
that was never written, a file nobody mentioned — say so. Memory has drifted, and the drift
gets corrected now, before it propagates into the next stage as confident wrong instruction.

### 3. Carry forward, then archive

Copy every **unanswered** line from CURRENT.md §Open questions into a scratch list. Then move
CURRENT.md to `memory\done\S<NN>-<slug>.md` unchanged. Archived files are never edited again.

### 4. Flip the status in STAGES.md

Current stage to `DONE (yyyy-mm-dd)`, next stage to `CURRENT`. Status flips are yours; anything
else in that file — adding, removing, reordering, rewording a goal or exit test — needs the
user's yes.

### 5. Record what was learned

Append to DECISIONS.md any decision the stage produced, append-only, with `supersedes D-NNN`
if it reverses an earlier one. Promote durable gotchas from the archived file into FACTS.md.

### 6. Generate the new CURRENT.md — for one stage only

Base the task list on what the finished stage **actually produced**, not on what the plan
predicted. Use stable task ids `S<NN>-T<NN>`. Fill *Files to touch* from the real tree you
listed in step 2. Re-add the carried-forward open questions. Never write detail for a stage
beyond the next one, however tempting the long view looks.

### 7. Rewrite CLAUDE.md §STATE, then report

Tell the user the new stage's goal, its exit tests, and the first task. List any carried-forward
open question so it gets answered now.

---

### Deriving stages (STAGES.md is empty)

Only do this if PLAN.md is filled in **and** the user has said it is frozen. If PLAN.md still
reads `TODO`, stop and work on PLAN.md with the user instead.

Derive one block per stage straight from PLAN.md, using the shape in STAGES.md's comment: goal,
exit tests, dependencies, status. Five lines maximum per stage, no implementation detail. Order
them so each stage leaves something checkable behind.

Then **stop**. Let the user review the stage list before any CURRENT.md is generated — a wrong
stage boundary is cheap to fix now and expensive to fix after three stages of detail.
