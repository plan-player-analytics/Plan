package com.djrapitops.plan.utilities.file;

import com.jayway.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(JUnitPlatform.class)
@ExtendWith(TempDirectory.class)
class FileWatcherTest {

    private Path temporaryDir;

    @BeforeEach
    void setUpTemporaryDir(@TempDirectory.TempDir Path dir) {
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
                    .atMost(1, TimeUnit.SECONDS)
                    .until(methodWasCalled::get);
        } finally {
            underTest.interrupt();
        }
    }

    private void createAndModifyFile(File modified) throws IOException {
        modified.createNewFile();
        Files.write(modified.toPath(), Collections.singletonList("DataToWrite"), StandardCharsets.UTF_8);
        Files.write(modified.toPath(), Collections.singletonList("OverWrite"), StandardCharsets.UTF_8);
    }
}