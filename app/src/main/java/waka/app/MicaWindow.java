package waka.app;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import javafx.stage.Stage;

/**
 * The window's material: the Windows 11 Mica backdrop, and the native caption recoloured to
 * match the theme.
 *
 * <p>Lifted from {@code spike/MicaWindow.java}, which S00 used to prove this works on this
 * machine, with one deliberate change: the privileged route to the window handle is gone. S00
 * proved that reading the handle out of {@code com.sun.glass.ui.Window} and finding it with
 * {@code FindWindowW} return the same value, so the {@code --add-exports} the glass route needs
 * bought nothing and would have had to be repeated in every Maven, IDE, jlink and jpackage
 * launch configuration for the life of the project. See D-034.
 *
 * <p>Two things about the call order are not preferences and will break the window if changed:
 *
 * <ul>
 *   <li>{@code DwmExtendFrameIntoClientArea} must be called FIRST, with every margin at -1.
 *       Without it the backdrop attribute below is accepted, returns S_OK, and then has almost
 *       nowhere to paint: the caption recolours and the body stays opaque. That was exactly what
 *       S00's first run looked like.</li>
 *   <li>The scene's fill has to be transparent for Mica to show through. A root that paints an
 *       opaque background hides the backdrop just as effectively as not asking for it.</li>
 * </ul>
 */
public final class MicaWindow {

    /** DwmSetWindowAttribute, DWMWA_USE_IMMERSIVE_DARK_MODE. Takes a BOOL. */
    private static final int USE_IMMERSIVE_DARK_MODE = 20;
    /** DwmSetWindowAttribute, DWMWA_CAPTION_COLOR. Takes a COLORREF. */
    private static final int CAPTION_COLOR = 35;
    /** DwmSetWindowAttribute, DWMWA_SYSTEMBACKDROP_TYPE. Windows 11 build 22621 and later. */
    private static final int SYSTEM_BACKDROP_TYPE = 38;

    /** DWMSBT_NONE: no system backdrop. The window is whatever the scene paints. */
    private static final int BACKDROP_NONE = 1;
    /** DWMSBT_MAINWINDOW: the backdrop Microsoft calls Mica. */
    private static final int BACKDROP_MICA = 2;

    /** DESIGN.md §Colour {@code --mica}: the opaque base the Mica tint sits on. */
    private static final int CAPTION_DARK = 0x202020;
    private static final int CAPTION_LIGHT = 0xF3F3F3;

    private static final MethodHandle DWM_SET_WINDOW_ATTRIBUTE = WindowsApi.downcall(
            "dwmapi.dll", "DwmSetWindowAttribute",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,     // HRESULT
                    ValueLayout.ADDRESS,                    // HWND hwnd
                    ValueLayout.JAVA_INT,                   // DWORD dwAttribute
                    ValueLayout.ADDRESS,                    // LPCVOID pvAttribute
                    ValueLayout.JAVA_INT));                 // DWORD cbAttribute

    private static final MethodHandle DWM_EXTEND_FRAME_INTO_CLIENT_AREA = WindowsApi.downcall(
            "dwmapi.dll", "DwmExtendFrameIntoClientArea",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,     // HRESULT
                    ValueLayout.ADDRESS,                    // HWND hwnd
                    ValueLayout.ADDRESS));                  // const MARGINS *pMarInset

    private static final MethodHandle FIND_WINDOW = WindowsApi.downcall(
            "user32.dll", "FindWindowW",
            FunctionDescriptor.of(ValueLayout.ADDRESS,      // HWND
                    ValueLayout.ADDRESS,                    // LPCWSTR lpClassName
                    ValueLayout.ADDRESS));                  // LPCWSTR lpWindowName

    private MicaWindow() {
    }

    /**
     * What the four DWM calls answered. Kept as a value rather than thrown away, because the
     * harness asserts on it and because "Mica is not showing" has four possible causes and
     * guessing between them wastes an afternoon.
     */
    public record Report(long windowHandle,
                         boolean micaRequested,
                         int frameExtensionResult,
                         int backdropResult,
                         int darkModeResult,
                         int captionColourResult) {

        /** DWM returns S_OK, which is zero, when it accepted the attribute. */
        public boolean everyCallSucceeded() {
            return frameExtensionResult == 0 && backdropResult == 0
                    && darkModeResult == 0 && captionColourResult == 0;
        }

        public String describe() {
            return String.format(
                    "window | handle=0x%X mica=%b | extendFrame=0x%08X backdrop=0x%08X "
                    + "darkMode=0x%08X captionColour=0x%08X | accepted=%b",
                    windowHandle, micaRequested, frameExtensionResult, backdropResult,
                    darkModeResult, captionColourResult, everyCallSucceeded());
        }
    }

    /**
     * Set the window's material and recolour its caption. The window handle does not exist
     * before {@link Stage#show()}, so calling this earlier can only fail.
     *
     * @param stage        the stage, already showing
     * @param windowTitle  the stage's title; the handle is found by it, so nothing else on the
     *                     desktop may be using the same title
     * @param dark         whether the dark theme is in effect, which decides the caption colour
     * @param micaEnabled  true for the Mica backdrop; false for an opaque window, which is the
     *                     setting that has to look finished on its own (G-016)
     */
    public static Report apply(Stage stage, String windowTitle, boolean dark, boolean micaEnabled) {
        if (!stage.isShowing()) {
            throw new IllegalStateException("the stage must be showing before Mica is applied");
        }

        long handle = findWindowByTitle(windowTitle);
        if (handle == 0) {
            throw new IllegalStateException("no window found titled " + windowTitle);
        }

        try (Arena arena = Arena.ofConfined()) {
            // Both directions matter, because Mica is a setting the user can turn off while the
            // app is running. Turning it off puts the margins back to zero: leaving the frame
            // extended over a window with no backdrop leaves the window manager painting nothing
            // in the extended area.
            int frameExtension = extendFrame(arena, handle, micaEnabled ? -1 : 0);
            int backdrop = setIntegerAttribute(arena, handle, SYSTEM_BACKDROP_TYPE,
                    micaEnabled ? BACKDROP_MICA : BACKDROP_NONE);

            // Immersive dark mode is what makes the caption's own text and its close button
            // light; the caption colour alone would leave black glyphs on a dark title bar.
            int darkMode = setIntegerAttribute(arena, handle, USE_IMMERSIVE_DARK_MODE, dark ? 1 : 0);
            int caption = setIntegerAttribute(arena, handle, CAPTION_COLOR,
                    toColorRef(dark ? CAPTION_DARK : CAPTION_LIGHT));

            return new Report(handle, micaEnabled, frameExtension, backdrop, darkMode, caption);
        }
    }

    /**
     * {@code DwmExtendFrameIntoClientArea}. Every margin at -1 is what Win32 calls the sheet of
     * glass: the frame covers the whole window, so the backdrop has the whole window to paint.
     * Every margin at 0 puts the frame back to the title bar alone.
     */
    private static int extendFrame(Arena arena, long windowHandle, int everyMargin) {
        // MARGINS is four LONGs: left, right, top, bottom.
        MemorySegment margins = arena.allocate(ValueLayout.JAVA_INT, 4);
        for (int edge = 0; edge < 4; edge++) {
            margins.setAtIndex(ValueLayout.JAVA_INT, edge, everyMargin);
        }
        try {
            return (int) DWM_EXTEND_FRAME_INTO_CLIENT_AREA.invokeExact(
                    MemorySegment.ofAddress(windowHandle), margins);
        } catch (Throwable callFailed) {
            throw new IllegalStateException("DwmExtendFrameIntoClientArea failed", callFailed);
        }
    }

    /** Needs no JVM flags, only a window title nothing else is using. */
    private static long findWindowByTitle(String windowTitle) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment wideTitle = arena.allocateFrom(windowTitle, StandardCharsets.UTF_16LE);
            MemorySegment found = (MemorySegment) FIND_WINDOW.invokeExact(MemorySegment.NULL, wideTitle);
            return found.address();
        } catch (Throwable callFailed) {
            throw new IllegalStateException("FindWindowW failed", callFailed);
        }
    }

    private static int setIntegerAttribute(Arena arena, long windowHandle, int attribute, int value) {
        MemorySegment buffer = arena.allocate(ValueLayout.JAVA_INT);
        buffer.set(ValueLayout.JAVA_INT, 0, value);
        try {
            return (int) DWM_SET_WINDOW_ATTRIBUTE.invokeExact(
                    MemorySegment.ofAddress(windowHandle),
                    attribute,
                    buffer,
                    (int) ValueLayout.JAVA_INT.byteSize());
        } catch (Throwable callFailed) {
            throw new IllegalStateException("DwmSetWindowAttribute " + attribute + " failed", callFailed);
        }
    }

    /**
     * A COLORREF is 0x00BBGGRR, the reverse of the 0xRRGGBB written in DESIGN.md. Both captions
     * happen to be grey, so a wrong byte order would not show; this swaps anyway, because the
     * next colour someone tries will not be grey.
     */
    private static int toColorRef(int redGreenBlue) {
        int red = (redGreenBlue >> 16) & 0xFF;
        int green = (redGreenBlue >> 8) & 0xFF;
        int blue = redGreenBlue & 0xFF;
        return (blue << 16) | (green << 8) | red;
    }
}
