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
package com.djrapitops.plan.settings;

import com.djrapitops.plan.settings.config.PlanConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.TestPluginLogger;
import utilities.TestResources;
import utilities.TestSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to check that configs contain all values required to run the plugin.
 *
 * @author AuroraLS3
 */
class ConfigSettingKeyTest {

    public static Path temporaryFolder;

    @BeforeAll
    static void prepareTempDir(@TempDir Path tempDir) {
        temporaryFolder = tempDir;
    }

    @Test
    @DisplayName("config.yml has valid default values")
    void serverConfigHasValidDefaultValues() throws IOException, IllegalAccessException {
        PlanConfig config = createConfig("config.yml");
        TestSettings.assertValidDefaultValuesForAllSettings(config, TestSettings.getServerSettings());
    }

    @Test
    @DisplayName("bungeeconfig.yml has valid default values")
    void proxyConfigHasValidDefaultValues() throws IOException, IllegalAccessException {
        PlanConfig config = createConfig("bungeeconfig.yml");
        TestSettings.assertValidDefaultValuesForAllSettings(config, TestSettings.getProxySettings());
    }

    @Test
    @DisplayName("config.yml key paths don't overlap")
    void serverConfigHasUniqueKeyPaths() throws IOException {
        PlanConfig config = createConfig("config.yml");
        checkConfigPathConflicts(config);
    }

    @Test
    @DisplayName("bungeeconfig.yml key paths don't overlap")
    void proxyConfigHasUniqueKeyPaths() throws IOException {
        PlanConfig config = createConfig("bungeeconfig.yml");
        checkConfigPathConflicts(config);
    }

    private void checkConfigPathConflicts(PlanConfig config) {
        List<Object> result = config.dfs((node, results) -> {
            String value = node.getString();
            boolean hasChildren = node.getChildren().isEmpty();
            boolean hasValue = value != null && !value.isEmpty();
            if (!hasChildren && hasValue) {
                results.add(node.getKey(true));
            }
        });
        assertTrue(result.isEmpty(), () -> "Following nodes have values and children: " + result);
    }

    private PlanConfig createConfig(String copyDefaultSettingsFrom) throws IOException {
        File configFile = Files.createTempFile(temporaryFolder, "config", ".yml").toFile();
        TestResources.copyResourceIntoFile(configFile, "/assets/plan/" + copyDefaultSettingsFrom);
        return createConfig(configFile);
    }

    private PlanConfig createConfig(File configFile) throws IOException {
        PlanConfig config = new PlanConfig(configFile, null, null, new TestPluginLogger());
        config.save();
        return config;
    }

}