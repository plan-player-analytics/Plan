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
    
    /**
     *
     * @return
     */
    public long getLength() {
        return sessionEnd-sessionStart;
    }

    @Override
    public String toString() {
        return "s:" + sessionStart + " e:" + sessionEnd;
    }
    
    /**
     *
     * @return
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
        if (this.sessionStart != other.sessionStart) {
            return false;
        }
        if (this.sessionEnd != other.sessionEnd) {
            return false;
        }
        return true;
    }
    
    
}
