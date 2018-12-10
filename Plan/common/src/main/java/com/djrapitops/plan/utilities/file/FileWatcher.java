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
package com.djrapitops.plan.utilities.file;

import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Class for watching files for changes.
 *
 * @author Rsl1122
 */
public class FileWatcher extends Thread {

    private final ErrorHandler errorHandler;

    private volatile boolean running;

    private Path watchedPath;
    private List<WatchedFile> watchedFiles;

    public FileWatcher(File folder, ErrorHandler errorHandler) {
        this(folder, errorHandler, new ArrayList<>());
    }

    public FileWatcher(
            File folder,
            ErrorHandler errorHandler,
            List<WatchedFile> watchedFiles
    ) {
        this.errorHandler = errorHandler;
        this.running = false;
        this.watchedFiles = watchedFiles;

        Verify.isTrue(Files.isDirectory(folder.toPath()), () -> new IllegalArgumentException("Given File " + folder.getAbsolutePath() + " was not a folder."));

        watchedPath = folder.toPath();
    }

    public void addToWatchlist(WatchedFile watchedFile) {
        watchedFiles.add(watchedFile);
    }

    @Override
    public void run() {
        this.running = true;
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            watchedPath.register(watcher, ENTRY_MODIFY);

            while (running) {
                WatchKey key = watcher.take();

                if (key == null) {
                    Thread.yield();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() != ENTRY_MODIFY) {
                        Thread.yield();
                    } else {
                        @SuppressWarnings("unchecked")
                        Path modifiedFile = ((WatchEvent<Path>) event).context();

                        for (WatchedFile watchedFile : watchedFiles) {
                            watchedFile.modified(modifiedFile);
                        }
                    }
                    if (!key.reset()) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        } catch (InterruptedException e) {
            interrupt();
        }
    }

    @Override
    public void interrupt() {
        running = false;
        super.interrupt();
    }
}
