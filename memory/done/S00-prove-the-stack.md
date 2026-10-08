<!--
READ WHEN : any request that writes or fixes code · debugging · wrap-up.
            Skip it for trivial questions, scope discussions, and "where are we".
EDIT      : yours, continuously. Tick tasks as they finish, never in a batch at the end.
CAP       : 150 lines — and it stays at 150 forever, because /stage archives it into done/.
HARD RULE : /stage archives this file to memory/done/ BEFORE regenerating it, and carries
            unanswered §Open questions forward. Overwriting it without archiving is data loss.
-->

# Current stage — S00 Prove the stack

> Detailed 2026-10-07, executed 2026-10-08. Goal: the three technical bets this stack rests on
> are confirmed on this machine before anything is built on them. STAGES.md holds the exit tests
> and they are the only definition of done; the tasks below are merely how we got there.

Scope fence: S00 writes throwaway code in `spike\`, which is NOT part of the Maven reactor.
No `core\`, no `app\`, no `dist\`, no FXML, no AtlantaFX theming, no shell layout — S01 owns all
of that. Full numbers and their caveats live in `spike\README.md`, not here.

## Tasks

### Toolchain
- [x] S00-T01 Temurin JDK 25 installed, USER-scope JAVA_HOME points at it, Machine scope stays
      on JDK 21 — D-029.
- [x] S00-T02 Maven 3.10.0 from scoop; `wrapper:wrapper -Dtype=only-script` run inside `spike\`.
      Three files, no jar, so `.gitignore`'s `*.jar` has nothing to swallow.
- [x] S00-T03 All six coordinates resolve. JavaFX GA on the 27 line is plain `27`; AtlantaFX 3.x
      contains only `3.0.0`.

### Probe A — Mica on StageStyle.UNIFIED, on the fast painter
- [x] S00-T04 `spike\pom.xml`: standalone, `release` 25, UTF-8, javafx-maven-plugin 0.0.8,
      exec-maven-plugin 3.5.1, `-Dprism.verbose=true`, one probe selected by `-Dprobe=`.
- [x] S00-T05 `MicaWindow.java`: FFM only, no JNI and no JNA. Both handle routes agree, all four
      DWM calls return S_OK. `DwmExtendFrameIntoClientArea` had to be ADDED — see §Gotchas.
- [x] S00-T06 `StackProbe.java`: one UNIFIED window, three probes behind three buttons.
      Screenshots in `spike\evidence\`, dark and light, both blur.
- [x] S00-T07 Painter proven from the log: `Not forcing UploadingPainter`,
      `com.sun.prism.d3d.D3DPipeline`, `vsync: true vpipe: true`,
      `prism.forceUploadingPainter=null`. The flag appears nowhere.
- [x] S00-T08 GPU recorded: NVIDIA GeForce RTX 4050 Laptop GPU, `nvldumdx.dll` 32.0.16.1742,
      D3D9Ex, `ven_10DE dev_28E1`, Windows build 26100, 1920x1080 @ 144Hz.

### Probe B — 100k rows at 60fps
- [x] S00-T09 `SyntheticTable.java`: in memory, no Weka, no file. Cells are references into one
      4096-string pool, so reading a cell is an array index and never a parse.
- [x] S00-T10 `FrameRateMeter.java`: AnimationTimer, min / avg / p95 plus the share of frames
      over 25ms, driven by a scripted sweep. Each sweep reports how far it actually moved.
- [x] S00-T11 100,000 x 10 with `setFixedCellSize(26)`: open 643ms, wheel avg 17.07ms
      p95 28.64ms 6.1% over 25ms; thumb drag avg 16.63ms p95 18.09ms 3.9%. See §Open questions 2.

### Probe C — 2,000 attributes, and the real column ceiling
- [x] S00-T12 Attribute view as ROWS, 2000 x 5: open 467ms, wheel avg 6.38ms p95 16.46ms.
      Comfortable — about 157fps on this panel.
- [x] S00-T13 Column sweep at 10k rows. **Ceiling is 500 columns**: both wheel gestures stay
      inside 16.7ms up to 500, 1000 is degraded but usable (opens in 4.2s), 2000 is neither
      (opens in 10.6s, 98% of frames dropped). Dragging the vertical scrollbar is a separate and
      much lower ceiling — it breaks at 200 columns.

### Side check — weka-stable resolves
- [x] S00-T14 `WekaLinkProbe.java`, headless: `iris.arff: 150 instances, 5 attributes`,
      commons-compress declared explicitly, `.arff.gz` round trip returns the same shape.

### Close
- [x] S00-T15 Nine lines added to FACTS.md and the 50-100 column guess corrected there with the
      user's yes. DECISIONS.md untouched: D-010 and D-014 both held, so there was nothing to
      supersede. Committed 2026-10-08.

## Files written
    spike\pom.xml · spike\README.md · spike\mvnw · spike\mvnw.cmd
    spike\.mvn\wrapper\maven-wrapper.properties
    spike\src\main\java\waka\spike\{StackProbe,MicaWindow,FrameRateMeter,SyntheticTable,
                                    WekaLinkProbe}.java
    spike\evidence\probe-a-mica-{dark,light}.png        committed, 242KB together
`.gitignore` was NOT touched: `target\` was already ignored and the wrapper has no jar.

## Done evidence
- D-010 holds: every pinned coordinate resolves and the stack runs. Nothing to supersede.
- D-014 holds: Mica on UNIFIED works on JavaFX 27 with the D3D painter. Nothing to supersede.
- Exit test 1 — passed. Screenshots show the body and the caption both blurring, dark and light.
- Exit test 2 — passed, with a warning the user asked to be carried: 100k rows meet 60fps with
  no headroom left. The thumb drag, the heavier gesture, is the one that clears it at 60.1fps.
- Exit test 3 — passed at 500 columns, which is the measured ceiling S03 now designs under.

## Gotchas found
- `DwmSetWindowAttribute(DWMWA_SYSTEMBACKDROP_TYPE)` returns S_OK and does almost nothing on its
  own. Without `DwmExtendFrameIntoClientArea` at margins -1, the frame covers only the caption,
  so the caption recolours and the body stays flat opaque. The first run looked exactly like a
  failed Mica and was a missing second call.
- A window parked off the desktop is throttled by the window manager. The control probe — one
  Label, new text each frame — costs 16.35ms on screen and 31.64ms off it, for identical work.
  Three runs of probe B were taken off-screen, reported ~48ms and 98% dropped, and were junk.
  A window on an inactive Windows virtual desktop is cloaked and should be expected to behave
  the same way. Measure on a visible window or do not measure.
- The idle baseline is not a floor. With nothing dirty JavaFX falls back to a 60Hz pulse timer
  and always reports about 16.00ms, while this 144Hz panel lets a fast table measure 4-9ms.
  Reading 16.7ms as "the best achievable" was wrong and cost a wrong conclusion once.
- `--enable-native-access=ALL-UNNAMED` does not cover a named module. JavaFX loads its own
  natives from `javafx.graphics`, so that module has to be listed too or the warning stays.
- `weka.core.Version.VERSION` says `3.8.8-SNAPSHOT` inside the `weka-stable:3.8.7` artifact.
- javafx-maven-plugin drops 17 of weka's transitive jars from the module path because their
  automatic module names are illegal (`netlib-native_*` contain `native`, `java-cup` has `11b`).
  Only a warning here. S01 should expect the same jars to fight jlink.
- Weka's `DataSource` keeps the file open, so deleting a temp dataset right after reading it
  loses a race on Windows. Use deleteOnExit.
- `winget` has no `Apache.Maven` package. User PATH is appended after Machine PATH, so a User
  PATH entry can never shadow a Machine-scope JDK; selecting a JDK without admin means
  JAVA_HOME, not PATH. scoop's maven creates no `mvn` shim.

## Open questions
<!-- 0. All three answered in session s06, 2026-10-08: exit test 2 passes but FACTS carries the
     "no headroom left" warning; the 50-100 column line in FACTS was corrected in place with the
     measured 500 / 1000 / 2000 numbers; the two screenshots are committed under spike\evidence\.

## Handover to S01
- The pom, the Mica call order and the measuring harness in `spike\` are the working reference.
  S01 copies the DWM call sequence from MicaWindow, it does not rediscover it.
- Three things S01 has to decide that S00 only exposed: whether the `--add-exports` for
  com.sun.glass.ui survives jpackage (both handle routes work, so dropping it costs nothing but
  a FindWindowW call); what jlink does with weka's 17 illegal module names; and whether the
  AtlantaFX token names in DESIGN §Colour match 3.0.0, which S00 deliberately did not check.
- S03 inherits two numbers it must design under: 500 columns, and a vertical scrollbar drag that
  collapses at 200. -->

