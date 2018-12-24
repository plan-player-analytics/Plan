package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.Config;
import com.djrapitops.plan.system.settings.config.ConfigReader;
import com.djrapitops.plan.system.settings.config.ConfigWriter;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.utilities.file.FileWatcher;
import com.djrapitops.plan.utilities.file.WatchedFile;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * In charge of updating network-server configs.
 * <p>
 * Performs the following tasks related to network configs:
 * - File modification watching related to server configs
 * - Database update operations related to server configs
 * - File update operations from database related to server configs
 *
 * @author Rsl1122
 */
@Singleton
public class NetworkSettingManager implements SubSystem {

    private final PlanFiles files;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final TaskSystem taskSystem;
    private PlanConfig config;
    private ErrorHandler errorHandler;

    private File serverSettingsFolder;

    private FileWatcher watcher;

    @Inject
    public NetworkSettingManager(
            PlanFiles files,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            TaskSystem taskSystem,
            ErrorHandler errorHandler
    ) {
        this.files = files;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.taskSystem = taskSystem;

        this.errorHandler = errorHandler;
    }

    @Override
    public void enable() throws EnableException {
        serverSettingsFolder = createServerSettingsFolder();

        watcher = prepareFileWatcher();
        watcher.start();

        scheduleDBCheckTask();
    }

    @Override
    public void disable() {
        if (watcher != null) {
            watcher.interrupt();
        }
    }

    private FileWatcher prepareFileWatcher() {
        FileWatcher fileWatcher = new FileWatcher(serverSettingsFolder, errorHandler);

        File[] files = serverSettingsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                addFileToWatchList(fileWatcher, file);
            }
        }

        return fileWatcher;
    }

    private void addFileToWatchList(FileWatcher fileWatcher, File file) {
        try {
            String fileName = file.getName();
            String uuidString = fileName.substring(0, fileName.length() - 4);
            UUID serverUUID = UUID.fromString(uuidString);

            fileWatcher.addToWatchlist(new WatchedFile(file, () -> updateConfigInDB(file, serverUUID)));
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignore) {
            /* Invalid file-name, ignored */
        }
    }

    private void scheduleDBCheckTask() {
        long checkPeriod = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MINUTES);
        taskSystem.registerTask("Config Update DB Checker", new AbsRunnable() {
            @Override
            public void run() {
                checkDBForNewConfigSettings();
            }
        }).runTaskTimerAsynchronously(checkPeriod, checkPeriod);
    }

    private File createServerSettingsFolder() throws EnableException {
        try {
            File serverConfigFolder = files.getFileFromPluginFolder("serverConfiguration");
            Files.createDirectories(serverConfigFolder.toPath());
            return serverConfigFolder;
        } catch (IOException e) {
            throw new EnableException("Could not initialize NetworkSettingManager: " + e.getMessage(), e);
        }
    }

    private File getServerConfigFile(UUID serverUUID) {
        return new File(serverSettingsFolder, serverUUID + ".yml");
    }

    private void checkDBForNewConfigSettings() {
        Database database = dbSystem.getDatabase();
        List<UUID> serverUUIDs = database.fetch().getServerUUIDs();
        serverUUIDs.remove(serverInfo.getServerUUID());

        for (UUID serverUUID : serverUUIDs) {
            checkDBForNewConfigSettings(database, serverUUID);
        }
    }

    private void checkDBForNewConfigSettings(Database database, UUID serverUUID) {
        File configFile = getServerConfigFile(serverUUID);
        long lastModified = configFile.exists() ? configFile.lastModified() : -1;

        Optional<Config> foundConfig = database.fetch().getNewConfig(lastModified, serverUUID);
        if (foundConfig.isPresent()) {
            try {
                new ConfigWriter(configFile.toPath()).write(foundConfig.get());
                addFileToWatchList(watcher, configFile);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void updateConfigInDB(File file, UUID serverUUID) {
        if (!file.exists()) {
            return;
        }

        Database database = dbSystem.getDatabase();

        try {
            Config config = new ConfigReader(file.toPath()).read();
            database.save().saveConfig(serverUUID, config);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
