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
import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.settings.config.ConfigWriter;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.upkeep.FileWatcher;
import com.djrapitops.plan.settings.upkeep.WatchedFile;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.NewerConfigQuery;
import com.djrapitops.plan.storage.database.transactions.StoreConfigTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * In charge of updating server-network config.
 * <p>
 * Performs following tasks related to the config:
 * - File modification watching related to config.yml
 * - Database updating related to config.yml
 * - File update operations from database related to config.yml
 *
 * @author Rsl1122
 */
@Singleton
public class ServerSettingsManager implements SubSystem {

    private final PlanFiles files;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final TaskSystem taskSystem;
    private final ErrorLogger errorLogger;
    private final PluginLogger logger;
    private FileWatcher watcher;

    @Inject
    public ServerSettingsManager(
            PlanFiles files,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            TaskSystem taskSystem,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.files = files;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.taskSystem = taskSystem;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    @Override
    public void enable() {
        watcher = prepareFileWatcher();
        watcher.start();
        logger.debug("Server Settings folder FileWatcher started.");
        scheduleDBCheckTask();
    }

    private FileWatcher prepareFileWatcher() {
        FileWatcher fileWatcher = new FileWatcher(files.getDataFolder(), errorLogger);
        File configFile = files.getConfigFile();
        fileWatcher.addToWatchlist(new WatchedFile(configFile,
                () -> updateConfigInDB(configFile)
        ));
        return fileWatcher;
    }

    private void updateConfigInDB(File file) {
        if (!file.exists()) {
            return;
        }

        Database database = dbSystem.getDatabase();
        Optional<UUID> serverUUID = serverInfo.getServerUUIDSafe();
        if (!serverUUID.isPresent()) {
            return;
        }

        try (ConfigReader reader = new ConfigReader(file.toPath())) {
            Config read = reader.read();
            database.executeTransaction(new StoreConfigTransaction(serverUUID.get(), read, file.lastModified()));
            logger.debug("Server config saved to database.");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void scheduleDBCheckTask() {
        long checkPeriod = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS);
        taskSystem.registerTask("Config Update DB Checker", new AbsRunnable() {
            @Override
            public void run() {
                checkDBForNewConfigSettings(dbSystem.getDatabase());
            }
        }).runTaskTimerAsynchronously(checkPeriod, checkPeriod);
    }

    private void checkDBForNewConfigSettings(Database database) {
        File configFile = files.getConfigFile();
        long lastModified = configFile.exists() ? configFile.lastModified() : -1;

        Optional<UUID> serverUUID = serverInfo.getServerUUIDSafe();
        if (!serverUUID.isPresent()) {
            return;
        }

        Optional<Config> foundConfig = database.query(new NewerConfigQuery(serverUUID.get(), lastModified));
        if (foundConfig.isPresent()) {
            try {
                new ConfigWriter(configFile.toPath()).write(foundConfig.get());
                logger.info("The Config was updated to match one on the Proxy. Reload for changes to take effect.");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void disable() {
        if (watcher != null) {
            watcher.interrupt();
        }
    }
}
