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
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.java.Reflection;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ServerShutdownSave implementation for Bukkit based servers.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitServerShutdownSave extends ServerShutdownSave {

    @Inject
    public BukkitServerShutdownSave(
            Locale locale,
            DBSystem dbSystem,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        super(locale, dbSystem, logger, errorLogger);
    }

    @Override
    protected boolean checkServerShuttingDownStatus() {
        try {
            return performCheck();
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            logger.debug("Server shutdown check failed, using JVM ShutdownHook instead. Error: " + e.toString());
            return false; // ShutdownHook handles save in case this fails upon plugin disable.
        }
    }

    private boolean performCheck() {
        // Special thanks to Fuzzlemann for figuring out the methods required for this check.
        // https://github.com/plan-player-analytics/Plan/issues/769#issuecomment-433898242
        Class<?> minecraftServerClass = Reflection.getMinecraftClass("MinecraftServer");
        Object minecraftServer = Reflection.getField(minecraftServerClass, "SERVER", minecraftServerClass).get(null);

        return Reflection.getField(minecraftServerClass, "isStopped", boolean.class).get(minecraftServer);
    }
}