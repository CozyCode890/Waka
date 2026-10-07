<!--
READ WHEN : any request that writes or fixes code · debugging · wrap-up.
            Skip it for trivial questions, scope discussions, and "where are we".
EDIT      : yours, continuously. Tick tasks as they finish, never in a batch at the end.
CAP       : 150 lines — and it stays at 150 forever, because /stage archives it into done/.
HARD RULE : /stage archives this file to memory/done/ BEFORE regenerating it, and carries
            unanswered §Open questions forward. Overwriting it without archiving is data loss.
-->

# Current stage — S00 Prove the stack

> Detailed 2026-10-07. Goal: the three technical bets this stack rests on are confirmed on this
> machine before anything is built on them. STAGES.md holds the exit tests and they are the only
> definition of done; the tasks below are merely how we get there, and may change.

Scope fence: S00 writes throwaway code in `spike\`, which is NOT part of the Maven reactor.
No `core\`, no `app\`, no `dist\`, no FXML, no AtlantaFX theming, no shell layout — S01 owns all
of that. If a task below starts to look like the real app, it is the wrong task.

## Tasks

### Toolchain
- [x] S00-T01 Install Temurin JDK 25 (`winget install EclipseAdoptium.Temurin.25.JDK`), point a
      USER-scope JAVA_HOME at it, confirm `$env:JAVA_HOME\bin\java` and its `javac` both say 25.
      Machine JAVA_HOME and Machine PATH deliberately stay on JDK 21 — D-029.
- [ ] S00-T02 Install Maven (`scoop install maven` — winget publishes no such package), then run
      `mvn wrapper:wrapper` inside `spike\` once its pom exists and commit the wrapper. The
      reactor root has no pom until S01, so the wrapper lands in `spike\` now and S01 generates
      the root one. INSTALL HALF DONE 2026-10-07; the wrapper half waits on T04.
- [x] S00-T03 Confirm the three coordinates actually publish, before writing a pom around them:
      `org.openjfx:javafx-{controls,graphics,media}:27`, `io.github.mkpaz:atlantafx-base:3.0.0`.
      Record the exact resolved version strings. If one does not exist, STOP and report — PLAN
      pins these versions and quietly substituting another would void D-010.

### Probe A — Mica on StageStyle.UNIFIED, on the fast painter
- [ ] S00-T04 `spike\pom.xml`: standalone pom, `release` 25, UTF-8, javafx-maven-plugin, one
      main class per probe, `-Dprism.verbose=true` in the plugin's `<options>`. AtlantaFX is
      declared only to prove it resolves and applies a stylesheet; confirming its token names is
      S01's job, not ours (DESIGN §Colour).
- [ ] S00-T05 `MicaWindow.java`: reach the native HWND and call `DwmSetWindowAttribute` through
      the FFM API (`java.lang.foreign` — no JNI, no JNA). DWMWA_SYSTEMBACKDROP_TYPE=38 set to 2
      (Mica), DWMWA_USE_IMMERSIVE_DARK_MODE=20, DWMWA_CAPTION_COLOR=35 per D-014. HWND route:
      `com.sun.glass.ui.Window.getNativeWindow()` behind
      `--add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED`, falling back to `FindWindowW`
      on a unique window title if that export turns out to cost anything in S01's jpackage.
- [ ] S00-T06 `StackProbe.java`: one `StageStyle.UNIFIED` window, no layout work, the three
      probes behind three buttons. Look at it over a bright wallpaper — the backdrop must blur.
- [ ] S00-T07 Prove the painter from the `prism.verbose` log: it must name the presenting
      painter, never `UploadingPainter`, and `prism.forceUploadingPainter` must appear nowhere in
      the pom, the launcher or the environment (FACTS: that flag also caps us at 60fps).
- [ ] S00-T08 Record the GPU and driver from the same log. FACTS says Mica-on-UNIFIED is inferred
      from a three-week-old fix and unconfirmed on NVIDIA/Intel/AMD — one machine is one data
      point, and the next person needs to know which one it was.

### Probe B — 100k rows at 60fps
- [ ] S00-T09 `SyntheticTable.java`: generate rows in memory, no Weka and no file on disk, so a
      slow ARFF parser can never be mistaken for a slow table.
- [ ] S00-T10 `FrameRateMeter.java`: an `AnimationTimer` printing min / avg / p95 frame time,
      driven by a scripted scroll sweep top → bottom → top, not by hand. 60fps is a number that
      goes into §Done evidence, not an impression.
- [ ] S00-T11 100,000 rows × ~10 columns with `setFixedCellSize` set — FACTS says horizontal
      virtualization only engages when it is. Record the three frame-time numbers.

### Probe C — 2,000 attributes, and the real column ceiling
- [ ] S00-T12 Attribute view in the shape the app actually ships (DESIGN: attributes are ROWS):
      2,000 rows of name / type / missing / distinct. Must open at once and scroll like Probe B.
- [ ] S00-T13 Column sweep, because a data sheet has no choice but one column per attribute:
      50 / 100 / 200 / 500 / 1000 / 2000 columns × 10k rows, open time and scroll FPS for each.
      The output is one number — the ceiling where it stops being usable — and S03 then designs
      the data sheet under a measured number instead of under FACTS' warning.

### Side check — weka-stable resolves (a task, deliberately not an exit test)
- [ ] S00-T14 `WekaLinkProbe.java`, headless, no UI: `weka-stable:3.8.7` plus an explicitly
      declared `commons-compress` resolve, and `new Instances(reader)` on
      `C:\Program Files\Weka-3-9-6\data\iris.arff` prints instance and attribute counts. FACTS
      says Weka marks commons-compress optional; if that bites, it bites here and not in S02
      where the options editor is already leaning on `OptionHandler`.

### Close
- [ ] S00-T15 Write the measured numbers into FACTS.md, supersede D-010 or D-014 in DECISIONS.md
      if a bet lost, commit, report. A probe that failed is a result, not a blocked stage.

## Files to touch
Every path below is new. The tree is `.claude\ design\ memory\ .gitignore CLAUDE.md` and nothing
else, confirmed against disk 2026-10-07.

    spike\pom.xml
    spike\mvnw · spike\mvnw.cmd · spike\.mvn\wrapper\maven-wrapper.properties   generated, T02
    spike\README.md                     why this directory outlives S00: re-run it for JavaFX 28
    spike\src\main\java\waka\spike\StackProbe.java       window + probe launcher
    spike\src\main\java\waka\spike\MicaWindow.java       FFM → DwmSetWindowAttribute
    spike\src\main\java\waka\spike\FrameRateMeter.java   AnimationTimer, min/avg/p95
    spike\src\main\java\waka\spike\SyntheticTable.java   in-memory row and column generators
    spike\src\main\java\waka\spike\WekaLinkProbe.java    headless, no UI
    .gitignore                          add `target\` if it is not already ignored

## Verify plan
- `java -version` and `javac -version` both print 25 — T01.
- `cd spike; .\mvnw -q javafx:run` — read the painter line and the GPU line out of the
  `prism.verbose` output — T07, T08.
- Eyes on the window over a bright wallpaper: body and caption must blur, caption colour must
  match the theme — T05, T06. A screenshot goes into §Done evidence.
- Console prints `rows=100000 min=.. avg=.. p95=..`, all under 16.7ms — T11.
- Console prints one line per column count and the sweep names the ceiling — T13.
- `.\mvnw -q exec:java -Dexec.mainClass=waka.spike.WekaLinkProbe` prints
  `iris.arff: 150 instances, 5 attributes` — T14.

## Done evidence
- S00-T01 — `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot`; `java -version` there
  prints `openjdk version "25.0.4.1" 2026-08-18 LTS`, `javac -version` prints `javac 25.0.4.1`.
- S00-T02 install half — `Apache Maven 3.10.0`, Maven home
  `C:\Users\LETHAIDUCTUNG\scoop\apps\maven\current`, reporting `Java version: 25.0.4.1`, so
  Maven already picks the right JDK through JAVA_HOME with no toolchains file.
- S00-T03 — all six coordinates resolve from Maven Central via `mvn dependency:get`:
  `javafx-{controls,graphics,media}:27`, `atlantafx-base:3.0.0`, `weka-stable:3.8.7`,
  `commons-compress:1.28.0`. The `win`-classified jars also resolve for javafx-base, -controls,
  -graphics and -media at 27, which is the form the runtime actually needs.
- S00-T03 — exact version strings to write into the pom: JavaFX is plain **`27`** (the 27 line
  has a GA and 29 `27-ea+N` builds, and no `27.0.1` exists yet); AtlantaFX 3.x contains **only**
  `3.0.0`. Central's `latest` for javafx-controls is already `28-ea+11`, so PLAN's March-2027
  bump to 28 is on schedule and D-014's wait for a custom header is live, not hypothetical.

## Gotchas found
- `winget` has no `Apache.Maven` package at all — searching `maven` returns M2 Repo Cleaner and
  four Minecraft launchers. Any future "install Maven" instruction must say scoop, not winget.
- User PATH is appended AFTER Machine PATH, so a User PATH entry can never shadow a Machine-scope
  JDK already on PATH. `java` in a bare shell answers 21 on this machine and that is expected;
  anything that must run on 25 goes through `$env:JAVA_HOME\bin` or through Maven.
- scoop's maven package adds `~\scoop\apps\maven\current\bin` straight to User PATH and creates
  no `mvn` shim, so `scoop\shims\mvn.cmd` does not exist — do not look for it there.

## Open questions
<!-- 0. The four S00 planning choices were answered in session s05, 2026-10-07: STAGES exit
     test 2 reworded to PLAN's own wording plus a measured column ceiling; `spike\` sits outside
     the reactor and survives the stage; the weka-stable resolve is a task and not an exit test;
     winget for both JDK 25 and Maven, JAVA_HOME moves to 25, the wrapper is committed. -->
