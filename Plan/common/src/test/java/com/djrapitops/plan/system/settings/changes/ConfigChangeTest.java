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
package com.djrapitops.plan.system.settings.changes;

import com.djrapitops.plan.system.settings.config.Config;
import com.djrapitops.plan.system.settings.config.ConfigNode;
import com.djrapitops.plan.system.settings.config.ConfigReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class ConfigChangeTest {

    private Path temporaryFolder;

    private Config config;

    @BeforeEach
    void prepareTemporaryFolder(@TempDirectory.TempDir Path temporaryFolder) {
        this.temporaryFolder = temporaryFolder;
    }

    private Config prepareConfig(String withValue) {
        try (ConfigReader reader = new ConfigReader(new Scanner(withValue))) {
            return new Config(temporaryFolder.resolve("config.yml").toFile(), reader.read());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void moveChangeRecognizesItHasNotBeenApplied() {
        config = prepareConfig("Test: 'value'");

        assertFalse(new ConfigChange.Moved("Test", "MovedTo").hasBeenApplied(config));
    }

    @Test
    void configParentIsSameObject() {
        config = prepareConfig("Test: 'value'");
        ConfigNode node = config.getConfigNode("Test");
        ConfigNode parent = node.getParent();
        assertTrue(parent instanceof Config);
        assertSame(config, parent);
    }

    @Test
    void configParentChildrenIsSameObject() {
        config = prepareConfig("Test: 'value'");
        ConfigNode node = config.getConfigNode("Test");
        ConfigNode parent = node.getParent();
        assertSame(config.getChildren(), parent.getChildren());
    }

    @Test
    void moveChangeRecognizesItHasBeenApplied() {
        config = prepareConfig("Test: 'value'");

        ConfigChange change = new ConfigChange.Moved("Test", "MovedTo");
        change.apply(config);

        assertFalse(config.getChildren().containsKey("Test"));
        assertFalse(config.contains("Test"), "Old node was not removed");
        assertTrue(config.contains("MovedTo"), "New node was not created");
        assertTrue(change.hasBeenApplied(config), "Did not recognize it has been applied");
    }

    @Test
    void stringSettingWithQuotesIsMovedAsString() {
        config = prepareConfig("Test: 'value'");

        new ConfigChange.Moved("Test", "MovedTo").apply(config);

        assertFalse(config.contains("Test"), "Old node was not removed");
        assertTrue(config.contains("MovedTo"), "New node was not created");
        String result = config.getConfigNode("MovedTo").getValue();
        assertEquals("value", result);
    }

    @Test
    void stringSettingWithDoubleQuotesIsMovedAsString() {
        config = prepareConfig("Test: \"value\"");

        new ConfigChange.Moved("Test", "MovedTo").apply(config);

        assertFalse(config.contains("Test"), "Old node was not removed");
        assertTrue(config.contains("MovedTo"), "New node was not created");
        String result = config.getConfigNode("MovedTo").getValue();
        assertEquals("value", result);
    }

    @Test
    void stringSettingWithQuotedDoubleQuotesIsMovedAsString() {
        config = prepareConfig("Test: '\"value\"'");

        new ConfigChange.Moved("Test", "MovedTo").apply(config);

        assertFalse(config.contains("Test"), "Old node was not removed");
        assertTrue(config.contains("MovedTo"), "New node was not created");
        String result = config.getConfigNode("MovedTo").getValue();
        assertEquals("\"value\"", result);
    }

    @Test
    void stringSettingWithQuotedQuotesIsMovedAsString() {
        config = prepareConfig("Test: \"'value'\"");

        new ConfigChange.Moved("Test", "MovedTo").apply(config);

        assertFalse(config.contains("Test"), "Old node was not removed");
        assertTrue(config.contains("MovedTo"), "New node was not created");
        String result = config.getConfigNode("MovedTo").getValue();
        assertEquals("'value'", result);
    }

    @Test
    void removedSettingIsNotPresent() {
        config = prepareConfig("Test: \"value\"");

        new ConfigChange.Removed("Test").apply(config);

        assertFalse(config.contains("Test"), "Old node was not removed");
    }

}