package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.data.time.WorldTimes;

/**
 * This class is used for storing start and end of a play session inside UserData
 * object.
 *
 * @author Rsl1122
 */
public class SessionData {

    private WorldTimes worldTimes; // TODO add World Times to SessionData
    private final long sessionStart;
    private long sessionEnd;
    // TODO Add kills & deaths to SessionData

    /**
     * Creates a new session with given start and end of -1.
     *
     * @param sessionStart Epoch millisecond the session was started.
     */
    public SessionData(long sessionStart) {
        this.sessionStart = sessionStart;
        this.sessionEnd = -1;
    }

    /**
     * Creates a new session with given start and end.
     *
     * @param sessionStart Epoch millisecond the session was started.
     * @param sessionEnd   Epoch millisecond the session ended.
     */
    public SessionData(long sessionStart, long sessionEnd) {
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
    }

    /**
     * Constructor for copying the object.
     *
     * @param s SessionData to copy.
     */
    public SessionData(SessionData s) {
        this.sessionStart = s.getSessionStart();
        this.sessionEnd = s.getSessionEnd();
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
     * Get the length of the session in milliseconds.
     *
     * @return Long in ms.
     */
    public long getLength() {
        return sessionEnd - sessionStart;
    }

    @Override
    public String toString() {
        return "s:" + sessionStart + " e:" + sessionEnd;
    }

    /**
     * Check if the session start was before the end.
     *
     * @return Is the length positive?
     */
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
