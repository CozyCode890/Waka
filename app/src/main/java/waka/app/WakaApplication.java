package waka.app;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The application. One window, one {@link StageStyle#UNIFIED} stage, the native Windows caption
 * kept and recoloured.
 *
 * <p>UNIFIED rather than a custom-drawn header: {@code GlassScene.getClearColor()} returns an
 * opaque white for every stage style except TRANSPARENT and UNIFIED, and TRANSPARENT forces
 * JavaFX's slow uploading painter unconditionally. So translucency and a custom header are
 * mutually exclusive until JavaFX 28, and D-014 chose translucency. PLAN's NON-goals list the
 * custom title bar as out of scope until then.
 *
 * <p>The window this class puts up is a placeholder on purpose. S01-T13 to T20 replace the root
 * below with the real shell; what is here exists so that the material, the two themes and the
 * Mica on/off swap can be looked at and graded before any layout is welded to them. It contains
 * no text at all, which is deliberate: D-024 requires every visible string to come from the
 * language pack from the very first screen, and the language pack is S01-T26. The first label
 * this application draws and its resource bundle arrive together.
 */
public final class WakaApplication extends Application {

    /**
     * The window title, and also how {@link MicaWindow} finds the native window handle, so
     * nothing else on the desktop may be using it. D-025 fixes the display name as "Waka".
     */
    public static final String WINDOW_TITLE = "Waka";

    /**
     * Where the window goes when it must not be seen. The person at the keyboard studies on this
     * machine while this application is built, so every automated run parks the window off the
     * desktop and bringing it onto the screen is asked for in that session, every time (D-030).
     *
     * <p>Note what this costs, because it will be measured later: a JavaFX window parked here is
     * throttled by the window manager - S00 measured 16.35ms visible against 31.64ms hidden for
     * identical work. Frame times taken from a hidden window are junk, so S01-T29 and every S03
     * measurement has to stop and ask for the screen instead of quietly measuring nothing.
     */
    static final double OFFSCREEN_X = -4000;
    static final double OFFSCREEN_Y = -4000;

    /** Set by {@code -Dwaka.visible=true}, and by nothing else. */
    static final String VISIBLE_PROPERTY = "waka.visible";

    /**
     * Set by {@code -Dwaka.mica=false} to start with the opaque theme. A real setting with a
     * stored value arrives in S01-T24; this exists so the half of exit test 2 that says the app
     * must look finished with Mica off can be looked at now.
     */
    static final String MICA_PROPERTY = "waka.mica";

    private static final double INITIAL_WIDTH = 1280;
    private static final double INITIAL_HEIGHT = 800;

    private final ThemeService theme = new ThemeService();
    private final BooleanProperty micaEnabled = new SimpleBooleanProperty(this, "micaEnabled", true);

    private Stage stage;
    private Region root;
    private MicaWindow.Report lastMaterialReport;

    public static void main(String[] arguments) {
        launch(WakaApplication.class, arguments);
    }

    /**
     * Whether this run is allowed to put a window on the visible screen. False unless
     * {@code -Dwaka.visible=true} was passed, which is the default that matters: a test run, a
     * build on this machine and a forgotten flag all land on "stay off the desktop".
     */
    public static boolean isVisibleRunAllowed() {
        return Boolean.getBoolean(VISIBLE_PROPERTY);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        micaEnabled.set(!"false".equalsIgnoreCase(System.getProperty(MICA_PROPERTY)));

        stage.initStyle(StageStyle.UNIFIED);
        stage.setTitle(WINDOW_TITLE);

        root = createPlaceholderRoot();
        Scene scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);

        // Transparent at the scene level in both materials. Whether the window ends up showing
        // the Mica backdrop or an opaque fill is decided by waka.css, so that DESIGN.md's colour
        // values live in exactly one file.
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(stylesheet());

        stage.setScene(scene);
        placeWindow(stage);

        // The appearance has to be re-applied, not just set: the theme can change while the
        // application runs, either because the user picked a mode or because Windows changed
        // underneath an AUTO mode.
        theme.darkProperty().addListener((property, wasDark, isDark) -> refreshAppearance(stage, root));
        micaEnabled.addListener((property, wasEnabled, isEnabled) -> refreshAppearance(stage, root));

        // Before show(), not after. show() is when JavaFX first applies the stylesheet, and a
        // root with no theme class on it has no -waka-* tokens defined: every rule that refers
        // to one fails with "String cannot be cast to Paint", is logged as a warning and
        // swallowed, and the window is painted once with nothing styled. Doing it here means the
        // first frame is already correct.
        applyStylesheet(dark());

        stage.show();

        // Only now does a native window handle exist. Everything before show() can only fail.
        applyWindowMaterial(stage);
        theme.followWindowsWhileFocused(stage);
    }

    /** Auto, light or dark, and whether Mica is on. The Settings screen in S01-T25 binds here. */
    public ThemeService themeService() {
        return theme;
    }

    public BooleanProperty micaEnabledProperty() {
        return micaEnabled;
    }

    /** The one window. Null until {@link #start(Stage)} has run. */
    public Stage stage() {
        return stage;
    }

    /** The scene root, which carries the theme and material style classes. */
    public Region root() {
        return root;
    }

    /**
     * What the window manager answered the last time the material was applied. Null before the
     * window is shown. Kept because "Mica is not showing" has four possible causes, and a
     * recorded HRESULT is the difference between reading one number and guessing for an hour.
     */
    public MicaWindow.Report lastMaterialReport() {
        return lastMaterialReport;
    }

    /**
     * Put the AtlantaFX base theme, this application's own stylesheet, the root's style classes
     * and the native window material into agreement. Called on every change rather than on
     * startup only, because all four have to move together: a dark palette under a light caption
     * is the kind of half-applied theme that looks like a bug in the window manager.
     */
    private void refreshAppearance(Stage stage, Region root) {
        applyStylesheet(theme.isDark());
        if (stage.isShowing()) {
            applyWindowMaterial(stage);
        }
    }

    /**
     * The half that can run before the window exists: which AtlantaFX theme is underneath, and
     * which palette {@code waka.css} should pick.
     */
    private void applyStylesheet(boolean dark) {
        // AtlantaFX arrives as the user-agent stylesheet, which JavaFX ranks below every scene
        // stylesheet. That is what lets waka.css override the whole -color-* palette without
        // fighting specificity. Primer is the most neutral of the seven themes AtlantaFX ships,
        // and every graded colour and radius is overridden anyway.
        Application.setUserAgentStylesheet(dark
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());

        root.getStyleClass().removeAll("theme-dark", "theme-light", "mica-on", "mica-off");
        root.getStyleClass().addAll(dark ? "theme-dark" : "theme-light",
                micaEnabled.get() ? "mica-on" : "mica-off");
    }

    /** The half that needs a native window handle, so it can only run after {@code show()}. */
    private void applyWindowMaterial(Stage stage) {
        lastMaterialReport = MicaWindow.apply(stage, WINDOW_TITLE, theme.isDark(), micaEnabled.get());
        if (!lastMaterialReport.everyCallSucceeded()) {
            // Not fatal: a window with no backdrop is still a usable window, and on a Windows 10
            // machine this is the expected answer rather than a failure.
            System.out.println(lastMaterialReport.describe());
        }
    }

    private boolean dark() {
        return theme.isDark();
    }

    /**
     * Centre the window, or park it off the desktop when this run is not allowed to be seen.
     * Centring is done by hand because the window is positioned before {@code show()}, and a
     * stage that has never been shown has no width to centre.
     */
    private static void placeWindow(Stage stage) {
        if (!isVisibleRunAllowed()) {
            stage.setX(OFFSCREEN_X);
            stage.setY(OFFSCREEN_Y);
            return;
        }
        Rectangle2D visible = Screen.getPrimary().getVisualBounds();
        stage.setX(visible.getMinX() + (visible.getWidth() - INITIAL_WIDTH) / 2);
        stage.setY(visible.getMinY() + (visible.getHeight() - INITIAL_HEIGHT) / 2);
    }

    private static String stylesheet() {
        return WakaApplication.class.getResource("waka.css").toExternalForm();
    }

    /**
     * A placeholder, deleted by S01-T13. Four surfaces, no text: the window's own material
     * behind a card, a control fill inside the card, one structural divider, and the accent bar.
     * Between them they show every colour decision that differs across the four combinations of
     * light/dark and Mica on/off, which is what has to be graded before the shell is framed.
     */
    private static Region createPlaceholderRoot() {
        Region accentBar = new Region();
        accentBar.getStyleClass().add("waka-placeholder-accent");
        accentBar.setMinHeight(Geometry.ACCENT_BAR_THICKNESS);
        accentBar.setMaxHeight(Geometry.ACCENT_BAR_THICKNESS);
        accentBar.setMaxWidth(Geometry.COMMAND_PALETTE_WIDTH);

        Region divider = new Region();
        divider.getStyleClass().add("waka-placeholder-divider");
        divider.setMinHeight(1);
        divider.setMaxHeight(1);
        divider.setMaxWidth(Geometry.COMMAND_PALETTE_WIDTH);

        Region control = new Region();
        control.getStyleClass().add("waka-placeholder-control");
        control.setMinSize(Geometry.SIDE_PANEL_WIDTH, Geometry.TOOLBAR_CONTROL_HEIGHT);
        control.setMaxSize(Geometry.SIDE_PANEL_WIDTH, Geometry.TOOLBAR_CONTROL_HEIGHT);

        StackPane card = new StackPane(control, divider, accentBar);
        card.getStyleClass().add("waka-placeholder-card");
        card.setMaxSize(Geometry.COMMAND_PALETTE_WIDTH, Geometry.BOTTOM_PANEL_HEIGHT);
        StackPane.setAlignment(control, Pos.CENTER);
        StackPane.setAlignment(divider, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(accentBar, Pos.TOP_CENTER);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("waka-root");
        return root;
    }
}
