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
package com.djrapitops.plan.delivery.webserver.cache;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.file.PlanFiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import utilities.TestPluginLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

class JSONStorageTest {

    private JSONStorage UNDER_TEST;
    private Path tempDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        PlanFiles files = Mockito.mock(PlanFiles.class);
        this.tempDir = tempDir;
        when(files.getJSONStorageDirectory()).thenReturn(this.tempDir);

        UNDER_TEST = new JSONFileStorage(files, value -> Long.toString(value), new TestPluginLogger());
    }

    private Optional<File> findTheFile() {
        File[] files = tempDir.toFile().listFiles();
        if (files != null && files.length > 0) {
            return Optional.of(files[0]);
        }
        return Optional.empty();
    }

    @Test
    void stringDataIsStored() throws IOException {
        JSONStorage.StoredJSON stored = UNDER_TEST.storeJson("Identifier", "data");

        File file = findTheFile().orElseThrow(AssertionError::new);
        assertEquals("Identifier-" + stored.timestamp + ".json", file.getName());
        try (Stream<String> lines = Files.lines(file.toPath())) {
            List<String> expected = Collections.singletonList(stored.json);
            List<String> result = lines.collect(Collectors.toList());
            assertEquals(expected, result);
        }
    }

    @Test
    void serializedDataIsStored() throws IOException {
        JSONStorage.StoredJSON stored = UNDER_TEST.storeJson("Identifier", Collections.singletonList("data"));

        File file = findTheFile().orElseThrow(AssertionError::new);
        assertEquals("Identifier-" + stored.timestamp + ".json", file.getName());
        try (Stream<String> lines = Files.lines(file.toPath())) {
            List<String> expected = Collections.singletonList(stored.json);
            List<String> result = lines.collect(Collectors.toList());
            assertEquals(expected, result);
        }
    }

    @Test
    void stringDataIsStoredWithTimestamp() throws IOException {
        long timestamp = System.currentTimeMillis();
        JSONStorage.StoredJSON stored = UNDER_TEST.storeJson("Identifier", "data", timestamp);

        File file = findTheFile().orElseThrow(AssertionError::new);
        assertEquals(timestamp, stored.timestamp);
        assertEquals("Identifier-" + timestamp + ".json", file.getName());
        try (Stream<String> lines = Files.lines(file.toPath())) {
            List<String> expected = Collections.singletonList(stored.json);
            List<String> result = lines.collect(Collectors.toList());
            assertEquals(expected, result);
        }
    }

    @Test
    void serializedDataIsStoredWithTimestamp() throws IOException {
        long timestamp = System.currentTimeMillis();
        JSONStorage.StoredJSON stored = UNDER_TEST.storeJson("Identifier", Collections.singletonList("data"), timestamp);

        File file = findTheFile().orElseThrow(AssertionError::new);
        assertEquals(timestamp, stored.timestamp);
        assertEquals("Identifier-" + timestamp + ".json", file.getName());
        try (Stream<String> lines = Files.lines(file.toPath())) {
            List<String> expected = Collections.singletonList(stored.json);
            List<String> result = lines.collect(Collectors.toList());
            assertEquals(expected, result);
        }
    }

    @Test
    void anythingStartingWithIsFetched() throws IOException {
        assertFalse(UNDER_TEST.fetchJSON("Identifier").isPresent());
        stringDataIsStoredWithTimestamp();
        JSONStorage.StoredJSON found = UNDER_TEST.fetchJSON("Identifier").orElseThrow(AssertionError::new);
        assertEquals("data", found.json);
    }

    @Test
    void storedWithExactDateIsFetched() {
        long timestamp = System.currentTimeMillis();
        JSONStorage.StoredJSON stored = UNDER_TEST.storeJson("Identifier", Collections.singletonList("data"), timestamp);
        JSONStorage.StoredJSON found = UNDER_TEST.fetchExactJson("Identifier", timestamp).orElseThrow(AssertionError::new);
        assertEquals(stored, found);
    }

    @Test
    void storedWithLaterDateIsFetched() {
        long timestamp = System.currentTimeMillis();
        JSONStorage.StoredJSON stored = UNDER_TEST.storeJson("Identifier", Collections.singletonList("data"), timestamp);
        JSONStorage.StoredJSON found = UNDER_TEST.fetchJsonMadeAfter("Identifier", timestamp - TimeUnit.DAYS.toMillis(1L)).orElseThrow(AssertionError::new);
        assertEquals(stored, found);
    }

    @Test
    void storedWithLaterDateIsNotFetched() {
        long timestamp = System.currentTimeMillis();
        UNDER_TEST.storeJson("Identifier", Collections.singletonList("data"), timestamp);
        assertFalse(UNDER_TEST.fetchJsonMadeAfter("Identifier", timestamp + TimeUnit.DAYS.toMillis(1L)).isPresent());
    }

    @Test
    void storedWithEarlierDateIsFetched() {
        long timestamp = System.currentTimeMillis();
        JSONStorage.StoredJSON stored = UNDER_TEST.storeJson("Identifier", Collections.singletonList("data"), timestamp);
        JSONStorage.StoredJSON found = UNDER_TEST.fetchJsonMadeBefore("Identifier", timestamp + TimeUnit.DAYS.toMillis(1L)).orElseThrow(AssertionError::new);
        assertEquals(stored, found);
    }

    @Test
    void storedWithEarlierDateIsNotFetched() {
        long timestamp = System.currentTimeMillis();
        UNDER_TEST.storeJson("Identifier", Collections.singletonList("data"), timestamp);
        assertFalse(UNDER_TEST.fetchJsonMadeBefore("Identifier", timestamp - TimeUnit.DAYS.toMillis(1L)).isPresent());
    }

    @Test
    void doesNotFetchWrongThing() {
        long timestamp = System.currentTimeMillis();
        UNDER_TEST.storeJson(DataID.SESSIONS_OVERVIEW.name(), Collections.singletonList("data"), timestamp);
        assertFalse(UNDER_TEST.fetchJsonMadeBefore(DataID.SESSIONS.name(), timestamp + TimeUnit.DAYS.toMillis(1L)).isPresent());
    }

    @Test
    void doesNotFetchWrongServer() {
        long timestamp = System.currentTimeMillis();
        UNDER_TEST.storeJson(DataID.SESSIONS_OVERVIEW.of(ServerUUID.randomUUID()), Collections.singletonList("data"), timestamp);
        assertFalse(UNDER_TEST.fetchJsonMadeBefore(DataID.SESSIONS_OVERVIEW.name(), timestamp + TimeUnit.DAYS.toMillis(1L)).isPresent());
    }
}