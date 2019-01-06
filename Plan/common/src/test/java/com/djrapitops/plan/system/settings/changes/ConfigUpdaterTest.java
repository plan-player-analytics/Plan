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

import com.djrapitops.plan.system.settings.ConfigSettingKeyTest;
import com.djrapitops.plan.system.settings.config.ConfigReader;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.junitpioneer.jupiter.TempDirectory;
import utilities.TestResources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@RunWith(JUnitPlatform.class)
@ExtendWith(TempDirectory.class)
class ConfigUpdaterTest {

    private static Path tempDir;

    private static File oldConfig;
    private static File oldBungeeConfig;

    private static Path newConfig;
    private static Path newBungeeConfig;

    private static ConfigUpdater UNDER_TEST;

    @BeforeAll
    static void prepareConfigFiles(@TempDirectory.TempDir Path temporaryDir) throws URISyntaxException, IOException {
        tempDir = temporaryDir;

        oldConfig = tempDir.resolve("config.yml").toFile();
        File configResource = TestResources.getTestResourceFile("config/4.5.2-config.yml", ConfigUpdater.class);
        Files.copy(configResource.toPath(), oldConfig.toPath());

        oldBungeeConfig = tempDir.resolve("bungeeconfig.yml").toFile();
        File bungeeConfigResource = TestResources.getTestResourceFile("config/4.5.2-bungeeconfig.yml", ConfigUpdater.class);
        Files.copy(bungeeConfigResource.toPath(), oldBungeeConfig.toPath());

        newConfig = tempDir.resolve("newconfig.yml");
        TestResources.copyResourceIntoFile(newConfig.toFile(), "/config.yml");

        newBungeeConfig = tempDir.resolve("newbungeeconfig.yml");
        TestResources.copyResourceIntoFile(newBungeeConfig.toFile(), "/bungeeconfig.yml");

        PluginLogger testLogger = new TestPluginLogger();
        UNDER_TEST = new ConfigUpdater(testLogger, new ConsoleErrorLogger(testLogger));
    }

    @Test
    void serverConfigIsPatchedCorrectly() throws IOException, IllegalAccessException {
        PlanConfig planConfig = new PlanConfig(oldConfig, null);

        UNDER_TEST.applyConfigUpdate(planConfig);

        // Ensure that added settings are present
        copyMissingFrom(planConfig, newConfig);

        Collection<Setting> settings = ConfigSettingKeyTest.getServerSettings();
        ConfigSettingKeyTest.assertValidDefaultValuesForAllSettings(planConfig, settings);
    }

    @Test
    void bungeeConfigIsPatchedCorrectly() throws IOException, IllegalAccessException {
        PlanConfig planConfig = new PlanConfig(oldBungeeConfig, null);

        UNDER_TEST.applyConfigUpdate(planConfig);

        // Ensure that added settings are present
        copyMissingFrom(planConfig, newBungeeConfig);

        Collection<Setting> settings = ConfigSettingKeyTest.getProxySettings();
        ConfigSettingKeyTest.assertValidDefaultValuesForAllSettings(planConfig, settings);
    }

    private void copyMissingFrom(PlanConfig planConfig, Path newBungeeConfig) throws IOException {
        try (ConfigReader reader = new ConfigReader(newBungeeConfig)) {
            planConfig.copyMissing(reader.read());
        }
    }

}