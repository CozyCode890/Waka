package waka.app;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

/**
 * Which of the two themes is in effect, and why.
 *
 * <p>Three modes, and {@link Mode#AUTO} is the default the user graded: follow Windows. Auto
 * reads {@code AppsUseLightTheme} under
 * {@code HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize} when the application
 * starts, and reads it again every time the window takes focus.
 *
 * <p>Focus, rather than a native change notification. Windows announces a theme change by
 * posting {@code WM_SETTINGCHANGE} to every top-level window, and receiving it would mean
 * subclassing the window procedure - replacing the one JavaFX installed, for a signal that only
 * matters while someone is looking at this window. Someone who changes their Windows theme
 * returns to this window afterwards, and that return is the focus event. The cost of being wrong
 * is that the window stays in the old theme until it is clicked.
 */
public final class ThemeService {

    /** What the user can choose. AUTO is the default (DESIGN.md §Ledger, "theme"). */
    public enum Mode {
        AUTO, LIGHT, DARK
    }

    private static final String PERSONALIZE_KEY =
            "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
    private static final String APPS_USE_LIGHT_THEME = "AppsUseLightTheme";

    /** HKEY_CURRENT_USER, which Win32 defines as a handle with this fixed value. */
    private static final long HKEY_CURRENT_USER = 0x80000001L;
    /** RRF_RT_REG_DWORD: fail rather than convert if the value is not a DWORD. */
    private static final int RESTRICT_TO_DWORD = 0x00000010;
    private static final int ERROR_SUCCESS = 0;

    private static final MethodHandle REG_GET_VALUE = WindowsApi.downcall(
            "advapi32.dll", "RegGetValueW",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,     // LSTATUS
                    ValueLayout.ADDRESS,                    // HKEY hkey
                    ValueLayout.ADDRESS,                    // LPCWSTR lpSubKey
                    ValueLayout.ADDRESS,                    // LPCWSTR lpValue
                    ValueLayout.JAVA_INT,                   // DWORD dwFlags
                    ValueLayout.ADDRESS,                    // LPDWORD pdwType
                    ValueLayout.ADDRESS,                    // PVOID pvData
                    ValueLayout.ADDRESS));                  // LPDWORD pcbData

    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(this, "mode", Mode.AUTO);
    private final ReadOnlyBooleanWrapper dark = new ReadOnlyBooleanWrapper(this, "dark", false);

    public ThemeService() {
        mode.addListener((property, oldMode, newMode) -> recomputeEffectiveTheme());
        recomputeEffectiveTheme();
    }

    /** Auto, light or dark. Writable: this is what the Settings screen binds to in S01-T25. */
    public ObjectProperty<Mode> modeProperty() {
        return mode;
    }

    public Mode getMode() {
        return mode.get();
    }

    public void setMode(Mode newMode) {
        mode.set(newMode);
    }

    /**
     * Whether the dark theme is in effect right now. This is the value every screen should bind
     * to: it already accounts for the mode, so no caller has to ask "and what did Windows say".
     */
    public ReadOnlyBooleanProperty darkProperty() {
        return dark.getReadOnlyProperty();
    }

    public boolean isDark() {
        return dark.get();
    }

    /**
     * Re-read Windows whenever this stage takes focus, which is how a theme change made outside
     * this application arrives. Does nothing while the mode is LIGHT or DARK, because then the
     * user has overridden Windows and the registry is not the answer any more.
     */
    public void followWindowsWhileFocused(Stage stage) {
        stage.focusedProperty().addListener((property, wasFocused, isFocused) -> {
            if (isFocused) {
                recomputeEffectiveTheme();
            }
        });
    }

    private void recomputeEffectiveTheme() {
        dark.set(switch (mode.get()) {
            case LIGHT -> false;
            case DARK -> true;
            case AUTO -> windowsPrefersDarkApps();
        });
    }

    /**
     * Reads {@code AppsUseLightTheme}, where 1 means light and 0 means dark.
     *
     * <p>A missing value means light: the key is absent on a Windows install that has never been
     * switched to dark, and light is what such a machine is showing. A read that fails for any
     * other reason also answers light rather than throwing, because a theme is a finish and
     * should never be the reason an application will not start.
     */
    private static boolean windowsPrefersDarkApps() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment subKey = arena.allocateFrom(PERSONALIZE_KEY, StandardCharsets.UTF_16LE);
            MemorySegment valueName = arena.allocateFrom(APPS_USE_LIGHT_THEME, StandardCharsets.UTF_16LE);
            MemorySegment value = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment valueSize = arena.allocate(ValueLayout.JAVA_INT);
            valueSize.set(ValueLayout.JAVA_INT, 0, (int) ValueLayout.JAVA_INT.byteSize());

            int status = (int) REG_GET_VALUE.invokeExact(
                    MemorySegment.ofAddress(HKEY_CURRENT_USER),
                    subKey,
                    valueName,
                    RESTRICT_TO_DWORD,
                    MemorySegment.NULL,
                    value,
                    valueSize);
            if (status != ERROR_SUCCESS) {
                return false;
            }
            return value.get(ValueLayout.JAVA_INT, 0) == 0;
        } catch (Throwable readFailed) {
            System.out.println("theme | could not read " + APPS_USE_LIGHT_THEME
                    + ", assuming light: " + readFailed);
            return false;
        }
    }
}
