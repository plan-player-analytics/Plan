package main.java.com.djrapitops.plan.data;

/**
 *
 * @author Rsl1122
 */
public class SessionData {

    private long sessionStart;
    private long sessionEnd;

    /**
     *
     * @param sessionStart
     */
    public SessionData(long sessionStart) {
        this.sessionStart = sessionStart;
        this.sessionEnd = -1;
    }

    /**
     *
     * @param sessionStart
     * @param sessionEnd
     */
    public SessionData(long sessionStart, long sessionEnd) {
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
    }

    /**
     *
     * @param endOfSession
     */
    public void endSession(long endOfSession) {
        sessionEnd = endOfSession;
    }

    /**
     *
     * @return
     */
    public long getSessionStart() {
        return sessionStart;
    }

    /**
     *
     * @return
     */
    public long getSessionEnd() {
        return sessionEnd;
    }
}
