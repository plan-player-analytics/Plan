/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.comparators.ActionComparator;
import main.java.com.djrapitops.plan.utilities.comparators.GeoInfoComparator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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
public class PlayerProfile implements OfflinePlayer {

    // Identification
    private final UUID uuid;
    private final String name;

    // Basic information
    private final long registered;
    private Map<UUID, Long> registeredMap;
    private Set<UUID> bannedOnServers;
    private Set<UUID> oppedOnServers;

    // Activity related information
    private Map<UUID, List<Session>> sessions;
    private List<Action> actions;
    private Map<UUID, WorldTimes> worldTimesMap;

    // Extra information
    private List<String> nicknames;
    private List<GeoInfo> geoInformation;

    // Plugin data
    private Map<String, String> pluginReplaceMap;

    // Value that requires lot of processing
    private Double activityIndex;

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

        pluginReplaceMap = new HashMap<>();
    }

    // Calculating Getters

    public boolean isActive() {
        return getActivityIndex() > 1.0;
    }

    public double getActivityIndex() {
        if (activityIndex != null) {
            return activityIndex;
        }
        return 0.0; // TODO
    }

    /**
     * Get the total world times of the player.
     *
     * @return returns the WorldTimes in the "null" key of the map.
     */
    public WorldTimes getWorldTimes() {
        return worldTimesMap.getOrDefault(null, new WorldTimes(new HashMap<>()));
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

    public long getLastSeen() {
        return getLastSeen(getAllSessions());
    }

    public long getLastSeen(UUID serverUUID) {
        return getLastSeen(getSessions(serverUUID).stream());
    }

    public long getLastSeen(Stream<Session> s) {
        OptionalLong max = s.mapToLong(Session::getSessionEnd)
                .max();
        if (max.isPresent()) {
            return max.getAsLong();
        }
        return 0;
    }

    public long getTotalPlaytime() {
        return getPlaytime(-1, MiscUtils.getTime() + 1L);
    }

    public long getPlaytime(long after, long before) {
        return getPlaytime(getSessions(after, before));
    }

    public long getPlaytime(UUID serverUUID) {
        return getPlaytime(getSessions(serverUUID).stream());
    }

    public static long getPlaytime(Stream<Session> s) {
        return s.map(Session::getLength)
                .mapToLong(i -> i)
                .sum();
    }

    public long getLongestSession() {
        return getLongestSession(-1, MiscUtils.getTime() + 1L);
    }

    public long getLongestSession(int after, long before) {
        return getLongestSession(getSessions(after, before));
    }

    public long getLongestSession(UUID serverUUID) {
        return getLongestSession(getSessions(serverUUID).stream());
    }

    public static long getLongestSession(Stream<Session> s) {
        OptionalLong longestSession = s.map(Session::getLength)
                .mapToLong(i -> i)
                .max();
        if (longestSession.isPresent()) {
            return longestSession.getAsLong();
        }
        return 0L;
    }

    public long getSessionMedian() {
        return getSessionMedian(-1, MiscUtils.getTime() + 1L);
    }

    public long getSessionMedian(int after, long before) {
        return getSessionMedian(getSessions(after, before));
    }

    public long getSessionMedian(UUID serverUUID) {
        return getSessionMedian(getSessions(serverUUID).stream());
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

    public long getSessionAverage() {
        return getSessionAverage(-1, MiscUtils.getTime() + 1L);
    }

    public long getSessionAverage(int after, long before) {
        return getSessionAverage(getSessions(after, before));
    }

    public long getSessionAverage(UUID serverUUID) {
        return getSessionAverage(getSessions(serverUUID).stream());
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


    // Special Getters

    public Stream<Session> getAllSessions() {
        return sessions.values().stream().flatMap(Collection::stream);
    }

    public Stream<Session> getSessions(long after, long before) {
        return getAllSessions()
                .filter(session -> session.getSessionStart() > after && session.getSessionEnd() < before);
    }

    public GeoInfo getMostRecentGeoInfo() {
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

    public static Stream<PlayerKill> getPlayerKills(Stream<Session> s) {
        return s.map(Session::getPlayerKills)
                .flatMap(Collection::stream);
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

    public static long getDeathCount(Stream<Session> s) {
        return s.mapToLong(Session::getDeaths)
                .sum();
    }

    public long getMobKillCount() {
        return getMobKillCount(getAllSessions());
    }

    public long getMobKillCount(UUID serverUUID) {
        return getMobKillCount(getSessions(serverUUID).stream());
    }

    public static long getMobKillCount(Stream<Session> s) {
        return s.mapToLong(Session::getMobKills)
                .sum();
    }

    public long getSessionCount() {
        return getAllSessions().count();
    }

    public long getSessionCount(UUID serverUUID) {
        return getSessions(serverUUID).size();
    }

    public long getRegistered(UUID serverUUID) {
        return registeredMap.getOrDefault(serverUUID, -1L);
    }

    // Setters & Adders

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
        UUID serverUUID = MiscUtils.getIPlan().getServerUuid();
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

    public void setWorldTimes(Map<UUID, WorldTimes> worldTimes) {
        worldTimesMap.putAll(worldTimes);
    }

    public void setTotalWorldTimes(WorldTimes worldTimes) {
        worldTimesMap.put(null, worldTimes);
    }

    public void setRegistered(UUID serverUUID, long registered) {
        registeredMap.put(serverUUID, registered);
    }

    // Default Setters

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void setNicknames(List<String> nicknames) {
        this.nicknames = nicknames;
    }

    public void setGeoInformation(List<GeoInfo> geoInformation) {
        this.geoInformation = geoInformation;
    }

    // Default Getters

    public List<String> getNicknames() {
        return nicknames;
    }

    public List<GeoInfo> getGeoInformation() {
        return geoInformation;
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

    public List<Action> getActions() {
        return actions;
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

    @Override
    public boolean isOnline() {
        Player p = getPlayer();
        return p != null && p.isOnline();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean isBanned() {
        return bannedOnServers.contains(MiscUtils.getIPlan().getServerUuid());
    }

    @Override
    public boolean isWhitelisted() {
        return true;
    }

    @Override
    public void setWhitelisted(boolean b) {
        /* Do nothing */
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public long getFirstPlayed() {
        return registered;
    }

    @Override
    public long getLastPlayed() {
        return getLastSeen(MiscUtils.getIPlan().getServerUuid());
    }

    @Override
    public boolean hasPlayedBefore() {
        return true;
    }

    @Override
    public Location getBedSpawnLocation() {
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<>();
    }

    @Override
    public boolean isOp() {
        return oppedOnServers.contains(MiscUtils.getIPlan().getServerUuid());
    }

    @Override
    public void setOp(boolean b) {
        /* Do nothing */
    }
}