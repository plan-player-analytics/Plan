package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.data.store.containers.PerServerData;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.WorldTimes;

import java.util.List;
import java.util.UUID;

/**
 * Class that holds Key objects for PlayerContainer.
 *
 * @author Rsl1122
 */
public class PlayerKeys {

    public static final Key<UUID> UUID = CommonKeys.UUID;
    public static final Key<String> NAME = new Key<>(String.class, "name");
    public static final Key<List<Nickname>> NICKNAMES = new Key<>(new Type<List<Nickname>>() {}, "nicknames");

    public static final Key<Long> REGISTERED = new Key<>(Long.class, "registered");

    public static final Key<Integer> KICK_COUNT = new Key<>(Integer.class, "kick_count");
    public static final Key<List<GeoInfo>> GEO_INFO = new Key<>(new Type<List<GeoInfo>>() {}, "geo_info");

    public static final Key<Session> ACTIVE_SESSION = new Key<Session>(Session.class, "active_session");
    public static final Key<List<Session>> SESSIONS = new Key<>(new Type<List<Session>>() {}, "sessions");
    public static final Key<WorldTimes> WORLD_TIMES = new Key<>(WorldTimes.class, "world_times");

    public static final Key<List<PlayerKill>> PLAYER_KILLS = new Key<>(new Type<List<PlayerKill>>() {}, "player_kills");
    public static final Key<Integer> PLAYER_KILL_COUNT = new Key<>(Integer.class, "player_kill_count");
    public static final Key<Integer> MOB_KILL_COUNT = new Key<>(Integer.class, "mob_kill_count");
    public static final Key<Integer> DEATH_COUNT = new Key<>(Integer.class, "death_count");
    public static final Key<PerServerData> PER_SERVER = new Key<>(PerServerData.class, "per_server_data");
    public static final Key<Long> LAST_SEEN = new Key<>(Long.class, "last_seen");

    public static final Key<Boolean> BANNED = new Key<>(Boolean.class, "banned");
    public static final Key<Boolean> OPERATOR = new Key<>(Boolean.class, "operator");

}