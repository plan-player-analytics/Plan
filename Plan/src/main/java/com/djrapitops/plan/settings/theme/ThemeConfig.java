/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.settings.theme;

import com.djrapitops.plan.api.IPlan;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Config that keeps track of theme.yml.
 *
 * @author Rsl1122
 */
public class ThemeConfig extends Config {

    private ThemeConfig(File file, List<String> defaults) {
        super(file, defaults);
    }

    public ThemeConfig(String fileName) throws IOException {
        this(getConfigFile(), getDefaults(fileName));
        save();
    }

    private static List<String> getDefaults(String fileName) throws IOException {
        String fileLocation = getFileLocation(fileName);

        IPlan plugin = MiscUtils.getIPlan();
        try {
            return FileUtil.lines(plugin, fileLocation);
        } catch (IOException e) {
            Log.error("Could not find theme " + fileLocation + ". Attempting to use default.");
            return FileUtil.lines(plugin, "themes/theme.yml");
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


    private static File getConfigFile() throws IOException {
        File folder = MiscUtils.getIPlan().getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File themeFile = new File(folder, "theme.yml");
        if (!themeFile.exists()) {
            themeFile.createNewFile();
        }
        return themeFile;
    }
}