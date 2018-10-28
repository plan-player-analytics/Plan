/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.settings.theme;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.config.Config;
import com.djrapitops.plugin.config.ConfigNode;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Config that keeps track of theme.yml.
 *
 * @author Rsl1122
 */
@Singleton
public class ThemeConfig extends Config {

    @Inject
    public ThemeConfig(PlanFiles files, PlanConfig config, PluginLogger logger) {
        this(getConfigFile(files), getDefaults(files, config, logger));
    }

    private ThemeConfig(File configFile, List<String> defaults) {
        super(configFile, defaults);

        if (defaults.isEmpty()) {
            ConfigNode util = new ConfigNode("", null, "");
            for (ThemeVal themeVal : ThemeVal.values()) {
                util.set(themeVal.getThemePath(), themeVal.getDefaultValue());
            }
            copyDefaults(util);
        }
    }

    private static List<String> getDefaults(PlanFiles files, PlanConfig config, PluginLogger logger) {
        String fileName = config.getString(Settings.THEME_BASE);
        String fileLocation = getFileLocation(fileName);

        try {
            return files.readFromResource(fileLocation);
        } catch (IOException e) {
            logger.error("Could not find theme " + fileLocation + ". Attempting to use default.");
            return new ArrayList<>();
        }
    }

    private static String getFileLocation(String fileName) {
        switch (fileName.toLowerCase()) {
            case "soft":
            case "soften":
                return "themes/soft.yml";
            case "mute":
            case "lowsaturation":
                return "themes/mute.yml";
            case "pastel":
            case "bright":
            case "harsh":
            case "saturated":
            case "high":
                return "themes/pastel.yml";
            case "sepia":
            case "brown":
                return "themes/sepia.yml";
            case "grey":
            case "gray":
            case "greyscale":
            case "grayscale":
                return "themes/greyscale.yml";
            default:
                return "themes/theme.yml";
        }
    }

    private static File getConfigFile(PlanFiles files) {
        return files.getFileFromPluginFolder("theme.yml");
    }
}
