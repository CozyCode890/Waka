<!--
READ WHEN : cold start · stage kickoff · before anything irreversible.
EDIT      : USER ONLY. Claude never writes to this file, not even to fix a typo. If Claude
            thinks a rule belongs here, it says so in chat and you decide.
CAP       : 40 lines. Keep it short enough that you actually re-read it.
HARD RULE : short imperative bullets. A paragraph here is a rule nobody follows.

NOTE  : this file is read on `cold` and `kickoff` rows only. A rule you need obeyed on EVERY
        request has to live in CLAUDE.md §RULES instead — ask Claude to add it there, and it
        will delete one to make room (the 85-line cap is the point). Keep this file for what
        you commit to, plus standing preferences worth re-reading at a stage boundary.
-->

# Rules

## User -> User
<!-- What you commit to, so this system does not rot. These are yours; rewrite freely. -->
- Freeze PLAN.md before letting any code be written.
- Answer the lines under CURRENT.md §Open questions before the next stage kickoff.
- Refuse line 86 of CLAUDE.md. Growth there is a tax paid every single session.
- Read the git diff of a memory change before accepting it. That is the whole reason memory
  is inside the repo.
- Run `/wrap` before closing a session, even a short one. Memory systems do not fail from bad
  structure, they fail because nobody writes at the end.

## User -> Claude
<!-- What Claude must and must not do, in your words. These outrank CLAUDE.md §RULES. -->
- Reply in Vietnamese. Keep every memory file in English.
- Show a plan and wait for approval before creating or rewriting files.
- Do not write application code before PLAN.md is frozen.
- Surface debatable choices as multiple-choice questions, not as paragraphs of options.
