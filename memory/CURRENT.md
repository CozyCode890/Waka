<!--
READ WHEN : any request that writes or fixes code · debugging · wrap-up.
            Skip it for trivial questions, scope discussions, and "where are we".
EDIT      : yours, continuously. Tick tasks as they finish, never in a batch at the end.
CAP       : 150 lines — and it stays at 150 forever, because /stage archives it into done/.
HARD RULE : /stage archives this file to memory/done/ BEFORE regenerating it, and carries
            unanswered §Open questions forward. Overwriting it without archiving is data loss.
-->

# Current stage — S01 The shell

> Detailed 2026-10-08, after S00 closed. Goal: a VS Code-shaped window the user can drive and
> call right, with no Weka call anywhere inside it. STAGES.md holds the two exit tests and they
> are the only definition of done; the tasks below are merely the route.

Scope fence: S01 creates the real reactor — `core\ app\ dist\` — and the shell's shape. It does
NOT load a dataset, render an options form, run a scheme, or call anything in `weka.*`. `core`
declares weka-stable so packaging meets it early (D-033) and imports nothing from it. Document
tabs open and close around a placeholder. Apply/Stop and the option string are drawn and inert.
`spike\` stays frozen as the reference for the DWM call order and the measuring harness.

## Tasks

### The reactor
- [x] S01-T01 Root `pom.xml`, packaging `pom`, modules `core app dist`, groupId `waka`,
      artifactId `waka`, 0.1.0-SNAPSHOT, `release` 25, UTF-8. dependencyManagement pins
      JavaFX 27, AtlantaFX 3.0.0, Ikonli-Feather, weka-stable 3.8.7, commons-compress,
      JUnit 5, TestFX.
- [x] S01-T02 Maven wrapper at the root, `-Dtype=only-script` like `spike\` — three files, no
      jar. `.gitignore` ignores `*.jar`, so a wrapper generated any other way is dropped
      SILENTLY: the build keeps working here and breaks for whoever clones. Verify with
      `git status --ignored` afterwards, not by eye.
- [x] S01-T03 `core`: UI-free by construction — its pom simply does not declare JavaFX, so a
      `javafx.*` import cannot compile. Declares weka-stable + commons-compress (D-033).
- [x] S01-T04 `app`: JavaFX + AtlantaFX, depends on `core`. `dist`: packaging only, no sources.

Reactor built, order `waka > core > app > dist`, `--ignored` named no wrapper file. Versions FACTS
had not fixed, resolved against Central: Ikonli `12.4.0`, JUnit `5.14.4`, TestFX `4.0.18`,
commons-compress `1.28.0`; plugins compiler `3.14.0` (4.x still beta), surefire `3.6.0`, wrapper
`3.3.4`, javafx `0.0.8`. JUnit stays on 5 though Central is at 6.1.3 — asked, answered, reason in
the root pom. `org.osgi.core` excluded from testfx-core. Two findings promoted to FACTS.

### The window and the material
- [x] S01-T05 `app` main class: one `StageStyle.UNIFIED` stage. DWM sequence lifted from
      `spike\MicaWindow.java` — `DwmExtendFrameIntoClientArea` at MARGINS -1 FIRST, then
      `DWMWA_SYSTEMBACKDROP_TYPE` (FACTS). Handle via `FindWindowW`, no `--add-exports` (D-034).
- [x] S01-T06 Caption recoloured per theme through `DWMWA_CAPTION_COLOR` (D-014).
- [x] S01-T07 Theme service: auto / light / dark. Auto reads `AppsUseLightTheme` under
      `HKCU:\...\Themes\Personalize` at start and re-reads on window focus — no native hook.
- [x] S01-T08 Mica on/off. OFF swaps to the solid Layer fills in DESIGN §Colour (G-016); it does
      not merely drop the blur. This is half of exit test 2.
- [x] S01-T09 Read AtlantaFX 3.0.0's real token names and correct DESIGN §Colour's guess column
      in place — DESIGN's own note pre-authorises exactly this edit and nothing more.
- [x] S01-T10 `waka.css` over AtlantaFX: every §Geometry and §Colour token as a CSS variable,
      in four combinations — light/dark × Mica on/off. No webfont (§Type).

T09: all 13 guessed names were RIGHT; §Colour's caveat is now a verification, `--cls1..3` maps to
`-color-chart-1..3`, two §Ledger rows record it, base theme Primer.
T10 asks for two things JavaFX cannot do (see FACTS), both now settled: §Geometry is
`waka.app.Geometry` in Java rather than CSS, and §Type's `.06em` panel-header tracking is DROPPED
— shown to the user at size and at 4x, dropped on their call, superseded in §Ledger with the
reason. §Colour is complete and all four combinations are proven by test.

### The hidden-test harness — build it before the layout, not after
- [x] S01-T11 JUnit 5 + TestFX. The stage is created at -4000,-4000 and is never visible
      (D-030). `-Dwaka.visible=true` brings it on screen and is passed ONLY after asking.
- [x] S01-T12 One smoke test that fails loudly if the harness ever starts a visible window, so
      the rule is enforced by the build and not by memory.

8 tests green, nothing on screen. `OffScreenApplicationTest` launches the REAL `WakaApplication`,
so the tests drive the production startup path rather than one written for tests. The TestFX trap
it hit is now a FACTS line. T12 checks every window in `Window.getWindows()`, not only Waka's —
because the first thing it caught was TestFX's own visible stage — and a third test proves the
guard answers YES for a window at the screen's origin, since a guard that has only ever reported
"nothing to see" has not been shown to work.
Proven by test, not by eye: all four DWM calls return S_OK on an off-screen UNIFIED stage; the
four combinations each reach both the stylesheet and the native window; `RegGetValueW` through
FFM agrees with `reg.exe` on `AppsUseLightTheme`; and `ColourTokenTest` reads the computed fill
off a real node, so §Colour is proven to ARRIVE and not merely to be written down.
That third test earned itself immediately. The first `waka.css` looked correct and was inert:
`stage.show()` is when JavaFX first applies a scene stylesheet, and the root did not carry
`theme-dark` yet, so no `-waka-*` token was defined and every rule using one died with "String
cannot be cast to Paint" — logged as a warning, swallowed, window painted unstyled. The style
classes now go on BEFORE `show()`; `applyStylesheet` and `applyWindowMaterial` are split because
only the second needs a window handle. Promote this to FACTS at /wrap: FACTS is at 90/90 and has
no room today. Remaining build noise, benign: JavaFX warns that it was loaded from an unnamed
module, which is what provided scope on the test classpath means; T27's jlink image fixes it.
NOT proven: that any of it LOOKS right. That needs the screen, and it is T29's job.

### The layout
- [ ] S01-T13 Shell skeleton: native caption 32px (not ours), rail 48px on the RIGHT, side panel
      260px, status bar 22px, document area centre. Rail side swappable by setting (D-013).
- [ ] S01-T14 Rail: Datasets · Search · Runs · Packages, Settings pinned bottom, 20px icon
      centred, active mode a 2px accent bar on the INNER edge (G-002, G-003).
- [ ] S01-T15 Document tabs 35px: 2px accent on the TOP edge, `--card` fill when active, unsaved
      dot becoming the close X on hover (G-005, G-006). Holds a placeholder in S01.
- [ ] S01-T16 Segmented bar 32px, the six Explorer words in full, never shortened, scrolls
      horizontally when narrow (G-007).
- [ ] S01-T17 Toolbar 40px / 28px controls: the Apply–Stop state-swapped pair (G-013) and the
      always-visible editable mono option string (G-008). Both inert until S02.
- [ ] S01-T18 Bottom panel 224px incl. its 34px tab strip, four providers — Weka Output ·
      Terminal · Package Log · Problems (D-017, D-022). Output is collapsible run blocks (D-028).
- [ ] S01-T19 Status bar 22px tinted `--accent-sub`: left what the app is doing, right the
      dataset plus a heap meter. The heap meter is real in S01 (G-012).
- [ ] S01-T20 Undo toast bottom right above the status bar, auto-dismissing (G-015). Nothing in
      this app asks "are you sure?".

### Driving it
- [ ] S01-T21 Command registry: every action is a named command with an id and a language-pack
      label. The palette and the keymap both resolve through it, or they will drift.
- [ ] S01-T22 Command palette 600px, 76px from top, fuzzy filter, each row showing its current
      binding (G-014).
- [ ] S01-T23 Keymap: defaults in a resource file, a user file at `%APPDATA%\Waka\keymap.json`
      layered over it that can REMOVE a default binding with explicit syntax, not an empty
      string (D-032). This is the half of exit test 1 most likely to be faked.
- [ ] S01-T24 Settings store under `%APPDATA%\Waka\` for theme, accent, rail side, Mica, panel
      heights. Not a hand-editable `settings.json` — PLAN keeps that under "Later" (D-032).
- [ ] S01-T25 Settings screen behind the rail's bottom pin: theme, accent, rail side, Mica. A
      hosted panel with close · pop out · open as tab on its header, never a dialog (D-027).

### Language pack
- [ ] S01-T26 One English `ResourceBundle`, every visible string through it from the first
      screen (D-024). Test asserts no missing and no duplicate key; it cannot catch a string
      hard-coded in Java, so that stays a review habit — say so rather than claiming coverage.

### Packaging
- [ ] S01-T27 `dist`: jlink image of JDK + `javafx.*` only, `javafx.media` included for S05's
      About (D-026). App and weka jars ride the classpath (D-016).
- [ ] S01-T28 `jpackage --type app-image`, then launch it by double-click outside the IDE.
      Record whether weka's 17 illegal automatic module names bite here — D-033 predicts not.
- [ ] S01-T29 Grade "looks finished with Mica off" on the packaged app. **Needs a visible
      window: ask the user before running it, and hand focus back after** (D-030).

### Close
- [ ] S01-T30 SOURCE.md gets its first real read-depth rows, now that `src\` exists.
- [ ] S01-T31 Fence check: `Select-String "weka\." src\` returns nothing outside `core`'s pom.
- [ ] S01-T32 Promote facts, append decisions, commit.

## Files to touch
Written so far: the root pom, the wrapper's three files, all three module poms,
`core\...\package-info.java`, and in `app` — `WakaApplication` `MicaWindow` `ThemeService`
`WindowsApi` `Geometry` + `waka.css` + the two test classes and their `OffScreenApplicationTest`.
Two files the plan below did not name: `WindowsApi` (the FFM linking, which MicaWindow and
ThemeService would otherwise each copy) and `Geometry` (because §Geometry cannot be CSS).

    pom.xml · mvnw · mvnw.cmd · .mvn\wrapper\maven-wrapper.properties
    core\pom.xml  core\src\main\java\waka\core\{settings,keymap,commands,text}\*.java
    app\pom.xml   app\src\main\java\waka\app\{WakaApplication,MicaWindow,ThemeService,
                                              WindowsApi,Geometry}.java
                  app\src\main\java\waka\app\shell\{Rail,SidePanel,DocumentTabs,SegmentedBar,
                                                    Toolbar,BottomPanel,StatusBar,UndoToast}.java
                  app\src\main\java\waka\app\palette\CommandPalette.java
                  app\src\main\resources\waka\app\{waka.css,strings.properties,keymap-default.json}
                  app\src\test\java\waka\app\...  (TestFX, off-screen)
    dist\pom.xml · dist\jpackage.ps1
Also edited: memory\DESIGN.md §Colour (the AtlantaFX column only), memory\SOURCE.md.

## Open questions
None open.

Answered 2026-10-08, this session: JUnit stays on 5.14.4 rather than 6.1.3, because TestFX 4.0.18
predates JUnit 6 and the harness it underpins is T11/T12's own deliverable. §Type's panel-header
tracking is dropped (§Ledger `type`). ThemeService reads the registry through FFM `RegGetValueW`,
in the same style as the DWM calls, rather than spawning `reg query` on every window focus.

Answered in s07: the default accent stays DESIGN's locked `#4CC2FF` dark / `#005FB8` light,
because the Windows accent often fails contrast against `--card` and would hand the OS control of
whether this app is readable; and icons are Ikonli + Feather (D-035).
