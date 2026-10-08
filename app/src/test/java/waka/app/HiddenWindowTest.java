package waka.app;

import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The guard. S01-T12: if anything in this project's test harness puts a window on the visible
 * screen, the build stops here and says so.
 *
 * <p>The rule it enforces: the person at the keyboard studies on this machine while the
 * application is being built, so a test run must never seize the screen, and bringing a window
 * onto it is asked for in that session, every time (D-030). A rule like that cannot live only in
 * a memory file, because the session that breaks it will be the one that did not read the file.
 * It lives here, where the build checks it.
 *
 * <p>It checks every window JavaFX has open, not just Waka's. That is not thoroughness for its
 * own sake: the first thing this test caught was TestFX's own primary stage, which
 * {@code FxToolkit.registerPrimaryStage()} shows, and which no amount of care about our own
 * window would have found.
 *
 * <p>There is one authorised way through: passing {@code -Dwaka.visible=true}, which S01-T29 and
 * every S03 performance measurement need, because a hidden window is throttled and its frame
 * times are meaningless. That path does not fail - it prints a banner and the test is skipped, so
 * a visible run shows up in the surefire report rather than happening quietly. An accidental
 * visible window, with no flag, fails the build.
 */
class HiddenWindowTest extends OffScreenApplicationTest {

    @Test
    @DisplayName("the default is hidden, whatever anyone later edits")
    void visibleRunsAreOffByDefault() {
        // Checked without looking at a window, so it still protects the rule on the day the
        // positioning code is rewritten: nothing but the flag may turn a visible run on.
        Assumptions.assumeFalse(Boolean.getBoolean(WakaApplication.VISIBLE_PROPERTY),
                "-Dwaka.visible=true was passed, so the default cannot be observed in this run");
        assertFalse(WakaApplication.isVisibleRunAllowed(),
                "with " + WakaApplication.VISIBLE_PROPERTY + " unset, no run may be visible. "
                + "If this fails, the default was flipped in code, and every future test run on "
                + "this machine will take over the screen.");
    }

    @Test
    @DisplayName("no window JavaFX has open is on any screen")
    void nothingIsAnywhereAnyoneCanSeeIt() throws Exception {
        if (Boolean.getBoolean(WakaApplication.VISIBLE_PROPERTY)) {
            System.out.println("""
                    ====================================================================
                    waka.visible=true : this test run is putting a REAL window on the
                    screen. That is only correct if the person at the keyboard was asked
                    in this session and said yes. If you are reading this in a build log
                    and nobody was asked, the flag is in a pom, a launch config or a
                    script where it does not belong. See D-030.
                    ====================================================================""");
            Assumptions.abort("a visible run was explicitly requested with -D"
                    + WakaApplication.VISIBLE_PROPERTY + "=true");
        }

        List<String> onScreen = onFxThread(HiddenWindowTest::describeWindowsOnScreen);

        assertTrue(onScreen.isEmpty(),
                "a test run has put " + onScreen.size() + " window(s) on screen: " + onScreen
                + ". That is what D-030 forbids - this machine is being studied on while the "
                + "application is built. Park the window at " + WakaApplication.OFFSCREEN_X + ","
                + WakaApplication.OFFSCREEN_Y + ", or hide it if it is a window nothing uses.");
    }

    @Test
    @DisplayName("the guard says yes to a window on the screen and no to one parked off it")
    void theGuardItselfWorks() throws Exception {
        Rectangle2D primaryScreen = onFxThread(() -> Screen.getPrimary().getBounds());

        Rectangle2D rightOnTheScreen = new Rectangle2D(
                primaryScreen.getMinX(), primaryScreen.getMinY(), 400, 300);
        assertTrue(onFxThread(() -> overlapsAScreen(rightOnTheScreen)),
                "the guard did not notice a window sitting at the top left of the primary screen, "
                + "so every run of the test above was passing for the wrong reason");

        Rectangle2D whereTheHarnessParks = new Rectangle2D(
                WakaApplication.OFFSCREEN_X, WakaApplication.OFFSCREEN_Y, 1280, 800);
        assertFalse(onFxThread(() -> overlapsAScreen(whereTheHarnessParks)),
                "the harness parks at " + WakaApplication.OFFSCREEN_X + ","
                + WakaApplication.OFFSCREEN_Y + " and the guard thinks that is on a screen, "
                + "which would fail every build on this machine");
    }

    /**
     * Every showing window that overlaps a screen, described well enough to identify which one.
     * Must run on the JavaFX application thread.
     */
    private static List<String> describeWindowsOnScreen() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(HiddenWindowTest::overlapsAScreen)
                .map(HiddenWindowTest::describe)
                .toList();
    }

    private static boolean overlapsAScreen(Window window) {
        if (Double.isNaN(window.getX()) || Double.isNaN(window.getY())) {
            // A showing window with no position yet cannot be judged, and reporting it as an
            // offender would be a false alarm every time a test starts.
            return false;
        }
        return overlapsAScreen(new Rectangle2D(
                window.getX(), window.getY(), window.getWidth(), window.getHeight()));
    }

    /**
     * Separate from the window it is asked about, so that {@link #theGuardItselfWorks()} can
     * prove this answers yes for a window on the screen. A guard that has only ever returned
     * "nothing to report" has not been shown to work, and proving it the other way round would
     * mean putting a real window on the screen - which is the thing it exists to prevent.
     */
    static boolean overlapsAScreen(Rectangle2D bounds) {
        // Screen.getBounds(), not getVisualBounds(): the visual bounds exclude the taskbar, and a
        // window hiding behind the taskbar is still a window on the screen.
        return Screen.getScreens().stream().anyMatch(screen -> screen.getBounds().intersects(bounds));
    }

    private static String describe(Window window) {
        String title = window instanceof Stage stage ? stage.getTitle() : null;
        return "%s(title=%s) at %.0f,%.0f sized %.0fx%.0f".formatted(
                window.getClass().getSimpleName(),
                title == null || title.isEmpty() ? "<none>" : title,
                window.getX(), window.getY(), window.getWidth(), window.getHeight());
    }
}
