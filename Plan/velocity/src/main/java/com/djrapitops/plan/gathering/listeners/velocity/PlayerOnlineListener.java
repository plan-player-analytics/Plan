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
package com.djrapitops.plan.gathering.listeners.velocity;

import com.djrapitops.plan.gathering.domain.VelocityPlayerData;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;
import com.djrapitops.plan.gathering.events.PlayerJoinEventConsumer;
import com.djrapitops.plan.gathering.events.PlayerLeaveEventConsumer;
import com.djrapitops.plan.gathering.events.PlayerSwitchServerEventConsumer;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Player Join listener for Velocity.
 * <p>
 * Based on the bungee version.
 *
 * @author MicleBrick
 */
@Singleton
public class PlayerOnlineListener {

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

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        try {
            actOnLogin(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    public void actOnLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        long time = System.currentTimeMillis();

        joinEventConsumer.onJoinProxyServer(PlayerJoin.builder()
                .server(serverInfo.getServer())
                .player(new VelocityPlayerData(player))
                .time(time)
                .build());
    }

    @Subscribe(priority = Short.MAX_VALUE)
    public void beforeLogout(DisconnectEvent event) {
        leaveEventConsumer.beforeLeave(PlayerLeave.builder()
                .server(serverInfo.getServer())
                .player(new VelocityPlayerData(event.getPlayer()))
                .time(System.currentTimeMillis())
                .build());
    }

    @Subscribe
    public void onLogout(DisconnectEvent event) {
        try {
            leaveEventConsumer.onLeaveProxyServer(PlayerLeave.builder()
                    .server(serverInfo.getServer())
                    .player(new VelocityPlayerData(event.getPlayer()))
                    .time(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        try {
            actOnServerSwitch(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    public void actOnServerSwitch(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        long time = System.currentTimeMillis();

        switchServerEventConsumer.onServerSwitch(new VelocityPlayerData(player), time);
    }
}
