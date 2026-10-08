package waka.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Proves that DESIGN.md §Colour actually reaches a node, rather than merely being written down
 * in {@code waka.css}.
 *
 * <p>This test exists because the first version of that stylesheet looked right and did nothing:
 * every rule that referred to a {@code -waka-*} token failed at run time with "class
 * java.lang.String cannot be cast to class javafx.scene.paint.Paint", logged as a warning and
 * swallowed, leaving the surfaces unpainted. A stylesheet cannot be reviewed by reading it.
 */
class ColourTokenTest extends OffScreenApplicationTest {

    @Test
    @DisplayName("dark accent resolves to the graded #4CC2FF")
    void darkAccentReachesTheNode() throws Exception {
        onFxThread(() -> {
            application.themeService().setMode(ThemeService.Mode.DARK);
            application.micaEnabledProperty().set(true);
        });

        assertEquals(Color.web("#4CC2FF"), backgroundOf(".waka-placeholder-accent"),
                "DESIGN §Colour locks the dark accent at #4CC2FF. If this is null or a String, "
                + "the -waka-* token did not resolve and every colour in waka.css is inert.");
    }

    @Test
    @DisplayName("light accent resolves to the graded #005FB8")
    void lightAccentReachesTheNode() throws Exception {
        onFxThread(() -> application.themeService().setMode(ThemeService.Mode.LIGHT));

        assertEquals(Color.web("#005FB8"), backgroundOf(".waka-placeholder-accent"));
    }

    @Test
    @DisplayName("G-016: turning Mica off swaps the card fill for a solid one")
    void micaOffSwapsTheLayerFills() throws Exception {
        onFxThread(() -> {
            application.themeService().setMode(ThemeService.Mode.LIGHT);
            application.micaEnabledProperty().set(true);
        });
        Paint overMica = backgroundOf(".waka-placeholder-card");

        onFxThread(() -> application.micaEnabledProperty().set(false));
        Paint withoutMica = backgroundOf(".waka-placeholder-card");

        assertEquals(Color.web("#FFFFFF", 0.7), overMica,
                "light --card over Mica is #FFF at 70%");
        assertEquals(Color.web("#FAFAFA"), withoutMica,
                "light --card with Mica off is the solid #FAFAFA, not the same alpha over "
                + "nothing - that is what G-016 changed");
    }

    /** The first background fill actually computed for the first node matching this selector. */
    private Paint backgroundOf(String selector) throws Exception {
        return onFxThread(() -> {
            Region region = (Region) application.root().lookup(selector);
            assertNotNull(region, "no node matches " + selector);
            region.applyCss();
            assertNotNull(region.getBackground(),
                    selector + " has no background at all, so its rule did not apply");
            return region.getBackground().getFills().getFirst().getFill();
        });
    }
}
