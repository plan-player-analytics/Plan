/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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
    public ThemeConfig(PlanFiles planFiles, PlanConfig config, PluginLogger logger) {
        this(getConfigFile(planFiles), getDefaults(planFiles, config, logger));
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

    private static List<String> getDefaults(PlanFiles planFiles, PlanConfig config, PluginLogger logger) {
        String fileName = config.getString(Settings.THEME_BASE);
        String fileLocation = getFileLocation(fileName);

        try {
            return planFiles.readFromResource(fileLocation);
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

    private static File getConfigFile(PlanFiles planFiles) {
        return planFiles.getFileFromPluginFolder("theme.yml");
    }
}
