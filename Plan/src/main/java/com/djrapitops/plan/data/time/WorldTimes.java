package main.java.com.djrapitops.plan.data.time;

import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TimeKeeper class that tracks the time spent in each World based on Playtime.
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

    public Optional<Long> getWorldPlaytime(String world) {
        GMTimes gmTimes = worldTimes.get(world);
        if (gmTimes != null) {
            return Optional.of(gmTimes.getTotal());
        }
        return Optional.empty();
    }

    public Optional<GMTimes> getGMTimes(String world) {
        GMTimes gmTimes = worldTimes.get(world);
        if (gmTimes != null) {
            return Optional.of(gmTimes);
        }
        return Optional.empty();
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

    public String getCurrentGamemode() {
        return currentGamemode;
    }
}
