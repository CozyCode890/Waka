package waka.spike;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import javafx.stage.Stage;

/**
 * Probe A, native half: switch a JavaFX stage over to the Windows 11 Mica backdrop.
 *
 * <p>Everything here goes through {@code java.lang.foreign} on purpose. No JNI, because that
 * would mean shipping a compiled stub library, and no JNA, because that is a third dependency
 * for three function calls.
 *
 * <p>The native window handle is read two ways and both are reported, because they fail
 * differently. The glass route needs
 * {@code --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED}; the FindWindowW route needs
 * only a window title that nothing else on the desktop shares. If S01 finds that the export costs
 * something in the jpackage image, the second route is already proven and can replace the first.
 */
public final class MicaWindow {

    /** DwmSetWindowAttribute, DWMWA_USE_IMMERSIVE_DARK_MODE. Takes a BOOL. */
    private static final int USE_IMMERSIVE_DARK_MODE = 20;
    /** DwmSetWindowAttribute, DWMWA_CAPTION_COLOR. Takes a COLORREF. */
    private static final int CAPTION_COLOR = 35;
    /** DwmSetWindowAttribute, DWMWA_SYSTEMBACKDROP_TYPE. Windows 11 build 22621 and later. */
    private static final int SYSTEM_BACKDROP_TYPE = 38;

    /** DWMSBT_MAINWINDOW, the backdrop Microsoft calls Mica. */
    private static final int BACKDROP_MICA = 2;

    /** DESIGN.md: the opaque base the Mica tint sits on, dark then light. */
    private static final int CAPTION_DARK = 0x202020;
    private static final int CAPTION_LIGHT = 0xF3F3F3;

    private static final Linker LINKER = Linker.nativeLinker();

    private static final MethodHandle DWM_SET_WINDOW_ATTRIBUTE = downcall(
            "dwmapi.dll", "DwmSetWindowAttribute",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,     // HRESULT
                    ValueLayout.ADDRESS,                    // HWND hwnd
                    ValueLayout.JAVA_INT,                   // DWORD dwAttribute
                    ValueLayout.ADDRESS,                    // LPCVOID pvAttribute
                    ValueLayout.JAVA_INT));                 // DWORD cbAttribute

    private static final MethodHandle DWM_EXTEND_FRAME_INTO_CLIENT_AREA = downcall(
            "dwmapi.dll", "DwmExtendFrameIntoClientArea",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,     // HRESULT
                    ValueLayout.ADDRESS,                    // HWND hwnd
                    ValueLayout.ADDRESS));                  // const MARGINS *pMarInset

    private static final MethodHandle FIND_WINDOW = downcall(
            "user32.dll", "FindWindowW",
            FunctionDescriptor.of(ValueLayout.ADDRESS,      // HWND
                    ValueLayout.ADDRESS,                    // LPCWSTR lpClassName
                    ValueLayout.ADDRESS));                  // LPCWSTR lpWindowName

    private MicaWindow() {
    }

    /** What the four DWM calls answered, and which route found the window. */
    public record Report(long handleFromGlass,
                         long handleFromFindWindow,
                         int frameExtensionResult,
                         int backdropResult,
                         int darkModeResult,
                         int captionColourResult) {

        /** DWM returns S_OK, which is zero, when it accepted the attribute. */
        public boolean everyCallSucceeded() {
            return frameExtensionResult == 0 && backdropResult == 0
                    && darkModeResult == 0 && captionColourResult == 0;
        }

        /** True when both routes to the handle agreed, which is the result S01 wants to know. */
        public boolean bothRoutesAgreed() {
            return handleFromGlass != 0 && handleFromGlass == handleFromFindWindow;
        }

        public String describe() {
            return String.format(
                    "mica | handle glass=0x%X findWindow=0x%X routes agree=%b%n"
                    + "mica | extendFrame=0x%08X backdrop=0x%08X darkMode=0x%08X "
                    + "captionColour=0x%08X | accepted=%b",
                    handleFromGlass, handleFromFindWindow, bothRoutesAgreed(),
                    frameExtensionResult, backdropResult, darkModeResult, captionColourResult,
                    everyCallSucceeded());
        }
    }

    /**
     * Turn Mica on for a stage that is already showing. The handle does not exist before
     * {@link Stage#show()}, so calling this earlier can only fail.
     *
     * @param windowTitle the stage's title, used to find the window by name as a cross-check
     * @param dark        whether the caption should be drawn in the dark theme
     */
    public static Report applyMica(Stage stage, String windowTitle, boolean dark) {
        if (!stage.isShowing()) {
            throw new IllegalStateException("the stage must be showing before Mica is applied");
        }

        long handleFromGlass = handleThroughGlass(windowTitle);
        long handleFromFindWindow = handleThroughFindWindow(windowTitle);
        long handle = handleFromGlass != 0 ? handleFromGlass : handleFromFindWindow;
        if (handle == 0) {
            throw new IllegalStateException("neither route found a window titled " + windowTitle);
        }

        try (Arena arena = Arena.ofConfined()) {
            // The backdrop is drawn by the window manager in the part of the window the frame
            // covers. Without this call that part is the title bar only, so the attribute below
            // is accepted and then has almost nowhere to paint: the caption recolours and the
            // body stays opaque, which is exactly what the first run of this probe looked like.
            int frameExtension = extendFrameOverWholeWindow(arena, handle);
            int backdrop = setIntegerAttribute(arena, handle, SYSTEM_BACKDROP_TYPE, BACKDROP_MICA);
            int darkMode = setIntegerAttribute(arena, handle, USE_IMMERSIVE_DARK_MODE, dark ? 1 : 0);
            int caption = setIntegerAttribute(arena, handle, CAPTION_COLOR,
                    toColorRef(dark ? CAPTION_DARK : CAPTION_LIGHT));
            return new Report(handleFromGlass, handleFromFindWindow,
                    frameExtension, backdrop, darkMode, caption);
        }
    }

    /**
     * DwmExtendFrameIntoClientArea with every margin set to -1, which Win32 calls the sheet of
     * glass: the frame covers the whole window, so the backdrop has the whole window to paint.
     */
    private static int extendFrameOverWholeWindow(Arena arena, long windowHandle) {
        // MARGINS is four LONGs: left, right, top, bottom.
        MemorySegment margins = arena.allocate(ValueLayout.JAVA_INT, 4);
        for (int edge = 0; edge < 4; edge++) {
            margins.setAtIndex(ValueLayout.JAVA_INT, edge, -1);
        }
        try {
            return (int) DWM_EXTEND_FRAME_INTO_CLIENT_AREA.invokeExact(
                    MemorySegment.ofAddress(windowHandle), margins);
        } catch (Throwable callFailed) {
            throw new IllegalStateException("DwmExtendFrameIntoClientArea failed", callFailed);
        }
    }

    /**
     * The privileged route. This is the only method that touches a com.sun package, so the
     * fallback below stays genuinely reachable: without the export, the reference fails here
     * and nowhere else.
     */
    private static long handleThroughGlass(String windowTitle) {
        try {
            for (com.sun.glass.ui.Window window : com.sun.glass.ui.Window.getWindows()) {
                if (windowTitle.equals(window.getTitle())) {
                    return window.getNativeWindow();
                }
            }
            return 0L;
        } catch (Throwable glassUnavailable) {
            System.out.println("mica | glass route unavailable, falling back: " + glassUnavailable);
            return 0L;
        }
    }

    /** The unprivileged route. Needs no JVM flags, only a title nothing else is using. */
    private static long handleThroughFindWindow(String windowTitle) {
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

    private static MethodHandle downcall(String library, String function, FunctionDescriptor shape) {
        SymbolLookup lookup = SymbolLookup.libraryLookup(library, Arena.global());
        MemorySegment address = lookup.find(function)
                .orElseThrow(() -> new IllegalStateException(function + " is missing from " + library));
        return LINKER.downcallHandle(address, shape);
    }
}
