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
package utilities;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.*;
import com.djrapitops.plan.settings.config.paths.key.Setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Utility for testing that configs contain all appropriate values.
 */
public class TestSettings {
    public static void assertValidDefaultValuesForAllSettings(PlanConfig config, Iterable<Setting> settings) {
        List<String> fails = new ArrayList<>();
        for (Setting<?> setting : settings) {
            checkSettingForFailures(config, setting).ifPresent(fails::add);
        }
        assertTrue(fails.isEmpty(), fails::toString);
    }

    private static Optional<String> checkSettingForFailures(PlanConfig config, Setting<?> setting) {
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
        for (Class<?> settingKeyClass : new Class[]{
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
        // Server settings contained in the key classes, remove
        settings.remove(PluginSettings.PROXY_COPY_CONFIG);
        settings.remove(DatabaseSettings.TYPE);
        settings.remove(DisplaySettings.WORLD_ALIASES);
        return settings;
    }
}
