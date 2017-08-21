package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.data.time.WorldTimes;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for storing start and end of a play session inside UserData
 * object.
 *
 * @author Rsl1122
 */
public class SessionData {

    private final WorldTimes worldTimes; // TODO add World Times to SessionData
    private final long sessionStart;
    private long sessionEnd;
    private final List<KillData> playerKills;
    private int mobKills;
    private int deaths;


    @Deprecated // TODO Remove
    public SessionData(long sessionStart) {
        worldTimes = null;
        this.sessionStart = 0;
        playerKills = null;
    }

    /**
     * Creates a new session with given start and end of -1.
     *
     * @param sessionStart Epoch millisecond the session was started.
     */
    public SessionData(long sessionStart, String world, String gm) {
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
    public SessionData(long sessionStart, long sessionEnd, WorldTimes worldTimes, List<KillData> playerKills, int mobKills, int deaths) {
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
        this.worldTimes = worldTimes;
        this.playerKills = playerKills;
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

    /**
     * Check if the session start was before the end.
     *
     * @return Is the length positive?
     */
    @Deprecated // TODO Remove
    public boolean isValid() {
        return sessionStart <= sessionEnd;
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
        final SessionData other = (SessionData) obj;
        return this.sessionStart == other.sessionStart && this.sessionEnd == other.sessionEnd;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.sessionStart ^ (this.sessionStart >>> 32));
        hash = 97 * hash + (int) (this.sessionEnd ^ (this.sessionEnd >>> 32));
        return hash;
    }
}
