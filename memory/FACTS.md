<!--
READ WHEN : any coding request · debugging · stage kickoff · refactor.
EDIT      : adding a line is yours. Editing or deleting an existing line needs the user's yes.
CAP       : 90 lines. Over cap: propose which lines to drop, do not drop them yourself.
HARD RULE : one durable fact per LINE. If it will be false next month it is not a fact —
            it belongs in CURRENT.md.
-->

# Facts

Format: `[tag] fact — yyyy-mm-dd`

| tag | holds |
|---|---|
| `[env]` | machine, toolchain, paths — things about where this is built |
| `[pref]` | how the user wants to be worked with |
| `[proj]` | durable truth about this codebase |
| `[gotcha]` | a trap that already cost time once |

Every line about code must contain the English identifier it is about — the class, method or
file name — so a later session can grep for that name and actually find this line.

[env] Windows 11 IoT Enterprise LTSC, PowerShell 7 (pwsh), no WSL — 2026-10-06
[env] Project root is D:\Weka-GUI, git-tracked, initialised 2026-10-06 — 2026-10-06
[pref] User writes Vietnamese and expects Vietnamese replies; every memory file stays in English — 2026-10-06
[pref] Show a plan and wait for approval before creating or rewriting files — 2026-10-06
[pref] Surface debatable design choices as multiple-choice questions, not prose — 2026-10-06
[proj] No Weka GUI source exists yet; PLAN.md is still empty — 2026-10-06

## §Promoted
<!-- /wrap lists here which SESSION.md lines became facts, so nothing is dropped silently.
     Shape: 2026-10-06 — promoted from s01: [env] ..., [proj] ... -->
