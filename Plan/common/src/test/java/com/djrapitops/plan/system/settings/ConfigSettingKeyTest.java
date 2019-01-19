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
package com.djrapitops.plan.system.settings;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.*;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.FieldFetcher;
import utilities.TestResources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to check that configs contain all values required to run the plugin.
 *
 * @author Rsl1122
 */
public class ConfigSettingKeyTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    public static void assertValidDefaultValuesForAllSettings(PlanConfig config, Iterable<Setting> settings) {
        List<String> fails = new ArrayList<>();
        for (Setting setting : settings) {
            checkSettingForFailures(config, setting).ifPresent(fails::add);
        }
        assertTrue(fails.isEmpty(), fails::toString);
    }

    private static Optional<String> checkSettingForFailures(PlanConfig config, Setting setting) {
        try {
            if (!config.contains(setting.getPath())) {
                return Optional.of("Did not contain " + setting.getPath());
            } else {
                config.get(setting);
                return Optional.empty();
            }
        } catch (IllegalStateException validationFailed) {
            return Optional.of(validationFailed.getMessage());
        }
    }

    public static Collection<Setting> getServerSettings() throws IllegalAccessException {
        List<Setting> settings = new ArrayList<>();
        for (Class settingKeyClass : new Class[]{
                DatabaseSettings.class,
                DataGatheringSettings.class,
                DisplaySettings.class,
                ExportSettings.class,
                FormatSettings.class,
                PluginDataSettings.class,
                PluginSettings.class,
                TimeSettings.class,
                WebserverSettings.class
        }) {
            settings.addAll(FieldFetcher.getPublicStaticFields(settingKeyClass, Setting.class));
        }
        return settings;
    }

    public static Collection<Setting> getProxySettings() throws IllegalAccessException {
        List<Setting> settings = new ArrayList<>();
        for (Class settingKeyClass : new Class[]{
                DatabaseSettings.class,
                DisplaySettings.class,
                ExportSettings.class,
                FormatSettings.class,
                PluginSettings.class,
                ProxySettings.class,
                TimeSettings.class,
                WebserverSettings.class
        }) {
            settings.addAll(FieldFetcher.getPublicStaticFields(settingKeyClass, Setting.class));
        }
        settings.add(PluginDataSettings.PLUGIN_BUYCRAFT_SECRET);

        // Server settings contained in the key classes, remove
        settings.remove(PluginSettings.SERVER_NAME);
        settings.remove(PluginSettings.BUNGEE_COPY_CONFIG);
        settings.remove(DatabaseSettings.TYPE);
        settings.remove(DisplaySettings.GRAPH_TPS_THRESHOLD_MED);
        settings.remove(DisplaySettings.GRAPH_TPS_THRESHOLD_HIGH);
        settings.remove(DisplaySettings.GRAPH_DISK_THRESHOLD_MED);
        settings.remove(DisplaySettings.GRAPH_DISK_THRESHOLD_HIGH);
        settings.remove(DisplaySettings.GRAPH_DISK_THRESHOLD_HIGH);
        settings.remove(DisplaySettings.WORLD_ALIASES);
        settings.remove(TimeSettings.ANALYSIS_REFRESH_PERIOD);
        return settings;
    }

    @Test
    public void serverConfigHasValidDefaultValues() throws IOException, IllegalAccessException {
        PlanConfig planConfig = createConfig("config.yml");
        Collection<Setting> settings = getServerSettings();
        assertValidDefaultValuesForAllSettings(planConfig, settings);
    }

    @Test
    public void proxyConfigHasValidDefaultValues() throws IOException, IllegalAccessException {
        PlanConfig planConfig = createConfig("bungeeconfig.yml");
        Collection<Setting> settings = getProxySettings();
        assertValidDefaultValuesForAllSettings(planConfig, settings);
    }

    private PlanConfig createConfig(String copyDefaultSettingsFrom) throws IOException {
        File configFile = temporaryFolder.newFile();
        TestResources.copyResourceIntoFile(configFile, "/" + copyDefaultSettingsFrom);
        return createConfig(configFile);
    }

    private PlanConfig createConfig(File configFile) throws IOException {
        PlanConfig config = new PlanConfig(configFile, null);
        config.save();
        return config;
    }

}