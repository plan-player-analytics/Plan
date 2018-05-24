package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Task in charge of removing old log files
 *
 * @author Rsl1122
 */
public class LogsFolderCleanTask extends AbsRunnable {

    private final File folder;

    public LogsFolderCleanTask(File folder) {
        super(LogsFolderCleanTask.class.getSimpleName());
        this.folder = folder;
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
            long forDaysMs = Settings.KEEP_LOGS_DAYS.getNumber() * TimeAmount.DAY.ms();
            if (now - file.lastModified() > (forDaysMs > 0 ? forDaysMs : TimeAmount.DAY.ms())) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    Log.warn("Could not delete log file at: " + file.getAbsolutePath() + ", " + e.getMessage());
                }
            }
        }
    }
}