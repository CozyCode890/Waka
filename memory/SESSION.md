<!--
READ WHEN : cold start · stage kickoff. Skip it otherwise — §STATE answers "where am I".
EDIT      : yours, written at /wrap.
CAP       : 50 lines. EXACTLY two blocks, newest first, 25 lines each at most.
HARD RULE : before dropping the third block, promote anything still true into FACTS.md and
            list it under FACTS.md §Promoted. Skip that and FACTS.md stays empty while this
            file quietly becomes the real memory at 400 lines.
-->

# Sessions

## 2026-10-08 — s06 · S00 executed end to end; two bets nearly recorded wrong
- All 15 tasks done, all three exit tests pass, committed as 2301e3a. S00 is ready for /stage.
- Probe A: Mica needs TWO calls. `DwmSetWindowAttribute(DWMWA_SYSTEMBACKDROP_TYPE)` returns
  S_OK and paints almost nothing unless `DwmExtendFrameIntoClientArea` ran first at MARGINS -1.
  Run one showed a recoloured caption over an opaque body — indistinguishable from a lost bet.
- Probe B and C: the first four runs used a window parked off the desktop, to keep the screen
  free while the user studied. They reported ~48ms and 98% dropped frames and were junk. A
  control probe drawing one label costs 16.35ms visible and 31.64ms hidden: the window manager
  throttles what it does not show, and an inactive virtual desktop should behave the same.
- I also argued at length, and wrongly, that "p95 <= 16.7ms" was unreachable by construction.
  That assumed a 60Hz panel; this one is 144Hz, and the 16.00ms "baseline" was JavaFX's idle
  pulse timer, not a floor. A fast table here measures 4-9ms. Check the refresh rate first.
- Abandoned: driving sweeps with `TableView.scrollTo`, which rebuilds cells to show a row that
  may already be visible — a gesture nobody performs. Replaced with `VirtualFlow.scrollPixels`
  (wheel) and `setPosition` (thumb), and every sweep now prints how far it actually moved.
- Numbers: 100k rows meet 60fps with NO headroom (avg 17.07ms). Column ceiling is 500, not the
  50-100 FACTS guessed — corrected in place with the user's yes. Dragging the vertical scrollbar
  is a separate ceiling at 200 columns and needs throttling in S03.
- For S01: both handle routes agree, so `--add-exports` for com.sun.glass.ui is optional; weka
  drags 17 jars with illegal automatic module names that jlink will likely fight.
- NEXT: run /stage to close S00 and detail S01. Still unapproved, three sessions old and now
  binding at 108/110: §CAPS `DESIGN 110`.

## 2026-10-07 — s05 · S00 detailed, and the toolchain proved (wrapped past midnight on 10-08)
- S00 is CURRENT with 15 tasks. `spike\` is fenced off: a standalone pom OUTSIDE the Maven
  reactor that outlives the stage, so JavaFX 28 can be re-tested in 20 minutes. `core\ app\
  dist\` are S01's to create, and CURRENT.md carries that as an explicit scope fence.
- STAGES.md exit test 2 was reworded, with the user's yes. It read "opens 2,000 COLUMNS", which
  contradicts FACTS (TableView dies past 500 columns) and DESIGN (attributes are ROWS) — an exit
  test written to fail, and a failed S00 blocks all six later stages. It now matches PLAN's own
  wording and makes a MEASURED column ceiling the deliverable S03 actually needs.
- Four planning choices asked, all answered as recommended and recorded in CURRENT.md.
- T01 and T03 done, T02 half done. Temurin 25.0.4.1 in, Maven 3.10.0 from scoop, all six
  coordinates resolve including the `win`-classified JavaFX jars. Pin JavaFX as plain `27`
  (no 27.0.1 exists) and AtlantaFX `3.0.0`, the only release on its 3.x line. Central already
  lists `28-ea+11`, so PLAN's March-2027 bump and D-014's title-bar wait are on schedule.
- Two assumptions inside the APPROVED plan broke mid-run; both are D-029. winget publishes no
  `Apache.Maven` package at all. And User PATH is appended after Machine PATH, so it can never
  shadow the Machine-scope jdk-21\bin — JDK 25 is selected by a User-scope JAVA_HOME instead,
  and `java` in a bare shell still answers 21, correctly. T01's own check had to be reworded
  from `java -version` to `$env:JAVA_HOME\bin\java` or it was a permanent false failure.
- Still unapproved, two sessions old: §CAPS `DESIGN 110`. Keeping it is right — without that row
  DESIGN.md has no cap at all and it sits at 108 — but it never got its own yes.
- NEXT: S00-T04, write `spike\pom.xml`, then T02's `mvn wrapper:wrapper` inside it at once.

