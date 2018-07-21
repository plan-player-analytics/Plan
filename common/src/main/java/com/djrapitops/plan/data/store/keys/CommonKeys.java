package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.container.PlayerDeath;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.PlaceholderKey;
import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.time.WorldTimes;

import java.util.*;

/**
 * Class holding Key objects that are commonly used across multiple DataContainers.
 *
 * @author Rsl1122
 */
public class CommonKeys {

    public static final Key<UUID> UUID = new Key<>(UUID.class, "uuid");
    public static final Key<UUID> SERVER_UUID = new Key<>(UUID.class, "server_uuid");
    public static final Key<String> NAME = new Key<>(String.class, "name");
    public static final PlaceholderKey<Long> REGISTERED = new PlaceholderKey<>(Long.class, "registered");
    public static final Key<List<Ping>> PING = new Key<>(new Type<List<Ping>>() {
    }, "ping");
    public static final Key<List<Session>> SESSIONS = new Key<>(new Type<List<Session>>() {
    }, "sessions");
    public static final Key<WorldTimes> WORLD_TIMES = new Key<>(WorldTimes.class, "world_times");
    public static final PlaceholderKey<Long> LAST_SEEN = new PlaceholderKey<>(Long.class, "lastSeen");
    public static final Key<List<PlayerDeath>> PLAYER_DEATHS = new Key<>(new Type<List<PlayerDeath>>() {
    }, "player_deaths");
    public static final Key<List<PlayerKill>> PLAYER_KILLS = new Key<>(new Type<List<PlayerKill>>() {
    }, "player_kills");
    public static final Key<Integer> PLAYER_KILL_COUNT = new Key<>(Integer.class, "player_kill_count");
    public static final Key<Integer> MOB_KILL_COUNT = new Key<>(Integer.class, "mob_kill_count");
    public static final Key<Integer> DEATH_COUNT = new Key<>(Integer.class, "death_count");
    public static final Key<Boolean> BANNED = new Key<>(Boolean.class, "banned");
    public static final Key<Boolean> OPERATOR = new Key<>(Boolean.class, "operator");
    public static final Key<SessionsMutator> SESSIONS_MUTATOR = new Key<>(SessionsMutator.class, "SESSIONS_MUTATOR");
    public static final Key<TPSMutator> TPS_MUTATOR = new Key<>(TPSMutator.class, "TPS_MUTATOR");
    public static final Key<PlayersMutator> PLAYERS_MUTATOR = new Key<>(PlayersMutator.class, "PLAYERS_MUTATOR");
    public static final Key<TreeMap<Long, Map<String, Set<UUID>>>> ACTIVITY_DATA = new Key<>(new Type<TreeMap<Long, Map<String, Set<UUID>>>>() {
    }, "ACTIVITY_DATA");

    private CommonKeys() {
        /* Static variable class */
    }

}