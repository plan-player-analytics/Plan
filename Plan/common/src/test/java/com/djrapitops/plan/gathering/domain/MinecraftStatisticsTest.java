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
package com.djrapitops.plan.gathering.domain;

import com.djrapitops.plan.storage.file.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MinecraftStatistics}.
 *
 * @author AuroraLS3
 */
class MinecraftStatisticsTest {

    private static final List<String> STATISTICS_FILES = List.of(
            "statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json",
            "statistics/a5bdfc31-36de-3836-80a7-fdf2760901fc.json",
            "statistics/b70c4628-c615-36b4-a127-a79300eefb88.json",
            "statistics/c246a8a0-3b84-3426-a4de-dddfb2240775.json"
    );

    static Stream<String> statisticsFiles() {
        return STATISTICS_FILES.stream();
    }

    /**
     * Creates a {@link Resource} from the given classpath resource file (old flat format).
     */
    private Resource createOldFormatResource(String classpathResource) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            assertNotNull(stream, "Resource not found: " + classpathResource);
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return createResource(classpathResource, json);
        }
    }

    /**
     * Creates a {@link Resource} from the given classpath resource file,
     * wrapping its flat JSON content in the "stats" root object (new format).
     */
    private Resource createNewFormatResource(String classpathResource) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            assertNotNull(stream, "Resource not found: " + classpathResource);
            String flatJson = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            String wrapped = "{\"stats\":" + flatJson + "}";
            return createResource(classpathResource, wrapped);
        }
    }

    /**
     * Creates a {@link Resource} with the given JSON content.
     */
    private Resource createResource(String name, String json) {
        return new Resource() {
            @Override
            public String getResourceName() {
                return name;
            }

            @Override
            public long getLastModifiedDate() {
                return 0;
            }

            @Override
            public byte[] asBytes() {
                return json.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public InputStream asInputStream() {
                return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public List<String> asLines() {
                return List.of(json);
            }

            @Override
            public String asString() {
                return json;
            }
        };
    }

    @ParameterizedTest
    @MethodSource("statisticsFiles")
    void oldFormatReturnsStatisticsFromResourceFile(String resourceFile) throws IOException {
        Resource resource = createOldFormatResource(resourceFile);

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertNotNull(parsed);
        assertFalse(parsed.isEmpty(), "Parsed statistics should not be empty");
        assertTrue(parsed.size() > 50, "Expected more than 50 statistics, got: " + parsed.size());
    }

    @ParameterizedTest
    @MethodSource("statisticsFiles")
    void newFormatReturnsStatisticsFromResourceFile(String resourceFile) throws IOException {
        Resource resource = createNewFormatResource(resourceFile);

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertNotNull(parsed);
        assertFalse(parsed.isEmpty(), "Parsed statistics should not be empty");
        assertTrue(parsed.size() > 50, "Expected more than 50 statistics, got: " + parsed.size());
    }

    @Test
    void player1OldFormatStatisticsContainsExpectedKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertTrue(parsed.containsKey("stat.deaths"));
        assertTrue(parsed.containsKey("stat.mobKills"));
        assertTrue(parsed.containsKey("stat.playerKills"));
        assertTrue(parsed.containsKey("stat.playOneMinute"));
        assertTrue(parsed.containsKey("stat.craftingTableInteraction"));
        assertTrue(parsed.containsKey("stat.furnaceInteraction"));
    }

    @Test
    void player1OldFormatStatisticsHasCorrectValues() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertEquals(5, parsed.get("stat.deaths"));
        assertEquals(27, parsed.get("stat.mobKills"));
        assertEquals(1, parsed.get("stat.playerKills"));
        assertEquals(76410, parsed.get("stat.playOneMinute"));
        assertEquals(11, parsed.get("stat.craftingTableInteraction"));
        assertEquals(35, parsed.get("stat.furnaceInteraction"));
    }

    @Test
    void player2OldFormatStatisticsContainsExpectedKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/a5bdfc31-36de-3836-80a7-fdf2760901fc.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertTrue(parsed.containsKey("stat.mobKills"));
        assertTrue(parsed.containsKey("stat.playOneMinute"));
        assertTrue(parsed.containsKey("stat.craftingTableInteraction"));
    }

    @Test
    void player2OldFormatStatisticsHasCorrectValues() throws IOException {
        Resource resource = createOldFormatResource("statistics/a5bdfc31-36de-3836-80a7-fdf2760901fc.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertEquals(224, parsed.get("stat.mobKills"));
        assertEquals(50, parsed.get("stat.craftingTableInteraction"));
    }

    @Test
    void player3OldFormatStatisticsContainsExpectedKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/b70c4628-c615-36b4-a127-a79300eefb88.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertTrue(parsed.containsKey("stat.mobKills"));
        assertTrue(parsed.containsKey("stat.playOneMinute"));
        assertTrue(parsed.containsKey("stat.craftingTableInteraction"));
    }

    @Test
    void player3OldFormatStatisticsHasCorrectValues() throws IOException {
        Resource resource = createOldFormatResource("statistics/b70c4628-c615-36b4-a127-a79300eefb88.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertEquals(1040, parsed.get("stat.mobKills"));
        assertEquals(382, parsed.get("stat.craftingTableInteraction"));
    }

    @Test
    void player4OldFormatStatisticsContainsExpectedKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/c246a8a0-3b84-3426-a4de-dddfb2240775.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertTrue(parsed.containsKey("stat.deaths"));
        assertTrue(parsed.containsKey("stat.mobKills"));
        assertTrue(parsed.containsKey("stat.playOneMinute"));
    }

    @Test
    void player4OldFormatStatisticsHasCorrectValues() throws IOException {
        Resource resource = createOldFormatResource("statistics/c246a8a0-3b84-3426-a4de-dddfb2240775.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        assertEquals(2, parsed.get("stat.deaths"));
        assertEquals(99, parsed.get("stat.mobKills"));
        assertEquals(24, parsed.get("stat.craftingTableInteraction"));
    }

    @ParameterizedTest
    @MethodSource("statisticsFiles")
    void oldFormatAllFilesHavePositiveValues(String resourceFile) throws IOException {
        Resource resource = createOldFormatResource(resourceFile);
        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        for (Map.Entry<String, Integer> entry : parsed.entrySet()) {
            assertTrue(entry.getValue() >= 0,
                    "Statistic '" + entry.getKey() + "' should have a non-negative value, but was: " + entry.getValue());
        }
    }

    @Test
    void oldFormatStatisticNamesAreDotted() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        for (String key : parsed.keySet()) {
            assertTrue(key.startsWith("stat."),
                    "Statistic key should start with 'stat.', but was: " + key);
        }
    }

    @Test
    void oldFormatStatisticNamesAreTruncatedToMaxLength() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = MinecraftStatistics.parse(resource);

        for (String key : parsed.keySet()) {
            assertTrue(key.length() <= 750,
                    "Statistic key should not exceed 750 characters, but was: " + key.length() + " (" + key + ")");
        }
    }
}