# spike — stage S00, prove the stack

Throwaway probes. **Not** part of the Waka Maven reactor: this directory has its own standalone
`pom.xml`, and nothing in it is a draft of the real application.

It survives the stage on purpose. When the project moves to JavaFX 28 in 2027, re-run these and
compare against the numbers below rather than arguing from memory about what used to be fast.

```
.\mvnw javafx:run                       the window with three buttons, for the Mica check
.\mvnw javafx:run -Dtheme=light         the same, with the light caption
.\mvnw javafx:run -Dprobe=b             100000 rows, sweeps, print the frame times, exit
.\mvnw javafx:run -Dprobe=c             2000 attribute rows and the column sweep, exit
.\mvnw javafx:run -Dprobe=paint         the control: one label, new text every frame
.\mvnw exec:java                        the headless Weka link probe
```

Knobs: `-Drows=N`, `-Dstylesheet=none` (Modena instead of AtlantaFX), `-Doffscreen=true`
(parks the window off the desktop — **read the warning below before trusting any number from it**).

## How to read a measurement

Each sweep prints `min`, `avg`, `p95`, the share of frames over 25ms, and `moved`, which is how
far the gesture actually scrolled the table. `moved` is there because a sweep that silently moved
nothing reads as a sweep that was cheap.

Two gestures are measured, and they are not the same question:

- **wheel** — three rows per frame through `VirtualFlow.scrollPixels`, which is the call a mouse
  wheel makes. Most cells survive to the next frame. This is what reading data looks like.
- **thumb drag** — `VirtualFlow.setPosition` swept end to end, about 1100 rows per frame. No cell
  survives, so every visible cell in every visible column is rebuilt every frame.

## Two traps this spike walked into

**An off-screen or hidden window is throttled.** The control probe — one `Label` whose text
changes each frame, nothing else — costs **16.35ms** on screen and **31.64ms** parked off the
desktop, with identical work. The first three runs of probe B were taken off-screen, reported
~48ms and 98% dropped frames, and were worthless. The same should be expected of a window on an
inactive Windows virtual desktop, which the shell cloaks. Measure on a visible window.

**The idle baseline is not a floor.** With nothing dirty, JavaFX falls back to a 60Hz pulse timer,
so `baseline` always reports about 16.00ms. This machine's panel runs at **144Hz**, and a table
that is comfortably fast measures 4–9ms, well under the idle baseline. A number near 16.7ms means
60fps, which here is the target and not the ceiling.

## Measured, 2026-10-08

Machine: Temurin JDK 25.0.4.1, JavaFX 27+30, AtlantaFX 3.0.0, Windows 11 build 26100,
NVIDIA GeForce RTX 4050 Laptop GPU, driver `nvldumdx.dll` 32.0.16.1742, D3D9Ex, 1920x1080 @ 144Hz.
Window 1280x820, `setFixedCellSize(26)`, `UNCONSTRAINED_RESIZE_POLICY`.

### Probe A — Mica on StageStyle.UNIFIED

Works. Screenshots in `evidence/`. Both routes to the window handle agree, and all four DWM calls
return `S_OK`. `DwmExtendFrameIntoClientArea` with every margin at -1 is **required**: without it
the backdrop attribute is still accepted, the caption recolours, and the body stays flat opaque.

Painter: `Not forcing UploadingPainter`, `Prism pipeline name = com.sun.prism.d3d.D3DPipeline`,
`vsync: true vpipe: true`, and `prism.forceUploadingPainter` is `null`.

### Probe B — rows

| table | gesture | min | avg | p95 | over 25ms | moved |
|---|---|---|---|---|---|---|
| control: one label | retext | 3.73 | 16.35 | 22.59 | 4.4% | — |
| 100000 x 10 | wheel | 6.69 | 17.07 | 28.64 | 6.1% | 13962px |
| 100000 x 10 | thumb drag | 7.62 | 16.63 | 18.09 | 3.9% | 5171189px |
| 2000 x 5 (attributes) | wheel | 1.45 | 6.38 | 16.46 | 1.7% | 13962px |
| 2000 x 5 (attributes) | thumb drag | 1.84 | 5.03 | 11.10 | 1.1% | 103501px |

100000 rows open in 643ms and scroll at an average of 17.07ms, which is 58.6fps: at the 60fps
line, with no headroom and occasional two-frame stalls. 2000 rows run at 6.38ms, about 157fps.
The row count is the difference; the stylesheet is not. Probe B at 1000 rows and at 100000 rows
measured the same only while both were being taken off-screen.

### Probe C — the column ceiling, 10000 rows

p95 in milliseconds. Bold is the first count that misses the 16.7ms budget.

| columns | open | wheel down | wheel sideways | thumb drag |
|---|---|---|---|---|
| 50 | 463ms | 6.38 | 16.57 | 25.58 |
| 100 | 514ms | 4.99 | 16.47 | 25.02 |
| 200 | 892ms | 6.76 | 10.11 | **37.82** (77% dropped) |
| 500 | 1710ms | 11.54 | 10.88 | 74.25 (99% dropped) |
| 1000 | 4179ms | **27.29** | **18.24** | 172.99 |
| 2000 | 10597ms | 44.64 (98% dropped) | 31.40 (99% dropped) | 369.62 |

**The ceiling is 500 columns.** Up to 500, both wheel gestures stay inside 16.7ms. At 1000 the
table is degraded but still usable, and it opens in 4.2 seconds. At 2000 it is neither: 10.6
seconds to open and a 98% dropped-frame rate.

Dragging the vertical scrollbar is a much lower ceiling — it breaks at **200 columns** — because
a drag rebuilds every visible cell in every visible column each frame, and `TableColumn` cells
are not pooled. A wide data sheet needs the drag throttled, not just the column count capped.

### Side check — weka-stable

`iris.arff: 150 instances, 5 attributes`, headless, with `commons-compress` declared explicitly
and an `.arff.gz` round trip returning the same shape. `weka.core.Version.VERSION` reports
`3.8.8-SNAPSHOT` from the `weka-stable:3.8.7` artifact; the coordinate and the self-reported
version do not match.

`javafx:run` warns that 17 of weka's transitive jars cannot be placed on the module path at all
(`netlib-native_*` contain `native`, `java-cup` contains `11b`, neither is a legal module name).
They are dropped with a warning here. S01 should expect the same jars to be a problem for jlink.
