package waka.spike;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Stage S00: one window that carries all three probes.
 *
 * <pre>
 *   mvnw javafx:run                      the window with three buttons, for the Mica check
 *   mvnw javafx:run -Dprobe=b            100000 rows, sweeps, print the frame times, exit
 *   mvnw javafx:run -Dprobe=c            2000 attribute rows and the column sweep, exit
 *   mvnw javafx:run -Dtheme=light        either of the above with the light caption
 *   mvnw javafx:run -Dprobe=c -Doffscreen=true   run it without taking over the screen
 *   mvnw javafx:run -Dprobe=b -Dstylesheet=none -Drows=1000   isolate what a cost belongs to
 * </pre>
 *
 * <p>The sweeps move the table the way the two real gestures do, and not by index. Scrolling by
 * index means TableView.scrollTo, which asks the flow to show a row it may already be showing
 * and rebuilds cells to do it; nobody reads data that way, so a frame time measured through it
 * belongs to the probe and not to the table. VirtualFlow.scrollPixels is the call a mouse wheel
 * makes, and setPosition is a scrollbar thumb being dragged.
 *
 * <p>Nothing here is a draft of the real shell. S01 owns that.
 */
public final class StackProbe extends Application {

    /** DESIGN.md geometry: 26px rows. FACTS: horizontal virtualization only engages when
     *  setFixedCellSize is set, so every table in this probe sets it. */
    private static final double ROW_HEIGHT = 26.0;

    /** Overridable with -Drows=N, so a cost can be told apart from the size of the data. */
    private static final int PROBE_B_ROWS = Integer.getInteger("rows", 100_000);
    private static final int PROBE_B_COLUMNS = 10;

    private static final int PROBE_C_ATTRIBUTES = 2_000;
    private static final int[] COLUMN_SWEEP = { 50, 100, 200, 500, 1000, 2000 };
    private static final int COLUMN_SWEEP_ROWS = 10_000;

    /** Frames per sweep. Three seconds of a 60Hz display. */
    private static final int SWEEP_FRAMES = 180;

    /** Rows moved per frame at wheel speed: one notch of a mouse wheel. */
    private static final int WHEEL_ROWS_PER_FRAME = 3;

    /** Columns moved per frame when scrolling sideways. */
    private static final int WHEEL_COLUMNS_PER_FRAME = 2;

    /** Past this p95 the table is roughly fifteen frames behind and no larger column count can
     *  change the answer, so the sweep stops instead of spending minutes confirming it. */
    private static final double GIVE_UP_P95_MILLIS = 250.0;

    /** A title nothing else on the desktop shares, so FindWindowW cannot pick up a stranger. */
    private final String windowTitle = "Waka stack probe S00 pid=" + ProcessHandle.current().pid();

    private final List<SweepResult> sweepResults = new ArrayList<>();

    private Stage stage;
    private boolean darkTheme;

    /** One column count of the sweep. The ceiling is read off these. */
    private record SweepResult(int columnCount, double openMillis,
                               FrameRateMeter.Measurement verticalWheel,
                               FrameRateMeter.Measurement sidewaysWheel,
                               FrameRateMeter.Measurement thumbDrag) {

        /** What decides the ceiling: the worse of the two gestures a person reads data with. */
        double worstWheelP95Millis() {
            return Math.max(verticalWheel.p95Millis(), sidewaysWheel.p95Millis());
        }
    }

    public static void main(String[] arguments) {
        launch(arguments);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.darkTheme = !"light".equalsIgnoreCase(System.getProperty("theme", "dark"));

        // -Dstylesheet=none leaves JavaFX on its own Modena theme. AtlantaFX restyles every cell
        // in the table, so this is the switch that says whether a slow table is TableView's
        // fault or the theme's.
        if (!"none".equalsIgnoreCase(System.getProperty("stylesheet", "atlantafx"))) {
            Application.setUserAgentStylesheet(darkTheme
                    ? new PrimerDark().getUserAgentStylesheet()
                    : new PrimerLight().getUserAgentStylesheet());
        }

        BorderPane root = new BorderPane();
        // Mica is painted by the window manager underneath us, so anything opaque in front of it
        // hides it. AtlantaFX gives .root an opaque background; an inline style outranks that.
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root, 1280, 820);
        scene.setFill(Color.TRANSPARENT);

        // FACTS: StageStyle.TRANSPARENT forces JavaFX's UploadingPainter unconditionally on
        // Windows, which costs a full-frame GPU readback. UNIFIED is the whole point of probe A.
        stage.initStyle(StageStyle.UNIFIED);
        stage.setTitle(windowTitle);
        stage.setScene(scene);

        String probe = System.getProperty("probe", "menu").trim().toLowerCase(Locale.ROOT);
        boolean automated = probe.equals("b") || probe.equals("c") || probe.equals("paint");

        if (automated) {
            root.setCenter(describe("Running probe " + probe + ". The numbers go to the console."));
        } else {
            root.setTop(buildButtonBar(root));
            root.setCenter(describe("Probe A is this window itself. Over a bright wallpaper the "
                    + "body and the caption must both blur, and the caption must match the theme."));
        }

        // -Doffscreen=true parks the window outside the visible desktop so a long sweep does not
        // take over the screen. The window manager still composites it, and the baseline sweep
        // proves that: if the frames were not being paced, the baseline would come out far under
        // the refresh period and every number after it would be worthless.
        if (Boolean.getBoolean("offscreen")) {
            stage.setX(-3200);
            stage.setY(120);
        }

        stage.show();

        printBanner(probe);
        System.out.println(MicaWindow.applyMica(stage, windowTitle, darkTheme).describe());

        switch (probe) {
            case "b" -> afterShortPause(() -> runBaseline(() -> runProbeB(root, StackProbe::finish)));
            case "c" -> afterShortPause(() -> runBaseline(() -> runProbeC(root, StackProbe::finish)));
            case "paint" -> afterShortPause(() -> runBaseline(() -> runPaintProbe(root, StackProbe::finish)));
            default -> System.out.println("probe | menu mode: the window stays open. "
                    + "Look at it, then press a button.");
        }
    }

    /**
     * A sweep with nothing to draw, run before the real one. Under vsync it has to come out at
     * the display's refresh period, so it reports the floor on this machine and proves the frames
     * are still being paced.
     */
    private void runBaseline(Runnable next) {
        FrameRateMeter.sweep("baseline | nothing to draw", 120,
                (progress, delta) -> {
                    // Nothing. That is the point.
                },
                measurement -> {
                    System.out.println(measurement);
                    next.run();
                });
    }

    /**
     * The control for the baseline. One label whose text changes every frame: almost no work to
     * draw, but the frame really is dirty, so the window manager really has to show a new one.
     * The baseline above never dirties anything and so proves only that pulses arrive on time.
     * If this comes out near the refresh period, a slow table afterwards is the table's doing;
     * if it comes out slow too, the window is not being shown at full rate and no number
     * measured in it means anything.
     */
    private void runPaintProbe(BorderPane root, Runnable whenFinished) {
        Label ticker = describe("frame 0");
        root.setCenter(ticker);
        forceLayout(root);

        int[] frameNumber = { 0 };
        measure("probe paint | one label, new text every frame",
                new Gesture() {
                    @Override
                    public void onFrame(double progress, double delta) {
                        ticker.setText("frame " + frameNumber[0]++);
                        moved(1);
                    }
                },
                measurement -> whenFinished.run());
    }

    // ---- probe B: 100000 rows ----

    private void runProbeB(BorderPane root, Runnable whenFinished) {
        TableView<Integer> table = newTable();
        long startNanos = System.nanoTime();
        table.getColumns().setAll(SyntheticTable.dataColumns(PROBE_B_COLUMNS));
        table.setItems(SyntheticTable.rowIndexes(PROBE_B_ROWS));
        root.setCenter(table);
        forceLayout(root);

        String prefix = String.format("probe B | rows=%d columns=%d open=%6.0fms",
                PROBE_B_ROWS, PROBE_B_COLUMNS, millisSince(startNanos));
        VirtualFlow<?> flow = virtualFlow(table);

        measure(prefix + " wheel     ", wheelVertically(flow),
                first -> measure(prefix + " thumb drag", dragThumb(flow),
                        second -> whenFinished.run()));
    }

    // ---- probe C: 2000 attributes, then the real column ceiling ----

    private void runProbeC(BorderPane root, Runnable whenFinished) {
        TableView<SyntheticTable.AttributeRow> table = newTable();
        long startNanos = System.nanoTime();
        table.getColumns().setAll(SyntheticTable.attributeColumns());
        table.setItems(SyntheticTable.attributeRows(PROBE_C_ATTRIBUTES));
        root.setCenter(table);
        forceLayout(root);

        String prefix = String.format("probe C attributes | rows=%d columns=5 open=%6.0fms",
                PROBE_C_ATTRIBUTES, millisSince(startNanos));
        VirtualFlow<?> flow = virtualFlow(table);

        measure(prefix + " wheel     ", wheelVertically(flow),
                first -> measure(prefix + " thumb drag", dragThumb(flow),
                        second -> {
                            sweepResults.clear();
                            runColumnSweep(root, 0, whenFinished);
                        }));
    }

    /**
     * One column count per step, measured three ways. Scrolling down alone would flatter the
     * table at wide column counts; scrolling sideways is what brings new columns into view, and
     * FACTS says TableColumn cells are not pooled, so that is where the cost should appear.
     */
    private void runColumnSweep(BorderPane root, int step, Runnable whenFinished) {
        if (step >= COLUMN_SWEEP.length) {
            reportColumnCeiling();
            whenFinished.run();
            return;
        }

        int columnCount = COLUMN_SWEEP[step];
        System.out.printf("probe C sweep | columns=%d building%n", columnCount);

        TableView<Integer> table = newTable();
        long startNanos = System.nanoTime();
        table.getColumns().setAll(SyntheticTable.dataColumns(columnCount));
        table.setItems(SyntheticTable.rowIndexes(COLUMN_SWEEP_ROWS));
        root.setCenter(table);
        forceLayout(root);
        double openMillis = millisSince(startNanos);

        String prefix = String.format("probe C sweep | columns=%4d rows=%d open=%7.0fms",
                columnCount, COLUMN_SWEEP_ROWS, openMillis);
        VirtualFlow<?> flow = virtualFlow(table);
        ScrollBar sideways = horizontalScrollBar(table);

        measure(prefix + " wheel down    ", wheelVertically(flow),
                verticalWheel -> measure(prefix + " wheel sideways", wheelSideways(sideways, columnCount),
                        sidewaysWheel -> measure(prefix + " thumb drag    ", dragThumb(flow),
                                thumbDrag -> finishSweepStep(root, step,
                                        new SweepResult(columnCount, openMillis, verticalWheel,
                                                sidewaysWheel, thumbDrag),
                                        whenFinished))));
    }

    private void finishSweepStep(BorderPane root, int step, SweepResult result,
                                 Runnable whenFinished) {
        sweepResults.add(result);

        if (result.worstWheelP95Millis() > GIVE_UP_P95_MILLIS) {
            System.out.printf("probe C sweep | stopping at %d columns: p95 is already %.0fms at "
                            + "wheel speed, so a larger count cannot move the ceiling%n",
                    result.columnCount(), result.worstWheelP95Millis());
            reportColumnCeiling();
            whenFinished.run();
            return;
        }
        runColumnSweep(root, step + 1, whenFinished);
    }

    /**
     * The ceiling under all three readings of the budget, because vsync makes the strictest one
     * hard to reach even for a table that behaves. Wheel speed decides it: a ceiling set by
     * flinging a scrollbar would be a ceiling for a gesture nobody uses to read data.
     */
    private void reportColumnCeiling() {
        System.out.println("probe C ceiling | measured counts " + describeMeasuredCounts());
        System.out.println("probe C ceiling | p95 <= 16.7ms (budget read literally)    : "
                + ceilingUnder(FrameRateMeter.SIXTY_FPS_BUDGET_MILLIS) + " columns");
        System.out.println("probe C ceiling | p95 <= 25.0ms (under 5% frames dropped)  : "
                + ceilingUnder(FrameRateMeter.DROPPED_FRAME_MILLIS) + " columns");
        System.out.println("probe C ceiling | p95 <= 33.3ms (degraded but still usable): "
                + ceilingUnder(FrameRateMeter.THIRTY_FPS_BUDGET_MILLIS) + " columns");
    }

    private int ceilingUnder(double budgetMillis) {
        int ceiling = 0;
        for (SweepResult result : sweepResults) {
            if (result.worstWheelP95Millis() > budgetMillis) {
                // The first count that misses ends it. A larger one passing after it is noise,
                // not a licence to claim the larger number.
                break;
            }
            ceiling = result.columnCount();
        }
        return ceiling;
    }

    private String describeMeasuredCounts() {
        StringBuilder counts = new StringBuilder();
        for (SweepResult result : sweepResults) {
            if (!counts.isEmpty()) {
                counts.append(", ");
            }
            counts.append(result.columnCount());
        }
        return counts.toString();
    }

    // ---- the two real gestures ----

    /**
     * A scripted gesture that keeps count of how far it actually moved the table. Without that
     * count a sweep that quietly moved nothing reads as a sweep that was cheap, and a table that
     * was never scrolled would be reported as a table that scrolls beautifully.
     */
    private abstract static class Gesture implements FrameRateMeter.FrameAction {
        private double movedPixels;

        protected void moved(double pixels) {
            movedPixels += Math.abs(pixels);
        }

        double movedPixels() {
            return movedPixels;
        }
    }

    /** Three rows per frame, downward then back up: one notch of a mouse wheel, repeated. */
    private static Gesture wheelVertically(VirtualFlow<?> flow) {
        return new Gesture() {
            @Override
            public void onFrame(double progress, double delta) {
                moved(flow.scrollPixels(Math.signum(delta) * WHEEL_ROWS_PER_FRAME * ROW_HEIGHT));
            }
        };
    }

    /** The vertical scrollbar thumb dragged from one end to the other and back. */
    private static Gesture dragThumb(VirtualFlow<?> flow) {
        return new Gesture() {
            @Override
            public void onFrame(double progress, double delta) {
                double before = flow.getPosition();
                flow.setPosition(progress);
                moved((flow.getPosition() - before) * flow.getCellCount() * ROW_HEIGHT);
            }
        };
    }

    /** Two columns per frame sideways, which is a shift-wheel or a trackpad swipe. */
    private static Gesture wheelSideways(ScrollBar sideways, int columnCount) {
        double step = (sideways.getMax() - sideways.getMin())
                * WHEEL_COLUMNS_PER_FRAME / (double) columnCount;
        return new Gesture() {
            @Override
            public void onFrame(double progress, double delta) {
                double before = sideways.getValue();
                sideways.setValue(Math.clamp(before + Math.signum(delta) * step,
                        sideways.getMin(), sideways.getMax()));
                moved(sideways.getValue() - before);
            }
        };
    }

    private static void measure(String label, Gesture gesture,
                                Consumer<FrameRateMeter.Measurement> whenFinished) {
        FrameRateMeter.sweep(label, SWEEP_FRAMES, gesture,
                measurement -> {
                    System.out.printf("%s moved=%7.0fpx%n", measurement, gesture.movedPixels());
                    whenFinished.accept(measurement);
                });
    }

    private static VirtualFlow<?> virtualFlow(TableView<?> table) {
        Node found = table.lookup(".virtual-flow");
        if (!(found instanceof VirtualFlow<?> flow)) {
            throw new IllegalStateException("no virtual flow under the table; was it laid out?");
        }
        return flow;
    }

    private static ScrollBar horizontalScrollBar(TableView<?> table) {
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar bar && bar.getOrientation() == Orientation.HORIZONTAL) {
                return bar;
            }
        }
        throw new IllegalStateException("no horizontal scroll bar; are the columns narrower "
                + "than the window?");
    }

    // ---- window plumbing ----

    private HBox buildButtonBar(BorderPane root) {
        Button probeA = new Button("Probe A: re-apply Mica, print the report");
        probeA.setOnAction(event ->
                System.out.println(MicaWindow.applyMica(stage, windowTitle, darkTheme).describe()));

        Button probeB = new Button("Probe B: 100,000 rows by 10 columns");
        probeB.setOnAction(event -> runProbeB(root, () -> System.out.println("probe B | done")));

        Button probeC = new Button("Probe C: 2,000 attribute rows, then the column sweep");
        probeC.setOnAction(event -> runProbeC(root, () -> System.out.println("probe C | done")));

        HBox bar = new HBox(8, probeA, probeB, probeC);
        bar.setPadding(new Insets(8));
        return bar;
    }

    private static Label describe(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setPadding(new Insets(16));
        return label;
    }

    private static <T> TableView<T> newTable() {
        TableView<T> table = new TableView<>();
        table.setFixedCellSize(ROW_HEIGHT);
        // CONSTRAINED would try to squeeze 2000 columns into the window width, which is not a
        // thing any data sheet does.
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("no rows"));
        return table;
    }

    /**
     * Open time means "the table is laid out and ready", so the css and layout passes have to be
     * paid before the clock is read. At 2000 columns they are nearly all of the cost.
     */
    private static void forceLayout(BorderPane root) {
        root.applyCss();
        root.layout();
    }

    private static double millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }

    private static void afterShortPause(Runnable work) {
        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(event -> work.run());
        pause.play();
    }

    private static void finish() {
        System.out.println("probe | done");
        Platform.exit();
    }

    private void printBanner(String probe) {
        System.out.println("probe | mode=" + probe + " theme=" + (darkTheme ? "dark" : "light")
                + " offscreen=" + Boolean.getBoolean("offscreen")
                + " stylesheet=" + System.getProperty("stylesheet", "atlantafx")
                + " rows=" + PROBE_B_ROWS);
        System.out.println("probe | java=" + System.getProperty("java.version")
                + " javafx=" + System.getProperty("javafx.runtime.version")
                + " os=" + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        // S00-T07: this flag must be absent. FACTS says it caps JavaFX at 60fps and is not needed
        // from JavaFX 27, so a value other than null here invalidates the frame-time numbers.
        System.out.println("probe | stageStyle=" + stage.getStyle()
                + " fixedCellSize=" + ROW_HEIGHT
                + " prism.forceUploadingPainter=" + System.getProperty("prism.forceUploadingPainter"));
    }
}
