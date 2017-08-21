package main.java.com.djrapitops.plan.data.time;

import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that tracks the time spent in each World based on GMTimes.
 *
 * @author Rsl1122
 * @since 4.0.0
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

    /**
     * Re-Creates an existing WorldTimes object for viewing.
     *
     * @param times Map of each World's GMTimes object.
     */
    public WorldTimes(Map<String, GMTimes> times) {
        worldTimes = times;
    }

    private void addWorld(String worldName, String gameMode, long changeTime) {
        worldTimes.put(worldName, new GMTimes(gameMode, changeTime));
    }

    /**
     * Updates the time status to match the new state.
     *
     * @param worldName World name of the world swapped to.
     * @param gameMode GameMode name of the gm swapped to.
     * @param changeTime Epoch ms the change occurred.
     */
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
        return gmTimes != null ? gmTimes.getTotal() : 0;
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
        return worldTimes.getOrDefault(world, new GMTimes());
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

    /**
     * Used to get the Map for saving.
     *
     * @return Current time map.
     */
    public Map<String, GMTimes> getWorldTimes() {
        return worldTimes;
    }
}
