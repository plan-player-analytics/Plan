package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.data.store.objects.DateMap;
import com.djrapitops.plan.data.store.objects.Nickname;

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
    public static final Key<DateMap<GeoInfo>> GEO_INFO = new Key<>(new Type<DateMap<GeoInfo>>() {}, "geo_info");
}