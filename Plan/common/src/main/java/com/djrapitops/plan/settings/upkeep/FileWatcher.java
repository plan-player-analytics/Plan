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
package com.djrapitops.plan.settings.upkeep;

import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.utilities.Verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Class for watching files for changes.
 *
 * @author AuroraLS3
 */
public class FileWatcher extends Thread {

    private final ErrorLogger errorLogger;

    private volatile boolean running;

    private final Path watchedPath;
    private final Set<WatchedFile> watchedFiles;

    public FileWatcher(
            File folder,
            ErrorLogger errorLogger
    ) {
        this(folder.toPath(), errorLogger);
    }

    public FileWatcher(
            Path watchedPath,
            ErrorLogger errorLogger
    ) {
        this.errorLogger = errorLogger;
        this.running = false;
        this.watchedFiles = Collections.newSetFromMap(new ConcurrentHashMap<>());

        Verify.isTrue(watchedPath.toFile().isDirectory(), () -> new IllegalArgumentException("Given File " + watchedPath.toString() + " was not a folder."));

        this.watchedPath = watchedPath;
    }

    public void addToWatchlist(WatchedFile watchedFile) {
        watchedFiles.remove(watchedFile);
        watchedFiles.add(watchedFile);
    }

    @Override
    public void run() {
        this.running = true;
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            watchedPath.register(watcher, ENTRY_MODIFY);
            runLoop(watcher);
        } catch (IOException e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().build());
            interrupt();
        } catch (InterruptedException e) {
            interrupt();
        }
    }

    private void runLoop(WatchService watcher) throws InterruptedException {
        while (running) {
            // Blocking operation
            WatchKey key = watcher.take();

            if (key == null) {
                Thread.yield();
                continue;
            }

            pollEvents(key);
        }
    }

    private void pollEvents(WatchKey key) {
        for (WatchEvent<?> event : key.pollEvents()) {
            handleEvent(event);
            if (!key.reset()) {
                break;
            }
        }
    }

    private void handleEvent(WatchEvent<?> event) {
        if (event.kind() != ENTRY_MODIFY) {
            Thread.yield();
        } else {
            @SuppressWarnings("unchecked")
            Path modifiedFile = ((WatchEvent<Path>) event).context();
            actOnModification(watchedPath.resolve(modifiedFile));
        }
    }

    private void actOnModification(Path modifiedFile) {
        for (WatchedFile watchedFile : watchedFiles) {
            watchedFile.modified(modifiedFile);
        }
    }

    @Override
    public void interrupt() {
        running = false;
        super.interrupt();
    }

    public boolean isRunning() {
        return running;
    }
}
