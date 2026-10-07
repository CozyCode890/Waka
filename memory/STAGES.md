<!--
READ WHEN : stage kickoff · status question · triaging an unplanned request · past-stage lookup.
            NOT for a one-file edit, a bug fix, or a trivial question.
EDIT      : status flips and done-dates are yours. Adding, removing, reordering or rewording
            a stage needs the user's yes.
CAP       : 80 lines.
HARD RULE : no implementation detail, ever. A stage block longer than five lines means you are
            writing CURRENT.md in the wrong file.
-->

# Stages

Derived from PLAN.md 2026-10-06, one block per stage. Status: `TODO` | `CURRENT` | `DONE (yyyy-mm-dd)`.
Exactly one stage may be `CURRENT`. The block shape is fixed — goal, exit test(s), depends,
status. Copy an existing block; never add a field, and keep each field on one line.

## S00 — Prove the stack
goal      : the three technical bets this stack rests on are confirmed on this machine before anything is built on them
exit test : a throwaway JavaFX 27 + AtlantaFX 3.0.0 window launches from Maven on JDK 25, shows Mica through StageStyle.UNIFIED, and is not on the uploading painter
exit test : a TableView scrolls 100k rows at 60fps, the ROW-based attribute view opens 2,000 attributes without degrading, and the real TableView column ceiling on this machine is measured and recorded
depends   : none
status    : CURRENT

## S01 — The shell
goal      : a VS Code-shaped window the user can drive and call right, with no Weka call anywhere inside it
exit test : rail, document tabs, bottom panel, status bar and command palette all work, and a keymap file can REMOVE a default binding, not only add one
exit test : a jpackage app-image launches by double-click outside the IDE, and the app looks finished with Mica off
depends   : S00
status    : TODO

## S02 — The options editor
goal      : any Weka OptionHandler renders as a real form, replacing the GenericObjectEditor dialog
exit test : J48, RandomForest, SMO and one filter each render usable controls, including schemes carrying only a flag letter and a synopsis
exit test : the form's option string is identical to the one the Explorer produces for the same settings
depends   : S01
status    : TODO

## S03 — Preprocess and Classify, end to end
goal      : one vertical slice that proves the engine split, the run model, cancel and run history at once
exit test : iris.arff reaches a J48 tree in under five clicks, a 100k-row ARFF loads without blocking the UI thread, and a 2,000-attribute ARFF opens without the attribute view degrading
exit test : cancel visibly stops a running cross-validation within about a second, our numbers match the Explorer's for the same option string, and a run from before a restart is still in history and comparable side by side
depends   : S02
status    : TODO

## S04 — The remaining task tabs
goal      : Cluster, Associate, Select attributes and Visualize reach the same standard as Classify — repetition, not invention
exit test : each of the four runs end to end on iris.arff with cancel and history working, through the same engine and options editor as S03
exit test : every result screen in the app can also produce Weka's original monospace text verbatim
depends   : S03
status    : TODO

## S05 — The package manager
goal      : packages are browsed, pinned, installed and loaded from inside the app, over WekaPackageManager
exit test : install a package from the manager and use its classifier without restarting the app
exit test : the three-state model (installed / installed-but-not-loaded / available) is visible, with version pinning and a dependency preview shown before any install
depends   : S04
status    : TODO

## S06 — Ship it
goal      : a stranger on a clean Windows 10 machine can install this and use it
exit test : the installer completes on Windows 10 without admin rights, bundles its own JRE, and the app looks finished there with no Mica and no rounded corners
exit test : nothing in the app asks "are you sure?" — every destructive action undoes instead
depends   : S05
status    : TODO
