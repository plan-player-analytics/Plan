/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;

import java.io.File;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class FileSystem implements SubSystem {

    private final File dataFolder;
    private File configFile;

    public FileSystem(IPlan plugin) {
        dataFolder = plugin.getDataFolder();
    }

    public static FileSystem getInstance() {
        return Systems.getInstance().fileSystem;
    }

    public static File getDataFolder() {
        return getInstance().dataFolder;
    }

    public static File getConfigFile() {
        return getInstance().configFile;
    }

    @Override
    public void init() throws PlanEnableException {
        dataFolder.mkdirs();
        configFile = new File(dataFolder, "config.yml");
    }

    @Override
    public void close() {

    }
}