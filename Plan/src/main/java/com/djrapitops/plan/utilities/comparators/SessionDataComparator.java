package main.java.com.djrapitops.plan.utilities.comparators;

import java.util.Comparator;
import main.java.com.djrapitops.plan.data.SessionData;

/**
 *
 * @author Rsl1122
 */
public class SessionDataComparator implements Comparator<SessionData> {

    // This method should only be used if FactionsHook.isEnabled() returns true.
    @Override
    public int compare(SessionData s1, SessionData s2) {
        if (s1.getSessionStart() == s2.getSessionStart()) {
            return 0;
        }
        if (s1.getSessionStart() > s2.getSessionStart()) {
            return 1;
        }
        return -1;
    }
}
