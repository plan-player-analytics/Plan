/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.settings.theme;

import com.djrapitops.plugin.api.utility.EnumUtility;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.io.IOException;
import java.util.List;

/**
 * Enum that contains available themes.
 * <p>
 * Change config setting Theme.Base to select one.
 *
 * @author Rsl1122
 */
public class Theme {

    private final ThemeConfig config;

    public Theme() throws PlanEnableException {
        String themeName = Settings.THEME_BASE.toString();
        try {

            config = new ThemeConfig(themeName);
        } catch (IOException e) {
            throw new PlanEnableException("Theme could not be loaded: " + themeName, e);
        }
    }

    public String getColor(ThemeVal color) {
        String path = color.getThemePath();
        try {
            String value = config.getString(path);
            String returnValue = "";

            if (value.isEmpty()) {
                return value;
            } else if (value.contains(".")) {
                returnValue += "url(\"" + value + "\")";
            } else {
                returnValue = value;
            }

            if (returnValue.isEmpty()) {
                returnValue = color.getDefaultValue();
            }
            return returnValue;
        } catch (Exception | NoSuchFieldError e) {
            Log.error("Something went wrong with getting color " + color.name() + " for: " + path);
        }
        return color.getDefaultValue();
    }

    public String replaceThemeColors(String resourceString) {
        String replaced = resourceString;
        List<ThemeVal> themeVariables = EnumUtility.getSupportedEnumValues(ThemeVal.class, "RED", "PINK", "PURPLE", "DEEP_PURPLE",
                "INDIGO", "BLUE", "LIGHT_BLUE", "CYAN", "TEAL", "GREEN", "LIGHT_GREEN", "LIME", "YELLOW", "AMBER",
                "ORANGE", "DEEP_ORANGE", "BROWN", "GREY", "BLUE_GREY", "BLACK", "WHITE",
                "GRAPH_PUNCHCARD", "GRAPH_PLAYERS_ONLINE", "GRAPH_TPS_HIGH", "GRAPH_TPS_MED", "GRAPH_TPS_LOW",
                "GRAPH_CPU", "GRAPH_RAM", "GRAPH_CHUNKS", "GRAPH_ENTITIES", "GRAPH_WORLD_PIE", "GRAPH_GM_PIE",
                "GRAPH_ACTIVITY_PIE", "GRAPH_SERVER_PREF_PIE", "FONT_STYLESHEET", "FONT_FAMILY");
        for (ThemeVal variable : themeVariables) {
            String value = getColor(variable);
            String defaultValue = variable.getDefaultValue();
            if (Verify.equalsOne(value, defaultValue)) {
                continue;
            }
            if (!value.contains("url")) {
                replaced = replaced.replace(defaultValue, value);
            } else {
                String[] colorAndUrl = value.split(" ");
                replaced = replaced.replace("background: " + defaultValue, "background: " + colorAndUrl[1]);
                replaced = replaced.replace(defaultValue, colorAndUrl[0]);
            }
        }
        return replaced;
    }

    public String getThemeValue(ThemeVal color) {
        return config.getString(color.getThemePath());
    }

    public static String getValue(ThemeVal color) {
        return MiscUtils.getIPlan().getTheme().getThemeValue(color);
    }

    public static String replaceColors(String resourceString) {
        return MiscUtils.getIPlan().getTheme().replaceThemeColors(resourceString);
    }
}