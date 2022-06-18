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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.container.PerServerContainer;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.gathering.domain.*;

import java.util.List;
import java.util.UUID;

/**
 * Class that holds Key objects for PlayerContainer.
 *
 * @author AuroraLS3
 * @see PlayerContainer For DataContainer.
 */
public class PlayerKeys {

    private PlayerKeys() {
        /* Static variable class */
    }

    public static final Key<UUID> UUID = CommonKeys.UUID;
    public static final Key<String> NAME = CommonKeys.NAME;
    public static final Key<List<Nickname>> NICKNAMES = new Key<>(new Type<>() {}, "nicknames");

    public static final PlaceholderKey<Long> REGISTERED = CommonKeys.REGISTERED;

    public static final Key<Integer> KICK_COUNT = new Key<>(Integer.class, "kick_count");
    public static final Key<List<GeoInfo>> GEO_INFO = new Key<>(new Type<>() {}, "geo_info");
    public static final Key<List<Ping>> PING = CommonKeys.PING;

    public static final Key<ActiveSession> ACTIVE_SESSION = new Key<>(ActiveSession.class, "active_session");
    public static final Key<List<FinishedSession>> SESSIONS = CommonKeys.SESSIONS;
    public static final Key<WorldTimes> WORLD_TIMES = CommonKeys.WORLD_TIMES;

    public static final Key<List<PlayerKill>> PLAYER_KILLS = CommonKeys.PLAYER_KILLS;
    public static final Key<List<PlayerKill>> PLAYER_DEATHS_KILLS = new Key<>(new Type<>() {
    }, "player_deaths_kills");
    @Deprecated(forRemoval = true, since = "5.0")
    public static final Key<List<PlayerDeath>> PLAYER_DEATHS = CommonKeys.PLAYER_DEATHS;
    public static final Key<Integer> PLAYER_KILL_COUNT = CommonKeys.PLAYER_KILL_COUNT;
    public static final Key<Integer> MOB_KILL_COUNT = CommonKeys.MOB_KILL_COUNT;
    public static final Key<Integer> DEATH_COUNT = CommonKeys.DEATH_COUNT;
    public static final Key<PerServerContainer> PER_SERVER = new Key<>(PerServerContainer.class, "per_server_data");
    public static final PlaceholderKey<Long> LAST_SEEN = CommonKeys.LAST_SEEN;

    public static final Key<Boolean> BANNED = CommonKeys.BANNED;
    public static final Key<Boolean> OPERATOR = CommonKeys.OPERATOR;

}