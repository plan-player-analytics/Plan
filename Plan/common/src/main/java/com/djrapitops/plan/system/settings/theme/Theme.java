/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.settings.theme;

import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.storage.file.PlanFiles;
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

    private final PlanFiles files;
    private final PlanConfig config;
    private final PluginLogger logger;

    private ThemeConfig themeConfig;

    @Inject
    public Theme(PlanFiles files, PlanConfig config, PluginLogger logger) {
        this.files = files;
        this.config = config;
        this.logger = logger;
    }

    public String getValue(ThemeVal variable) {
        try {
            return getThemeValue(variable);
        } catch (NullPointerException | IllegalStateException e) {
            return variable.getDefaultValue();
        }
    }

    @Override
    public void enable() throws EnableException {
        try {
            themeConfig = new ThemeConfig(files, config, logger);
            themeConfig.save();
        } catch (IOException e) {
            throw new EnableException("theme.yml could not be saved.", e);
        }
    }

    @Override
    public void disable() {
        // No need to save theme on disable
    }

    private String getColor(ThemeVal variable) {
        String path = variable.getThemePath();
        try {
            String value = themeConfig.getString(path);

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
        return replaced.replace("${defaultTheme}", getValue(ThemeVal.THEME_DEFAULT));
    }

    private String getThemeValue(ThemeVal color) {
        return themeConfig.getString(color.getThemePath());
    }
}
