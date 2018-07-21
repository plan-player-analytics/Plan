/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.importing;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.system.info.server.ServerInfo;

import java.util.*;

/**
 * @author Fuzzlemann
 * @since 4.0.0
 */
public class UserImportData {

    private String name;
    private UUID uuid;
    private List<Nickname> nicknames;

    private long registered;
    private boolean op;

    private boolean banned;
    private int timesKicked;

    private List<String> ips;
    private Map<String, GMTimes> worldTimes;

    private List<PlayerKill> kills;
    private int mobKills;
    private int deaths;

    private UserImportData(String name, UUID uuid, List<Nickname> nicknames, long registered, boolean op, boolean banned, int timesKicked, List<String> ips, Map<String, GMTimes> worldTimes, List<PlayerKill> kills, int mobKills, int deaths) {
        this.name = name;
        this.uuid = uuid;
        this.nicknames = nicknames;
        this.registered = registered;
        this.op = op;
        this.banned = banned;
        this.timesKicked = timesKicked;
        this.ips = ips;
        this.worldTimes = worldTimes;
        this.kills = kills;
        this.mobKills = mobKills;
        this.deaths = deaths;
    }

    public static UserImportDataBuilder builder() {
        return new UserImportDataBuilder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<Nickname> getNicknames() {
        return nicknames;
    }

    public void setNicknames(List<Nickname> nicknames) {
        this.nicknames = nicknames;
    }

    public long getRegistered() {
        return registered;
    }

    public void setRegistered(long registered) {
        this.registered = registered;
    }

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean op) {
        this.op = op;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public int getTimesKicked() {
        return timesKicked;
    }

    public void setTimesKicked(int timesKicked) {
        this.timesKicked = timesKicked;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public Map<String, GMTimes> getWorldTimes() {
        return worldTimes;
    }

    public void setWorldTimes(Map<String, GMTimes> worldTimes) {
        this.worldTimes = worldTimes;
    }

    public List<PlayerKill> getKills() {
        return kills;
    }

    public void setKills(List<PlayerKill> kills) {
        this.kills = kills;
    }

    public int getMobKills() {
        return mobKills;
    }

    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public static final class UserImportDataBuilder {
        private final List<Nickname> nicknames = new ArrayList<>();
        private final List<String> ips = new ArrayList<>();
        private final Map<String, GMTimes> worldTimes = new HashMap<>();
        private final List<PlayerKill> kills = new ArrayList<>();
        private String name;
        private UUID uuid;
        private long registered;
        private boolean op;
        private boolean banned;
        private int timesKicked;
        private int mobKills;
        private int deaths;

        private UserImportDataBuilder() {
            /* Private Constructor */
        }

        public UserImportDataBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserImportDataBuilder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public UserImportDataBuilder uuid(String uuid) {
            return uuid(UUID.fromString(uuid));
        }

        public UserImportDataBuilder registered(long registered) {
            this.registered = registered;
            return this;
        }

        public UserImportDataBuilder op() {
            return op(true);
        }

        public UserImportDataBuilder op(boolean op) {
            this.op = op;
            return this;
        }

        public UserImportDataBuilder nicknames(String... nicknames) {
            long time = System.currentTimeMillis();
            UUID serverUUID = ServerInfo.getServerUUID();

            Arrays.stream(nicknames)
                    .map(nick -> new Nickname(nick, time, serverUUID))
                    .forEach(this.nicknames::add);
            return this;
        }

        public UserImportDataBuilder nicknames(Collection<Nickname> nicknames) {
            this.nicknames.addAll(nicknames);
            return this;
        }

        public UserImportDataBuilder banned() {
            return banned(true);
        }

        public UserImportDataBuilder banned(boolean banned) {
            this.banned = banned;
            return this;
        }

        public UserImportDataBuilder timesKicked(int timesKicked) {
            this.timesKicked += timesKicked;
            return this;
        }

        public UserImportDataBuilder ips(String... ips) {
            this.ips.addAll(Arrays.asList(ips));
            return this;
        }

        public UserImportDataBuilder ips(Collection<String> ips) {
            this.ips.addAll(ips);
            return this;
        }

        public UserImportDataBuilder worldTimes(String worldName, long... times) {
            GMTimes gmTimes = new GMTimes();
            gmTimes.setAllGMTimes(times);

            this.worldTimes.put(worldName, gmTimes);
            return this;
        }

        public UserImportDataBuilder worldTimes(String worldName, GMTimes gmTimes) {
            this.worldTimes.put(worldName, gmTimes);
            return this;
        }

        public UserImportDataBuilder worldTimes(Map<String, GMTimes> worldTimes) {
            this.worldTimes.putAll(worldTimes);
            return this;
        }

        public UserImportDataBuilder kills(PlayerKill... kills) {
            this.kills.addAll(Arrays.asList(kills));
            return this;
        }

        public UserImportDataBuilder kills(Collection<PlayerKill> kills) {
            this.kills.addAll(kills);
            return this;
        }

        public UserImportDataBuilder mobKills(int mobKills) {
            this.mobKills += mobKills;
            return this;
        }

        public UserImportDataBuilder deaths(int deaths) {
            this.deaths += deaths;
            return this;
        }

        public UserImportData build() {
            return new UserImportData(name, uuid, nicknames, registered, op, banned, timesKicked, ips, worldTimes, kills, mobKills, deaths);
        }
    }
}
