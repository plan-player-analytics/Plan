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
package net.playeranalytics.plan;

import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.gathering.afk.AFKTracker;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.playeranalytics.plan.gathering.listeners.fabric.FabricAFKListener;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * ServerShutdownSave implementation for Fabric-based servers.
 *
 * @author Kopo942
 */
@Singleton
public class FabricServerShutdownSave extends ServerShutdownSave {

    private final MinecraftDedicatedServer server;

    @Inject
    public FabricServerShutdownSave(
            MinecraftDedicatedServer server,
            Locale locale,
            DBSystem dbSystem,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        super(locale, dbSystem, logger, errorLogger);
        this.server = server;
    }

    @Override
    protected boolean checkServerShuttingDownStatus() {
        return !server.isRunning();
    }

    @Override
    public Optional<AFKTracker> getAfkTracker() {
        return Optional.ofNullable(FabricAFKListener.getAfkTracker());
    }
}
