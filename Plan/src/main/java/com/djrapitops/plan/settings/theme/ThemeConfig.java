/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.settings.theme;

import com.djrapitops.plugin.api.config.Config;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;

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
        String fileLocation = "themes/";

        switch (fileName.toLowerCase()) {
            case "mute":
            case "lowsaturation":
                fileLocation += "mute.yml";
                break;
            case "pastel":
            case "brigth":
            case "saturated":
            case "high":
                fileLocation += "pastel.yml";
                break;
            case "sepia":
            case "brown":
                fileLocation += "sepia.yml";
                break;
            case "grey":
            case "gray":
            case "greyscale":
            case "grayscale":
                fileLocation += "greyscale.yml";
                break;
            default:
                fileLocation += "theme.yml";
                break;
        }

        IPlan plugin = MiscUtils.getIPlan();
        try {
            return FileUtil.lines(plugin, fileLocation);
        } catch (IOException e) {
            return FileUtil.lines(plugin, "themes/theme.yml");
        }
    }


    private static File getConfigFile() throws IOException {
        File folder = MiscUtils.getIPlan().getDataFolder();
        folder.mkdirs();
        File themeFile = new File(folder, "theme.yml");
        if (!themeFile.exists()) {
            themeFile.createNewFile();
        }
        return themeFile;
    }
}