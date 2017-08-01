package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.SessionData;

import java.util.Comparator;

/**
 * @author Rsl1122
 */
public class SessionDataComparator implements Comparator<SessionData> {

    @Override
    public int compare(SessionData s1, SessionData s2) {
        return Long.compare(s1.getSessionStart(), s2.getSessionStart());
    }
}
