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
[gotcha] JavaFX TableView degrades around 50-100 columns and is unusable past 500 because TableColumn cells are not pooled; horizontal virtualization only engages when setFixedCellSize is set — 2026-10-06
[gotcha] Weka package classes are invisible to Class.forName — use WekaPackageClassLoaderManager.forName and SerializationHelper.getObjectInputStream for models built with package schemes — 2026-10-06
[gotcha] weka.Run prints a numbered menu and blocks on stdin for an ambiguous scheme name — always pass the fully-qualified class name from our side — 2026-10-06
[proj] weka-stable marks commons-compress optional so it is not inherited transitively; declare it explicitly or .arff.gz and .arff.bz2 loading fails at runtime — 2026-10-06
[proj] weka-stable 3.8.7 already depends on com.formdev:flatlaf:3.7.1 at runtime, so "Weka's GUI looks old" is no longer an accurate pitch — 2026-10-06

## §Promoted
<!-- /wrap lists here which SESSION.md lines became facts, so nothing is dropped silently.
     Shape: 2026-10-06 — promoted from s01: [env] ..., [proj] ... -->
