/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.file;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plan.utilities.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Abstracts File methods of Plugin classes so that they can be tested without Mocks.
 *
 * @author Rsl1122
 */
public class FileSystem implements SubSystem {

    private final File dataFolder;
    private File configFile;

    public FileSystem(PlanPlugin plugin) {
        this(plugin.getDataFolder());
    }

    public FileSystem(File dataFolder) {
        this.dataFolder = dataFolder;
        configFile = new File(dataFolder, "config.yml");
    }

    public static FileSystem getInstance() {
        FileSystem fileSystem = PlanSystem.getInstance().getFileSystem();
        NullCheck.check(fileSystem, new IllegalStateException("File system was not initialized."));
        return fileSystem;
    }

    public static File getDataFolder() {
        return getInstance().dataFolder;
    }

    public static File getConfigFile() {
        return getInstance().configFile;
    }

    public static File getLocaleFile() {
        return new File(getInstance().dataFolder, "locale.txt");
    }

    public static List<String> readFromResource(String fileName) throws IOException {
        return FileUtil.lines(PlanPlugin.getInstance(), fileName);
    }

    @Override
    public void enable() {
        dataFolder.mkdirs();
    }

    @Override
    public void disable() {

    }
}