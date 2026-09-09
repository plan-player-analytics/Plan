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
package com.djrapitops.plan.settings.theme;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

/**
 * Enum that contains available themes.
 * <p>
 * Change config setting Theme.Base to select one.
 *
 * @author AuroraLS3
 */
@Singleton
public class Theme implements SubSystem {

    private final PlanFiles files;
    private final PlanConfig config;
    private final PluginLogger logger;

    @Inject
    public Theme(PlanFiles files, PlanConfig config, PluginLogger logger) {
        this.files = files;
        this.config = config;
        this.logger = logger;
    }

    public String[] getWorldPieColors() {
        return Arrays.stream(StringUtils.split(config.get(DisplaySettings.WORLD_PIE), ','))
                .map(color -> StringUtils.remove(StringUtils.trim(color), '"'))
                .toArray(String[]::new);
    }

    public String[] getDefaultPieColors(ThemeVal val) {
        return Arrays.stream(StringUtils.split(val.getDefaultValue(), ','))
                .map(color -> StringUtils.remove(StringUtils.trim(color), '"'))
                .toArray(String[]::new);
    }

    @Override
    public void enable() {
        if (files.getFileFromPluginFolder("theme.yml").exists()) {
            migrateThemeYmlValues();
        }
    }

    private void migrateThemeYmlValues() {
        ThemeConfig themeConfig = new ThemeConfig(files, config, logger);
        if (containsNonDefaultValues(themeConfig)) {
            if (themeConfig.contains("GraphColors.WorldPie")) {
                logger.info("Copied theme.yml 'GraphColors.WorldPie' to config.yml '" + DisplaySettings.WORLD_PIE.getPath() + "'");
                config.set(DisplaySettings.WORLD_PIE, themeConfig.getString("GraphColors.WorldPie"));
            }
            logger.warn("'theme.yml' file has been deprecated in favor of theme-editor on the website. Please delete it manually after noting necessary details (modifications from default were detected.)");
        } else {
            try {
                logger.info("Deleting deprecated 'theme.yml' file automatically since it contains only default values.");
                Files.deleteIfExists(ThemeConfig.getConfigFile(files).toPath());
            } catch (IOException e) {
                logger.warn("'theme.yml' failed to be deleted automatically (" + e.getMessage() + "). Please delete it manually.");
            }
        }
    }

    @VisibleForTesting
    boolean containsNonDefaultValues(ThemeConfig themeConfig) {
        ConfigNode defaults = ThemeConfig.getDefaults(files, config, logger);
        for (ThemeVal value : ThemeVal.values()) {
            if (!Objects.equals(defaults.getString(value.getThemePath()), themeConfig.getString(value.getThemePath()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void disable() {
        // No need to save theme on disable
    }
}
