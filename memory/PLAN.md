<!--
READ WHEN : cold start · stage kickoff · unplanned feature · refactor · scope change.
            NOT for a one-file edit, a bug fix, or a trivial question.
EDIT      : propose-only. The user approves every change, typos included.
CAP       : 120 lines. Over cap means this file holds detail that belongs in STAGES.md.
HARD RULE : this file answers WHAT and WHY. It never answers HOW.
-->

# Weka-GUI — master plan

> STATUS: **EMPTY**. Fill this in with the user in the planning session, then freeze it.
> Until it is frozen, no code gets written. Nothing below is decided yet.

## Goal
<!-- One paragraph. What does this GUI let a person do that Weka's own Explorer does not,
     or does worse? If the answer is "the same thing but prettier", say that plainly. -->
TODO

## In scope
<!-- The things this GUI will do. Each line should be something a user can point at. -->
TODO

## NON-goals
<!-- The most valuable section in this file. Each line here is a request that a future
     session must refuse, and a feature the user must not be talked into. -->
TODO

## Stack
<!-- Swing or JavaFX (and why) · how weka.jar is consumed: vendored / classpath / Maven
     dependency · build tool · target JDK · does it ship as a jar, an installer, or neither -->
TODO

## Target user
<!-- Who sits in front of this. A student? The user themselves? A class of 40? It decides
     how much hand-holding the UI owes and how much it may assume. -->
TODO

## Success tests
<!-- Concrete and checkable, not aspirational. Shape:
     "A user can load iris.arff and see a J48 tree in under five clicks."
     "A 100k-row ARFF loads without freezing the UI thread."
     These become the exit tests of the last stage. -->
TODO

## Known risks
<!-- The things most likely to break this project. Weka API surprises, Swing threading,
     dataset size, scope creep. One line each. -->
TODO

## Why the stages are cut this way
<!-- One or two sentences on the ordering principle. The stage list itself lives in
     STAGES.md — do not duplicate it here. -->
TODO
