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

import com.djrapitops.plan.storage.file.FileResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.TestResources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test for {@link ConfigWriter}.
 *
 * @author AuroraLS3
 */
class ConfigWriterTest {

    private Path tempFolder;

    @BeforeEach
    void prepareTempFolder(@TempDir Path tempFolder) {
        this.tempFolder = tempFolder;
    }

    @Test
    void defaultConfigIsWrittenCorrectly() throws IOException {
        File original = tempFolder.resolve("loaded.yml").toFile();
        File written = tempFolder.resolve("written.yml").toFile();

        TestResources.copyResourceIntoFile(original, "/assets/plan/config.yml");

        try (ConfigReader reader = new ConfigReader(Files.newInputStream(original.toPath()))) {
            new ConfigWriter(written.toPath()).write(reader.read());
        }

        List<String> originalLines = FileResource.lines(original);
        List<String> writtenLines = FileResource.lines(written);

        assertFalse(originalLines.isEmpty());
        assertFalse(writtenLines.isEmpty());

        StringBuilder differing = new StringBuilder();
        for (int i = 0; i < originalLines.size(); i++) {
            String origLine = originalLines.get(i);
            String testLine = writtenLines.get(i).replace("    ", "  ");
            if (!origLine.equals(testLine)) {
                differing.append(i + 1).append("! ").append(origLine).append("\n");
                differing.append(i + 1).append("! ").append(testLine).append("\n\n");
            }
        }
        assertEquals(0, differing.length(), differing::toString);
    }

    @Test
    void indentedConfigIsWrittenCorrectly() throws IOException {
        File original = tempFolder.resolve("loaded.yml").toFile();
        File indented = tempFolder.resolve("indented.yml").toFile();
        File written = tempFolder.resolve("written.yml").toFile();

        TestResources.copyResourceIntoFile(original, "/assets/plan/config.yml");

        try (ConfigReader reader = new ConfigReader(Files.newInputStream(original.toPath()))) {
            new ConfigWriter(indented.toPath()).write(reader.read());
        }
        try (ConfigReader reader = new ConfigReader(Files.newInputStream(indented.toPath()))) {
            new ConfigWriter(written.toPath()).write(reader.read());
        }

        List<String> originalLines = FileResource.lines(indented);
        List<String> writtenLines = FileResource.lines(written);

        assertFalse(originalLines.isEmpty());
        assertFalse(writtenLines.isEmpty());

        StringBuilder differing = new StringBuilder();
        for (int i = 0; i < originalLines.size(); i++) {
            String origLine = originalLines.get(i);
            String testLine = writtenLines.get(i);
            if (!origLine.equals(testLine)) {
                differing.append(i + 1).append("! ").append(origLine).append("\n");
                differing.append(i + 1).append("! ").append(testLine).append("\n\n");
            }
        }
        assertEquals(0, differing.length(), differing::toString);
    }

    @Test
    void listIndent() throws IOException {
        ConfigNode root = new ConfigNode(null, null, null);
        root.addNode("Test").set(Arrays.asList("First", "Second", "Third"));

        Path out = tempFolder.resolve("listIndent.yml");
        new ConfigWriter(out).write(root);

        List<String> writtenLines = FileResource.lines(out.toFile());
        List<String> expected = Arrays.asList(
                "Test:",
                "  - \"First\"",
                "  - \"Second\"",
                "  - \"Third\""
        );
        assertEquals(expected, writtenLines);
    }

    @Test
    void listIndentSecondLevel() throws IOException {
        ConfigNode root = new ConfigNode(null, null, null);
        root.addNode("Test").addNode("List").set(Arrays.asList("First", "Second", "Third"));

        Path out = tempFolder.resolve("listIndent.yml");
        new ConfigWriter(out).write(root);

        List<String> writtenLines = FileResource.lines(out.toFile());
        List<String> expected = Arrays.asList(
                "Test:",
                "    List:",
                "      - \"First\"",
                "      - \"Second\"",
                "      - \"Third\""
        );
        assertEquals(expected, writtenLines);
    }

    @Test
    void valueAfterAList() throws IOException {
        ConfigNode root = new ConfigNode(null, null, null);
        ConfigNode test = root.addNode("Test");
        test.addNode("List").set(Arrays.asList("First", "Second", "Third"));
        test.addNode("Value").set("Example");
        root.addNode("Second").set(2);

        Path out = tempFolder.resolve("listIndent.yml");
        new ConfigWriter(out).write(root);

        List<String> writtenLines = FileResource.lines(out.toFile());
        List<String> expected = Arrays.asList(
                "Test:",
                "    List:",
                "      - \"First\"",
                "      - \"Second\"",
                "      - \"Third\"",
                "    Value: \"Example\"",
                "Second: 2"
        );
        assertEquals(expected, writtenLines);
    }

}