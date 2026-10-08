package waka.app;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javafx.stage.StageStyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S01-T11: the harness, put to work on the window S01-T05 to T08 build.
 *
 * <p>These are not screenshots and cannot be. What they check is everything about the window that
 * is a fact rather than a judgement: that the stage is the style Mica requires, that the window
 * manager accepted all four DWM calls, that the four combinations of theme and material each
 * reach both the stylesheet and the native window, and that the registry read behind AUTO mode
 * agrees with what Windows' own tool reports.
 *
 * <p>Whether the result looks right is S01-T29's question, and it needs eyes and permission.
 */
class WakaApplicationTest extends OffScreenApplicationTest {

    @Test
    @DisplayName("the stage is UNIFIED, titled, showing, and parked off the desktop")
    void theWindowIsTheOneMicaNeeds() throws Exception {
        assertEquals(StageStyle.UNIFIED, onFxThread(() -> stage().getStyle()),
                "Mica needs UNIFIED or TRANSPARENT, because GlassScene.getClearColor() is an "
                + "opaque white for every other style; TRANSPARENT forces the slow uploading "
                + "painter, so UNIFIED is the only option left. See D-014.");

        assertEquals(WakaApplication.WINDOW_TITLE, onFxThread(() -> stage().getTitle()),
                "the title is how MicaWindow finds the native handle, so it is load-bearing "
                + "rather than decoration. See D-034.");

        assertTrue(onFxThread(() -> stage().isShowing()), "the window should be showing");

        assertEquals(WakaApplication.OFFSCREEN_X, onFxThread(() -> stage().getX()));
        assertEquals(WakaApplication.OFFSCREEN_Y, onFxThread(() -> stage().getY()));
    }

    @Test
    @DisplayName("the window manager accepted every DWM call")
    void theMaterialWasApplied() {
        MicaWindow.Report report = application.lastMaterialReport();
        assertNotNull(report, "the material should have been applied once the window was shown");
        assertNotEquals(0L, report.windowHandle(),
                "FindWindowW found no window titled " + WakaApplication.WINDOW_TITLE);
        assertTrue(report.everyCallSucceeded(),
                "a DWM call was refused: " + report.describe() + ". On Windows 11 build 22621 or "
                + "later all four return S_OK. On Windows 10 the backdrop attribute is refused, "
                + "which is expected there and is why S06's exit test says the application must "
                + "look finished with no Mica.");
    }

    @Test
    @DisplayName("all four combinations of theme and material reach the window")
    void everyCombinationIsApplied() throws Exception {
        for (boolean dark : List.of(false, true)) {
            for (boolean mica : List.of(true, false)) {
                onFxThread(() -> {
                    application.themeService().setMode(
                            dark ? ThemeService.Mode.DARK : ThemeService.Mode.LIGHT);
                    application.micaEnabledProperty().set(mica);
                });

                String combination = (dark ? "dark" : "light") + " with mica "
                        + (mica ? "on" : "off");
                List<String> styleClasses = onFxThread(
                        () -> List.copyOf(application.root().getStyleClass()));

                assertTrue(styleClasses.contains(dark ? "theme-dark" : "theme-light"),
                        combination + ": the root carries " + styleClasses
                        + ", so waka.css cannot pick the right palette");
                assertTrue(styleClasses.contains(mica ? "mica-on" : "mica-off"),
                        combination + ": the root carries " + styleClasses
                        + ", so waka.css cannot pick the right layer fills (G-016)");

                MicaWindow.Report report = application.lastMaterialReport();
                assertEquals(mica, report.micaRequested(),
                        combination + ": the stylesheet and the native window disagree about the "
                        + "material, which is how a window ends up with a dark palette under a "
                        + "light caption");
                assertTrue(report.everyCallSucceeded(),
                        combination + ": " + report.describe());
            }
        }
    }

    @Test
    @DisplayName("AUTO mode reads the same registry value that reg.exe reports")
    void autoModeAgreesWithWindows() throws Exception {
        Boolean windowsSaysLight = readAppsUseLightThemeWithRegExe();
        Assumptions.assumeTrue(windowsSaysLight != null,
                "AppsUseLightTheme is not set on this machine, so there is nothing to compare "
                + "against; ThemeService treats that as light, which is what such a machine shows");

        onFxThread(() -> application.themeService().setMode(ThemeService.Mode.AUTO));

        assertEquals(!windowsSaysLight, application.themeService().isDark(),
                "ThemeService reads AppsUseLightTheme through RegGetValueW and reg.exe reads it "
                + "through the same registry; if they disagree, the FFM call is reading the "
                + "wrong key, the wrong value, or the wrong number of bytes");
    }

    @Test
    @DisplayName("LIGHT and DARK override Windows rather than consulting it")
    void explicitModesIgnoreTheRegistry() throws Exception {
        onFxThread(() -> application.themeService().setMode(ThemeService.Mode.DARK));
        assertTrue(application.themeService().isDark(), "DARK mode should be dark");

        onFxThread(() -> application.themeService().setMode(ThemeService.Mode.LIGHT));
        assertTrue(!application.themeService().isDark(), "LIGHT mode should be light");
    }

    /**
     * What Windows' own tool says {@code AppsUseLightTheme} is: TRUE for light, FALSE for dark,
     * null when the value does not exist.
     *
     * <p>An independent reading on purpose. ThemeService calls {@code RegGetValueW} through
     * {@code java.lang.foreign}, and a hand-written native call that reads the wrong offset tends
     * to return a plausible number rather than an error. Comparing it against a different
     * implementation is the only cheap way to notice.
     */
    private static Boolean readAppsUseLightThemeWithRegExe() throws Exception {
        Process regExe = new ProcessBuilder("reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme")
                .redirectErrorStream(true)
                .start();
        String output = new String(regExe.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!regExe.waitFor(20, TimeUnit.SECONDS) || regExe.exitValue() != 0) {
            return null;
        }

        Matcher value = Pattern.compile("REG_DWORD\\s+0x([0-9a-fA-F]+)").matcher(output);
        if (!value.find()) {
            return null;
        }
        return Integer.parseInt(value.group(1), 16) != 0;
    }
}
