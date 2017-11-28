package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Object for storing various information about a player's play session.
 * <p>
 * Includes:
 * <ul>
 * <li>World and GameMode playtimes</li>
 * <li>Player and Mob kills</li>
 * <li>Deaths</li>
 * </ul>
 * <p>
 * Following data can be derived from Sessions in the database (Between any time span):
 * <ul>
 * <li>Playtime</li>
 * <li>LoginTimes</li>
 * </ul>
 *
 * @author Rsl1122
 */
public class Session implements HasDate {

    private final long sessionStart;
    private Integer sessionID;
    private WorldTimes worldTimes;
    private long sessionEnd;
    private List<PlayerKill> playerKills;
    private int mobKills;
    private int deaths;

    /**
     * Creates a new session with given start and end of -1.
     *
     * @param sessionStart Epoch millisecond the session was started.
     */
    public Session(long sessionStart, String world, String gm) {
        this.worldTimes = new WorldTimes(world, gm);
        this.sessionStart = sessionStart;
        this.sessionEnd = -1;
        playerKills = new ArrayList<>();
        mobKills = 0;
        deaths = 0;
    }

    /**
     * Re-Creates a session data object for viewing.
     *
     * @param sessionStart Epoch millisecond the session was started.
     * @param sessionEnd   Epoch millisecond the session ended.
     */
    public Session(int id, long sessionStart, long sessionEnd, int mobKills, int deaths) {
        this.sessionID = id;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
        this.worldTimes = new WorldTimes(new HashMap<>());
        this.playerKills = new ArrayList<>();
        this.mobKills = mobKills;
        this.deaths = deaths;
    }

    /**
     * Starts a new Session.
     *
     * @param time  Time the session started.
     * @param world World the session started in.
     * @param gm    GameMode the session started in.
     * @return a new Session object.
     */
    public static Session start(long time, String world, String gm) {
        return new Session(time, world, gm);
    }

    /**
     * Ends the session with given end point.
     * <p>
     * (Changes the end to the parameter.).
     *
     * @param endOfSession Epoch millisecond the session ended.
     */
    public void endSession(long endOfSession) {
        sessionEnd = endOfSession;
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
        if (sessionEnd == -1) {
            return MiscUtils.getTime() - sessionStart;
        }
        return sessionEnd - sessionStart;
    }

    /**
     * Get the start of the session.
     *
     * @return Epoch millisecond the session started.
     */
    public long getSessionStart() {
        return sessionStart;
    }

    /**
     * Get the end of the session.
     *
     * @return Epoch millisecond the session ended.
     */
    public long getSessionEnd() {
        return sessionEnd;
    }

    public WorldTimes getWorldTimes() {
        return worldTimes;
    }

    public void setWorldTimes(WorldTimes worldTimes) {
        this.worldTimes = worldTimes;
    }

    public List<PlayerKill> getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(List<PlayerKill> playerKills) {
        this.playerKills = playerKills;
    }

    public int getMobKills() {
        return mobKills;
    }

    public int getDeaths() {
        return deaths;
    }

    public boolean isFetchedFromDB() {
        return sessionID != null;
    }

    /**
     * Used to get the ID of the session in the Database.
     *
     * @return ID if present.
     * @throws NullPointerException if Session was not fetched from DB. Condition using {@code isFetchedFromDB}
     */
    public int getSessionID() {
        return sessionID != null ? sessionID : -1;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return sessionStart == session.sessionStart &&
                sessionEnd == session.sessionEnd &&
                mobKills == session.mobKills &&
                deaths == session.deaths &&
                Objects.equals(worldTimes, session.worldTimes) &&
                Objects.equals(playerKills, session.playerKills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionStart, sessionID, worldTimes, sessionEnd, playerKills, mobKills, deaths);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sessionStart", sessionStart)
                .append("sessionID", sessionID)
                .append("worldTimes", worldTimes)
                .append("sessionEnd", sessionEnd)
                .append("playerKills", playerKills)
                .append("mobKills", mobKills)
                .append("deaths", deaths)
                .toString();
    }

    @Override
    public long getDate() {
        return getSessionStart();
    }
}
