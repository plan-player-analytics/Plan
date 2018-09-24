package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.AbsRunnable;

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
 * @author Rsl1122
 */
@Singleton
public class LogsFolderCleanTask extends AbsRunnable {

    private final int keepLogDayThreshold;

    private final File folder;
    private final PluginLogger logger;

    @Inject
    public LogsFolderCleanTask(
            PlanFiles files,
            PlanConfig config,
            PluginLogger logger
    ) {
        this.folder = files.getLogsFolder();
        this.keepLogDayThreshold = config.getNumber(Settings.KEEP_LOGS_DAYS);
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
            cancel();
        }
    }

    private void cleanFolder() {
        long now = System.currentTimeMillis();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            long forDaysMs = TimeUnit.DAYS.toMillis(keepLogDayThreshold);
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