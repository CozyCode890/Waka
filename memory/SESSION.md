<!--
READ WHEN : cold start · stage kickoff. Skip it otherwise — §STATE answers "where am I".
EDIT      : yours, written at /wrap.
CAP       : 50 lines. EXACTLY two blocks, newest first, 25 lines each at most.
HARD RULE : before dropping the third block, promote anything still true into FACTS.md and
            list it under FACTS.md §Promoted. Skip that and FACTS.md stays empty while this
            file quietly becomes the real memory at 400 lines.
-->

# Sessions

## 2026-10-08 — s09 · S01-T01..T12: the reactor, the Mica window, the hidden-test harness
- Three commits, 11 tests green, nothing ever on screen. Reactor is `core > app > dist`; `core`
  declares no JavaFX, so `import javafx.*` there fails to compile rather than failing review.
- Versions written once, in the root pom. JUnit stays 5.14.4 though Central is at 6.1.3: TestFX
  4.0.18 predates JUnit 6 and declares no Jupiter at all, and the harness it underpins is
  T11/T12's own deliverable — build the detector before changing what it detects.
- One UNIFIED stage, Mica via `FindWindowW` only (`--add-exports` gone, D-034). Theme reads
  `AppsUseLightTheme` through FFM `RegGetValueW`, cross-checked in a test against `reg.exe`.
- T09: all thirteen AtlantaFX token names DESIGN.md guessed were RIGHT; the caveat is now a check.
- T10 hit two JavaFX walls, both now FACTS lines: §Geometry became `waka.app.Geometry` in Java,
  and §Type's `.06em` tracking is DROPPED — user shown both renderings at 11px and 4x (§Ledger).
- Two defects earned their tests: `waka.css` was inert because the root carried no theme class
  when `show()` first applied it; and `FxToolkit.registerPrimaryStage()` shows a stage of its own,
  which broke `initStyle(UNIFIED)` and was itself ON SCREEN. T12 now checks every window.
- Trap: this session's first read of CURRENT.md was a snapshot at 8c730b3, four commits stale.
- NEXT: S01-T13 shell skeleton — caption 32 · rail 48 RIGHT · side 260 · status 22 · doc centre.

## 2026-10-08 — s08 · Repo made public: README, LICENSE, eol, and a lock on README
- S01 untouched at the user's call, still 0/32. This session only made the repo publishable.
- `origin` = git@github.com:CozyCode890/Waka.git, `main` pushed, repo confirmed PUBLIC by `gh`.
  memory\ is public by the user's explicit intent, not by oversight.
- README.md is Vietnamese while the rest of the repo stays English (D-036) — prose carries no
  identifier for a later grep, which is all D-003 and D-023 were protecting.
- Its opening says plainly that no app exists yet and only spike\ runs. That is the line most
  likely to go stale: it must come out the session S01 produces a launchable image.
- LICENSE is verbatim GPL-3.0 copied from `C:\Program Files\Weka-3-9-6\COPYING` and hash-matched
  against it — nothing downloaded, no clause written from memory.
- No ROUTER row could write a README: it needs PLAN + STAGES + a file under done\ at once, and
  every row banned done\. Hence the `docs` row, and a §AUTHORITY row for README.md instead of a
  §RULES rule (which costs deleting one). Every change asks first, no "stage closed" exemption (D-037).
- .gitignore already kept memory\ and ignored .vscode\, so nothing was rewritten. Of five added
  rules the one that matters is `assets/audio/*`: D-026's mp3 would otherwise be staged, and a
  copyrighted master inside a public GPLv3 repo poisons the whole distribution.
- .gitattributes pins eol — text LF, `*.cmd`/`*.bat` CRLF. `renormalize` changed zero bytes, so
  it was free now and a whole-codebase diff after S01. I first blamed "the index stores LF"; git
  always does, and the defect was checkout depending on each clone's core.autocrlf.
- §CAPS gained `README 200`; the count command had to gain README.md too, or the number counts
  nothing. FACTS lost its one occurrence of the Windows account name.
- NEXT: S01-T01 — root `pom.xml`, modules `core app dist`, release 25, versions pinned once.
