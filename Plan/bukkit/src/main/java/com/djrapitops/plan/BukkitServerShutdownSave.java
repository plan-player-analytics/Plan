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
import com.djrapitops.plan.gathering.listeners.bukkit.BukkitAFKListener;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.java.Reflection;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * ServerShutdownSave implementation for Bukkit based servers.
 *
 * @author AuroraLS3
 */
@Singleton
public class BukkitServerShutdownSave extends ServerShutdownSave {

    private static final String IS_STOPPED = "isStopped";

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
        return isStoppedBefore1p17() || isStoppedAfterV1p17();
    }

    @Override
    public Optional<AFKTracker> getAfkTracker() {
        return Optional.ofNullable(BukkitAFKListener.getAfkTracker());
    }

    private boolean isStoppedBefore1p17() {
        try {
            // Special thanks to Fuzzlemann for figuring out the methods required for this check.
            // https://github.com/plan-player-analytics/Plan/issues/769#issuecomment-433898242
            Class<?> minecraftServerClass = Reflection.getMinecraftClass("MinecraftServer");
            return Reflection.getField(minecraftServerClass, IS_STOPPED, boolean.class).get(Reflection.getMinecraftServer().orElse(null));
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            return false;
        }
    }

    private boolean isStoppedAfterV1p17() {
        try {
            // Special thanks to Fuzzlemann for figuring out the methods required for this check.
            // https://github.com/plan-player-analytics/Plan/issues/769#issuecomment-433898242
            Class<?> minecraftServerClass = Reflection.getMinecraftClass("MinecraftServer");
            return (Boolean) minecraftServerClass.getMethod(IS_STOPPED).invoke(Reflection.getMinecraftServer().orElse(null));
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            return false;
        }
    }
}