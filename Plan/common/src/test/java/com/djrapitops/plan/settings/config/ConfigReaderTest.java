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
package com.djrapitops.plan.settings.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link ConfigReader}
 *
 * @author AuroraLS3
 */
class ConfigReaderTest {

    @Test
    void valueAfterAList() {
        String read = "Test:\n" +
                "  List:\n" +
                "    - First\n" +
                "    - Second\n" +
                "    - Third\n" +
                "  Value: Example";

        try (ConfigReader reader = new ConfigReader(new Scanner(read))) {
            Config readConfig = reader.read();

            assertTrue(readConfig.getNode("Test.List").isPresent());
            assertTrue(readConfig.getNode("Test.Value").isPresent());
            assertFalse(readConfig.getNode("Test.List.Value").isPresent());

            assertEquals("Example", readConfig.getString("Test.Value"));
            assertEquals(Arrays.asList("First", "Second", "Third"), readConfig.getStringList("Test.List"));
        }
    }


    @Test
    void valueAfterAListQuoted() {
        String read = "Test:\n" +
                "  List:\n" +
                "    - \"First\"\n" +
                "    - \"Second\"\n" +
                "    - \"Third\"\n" +
                "  Value: \"Example\"";

        try (ConfigReader reader = new ConfigReader(new Scanner(read))) {
            Config readConfig = reader.read();

            assertTrue(readConfig.getNode("Test.List").isPresent());
            assertTrue(readConfig.getNode("Test.Value").isPresent());
            assertFalse(readConfig.getNode("Test.List.Value").isPresent());

            assertEquals("Example", readConfig.getString("Test.Value"));
            assertEquals(Arrays.asList("First", "Second", "Third"), readConfig.getStringList("Test.List"));
        }
    }

    @Test
    void valueAfterAListRealWorld() {
        String read = "Plugins:\n" +
                "  Factions:\n" +
                "    HideFactions:\n" +
                "      - ExampleFaction\n" +
                "  Towny:\n" +
                "    HideTowns:\n" +
                "      - ExampleTown\n" +
                "    Enabled: true";

        try (ConfigReader reader = new ConfigReader(new Scanner(read))) {
            Config readConfig = reader.read();

            assertTrue(readConfig.getNode("Plugins.Towny.HideTowns").isPresent());
            assertTrue(readConfig.getNode("Plugins.Towny.Enabled").isPresent());
            assertFalse(readConfig.getNode("Plugins.Towny.HideTowns.Enabled").isPresent());
        }
    }

}