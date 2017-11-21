package main.java.com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.data.analysis.GeolocationPart;
import main.java.com.djrapitops.plan.data.analysis.JoinInfoPart;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class PlayersTableCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private PlayersTableCreator() {
        throw new IllegalStateException("Utility class");
    }

    @Deprecated
    public static String createTable(List<UserInfo> userInfo, JoinInfoPart joinInfoPart, GeolocationPart geolocationPart) {
        if (userInfo.isEmpty()) {
            return Html.TABLELINE_PLAYERS.parse("<b>No Players</b>", "", "", "", "", "", "", "", "", "");
        }

        StringBuilder html = new StringBuilder();

        Map<UUID, List<Session>> sessions = joinInfoPart.getSessions();
        Map<UUID, String> geoLocations = geolocationPart.getMostCommonGeoLocations();

        long now = MiscUtils.getTime();

        int i = 0;
        for (UserInfo user : userInfo) {
            if (i >= 750) {
                break;
            }

            try {
                UUID uuid = user.getUuid();
                boolean isBanned = user.isBanned();
                List<Session> userSessions = sessions.get(uuid);
                int loginTimes = 0;
                long playtime = 0;
                if (userSessions != null) {
                    loginTimes = userSessions.size();
                    playtime = AnalysisUtils.getTotalPlaytime(userSessions);
                }
                boolean isUnknown = loginTimes == 1;
                long registered = user.getRegistered();

                boolean isActive = AnalysisUtils.isActive(now, user.getLastSeen(), playtime, loginTimes);

                long lastSeen = user.getLastSeen();

                String activityString = getActivityString(isBanned, isUnknown, isActive);


                String geoLocation = geoLocations.get(uuid);
                if (geoLocation == null) {
                    geoLocation = "Not Known";
                }
                html.append(Html.TABLELINE_PLAYERS.parse(
                        Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(user.getName()), user.getName()),
                        activityString,
                        String.valueOf(playtime), FormatUtils.formatTimeAmount(playtime),
                        String.valueOf(loginTimes),
                        String.valueOf(registered), FormatUtils.formatTimeStampYear(registered),
                        String.valueOf(lastSeen), lastSeen != 0 ? FormatUtils.formatTimeStamp(lastSeen) : "-",
                        String.valueOf(geoLocation)
                ));
            } catch (NullPointerException e) {
                if (Settings.DEV_MODE.isTrue()) {
                    Log.toLog(PlayersTableCreator.class.getName(), e);
                }
            }

            i++;
        }

        return html.toString();
    }

    private static String getActivityString(boolean isBanned, boolean isUnknown, boolean isActive) {
        if (isBanned) {
            return "Banned";
        }

        if (isUnknown) {
            return "Unknown";
        }

        return isActive ? "Active" : "Inactive";
    }

    public static String createTable(List<PlayerProfile> players) {
        if (players.isEmpty()) {
            return Html.TABLELINE_PLAYERS.parse("<b>No Players</b>", "", "", "", "", "", "", "", "", "");
        }

        StringBuilder html = new StringBuilder();

        long now = MiscUtils.getTime();
        UUID serverUUID = MiscUtils.getIPlan().getServerUuid();

        int i = 0;
        for (PlayerProfile profile : players) {
            if (i >= 2000) {
                break;
            }

            try {
                boolean isBanned = profile.isBanned();
                long loginTimes = profile.getSessionCount(serverUUID);
                long playtime = profile.getPlaytime(serverUUID);
                boolean isUnknown = loginTimes <= 1;
                long registered = profile.getRegistered();

                boolean isActive = AnalysisUtils.isActive(now, profile.getLastSeen(), playtime, loginTimes);

                long lastSeen = profile.getLastSeen();

                String activityString = getActivityString(isBanned, isUnknown, isActive);

                String geoLocation = profile.getMostRecentGeoInfo().getGeolocation();
                html.append(Html.TABLELINE_PLAYERS.parse(
                        Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(profile.getName()), profile.getName()),
                        activityString,
                        String.valueOf(playtime), FormatUtils.formatTimeAmount(playtime),
                        String.valueOf(loginTimes),
                        String.valueOf(registered), FormatUtils.formatTimeStampYear(registered),
                        String.valueOf(lastSeen), lastSeen != 0 ? FormatUtils.formatTimeStamp(lastSeen) : "-",
                        String.valueOf(geoLocation)
                ));
            } catch (NullPointerException e) {
                if (Settings.DEV_MODE.isTrue()) {
                    Log.toLog(PlayersTableCreator.class.getName(), e);
                }
            }

            i++;
        }

        return html.toString();
    }
}
