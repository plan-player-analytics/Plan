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
package com.djrapitops.plan.version;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.ore.OreVersionInfoLoader;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * System for checking if new Version is available when the System initializes, using the Ore API.
 */
@Singleton
public class SpongeVersionChecker extends VersionChecker {
    @Inject
    public SpongeVersionChecker(
            @Named("currentVersion") String currentVersion,
            Locale locale,
            PlanConfig config,
            PluginLogger logger,
            RunnableFactory runnableFactory,
            ErrorLogger errorLogger
    ) {
        super(currentVersion, locale, config, logger, runnableFactory, errorLogger);
    }

    @Override
    protected Optional<List<VersionInfo>> loadVersionInfo() {
        try {
            return Optional.of(OreVersionInfoLoader.load());
        } catch (IOException e) {
            logger.warn("Failed to check updates from Ore (" + e.getMessage() + "), allow connection or disable update check from Plan config");
            return Optional.empty();
        }
    }
}
