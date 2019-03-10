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
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.containers.DynamicDataContainer;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.time.WorldTimes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DataContainer for information about a player's play session.
 *
 * @author Rsl1122
 * @see SessionKeys for Key objects.
 */
public class Session extends DynamicDataContainer implements DateHolder {

    private long sessionStart;
    private WorldTimes worldTimes;
    private List<PlayerKill> playerKills;

    private int mobKills;
    private int deaths;
    private long afkTime;

    /**
     * Creates a new session.
     *
     * @param uuid         UUID of the Player.
     * @param serverUUID   UUID of the server.
     * @param sessionStart Epoch ms the session started.
     * @param world        Starting world.
     * @param gm           Starting GameMode.
     */
    public Session(UUID uuid, UUID serverUUID, long sessionStart, String world, String gm) {
        this.sessionStart = sessionStart;
        worldTimes = new WorldTimes(world, gm, sessionStart);
        playerKills = new ArrayList<>();

        mobKills = 0;
        deaths = 0;
        afkTime = 0;

        putRawData(SessionKeys.UUID, uuid);
        putRawData(SessionKeys.SERVER_UUID, serverUUID);
        putSupplier(SessionKeys.START, this::getSessionStart);
        putSupplier(SessionKeys.WORLD_TIMES, this::getWorldTimes);
        putSupplier(SessionKeys.PLAYER_KILLS, this::getPlayerKills);
        putRawData(SessionKeys.PLAYER_DEATHS, new ArrayList<>());
        putSupplier(SessionKeys.MOB_KILL_COUNT, this::getMobKills);
        putSupplier(SessionKeys.DEATH_COUNT, this::getDeaths);
        putSupplier(SessionKeys.AFK_TIME, this::getAfkTime);

        putSupplier(SessionKeys.PLAYER_KILL_COUNT, getUnsafe(SessionKeys.PLAYER_KILLS)::size);
        putSupplier(SessionKeys.LENGTH, () ->
                getValue(SessionKeys.END).orElse(System.currentTimeMillis()) - getUnsafe(SessionKeys.START));
        putSupplier(SessionKeys.ACTIVE_TIME, () -> getUnsafe(SessionKeys.LENGTH) - getUnsafe(SessionKeys.AFK_TIME));

        putRawData(SessionKeys.LONGEST_WORLD_PLAYED, "Key is Deprecated, use WorldAliasSettings#getLongestWorldPlayed(Session) instead.");
    }

    /**
     * Recreates a Session found in the database.
     * <p>
     * WorldTimes and Player kills need to be set separately.
     *
     * @param id           ID in the database (Used for fetching world times and player kills.
     * @param uuid         UUID of the Player.
     * @param serverUUID   UUID of the Server.
     * @param sessionStart Epoch ms the session started.
     * @param sessionEnd   Epoch ms the session ended.
     * @param mobKills     Mobs killed during the session.
     * @param deaths       Death count during the session.
     * @param afkTime      Time spent AFK during the session.
     */
    public Session(int id, UUID uuid, UUID serverUUID, long sessionStart, long sessionEnd, int mobKills, int deaths, long afkTime) {
        this.sessionStart = sessionStart;
        worldTimes = new WorldTimes();
        playerKills = new ArrayList<>();

        this.mobKills = mobKills;
        this.deaths = deaths;
        this.afkTime = afkTime;

        putRawData(SessionKeys.DB_ID, id);
        putRawData(SessionKeys.UUID, uuid);
        putRawData(SessionKeys.SERVER_UUID, serverUUID);
        putSupplier(SessionKeys.START, this::getSessionStart);
        putRawData(SessionKeys.END, sessionEnd);
        putSupplier(SessionKeys.WORLD_TIMES, this::getWorldTimes);
        putSupplier(SessionKeys.PLAYER_KILLS, this::getPlayerKills);
        putRawData(SessionKeys.PLAYER_DEATHS, new ArrayList<>());
        putSupplier(SessionKeys.MOB_KILL_COUNT, this::getMobKills);
        putSupplier(SessionKeys.DEATH_COUNT, this::getDeaths);
        putSupplier(SessionKeys.AFK_TIME, this::getAfkTime);

        putSupplier(SessionKeys.PLAYER_KILL_COUNT, () -> getUnsafe(SessionKeys.PLAYER_KILLS).size());
        putSupplier(SessionKeys.LENGTH, () ->
                getValue(SessionKeys.END).orElse(System.currentTimeMillis()) - getUnsafe(SessionKeys.START));
        putSupplier(SessionKeys.ACTIVE_TIME, () -> getUnsafe(SessionKeys.LENGTH) - getUnsafe(SessionKeys.AFK_TIME));

        putRawData(SessionKeys.LONGEST_WORLD_PLAYED, "Key is Deprecated, use WorldAliasSettings#getLongestWorldPlayed(Session) instead.");
    }

    /**
     * Ends the session with given end point.
     * <p>
     * Updates world times to the latest value.
     *
     * @param endOfSession Epoch millisecond the session ended.
     */
    public void endSession(long endOfSession) {
        putRawData(SessionKeys.END, endOfSession);
        worldTimes.updateState(endOfSession);
    }

    /**
     * Updates WorldTimes state.
     *
     * @param world World Name the player has moved to
     * @param gm    GameMode the player is in.
     * @param time  Epoch ms of the event.
     */
    public void changeState(String world, String gm, long time) {
        worldTimes.updateState(world, gm, time);
    }

    public void playerKilled(PlayerKill kill) {
        playerKills.add(kill);
    }

    public void mobKilled() {
        mobKills++;
    }

    public void died() {
        deaths++;
    }

    /**
     * Get the length of the session in milliseconds.
     *
     * @return Long in ms.
     */
    public long getLength() {
        return getValue(SessionKeys.LENGTH).orElse(0L);
    }

    @Override
    public long getDate() {
        return getUnsafe(SessionKeys.START);
    }

    public void setWorldTimes(WorldTimes worldTimes) {
        this.worldTimes = worldTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return getUnsafe(SessionKeys.START).equals(session.getUnsafe(SessionKeys.START)) &&
                getValue(SessionKeys.END).orElse(-1L).equals(session.getValue(SessionKeys.END).orElse(-1L)) &&
                mobKills == session.mobKills &&
                deaths == session.deaths &&
                Objects.equals(playerKills, session.playerKills) &&
                Objects.equals(worldTimes, session.worldTimes);
    }

    public boolean isFetchedFromDB() {
        return supports(SessionKeys.DB_ID);
    }

    public void addAFKTime(long timeAFK) {
        afkTime += timeAFK;
    }

    public void setSessionID(int sessionID) {
        putRawData(SessionKeys.DB_ID, sessionID);
    }

    public List<PlayerKill> getPlayerKills() {
        return playerKills;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mobKills, deaths, afkTime);
    }

    private long getSessionStart() {
        return sessionStart;
    }

    private WorldTimes getWorldTimes() {
        return worldTimes;
    }

    public void setPlayerKills(List<PlayerKill> playerKills) {
        this.playerKills = playerKills;
    }

    private int getMobKills() {
        return mobKills;
    }

    private int getDeaths() {
        return deaths;
    }

    private long getAfkTime() {
        return afkTime;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionStart=" + getUnsafe(SessionKeys.START) +
                ", sessionEnd=" + getUnsafe(SessionKeys.END) +
                ", worldTimes=" + worldTimes +
                ", playerKills=" + playerKills +
                ", mobKills=" + mobKills +
                ", deaths=" + deaths +
                ", afkTime=" + afkTime +
                '}';
    }
}
