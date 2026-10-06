---
description: Close the session — check for conflicts, count lines, promote facts, write SESSION.md, rewrite STATE
---

Run all five wrap-up steps from CLAUDE.md §RULES, in this order. Do not skip one. If a step
cannot be completed, say so and stop rather than carrying on to the next.

### 1. Check for a parallel writer

```
git -C D:\Weka-GUI status --short memory/ CLAUDE.md
```

If there are changes you did not make in this session, **stop**. Another session — or the user
— wrote here, and the last write would win silently. Report what you see and let the user
decide before you touch anything.

### 2. Count lines against §CAPS

```
Get-ChildItem D:\Weka-GUI\CLAUDE.md, D:\Weka-GUI\memory\*.md | ForEach-Object { "{0,4}  {1}" -f (Get-Content $_ | Measure-Object -Line).Lines, $_.Name }
```

For every file over its cap, consolidate it: merge duplicate lines, or move detail into the
file that owns it. Never truncate. If consolidating would require an ask-first edit — anything
in PLAN.md, a deletion from FACTS.md, structure in STAGES.md — propose it instead of doing it.

### 3. Promote, then drop

If SESSION.md already holds two blocks, read the older one. Move anything still true into
FACTS.md as a tagged one-line fact, then list what you promoted under FACTS.md §Promoted.
**Only then** may that block be dropped. A fact about code must contain the English identifier
it is about, or a later grep will not find it.

### 4. Prepend this session's block to SESSION.md

At most 25 lines, newest first. Cover: what actually changed, what was tried and abandoned and
why, and what the next session should do first. Keep the second block; drop the third (step 3
has already drained it). Write what a stranger would need, not a transcript.

### 5. Rewrite CLAUDE.md §STATE

- `phase` — PLANNING / STAGING / DETAILING / EXECUTING / CLOSING / REPLANNING
- `stage` — N/M from STAGES.md
- `NEXT` — one imperative sentence. Not a topic, an instruction.
- `ASK` — the number of lines under CURRENT.md §Open questions
- `updated` — today's date

### Then report, in chat

What you changed in memory, which files are over cap, and anything waiting on the user's
approval. If §Open questions is not empty, list those questions so the user can answer now
rather than at the next kickoff.
