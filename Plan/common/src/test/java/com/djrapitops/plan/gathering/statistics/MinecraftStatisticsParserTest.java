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
package com.djrapitops.plan.gathering.statistics;

import com.djrapitops.plan.storage.file.Resource;
import com.djrapitops.plan.storage.file.StringResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MinecraftStatisticsParser}.
 *
 * @author AuroraLS3
 */
class MinecraftStatisticsParserTest {

    private static final List<String> STATISTICS_FILES = List.of(
            "statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json",
            "statistics/a5bdfc31-36de-3836-80a7-fdf2760901fc.json",
            "statistics/b70c4628-c615-36b4-a127-a79300eefb88.json",
            "statistics/c246a8a0-3b84-3426-a4de-dddfb2240775.json"
    );

    private MinecraftStatisticsParser parser;

    static Stream<String> statisticsFiles() {
        return STATISTICS_FILES.stream();
    }

    @BeforeEach
    void setUp() throws IOException {
        LegacyStatisticTranslator translator = new LegacyStatisticTranslator(
                createClasspathResource("assets/plan/mappings/stats/rules.json"),
                createClasspathResource("assets/plan/mappings/stats/custom.json"),
                createClasspathResource("assets/plan/mappings/stats/unsupported.json"),
                createClasspathResource("assets/plan/mappings/stats/entity_aliases.json"),
                createClasspathResource("assets/plan/mappings/stats/block_aliases.json"),
                createClasspathResource("assets/plan/mappings/stats/item_aliases.json")
        );
        parser = new MinecraftStatisticsParser(translator);
    }

    /**
     * Creates a {@link Resource} from a classpath resource file.
     */
    private Resource createClasspathResource(String classpathResource) throws IOException {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathResource)) {
            assertNotNull(stream, "Resource not found: " + classpathResource);
            byte[] bytes = stream.readAllBytes();
            return createResource(classpathResource, new String(bytes, StandardCharsets.UTF_8));
        }
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
        return new StringResource(name, json, 0);
    }

    @ParameterizedTest
    @MethodSource("statisticsFiles")
    void oldFormatReturnsStatisticsFromResourceFile(String resourceFile) throws IOException {
        Resource resource = createOldFormatResource(resourceFile);

        Map<String, Integer> parsed = parser.parse(resource);

        assertNotNull(parsed);
        assertFalse(parsed.isEmpty(), "Parsed statistics should not be empty");
        assertTrue(parsed.size() > 50, "Expected more than 50 statistics, got: " + parsed.size());
    }

    @ParameterizedTest
    @MethodSource("statisticsFiles")
    void newFormatReturnsStatisticsFromResourceFile(String resourceFile) throws IOException {
        Resource resource = createNewFormatResource(resourceFile);

        Map<String, Integer> parsed = parser.parse(resource);

        assertNotNull(parsed);
        assertFalse(parsed.isEmpty(), "Parsed statistics should not be empty");
        assertTrue(parsed.size() > 50, "Expected more than 50 statistics, got: " + parsed.size());
    }

    @Test
    void player1OldFormatStatisticsContainsExpectedKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertTrue(parsed.containsKey("minecraft.custom:minecraft.deaths"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.mob_kills"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.player_kills"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.play_time"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.interact_with_crafting_table"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.interact_with_furnace"));
    }

    @Test
    void player1OldFormatStatisticsHasCorrectValues() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertEquals(5, parsed.get("minecraft.custom:minecraft.deaths"));
        assertEquals(27, parsed.get("minecraft.custom:minecraft.mob_kills"));
        assertEquals(1, parsed.get("minecraft.custom:minecraft.player_kills"));
        assertEquals(76410, parsed.get("minecraft.custom:minecraft.play_time"));
        assertEquals(11, parsed.get("minecraft.custom:minecraft.interact_with_crafting_table"));
        assertEquals(35, parsed.get("minecraft.custom:minecraft.interact_with_furnace"));
    }

    @Test
    void player2OldFormatStatisticsContainsExpectedKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/a5bdfc31-36de-3836-80a7-fdf2760901fc.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertTrue(parsed.containsKey("minecraft.custom:minecraft.mob_kills"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.play_time"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.interact_with_crafting_table"));
    }

    @Test
    void player2OldFormatStatisticsHasCorrectValues() throws IOException {
        Resource resource = createOldFormatResource("statistics/a5bdfc31-36de-3836-80a7-fdf2760901fc.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertEquals(224, parsed.get("minecraft.custom:minecraft.mob_kills"));
        assertEquals(50, parsed.get("minecraft.custom:minecraft.interact_with_crafting_table"));
    }

    @Test
    void player3OldFormatStatisticsContainsExpectedKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/b70c4628-c615-36b4-a127-a79300eefb88.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertTrue(parsed.containsKey("minecraft.custom:minecraft.mob_kills"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.play_time"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.interact_with_crafting_table"));
    }

    @Test
    void player3OldFormatStatisticsHasCorrectValues() throws IOException {
        Resource resource = createOldFormatResource("statistics/b70c4628-c615-36b4-a127-a79300eefb88.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertEquals(1040, parsed.get("minecraft.custom:minecraft.mob_kills"));
        assertEquals(382, parsed.get("minecraft.custom:minecraft.interact_with_crafting_table"));
    }

    @Test
    void player4OldFormatStatisticsContainsExpectedKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/c246a8a0-3b84-3426-a4de-dddfb2240775.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertTrue(parsed.containsKey("minecraft.custom:minecraft.deaths"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.mob_kills"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.play_time"));
    }

    @Test
    void player4OldFormatStatisticsHasCorrectValues() throws IOException {
        Resource resource = createOldFormatResource("statistics/c246a8a0-3b84-3426-a4de-dddfb2240775.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertEquals(2, parsed.get("minecraft.custom:minecraft.deaths"));
        assertEquals(99, parsed.get("minecraft.custom:minecraft.mob_kills"));
        assertEquals(24, parsed.get("minecraft.custom:minecraft.interact_with_crafting_table"));
    }

    @ParameterizedTest
    @MethodSource("statisticsFiles")
    void oldFormatAllFilesHavePositiveValues(String resourceFile) throws IOException {
        Resource resource = createOldFormatResource(resourceFile);
        Map<String, Integer> parsed = parser.parse(resource);

        for (Map.Entry<String, Integer> entry : parsed.entrySet()) {
            assertTrue(entry.getValue() >= 0,
                    "Statistic '" + entry.getKey() + "' should have a non-negative value, but was: " + entry.getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("statisticsFiles")
    void allLegacyStatsAreTranslated(String resourceFile) throws IOException {
        Resource resource = createOldFormatResource(resourceFile);
        Map<String, Integer> parsed = parser.parse(resource);
        List<String> result = parsed.keySet().stream()
                .filter(key -> !key.startsWith("minecraft."))
                .toList();
        assertEquals(List.of(), result);
    }

    @Test
    void oldFormatStatisticNamesAreTruncatedToMaxLength() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = parser.parse(resource);

        for (String key : parsed.keySet()) {
            assertTrue(key.length() <= 750,
                    "Statistic key should not exceed 750 characters, but was: " + key.length() + " (" + key + ")");
        }
    }

    @Test
    void oldFormatTranslatesLegacyCustomKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = parser.parse(resource);

        // Custom keys are translated to modern namespaced equivalents
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.deaths"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.mob_kills"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.play_time"));
        assertTrue(parsed.containsKey("minecraft.custom:minecraft.interact_with_crafting_table"));
    }

    @Test
    void oldFormatTranslatesLegacyEntityKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertTrue(parsed.containsKey("minecraft.killed:minecraft.cow"));
    }

    @Test
    void oldFormatTranslatesLegacyBlockKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/46d2fd33-a4fc-3788-98a7-dcb4ceddcfbd.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertTrue(parsed.containsKey("minecraft.mined:minecraft.stone"));
    }

    @Test
    void oldFormatTranslatesLegacyItemKeys() throws IOException {
        Resource resource = createOldFormatResource("statistics/c246a8a0-3b84-3426-a4de-dddfb2240775.json");

        Map<String, Integer> parsed = parser.parse(resource);

        assertTrue(parsed.containsKey("minecraft.crafted:minecraft.stone_axe"));
        assertTrue(parsed.containsKey("minecraft.used:minecraft.bow"));
        assertTrue(parsed.containsKey("minecraft.picked_up:minecraft.string"));
    }

}