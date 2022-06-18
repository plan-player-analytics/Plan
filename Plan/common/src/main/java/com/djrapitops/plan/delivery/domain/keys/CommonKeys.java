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

import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.WorldTimes;

import java.util.List;
import java.util.UUID;

/**
 * Class holding Key objects that are commonly used across multiple DataContainers.
 *
 * @author AuroraLS3
 */
public class CommonKeys {

    private CommonKeys() {
        /* Static variable class */
    }

    public static final Key<UUID> UUID = new Key<>(UUID.class, "uuid");
    public static final Key<UUID> SERVER_UUID = new Key<>(UUID.class, "server_uuid");
    public static final Key<String> NAME = new Key<>(String.class, "name");
    public static final PlaceholderKey<Long> REGISTERED = new PlaceholderKey<>(Long.class, "registered");
    public static final Key<List<Ping>> PING = new Key<>(new Type<>() {}, "ping");

    public static final Key<List<FinishedSession>> SESSIONS = new Key<>(new Type<>() {}, "sessions");
    public static final Key<WorldTimes> WORLD_TIMES = new Key<>(WorldTimes.class, "world_times");
    public static final PlaceholderKey<Long> LAST_SEEN = new PlaceholderKey<>(Long.class, "lastSeen");

    public static final Key<List<PlayerKill>> PLAYER_KILLS = new Key<>(new Type<>() {}, "player_kills");
    public static final Key<Integer> PLAYER_KILL_COUNT = new Key<>(Integer.class, "player_kill_count");
    public static final Key<Integer> MOB_KILL_COUNT = new Key<>(Integer.class, "mob_kill_count");
    public static final Key<Integer> DEATH_COUNT = new Key<>(Integer.class, "death_count");

    public static final Key<Boolean> BANNED = new Key<>(Boolean.class, "banned");
    public static final Key<Boolean> OPERATOR = new Key<>(Boolean.class, "operator");
    public static final Key<String> JOIN_ADDRESS = new Key<>(String.class, "join_address");

}