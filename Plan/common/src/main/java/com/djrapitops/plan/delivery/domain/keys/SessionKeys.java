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

import com.djrapitops.plan.gathering.domain.PlayerDeath;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.gathering.domain.WorldTimes;

import java.util.List;
import java.util.UUID;

/**
 * Class holding Key objects for Session (DataContainer).
 *
 * @author AuroraLS3
 * @see Session for DataContainer.
 */
public class SessionKeys {

    public static final Key<Integer> DB_ID = new Key<>(Integer.class, "db_id");
    public static final Key<UUID> UUID = CommonKeys.UUID;
    public static final Key<UUID> SERVER_UUID = CommonKeys.SERVER_UUID;
    public static final Key<String> NAME = CommonKeys.NAME;
    public static final Key<String> SERVER_NAME = new Key<>(String.class, "server_name");

    public static final Key<Long> START = new Key<>(Long.class, "start");
    public static final Key<Long> END = new Key<>(Long.class, "end");
    public static final Key<Long> LENGTH = new Key<>(Long.class, "length");
    public static final Key<Long> AFK_TIME = new Key<>(Long.class, "afk_time");
    public static final Key<Long> ACTIVE_TIME = new Key<>(Long.class, "active_time");
    public static final Key<WorldTimes> WORLD_TIMES = CommonKeys.WORLD_TIMES;
    public static final Key<List<PlayerKill>> PLAYER_KILLS = CommonKeys.PLAYER_KILLS;
    public static final Key<Integer> PLAYER_KILL_COUNT = CommonKeys.PLAYER_KILL_COUNT;
    public static final Key<Integer> MOB_KILL_COUNT = CommonKeys.MOB_KILL_COUNT;
    public static final Key<Integer> DEATH_COUNT = CommonKeys.DEATH_COUNT;
    public static final Key<Boolean> FIRST_SESSION = new Key<>(Boolean.class, "first_session");
    @Deprecated
    public static final Key<List<PlayerDeath>> PLAYER_DEATHS = CommonKeys.PLAYER_DEATHS;

    /**
     * @deprecated use WorldAliasSettings#getLongestWorldPlayed(Session) instead.
     */
    @Deprecated
    public static final Key<String> LONGEST_WORLD_PLAYED = new Key<>(String.class, "longest_world_played");
    public static final Key<Double> AVERAGE_PING = new Key<>(Double.class, "averagePing");

    private SessionKeys() {
        /* Static variable class */
    }

}