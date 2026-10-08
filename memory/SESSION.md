<!--
READ WHEN : cold start · stage kickoff. Skip it otherwise — §STATE answers "where am I".
EDIT      : yours, written at /wrap.
CAP       : 50 lines. EXACTLY two blocks, newest first, 25 lines each at most.
HARD RULE : before dropping the third block, promote anything still true into FACTS.md and
            list it under FACTS.md §Promoted. Skip that and FACTS.md stays empty while this
            file quietly becomes the real memory at 400 lines.
-->

# Sessions

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

