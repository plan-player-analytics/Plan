package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.data.time.WorldTimes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Keys for the ServerContainer.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.system.database.databases.sql.operation.SQLFetchOps For Suppliers for each key
 * @see com.djrapitops.plan.data.store.containers.ServerContainer For DataContainer.
 */
public class ServerKeys {

    public static final Key<UUID> SERVER_UUID = CommonKeys.SERVER_UUID;
    public static final Key<String> NAME = CommonKeys.NAME;
    public static final Key<List<PlayerContainer>> PLAYERS = new Key<>(new Type<List<PlayerContainer>>() {
    }, "players");
    public static final Key<List<PlayerContainer>> OPERATORS = new Key<>(new Type<List<PlayerContainer>>() {
    }, "operators");
    public static final Key<Integer> PLAYER_COUNT = new Key<>(Integer.class, "player_count");
    public static final Key<List<Session>> SESSIONS = CommonKeys.SESSIONS;
    public static final Key<List<Ping>> PING = CommonKeys.PING;
    public static final Key<WorldTimes> WORLD_TIMES = CommonKeys.WORLD_TIMES;
    public static final Key<List<PlayerKill>> PLAYER_KILLS = CommonKeys.PLAYER_KILLS;
    public static final Key<Integer> PLAYER_KILL_COUNT = CommonKeys.PLAYER_KILL_COUNT;
    public static final Key<Integer> MOB_KILL_COUNT = CommonKeys.MOB_KILL_COUNT;
    public static final Key<Integer> DEATH_COUNT = CommonKeys.DEATH_COUNT;
    public static final Key<List<TPS>> TPS = new Key<>(new Type<List<TPS>>() {
    }, "tps");
    public static final Key<DateObj<Integer>> ALL_TIME_PEAK_PLAYERS = new Key<>(new Type<DateObj<Integer>>() {
    }, "all_time_peak_players");
    public static final Key<DateObj<Integer>> RECENT_PEAK_PLAYERS = new Key<>(new Type<DateObj<Integer>>() {
    }, "recent_peak_players");
    public static final Key<Map<String, Integer>> COMMAND_USAGE = new Key<>(new Type<Map<String, Integer>>() {
    }, "command_usage");
    private ServerKeys() {
        /* Static variable class */
    }
}