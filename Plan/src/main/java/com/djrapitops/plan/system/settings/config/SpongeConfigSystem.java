/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.settings.config;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Sponge ConfigSystem that disables WebServer and Geolocations on first enable.
 *
 * @author Rsl1122
 */
@Singleton
public class SpongeConfigSystem extends BukkitConfigSystem {

    private boolean firstInstall;

    @Inject
    public SpongeConfigSystem(
            PlanFiles files,
            PlanConfig config,
            Theme theme,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(files, config, theme, logger, errorHandler);
    }

    @Override
    public void enable() throws EnableException {
        firstInstall = !files.getConfigFile().exists();
        super.enable();
    }

    @Override
    protected void copyDefaults() throws IOException {
        super.copyDefaults();
        if (firstInstall) {
            logger.info("§eWebServer and Geolocations disabled by default on Sponge servers. You can enable them in the config:");
            logger.info("§e  " + Settings.WEBSERVER_DISABLED.getPath());
            logger.info("§e  " + Settings.DATA_GEOLOCATIONS.getPath());

            config.set(Settings.WEBSERVER_DISABLED, true);
            config.set(Settings.DATA_GEOLOCATIONS, false);
            config.save();
        }
    }
}