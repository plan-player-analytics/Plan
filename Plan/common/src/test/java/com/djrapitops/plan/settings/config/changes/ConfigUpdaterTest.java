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
package com.djrapitops.plan.settings.config.changes;

import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import utilities.TestResources;
import utilities.TestSettings;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for ConfigUpdater.
 *
 * @author Rsl1122
 */
class ConfigUpdaterTest {

    @TempDir
    public static Path tempDir;

    private static File oldConfig;
    private static File oldBungeeConfig;

    private static Path newConfig;
    private static Path newBungeeConfig;

    private static ConfigUpdater UNDER_TEST;
    private static ErrorLogger errorLogger;

    @BeforeAll
    static void prepareConfigFiles() throws URISyntaxException, IOException {
        oldConfig = tempDir.resolve("config.yml").toFile();
        File configResource = TestResources.getTestResourceFile("config/4.5.2-config.yml", ConfigUpdater.class);
        Files.copy(configResource.toPath(), oldConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);

        oldBungeeConfig = tempDir.resolve("bungeeconfig.yml").toFile();
        File bungeeConfigResource = TestResources.getTestResourceFile("config/4.5.2-bungeeconfig.yml", ConfigUpdater.class);
        Files.copy(bungeeConfigResource.toPath(), oldBungeeConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);

        newConfig = tempDir.resolve("newconfig.yml");
        TestResources.copyResourceIntoFile(newConfig.toFile(), "/assets/plan/config.yml");

        newBungeeConfig = tempDir.resolve("newbungeeconfig.yml");
        TestResources.copyResourceIntoFile(newBungeeConfig.toFile(), "/assets/plan/bungeeconfig.yml");

        PluginLogger testLogger = new TestPluginLogger();
        errorLogger = Mockito.mock(ErrorLogger.class);
        UNDER_TEST = new ConfigUpdater(testLogger, errorLogger);
    }

    @AfterEach
    void ensureNoErrors() {
        Mockito.verifyNoInteractions(errorLogger);
    }

    @Test
    void serverConfigIsPatchedCorrectly() throws IOException, IllegalAccessException {
        Path configPath = tempDir.resolve("oldconfig.yml");
        Files.copy(oldConfig.toPath(), configPath, StandardCopyOption.REPLACE_EXISTING);

        PlanConfig config = new PlanConfig(configPath.toFile(), null, null, new TestPluginLogger());

        UNDER_TEST.applyConfigUpdate(config);

        // Ensure that added settings are present
        copyMissingFrom(config, newConfig);

        TestSettings.assertValidDefaultValuesForAllSettings(config, TestSettings.getServerSettings());
    }

    @Test
    void proxyConfigIsPatchedCorrectly() throws IOException, IllegalAccessException {
        Path configPath = tempDir.resolve("oldconfig.yml");
        Files.copy(oldBungeeConfig.toPath(), configPath, StandardCopyOption.REPLACE_EXISTING);

        PlanConfig config = new PlanConfig(configPath.toFile(), null, null, new TestPluginLogger());

        UNDER_TEST.applyConfigUpdate(config);

        // Ensure that added settings are present
        copyMissingFrom(config, newBungeeConfig);

        TestSettings.assertValidDefaultValuesForAllSettings(config, TestSettings.getProxySettings());
    }

    private void copyMissingFrom(PlanConfig config, Path newBungeeConfig) throws IOException {
        try (ConfigReader reader = new ConfigReader(newBungeeConfig)) {
            config.copyMissing(reader.read());
        }
    }

    @Test
    void serverMoveChangesDoNotLeaveNewEmptyValues() throws IOException {
        Path configPath = tempDir.resolve("oldconfig.yml");
        Files.copy(oldConfig.toPath(), configPath, StandardCopyOption.REPLACE_EXISTING);

        PlanConfig config = new PlanConfig(configPath.toFile(), null, null, new TestPluginLogger());

        ConfigChange[] changes = UNDER_TEST.configEnhancementPatch();
        assertMoveChangesAreAppliedProperly(config, changes);
    }

    @Test
    void proxyMoveChangesDoNotLeaveNewEmptyValues() throws IOException {
        Path configPath = tempDir.resolve("oldconfig.yml");
        Files.copy(oldBungeeConfig.toPath(), configPath, StandardCopyOption.REPLACE_EXISTING);

        PlanConfig config = new PlanConfig(configPath.toFile(), null, null, new TestPluginLogger());

        ConfigChange[] changes = UNDER_TEST.configEnhancementPatch();
        assertMoveChangesAreAppliedProperly(config, changes);
    }

    private void assertMoveChangesAreAppliedProperly(PlanConfig config, ConfigChange[] changes) {
        for (ConfigChange change : changes) {
            if (change.hasBeenApplied(config)) {
                continue;
            }

            if (change instanceof ConfigChange.Moved) {
                ConfigChange.Moved move = (ConfigChange.Moved) change;
                String expected = config.getString(move.oldPath);

                move.apply(config);

                assertEquals(expected, config.getString(move.newPath));
            }
        }
    }
}