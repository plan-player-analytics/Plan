package main.java.com.djrapitops.plan.data;

/**
 *
 * @author Rsl1122
 */
public class SessionData {

    private long sessionStart;
    private long sessionEnd;

    public SessionData(long sessionStart) {
        this.sessionStart = sessionStart;
        this.sessionEnd = -1;
    }

    public SessionData(long sessionStart, long sessionEnd) {
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
    }

    public void endSession(long endOfSession) {
        sessionEnd = endOfSession;
    }

    public long getSessionStart() {
        return sessionStart;
    }

    public long getSessionEnd() {
        return sessionEnd;
    }
}
