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
[pref] Every option list must say which option is recommended and why — a bare list is not an answer — 2026-10-06
[pref] Explain concretely: name the file, say what breaks. Abstract process talk gets rejected outright — 2026-10-06
[proj] No Weka GUI source exists yet — 2026-10-06
[proj] PLAN.md was frozen 2026-10-06; stack is JDK 25 + JavaFX 27 + AtlantaFX 3.0.0 + weka-stable 3.8.7, GPLv3 — 2026-10-06
[env] Temurin JDK 21.0.12 is installed and JAVA_HOME points at it; the project targets JDK 25, which is not installed yet — 2026-10-06
[env] This machine reports ProductName "Windows 10 IoT Enterprise LTSC 2024" but CurrentBuildNumber 26100, i.e. Windows 11 24H2 — 2026-10-06
[env] Weka 3-9-6 is installed at C:\Program Files\Weka-3-9-6; %USERPROFILE%\wekafiles holds repCache plus packages chiSquaredAttributeEval and userClassifier — 2026-10-06
[env] No Maven or Gradle on PATH (use the wrapper); winget and scoop are available; Inno Setup is not installed — 2026-10-06
[proj] Design reference is D:\Guzz — PySide6 + qfluentwidgets 1.11.3; its visual tokens live in the library's 68 compiled QSS resources, not in Guzz's own code, which holds only 3 colour constants — 2026-10-06
[proj] Fluent geometry baseline from the reference: nav rail 48px collapsed / 322px expanded, title bar 48px, workhorse corner radius 5px, content sheet radius 10px on the top-left corner only, selection indicator a 3x16 bar at radius 1.5 — 2026-10-06
[proj] Fluent surface alphas over Mica: content sheet rgba(255,255,255,0.5) light and 0.0314 dark, cards alpha 170 and 13, controls 0.7 and 0.0605 — nothing above the window is opaque — 2026-10-06
[gotcha] WekaPackageManager.loadPackages(boolean) calls refreshGOEProperties(), which boots KnowledgeFlowApp and Swing file choosers inside our process — always call loadPackages(false, false, false) — 2026-10-06
[gotcha] StageStyle.TRANSPARENT on Windows forces JavaFX's UploadingPainter unconditionally via WindowStage.needsUpdateWindow(), costing a full-frame GPU readback every frame — use StageStyle.UNIFIED for Mica — 2026-10-06
[gotcha] -Dprism.forceUploadingPainter caps JavaFX at 60fps and is not needed from JavaFX 27, where the D3DResourceManager BackBufferFormat fix landed — 2026-10-06
[gotcha] JavaFX TableView horizontal virtualization only engages when setFixedCellSize is set; measured 2026-10-08 at 10k rows it is fine to 500 columns, degraded at 1000 (opens in 4.2s), unusable at 2000 (opens in 10.6s) — 2026-10-08 supersedes the "50-100 columns" guess of 2026-10-06
[gotcha] Weka package classes are invisible to Class.forName — use WekaPackageClassLoaderManager.forName and SerializationHelper.getObjectInputStream for models built with package schemes — 2026-10-06
[gotcha] weka.Run prints a numbered menu and blocks on stdin for an ambiguous scheme name — always pass the fully-qualified class name from our side — 2026-10-06
[proj] weka-stable marks commons-compress optional so it is not inherited transitively; declare it explicitly or .arff.gz and .arff.bz2 loading fails at runtime — 2026-10-06
[proj] weka-stable 3.8.7 already depends on com.formdev:flatlaf:3.7.1 at runtime, so "Weka's GUI looks old" is no longer an accurate pitch — 2026-10-06
[pref] Abstract UI vocabulary does not land — "accent", "toast", "option string", "indicator edge" each had to be redrawn as an ASCII diagram before the user could answer. Draw the thing, then ask — 2026-10-07
[proj] UI is graded before it is built: mockups in design\*.html, verdicts frozen in memory\DESIGN.md. W1 (shell) closed 2026-10-07; W2 is the options dialog at S02 kickoff — 2026-10-07
[gotcha] Nothing validates a memory write — the SessionStart hook only prints §STATE. Two parallel sessions can both rewrite §STATE, and /wrap step 1 (`git status --short memory/`) is the only detector — 2026-10-06
[gotcha] A session that dies without /wrap loses its SESSION.md block entirely; only what it wrote to PLAN/DECISIONS/FACTS as it went survives — tick CURRENT.md continuously, never in a batch at the end — 2026-10-06
[proj] memory\SOURCE.md is almost empty on purpose — it is a guess until src\ exists, and CLAUDE.md §RULES 8 folds it into CURRENT.md if no row ever reads it for real — 2026-10-06
[proj] App name is Waka — display name `Waka`, Maven artifactId `waka`, install dir `Waka`, GitHub repo `Waka`; the working copy stays D:\Weka-GUI (D-025) — 2026-10-07
[pref] All code is English — identifiers, comments, file and directory names, assets, config keys, console output — in plain words a student knows, no clipped abbreviations (D-023) — 2026-10-07
[pref] Code is written to be re-read, checked and inherited: where two forms do the same job, the clearer one wins over the shorter one (D-023) — 2026-10-07
[proj] Every UI string goes through a language pack from the first screen; English is the only pack in v1, Vietnamese is a separate later project (D-024) — 2026-10-07
[proj] No modal dialogs — the options editor and decision-tree views are reparentable hosted panels with close / pop-out / open-as-tab on their header (D-027) — 2026-10-07
[proj] The About screen carries the Weka attribution and the GPLv3 notice, so it is a licence obligation and not decoration (D-026) — 2026-10-07
[proj] The About music player needs javafx.media in the jlink image; audio is never committed — assets\audio\ ships empty with a README (D-026) — 2026-10-07
[proj] S00 is a risk spike the user added ahead of the shell; folding its three checks into S01's exit tests was offered and refused — they must be proven before anything rests on them — 2026-10-06
[proj] The jpackage app-image rehearsal sits in S01, not S06, so "it only runs from the IDE" is never true for long — 2026-10-06
[proj] S03 is deliberately not split despite carrying four proofs — one vertical slice must prove the engine split, run model, cancel and history at once — 2026-10-06
[env] Temurin JDK 25.0.4.1 lives at C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot and USER-scope JAVA_HOME points there; Machine JAVA_HOME and Machine PATH still point at jdk-21.0.12.101 (D-029) — 2026-10-07
[env] Maven 3.10.0 came from scoop and lives at C:\Users\LETHAIDUCTUNG\scoop\apps\maven\current, whose bin is on User PATH; there is no mvn shim under scoop\shims — 2026-10-07
[gotcha] winget publishes no `Apache.Maven` package, and a User PATH entry is appended AFTER Machine PATH so it can never shadow a Machine-scope JDK — selecting a JDK without admin means JAVA_HOME, not PATH — 2026-10-07
[proj] JavaFX GA on the 27 line is the plain version string `27` (no 27.0.1 exists); AtlantaFX 3.x contains only 3.0.0; the `win`-classified jars needed at runtime resolve for javafx-base, -controls, -graphics and -media at 27 — 2026-10-07
[env] Display is 1920x1080 at 144Hz on an NVIDIA GeForce RTX 4050 Laptop GPU, driver nvldumdx.dll 32.0.16.1742, running JavaFX through D3D9Ex — 2026-10-08
[proj] Mica on StageStyle.UNIFIED is confirmed working on JavaFX 27 with com.sun.prism.d3d.D3DPipeline, so D-014 holds and no fallback is needed — 2026-10-08
[gotcha] DwmSetWindowAttribute(DWMWA_SYSTEMBACKDROP_TYPE) returns S_OK and still shows no Mica unless DwmExtendFrameIntoClientArea is called first with every MARGINS field at -1; without it only the caption recolours — 2026-10-08
[gotcha] A JavaFX window parked off the desktop is throttled by the window manager — identical work costs 16.35ms visible and 31.64ms hidden, so hidden-window frame times are junk; expect the same on an inactive Windows virtual desktop, which the shell cloaks — 2026-10-08
[gotcha] With nothing dirty JavaFX falls back to a 60Hz pulse timer, so an idle AnimationTimer baseline always reads ~16.00ms; that is not a floor, and on this 144Hz panel a fast TableView measures 4-9ms — 2026-10-08
[proj] 100,000 rows by 10 columns scrolls at avg 17.07ms (58.6fps), p95 28.64ms: it meets 60fps with no headroom left, so S03 must not add per-frame work to the row path — 2026-10-08
[gotcha] Dragging a TableView's vertical scrollbar rebuilds every visible cell in every visible column, so it collapses at 200 columns — far below the 500-column scrolling ceiling. Throttle the drag, do not just cap the columns — 2026-10-08
[gotcha] weka-stable drags 17 transitive jars whose automatic module names are illegal (netlib-native_* contain `native`, java-cup contains `11b`); javafx-maven-plugin drops them from the module path with a warning, and S01's jlink should expect the same fight — 2026-10-08
[gotcha] --enable-native-access=ALL-UNNAMED does not cover a named module; javafx.graphics loads its own natives and has to be listed by name as well — 2026-10-08

## §Promoted
2026-10-07 — promoted from s01 before dropping it: the three "known holes" lines became the two
`[gotcha]` lines and the `[proj]` SOURCE.md line above. Everything else in that block (the
parallel-design method, the critic's cut list, the eight settled questions) is already in
DECISIONS.md D-001..D-008 and was not duplicated.
2026-10-07 — promoted from s02 before dropping it: the three `[proj]` lines about why S00 is a
spike, why the jpackage rehearsal sits in S01, and why S03 stays unsplit. The rest of that block
was bookkeeping (a commit hash, a FACTS line already fixed) or already in DECISIONS.md D-020.
2026-10-08 — dropped s03 with nothing new promoted, because every still-true line in it is
already recorded elsewhere: the mockup-grading method and the rejected JavaFX prototype are
D-021, Terminal as a fourth provider is D-022, the four changed marks G-002 / G-010 / G-013 /
G-016 are in DESIGN.md §Ledger, the W1 artifact URL is DESIGN.md §Mockups, and "draw first, then
ask" is the `[pref]` line above. The one unsettled item in that block — the unapproved §CAPS
`DESIGN 110` — is an open approval and not a fact, so it moved into the s05 SESSION block.
<!-- /wrap lists here which SESSION.md lines became facts, so nothing is dropped silently.
     Shape: 2026-10-06 — promoted from s01: [env] ..., [proj] ... -->
