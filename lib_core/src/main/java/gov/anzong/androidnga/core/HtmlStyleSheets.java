package gov.anzong.androidnga.core;

/** Stylesheet for rendered post bodies, next to the shared style.css. */
public final class HtmlStyleSheets {

    private HtmlStyleSheets() {
    }

    public static String select(boolean darkMode, boolean miuixStyle, boolean couiStyle) {
        if (couiStyle) {
            return darkMode ? "style_coui_dark.css" : "style_coui_light.css";
        }
        return select(darkMode, miuixStyle);
    }

    public static String select(boolean darkMode, boolean miuixStyle) {
        if (miuixStyle) {
            return darkMode ? "style_miuix_dark.css" : "style_miuix_light.css";
        }
        return darkMode ? "style_dark.css" : "style_light.css";
    }
}
