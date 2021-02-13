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
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.network.ServerSettingsManager;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Sponge ConfigSystem that disables Geolocations on first enable.
 *
 * @author AuroraLS3
 */
@Singleton
public class SpongeConfigSystem extends BukkitConfigSystem {

    private boolean firstInstall;

    @Inject
    public SpongeConfigSystem(
            PlanFiles files,
            PlanConfig config,
            ConfigUpdater configUpdater,
            ServerSettingsManager serverSettingsManager,
            Theme theme,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        super(files, config, configUpdater, serverSettingsManager, theme, logger, errorLogger);
    }

    @Override
    public void enable() {
        firstInstall = !files.getConfigFile().exists();
        super.enable();
    }

    @Override
    protected void copyDefaults() throws IOException {
        super.copyDefaults();
        if (firstInstall) {
            logger.info("§eGeolocations disabled by default on Sponge servers. You can enable them in the config:");
            logger.info("§e  " + DataGatheringSettings.GEOLOCATIONS.getPath());

            config.set(DataGatheringSettings.GEOLOCATIONS, false);
            config.save();
        }
    }
}