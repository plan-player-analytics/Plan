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

import com.jayway.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileWatcherTest {

    private Path temporaryDir;

    @BeforeEach
    void setUpTemporaryDir(@TempDir Path dir) {
        temporaryDir = dir;
    }

    @Test
    void fileWatcherCallsWatchedFile() throws IOException {
        FileWatcher underTest = new FileWatcher(temporaryDir, null);
        File modified = temporaryDir.resolve("modifiedFile").toFile();

        AtomicBoolean methodWasCalled = new AtomicBoolean(false);
        WatchedFile watchedFile = new WatchedFile(modified, () -> methodWasCalled.set(true));
        underTest.addToWatchlist(watchedFile);

        try {
            underTest.start();
            Awaitility.await()
                    .atMost(5, TimeUnit.SECONDS)
                    .until(underTest::isRunning);

            // Modification should trigger a watch event here.
            createAndModifyFile(modified);

            Awaitility.await()
                    .atMost(5, TimeUnit.SECONDS)
                    .until(methodWasCalled::get);

            assertTrue(methodWasCalled.get());
        } finally {
            underTest.interrupt();
        }
    }

    private void createAndModifyFile(File modified) throws IOException {
        Files.createFile(modified.toPath());
        Files.write(modified.toPath(), Collections.singletonList("DataToWrite"), StandardCharsets.UTF_8);
        Files.write(modified.toPath(), Collections.singletonList("OverWrite"), StandardCharsets.UTF_8);
    }
}