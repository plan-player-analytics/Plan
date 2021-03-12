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
package com.djrapitops.plan.delivery.domain.keys;

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.container.ServerContainer;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.gathering.domain.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Keys for the ServerContainer.
 *
 * @author AuroraLS3
 * @see ServerContainer For DataContainer.
 */
public class ServerKeys {

    private ServerKeys() {
        /* Static variable class */
    }

    public static final Key<UUID> SERVER_UUID = CommonKeys.SERVER_UUID;
    public static final Key<String> NAME = CommonKeys.NAME;

    public static final Key<List<PlayerContainer>> PLAYERS = new Key<>(new Type<List<PlayerContainer>>() {}, "players");
    public static final Key<List<PlayerContainer>> OPERATORS = new Key<>(new Type<List<PlayerContainer>>() {}, "operators");
    public static final Key<Integer> PLAYER_COUNT = new Key<>(Integer.class, "player_count");

    public static final Key<List<FinishedSession>> SESSIONS = CommonKeys.SESSIONS;
    public static final Key<List<Ping>> PING = CommonKeys.PING;
    public static final Key<WorldTimes> WORLD_TIMES = CommonKeys.WORLD_TIMES;

    public static final Key<List<PlayerKill>> PLAYER_KILLS = CommonKeys.PLAYER_KILLS;
    public static final Key<Integer> PLAYER_KILL_COUNT = CommonKeys.PLAYER_KILL_COUNT;
    public static final Key<Integer> MOB_KILL_COUNT = CommonKeys.MOB_KILL_COUNT;
    public static final Key<Integer> DEATH_COUNT = CommonKeys.DEATH_COUNT;

    public static final Key<List<TPS>> TPS = new Key<>(new Type<List<TPS>>() {}, "tps");
    public static final Key<DateObj<Integer>> ALL_TIME_PEAK_PLAYERS = new Key<>(new Type<DateObj<Integer>>() {}, "all_time_peak_players");
    public static final Key<DateObj<Integer>> RECENT_PEAK_PLAYERS = new Key<>(new Type<DateObj<Integer>>() {}, "recent_peak_players");
    @Deprecated
    public static final Key<Map<String, Integer>> COMMAND_USAGE = new Key<>(new Type<Map<String, Integer>>() {}, "command_usage");
    public static final Key<List<ExtensionData>> EXTENSION_DATA = new Key<>(new Type<List<ExtensionData>>() {}, "extension_data");
}