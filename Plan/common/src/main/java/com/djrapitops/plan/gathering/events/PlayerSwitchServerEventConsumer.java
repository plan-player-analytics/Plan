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
package com.djrapitops.plan.gathering.events;

import com.djrapitops.plan.gathering.domain.PlatformPlayerData;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.identification.ServerInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlayerSwitchServerEventConsumer {

    private final PlayerJoinEventConsumer joinEventConsumer;
    private final ServerInfo serverInfo;

    @Inject
    public PlayerSwitchServerEventConsumer(PlayerJoinEventConsumer joinEventConsumer, ServerInfo serverInfo) {
        this.joinEventConsumer = joinEventConsumer;
        this.serverInfo = serverInfo;
    }

    public void onServerSwitch(PlatformPlayerData player, long time) {
        PlayerJoin asJoin = PlayerJoin.builder()
                .player(player)
                .server(serverInfo.getServer())
                .time(time)
                .build();
        joinEventConsumer.cacheActiveSession(asJoin);
        joinEventConsumer.updateExport(asJoin);
    }

}
