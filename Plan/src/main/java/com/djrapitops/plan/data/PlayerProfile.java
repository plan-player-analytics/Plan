/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.data;

import com.djrapitops.plan.data.calculation.ActivityIndex;
import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.comparators.ActionComparator;
import com.djrapitops.plan.utilities.comparators.GeoInfoComparator;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data container object for a single player.
 * <p>
 * Created to streamline analysis and to make it easier to understand.
 *
 * @author Rsl1122
 */
public class PlayerProfile {

    // Identification
    private final UUID uuid;
    private final String name;

    // Basic information
    private final long registered;
    private Map<UUID, Long> registeredMap;
    private Set<UUID> bannedOnServers;
    private Set<UUID> oppedOnServers;
    private int timesKicked;

    // Activity related information
    private Map<UUID, List<Session>> sessions;
    private List<Action> actions;
    private Map<UUID, WorldTimes> worldTimesMap;

    // Extra information
    private Map<UUID, List<String>> nicknames;
    private List<GeoInfo> geoInformation;

    // Plugin data
    private Map<String, String> pluginReplaceMap;

    // Value that requires lot of processing
    private Map<Long, ActivityIndex> activityIndexCache;

    public PlayerProfile(UUID uuid, String name, long registered) {
        this.uuid = uuid;
        this.name = name;
        this.registered = registered;
        registeredMap = new HashMap<>();

        bannedOnServers = new HashSet<>();
        oppedOnServers = new HashSet<>();

        sessions = new HashMap<>();
        actions = new ArrayList<>();
        worldTimesMap = new HashMap<>();

        nicknames = new HashMap<>();
        geoInformation = new ArrayList<>();

        pluginReplaceMap = new HashMap<>();
        activityIndexCache = new HashMap<>();
    }

    public static long getPlaytime(Stream<Session> s) {
        return s.mapToLong(Session::getLength).sum();
    }

    public static long getLongestSession(Stream<Session> s) {
        OptionalLong longestSession = s.mapToLong(Session::getLength).max();
        if (longestSession.isPresent()) {
            return longestSession.getAsLong();
        }
        return -1;
    }

    public static long getSessionMedian(Stream<Session> s) {
        List<Long> sessionLenghts = s.map(Session::getLength)
                .sorted()
                .collect(Collectors.toList());
        if (sessionLenghts.isEmpty()) {
            return 0;
        }
        return sessionLenghts.get(sessionLenghts.size() / 2);
    }

    public static long getSessionAverage(Stream<Session> s) {
        OptionalDouble average = s.map(Session::getLength)
                .mapToLong(i -> i)
                .average();
        if (average.isPresent()) {
            return (long) average.getAsDouble();
        }
        return 0L;
    }

    public static Stream<PlayerKill> getPlayerKills(Stream<Session> s) {
        return s.map(Session::getPlayerKills)
                .flatMap(Collection::stream);
    }

    public static long getDeathCount(Stream<Session> s) {
        return s.mapToLong(Session::getDeaths)
                .sum();
    }

    public static long getMobKillCount(Stream<Session> s) {
        return s.mapToLong(Session::getMobKills)
                .sum();
    }

    // Calculating Getters
    public ActivityIndex getActivityIndex(long date) {
        return activityIndexCache.computeIfAbsent(date, dateValue -> new ActivityIndex(this, dateValue));
    }

    /**
     * Get the total world times of the player.
     *
     * @return returns the WorldTimes in the "null" key of the map.
     */
    public WorldTimes getWorldTimes() {
        return worldTimesMap.getOrDefault(null, new WorldTimes(new HashMap<>()));
    }

    public void setWorldTimes(Map<UUID, WorldTimes> worldTimes) {
        worldTimesMap.putAll(worldTimes);
    }

    /**
     * Get world times per server for this player.
     *
     * @return a copy of the WorldTimes Map without the "null" key.
     */
    public Map<UUID, WorldTimes> getWorldTimesPerServer() {
        Map<UUID, WorldTimes> map = new HashMap<>(worldTimesMap);
        map.remove(null);
        return map;
    }

    public UUID getFavoriteServer() {
        long max = 0L;
        UUID maxServer = null;
        for (Map.Entry<UUID, WorldTimes> entry : getWorldTimesPerServer().entrySet()) {
            long playTime = entry.getValue().getTotal();
            if (playTime > max) {
                max = playTime;
                maxServer = entry.getKey();
            }
        }
        return maxServer;
    }

    public long getLastSeen() {
        return getLastSeen(getAllSessions());
    }

    public long getLastSeen(Stream<Session> s) {
        OptionalLong max = s.mapToLong(session -> Math.max(session.getSessionStart(), session.getSessionEnd())).max();
        if (max.isPresent()) {
            return max.getAsLong();
        }
        return 0;
    }

    public long getTotalPlaytime() {
        return getPlaytime(-1, System.currentTimeMillis() + 1L);
    }

    public long getPlaytime(long after, long before) {
        return getPlaytime(getSessions(after, before));
    }

    public long getPlaytime(UUID serverUUID) {
        return getPlaytime(getSessions(serverUUID).stream());
    }

    public long getLongestSession() {
        return getLongestSession(-1, System.currentTimeMillis() + 1L);
    }

    public long getLongestSession(int after, long before) {
        return getLongestSession(getSessions(after, before));
    }

    public long getLongestSession(UUID serverUUID) {
        return getLongestSession(getSessions(serverUUID).stream());
    }

    public long getSessionMedian() {
        return getSessionMedian(-1, System.currentTimeMillis() + 1L);
    }

    public long getSessionMedian(int after, long before) {
        return getSessionMedian(getSessions(after, before));
    }

    public long getSessionMedian(UUID serverUUID) {
        return getSessionMedian(getSessions(serverUUID).stream());
    }

    // Special Getters

    public long getSessionAverage() {
        return getSessionAverage(-1, System.currentTimeMillis() + 1L);
    }

    public long getSessionAverage(int after, long before) {
        return getSessionAverage(getSessions(after, before));
    }

    public long getSessionAverage(UUID serverUUID) {
        return getSessionAverage(getSessions(serverUUID).stream());
    }

    public boolean playedBetween(long after, long before) {
        return getSessions(after, before).findFirst().isPresent();
    }

    public Stream<Session> getAllSessions() {
        return sessions.values().stream().flatMap(Collection::stream);
    }

    public Stream<Session> getSessions(long after, long before) {
        return getAllSessions()
                .filter(session -> session.getSessionStart() >= after && session.getSessionStart() <= before);
    }

    public GeoInfo getMostRecentGeoInfo() {
        if (geoInformation.isEmpty()) {
            return new GeoInfo("-", "Not Known", System.currentTimeMillis(), "");
        }
        geoInformation.sort(new GeoInfoComparator());
        return geoInformation.get(0);
    }

    public List<Action> getAllActions() {
        List<Action> actions = new ArrayList<>(this.actions);
        getPlayerKills().map(PlayerKill::convertToAction).forEach(actions::add);
        actions.sort(new ActionComparator());
        return actions;
    }

    public Stream<PlayerKill> getPlayerKills() {
        return getPlayerKills(getAllSessions());
    }

    public Stream<PlayerKill> getPlayerKills(UUID serverUUID) {
        return getPlayerKills(getSessions(serverUUID).stream());
    }

    public long getPlayerKillCount() {
        return getPlayerKills().count();
    }

    public long getPlayerKillCount(UUID serverUUID) {
        return getPlayerKills(serverUUID).count();
    }

    public long getDeathCount() {
        return getDeathCount(getAllSessions());
    }

    public long getDeathCount(UUID serverUUID) {
        return getDeathCount(getSessions(serverUUID).stream());
    }

    public long getMobKillCount() {
        return getMobKillCount(getAllSessions());
    }

    public long getMobKillCount(UUID serverUUID) {
        return getMobKillCount(getSessions(serverUUID).stream());
    }

    public long getSessionCount() {
        return getAllSessions().count();
    }

    public long getSessionCount(UUID serverUUID) {
        return getSessions(serverUUID).size();
    }

    // Setters & Adders

    public long getRegistered(UUID serverUUID) {
        return registeredMap.getOrDefault(serverUUID, -1L);
    }

    public void bannedOnServer(UUID serverUUID) {
        bannedOnServers.add(serverUUID);
    }

    public void oppedOnServer(UUID serverUUID) {
        oppedOnServers.add(serverUUID);
    }

    public void bannedOnServer(Collection<UUID> serverUUIDs) {
        bannedOnServers.addAll(serverUUIDs);
    }

    public void oppedOnServer(Collection<UUID> serverUUIDs) {
        oppedOnServers.addAll(serverUUIDs);
    }

    public void setSessions(UUID serverUUID, List<Session> sessions) {
        this.sessions.put(serverUUID, sessions);
    }

    public void addActiveSession(Session activeSession) {
        UUID serverUUID = ServerInfo.getServerUUID();
        List<Session> sessions = getSessions(serverUUID);
        sessions.add(activeSession);
        this.sessions.put(serverUUID, sessions);
    }

    public List<Session> getSessions(UUID serverUUID) {
        return this.sessions.getOrDefault(serverUUID, new ArrayList<>());
    }

    public void addReplaceValue(String placeholder, Serializable value) {
        pluginReplaceMap.put(placeholder, value.toString());
    }

    public void setWorldTimes(UUID serverUUID, WorldTimes worldTimes) {
        worldTimesMap.put(serverUUID, worldTimes);
    }

    public void setTotalWorldTimes(WorldTimes worldTimes) {
        worldTimesMap.put(null, worldTimes);
    }

    public void setRegistered(UUID serverUUID, long registered) {
        registeredMap.put(serverUUID, registered);
    }

    public int getTimesKicked() {
        return timesKicked;
    }

    // Default Setters

    public void setTimesKicked(int timesKicked) {
        this.timesKicked = timesKicked;
    }

    public Map<UUID, List<String>> getNicknames() {
        return nicknames;
    }

    public void setNicknames(Map<UUID, List<String>> nicknames) {
        this.nicknames = nicknames;
    }

    public List<GeoInfo> getGeoInformation() {
        return geoInformation;
    }

    // Default Getters

    public void setGeoInformation(List<GeoInfo> geoInformation) {
        this.geoInformation = geoInformation;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getRegistered() {
        return registered;
    }

    public Set<UUID> getBannedOnServers() {
        return bannedOnServers;
    }

    public Set<UUID> getOppedOnServers() {
        return oppedOnServers;
    }

    public Map<UUID, List<Session>> getSessions() {
        return sessions;
    }

    public void setSessions(Map<UUID, List<Session>> sessions) {
        this.sessions.putAll(sessions);
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public Map<String, String> getPluginReplaceMap() {
        return pluginReplaceMap;
    }

    /**
     * Get the WorldTimes map.
     *
     * @return Map that contains WorldTimes for each server and a total in the "null" key.
     */
    public Map<UUID, WorldTimes> getWorldTimesMap() {
        return worldTimesMap;
    }

    // OfflinePlayer methods for possible PluginData analysis

    public boolean isBanned() {
        return bannedOnServers.size() != 0;
    }

    public boolean isOp() {
        return oppedOnServers.contains(ServerInfo.getServerUUID());
    }

    public static long getAFKTime(Stream<Session> sessions) {
        return sessions.mapToLong(Session::getAfkLength).sum();
    }

    public static long getActivePlaytime(Stream<Session> sessions) {
        return sessions.mapToLong(Session::getActiveLength).sum();
    }

    public void calculateWorldTimesPerServer() {
        if (worldTimesMap.containsKey(ServerInfo.getServerUUID())) {
            return;
        }

        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            UUID serverUUID = entry.getKey();
            List<Session> sessions = entry.getValue();

            WorldTimes times = worldTimesMap.getOrDefault(serverUUID, new WorldTimes(new HashMap<>()));
            for (Session session : sessions) {
                WorldTimes worldTimes = session.getWorldTimes();
                times.add(worldTimes);
            }
            worldTimesMap.put(serverUUID, times);
        }

    }
}
