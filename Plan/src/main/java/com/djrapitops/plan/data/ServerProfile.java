/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * Data class for streamlining Analysis data.
 * <p>
 * Most of the methods are not the most efficient when multiple of them are used.
 *
 * @author Rsl1122
 */
public class ServerProfile {

    private final UUID serverUUID;

    // Database information
    private List<PlayerProfile> players;
    private List<TPS> tps;
    private Map<String, Integer> commandUsage;

    // Active information
    private int playersOnline;
    private int playersMax;

    public ServerProfile(UUID serverUUID) {
        this.serverUUID = serverUUID;
        players = new ArrayList<>();
        tps = new ArrayList<>();
        commandUsage = new HashMap<>();
    }

    public List<PlayerProfile> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerProfile> players) {
        this.players = players;
    }

    public List<TPS> getTps() {
        return tps;
    }

    public void setTps(List<TPS> tps) {
        this.tps = tps;
    }

    public Map<String, Integer> getCommandUsage() {
        return commandUsage;
    }

    public void setCommandUsage(Map<String, Integer> commandUsage) {
        this.commandUsage = commandUsage;
    }

    public double getAverageTPS(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getTicksPerSecond)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double getAverageCPU(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getCPUUsage)
                .filter(num -> num >= 0)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double getAverageRAM(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getUsedMemory)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double getAverageEntities(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getEntityCount)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double getAverageChunks(long after, long before) {
        OptionalDouble average = getTPSData(after, before)
                .mapToDouble(TPS::getChunksLoaded)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public long getNewPlayers(long after, long before) {
        return getPlayersWhoRegistered(after, before).count();
    }

    public long getUniquePlayers(long after, long before) {
        return getPlayersWhoPlayedBetween(after, before).count();
    }

    public double getNewPlayersPerDay(long after, long before) {
        return getNewPlayers(after, before) * 1.0 / AnalysisUtils.getNumberOfDaysBetween(after, before);
    }

    private Stream<PlayerProfile> getPlayersWhoPlayedBetween(long after, long before) {
        return players.stream()
                .filter(player -> player.playedBetween(after, before));
    }

    public Stream<PlayerProfile> getPlayersWhoRegistered(long after, long before) {
        return players.stream()
                .filter(player -> player.getRegistered() >= after && player.getRegistered() <= before);
    }

    public Stream<TPS> getTPSData(long after, long before) {
        return tps.stream().filter(tps -> tps.getDate() >= after && tps.getDate() <= before);
    }
}