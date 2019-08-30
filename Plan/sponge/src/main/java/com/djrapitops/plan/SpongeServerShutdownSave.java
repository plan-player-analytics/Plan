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
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.spongepowered.api.GameState;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ServerShutdownSave implementation for Sponge
 *
 * @author Rsl1122
 */
@Singleton
public class SpongeServerShutdownSave extends ServerShutdownSave {

    private boolean shuttingDown = false;

    @Inject
    public SpongeServerShutdownSave(
            Locale locale,
            DBSystem dbSystem,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(locale, dbSystem, logger, errorHandler);
    }

    @Override
    protected boolean checkServerShuttingDownStatus() {
        return shuttingDown;
    }

    @Listener(order = Order.PRE)
    public void onServerShutdown(GameStoppingServerEvent event) {
        GameState state = event.getState();
        shuttingDown = state == GameState.SERVER_STOPPING
                || state == GameState.GAME_STOPPING
                || state == GameState.SERVER_STOPPED
                || state == GameState.GAME_STOPPED;
    }
}