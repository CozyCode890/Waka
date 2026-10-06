<!--
READ WHEN : architecture question · refactor · unplanned feature · stage kickoff.
EDIT      : appending is yours. Never edit or delete an existing line.
CAP       : 80 lines. Over cap: move superseded lines to the bottom under §Retired.
HARD RULE : append-only. Changed your mind? Add a new line ending in `supersedes D-NNN`.
            A rewritten decision log is unrecoverable, and re-litigating a settled decision
            every few sessions is exactly what this file exists to stop.
-->

# Decisions

Format: `D-NNN | yyyy-mm-dd | decision | why | rejected`

D-001 | 2026-10-06 | Project memory lives in `D:\Weka-GUI\memory\`, tracked by git | memory travels with the code, every automatic edit becomes a reviewable diff, and the user can open RULES.md themselves | Claude Code's native per-project store, which sits outside the repo and whose one-fact-per-file convention costs one read per fact; mirroring into both, which guarantees two sources of truth with no rule to arbitrate between them

D-002 | 2026-10-06 | §ROUTER lives in CLAUDE.md; source read-depth lives in SOURCE.md | the router must be free to consult, so it belongs in the only auto-loaded file, while the source map grows with `src\` and the auto-loaded file must not grow | a single POLICY.md for criteria 8+9+10, which costs a ~2.3k-token read before any work can start and is simply skipped by sessions in a hurry; putting everything in CLAUDE.md, which pushes it past 200 lines until it gets skimmed

D-003 | 2026-10-06 | Every memory file is written in English | the facts are about Java identifiers, so English keeps a grep for a class name working; routing keywords stay short | all-Vietnamese, where a `[gotcha]` about a Weka API would not be found by grepping its English class name; mixing languages inside a line, which makes diffs noisy

D-004 | 2026-10-06 | Stage detail is generated just-in-time — one CURRENT.md, for the current or next stage only | detail written after the previous stage's real outcome is far less often wrong, and the read cost stays flat however many stages the project grows | pre-generating two stages ahead; a `stages\` directory with every stage detailed up front, where most content goes stale before it is used and is wrong *confidently*

D-005 | 2026-10-06 | One SessionStart hook that prints §STATE; no PostToolUse hook | it recovers the state cursor when a long session's compaction drops CLAUDE.md out of context, for about five lines of config and no interruption to any write | a PostToolUse write-log, since `git diff` already answers "what did you change" and an append nothing enforces becomes a partial record that looks authoritative; no hooks at all, which would leave the whole system resting on compliance alone

D-006 | 2026-10-06 | PLAN.md and STAGES.md stay separate files | they have opposite edit rhythms — the plan must freeze, stage status changes several times per session — and a clean authority boundary is what gives the §AUTHORITY table any force | one file with a stage table at the end, where Claude would be writing into the same file that holds the project's scope, and "the plan is inviolable" stops meaning anything

D-007 | 2026-10-06 | Decisions get their own append-only DECISIONS.md rather than a `[dec]` tag inside FACTS.md | a decision carries a rejected alternative and a fact does not, and append-only is natural here while facts stay editable | folding them into FACTS.md, which saves a file and a read but mixes two write modes in one file

D-008 | 2026-10-06 | SESSION.md keeps exactly two blocks, and the third may only be dropped after its still-true lines are promoted into FACTS.md | forgetting is deliberate and read cost stays flat, with a funnel into long-term memory so nothing useful is lost silently | one file per session in a `log\` directory, which grows without bound and which no router row reads

D-009 | 2026-10-06 | UI toolkit is JavaFX + AtlantaFX, not Swing + FlatLaf | real CSS lets the Fluent token system from the design reference port almost 1:1, scrolling and animation are first-class, and translucency works today; we are replacing Weka's panels rather than embedding them, so the usual "Weka is Swing" argument does not apply | Swing + FlatLaf, which is cheaper and has better accessibility but gives no Mica without going undecorated and has no animation story; Compose Desktop, which has the best motion but is Material not Fluent, forces Kotlin + Gradle, and does not work with Windows Narrator; SWT, which is Windows-10-era native, not Fluent

D-010 | 2026-10-06 | Pin JavaFX 27 (non-LTS) on JDK 25 LTS | three hard blockers on JavaFX 25, not preferences: the D3D9 `BackBufferFormat = D3DFMT_UNKNOWN` fix that makes UNIFIED + Mica run on the fast painter, the large-table selection and sort fixes, and `ConditionalFeature.EXTENDED_WINDOW`; JavaFX is a dependency, not a platform, so the March 2027 move to 28 is a coordinate bump while the JDK stays on an LTS | JavaFX 25 LTS with AtlantaFX 2.1.0, which has a support window to ~2030 but would mean a 60fps cap or a preview flag for the features we need; JDK 27, which is non-LTS with no upside here

D-011 | 2026-10-06 | GPLv3-or-later, public repository, Weka linked in-process | Weka is GPL-3.0-or-later with no linking exception, so linking forces it; in exchange we get typed `Instances`/`Evaluation`/`Package` objects, the whole headless package-manager API, and no stdout parsing | a closed-source app that only spawns the CLI and does not bundle weka.jar, which is the sole way to stay proprietary but costs regex-parsing confusion matrices out of text, a JVM start per command, and the typed package API

D-012 | 2026-10-06 | Rail = app modes; datasets open as document tabs; a segmented bar inside a tab switches Preprocess/Classify/Cluster/Associate/Select/Visualize | keeps Explorer's vocabulary exactly where people expect it while adding the one thing Explorer cannot do — several datasets open and comparable at once; the segmented control is also what the design reference uses for in-page navigation | putting the Weka tasks on the rail itself, which is simpler and closer to the reference app but allows only one active dataset and wastes document tabs; a pure VS Code shape driving everything from the command palette, where an Explorer user would never find "Classify"

D-013 | 2026-10-06 | Rail and side panel default to the RIGHT edge, swappable by setting | matches the user's own daily VS Code configuration (`workbench.sideBar.location: right`), so the shell feels like the tool they already drive | the left edge, which is what the rest of the world and the design reference do — kept as a setting rather than a default

D-014 | 2026-10-06 | `StageStyle.UNIFIED` + Mica now; the native title bar stays, recoloured via `DWMWA_CAPTION_COLOR`; a custom header waits for JavaFX 28 | these are mutually exclusive today, because `GlassScene.getClearColor()` returns an opaque white for every style except TRANSPARENT and UNIFIED; UNIFIED keeps the fast presenting painter and costs ~15 lines, while a custom header would have to be built twice once PR openjdk/jfx#2048 lands | `StageStyle.EXTENDED` + `HeaderBar` now, which gives a genuine VS Code header with working snap layouts but forfeits translucency entirely; building both behind a setting, which doubles stage-1 scene work for a finish, not a layout

D-015 | 2026-10-06 | Pin `weka-stable` 3.8.7 | it is what the official installers ship and what most repository packages are built and tested against, and the stable line takes bugfixes only, which matters because we lean hard on internal API (`OptionHandler`, `Capabilities`, `WekaPackageManager`) | `weka-dev` 3.9.7, which matches the 3.9.6 already installed on this machine and reads its old models, but whose API can shift between dev releases; supporting both behind a compatibility layer, which doubles the test surface for v1

D-016 | 2026-10-06 | Maven, three modules `core` → `app` → `dist`, with JavaFX at `provided` scope and the application on the classpath; jlink images only JDK + `javafx.*` modules | the only real capability gap favouring Gradle is badass-jlink's merged-module machinery, which exists to drag non-modular jars into a modular link — an architecture we are deliberately not using, since Weka resolves classes by name from props files and loads package jars at runtime | Gradle with badass-jlink, correct only if we went genuinely modular, which is the wrong goal here; making the app a JPMS module, which fights Weka's reflection for no benefit

D-017 | 2026-10-06 | The bottom panel is a tabbed container of output providers; ship a plain console pane first, add JediTermFX later as an optional module | ~95% of "I want a terminal" here is watch, scroll, search, copy, save, kill — none of which needs a VT emulator; designing the panel as providers from day one makes the real terminal one more tab rather than a rewrite | JediTerm inside a SwingNode, which drags AWT into the runtime image and carries known Tab/Escape, popup and focus defects; starting with a PTY at all, which front-loads native dependencies before the layout is even approved

D-018 | 2026-10-06 | Share the standard `%USERPROFILE%\wekafiles` rather than forking `WEKA_HOME`, and surface the effective values in Settings | forking it would make the user's already-installed packages vanish from our package manager, which is a far nastier surprise than inheriting their props files | our own per-app WEKA_HOME, which is cleaner and isolates us from a stale `DatabaseUtils.props` but hides their existing packages

D-019 | 2026-10-06 | `jpackage --type app-image` then Inno Setup 7 for the installer, per-user, no admin | app-image needs nothing but the JDK and gives a portable build for free; Inno adds LZMA2 compression that matters for a bundled JRE, per-user installs with no UAC, dark-mode installer UI, signed uninstallers, and `AppMutex`/`CloseApplications`, which is the hard half of self-updating on Windows | jpackage's own MSI/EXE, which needs WiX plus the .NET SDK on the build machine and has an unthemeable UI; MSIX, whose container fights an app whose job is spawning child processes; Velopack, which still has no JVM SDK

## §Retired
<!-- Superseded lines move here, unchanged, so the reasoning survives without cluttering the
     live list. Never delete one. -->
