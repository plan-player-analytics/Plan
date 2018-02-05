/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for creating HTML {@code <table>}-element with Sessions inside it.
 *
 * @author Rsl1122
 */
// TODO start using TableContainer
public class SessionsTableCreator {

    private static Map<Integer, UUID> getUUIDsByID(Map<UUID, List<Session>> sessionsByUser) {
        Map<Integer, UUID> uuidByID = new HashMap<>();
        for (Map.Entry<UUID, List<Session>> entry : sessionsByUser.entrySet()) {
            List<Session> sessions = entry.getValue();
            for (Session session : sessions) {
                uuidByID.put(session.getSessionID(), entry.getKey());
            }
        }
        return uuidByID;
    }

    public static String[] createTable(Map<UUID, List<Session>> sessionsByUser, List<Session> allSessions) {
        if (allSessions.isEmpty()) {
            return new String[]{Html.TABLELINE_4.parse("<b>No Sessions</b>", "", "", ""),
                    Html.TABLELINE_2.parse("<b>No Sessions</b>", "")};
        }

        Map<Integer, UUID> uuidByID = getUUIDsByID(sessionsByUser);

        allSessions.sort(new SessionStartComparator());

        StringBuilder sessionTableBuilder = new StringBuilder();
        StringBuilder recentLoginsBuilder = new StringBuilder();


        Set<String> recentLoginsNames = new HashSet<>();



        Map<Long, UUID> uuidBySessionStart = new HashMap<>();
        for (Map.Entry<UUID, Session> entry : SessionCache.getActiveSessions().entrySet()) {
            uuidBySessionStart.put(entry.getValue().getSessionStart(), entry.getKey());
        }
        int i = 0;
        int maxSessions = Settings.MAX_SESSIONS.getNumber();
        if (maxSessions <= 0) {
            maxSessions = 50;
        }

        DataCache dataCache = DataCache.getInstance();
        for (Session session : allSessions) {
            if (i >= maxSessions) {
                break;
            }

            UUID uuid;
            if (session.isFetchedFromDB()) {
                uuid = uuidByID.get(session.getSessionID());
            } else {
                uuid = uuidBySessionStart.get(session.getSessionStart());
            }

            String name = dataCache.getName(uuid);
            String start = FormatUtils.formatTimeStampYear(session.getSessionStart());
            String length = session.getSessionEnd() != -1 ? FormatUtils.formatTimeAmount(session.getLength()) : "Online";
            String world = getLongestWorldPlayed(session);

            String inspectUrl = PlanAPI.getInstance().getPlayerInspectPageLink(name);
            String toolTip = "Session ID: " + (session.isFetchedFromDB() ? session.getSessionID() : "Not Saved.");
            sessionTableBuilder.append(Html.TABLELINE_4.parse(
                    Html.LINK_TOOLTIP.parse(inspectUrl, name, toolTip),
                    start,
                    length,
                    world
            ));

            if (recentLoginsNames.size() < 20 && !recentLoginsNames.contains(name)) {
                boolean isNew = sessionsByUser.get(uuid).size() <= 2;

                recentLoginsBuilder.append("<li><a class=\"col-").append(isNew ? "light-green" : "blue").append(" font-bold\" href=\"").append(inspectUrl)
                        .append("\">").append(name).append("</a><span class=\"pull-right\">").append(start).append("</span></li>");

                recentLoginsNames.add(name);
            }

            i++;
        }
        return new String[]{sessionTableBuilder.toString(), recentLoginsBuilder.toString()};
    }

    public static String getLongestWorldPlayed(Session session) {
        WorldAliasSettings aliasSettings = new WorldAliasSettings();
        Map<String, String> aliases = aliasSettings.getAliases();
        if (session.getSessionEnd() == -1) {
            return "Current: " + aliases.get(session.getWorldTimes().getCurrentWorld());
        }

        Map<String, Long> playtimePerWorld = session.getWorldTimes().getWorldTimes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTotal()));
        Map<String, Long> playtimePerAlias = WorldPie.transformToAliases(playtimePerWorld, aliases);

        WorldTimes worldTimes = session.getWorldTimes();
        long total = worldTimes.getTotal();
        long longest = 0;
        String theWorld = "-";
        for (Map.Entry<String, Long> entry : playtimePerAlias.entrySet()) {
            String world = entry.getKey();
            long time = entry.getValue();
            if (time > longest) {
                longest = time;
                theWorld = world;
            }
        }

        double percentage = longest * 100.0 / total;

        return theWorld + " (" + FormatUtils.cutDecimals(percentage) + "%)";
    }
}