package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.data.time.WorldTimes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Object for storing various information about a player's play session.
 * <p>
 * Includes:
 * <ul>
 * <li>World & GameMode playtimes</li>
 * <li>Player & Mob kills</li>
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
public class Session {

    private Long sessionID;
    private WorldTimes worldTimes;
    private final long sessionStart;
    private long sessionEnd;
    private List<KillData> playerKills;
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
    public Session(long id, long sessionStart, long sessionEnd, int mobKills, int deaths) {
        this.sessionID = id;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
        this.worldTimes = new WorldTimes(new HashMap<>());
        this.playerKills = new ArrayList<>();
        this.mobKills = mobKills;
        this.deaths = deaths;
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

    public void playerKilled(KillData kill) {
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

    public List<KillData> getPlayerKills() {
        return playerKills;
    }

    public int getMobKills() {
        return mobKills;
    }

    public int getDeaths() {
        return deaths;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Session other = (Session) obj;
        return this.sessionStart == other.sessionStart && this.sessionEnd == other.sessionEnd;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.sessionStart ^ (this.sessionStart >>> 32));
        hash = 97 * hash + (int) (this.sessionEnd ^ (this.sessionEnd >>> 32));
        return hash;
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

    public boolean isFetchedFromDB() {
        return sessionID != null;
    }

    public void setWorldTimes(WorldTimes worldTimes) {
        this.worldTimes = worldTimes;
    }

    public void setPlayerKills(List<KillData> playerKills) {
        this.playerKills = playerKills;
    }

    /**
     * Used to get the ID of the session in the Database.
     *
     * @return ID if present.
     * @throws NullPointerException if Session was not fetched from DB. Check using {@code isFetchedFromDB}
     */
    public long getSessionID() {
        return sessionID;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }
}
