/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html.tables;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.analysis.JoinInfoPart;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.SessionCache;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.*;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class SessionsTableCreator {

    public static String[] createTables(JoinInfoPart joinInfoPart) {
        Map<Integer, UUID> uuidByID = new HashMap<>();
        for (Map.Entry<UUID, List<Session>> entry : joinInfoPart.getSessions().entrySet()) {
            List<Session> sessions = entry.getValue();
            for (Session session : sessions) {
                uuidByID.put(session.getSessionID(), entry.getKey());
            }
        }

        List<Session> allSessions = joinInfoPart.getAllSessions();
        if (allSessions.isEmpty()) {
            return new String[]{Html.TABLELINE_4.parse("<b>No Sessions</b>", "", "", ""),
                    Html.TABLELINE_2.parse("<b>No Sessions</b>", "")};
        }

        allSessions.sort(new SessionStartComparator());

        StringBuilder sessionTableBuilder = new StringBuilder();
        StringBuilder recentLoginsBuilder = new StringBuilder();

        int i = 0;
        Set<String> recentLoginsNames = new HashSet<>();

        DataCache dataCache = Plan.getInstance().getDataCache();

        Map<Long, UUID> uuidBySessionStart = new HashMap<>();
        for (Map.Entry<UUID, Session> entry : SessionCache.getActiveSessions().entrySet()) {
            uuidBySessionStart.put(entry.getValue().getSessionStart(), entry.getKey());
        }


        for (Session session : allSessions) {
            if (i >= 50) {
                break;
            }

            UUID uuid;
            if (session.isFetchedFromDB()) {
                uuid = uuidByID.get(session.getSessionID());
            } else {
                uuid = uuidBySessionStart.get(session.getSessionStart());
            }

            String name = dataCache.getName(uuid);
            String start = FormatUtils.formatTimeStamp(session.getSessionStart());
            String length = session.getSessionEnd() != -1 ? FormatUtils.formatTimeAmount(session.getLength()) : "Online";
            String world = getLongestWorldPlayed(session);

            String inspectUrl = Plan.getPlanAPI().getPlayerInspectPageLink(name);
            sessionTableBuilder.append(Html.TABLELINE_4.parse(
                    Html.LINK.parse(inspectUrl, name),
                    start,
                    length,
                    world
            ));

            if (recentLoginsNames.size() < 20 && !recentLoginsNames.contains(name)) {
                recentLoginsBuilder.append(Html.TABLELINE_2.parse(Html.LINK.parse(inspectUrl, name), start));
                recentLoginsNames.add(name);
            }

            i++;
        }
        return new String[]{sessionTableBuilder.toString(), recentLoginsBuilder.toString()};
    }

    private static String getLongestWorldPlayed(Session session) {
        if (session.getSessionEnd() == -1) {
            return "Current: " + session.getWorldTimes().getCurrentWorld();
        }

        WorldTimes worldTimes = session.getWorldTimes();
        long total = worldTimes.getTotal();
        long longest = 0;
        String theWorld = "-";
        for (Map.Entry<String, GMTimes> entry : worldTimes.getWorldTimes().entrySet()) {
            String world = entry.getKey();
            long time = entry.getValue().getTotal();
            if (time > longest) {
                longest = time;
                theWorld = world;
            }
        }

        double percentage = longest * 100.0 / total;

        return theWorld + " (" + FormatUtils.cutDecimals(percentage) + "%)";
    }
}