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
package com.djrapitops.plan;

import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.gathering.afk.AFKTracker;
import com.djrapitops.plan.gathering.listeners.nukkit.NukkitAFKListener;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * ServerShutdownSave implementation for Nukkit based servers.
 *
 * @author AuroraLS3
 */
@Singleton
public class NukkitServerShutdownSave extends ServerShutdownSave {

    @Inject
    public NukkitServerShutdownSave(
            Locale locale,
            DBSystem dbSystem,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        super(locale, dbSystem, logger, errorLogger);
    }

    @Override
    protected boolean checkServerShuttingDownStatus() {
        return false; // No check implementation for Nukkit yet, JVM shutdown save used instead.
    }

    @Override
    public Optional<AFKTracker> getAfkTracker() {
        return Optional.ofNullable(NukkitAFKListener.getAfkTracker());
    }
}