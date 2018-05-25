package com.djrapitops.plan.data.time;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        addWorld(startingWorld, startingGM, System.currentTimeMillis());
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
     * Updates the state at the end of the session.
     * Does not change world or GameMode.
     *
     * @param changeTime epoch ms session ended.
     */
    public void updateState(long changeTime) {
        updateState(currentWorld, currentGamemode, changeTime);
    }

    /**
     * Updates the time status to match the new state.
     *
     * @param worldName  World name of the world swapped to.
     * @param gameMode   GameMode name of the gm swapped to.
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

    /**
     * Used to get the Map for saving.
     *
     * @return Current time map.
     */
    public Map<String, GMTimes> getWorldTimes() {
        return worldTimes;
    }

    public void setGMTimesForWorld(String world, GMTimes gmTimes) {
        worldTimes.put(world, gmTimes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldTimes that = (WorldTimes) o;
        return Objects.equals(worldTimes, that.worldTimes) &&
                Objects.equals(currentWorld, that.currentWorld) &&
                Objects.equals(currentGamemode, that.currentGamemode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldTimes, currentWorld, currentGamemode);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("WorldTimes (Current: " + currentWorld + "){\n");

        for (Map.Entry<String, GMTimes> entry : worldTimes.entrySet()) {
            GMTimes value = entry.getValue();
            b.append("World '").append(entry.getKey()).append("':\n")
                    .append("  Total: ").append(value.getTotal()).append("\n")
                    .append("  ").append(value.toString()).append("\n");
        }

        b.append("}");
        return b.toString();
    }

    public String getCurrentWorld() {
        return currentWorld;
    }

    public void add(WorldTimes toAdd) {
        Map<String, GMTimes> times = toAdd.getWorldTimes();
        for (Map.Entry<String, GMTimes> entry : times.entrySet()) {
            String worldName = entry.getKey();
            GMTimes gmTimes = entry.getValue();

            GMTimes currentGMTimes = getGMTimes(worldName);
            for (String gm : GMTimes.getGMKeyArray()) {
                currentGMTimes.addTime(gm, gmTimes.getTime(gm));
            }
            worldTimes.put(worldName, currentGMTimes);
        }
    }
}
