<!--
READ WHEN : cold start · stage kickoff · unplanned feature · refactor · scope change.
            NOT for a one-file edit, a bug fix, or a trivial question.
EDIT      : propose-only. The user approves every change, typos included.
CAP       : 120 lines. Over cap means this file holds detail that belongs in STAGES.md.
HARD RULE : this file answers WHAT and WHY. It never answers HOW.
-->

# Weka-GUI — master plan

> STATUS: **FROZEN 2026-10-06.** Changing any line below needs the user's explicit yes.

## Goal
A VS Code-shaped desktop shell over Weka's own algorithms. Weka 3.8.7 already ships a FlatLaf
skin, so "the same thing but prettier" is NOT the pitch and must never become it. Four things
the Explorer does badly are the whole reason this exists: the GenericObjectEditor dialog, a run
that cannot be cancelled, a result history that dies with the process, and one dataset at a
time. The analysis itself stays Weka's — this project never reimplements an algorithm.

## In scope
- A VS Code shell: vertical icon rail, document tabs, a bottom panel, a status bar, a command
  palette, and a user-editable keymap that can REMOVE a default binding, not only add one.
- Datasets open as document tabs. Inside a tab, a segmented bar switches Preprocess / Classify
  / Cluster / Associate / Select attributes / Visualize. Several datasets open at once.
- A rebuilt options editor generated from `OptionHandler.listOptions()` + `@OptionMetadata`,
  with a hand-written override table for the schemes people actually touch.
- A package manager over `WekaPackageManager`: version pinning, dependency preview, and a
  three-state model (installed / installed-but-not-loaded / available).
- A bottom panel of output providers — Weka Output, Package Log, Problems — one of which later
  becomes a real terminal.
- Run history that survives a restart, keyed by option string plus dataset fingerprint, with
  runs comparable side by side.
- Every result screen can also render Weka's original monospace text verbatim.
- Windows 11 Mica, with an opaque theme that looks finished on its own.
- An installer that bundles its own JRE, needs no admin rights, and runs on Windows 10.

## NON-goals
Each line here is a request a future session must refuse.
- KnowledgeFlow, Experimenter, Boundary Visualizer, Cost-Benefit Analysis. Not in v1.
- Hosting legacy Swing Explorer tabs contributed by packages. We read package props files; we
  do not embed their UI.
- Reimplementing any Weka algorithm, filter, evaluation or metric.
- A database connection UI — no rebuild of `weka.gui.sql`.
- macOS and Linux builds. Windows x64 only; ARM64 is a later question.
- A custom-drawn title bar, until JavaFX 28 lets it coexist with Mica.
- Theming beyond one user accent plus light/dark/auto.
- A self-hosted package mirror. The design is known and recorded; building it is not v1.
- Auto-update beyond check-the-GitHub-release, notify, hand off to the installer.
- Any closed-source or proprietary licensing. That door is shut by linking Weka.
- Chasing pixel-identical Fluent. Geometry and material, not a WinUI 3 clone.

## Stack
- **Java 25 LTS** (Temurin), bundled with the app. The machine currently has 21.
- **JavaFX 27 + AtlantaFX 3.0.0.** Deliberately not JavaFX 25 LTS: 27 carries the D3D9
  swap-chain alpha fix that makes Mica free, the large-table selection/sort fixes, and
  `ConditionalFeature.EXTENDED_WINDOW`. JavaFX is a dependency, not a platform — moving to 28
  is a coordinate bump. The JDK stays on an LTS.
- **weka-stable 3.8.7** from Maven Central, linked in-process, with `commons-compress` declared
  explicitly because Weka marks it optional.
- **GPLv3-or-later**, public repository. Forced by linking Weka; accepted deliberately.
- **Maven**, three modules: `core` (UI-free, the only place that imports `weka.*`) → `app`
  (JavaFX) → `dist` (packaging only), plus a non-Maven `installer/`.
- **jpackage app-image → Inno Setup 7**, per-user install, user data outside the install dir.
- **StageStyle.UNIFIED + Mica**; the native title bar recoloured to match the theme.
- Two engines behind one interface: in-process for loading, filtering, preview and short fits;
  subprocess for long or risky runs, because that is the only way to truly cancel one.

## Target user
The author first — this has to be the tool they reach for instead of the Explorer. Then
students and teaching staff who already speak Explorer's vocabulary: they must find Classify
where they expect it, and must be able to check our numbers against the old GUI. The UI may
assume the domain and must not assume the tool.

## Success tests
These become the exit tests of the last stage.
- Load `iris.arff` and see a J48 tree in under five clicks.
- A 100k-row ARFF loads without blocking the UI thread, and scrolls at 60fps.
- An ARFF with 2,000 attributes opens without the attribute view degrading.
- Cancel visibly stops a running cross-validation within about a second.
- For the same option string on the same dataset, our numbers match the Explorer's.
- Install a package from the manager and use its classifier without restarting.
- Every result screen can produce Weka's original text output verbatim.
- The installer completes on Windows 10 without admin rights, and the app looks finished there
  with no Mica and no rounded corners.
- Nothing in the app asks "are you sure?" — destructive actions undo instead.

## Known risks
- The options editor is the single biggest build item, and every task screen depends on it.
  Older schemes carry only a flag letter and a synopsis, so part of it is heuristics.
- JavaFX TableView breaks on COLUMN count, not row count — painful past ~50-100, unusable past
  500. A wide ARFF must never map one column per attribute.
- JavaFX 27 is non-LTS; expect a bump to 28 around March 2027.
- Mica-on-UNIFIED-without-the-slow-painter is inferred from a fix that shipped three weeks ago
  and has not been confirmed on NVIDIA/Intel/AMD. Verify early; it is a 20-minute test.
- Weka has no cooperative cancellation, so an honest Cancel means a subprocess.
- Sharing `%USERPROFILE%\wekafiles` means a user's existing props files can silently
  reconfigure us. Accepted, because forking WEKA_HOME would hide their installed packages.
- Scope creep toward KnowledgeFlow and the Experimenter. See NON-goals.
- Memory: `Instances` is an in-RAM double matrix, and an in-process OOM takes the UI with it.

## Why the stages are cut this way
The shell comes before any Weka call, because the user must look at the layout and say it is
right before logic is welded to it. The options editor comes next, since every task screen
needs it. Then one vertical slice — Preprocess plus Classify, end to end — because that single
slice proves the engine split, the run model, the cancel story and the history at once. The
remaining tabs are then repetition, not invention. Packaging comes last, but is rehearsed early
enough that "it only runs from the IDE" is never true for long.
