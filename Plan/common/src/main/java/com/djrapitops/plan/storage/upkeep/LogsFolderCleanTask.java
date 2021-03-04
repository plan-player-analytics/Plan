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
package com.djrapitops.plan.storage.upkeep;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Task in charge of removing old log files
 *
 * @author AuroraLS3
 */
@Singleton
public class LogsFolderCleanTask extends TaskSystem.Task {

    private final File folder;
    private final PlanConfig config;
    private final PluginLogger logger;

    @Inject
    public LogsFolderCleanTask(
            PlanFiles files,
            PlanConfig config,
            PluginLogger logger
    ) {
        this.folder = files.getLogsFolder();
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            if (!folder.exists() || folder.isFile()) {
                return;
            }
            cleanFolder();
        } catch (NullPointerException ignore) {
            /* Ignored - not supposed to occur. */
        } finally {
            try {
                cancel();
            } catch (Exception ignore) {
                /* Ignored, TaskCenter concurrent modification exception, will be fixed later in apf-3.3.0. */
            }
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        long delay = TimeAmount.toTicks(30L, TimeUnit.SECONDS);
        runnableFactory.create(null, this).runTaskLaterAsynchronously(delay);
    }

    private void cleanFolder() {
        long now = System.currentTimeMillis();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            long forDaysMs = TimeUnit.DAYS.toMillis(config.get(PluginSettings.KEEP_LOGS_DAYS));
            if (now - file.lastModified() > (forDaysMs > 0 ? forDaysMs : TimeUnit.DAYS.toMillis(1L))) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    logger.warn("Could not delete log file at: " + file.getAbsolutePath() + ", " + e.getMessage());
                }
            }
        }
    }
}