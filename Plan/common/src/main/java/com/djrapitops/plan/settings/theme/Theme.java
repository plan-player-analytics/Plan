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
package com.djrapitops.plan.settings.theme;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.file.PlanFiles;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.djrapitops.plan.settings.theme.ThemeVal.*;

/**
 * Enum that contains available themes.
 * <p>
 * Change config setting Theme.Base to select one.
 *
 * @author AuroraLS3
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

    @Deprecated
    public String getValue(ThemeVal variable) {
        try {
            return getThemeValue(variable);
        } catch (NullPointerException | IllegalStateException e) {
            return variable.getDefaultValue();
        }
    }

    public String[] getPieColors(ThemeVal variable) {
        return Arrays.stream(StringUtils.split(getValue(variable), ','))
                .map(color -> StringUtils.remove(StringUtils.trim(color), '"'))
                .toArray(String[]::new);
    }

    @Override
    public void enable() {
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
            return themeConfig.getString(path);
        } catch (Exception | NoSuchFieldError e) {
            logger.error("Something went wrong with getting variable " + variable.name() + " for: " + path);
        }
        return variable.getDefaultValue();
    }

    @Deprecated
    public String replaceThemeColors(String resourceString) {
        return replaceVariables(resourceString,
                RED, PINK, PURPLE,
                DEEP_PURPLE, INDIGO, BLUE, LIGHT_BLUE, CYAN, TEAL, GREEN, LIGHT_GREEN, LIME,
                YELLOW, AMBER, ORANGE, DEEP_ORANGE, BROWN, GREY, BLUE_GREY, BLACK, WHITE,
                GRAPH_PUNCHCARD, GRAPH_PLAYERS_ONLINE, GRAPH_TPS_HIGH, GRAPH_TPS_MED, GRAPH_TPS_LOW,
                GRAPH_CPU, GRAPH_RAM, GRAPH_CHUNKS, GRAPH_ENTITIES, GRAPH_WORLD_PIE, FONT_STYLESHEET, FONT_FAMILY
        );
    }

    private String replaceVariables(String resourceString, ThemeVal... themeVariables) {
        List<String> replace = new ArrayList<>();
        List<String> with = new ArrayList<>();
        for (ThemeVal variable : themeVariables) {
            String value = getColor(variable);
            String defaultValue = variable.getDefaultValue();
            if (defaultValue.equals(value)) {
                continue;
            }
            replace.add(defaultValue);
            with.add(value);
        }
        replace.add("${defaultTheme}");
        with.add(getValue(ThemeVal.THEME_DEFAULT));

        return StringUtils.replaceEach(resourceString, replace.toArray(new String[0]), with.toArray(new String[0]));
    }

    private String getThemeValue(ThemeVal color) {
        return themeConfig.getString(color.getThemePath());
    }
}
