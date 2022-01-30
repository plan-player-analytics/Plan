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

import com.djrapitops.plan.delivery.domain.container.PerServerContainer;
import com.djrapitops.plan.gathering.domain.*;

import java.util.List;

/**
 * Key objects for PerServerContainer container.
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.storage.database.queries.containers.PerServerContainerQuery For Suppliers for each key
 * @see PerServerContainer For the DataContainer.
 */
public class PerServerKeys {

    private PerServerKeys() {
        /* Static variable class */
    }

    public static final Key<Long> REGISTERED = CommonKeys.REGISTERED;
    public static final Key<List<Ping>> PING = CommonKeys.PING;

    public static final Key<List<FinishedSession>> SESSIONS = CommonKeys.SESSIONS;
    public static final Key<WorldTimes> WORLD_TIMES = CommonKeys.WORLD_TIMES;

    @Deprecated
    public static final Key<List<PlayerKill>> PLAYER_KILLS = CommonKeys.PLAYER_KILLS;
    @Deprecated
    public static final Key<List<PlayerDeath>> PLAYER_DEATHS = CommonKeys.PLAYER_DEATHS;
    public static final Key<Integer> PLAYER_KILL_COUNT = CommonKeys.PLAYER_KILL_COUNT;
    @Deprecated
    public static final Key<Integer> PLAYER_DEATH_COUNT = CommonKeys.PLAYER_DEATH_COUNT;
    public static final Key<Integer> MOB_KILL_COUNT = CommonKeys.MOB_KILL_COUNT;
    @Deprecated
    public static final Key<Integer> MOB_DEATH_COUNT = CommonKeys.MOB_DEATH_COUNT;
    public static final Key<Integer> DEATH_COUNT = CommonKeys.DEATH_COUNT;
    public static final Key<Long> LAST_SEEN = CommonKeys.LAST_SEEN;

    public static final Key<Boolean> BANNED = CommonKeys.BANNED;
    public static final Key<Boolean> OPERATOR = CommonKeys.OPERATOR;
    public static final Key<String> JOIN_ADDRESS = CommonKeys.JOIN_ADDRESS;

}