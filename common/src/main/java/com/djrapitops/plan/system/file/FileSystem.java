/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.file;

import com.djrapitops.plan.PlanHelper;
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
        Verify.nullCheck(fileSystem, () -> new IllegalStateException("File system was not initialized."));
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
        return FileUtil.lines(PlanHelper.getInstance(), fileName);
    }

    @Override
    public void enable() throws EnableException {
        Verify.isTrue((dataFolder.exists() && dataFolder.isDirectory()) || dataFolder.mkdirs(),
                () -> new EnableException("Could not create data folder at " + dataFolder.getAbsolutePath()));
        try {
            Verify.isTrue((configFile.exists() && configFile.isFile()) || configFile.createNewFile(),
                    () -> new EnableException("Could not create config file at " + configFile.getAbsolutePath()));

            RunnableFactory.createNew(new LogsFolderCleanTask(Log.getLogsFolder()))
                    .runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 30L);
        } catch (IOException e) {
            throw new EnableException("Failed to create config.yml", e);
        }
    }

    @Override
    public void disable() {

    }
}
