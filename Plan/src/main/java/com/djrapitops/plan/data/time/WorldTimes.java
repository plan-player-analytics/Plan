package main.java.com.djrapitops.plan.data.time;

import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that tracks the time spent in each World based on GMTimes.
 *
 * @author Rsl1122
 * @since 3.6.0
 */
public class WorldTimes {

    private final Map<String, GMTimes> worldTimes;
    private String currentWorld;
    private String currentGamemode;

    /**
     * Creates a new Empty WorldTimes object.
     *
     * @param startingWorld World to start the calculations at.
     * @param startingGM    GameMode to start the calculations at.
     */
    public WorldTimes(String startingWorld, String startingGM) {
        worldTimes = new HashMap<>();
        currentWorld = startingWorld;
        currentGamemode = startingGM;
        addWorld(startingWorld, startingGM, MiscUtils.getTime());
    }

    public WorldTimes(Map<String, GMTimes> times, String lastWorld, String lastGM) {
        worldTimes = times;
        currentWorld = lastWorld;
        currentGamemode = lastGM;
    }

    private void addWorld(String worldName, String gameMode, long changeTime) {
        worldTimes.put(worldName, new GMTimes(gameMode, changeTime));
    }

    public void updateState(String worldName, String gameMode, long changeTime) {
        GMTimes currentGMTimes = worldTimes.get(currentWorld);
        if (worldName.equals(currentWorld)) {
            currentGMTimes.changeState(gameMode, changeTime);
        } else {
            GMTimes newGMTimes = worldTimes.get(worldName);
            if (newGMTimes == null) {
                addWorld(worldName, gameMode, currentGMTimes.getLastStateChange());
            }
            currentGMTimes.changeState(currentGamemode, changeTime);
        }
        for (GMTimes gmTimes : worldTimes.values()) {
            gmTimes.setLastStateChange(changeTime);
        }
        currentWorld = worldName;
        currentGamemode = gameMode;
    }

    /**
     * Used to get a total playtime of a world.
     *
     * @param world World name being checked.
     * @return total milliseconds spent in a world.
     */
    public long getWorldPlaytime(String world) {
        GMTimes gmTimes = worldTimes.get(world);
        if (gmTimes != null) {
            return gmTimes.getTotal();
        }
        return 0;
    }

    public long getTotal() {
        return worldTimes.values().stream()
                .mapToLong(GMTimes::getTotal)
                .sum();
    }

    /**
     * Used for Quick access to time of each GameMode.
     * <p>
     * Should not be used for changing state,
     * because if player has not played in the world,
     * an empty GMTimes is given, with 0 as playtime
     *
     * @param world World name being checked.
     * @return GMTimes object with play times of each GameMode.
     */
    public GMTimes getGMTimes(String world) {
        GMTimes gmTimes = worldTimes.get(world);
        if (gmTimes != null) {
            return gmTimes;
        }
        return new GMTimes();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("WorldTimes (Current: " + currentWorld + "){\n");
        for (Map.Entry<String, GMTimes> entry : worldTimes.entrySet()) {
            b.append("World '").append(entry.getKey()).append("':\n");
            GMTimes value = entry.getValue();
            b.append("  Total: ").append(value.getTotal()).append("\n");
            b.append("  ").append(value.toString()).append("\n");
        }
        b.append("}");
        return b.toString();
    }

    public String getCurrentWorld() {
        return currentWorld;
    }
}
