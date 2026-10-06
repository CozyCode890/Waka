<!--
READ WHEN : a feature inside the current stage · refactor · any request needing code you have
            not already opened this session. Skip it for a one-file edit you were pointed at.
EDIT      : read-depth rows are yours. Adding to §NEVER-READ needs the user's yes, because it
            blinds every future session to that area.
CAP       : 90 lines.
HARD RULE : do not invent rows. A module earns a row only after a session has actually read it
            and knows what depth it needed. A guessed map is worse than no map — it sends the
            next session to the wrong file with confidence.
-->

# Source map

No source exists yet — the project directory was empty on 2026-10-06. This file stays almost
empty on purpose. Add one row per module as it is written, recording the depth a session
actually needed, not the depth you assume it will need.

Depth verdicts:

| verdict | means |
|---|---|
| `skim` | signatures and public API only; do not read bodies |
| `read` | the whole file |
| `trace` | the file plus its callers and callees — the expensive one, justify it |
| `never` | see §NEVER-READ below |

<!-- Shape — one row per module:
| module | path | purpose | depth |
|---|---|---|---|
-->

## §NEVER-READ
Adding a row here needs the user's yes: it makes every future session blind to that area.

| what | why |
|---|---|
| `*.jar`, including `weka.jar` | binary. Use the Weka javadoc, or ask the user what the API does |
| build output — `target\`, `build\`, `out\`, `bin\`, `*.class` | generated; the source is next door |
| generated code and generated resources | the generator's input is the real source |
| `memory\done\` | archived stage detail. Read it only if the user names that stage |
| `.git\` internals | use git commands; never read the object store |
| `*.arff` test datasets beyond their header | the header gives the schema; the rows are bulk |
