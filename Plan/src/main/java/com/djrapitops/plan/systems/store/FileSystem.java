/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.store;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;
import main.java.com.djrapitops.plan.systems.SubSystem;
import main.java.com.djrapitops.plan.systems.Systems;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class FileSystem implements SubSystem {

    private final File dataFolder;
    private File configFile;

    public FileSystem(IPlan plugin) {
        this(plugin.getDataFolder());
    }

    public FileSystem(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public static FileSystem getInstance() {
        return Systems.getInstance().getFileSystem();
    }

    public static File getDataFolder() {
        return getInstance().dataFolder;
    }

    public static File getConfigFile() {
        return getInstance().configFile;
    }

    public static List<String> readFromResource(String fileName) throws IOException {
        return FileUtil.lines(MiscUtils.getIPlan(), fileName);
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