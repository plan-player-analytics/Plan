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

import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.File;
import java.io.IOException;

/**
 * Config that keeps track of theme.yml.
 *
 * @author AuroraLS3
 */
public class ThemeConfig extends Config {

    public ThemeConfig(PlanFiles files, PlanConfig config, PluginLogger logger) {
        this(getConfigFile(files), getDefaults(files, config, logger));
    }

    private ThemeConfig(File configFile, ConfigNode defaults) {
        super(configFile, defaults);

        if (defaults.isLeafNode()) {
            ConfigNode util = new ConfigNode("", null, "");
            for (ThemeVal themeVal : ThemeVal.values()) {
                util.set(themeVal.getThemePath(), themeVal.getDefaultValue());
            }
            copyMissing(util);
        }
    }

    public static ConfigNode getDefaults(PlanFiles files, PlanConfig config, PluginLogger logger) {
        String fileName = config.get(DisplaySettings.THEME);
        String fileLocation = getFileLocation(fileName);

        try (ConfigReader reader = new ConfigReader(files.getResourceFromJar(fileLocation).asInputStream())) {
            return reader.read();
        } catch (IOException e) {
            logger.error("Could not find theme " + fileLocation + ". Attempting to use default.");
            return new ConfigNode(null, null, null);
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

    public static File getConfigFile(PlanFiles files) {
        return files.getFileFromPluginFolder("theme.yml");
    }
}
