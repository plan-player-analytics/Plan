package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.container.PlayerDeath;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.time.WorldTimes;

import java.util.List;
import java.util.UUID;

/**
 * Class holding Key objects for Session (DataContainer).
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.container.Session for DataContainer.
 */
public class SessionKeys {

    public static final Key<Integer> DB_ID = new Key<>(Integer.class, "db_id");
    public static final Key<UUID> UUID = CommonKeys.UUID;
    public static final Key<UUID> SERVER_UUID = CommonKeys.SERVER_UUID;

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
    public static final Key<List<PlayerDeath>> PLAYER_DEATHS = CommonKeys.PLAYER_DEATHS;

    private SessionKeys() {
        /* Static variable class */
    }

}