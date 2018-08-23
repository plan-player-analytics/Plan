/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.settings.theme;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

import static com.djrapitops.plan.system.settings.theme.ThemeVal.*;

/**
 * Enum that contains available themes.
 * <p>
 * Change config setting Theme.Base to select one.
 *
 * @author Rsl1122
 */
@Singleton
public class Theme implements SubSystem {

    private final ThemeConfig config;
    private final PluginLogger logger;

    @Inject
    public Theme(ThemeConfig config, PluginLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Deprecated
    public static Theme getInstance() {
        Theme themeSystem = ConfigSystem.getInstance().getThemeSystem();
        Verify.nullCheck(themeSystem, () -> new IllegalStateException("Theme System has not been initialized."));
        return themeSystem;
    }

    @Deprecated
    public static String getValue(ThemeVal variable) {
        try {
            return getInstance().getThemeValue(variable);
        } catch (NullPointerException | IllegalStateException e) {
            return variable.getDefaultValue();
        }
    }

    @Deprecated
    public static String replaceColors(String resourceString) {
        return getInstance().replaceThemeColors(resourceString);
    }

    @Override
    public void enable() throws EnableException {
        try {
            config.save();
        } catch (IOException e) {
            throw new EnableException("theme.yml could not be saved.", e);
        }
    }

    @Override
    public void disable() {

    }

    public String getColor(ThemeVal variable) {
        String path = variable.getThemePath();
        try {
            String value = config.getString(path);

            if (value.contains(".")) {
                return "url(\"" + value + "\")";
            } else {
                return value;
            }
        } catch (Exception | NoSuchFieldError e) {
            logger.error("Something went wrong with getting variable " + variable.name() + " for: " + path);
        }
        return variable.getDefaultValue();
    }

    public String replaceThemeColors(String resourceString) {
        String replaced = resourceString;
        ThemeVal[] themeVariables = new ThemeVal[]{
                RED, PINK, PURPLE,
                DEEP_PURPLE, INDIGO, BLUE, LIGHT_BLUE, CYAN, TEAL, GREEN, LIGHT_GREEN, LIME,
                YELLOW, AMBER, ORANGE, DEEP_ORANGE, BROWN, GREY, BLUE_GREY, BLACK, WHITE,
                GRAPH_PUNCHCARD, GRAPH_PLAYERS_ONLINE, GRAPH_TPS_HIGH, GRAPH_TPS_MED, GRAPH_TPS_LOW,
                GRAPH_CPU, GRAPH_RAM, GRAPH_CHUNKS, GRAPH_ENTITIES, GRAPH_WORLD_PIE, GRAPH_GM_PIE,
                GRAPH_ACTIVITY_PIE, GRAPH_SERVER_PREF_PIE, FONT_STYLESHEET, FONT_FAMILY
        };
        for (ThemeVal variable : themeVariables) {
            String value = getColor(variable);
            String defaultValue = variable.getDefaultValue();
            if (Verify.equalsOne(value, defaultValue)) {
                continue;
            }
            if (value.contains("url")) {
                String[] colorAndUrl = value.split(" ");
                if (colorAndUrl.length >= 2) {
                    replaced = replaced.replace("background: " + defaultValue, "background: " + colorAndUrl[1]);
                    replaced = replaced.replace(defaultValue, colorAndUrl[0]);
                    return replaced;
                }
            } else {
                replaced = replaced.replace(defaultValue, value);
            }
        }
        return replaced;
    }

    public String getThemeValue(ThemeVal color) {
        return config.getString(color.getThemePath());
    }
}
