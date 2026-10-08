package waka.app;

/**
 * DESIGN.md §Geometry, LOCKED 2026-10-07, as Java constants.
 *
 * <p>These are not in {@code waka.css} and cannot be. JavaFX's "looked-up value" mechanism -
 * the thing that makes {@code -waka-card} work in a stylesheet - resolves colours only; JavaFX
 * CSS has no numeric custom property, so {@code -waka-h-doctab: 35px} is not a thing that can be
 * written. Java constants are also the form the shell actually needs, because D-031 builds every
 * region in Java and a {@code setPrefWidth} call wants a number.
 *
 * <p>A number here without a row in DESIGN.md §Geometry or a graded {@code G-NNN} verdict behind
 * it is a defect. Each one below says where it comes from.
 */
public final class Geometry {

    /** The native Win11 caption. Not ours to change - it is here to be subtracted, not set. */
    public static final double CAPTION_HEIGHT = 32;

    /** Dataset tabs (§Geometry {@code --h-doctab}). */
    public static final double DOCUMENT_TAB_HEIGHT = 35;

    /** The six Weka tasks (§Geometry {@code --h-segbar}). */
    public static final double SEGMENTED_BAR_HEIGHT = 32;

    /** Per-task action row (§Geometry {@code --h-toolbar}). */
    public static final double TOOLBAR_HEIGHT = 40;

    /** Controls sitting inside the toolbar, which is shorter than the row holding them. */
    public static final double TOOLBAR_CONTROL_HEIGHT = 28;

    /** Status bar (§Geometry {@code --h-statusbar}). */
    public static final double STATUS_BAR_HEIGHT = 22;

    /** Bottom panel when open, INCLUDING its tab strip (§Geometry {@code --h-panel}). */
    public static final double BOTTOM_PANEL_HEIGHT = 224;

    /** The bottom panel's own tab strip, which is part of the 224 above, not added to it. */
    public static final double BOTTOM_PANEL_TAB_STRIP_HEIGHT = 34;

    /** Attribute rows and tree rows (§Geometry {@code --h-row}, graded as G-009). */
    public static final double ROW_HEIGHT = 26;

    /** The rail (§Geometry {@code --w-rail}). */
    public static final double RAIL_WIDTH = 48;

    /** Icon inside the rail, centred in the 48 (§Geometry, §Components). */
    public static final double RAIL_ICON_SIZE = 20;

    /** Side panel (§Geometry {@code --w-side}, graded as G-004). */
    public static final double SIDE_PANEL_WIDTH = 260;

    /**
     * The active-mode bar on the rail's inner edge, and the active document tab's top edge
     * (§Components; graded as G-003 and G-005). Not a §Geometry row, but a graded number.
     */
    public static final double ACCENT_BAR_THICKNESS = 2;

    /** Controls (§Geometry {@code --r-ctl}, graded as G-017). There is no third radius. */
    public static final double CONTROL_RADIUS = 4;

    /** Cards, flyouts and the window (§Geometry {@code --r-card}, graded as G-017). */
    public static final double CARD_RADIUS = 8;

    /** Every gap and pad in this application is a multiple of this (§Geometry {@code --sp}). */
    public static final double SPACING = 4;

    /** Command palette width and its distance from the top of the window (graded as G-014). */
    public static final double COMMAND_PALETTE_WIDTH = 600;
    public static final double COMMAND_PALETTE_TOP_INSET = 76;

    private Geometry() {
    }
}
