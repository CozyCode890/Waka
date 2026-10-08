<!--
READ WHEN : cold start · stage kickoff. Skip it otherwise — §STATE answers "where am I".
EDIT      : yours, written at /wrap.
CAP       : 50 lines. EXACTLY two blocks, newest first, 25 lines each at most.
HARD RULE : before dropping the third block, promote anything still true into FACTS.md and
            list it under FACTS.md §Promoted. Skip that and FACTS.md stays empty while this
            file quietly becomes the real memory at 400 lines.
-->

# Sessions

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

## 2026-10-08 — s07 · S00 closed, S01 detailed, and a standing rule about the screen
- S00 archived unchanged to `memory\done\S00-prove-the-stack.md` via `git mv`; its exit tests
  were re-checked against the artifacts, not the README. STAGES flipped, CURRENT.md = S01, 32 tasks.
- New standing constraint from the user: GUI tests run hidden, and putting a window on screen
  needs their permission asked each time. Written in three places on purpose — D-030, a
  `[pref]` line in FACTS, and S01-T12, a smoke test that fails the build if the harness ever
  opens a visible window. A rule that lives only in a session's memory is not a rule.
- It carries a known conflict: a hidden window is throttled (16.35ms visible vs 31.64ms hidden,
  identical work), so S03's performance numbers cannot be taken hidden. S01-T29 and every S03
  measurement must stop and ask for the screen instead of quietly measuring junk.
- Six choices asked, all answered as recommended → D-030..D-035: TestFX on a real stage at
  -4000,-4000 (`openjfx-monocle`'s newest release targets JDK 12, so true headless is out on
  JavaFX 27); pure Java and no FXML, since D-027's reparenting needs Java regardless; keymap
  file plus an internal store, `settings.json` staying in PLAN's "Later"; accent; Ikonli.
- Two stale approvals landed: §CAPS `DESIGN 110` → 140, and FACTS §Promoted consolidated from
  19 lines to 5, which is what made room for the new facts. FACTS went 89/90 → 84/90.
- Self-decided, open to objection: `core` declares weka-stable from S01 so packaging meets the
  17 illegal module names early (D-033); groupId `waka` needs `io.github.<user>` before publish.
- NEXT: S01-T01 — root `pom.xml`, modules `core app dist`, release 25, versions pinned once.
