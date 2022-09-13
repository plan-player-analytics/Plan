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
package com.djrapitops.plan.gathering.listeners.bungee;

import com.djrapitops.plan.gathering.domain.BungeePlayerData;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;
import com.djrapitops.plan.gathering.events.PlayerJoinEventConsumer;
import com.djrapitops.plan.gathering.events.PlayerLeaveEventConsumer;
import com.djrapitops.plan.gathering.events.PlayerSwitchServerEventConsumer;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import javax.inject.Inject;

/**
 * Player Join listener for Bungee.
 *
 * @author AuroraLS3
 */
public class PlayerOnlineListener implements Listener {

    private final PlayerJoinEventConsumer joinEventConsumer;
    private final PlayerLeaveEventConsumer leaveEventConsumer;
    private final PlayerSwitchServerEventConsumer switchServerEventConsumer;

    private final ServerInfo serverInfo;
    private final ErrorLogger errorLogger;

    @Inject
    public PlayerOnlineListener(
            PlayerJoinEventConsumer joinEventConsumer,
            PlayerLeaveEventConsumer leaveEventConsumer,
            PlayerSwitchServerEventConsumer switchServerEventConsumer,
            ServerInfo serverInfo,
            ErrorLogger errorLogger
    ) {
        this.joinEventConsumer = joinEventConsumer;
        this.leaveEventConsumer = leaveEventConsumer;
        this.switchServerEventConsumer = switchServerEventConsumer;
        this.serverInfo = serverInfo;
        this.errorLogger = errorLogger;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLogin(PostLoginEvent event) {
        try {
            actOnLogin(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnLogin(PostLoginEvent event) {
        long time = System.currentTimeMillis();
        ProxiedPlayer player = event.getPlayer();

        joinEventConsumer.onJoinProxyServer(PlayerJoin.builder()
                .server(serverInfo.getServer())
                .player(new BungeePlayerData(player))
                .time(time)
                .build());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void beforeLogout(PlayerDisconnectEvent event) {
        try {
            leaveEventConsumer.beforeLeave(PlayerLeave.builder()
                    .server(serverInfo.getServer())
                    .player(new BungeePlayerData(event.getPlayer()))
                    .time(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogout(PlayerDisconnectEvent event) {
        try {
            leaveEventConsumer.onLeaveProxyServer(PlayerLeave.builder()
                    .server(serverInfo.getServer())
                    .player(new BungeePlayerData(event.getPlayer()))
                    .time(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerSwitchEvent event) {
        try {
            switchServerEventConsumer.onServerSwitch(new BungeePlayerData(event.getPlayer()), System.currentTimeMillis());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }
}
