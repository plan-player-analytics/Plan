package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.time.WorldTimes;

import java.util.List;

/**
 * Key objects for PerServerData container.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.containers.PerServerData For the DataContainer.
 */
public class PerServerKeys {

    public static final Key<Long> REGISTERED = CommonKeys.REGISTERED;

    public static final Key<List<Session>> SESSIONS = CommonKeys.SESSIONS;
    public static final Key<WorldTimes> WORLD_TIMES = CommonKeys.WORLD_TIMES;

    public static final Key<List<PlayerKill>> PLAYER_KILLS = CommonKeys.PLAYER_KILLS;
    public static final Key<Integer> PLAYER_KILL_COUNT = CommonKeys.PLAYER_KILL_COUNT;
    public static final Key<Integer> MOB_KILL_COUNT = CommonKeys.MOB_KILL_COUNT;
    public static final Key<Integer> DEATH_COUNT = CommonKeys.DEATH_COUNT;
    public static final Key<Long> LAST_SEEN = CommonKeys.LAST_SEEN;

    public static final Key<Boolean> BANNED = CommonKeys.BANNED;
    public static final Key<Boolean> OPERATOR = CommonKeys.OPERATOR;

}