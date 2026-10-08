package waka.app;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

/**
 * The one place this application binds a Win32 function.
 *
 * <p>Everything goes through {@code java.lang.foreign}. Not JNI, because that would mean
 * shipping a compiled stub library and a second build toolchain for four function calls; and not
 * JNA, because that is a third-party dependency for the same four calls.
 *
 * <p>Two classes need this: {@link MicaWindow}, for the window material, and
 * {@link ThemeService}, for whether Windows is currently in its light or dark theme. Each binds
 * its own functions and keeps its own constants - this class holds only the linking, which is
 * the part that would otherwise be copied.
 *
 * <p>Windows-only by design. PLAN's NON-goals rule out macOS and Linux builds, so there is no
 * abstraction here waiting for a second implementation: a class that calls into Windows says so
 * in its name instead.
 */
public final class WindowsApi {

    /**
     * Looked up once per process. A {@code libraryLookup} bound to {@link Arena#global()} keeps
     * the library loaded for the life of the JVM, which is what we want for dwmapi, user32 and
     * advapi32 - all three are already in the process anyway.
     */
    private static final Linker LINKER = Linker.nativeLinker();

    private WindowsApi() {
    }

    /**
     * Bind one exported function to a {@link MethodHandle}.
     *
     * <p>Call this from a {@code static final} field, so a missing export is a class
     * initialisation failure at startup rather than a surprise on the first click.
     *
     * @param library  the DLL name as the loader sees it, for example {@code "dwmapi.dll"}
     * @param function the exported symbol, which for the wide-character Win32 calls ends in W
     * @param shape    the return type and parameter types, in native terms
     */
    public static MethodHandle downcall(String library, String function, FunctionDescriptor shape) {
        SymbolLookup lookup = SymbolLookup.libraryLookup(library, Arena.global());
        MemorySegment address = lookup.find(function)
                .orElseThrow(() -> new IllegalStateException(function + " is missing from " + library));
        return LINKER.downcallHandle(address, shape);
    }
}
