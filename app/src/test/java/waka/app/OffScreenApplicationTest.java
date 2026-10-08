package waka.app;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * The harness every GUI test in this project inherits: one real JavaFX stage, driven by TestFX,
 * parked off the visible desktop.
 *
 * <p>A real stage rather than a headless one. The headless route on JavaFX is
 * {@code org.testfx:openjfx-monocle}, whose newest release targets JDK 12 and cannot be trusted
 * on JavaFX 27 - and it could never test Mica or the native caption anyway, which are two of the
 * things this stage exists to check. So the window genuinely exists; it is simply nowhere anyone
 * can see it. See D-030.
 *
 * <p>Two consequences worth knowing before trusting a number out of a test that extends this:
 *
 * <ul>
 *   <li>A window parked off the desktop is throttled by the window manager. S00 measured 16.35ms
 *       visible against 31.64ms hidden for identical work, so frame times taken here are junk.
 *       Anything measuring performance has to ask for the screen instead.</li>
 *   <li>It launches {@link WakaApplication} itself, through {@code FxToolkit}, rather than
 *       building a stage of its own. That is on purpose: a harness that constructs its own window
 *       tests a startup path no user ever runs.</li>
 * </ul>
 *
 * <p>Two things about {@code FxToolkit} were found the hard way and are the reason the setup
 * below is not the three lines its documentation suggests:
 *
 * <ul>
 *   <li>{@code registerPrimaryStage()} boots JavaFX and SHOWS a stage of its own. Calling
 *       {@code setupApplication} straight after it hands {@link WakaApplication} a stage that is
 *       already visible, and {@code initStyle(UNIFIED)} then throws "Cannot set style once stage
 *       has been set visible" - so the test would be unable to test the only stage style Mica
 *       works on. {@code registerStage(Stage::new)} replaces the target with a fresh, unshown
 *       stage, which is what {@code start} is given.</li>
 *   <li>That stage of TestFX's own is a window on the screen, which is exactly what D-030
 *       forbids, and no amount of care about our own window would have caught it. It is parked
 *       and hidden below, and {@link HiddenWindowTest} checks every window JavaFX has open
 *       rather than only ours.</li>
 * </ul>
 */
abstract class OffScreenApplicationTest {

    /** How long a value read on the JavaFX thread may take before the test gives up. */
    private static final long FX_TIMEOUT_SECONDS = 10;

    protected WakaApplication application;

    @BeforeAll
    static void bootJavaFxWithNothingOnScreen() throws TimeoutException {
        Stage testFxOwnStage = FxToolkit.registerPrimaryStage();
        FxToolkit.setupFixture(() -> {
            // Hiding every window would otherwise end the toolkit between tests.
            Platform.setImplicitExit(false);
            testFxOwnStage.setX(WakaApplication.OFFSCREEN_X);
            testFxOwnStage.setY(WakaApplication.OFFSCREEN_Y);
            testFxOwnStage.hide();
        });
    }

    @BeforeEach
    void launchWaka() throws TimeoutException {
        // A fresh stage per test, never shown, so start() may still choose its style. Also means
        // one test cannot leave a window behind for FindWindowW to pick up in the next.
        FxToolkit.registerStage(Stage::new);
        application = (WakaApplication) FxToolkit.setupApplication(WakaApplication.class);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void closeWaka() throws TimeoutException {
        FxToolkit.cleanupApplication(application);
    }

    protected Stage stage() {
        return application.stage();
    }

    /**
     * Read or change something on the JavaFX application thread and wait for the answer.
     *
     * <p>Every scene graph and stage property belongs to that thread. Reading one from the test
     * thread mostly appears to work, which is worse than failing: it produces a value that was
     * true at some unspecified moment. Go through here instead.
     */
    protected <T> T onFxThread(Callable<T> work) throws Exception {
        T result = WaitForAsyncUtils.asyncFx(work).get(FX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        return result;
    }

    /** The same, for work with nothing to return. */
    protected void onFxThread(Runnable work) throws Exception {
        onFxThread(() -> {
            work.run();
            return null;
        });
    }
}
