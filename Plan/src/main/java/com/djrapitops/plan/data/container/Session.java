package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.info.server.ServerInfo;

import java.util.*;

/**
 * DataContainer for information about a player's play session.
 *
 * @author Rsl1122
 * @see SessionKeys for Key objects.
 */
public class Session extends DataContainer implements DateHolder {

    private int mobKills;
    private int deaths;
    private long afkTime;

    /**
     * Creates a new session.
     *
     * @param uuid         UUID of the Player.
     * @param sessionStart Epoch ms the session started.
     * @param world        Starting world.
     * @param gm           Starting GameMode.
     */
    public Session(UUID uuid, long sessionStart, String world, String gm) {
        mobKills = 0;
        deaths = 0;
        afkTime = 0;

        putRawData(SessionKeys.UUID, uuid);
        putRawData(SessionKeys.START, sessionStart);
        putRawData(SessionKeys.WORLD_TIMES, new WorldTimes(world, gm));
        putRawData(SessionKeys.PLAYER_KILLS, new ArrayList<>());
        putSupplier(SessionKeys.MOB_KILL_COUNT, () -> mobKills);
        putSupplier(SessionKeys.DEATH_COUNT, () -> deaths);
        putSupplier(SessionKeys.AFK_TIME, () -> afkTime);

        putSupplier(SessionKeys.PLAYER_KILL_COUNT, getUnsafe(SessionKeys.PLAYER_KILLS)::size);
        putSupplier(SessionKeys.LENGTH, () ->
                getValue(SessionKeys.END).orElse(System.currentTimeMillis()) - getUnsafe(SessionKeys.START));
        putSupplier(SessionKeys.ACTIVE_TIME, () -> getUnsafe(SessionKeys.LENGTH) - getUnsafe(SessionKeys.AFK_TIME));
        putSupplier(SessionKeys.SERVER_UUID, ServerInfo::getServerUUID);
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
        putRawData(SessionKeys.DB_ID, id);
        putRawData(SessionKeys.UUID, uuid);
        putRawData(SessionKeys.SERVER_UUID, serverUUID);
        putRawData(SessionKeys.START, sessionStart);
        putRawData(SessionKeys.END, sessionEnd);
        putRawData(SessionKeys.WORLD_TIMES, new WorldTimes(new HashMap<>()));
        putRawData(SessionKeys.PLAYER_KILLS, new ArrayList<>());
        putSupplier(SessionKeys.MOB_KILL_COUNT, () -> mobKills);
        putSupplier(SessionKeys.DEATH_COUNT, () -> deaths);
        putSupplier(SessionKeys.AFK_TIME, () -> afkTime);

        this.mobKills = mobKills;
        this.deaths = deaths;
        this.afkTime = afkTime;

        putSupplier(SessionKeys.PLAYER_KILL_COUNT, () -> getUnsafe(SessionKeys.PLAYER_KILLS).size());
        putSupplier(SessionKeys.LENGTH, () ->
                getValue(SessionKeys.END).orElse(System.currentTimeMillis()) - getUnsafe(SessionKeys.START));
        putSupplier(SessionKeys.ACTIVE_TIME, () -> getUnsafe(SessionKeys.LENGTH) - getUnsafe(SessionKeys.AFK_TIME));
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
        WorldTimes worldTimes = getValue(SessionKeys.WORLD_TIMES)
                .orElseThrow(() -> new IllegalStateException("World times have not been defined"));
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
        WorldTimes worldTimes = getValue(SessionKeys.WORLD_TIMES)
                .orElseThrow(() -> new IllegalStateException("World times is not defined"));
        worldTimes.updateState(world, gm, time);
    }

    public void playerKilled(PlayerKill kill) {
        List<PlayerKill> playerKills = getValue(SessionKeys.PLAYER_KILLS)
                .orElseThrow(() -> new IllegalStateException("Player kills is not defined."));
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

    /**
     * Get the start of the session.
     *
     * @return Epoch millisecond the session started.
     */
    @Deprecated
    public long getSessionStart() {
        return getUnsafe(SessionKeys.START);
    }

    /**
     * Get the end of the session.
     *
     * @return Epoch millisecond the session ended.
     */
    @Deprecated
    public long getSessionEnd() {
        return getValue(SessionKeys.END).orElse(-1L);
    }

    @Deprecated
    public WorldTimes getWorldTimes() {
        return getValue(SessionKeys.WORLD_TIMES).orElse(null);
    }

    public void setWorldTimes(WorldTimes worldTimes) {
        putRawData(SessionKeys.WORLD_TIMES, worldTimes);
    }

    @Deprecated
    public List<PlayerKill> getPlayerKills() {
        return getValue(SessionKeys.PLAYER_KILLS).orElse(new ArrayList<>());
    }

    public void setPlayerKills(List<PlayerKill> playerKills) {
        putRawData(SessionKeys.PLAYER_KILLS, playerKills);
    }

    @Deprecated
    public int getMobKills() {
        return mobKills;
    }

    @Deprecated
    public int getDeaths() {
        return deaths;
    }

    public boolean isFetchedFromDB() {
        return supports(SessionKeys.DB_ID);
    }

    public void addAFKTime(long timeAFK) {
        afkTime += timeAFK;
    }

    @Deprecated
    public long getAfkLength() {
        return afkTime;
    }

    @Deprecated
    public long getActiveLength() {
        return getLength() - getAfkLength();
    }

    /**
     * Used to get the ID of the session in the Database.
     *
     * @return ID if present.
     * @throws NullPointerException if Session was not fetched from DB. Check {@code isFetchedFromDB} first.
     */
    @Deprecated
    public int getSessionID() {
        return getValue(SessionKeys.DB_ID).orElseThrow(() -> new NullPointerException("Session not fetched from DB."));
    }

    public void setSessionID(int sessionID) {
        putRawData(SessionKeys.DB_ID, sessionID);
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
                Objects.equals(
                        getValue(SessionKeys.WORLD_TIMES).orElse(null),
                        session.getValue(SessionKeys.WORLD_TIMES).orElse(null)
                ) &&
                Objects.equals(
                        getValue(SessionKeys.PLAYER_KILLS).orElse(new ArrayList<>()),
                        session.getValue(SessionKeys.PLAYER_KILLS).orElse(new ArrayList<>())
                );
    }
}
