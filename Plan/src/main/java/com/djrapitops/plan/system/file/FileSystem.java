/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.file;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.tasks.LogsFolderCleanTask;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
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
    private final PlanPlugin plugin;
    private File configFile;

    @Inject
    public FileSystem(PlanPlugin plugin) {
        this.dataFolder = plugin.getDataFolder();
        this.plugin = plugin;
        configFile = new File(dataFolder, "config.yml");
    }

    @Deprecated
    public static FileSystem getInstance() {
        FileSystem fileSystem = PlanSystem.getInstance().getFileSystem();
        Verify.nullCheck(fileSystem, () -> new IllegalStateException("File system was not initialized."));
        return fileSystem;
    }

    @Deprecated
    public static File getDataFolder_Old() {
        return getInstance().dataFolder;
    }

    @Deprecated
    public static File getConfigFile_Old() {
        return getInstance().configFile;
    }

    @Deprecated
    public static File getLocaleFile_Old() {
        return getInstance().getLocaleFile();
    }

    @Deprecated
    public static List<String> readFromResource_Old(String fileName) throws IOException {
        return FileUtil.lines(PlanPlugin.getInstance(), fileName);
    }

    public List<String> readFromResource(String fileName) throws IOException {
        return FileUtil.lines(plugin, fileName);
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getLocaleFile() {
        return getFileFromPluginFolder("locale.txt");
    }

    public File getFileFromPluginFolder(String name) {
        return new File(dataFolder, name);
    }

    @Override
    public void enable() throws EnableException {
        Verify.isTrue((dataFolder.exists() && dataFolder.isDirectory()) || dataFolder.mkdirs(),
                () -> new EnableException("Could not create data folder at " + dataFolder.getAbsolutePath()));
        try {
            Verify.isTrue((configFile.exists() && configFile.isFile()) || configFile.createNewFile(),
                    () -> new EnableException("Could not create config file at " + configFile.getAbsolutePath()));

            RunnableFactory.createNew("Logs folder Clean Task", new LogsFolderCleanTask(Log.getLogsFolder()))
                    .runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 30L);
        } catch (IOException e) {
            throw new EnableException("Failed to create config.yml", e);
        }
    }

    @Override
    public void disable() {
        // No disable actions necessary.
    }
}
