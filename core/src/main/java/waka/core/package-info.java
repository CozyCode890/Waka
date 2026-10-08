/**
 * The UI-free half of Waka: settings, the keymap, the command registry, the language pack, and
 * from S02 onward every call this project makes into {@code weka.*}.
 *
 * <p>Nothing in this module or below it may import {@code javafx.*}. That is not a convention
 * kept by review - {@code core/pom.xml} simply does not declare JavaFX, so such an import fails
 * to compile. If a class here ever seems to need a JavaFX type, the class belongs in
 * {@code waka.app} instead, or the type it wants is a view concern that {@code core} should be
 * handing out as plain data.
 *
 * <p>The reason the split exists: the screens are the part most likely to be rewritten, and
 * anything that survives a rewrite - a keymap file format, a command id, a settings key - must
 * not be reachable only through a window.
 */
package waka.core;
