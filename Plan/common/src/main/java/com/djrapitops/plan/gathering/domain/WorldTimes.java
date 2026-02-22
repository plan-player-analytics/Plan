/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.gathering.domain;

import org.apache.commons.text.TextStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Class that tracks the time spent in each World based on GMTimes.
 *
 * @author AuroraLS3
 */
public class WorldTimes {

    private final Map<String, GMTimes> times;
    private String currentWorld;
    private String currentGamemode;

    /**
     * Creates a new Empty WorldTimes object.
     *
     * @param startingWorld World to start the calculations at.
     * @param startingGM    GameMode to start the calculations at.
     * @param time          Epoch ms the time calculation should start
     */
    public WorldTimes(String startingWorld, String startingGM, long time) {
        times = new HashMap<>();
        currentWorld = startingWorld;
        currentGamemode = startingGM;
        addWorld(startingWorld, startingGM, time);
    }

    /**
     * Re-Creates an existing WorldTimes object for viewing.
     *
     * @param times Map of each World's GMTimes object.
     */
    public WorldTimes(Map<String, GMTimes> times) {
        this.times = times;
    }

    public WorldTimes() {
        this(new HashMap<>());
    }

    private void addWorld(String worldName, String gameMode, long changeTime) {
        if (worldName == null || gameMode == null) return;
        times.put(worldName, new GMTimes(gameMode, changeTime));
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
        if (worldName == null || gameMode == null) return;

        GMTimes currentGMTimes = times.get(currentWorld);
        if (currentWorld.equals(worldName)) {
            currentGMTimes.changeState(gameMode, changeTime);
        } else {
            GMTimes newGMTimes = times.get(worldName);
            if (newGMTimes == null) {
                addWorld(worldName, gameMode, currentGMTimes.getLastStateChange());
            }
            currentGMTimes.changeState(currentGamemode, changeTime);
        }

        for (GMTimes gmTimes : times.values()) {
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
        GMTimes gmTimes = times.get(world);
        return gmTimes != null ? gmTimes.getTotal() : 0;
    }

    public long getTotal() {
        return times.values().stream()
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
        return times.getOrDefault(world, new GMTimes());
    }

    public Map<String, GMTimes> getWorldTimes() {
        return times;
    }

    public void setGMTimesForWorld(String world, GMTimes gmTimes) {
        times.put(world, gmTimes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldTimes that = (WorldTimes) o;
        return Objects.equals(times, that.times) &&
                Objects.equals(currentWorld, that.currentWorld) &&
                Objects.equals(currentGamemode, that.currentGamemode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(times, currentWorld, currentGamemode);
    }

    @Override
    public String toString() {
        return "WorldTimes{" +
                "times=" + times +
                ", currentWorld='" + currentWorld + '\'' +
                ", currentGamemode='" + currentGamemode + '\'' +
                '}';
    }

    public Optional<String> getCurrentWorld() {
        return Optional.ofNullable(currentWorld);
    }

    public void add(WorldTimes toAdd) {
        for (Map.Entry<String, GMTimes> entry : toAdd.getWorldTimes().entrySet()) {
            String worldName = entry.getKey();
            GMTimes gmTimes = entry.getValue();

            GMTimes currentGMTimes = getGMTimes(worldName);
            for (String gm : GMTimes.getGMKeyArray()) {
                currentGMTimes.addTime(gm, gmTimes.getTime(gm));
            }
            this.times.put(worldName, currentGMTimes);
        }
    }

    public boolean contains(String worldName) {
        return times.containsKey(worldName);
    }

    public boolean isEmpty() {
        return getWorldTimes().isEmpty();
    }

    public void setAll(WorldTimes worldTimes) {
        times.clear();
        for (Map.Entry<String, GMTimes> entry : worldTimes.getWorldTimes().entrySet()) {
            setGMTimesForWorld(entry.getKey(), entry.getValue());
        }
    }

    public String toJson() {
        return "{\"times\": {" +
                new TextStringBuilder().appendWithSeparators(times.entrySet().stream()
                        .map(entry -> "\"" + entry.getKey() + "\": " + entry.getValue().toJson())
                        .iterator(), ",").get() +
                "  }," +
                (currentWorld != null ? "\"currentWorld\": \"" + currentWorld + "\"," : "\"currentWorld\": null,") +
                (currentGamemode != null ? "\"currentGamemode\": \"" + currentGamemode + "\"" : "\"currentGamemode\": null") +
                "}";
    }
}
