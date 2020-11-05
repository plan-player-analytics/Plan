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
package com.djrapitops.plan.settings.network;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.*;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.upkeep.FileWatcher;
import com.djrapitops.plan.settings.upkeep.WatchedFile;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.NewerConfigQuery;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.StoreConfigTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;
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
    private final RunnableFactory runnableFactory;
    private final PlanConfig config;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    private File serverSettingsFolder;

    private FileWatcher watcher;

    @Inject
    public NetworkSettingManager(
            PlanFiles files,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            RunnableFactory runnableFactory,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.files = files;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.runnableFactory = runnableFactory;
        this.logger = logger;

        this.errorLogger = errorLogger;
    }

    @Override
    public void enable() {
        serverSettingsFolder = createServerSettingsFolder();

        watcher = prepareFileWatcher();
        watcher.start();
        logger.debug("Server Settings folder FileWatcher started.");

        scheduleDBCheckTask();
    }

    @Override
    public void disable() {
        if (watcher != null) {
            watcher.interrupt();
        }
    }

    public static UUID getServerUUIDFromFilename(File file) {
        String fileName = file.getName();
        String uuidString = fileName.substring(0, fileName.length() - 4);
        return UUID.fromString(uuidString);
    }

    private FileWatcher prepareFileWatcher() {
        FileWatcher fileWatcher = new FileWatcher(serverSettingsFolder, errorLogger);

        File[] configFiles = getConfigFiles();
        if (configFiles != null) {
            for (File file : configFiles) {
                addFileToWatchList(fileWatcher, file);
            }
        }

        return fileWatcher;
    }

    public File[] getConfigFiles() {
        return serverSettingsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
    }

    private void addFileToWatchList(FileWatcher fileWatcher, File file) {
        try {
            UUID serverUUID = getServerUUIDFromFilename(file);

            fileWatcher.addToWatchlist(new WatchedFile(file, () -> updateConfigInDB(file, serverUUID)));
        } catch (IndexOutOfBoundsException | IllegalArgumentException ignore) {
            /* Invalid file-name, ignored */
        }
    }

    private void scheduleDBCheckTask() {
        long checkPeriod = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS);
        runnableFactory.create("Config Update DB Checker", new AbsRunnable() {
            @Override
            public void run() {
                updateConfigFromDBIfUpdated();
            }
        }).runTaskTimerAsynchronously(checkPeriod, checkPeriod);
    }

    private File createServerSettingsFolder() {
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

    private void updateConfigFromDBIfUpdated() {
        Database database = dbSystem.getDatabase();
        Set<UUID> serverUUIDs = database.query(ServerQueries.fetchPlanServerInformation()).keySet();
        // Remove the proxy server from the list
        serverUUIDs.remove(serverInfo.getServerUUID());

        for (UUID serverUUID : serverUUIDs) {
            updateConfigFromDBIfUpdated(database, serverUUID);
        }
    }

    private void updateConfigFromDBIfUpdated(Database database, UUID serverUUID) {
        File configFile = getServerConfigFile(serverUUID);
        long lastModified = configFile.exists() ? configFile.lastModified() : -1;

        Optional<Config> foundConfig = database.query(new NewerConfigQuery(serverUUID, lastModified));
        if (foundConfig.isPresent()) {
            try {
                Config writing = foundConfig.get();
                String serverName = writing.getNode(PluginSettings.SERVER_NAME.getPath()).map(ConfigNode::getString).orElse("Unknown");

                new ConfigWriter(configFile.toPath()).write(writing);
                logger.info("Config file for server '" + serverName + "' updated in /Plan/serverConfiguration");
                addFileToWatchList(watcher, configFile);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public void updateConfigInDB(File file, UUID serverUUID) {
        if (!file.exists()) {
            return;
        }

        Database database = dbSystem.getDatabase();

        try (ConfigReader reader = new ConfigReader(file.toPath())) {
            Config config = reader.read();
            database.executeTransaction(new StoreConfigTransaction(serverUUID, config, file.lastModified()));
            String serverName = config.getNode(PluginSettings.SERVER_NAME.getPath()).map(ConfigNode::getString).orElse("Unknown");
            logger.debug("Server config '" + serverName + "' in db now up to date.");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
